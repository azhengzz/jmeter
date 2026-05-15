plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-extractor"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
}
