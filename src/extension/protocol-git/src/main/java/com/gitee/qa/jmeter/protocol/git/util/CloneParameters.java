package com.gitee.qa.jmeter.protocol.git.util;

// class的类型为final，防止该类被继承
public final class CloneParameters {

    // 私有构造函数，避免该类被实例化
    private CloneParameters(){}

    public static final String BRANCH = "--branch";
    public static final String BRANCH_DESC = "Instead of pointing the newly created HEAD to the branch pointed to by the cloned repository’s HEAD, point to <name> branch instead.";

    public static final String REPOSITORY = "<repository>";
    public static final String REPOSITORY_DESC = "The (possibly remote) repository to clone from.";

    public static final String DIRECTORY = "<directory>";
    public static final String DIRECTORY_DESC = "The name of a new directory to clone into.";
}
