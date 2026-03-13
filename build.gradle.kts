import groovy.json.JsonSlurper
import org.gradle.internal.os.OperatingSystem

// ---------------------------------------------------------------------------
// Read config.json
// ---------------------------------------------------------------------------
val configFile = file("config.json")
val config = JsonSlurper().parseText(configFile.readText()) as Map<*, *>
val projectCfg = config["project"] as Map<*, *>
val buildCfg   = config["build"]   as Map<*, *>
val packageCfg = config["package"] as Map<*, *>

val isWindows = OperatingSystem.current().isWindows

val projectName    = projectCfg["name"]    as String
val projectVersion = projectCfg["version"] as String
val buildDir       = buildCfg["buildDir"]       as String
val cmakeBuildType = buildCfg["cmakeBuildType"] as String
val conanProfileDir    = buildCfg["conanProfileDir"]    as String
val conanProfileDefault = if (isWindows) buildCfg["conanProfileWindows"] as String
                          else buildCfg["conanProfileLinux"] as String
val conanProfile       = project.findProperty("conanProfile") as String? ?: conanProfileDefault
val packageName    = packageCfg["name"]         as String

// ---------------------------------------------------------------------------
// Helper – pick the right shell command
// ---------------------------------------------------------------------------
fun shellExec(vararg cmd: String): List<String> =
    if (isWindows) listOf("cmd", "/c") + cmd.toList()
    else listOf("bash", "-c") + cmd.toList()

// ---------------------------------------------------------------------------
// Task: conanInstall – install dependencies via Conan
// ---------------------------------------------------------------------------
tasks.register<Exec>("conanInstall") {
    group = "build"
    description = "Install C++ dependencies using Conan"

    commandLine(
        shellExec(
            "conan install . --output-folder=$buildDir --build=missing --profile=$conanProfileDir/$conanProfile"
        )
    )
}

// ---------------------------------------------------------------------------
// Task: cmakeConfigure – configure the CMake project
// ---------------------------------------------------------------------------
tasks.register<Exec>("cmakeConfigure") {
    group = "build"
    description = "Configure the CMake project"
    dependsOn("conanInstall")

    val toolchainFile = "$buildDir/conan_toolchain.cmake"

    commandLine(
        shellExec(
            "cmake -S . -B $buildDir -DCMAKE_BUILD_TYPE=$cmakeBuildType -DCMAKE_TOOLCHAIN_FILE=$toolchainFile -DCMAKE_POLICY_DEFAULT_CMP0091=NEW"
        )
    )
}

// ---------------------------------------------------------------------------
// Task: cmakeBuild – compile the project
// ---------------------------------------------------------------------------
tasks.register<Exec>("cmakeBuild") {
    group = "build"
    description = "Build the C++ project"
    dependsOn("cmakeConfigure")

    commandLine(
        shellExec(
            "cmake --build $buildDir --config $cmakeBuildType"
        )
    )
}

// ---------------------------------------------------------------------------
// Task: test – run Google Test suite via CTest
// ---------------------------------------------------------------------------
tasks.register<Exec>("test") {
    group = "verification"
    description = "Run unit tests via CTest"
    dependsOn("cmakeBuild")

    commandLine(
        shellExec(
            "ctest --test-dir $buildDir --build-config $cmakeBuildType --output-on-failure"
        )
    )
}

// ---------------------------------------------------------------------------
// Task: package – create distributable archive
//   .zip on Windows, .tar.gz on Linux
// ---------------------------------------------------------------------------
tasks.register<Exec>("package") {
    group = "distribution"
    description = "Create a distributable archive (.zip on Windows, .tar.gz on Linux)"
    dependsOn("test")

    val installDir = "$buildDir/install"

    // First install into a staging directory, then archive it
    val installCmd  = "cmake --install $buildDir --config $cmakeBuildType --prefix $installDir"
    val archiveCmd = if (isWindows) {
        val archiveName = "${packageName}-${projectVersion}-windows.zip"
        "cd $installDir && powershell -Command \"Compress-Archive -Path * -DestinationPath ../$archiveName -Force\""
    } else {
        val archiveName = "${packageName}-${projectVersion}-linux.tar.gz"
        "cd $installDir && tar czf ../$archiveName *"
    }

    commandLine(shellExec("$installCmd && $archiveCmd"))
}

// ---------------------------------------------------------------------------
// Task: clean – remove build directory
// ---------------------------------------------------------------------------
tasks.register<Delete>("clean") {
    group = "build"
    description = "Remove the build directory"
    delete(buildDir)
}

// ---------------------------------------------------------------------------
// Default task
// ---------------------------------------------------------------------------
tasks.register("buildAll") {
    group = "build"
    description = "Run full pipeline: conan install → cmake configure → build → test → package"
    dependsOn("package")
}
