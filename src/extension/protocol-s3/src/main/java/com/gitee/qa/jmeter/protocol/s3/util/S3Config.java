package com.gitee.qa.jmeter.protocol.s3.util;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.util.UUID;

/**
 * S3客户端配置类
 * 用于创建和配置S3客户端
 */
public class S3Config {

    private final String endpoint;
    private final String region;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final boolean pathStyleAccess;


    public S3Config(String endpoint, String region, String accessKeyId, String secretAccessKey) {
        this(endpoint, region, accessKeyId, secretAccessKey, true);
    }

    public S3Config(String endpoint, String region, String accessKeyId, String secretAccessKey, boolean pathStyleAccess) {
        this.endpoint = endpoint;
        if (StringUtils.isEmpty(region)) {
            this.region = "region-" + UUID.randomUUID().toString().substring(0, 8);
        } else {
            this.region = region;
        }
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.pathStyleAccess = pathStyleAccess;
    }

    /**
     * 创建S3客户端
     */
    public S3Client createS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleAccess)
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(s3Config)
                .build();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRegion() {
        return region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }
}
