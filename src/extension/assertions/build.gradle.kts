plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-assertions"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    implementation("com.jayway.jsonpath:json-path")
    implementation("org.json:json")
    implementation("org.skyscreamer:jsonassert")
}
