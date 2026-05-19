package com.gitee.qa.jmeter.protocol.git.util;

// class的类型为final，防止该类被继承
public final class BranchParameters {

    // 私有构造函数，避免该类被实例化
    private BranchParameters(){}

    public static final String REPO_PATH = "<repo-path>";
    public static final String REPO_PATH_DESC = "本地仓库路径";

    public static final String ACTION = "<branch-action>";
    public static final String ACTION_DESC = "分支动作(目前只支持create).";

    public static final String NAME = "<branch-name>";
    public static final String NAME_DESC = "分支名.";

    public final static class BranchAction{
        public static final String CREATE = "create";
    }

}
