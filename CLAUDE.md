# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## Build System

JMeter uses Gradle 8.5 with Kotlin DSL. The build requires Java 17+ but targets Java 8 compatibility.

**Common commands:**
```bash
./gradlew build              # Full build with tests
./gradlew build -x test      # Build without tests
./gradlew runGui             # Build and launch JMeter GUI
./gradlew createDist         # Build and populate lib/ directory
./gradlew test               # Run unit tests
./gradlew check              # Run tests + style checks
./gradlew spotlessApply      # Auto-fix code formatting
./gradlew checkstyleAll      # Verify code style compliance
```

**Testing specific modules:**
```bash
./gradlew :src:core:test
./gradlew :src:components:test
./gradlew :src:jorphan:test
./gradlew test --tests org.apache.jmeter.assertions.DurationAssertionTest
```

**Optional quality checks (disabled by default):**
```bash
./gradlew build -Pspotbugs                      # Enable SpotBugs
./gradlew build -PenableErrorprone              # Enable ErrorProne
./gradlew build -PenableCheckerframework        # Enable nullness checks
./gradlew jacocoTestReport -Pcoverage           # Generate coverage report
```

**Build parameters:**
- `-PjdkBuildVersion=11` - Use custom JDK for building
- `-PjdkTestVersion=21` - Use custom JDK for testing
- `-PskipCheckstyle` - Skip code style checks
- `./gradlew parameters` - List all available parameters

See [gradle.md](gradle.md) for comprehensive command reference.

### Build Troubleshooting

**Gradle wrapper network timeout:**
If `./gradlew` fails to download Gradle due to network issues, use system Gradle instead:
```bash
gradle build              # Use system Gradle instead of wrapper
gradle runGui             # Build and launch GUI with system Gradle
```

**Dependency checksum validation failures:**
JMeter validates dependency checksums/PGP signatures. If validation fails:
```bash
gradle build -PchecksumIgnore           # Skip checksum validation (temporary)
gradle runGui -PchecksumIgnore          # Skip when launching GUI
```

For permanent checksum updates (after verifying dependencies):
```bash
gradle build -PchecksumUpdate           # Update checksum.xml for review
gradle build -PchecksumUpdateAll        # Update all checksums (insecure)
```

## High-Level Architecture

JMeter is organized as a modular load testing framework with a plugin-based architecture.

### Module Structure

```
src/core/        - Core interfaces and engine (TestElement, Sampler, Controller)
src/components/  - Standard components (assertions, timers, visualizers)
src/protocol/    - Protocol implementations (http, jdbc, ftp, jms, java, etc.)
src/functions/   - Built-in functions for dynamic data
src/jorphan/     - JMeter utility library (collections, HashTree)
src/dist/        - Distribution packaging
```

**Dependency flow:** Protocol modules → Components → Core → JOrphan

### Key Architectural Patterns

#### TestElement Pattern
All JMeter components extend from `AbstractTestElement` ([src/core/src/main/java/org/apache/jmeter/testelement/AbstractTestElement.java](src/core/src/main/java/org/apache/jmeter/testelement/AbstractTestElement.java)):
- Provides thread-safe property storage via `ReentrantReadWriteLock`
- Supports transient properties during test runs
- Integrates with `JMeterContext` for thread-local variables

#### TestBean Pattern (Automatic GUI Generation)
The `TestBean` marker interface enables automatic GUI generation:
- Implement `TestBean` interface ([src/core/src/main/java/org/apache/jmeter/testbeans/TestBean.java](src/core/src/main/java/org/apache/jmeter/testbeans/TestBean.java))
- Create `[Component]BeanInfo` extending `BeanInfoSupport`
- JMeter automatically generates GUI from bean properties—no custom GUI class needed

#### ServiceLoader Discovery
Components are discovered via Java's `ServiceLoader` mechanism:
- Service declarations in `META-INF/services/`:
  - `org.apache.jmeter.functions.Function` - Function implementations
  - `org.apache.jmeter.gui.action.Command` - GUI commands
  - `org.apache.jmeter.visualizers.backend.BackendListenerClient` - Backend listeners

#### Core Extension Points

**Samplers** - Generate load by sampling a protocol:
- Extend `AbstractSampler` or implement `Sampler` ([src/core/src/main/java/org/apache/jmeter/samplers/Sampler.java](src/core/src/main/java/org/apache/jmeter/samplers/Sampler.java))
- Create GUI class extending `AbstractSamplerGui`, or use TestBean pattern

**Functions** - Provide dynamic input to tests:
- Implement `Function` interface ([src/core/src/main/java/org/apache/jmeter/functions/Function.java](src/core/src/main/java/org/apache/jmeter/functions/Function.java))
- Annotate with `@JMeterService`
- Register in `META-INF/services/org.apache.jmeter.functions.Function`

**Controllers** - Control test flow:
- Implement `Controller` interface ([src/core/src/main/java/org/apache/jmeter/control/Controller.java](src/core/src/main/java/org/apache/jmeter/control/Controller.java))
- Provide `next()` method to return next sampler to execute

