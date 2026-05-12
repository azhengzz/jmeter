plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-controller"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    api(projects.src.extension.util)
    implementation("commons-beanutils:commons-beanutils")
    implementation("org.apache.commons:commons-lang3")
}
