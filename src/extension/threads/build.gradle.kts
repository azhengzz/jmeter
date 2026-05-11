plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-threads"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    api(projects.src.protocol.http)
    api(projects.src.extension.util)
}