**Listeners** - Process sample results:
- Implement `SampleListener` interface ([src/core/src/main/java/org/apache/jmeter/samplers/SampleListener.java](src/core/src/main/java/org/apache/jmeter/samplers/SampleListener.java))
- Extend `AbstractListenerElement` for state management

**Assertions** - Validate sample results:
- Implement `Assertion` interface ([src/core/src/main/java/org/apache/jmeter/assertions/Assertion.java](src/core/src/main/java/org/apache/jmeter/assertions/Assertion.java))

**Config Elements** - Provide shared configuration:
- Extend `ConfigTestElement` ([src/core/src/main/java/org/apache/jmeter/config/ConfigTestElement.java](src/core/src/main/java/org/apache/jmeter/config/ConfigTestElement.java))

### Test Execution

- Test plans stored as `HashTree` structure ([src/jorphan/src/main/java/org/apache/jorphan/collections/HashTree.java](src/jorphan/src/main/java/org/apache/jorphan/collections/HashTree.java))
- `JMeterThread` ([src/core/src/main/java/org/apache/jmeter/threads/JMeterThread.java](src/core/src/main/java/org/apache/jmeter/threads/JMeterThread.java)) executes samplers via controller's `next()` method
- Each thread has its own `JMeterContext` and `JMeterVariables` for thread-safe execution
- `StandardJMeterEngine` ([src/core/src/main/java/org/apache/jmeter/engine/StandardJMeterEngine.java](src/core/src/main/java/org/apache/jmeter/engine/StandardJMeterEngine.java)) orchestrates test execution

### GUI Architecture

- GUI components implement `JMeterGUIComponent` ([src/core/src/main/java/org/apache/jmeter/gui/JMeterGUIComponent.java](src/core/src/main/java/org/apache/jmeter/gui/JMeterGUIComponent.java))
- Clear separation: TestElement (logic) vs JMeterGUIComponent (presentation)
- `MainFrame` is main window, `GuiPackage` manages global GUI state

## Running JMeter

After `./gradlew createDist`:
```bash
./bin/jmeter              # GUI mode (Unix)
./bin/jmeter.bat          # GUI mode (Windows)
./bin/jmeter -n           # Non-GUI mode (Unix)
./bin/jmeter-n.cmd        # Non-GUI mode (Windows)
```

## Development Guidelines

- All tests must pass before submitting: `./gradlew check`
- Use `./gradlew spotlessApply` to fix formatting before committing
- Discuss new features on dev mailing list before implementing
- 100% test coverage with JUnit required for new code
- Descriptive commit messages required
- See [CONTRIBUTING.md](CONTRIBUTING.md) for full contribution guidelines

## IDE Setup

**IntelliJ IDEA 2018.3.1+:**
- Open `build.gradle.kts` as project
- Enable "Create separate module per source set"

**Eclipse:**
- Run `./gradlew eclipse` to generate project files
- Import as "Existing Gradle Project"
- Install "Kotlin for Eclipse" plugin

## Documentation Location

- User documentation in `xdocs/` (XML format)
- Component reference: `xdocs/usermanual/component_reference.xml`
- Extension development: `xdocs/extending/`
- Building instructions: `xdocs/building.xml`

## Custom Extensions (二次开发插件)

This project contains 12 custom extension modules under `src/extension/`, migrated from JMeter 5.1.1.

### Module Map

| Module | JAR | Contents |
|--------|-----|----------|
| `src:extension:assertions` | jmeter-plugins-gitee-assertions.jar | JsonAutoAssertion, ValueAssertion, VariableAssertion |
| `src:extension:casutg` | jmeter-plugins-gitee-casutg.jar | Stepping/Ultimate/Concurrency/Arrivals Thread Groups + PerforAuto variants |
| `src:extension:config` | jmeter-plugins-gitee-config.jar | ExcelDataConfig |
| `src:extension:control` | jmeter-plugins-gitee-controller.jar | CaseController, LoopController, ParameterIncludeController, etc. |
| `src:extension:extractor` | jmeter-plugins-gitee-extractor.jar | ResponseAutoExtractor |
| `src:extension:gitee-functions` | jmeter-plugins-gitee-functions.jar | AESEncrypt, RSAEncrypt, ObjectVariable, TimePick, etc. |
| `src:extension:protocol-git` | jmeter-plugins-gitee-protocol-git.jar | Git Sampler (clone/commit/push/pull) |
| `src:extension:protocol-httpud` | jmeter-plugins-gitee-protocol-httpud.jar | HTTP User Defined Sampler + IncludeController |
| `src:extension:protocol-s3` | jmeter-plugins-gitee-protocol-s3.jar | S3 Sampler (upload/download/list/delete) |
| `src:extension:threads` | jmeter-plugins-gitee-threads.jar | 性能自动化线程组 |
| `src:extension:util` | jmeter-plugins-gitee-util.jar | CsvUtil, JMeterNewUtils |
| `src:extension:visualizers` | jmeter-plugins-gitee-visualizers.jar | InfluxDB Backend Listener |

### Module Dependencies

```
control → util, protocol-httpud
protocol-httpud → core, components, protocol.http
threads → util
casutg → util, threads, kg.apc:jmeter-plugins-cmn-jmeter
```

