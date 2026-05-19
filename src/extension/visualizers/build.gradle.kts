plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-visualizers"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    implementation("org.apache.httpcomponents:httpasyncclient")
    implementation("org.apache.httpcomponents:httpcore-nio")
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-io:commons-io")
}
