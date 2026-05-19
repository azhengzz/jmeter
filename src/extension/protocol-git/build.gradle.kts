plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-protocol-git"
dependencies {
    api(projects.src.core)
    implementation("org.eclipse.jgit:org.eclipse.jgit")
    implementation("com.github.mwiede:jsch")
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-io:commons-io")
}
