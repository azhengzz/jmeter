plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-functions"
dependencies {
    api(projects.src.core)
    implementation("com.jayway.jsonpath:json-path")
    implementation("org.bouncycastle:bcprov-jdk15on")
    implementation("org.bouncycastle:bcpkix-jdk15on")
    implementation("commons-codec:commons-codec")
    implementation("org.apache.commons:commons-lang3")
}
