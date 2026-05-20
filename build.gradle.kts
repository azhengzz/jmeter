/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.vlsi.gradle.properties.dsl.lastEditYear
import com.github.vlsi.gradle.release.RepositoryType
import org.ajoberstar.grgit.Grgit

plugins {
    id("build-logic.root-build")
    id("com.github.vlsi.stage-vote-release")
}

fun Project.boolProp(name: String) =
    findProperty(name)
        // Project properties include tasks, extensions, etc, and we want only String properties
        // We don't want to use "task" as a boolean property
        ?.let { it as? String }
        ?.equals("false", ignoreCase = true)?.not()

// Release candidate index
val String.v: String get() = rootProject.extra["$this.version"] as String
version = "jmeter".v + releaseParams.snapshotSuffix

allprojects {
    group = "org.apache.jmeter"
    version = rootProject.version
}

val platformProjects by extra {
    setOf(
        projects.src.bom,
        projects.src.bomThirdparty,
    ).mapTo(mutableSetOf()) { it.dependencyProject }
}

val notPublishedProjects by extra {
    listOf(
        projects.jmeter,
        projects.src,
        projects.src.bshclient,
        projects.src.dist,
        projects.src.distCheck,
        projects.src.examples,
        projects.src.extension,
        projects.src.extension.assertions,
        projects.src.extension.config,
        projects.src.extension.control,
        projects.src.extension.extractor,
        projects.src.extension.giteeFunctions,
        projects.src.extension.protocolGit,
        projects.src.extension.protocolHttpud,
        projects.src.extension.protocolS3,
        projects.src.extension.threads,
        projects.src.extension.util,
        projects.src.extension.visualizers,
        projects.src.extension.casutg,
        projects.src.generator,
        projects.src.licenses,
        projects.src.protocol,
        projects.src.release,
        projects.src.testkit,
        projects.src.testkitWiremock,
        projects.src.testServices,
    ).mapTo(mutableSetOf()) { it.dependencyProject }
}

val publishedProjects by extra {
    allprojects - notPublishedProjects
}

notPublishedProjects.forEach { project ->
    if (project != rootProject) {
        project.plugins.withId("maven-publish") {
            throw IllegalStateException(
                "Project ${project.path} is listed in notPublishedProjects, however it has maven-publish plugin applied. " +
                    "Please remove maven-publish plugin (e.g. replace build-logic.jvm-published-library with build-logic.jvm-library) or " +
                    "move the project to the list of published ones"
            )
        }
    }
}

publishedProjects.forEach {project ->
    project.afterEvaluate {
        if (!pluginManager.hasPlugin("maven-publish")) {
            throw IllegalStateException(
                "Project ${project.path} is listed in publishedProjects, however it misses maven-publish plugin. " +
                    "Please add maven-publish plugin (e.g. replace build-logic.jvm-library with build-logic.jvm-published-library) or " +
                    "move the project to the list of notPublishedProjects"
            )
        }
    }
}

val displayVersion by extra {
    version.toString() +
        if (releaseParams.release.get()) {
            ""
        } else {
            // Append 7 characters of Git commit id for snapshot version
            val grgit: Grgit? by project
            grgit?.let { " " + it.head().abbreviatedId }
        }
}

println("Building JMeter $version")

fun reportsForHumans() = !(System.getenv()["CI"]?.toBoolean() ?: boolProp("CI") ?: false)

val lastEditYear by extra(lastEditYear().toString())


tasks.validateBeforeBuildingReleaseArtifacts {
    dependsOn(tasks.rat)
}

releaseArtifacts {
    fromProject(projects.src.dist.dependencyProject.path)
    previewSite {
        into("rat")
        from(tasks.rat) {
            filteringCharset = "UTF-8"
            // XML is not really interesting for now
            exclude("rat-report.xml")
            // RAT reports have absolute paths, and we don't want to expose them
            filter { str: String -> str.replace(rootDir.absolutePath, "") }
        }
    }
}

releaseParams {
    tlp.set("JMeter")
    releaseTag.set("rel/v${project.version}")
    rcTag.set(rc.map { "v${project.version}-rc$it" })
    svnDist {
        // All the release versions are put under release/jmeter/{source,binary}
        releaseFolder.set("release/jmeter")
        releaseSubfolder.apply {
            put(Regex("_src\\."), "source")
            put(Regex("."), "binaries")
        }
        staleRemovalFilters {
            excludes.add(Regex("release/.*/HEADER\\.html"))
        }
    }
    nexus {
        if (repositoryType.get() == RepositoryType.PROD) {
            // org.apache.jmeter at repository.apache.org
            stagingProfileId.set("4d29c092016673")
        }
    }
}

