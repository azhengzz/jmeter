package com.gitee.qa.jmeter.protocol.s3.sampler;

import com.gitee.qa.jmeter.protocol.s3.config.S3ConfigElement;
import com.gitee.qa.jmeter.protocol.s3.util.S3Action;
import com.gitee.qa.jmeter.protocol.s3.util.S3Arguments;
import com.gitee.qa.jmeter.protocol.s3.util.S3Config;
import com.gitee.qa.jmeter.protocol.s3.util.FileMetadata;
import com.gitee.qa.jmeter.protocol.s3.util.S3ObjectInfo;
import com.gitee.qa.jmeter.protocol.s3.util.S3Result;
import com.gitee.qa.jmeter.protocol.s3.util.S3Service;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.BucketExistsParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.CreateBucketParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.DeleteBucketParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.DeleteFileParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.DownloadFileParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.FileExistsParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.GetFileMetadataParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.ListObjectsParameters;
import com.gitee.qa.jmeter.protocol.s3.util.parameters.UploadFileParameters;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JsonProvider;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3Sampler extends AbstractSampler {

    private static final Logger LOG = LoggerFactory.getLogger(S3Sampler.class);
    // 复用JsonProvider，避免重复创建
    private static final JsonProvider JSON_PROVIDER = Configuration.defaultConfiguration().jsonProvider();

    public static final String UID_NAME = "S3Sampler.uidName";
    public static final String ACTION = "S3Sampler.action"; // $NON-NLS-1$
    public static final String ARGUMENTS = "S3Sampler.Arguments"; // $NON-NLS-1$


    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());

        S3Service s3Service;
        try {
            s3Service = S3ConfigElement.getS3Service(getUidName());
        } catch (Exception ex) {
            res.setSuccessful(false);
            res.setResponseData(String.format("无法根据标识名 %s 获取S3服务，请检查 S3 Connection Configuration 中是否已定义", UID_NAME), "utf-8");
            return res;
        }

        String action = getAction();
        if (S3Action.LIST_ALL_BUCKETS.getCode().equals(action)) {
            res = listAllBucketsSample(res, s3Service);
        } else if (S3Action.CREATE_BUCKET.getCode().equals(action)) {
            res = createBucketsSample(res, s3Service);
        } else if (S3Action.DELETE_BUCKET.getCode().equals(action)) {
            res = deleteBucketsSample(res, s3Service);
        } else if (S3Action.BUCKET_EXISTS.getCode().equals(action)) {
            res = bucketExistsSample(res, s3Service);
        } else if (S3Action.LIST_OBJECTS.getCode().equals(action)) {
            res = listObjectsSample(res, s3Service);
        } else if (S3Action.GET_FILE_METADATA.getCode().equals(action)) {
            res = getFileMetadataSample(res, s3Service);
        } else if (S3Action.FILE_EXISTS.getCode().equals(action)) {
            res = fileExistsSample(res, s3Service);
        } else if (S3Action.UPLOAD_FILE.getCode().equals(action)) {
            res = uploadFileSample(res, s3Service);
        } else if (S3Action.DOWNLOAD_FILE.getCode().equals(action)) {
            res = downloadFileSample(res, s3Service);
        } else if (S3Action.DELETE_FILE.getCode().equals(action)) {
            res = deleteFileSample(res, s3Service);
        } else {
            res.setSuccessful(false);
            res.setResponseData(String.format("参数: %s 有误", ACTION), "utf-8");
            return res;
        }
        return res;
    }

    public String getAction() {
        return getPropertyAsString(ACTION);
    }

    public void setAction(String action) {
        setProperty(ACTION, action);
    }

    public String getUidName() {
        return getPropertyAsString(UID_NAME);
    }

    public void setUidName(String uidName) {
        setProperty(UID_NAME, uidName);
    }

    public S3Arguments getArguments() {
        return (S3Arguments) getProperty(S3Sampler.ARGUMENTS).getObjectValue();

    }

    public Map<String, String> getArgumentsAsMap() {
        S3Arguments arguments = (S3Arguments) getProperty(S3Sampler.ARGUMENTS).getObjectValue();
        return arguments.getArgumentsAsMap();
    }

    public void setArguments(TestElement value) {
        setProperty(new TestElementProperty(S3Sampler.ARGUMENTS, value));
    }


    private void setRequestHeaders(SampleResult res, S3Service s3Service) {
        S3Config s3Config = s3Service.getS3Config();
        res.setRequestHeaders(
            "Endpoint" + ": " + s3Config.getEndpoint() + "\n" +
            "Region" + ": " + s3Config.getRegion() + "\n" +
            "AccessKeyId" + ": " + s3Config.getAccessKeyId() + "\n" +
            "SecretAccessKey" + ": " + maskSensitiveInfo(s3Config.getSecretAccessKey()) + "\n"
        );
    }

    /**
     * 脱敏处理敏感信息
     */
    private String maskSensitiveInfo(String value) {
        if (value == null || value.length() <= 4) {
            return "***";
        }
        return value.substring(0, 4) + "***";
    }

    /**
     * 设置成功的响应
     */
    private void setSuccessResponse(SampleResult res, S3Result<?> result, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("errorMessage", null);
        response.put("data", data);

        String jsonData = JSON_PROVIDER.toJson(response);
        res.setResponseCode(result.getSdkHttpResponseStatusCode());
        res.setResponseMessage(result.getSdkHttpResponseStatusText());
        res.setResponseHeaders("");
        res.setResponseData(jsonData, "utf-8");
        res.setSuccessful(true);
    }

    /**
     * 设置失败的响应
     */
    private void setFailureResponse(SampleResult res, S3Result<?> result) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorMessage", result.getErrorMessage());
        response.put("data", null);

        String jsonData = JSON_PROVIDER.toJson(response);
        res.setSuccessful(false);
        res.setResponseCode("500");
        res.setResponseMessage("Internal Server Error");
        res.setResponseData(jsonData, "utf-8");
    }

    /**
     * 设置失败的响应（带自定义数据）
     */
    private void setFailureResponseWithData(SampleResult res, S3Result<?> result, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorMessage", result.getErrorMessage());
        response.put("data", data);

        String jsonData = JSON_PROVIDER.toJson(response);
        res.setSuccessful(false);
        res.setResponseCode("500");
        res.setResponseMessage("Internal Server Error");
        res.setResponseData(jsonData, "utf-8");
    }


    public SampleResult listAllBucketsSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        res.setSamplerData(
            "Action" + ": " + S3Action.LIST_ALL_BUCKETS.getCode() + "\n"
        );

        S3Result<List<String>> result = s3Service.listBuckets();
        res.sampleEnd();

        if (result.isSuccess()) {
            List<String> buckets = result.getData();
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": {
            //         "buckets": ["bucket1", "bucket2", "bucket3"],
            //         "count": 3
            //     }
            // }
            Map<String, Object> data = new HashMap<>();
            data.put("buckets", buckets);
            data.put("count", buckets.size());
            setSuccessResponse(res, result, data);
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": null
            // }
            setFailureResponse(res, result);
        }
        return res;
    }


    public SampleResult createBucketsSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(CreateBucketParameters.BUCKET_NAME);
        res.setSamplerData(
            "Action" + ": " + S3Action.CREATE_BUCKET.getCode() + "\n" +
            CreateBucketParameters.BUCKET_NAME + ": " + bucketName + "\n"
        );

        S3Result<Boolean> result = s3Service.createBucket(bucketName);
        res.sampleEnd();

        if (result.isSuccess()) {
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": true
            // }
            setSuccessResponse(res, result, true);
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": false
            // }
            setFailureResponseWithData(res, result, false);
        }
        return res;
    }

    public SampleResult deleteBucketsSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(DeleteBucketParameters.BUCKET_NAME);
        res.setSamplerData(
                "Action" + ": " + S3Action.DELETE_BUCKET.getCode() + "\n" +
                        DeleteBucketParameters.BUCKET_NAME + ": " + bucketName + "\n"
        );

        S3Result<Boolean> result = s3Service.deleteBucket(bucketName);
        res.sampleEnd();

        if (result.isSuccess()) {
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": true
            // }
            setSuccessResponse(res, result, true);
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": false
            // }
            setFailureResponseWithData(res, result, false);
        }
        return res;
    }

    public SampleResult bucketExistsSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(BucketExistsParameters.BUCKET_NAME);
        res.setSamplerData(
                "Action" + ": " + S3Action.BUCKET_EXISTS.getCode() + "\n" +
                        BucketExistsParameters.BUCKET_NAME + ": " + bucketName + "\n"
        );

        S3Result<Boolean> result = s3Service.bucketExists(bucketName);
        res.sampleEnd();

        HashMap<String, Boolean> data = new HashMap<>();
        data.put("exists", result.getData());
        // 构造JSON响应，示例：
        // {
        //     "success": true,
        //     "errorMessage": null,
        //     "data": {
        //         "exists": true,
        //     }
        // }
        setSuccessResponse(res, result, data);
        return res;
    }

    public SampleResult listObjectsSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(ListObjectsParameters.BUCKET_NAME);
        String prefix = this.getArgumentsAsMap().get(ListObjectsParameters.PREFIX);
        res.setSamplerData(
                "Action" + ": " + S3Action.LIST_OBJECTS.getCode() + "\n" +
                ListObjectsParameters.BUCKET_NAME + ": " + bucketName + "\n" +
                ListObjectsParameters.PREFIX + ": " + prefix + "\n"
        );

        S3Result<List<S3ObjectInfo>> result;
        if (prefix != null && !prefix.isEmpty()) {
            result = s3Service.listObjectsByPrefix(bucketName, prefix);
        } else {
            result = s3Service.listObjects(bucketName);
        }
        res.sampleEnd();

        if (result.isSuccess()) {
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": {
            //         "bucketName": "my-bucket",
            //         "prefix": "path/",
            //         "objects": [
            //             {"key": "file1.txt", "size": 1024, "lastModified": "2024-01-01T00:00:00Z", "eTag": "abc123"},
            //             {"key": "file2.txt", "size": 2048, "lastModified": "2024-01-02T00:00:00Z", "eTag": "def456"}
            //         ],
            //         "count": 2
            //     }
            // }
            List<S3ObjectInfo> objectList = result.getData();
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", bucketName);
            data.put("prefix", prefix != null ? prefix : "");
            data.put("objects", objectList);
            data.put("count", objectList.size());
            setSuccessResponse(res, result, data);
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": null
            // }
            setFailureResponse(res, result);
        }
        return res;
    }

    public SampleResult getFileMetadataSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(GetFileMetadataParameters.BUCKET_NAME);
        String key = this.getArgumentsAsMap().get(GetFileMetadataParameters.KEY);
        res.setSamplerData(
                "Action" + ": " + S3Action.GET_FILE_METADATA.getCode() + "\n" +
                GetFileMetadataParameters.BUCKET_NAME + ": " + bucketName + "\n" +
                GetFileMetadataParameters.KEY + ": " + key + "\n"
        );

        S3Result<FileMetadata> result = s3Service.getFileMetadata(bucketName, key);
        res.sampleEnd();

        if (result.isSuccess()) {
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": {
            //         "key": "path/to/file.txt",
            //         "size": 1024,
            //         "lastModified": "2024-01-01T00:00:00Z",
            //         "contentType": "text/plain",
            //         "eTag": "abc123"
            //     }
            // }
            FileMetadata metadata = result.getData();
            Map<String, Object> data = new HashMap<>();
            data.put("key", metadata.getKey());
            data.put("size", metadata.getSize());
            data.put("lastModified", metadata.getLastModified());
            data.put("contentType", metadata.getContentType());
            data.put("eTag", metadata.getETag());
            setSuccessResponse(res, result, data);
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": null
            // }
            setFailureResponse(res, result);
        }
        return res;
    }

    public SampleResult fileExistsSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(FileExistsParameters.BUCKET_NAME);
        String key = this.getArgumentsAsMap().get(FileExistsParameters.KEY);
        res.setSamplerData(
                "Action" + ": " + S3Action.FILE_EXISTS.getCode() + "\n" +
                FileExistsParameters.BUCKET_NAME + ": " + bucketName + "\n" +
                FileExistsParameters.KEY + ": " + key + "\n"
        );

        S3Result<Boolean> result = s3Service.fileExists(bucketName, key);
        res.sampleEnd();

        if (result.isSuccess()) {
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": {
            //         "exists": true
            //     }
            // }
            HashMap<String, Boolean> data = new HashMap<>();
            data.put("exists", result.getData());
            setSuccessResponse(res, result, data);
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": false
            // }
            setFailureResponseWithData(res, result, false);
        }
        return res;
    }

    public SampleResult uploadFileSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(UploadFileParameters.BUCKET_NAME);
        String key = this.getArgumentsAsMap().get(UploadFileParameters.KEY);
        String filePath = this.getArgumentsAsMap().get(UploadFileParameters.FILE_PATH);
        res.setSamplerData(
                "Action" + ": " + S3Action.UPLOAD_FILE.getCode() + "\n" +
                UploadFileParameters.BUCKET_NAME + ": " + bucketName + "\n" +
                UploadFileParameters.KEY + ": " + key + "\n" +
                UploadFileParameters.FILE_PATH + ": " + filePath + "\n"
        );

        S3Result<Boolean> result = s3Service.uploadFile(bucketName, key, filePath);
        res.sampleEnd();

        if (result.isSuccess()) {
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": {
            //         "uploaded": true,
            //         "bucketName": "my-bucket",
            //         "key": "path/to/file.txt",
            //         "filePath": "local path/file.txt"
            //     }
            // }
            Map<String, Object> data = new HashMap<>();
            data.put("uploaded", true);
            data.put("bucketName", bucketName);
            data.put("key", key);
            data.put("filePath", filePath);
            // 获取文件大小用于性能统计
            File file = new File(filePath);
            if (file.exists()) {
                long fileSize = file.length();
                data.put("fileSize", fileSize);
            }
            setSuccessResponse(res, result, data);
            // 设置发送字节数（上传文件大小）
            if (file.exists()) {
                res.setSentBytes(file.length());
            }
        } else {
            setFailureResponseWithData(res, result, false);
        }
        return res;
    }

    public SampleResult downloadFileSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(DownloadFileParameters.BUCKET_NAME);
        String key = this.getArgumentsAsMap().get(DownloadFileParameters.KEY);
        String downloadDir = this.getArgumentsAsMap().get(DownloadFileParameters.DOWNLOAD_DIR);
        String downloadFileName = this.getArgumentsAsMap().get(DownloadFileParameters.DOWNLOAD_FILE_NAME);
        String discardFileStr = this.getArgumentsAsMap().get(DownloadFileParameters.DISCARD_FILE);
        boolean discardFile = discardFileStr != null && discardFileStr.equalsIgnoreCase("true");

        res.setSamplerData(
                "Action" + ": " + S3Action.DOWNLOAD_FILE.getCode() + "\n" +
                DownloadFileParameters.BUCKET_NAME + ": " + bucketName + "\n" +
                DownloadFileParameters.KEY + ": " + key + "\n" +
                DownloadFileParameters.DOWNLOAD_DIR + ": " + downloadDir + "\n" +
                DownloadFileParameters.DOWNLOAD_FILE_NAME + ": " + downloadFileName + "\n" +
                DownloadFileParameters.DISCARD_FILE + ": " + discardFile + "\n"
        );

        S3Result<String> result = s3Service.downloadFile(bucketName, key, downloadDir, downloadFileName, discardFile);
        res.sampleEnd();

        if (result.isSuccess()) {
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", bucketName);
            data.put("key", key);

            if (discardFile) {
                // 丢弃模式：返回下载的字节数
                // 构造JSON响应，示例：
                // {
                //     "success": true,
                //     "errorMessage": null,
                //     "data": {
                //         "downloadedBytes": 1024000,
                //         "bucketName": "my-bucket",
                //         "key": "path/to/file.txt",
                //         "discarded": true
                //     }
                // }
                long downloadedBytes = Long.parseLong(result.getData());
                data.put("downloadedBytes", downloadedBytes);
                data.put("discarded", true);
                data.put("fileSize", downloadedBytes);
                setSuccessResponse(res, result, data);
                // 设置接收字节数（模拟带宽IO）
                res.setBytes(downloadedBytes);
            } else {
                // 保存模式：返回文件路径
                // 构造JSON响应，示例：
                // {
                //     "success": true,
                //     "errorMessage": null,
                //     "data": {
                //         "filePath": "/path/to/downloaded/file.txt",
                //         "bucketName": "my-bucket",
                //         "key": "path/to/file.txt"
                //     }
                // }
                String filePath = result.getData();
                data.put("filePath", filePath);
                data.put("discarded", false);
                // 获取下载文件大小用于性能统计
                File file = new File(filePath);
                if (file.exists()) {
                    long fileSize = file.length();
                    data.put("fileSize", fileSize);
                    // 设置接收字节数（下载文件大小）
                    res.setBytes(fileSize);
                }
                setSuccessResponse(res, result, data);
            }
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": null
            // }
            setFailureResponse(res, result);
        }
        return res;
    }

    public SampleResult deleteFileSample(SampleResult res, S3Service s3Service) {
        res.sampleStart();

        // 设置请求头 -> res.setRequestHeaders
        this.setRequestHeaders(res, s3Service);
        String bucketName = this.getArgumentsAsMap().get(DeleteFileParameters.BUCKET_NAME);
        String key = this.getArgumentsAsMap().get(DeleteFileParameters.KEY);
        res.setSamplerData(
                "Action" + ": " + S3Action.DELETE_FILE.getCode() + "\n" +
                DeleteFileParameters.BUCKET_NAME + ": " + bucketName + "\n" +
                DeleteFileParameters.KEY + ": " + key + "\n"
        );

        S3Result<Boolean> result = s3Service.deleteFile(bucketName, key);
        res.sampleEnd();

        if (result.isSuccess()) {
            // 构造JSON响应，示例：
            // {
            //     "success": true,
            //     "errorMessage": null,
            //     "data": true
            // }
            setSuccessResponse(res, result, true);
        } else {
            // 构造JSON响应，示例：
            // {
            //     "success": false,
            //     "errorMessage": "错误信息",
            //     "data": false
            // }
            setFailureResponseWithData(res, result, false);
        }
        return res;
    }


}
