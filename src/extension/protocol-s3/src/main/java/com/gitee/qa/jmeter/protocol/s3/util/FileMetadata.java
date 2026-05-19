package com.gitee.qa.jmeter.protocol.s3.util;

import java.time.Instant;

/**
 * S3文件元数据
 */
public class FileMetadata {
    private String key;
    private Long size;
    private Instant lastModified;
    private String contentType;
    private String eTag;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "key='" + key + '\'' +
                ", size=" + size +
                ", lastModified=" + lastModified +
                ", contentType='" + contentType + '\'' +
                ", eTag='" + eTag + '\'' +
                '}';
    }
}
