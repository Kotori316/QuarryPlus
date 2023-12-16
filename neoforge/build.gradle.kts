plugins {
    id("com.kotori316.common")
    id("com.kotori316.jars")
    id("com.kotori316.publishments")
    id("scala")
    id("net.neoforged.gradle.userdev") version ("[7.0.49, 8)")
    // id("org.parchmentmc.librarian.forgegradle") version("1.+")
}
// Only edit below this line, the above code adds and enables the necessary things for Forge to be setup.

tasks.compileScala {
    scalaCompileOptions.additionalParameters = listOf("-Wconf:cat=deprecation:w,any:e")
}

runs {
    configureEach {
        systemProperties.put("forge.logging.markers", "")
        systemProperties.put("mixin.env.remapRefMap", "true")
        systemProperties.put("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
        systemProperties.put("mixin.debug.export", "true")
        systemProperties.put("forge.logging.console.level", "debug")
    }

    create("client") {
        workingDirectory.set(project.file("Minecraft"))
        systemProperties.put("forge.enabledGameTestNamespaces", "QuarryPlus".lowercase())
        // Your access token, you can find it in your ".minecraft/launcher_profiles.json" file
        // If not set, use "0" to prevent authentication exception.
        programArguments.addAll("--accessToken", (project.findProperty("mc_token") ?: "0") as String)
        jvmArguments.add("-EnableAssertions".lowercase())

        modSources.add(sourceSets.getByName("main"))
    }

    create("server") {
        workingDirectory.set(project.file("run-server"))

        // Recommended logging data for a userdev environment
        programArguments.add("--nogui")

        modSources.add(sourceSets.getByName("main"))
    }

    create("data") {
        workingDirectory.set(project.file("run-server"))
        programArguments.addAll(
            "--mod",
            "quarryplus",
            "--all",
            "--output",
            file("src/generated/resources/").toString(),
            "--existing",
            file("src/main/resources/").toString()
        )

        modSources.add(sourceSets.getByName("main"))
        modSources.add(sourceSets.getByName("test"))
    }

    create("gameTestServer") {
        workingDirectory.set(project.file("game-test"))
        systemProperties.put("forge.enabledGameTestNamespaces", "QuarryPlus".lowercase())
        jvmArguments.add("-EnableAssertions".lowercase())

        modSources.add(sourceSets.getByName("main"))
        modSources.add(sourceSets.getByName("test"))
        dependencies {
            runtime(configuration(configurations.getByName("game_test")))
        }
    }
}

dependencies {
    implementation(libs.neoforge)
    compileOnly(libs.scala2)
    // compileOnly(libs.scala3)
    compileOnly(libs.bundles.cats) {
        exclude(group = "org.scala-lang", module = "scala3-library_3")
    }
    testImplementation(libs.scala2)
    // testImplementation(libs.scala3)
    testImplementation(libs.bundles.cats) {
        exclude(group = "org.scala-lang", module = "scala3-library_3")
    }

    compileOnly(libs.jei.common.api)
    compileOnly(libs.jei.forge.api)
    compileOnly(libs.rei.neoforge)
    // runtimeOnly(group: "me.shedaniel.cloth", name: "cloth-config-neoforge", version: project.clothVersion)
    // runtimeOnly(group: "dev.architectury", name: "architectury-neoforge", version: project.architecturyVersion)

    runtimeOnly(variantOf(libs.slp.neoforge) { classifier("with-library") }) {
        isTransitive = false
    }

    val enableFTB = false // Boolean.parseBoolean(System.getenv("USE_FTB"))
    if (enableFTB) { // FTB Chunks stuff
        implementation(libs.ftb.chunks.forge)
        implementation(libs.ftb.library.forge)
        runtimeOnly(libs.ftb.teams.forge)
        implementation(libs.architectury.neoforge)
    } else {
        compileOnly(libs.ftb.chunks.forge) {
            isTransitive = false
        }
        compileOnly(libs.ftb.library.forge) {
            isTransitive = false
        }
    }

    // IC2 Classic
    if (false) {
        // if (System.getenv("IGNORE_OTHER_MODS_IN_RUNTIME") == null) {
        compileOnly(libs.ic2)
        testImplementation(libs.ic2)
    } else {
        compileOnly(libs.ic2)
        // testCompileOnly(libs.ic2)
    }

    // Test Dependencies.
    // Required these libraries to execute the tests.
    // The library will avoid errors of ForgeRegistry and Capability.
    implementation(group = "com.kotori316", name = "test-utility-neoforge", version = libs.versions.tu.get())

    testImplementation(libs.bundles.jupiter)
    game_test(libs.bundles.jupiter)
}

tasks.named("jar", Jar::class) {
    finalizedBy("jksSignJar")
}

tasks.register("jksSignJar", com.kotori316.common.JarSignTask::class) {
    dependsOn(tasks.jar)
    jarTask = tasks.jar
}
