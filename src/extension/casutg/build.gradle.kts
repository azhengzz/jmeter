plugins {
    id("build-logic.jvm-library")
}
base.archivesName = "jmeter-plugins-gitee-casutg"
dependencies {
    api(projects.src.core)
    api(projects.src.components)
    api(projects.src.protocol.http)
    api(projects.src.extension.util)
    api(projects.src.extension.threads)
    implementation("kg.apc:jmeter-plugins-cmn-jmeter") {
        // jmeter-plugins-cmn-jmeter:0.6 transitively depends on JMeter 2.13 core + avalon-logkit:2.1
        // which conflicts with JMeter 5.6.3's own logkit-2.0 (Logger.log() became final in 2.1).
        // JMeter 5.6.3 already provides all these libs, so exclude them.
        exclude(group = "org.apache.jmeter")
        exclude(group = "avalon-framework")
        exclude(group = "avalon-logkit")
        exclude(group = "excalibur-datasource")
        exclude(group = "log4j")
    }
    implementation("org.apache.commons:commons-lang3")
}
