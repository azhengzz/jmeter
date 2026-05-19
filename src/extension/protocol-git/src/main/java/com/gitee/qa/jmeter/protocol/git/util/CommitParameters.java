package com.gitee.qa.jmeter.protocol.git.util;

// class的类型为final，防止该类被继承
public final class CommitParameters {

    // 私有构造函数，避免该类被实例化
    private CommitParameters(){}

    public static final String REPO_PATH = "<repo-path>";
    public static final String REPO_PATH_DESC = "本地仓库路径";

    public static final String MESSAGE = "--message";
    public static final String MESSAGE_DESC = "The commit message.";

    public static final String AUTHOR_NAME = "--author-name";
    public static final String AUTHOR_NAME_DESC = "The committer name.";

    public static final String AUTHOR_EMAIL = "--author-email";
    public static final String AUTHOR_EMAIL_DESC = "The committer email.";
}
