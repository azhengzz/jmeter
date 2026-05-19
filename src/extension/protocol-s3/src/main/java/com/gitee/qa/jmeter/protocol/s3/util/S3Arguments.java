package com.gitee.qa.jmeter.protocol.s3.util;

import com.gitee.qa.jmeter.protocol.s3.util.parameters.*;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.PropertyIterator;

import java.util.LinkedHashMap;
import java.util.Map;

public class S3Arguments extends Arguments {

    public S3Arguments(){
        super();
    }

    public Map<String, String[]> getS3ArgumentsAsMap() {
        PropertyIterator iter = getArguments().iterator();
        Map<String, String[]> argMap = new LinkedHashMap<>();

        while (iter.hasNext()) {
            S3Argument arg = (S3Argument) iter.next().getObjectValue();
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), new String[]{arg.getValue(), arg.getDescription(), String.valueOf(arg.isRequired()), String.valueOf(arg.isNotNull())});
            }
        }
        return argMap;
    }

    public S3Arguments getListAllBucketsParameters() {
        S3Arguments params = new S3Arguments();
        return params;
    }

    public S3Arguments getCreateBucketParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                CreateBucketParameters.BUCKET_NAME,
                "",
                CreateBucketParameters.BUCKET_NAME_DESC,
                CreateBucketParameters.BUCKET_NAME_NOTNULL,
                CreateBucketParameters.BUCKET_NAME_REQUIRED
        ));
        return params;
    }

    public S3Arguments getDeleteBucketParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                DeleteBucketParameters.BUCKET_NAME,
                "",
                DeleteBucketParameters.BUCKET_NAME_DESC,
                DeleteBucketParameters.BUCKET_NAME_NOTNULL,
                DeleteBucketParameters.BUCKET_NAME_REQUIRED
        ));
        return params;
    }

    public S3Arguments getBucketExistsParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                BucketExistsParameters.BUCKET_NAME,
                "",
                BucketExistsParameters.BUCKET_NAME_DESC,
                BucketExistsParameters.BUCKET_NAME_NOTNULL,
                BucketExistsParameters.BUCKET_NAME_REQUIRED
        ));
        return params;
    }

    public S3Arguments getListObjectsParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                ListObjectsParameters.BUCKET_NAME,
                "",
                ListObjectsParameters.BUCKET_NAME_DESC,
                ListObjectsParameters.BUCKET_NAME_NOTNULL,
                ListObjectsParameters.BUCKET_NAME_REQUIRED
        ));
        params.addArgument(new S3Argument(
                ListObjectsParameters.PREFIX,
                "",
                ListObjectsParameters.PREFIX_DESC,
                ListObjectsParameters.PREFIX_NOTNULL,
                ListObjectsParameters.PREFIX_REQUIRED
        ));
        return params;
    }

    public S3Arguments getFileMetadataParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                GetFileMetadataParameters.BUCKET_NAME,
                "",
                GetFileMetadataParameters.BUCKET_NAME_DESC,
                GetFileMetadataParameters.BUCKET_NAME_NOTNULL,
                GetFileMetadataParameters.BUCKET_NAME_REQUIRED
        ));
        params.addArgument(new S3Argument(
                GetFileMetadataParameters.KEY,
                "",
                GetFileMetadataParameters.KEY_DESC,
                GetFileMetadataParameters.KEY_NOTNULL,
                GetFileMetadataParameters.KEY_REQUIRED
        ));
        return params;
    }

    public S3Arguments getUploadFileParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                UploadFileParameters.BUCKET_NAME,
                "",
                UploadFileParameters.BUCKET_NAME_DESC,
                UploadFileParameters.BUCKET_NAME_NOTNULL,
                UploadFileParameters.BUCKET_NAME_REQUIRED
        ));
        params.addArgument(new S3Argument(
                UploadFileParameters.KEY,
                "",
                UploadFileParameters.KEY_DESC,
                UploadFileParameters.KEY_NOTNULL,
                UploadFileParameters.KEY_REQUIRED
        ));
        params.addArgument(new S3Argument(
                UploadFileParameters.FILE_PATH,
                "",
                UploadFileParameters.FILE_PATH_DESC,
                UploadFileParameters.FILE_PATH_NOTNULL,
                UploadFileParameters.FILE_PATH_REQUIRED
        ));
        return params;
    }

    public S3Arguments getDownloadFileParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                DownloadFileParameters.BUCKET_NAME,
                "",
                DownloadFileParameters.BUCKET_NAME_DESC,
                DownloadFileParameters.BUCKET_NAME_NOTNULL,
                DownloadFileParameters.BUCKET_NAME_REQUIRED
        ));
        params.addArgument(new S3Argument(
                DownloadFileParameters.KEY,
                "",
                DownloadFileParameters.KEY_DESC,
                DownloadFileParameters.KEY_NOTNULL,
                DownloadFileParameters.KEY_REQUIRED
        ));
        params.addArgument(new S3Argument(
                DownloadFileParameters.DOWNLOAD_DIR,
                "",
                DownloadFileParameters.DOWNLOAD_DIR_DESC,
                DownloadFileParameters.DOWNLOAD_DIR_NOTNULL,
                DownloadFileParameters.DOWNLOAD_DIR_REQUIRED
        ));
        params.addArgument(new S3Argument(
                DownloadFileParameters.DOWNLOAD_FILE_NAME,
                "",
                DownloadFileParameters.DOWNLOAD_FILE_NAME_DESC,
                DownloadFileParameters.DOWNLOAD_FILE_NAME_NOTNULL,
                DownloadFileParameters.DOWNLOAD_FILE_NAME_REQUIRED
        ));
        params.addArgument(new S3Argument(
                DownloadFileParameters.DISCARD_FILE,
                DownloadFileParameters.DISCARD_FILE_DEFAULT_VALUE,
                DownloadFileParameters.DISCARD_FILE_DESC,
                DownloadFileParameters.DISCARD_FILE_NOTNULL,
                DownloadFileParameters.DISCARD_FILE_REQUIRED
        ));
        return params;
    }

    public S3Arguments getDeleteFileParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                DeleteFileParameters.BUCKET_NAME,
                "",
                DeleteFileParameters.BUCKET_NAME_DESC,
                DeleteFileParameters.BUCKET_NAME_NOTNULL,
                DeleteFileParameters.BUCKET_NAME_REQUIRED
        ));
        params.addArgument(new S3Argument(
                DeleteFileParameters.KEY,
                "",
                DeleteFileParameters.KEY_DESC,
                DeleteFileParameters.KEY_NOTNULL,
                DeleteFileParameters.KEY_REQUIRED
        ));
        return params;
    }

    public S3Arguments getFileExistsParameters() {
        S3Arguments params = new S3Arguments();
        params.addArgument(new S3Argument(
                FileExistsParameters.BUCKET_NAME,
                "",
                FileExistsParameters.BUCKET_NAME_DESC,
                FileExistsParameters.BUCKET_NAME_NOTNULL,
                FileExistsParameters.BUCKET_NAME_REQUIRED
        ));
        params.addArgument(new S3Argument(
                FileExistsParameters.KEY,
                "",
                FileExistsParameters.KEY_DESC,
                FileExistsParameters.KEY_NOTNULL,
                FileExistsParameters.KEY_REQUIRED
        ));
        return params;
    }


}
