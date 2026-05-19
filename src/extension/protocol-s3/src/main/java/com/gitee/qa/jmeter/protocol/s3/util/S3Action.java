package com.gitee.qa.jmeter.protocol.s3.util;

//public class S3Action {
//    public final static String LIST_ALL_BUCKETS = "LIST_ALL_BUCKETS";
//    public final static String LIST_ALL_BUCKETS_DESC = "列出所有存储桶";
//
//    public final static String CREATE_BUCKET = "CREATE_BUCKET";
//    public final static String CREATE_BUCKET_DESC = "创建存储桶";
//
//}

public enum S3Action {
    // ========================= 桶操作 =========================
    LIST_ALL_BUCKETS("LIST_ALL_BUCKETS", "列出所有存储桶"),
    CREATE_BUCKET("CREATE_BUCKET", "创建存储桶"),
    DELETE_BUCKET("DELETE_BUCKET", "删除存储桶"),
    BUCKET_EXISTS("BUCKET_EXISTS", "存储桶是否存在"),
    // ========================= 对象操作 =========================
    LIST_OBJECTS("LIST_OBJECTS", "列出存储桶中的对象（最多返回1000个对象数据）"),
    GET_FILE_METADATA("GET_FILE_METADATA", "获取文件元数据"),
    FILE_EXISTS("FILE_EXISTS", "检查文件是否存在"),
    UPLOAD_FILE("UPLOAD_FILE", "上传文件"),
    DOWNLOAD_FILE("DOWNLOAD_FILE", "下载文件"),
    DELETE_FILE("DELETE_FILE", "删除文件"),
    ;

    private final String code;
    private final String description;

    S3Action(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取对应的description
     *
     * @param code 操作代码
     * @return 对应的description，未找到返回null
     */
    public static String getDescriptionByCode(String code) {
        for (S3Action action : values()) {
            if (action.getCode().equals(code)) {
                return action.getDescription();
            }
        }
        return "";
    }
}

