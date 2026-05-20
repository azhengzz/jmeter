plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-protocol-httpud"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    api(projects.src.protocol.http)
    implementation("org.apache.commons:commons-lang3")
}