// ============================================================
// 二次开发插件打包（自动发现 src/extension/ 下所有子模块）
//   ./gradlew listPluginDeps
//   ./gradlew packagePlugins
//   ./gradlew installToJmeter -PtargetJmeter=/path/to/jmeter
// 新增模块只需在 settings.gradle.kts / build.gradle.kts / dist 注册即可自动生效
// 第三方依赖自动检测：扩展模块 classpath - 原版JMeter classpath = 额外依赖
// ============================================================
val extModules: List<String> = file("src/extension").listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").exists() }
    ?.map { "src:extension:${it.name}" }
    ?: emptyList()

// 扩展模块的完整 runtime classpath
val extensionRuntime = configurations.create("extensionRuntime")
extModules.forEach { mod ->
    extensionRuntime.dependencies.add(dependencies.create(project(":$mod")))
}

// 原版 JMeter 的 runtime classpath（components -> core -> jorphan -> ...）
val stockRuntime = configurations.create("stockRuntime")
stockRuntime.dependencies.add(dependencies.create(project(":src:components")))

// 差集 = 扩展模块额外引入的第三方依赖（自动排除原版JMeter已有的和插件自身JAR）
val extraPluginDeps = extensionRuntime
    .minus(stockRuntime)
    .filter { !it.name.startsWith("jmeter-plugins-gitee-") }

tasks.register("listPluginDeps") {
    group = "plugin-packaging"
    description = "列出扩展JAR及其新增第三方依赖（自动发现、自动检测）"

    dependsOn(extModules.map { ":${it}:jar" })

    doLast {
        println("\n=== 扩展 JAR (${extModules.size}个，自动发现) ===")
        extModules.forEach { m ->
            println("  lib/ext/jmeter-plugins-gitee-${m.substringAfterLast(":")}.jar")
        }
        println("\n=== 新增第三方依赖（原版JMeter不含，自动检测 ${extraPluginDeps.files.size}个）===")
        extraPluginDeps.files.sortedBy { it.name }.forEach { jar ->
            println("  ${jar.name}")
        }
    }
}

tasks.register<Copy>("packagePlugins") {
    group = "plugin-packaging"
    description = "打包全部扩展JAR及第三方依赖到 plugin-package/（自动发现模块）"

    val out = layout.projectDirectory.dir("plugin-package")
    into(out)

    // 插件 JAR -> lib/ext/
    into("lib/ext") {
        extModules.forEach { dependsOn(":${it}:jar"); from(tasks.getByPath(":${it}:jar").outputs.files) }
        rename("(.*)-[0-9].*\\.jar$", "$1.jar")
    }

    // 新增第三方依赖 -> lib/（自动检测差集）
    into("lib") {
        from(extraPluginDeps)
    }

    doLast { println("打包完成 (${extModules.size}个插件JAR + ${extraPluginDeps.files.size}个第三方依赖): ${out.asFile}") }
}

tasks.register("installToJmeter") {
    group = "plugin-packaging"
    description = "安装扩展JAR及第三方依赖到目标JMeter目录 (-PtargetJmeter=路径，基于目标lib/自动检测)"

    val target = project.findProperty("targetJmeter") as? String
        ?: throw GradleException("请指定: -PtargetJmeter=/path/to/jmeter-5.6.3")
    val root = file(target)
    if (!root.resolve("bin/jmeter.bat").exists() && !root.resolve("bin/jmeter").exists())
        throw GradleException("$target 不是合法JMeter目录")

    dependsOn(extModules.map { ":${it}:jar" })

    doLast {
        val targetLib = File(root, "lib")
        val targetExt = File(root, "lib/ext")

        // 从 jar 文件名中剥离版本号，如 xmlbeans-3.1.0.jar -> xmlbeans
        fun baseName(name: String) = name.replaceFirst(Regex("-[\\d.]+.*\\.jar$"), "")

        // 1. 复制插件 JAR（去掉版本号）
        extModules.forEach { mod ->
            val jarTask = tasks.getByPath(":${mod}:jar")
            jarTask.outputs.files.forEach { jar ->
                val newName = jar.name.replaceFirst(Regex("-[\\d.]+.*\\.jar$"), ".jar")
                jar.copyTo(File(targetExt, newName), overwrite = true)
            }
        }

        // 2. 收集目标 lib/ 中已有的 jar 基名（用于去重）
        val existing = targetLib.listFiles()
            ?.filter { it.name.endsWith(".jar") }
            ?.map { baseName(it.name) }
            ?.toSet() ?: emptySet()

        // 3. 复制扩展模块的传递依赖（目标 lib/ 中没有的）
        val newDeps = extensionRuntime.files
            .filter { !it.name.startsWith("jmeter-plugins-gitee-") }
            .filter { baseName(it.name) !in existing }
        newDeps.forEach { jar ->
            jar.copyTo(File(targetLib, jar.name), overwrite = true)
        }

        println("已安装 ${extModules.size} 个插件JAR + ${newDeps.size}个新增第三方依赖到: $target")
    }
}
