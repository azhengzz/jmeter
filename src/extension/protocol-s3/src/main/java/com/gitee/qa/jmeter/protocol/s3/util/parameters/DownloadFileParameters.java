package com.gitee.qa.jmeter.protocol.s3.util.parameters;

public final class DownloadFileParameters {

    // 私有构造函数，避免该类被实例化
    private DownloadFileParameters(){}

    public static final String BUCKET_NAME = "<bucket-name>";
    public static final String BUCKET_NAME_DESC = "存储桶名称";
    public static final boolean BUCKET_NAME_NOTNULL = true;
    public static final boolean BUCKET_NAME_REQUIRED = true;

    public static final String KEY = "<key>";
    public static final String KEY_DESC = "对象键（S3中的文件路径）";
    public static final boolean KEY_NOTNULL = true;
    public static final boolean KEY_REQUIRED = true;

    public static final String DOWNLOAD_DIR = "<download-dir>";
    public static final String DOWNLOAD_DIR_DESC = "本地下载目录";
    public static final boolean DOWNLOAD_DIR_NOTNULL = true;
    public static final boolean DOWNLOAD_DIR_REQUIRED = true;

    public static final String DOWNLOAD_FILE_NAME = "<download-file-name>";
    public static final String DOWNLOAD_FILE_NAME_DESC = "保存文件名，如果为空则使用原文件名";
    public static final boolean DOWNLOAD_FILE_NAME_NOTNULL = false;
    public static final boolean DOWNLOAD_FILE_NAME_REQUIRED = false;

    public static final String DISCARD_FILE = "<discard-file>";
    public static final String DISCARD_FILE_DEFAULT_VALUE = "false";
    public static final String DISCARD_FILE_DESC = "是否丢弃文件（true: 下载后丢弃不写磁盘；false: 保存到本地）";
    public static final boolean DISCARD_FILE_NOTNULL = false;
    public static final boolean DISCARD_FILE_REQUIRED = false;

}