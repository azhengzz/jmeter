package com.gitee.qa.jmeter.protocol.git.util;

// class的类型为final，防止该类被继承
public final class PullParameters {

    // 私有构造函数，避免该类被实例化
    private PullParameters(){}

    public static final String REPO_PATH = "<repo-path>";
    public static final String REPO_PATH_DESC = "本地仓库路径";
}
