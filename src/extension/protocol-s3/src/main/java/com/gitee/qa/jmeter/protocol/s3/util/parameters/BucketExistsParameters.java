package com.gitee.qa.jmeter.protocol.s3.util.parameters;

public final class BucketExistsParameters {

    // 私有构造函数，避免该类被实例化
    private BucketExistsParameters(){}

    public static final String BUCKET_NAME = "<bucket-name>";
    public static final String BUCKET_NAME_DESC = "存储桶名称";
    public static final boolean BUCKET_NAME_NOTNULL = true;
    public static final boolean BUCKET_NAME_REQUIRED = true;

}
