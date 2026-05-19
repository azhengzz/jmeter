package com.gitee.qa.jmeter.protocol.s3.util;

import com.gitee.qa.jmeter.protocol.s3.util.parameters.DownloadFileParameters;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * S3服务类
 * 提供文件上传、下载、更新、查看等操作
 */
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Config s3Config;

    public S3Service(S3Config s3Config) {
        this.s3Client = s3Config.createS3Client();
        this.s3Config = s3Config;
    }

    /**
     * 上传文件到S3
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @param filePath    本地文件路径
     * @return S3Result包含上传是否成功、错误信息
     */
    public S3Result<Boolean> uploadFile(String bucketName, String key, String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return S3Result.failure(new IllegalArgumentException("文件不存在: " + filePath));
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            logger.info("文件上传成功: bucket={}, key={}, file={}", bucketName, key, filePath);
            return S3Result.success(true, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 上传字节内容到S3
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @param content     文件内容
     * @param contentType 内容类型
     * @return 上传是否成功
     */
    public boolean uploadBytes(String bucketName, String key, byte[] content, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            logger.info("字节内容上传成功: bucket={}, key={}, size={}", bucketName, key, content.length);
            return true;

        } catch (Exception e) {
            logger.error("字节内容上传失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 下载文件从S3
     *
     * @param bucketName     存储桶名称
     * @param key            对象键（文件路径）
     * @param downloadDir    下载目录
     * @param downloadFileName 自定义文件名（可选，为空则从key中提取）
     * @param discardFile    是否丢弃文件（true: 读取后丢弃不写磁盘; false: 保存到本地）
     * @return S3Result包含下载的文件路径/字节数、成功状态和错误信息
     */
    public S3Result<String> downloadFile(String bucketName, String key, String downloadDir, String downloadFileName, boolean discardFile) {
        // 参数校验
        if (StringUtils.isAnyEmpty(bucketName, key)) {
            return S3Result.failure(new IllegalArgumentException(
                    DownloadFileParameters.BUCKET_NAME + "、" +
                            DownloadFileParameters.KEY + "参数不能为空，请检查"));
        }

        if (!discardFile && StringUtils.isEmpty(downloadDir)) {
            return S3Result.failure(new IllegalArgumentException(
                    DownloadFileParameters.DOWNLOAD_DIR + "参数不能为空，请检查"));
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            if (discardFile) {
                // 丢弃模式：读取流后直接丢弃，不写入磁盘
                try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
                    byte[] buffer = new byte[65536];  // 64KB buffer，优化S3下载性能
                    long totalBytes = 0;
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                        // 读取后直接丢弃，不写入磁盘
                    }

                    logger.info("文件下载并丢弃成功: bucket={}, key={}, bytes={}", bucketName, key, totalBytes);
                    return S3Result.success(String.valueOf(totalBytes), null, null);
                }
            } else {
                // 保存模式：写入本地磁盘
                // 确保下载目录存在
                Path dirPath = Paths.get(downloadDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }

                // 确定文件名
                String fileName;
                if (StringUtils.isEmpty(downloadFileName)) {
                    fileName = extractFileName(key);
                } else {
                    fileName = downloadFileName;
                }

                Path outputPath = dirPath.resolve(fileName);

                // 如果目标文件已存在，先删除
                if (Files.exists(outputPath)) {
                    try {
                        Files.delete(outputPath);
                    } catch (IOException e) {
                        return S3Result.failure(new IOException("文件已存在且无法删除: " + outputPath + ", " + e.getMessage(), e));
                    }
                }

                GetObjectResponse response = s3Client.getObject(getObjectRequest, outputPath);
                logger.info("文件下载成功: bucket={}, key={}, savePath={}", bucketName, key, outputPath);
                return S3Result.success(outputPath.toString(), response.sdkHttpResponse(), response.responseMetadata());
            }

        } catch (Exception e) {
            logger.error("文件下载失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 下载文件内容为字节数组
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @return 文件内容字节数组，失败返回null
     */
    public byte[] downloadFileAsBytes(String bucketName, String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (Exception e) {
            logger.error("下载文件字节内容失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 更新S3中的文件（覆盖上传）
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @param filePath    本地文件路径
     * @return 更新是否成功
     */
//    public boolean updateFile(String bucketName, String key, String filePath) {
//        logger.info("更新文件: bucket={}, key={}, file={}", bucketName, key, filePath);
//        // 上传操作本身就是覆盖的，所以直接调用uploadFile
//        return uploadFile(bucketName, key, filePath);
//    }

    /**
     * 更新S3中的文件内容
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @param content     新的文件内容
     * @param contentType 内容类型
     * @return 更新是否成功
     */
    public boolean updateFileContent(String bucketName, String key, byte[] content, String contentType) {
        logger.info("更新文件内容: bucket={}, key={}, size={}", bucketName, key, content.length);
        return uploadBytes(bucketName, key, content, contentType);
    }

    /**
     * 删除S3中的文件
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @return S3Result包含删除是否成功、错误信息
     */
    public S3Result<Boolean> deleteFile(String bucketName, String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            DeleteObjectResponse response = s3Client.deleteObject(deleteObjectRequest);
            logger.info("文件删除成功: bucket={}, key={}", bucketName, key);
            return S3Result.success(true, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            logger.error("文件删除失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @return S3Result包含文件是否存在、成功状态和错误信息
     */
    public S3Result<Boolean> fileExists(String bucketName, String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return S3Result.success(true, response.sdkHttpResponse(), response.responseMetadata());

        } catch (NoSuchKeyException e) {
            return S3Result.success(false);
        } catch (Exception e) {
            logger.error("检查文件存在性失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 获取文件元数据
     *
     * @param bucketName  存储桶名称
     * @param key         对象键（文件路径）
     * @return S3Result包含文件元数据、成功状态和错误信息
     */
    public S3Result<FileMetadata> getFileMetadata(String bucketName, String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);

            FileMetadata metadata = new FileMetadata();
            metadata.setKey(key);
            metadata.setSize(response.contentLength());
            metadata.setLastModified(response.lastModified());
            metadata.setContentType(response.contentType());
            metadata.setETag(response.eTag());

            return S3Result.success(metadata, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            return S3Result.failure(e);
        }
    }

    /**
     * 列出存储桶中的所有对象
     *
     * @param bucketName  存储桶名称
     * @return S3Result包含对象列表、成功状态和错误信息
     */
    public S3Result<List<S3ObjectInfo>> listObjects(String bucketName) {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

            List<S3ObjectInfo> objectList = response.contents().stream()
                    .map(obj -> {
                        S3ObjectInfo info = new S3ObjectInfo();
                        info.setKey(obj.key());
                        info.setSize(obj.size());
                        info.setLastModified(obj.lastModified());
                        info.setETag(obj.eTag());
                        return info;
                    })
                    .collect(Collectors.toList());

            return S3Result.success(objectList, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            logger.error("列出对象失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 使用前缀列出对象
     *
     * @param bucketName  存储桶名称
     * @param prefix      对象键前缀
     * @return S3Result包含对象列表、成功状态和错误信息
     */
    public S3Result<List<S3ObjectInfo>> listObjectsByPrefix(String bucketName, String prefix) {
        try {
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

            List<S3ObjectInfo> objectList = response.contents().stream()
                    .map(obj -> {
                        S3ObjectInfo info = new S3ObjectInfo();
                        info.setKey(obj.key());
                        info.setSize(obj.size());
                        info.setLastModified(obj.lastModified());
                        info.setETag(obj.eTag());
                        return info;
                    })
                    .collect(Collectors.toList());

            return S3Result.success(objectList, response.sdkHttpResponse(), response.responseMetadata());
        } catch (Exception e) {
//            logger.error("按前缀列出对象失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 创建存储桶
     *
     * @param bucketName  存储桶名称
     * @return S3Result包含创建是否成功、错误信息
     */
    public S3Result<Boolean> createBucket(String bucketName) {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            CreateBucketResponse response = s3Client.createBucket(createBucketRequest);
            logger.info("存储桶创建成功: {}", bucketName);
            return S3Result.success(true, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            logger.error("存储桶创建失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName  存储桶名称
     * @return 存储桶是否存在
     */
    public S3Result<Boolean> bucketExists(String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            HeadBucketResponse response = s3Client.headBucket(headBucketRequest);
            return S3Result.success(true, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            return S3Result.success(false);
        }
    }

    /**
     * 列出所有存储桶
     *
     * @return S3Result包含存储桶名称列表、成功状态和错误信息
     */
    public S3Result<List<String>> listBuckets() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            List<String> bucketNames = response.buckets().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
            logger.info("列出存储桶成功，共{}个", bucketNames.size());
            return S3Result.success(bucketNames, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            logger.error("列出存储桶失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 删除存储桶
     *
     * @param bucketName  存储桶名称
     * @return 删除是否成功
     */
    public S3Result<Boolean> deleteBucket(String bucketName) {
        try {
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            DeleteBucketResponse response = s3Client.deleteBucket(deleteBucketRequest);
            logger.info("存储桶删除成功: {}", bucketName);
            return S3Result.success(true, response.sdkHttpResponse(), response.responseMetadata());

        } catch (Exception e) {
            logger.error("存储桶删除失败: {}", e.getMessage(), e);
            return S3Result.failure(e);
        }
    }

    /**
     * 获取S3配置信息
     *
     * @return S3配置
     */
    public S3Config getS3Config() {
        return s3Config;
    }

    /**
     * 从对象键中提取文件名，兼容 / 和 \ 两种分隔符
     *
     * @param key 对象键（文件路径）
     * @return 文件名
     */
    private String extractFileName(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key不能为空");
        }
        int lastSlash = key.lastIndexOf("/");
        int lastBackslash = key.lastIndexOf("\\");
        int lastSeparator = Math.max(lastSlash, lastBackslash);
        String fileName = lastSeparator >= 0 ? key.substring(lastSeparator + 1) : key;
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("无法从key中提取有效文件名: " + key);
        }
        return fileName;
    }

    /**
     * 关闭S3客户端
     */
    public void close() {
        if (s3Client != null) {
            s3Client.close();
            logger.info("S3客户端已关闭");
        }
    }
}