### Adding a New Extension Module

**packaging 命令会自动发现 `src/extension/` 下的模块**，新增模块只需完成 4 步注册：

1. **创建目录和 `build.gradle.kts`**：
   ```
   src/extension/new-module/
   ├── build.gradle.kts
   └── src/main/java/com/gitee/qa/jmeter/...
   ```

2. **注册到 `settings.gradle.kts`** — 在 include 块中添加：
   ```kotlin
   "src:extension:new-module",
   ```

3. **注册到 `build.gradle.kts`** — 在 `notPublishedProjects` 中添加（注意命名中的 `-` 转为驼峰）：
   ```kotlin
   projects.src.extension.newModule,
   ```

4. **注册到 `src/dist/build.gradle.kts`** — 在 `jars` 数组添加：
   ```kotlin
   ":src:extension:new-module",
   ```

完成后 `listPluginDeps` / `packagePlugins` / `installToJmeter` / `createDist` 自动生效，无需修改打包逻辑。

### Extension Build Commands

**Building the full distribution (includes all 11 plugin JARs in lib/ext/):**
```bash
./gradlew createDist -PchecksumIgnore -PenableErrorprone=false
./gradlew runGui -PchecksumIgnore -PenableErrorprone=false
```

**Building a single extension module:**
```bash
./gradlew :src:extension:protocol-s3:jar -PchecksumIgnore -PenableErrorprone=false
```

**Quick compile check (no jar):**
```bash
./gradlew :src:extension:assertions:compileJava -PchecksumIgnore -PenableErrorprone=false
```

**Building all extension modules without full dist:**
```bash
# Each module individually
for mod in assertions config control extractor gitee-functions protocol-git protocol-httpud protocol-s3 threads util visualizers; do
  ./gradlew :src:extension:$mod:jar -PchecksumIgnore -PenableErrorprone=false
done
```

### Plugin Packaging Tasks

Three convenience tasks are defined in root `build.gradle.kts`:

| Task | Purpose |
|------|---------|
| `./gradlew listPluginDeps` | List all 11 plugin JARs and their 3rd-party dependencies |
| `./gradlew packagePlugins` | Package all plugin JARs into `plugin-package/lib/ext/` (no version suffix) |
| `./gradlew installToJmeter -PtargetJmeter=<path>` | One-click install plugin JARs to a target JMeter installation |

All three require the standard flags:
```bash
./gradlew packagePlugins -PchecksumIgnore -PenableErrorprone=false
./gradlew installToJmeter -PtargetJmeter=/opt/jmeter-5.6.3 -PchecksumIgnore -PenableErrorprone=false
```

### New Third-Party Dependencies

These dependencies were added to `src/bom-thirdparty/build.gradle.kts` and are NOT in stock JMeter 5.6.3:

| Dependency | Used By |
|------------|---------|
| software.amazon.awssdk:s3, auth, regions, sdk-core | S3 Sampler |
| org.eclipse.jgit:org.eclipse.jgit | Git Sampler |
| com.github.mwiede:jsch | Git Sampler (SSH) |
| com.alibaba:easyexcel | Excel Data Config |
| org.skyscreamer:jsonassert | JSON Auto Assertion |
| commons-beanutils:commons-beanutils | Include Controller (with parameters) |
| org.apache.httpcomponents:httpasyncclient, httpcore-nio | InfluxDB Backend Listener |
| kg.apc:jmeter-plugins-cmn-jmeter | CASUTG Thread Groups (charting/GUI) |

### API Compatibility Notes (5.1.1 → 5.6.3)

When modifying extension code, be aware of these known incompatibilities from the 5.1.1 migration:

- **ErrorMetric**: 5.6.3 simplified to only `getResponseCode()`/`getResponseMessage()`. Custom fields (rawResponseCode, responseBody, requestHeader, sampleData) were removed.
- **JGit SSH**: `JschConfigSessionFactory` removed in JGit 5.13. Use custom `SshSessionFactory` with `JschRemoteSession` wrapper (see `GitSampler.java`).
- **HTTPAbstractImpl.sample()**: Method is `protected` in 5.6.3. HTTPUDSampler uses reflection as workaround pending Layer 3 core port.
- **ArgumentsPanel**: `getAddFromClipboardButton()`, `setReadOnly()` removed in 5.6.
- **JMeterTreeModel**: `recursiveHashTree()` removed in 5.6.
- **JMeterUtils.getJmxName()**: Removed in 5.6.3. Use `FileServer.getFileServer().getScriptName()` instead.
- **CsvUtil**: Moved from `org.apache.jmeter.util` to `com.gitee.qa.jmeter.util` (was a core modification in 5.1.1).

### Required Build Flags

These flags are **always required** when building extension modules:

- `-PchecksumIgnore` — New third-party deps don't have checksums in JMeter's verification file. For CI, run `-PchecksumUpdate` once and commit the updated `checksum.xml`.
- `-PenableErrorprone=false` — Extension code triggers ErrorProne warnings (unused variables, etc.) that become errors due to `-Werror`. These are style issues in legacy code, not bugs.
