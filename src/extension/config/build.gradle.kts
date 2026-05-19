plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-config"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    implementation("com.alibaba:easyexcel")
    implementation("org.apache.commons:commons-lang3")
}
