plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-protocol-s3"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    implementation("com.jayway.jsonpath:json-path")
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:auth")
    implementation("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:sdk-core")
    implementation("org.apache.commons:commons-lang3")
}
