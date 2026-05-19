package com.gitee.qa.jmeter.protocol.git.util;

// class的类型为final，防止该类被继承
public final class PushParameters {

    // 私有构造函数，避免该类被实例化
    private PushParameters(){}

    public static final String REPO_PATH = "<repo-path>";
    public static final String REPO_PATH_DESC = "本地仓库路径";

    public static final String FORCE = "--force";
    public static final String FORCE_DESC = "强推(true: 强制推送; false: 非强制推送; 默认为false)";

    public static final String REF_SPEC = "<refspec>";
    public static final String REF_SPEC_DESC = "Specify what destination ref to update with what source object. The format of a <refspec> parameter is an optional plus +, followed by the source object <src>, followed by a colon :, followed by the destination ref <dst>.";
}
