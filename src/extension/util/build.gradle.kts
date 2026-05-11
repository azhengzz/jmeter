plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-util"
dependencies {
    api(projects.src.core)
}
