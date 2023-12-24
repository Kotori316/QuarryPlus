plugins {
    id("com.kotori316.common")
    id("com.kotori316.jars")
    id("com.kotori316.publishments")
    id("scala")
    id("net.minecraftforge.gradle") version ("[6.0,6.2)")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}
// Only edit below this line, the above code adds and enables the necessary things for Forge to be setup.

tasks.compileScala {
    scalaCompileOptions.additionalParameters = listOf("-Wconf:cat=deprecation:w,any:e")
    dependsOn(tasks.processResources)
}

tasks.compileTestScala {
    dependsOn(tasks.processTestResources)
}

sourceSets {
    create("runGame")
}

configurations {
    getByName("runGameCompileClasspath") {
        extendsFrom(compileClasspath.get())
        extendsFrom(testCompileClasspath.get())
    }
    getByName("runGameRuntimeClasspath") {
        extendsFrom(runtimeClasspath.get())
        extendsFrom(testRuntimeClasspath.get())
    }
}

tasks.named("compileRunGameScala", ScalaCompile::class) {
    source(
        sourceSets.main.get().java, sourceSets.main.get().scala,
        sourceSets.test.get().java, sourceSets.test.get().scala,
    )
    dependsOn("processRunGameResources")
}

tasks.named("processRunGameResources", ProcessResources::class) {
    from(sourceSets.main.get().resources, sourceSets.test.get().resources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

sourceSets.forEach {
    val dir = layout.buildDirectory.dir("sourcesSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
    it.scala.destinationDirectory = dir
}

val modId: String = "QuarryPlus".lowercase()
minecraft {
    mappings(
        mapOf(
            "channel" to "parchment",
            "version" to libs.versions.parchment.get() + "-" + libs.versions.minecraft.get(),
        )
    )

    runs {
        configureEach {
            property("forge.logging.markers", "")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            property("mixin.debug.export", "true")
            property("forge.logging.console.level", "debug")
        }

        val client = create("client") {
            workingDirectory(project.file("Minecraft"))
            property("forge.enabledGameTestNamespaces", modId)
            // From https://github.com/SlimeKnights/TinkersConstruct/blob/1.18/build.gradle
            if (project.hasProperty("mc_uuid")) {
                // Your uuid without any dashes in the middle
                args("--uuid", project.findProperty("mc_uuid"))
            }
            if (project.hasProperty("mc_username")) {
                // Your username/display name, this is the name that shows up in chat
                // Note: This is not your email, even if you have a Mojang account
                args("--username", project.findProperty("mc_username"))
            }
            // Your access token, you can find it in your ".minecraft/launcher_profiles.json" file
            // If not set, use "0" to prevent authentication exception.
            args("--accessToken", project.findProperty("mc_token") ?: "0")
            jvmArgs("-EnableAssertions".lowercase())

            mods {
                create(modId) {
                    source(sourceSets["runGame"])
                    // source(sourceSets.test)
                }
            }
            lazyToken("minecraft_classpath") {
                configurations.game_test.get().resolvedConfiguration.files.joinToString(File.pathSeparator) { it.absolutePath }
            }
        }

        create("client2") {
            parent(client)
            workingDirectory(project.file("run-server-client"))
            property("forge.enabledGameTestNamespaces", "")
            property("forge.enableGameTest", "false")
            mods {
                create(modId) {
                    source(sourceSets["main"])
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run-server"))

            // Recommended logging data for a userdev environment
            args("--nogui")

            mods {
                create(modId) {
                    source(sourceSets["main"])
                }
            }
        }

        create("data") {
            workingDirectory(project.file("run-server"))
            property("bsl.debug", "true")
            args(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/"),
                "--existing",
                file("src/main/resources/")
            )

            mods {
                create(modId) {
                    source(sourceSets["runGame"])
                    // source(sourceSets["test"])
                }
            }
        }

        create("gameTestServer") {
            workingDirectory(project.file("game-test"))
            property("forge.enabledGameTestNamespaces", modId)
            jvmArgs("-EnableAssertions".lowercase())

            mods {
                create(modId) {
                    source(sourceSets["runGame"])
                    // source(sourceSets["test"])
                }
            }
            lazyToken("minecraft_classpath") {
                configurations.game_test.get().resolvedConfiguration.files.joinToString(File.pathSeparator) { it.absolutePath }
            }
        }
    }
}

dependencies {
    minecraft(libs.forge)
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

    val useREI = true // Boolean.parseBoolean(System.getenv("USE_REI"))
    compileOnly(fg.deobf(libs.jei.common.api.get()))
    compileOnly(fg.deobf(libs.jei.forge.api.get()))
    if (useREI) {
        implementation(fg.deobf(libs.rei.forge.get()))
        implementation(fg.deobf(libs.cloth.forge.get()))
        implementation(fg.deobf(libs.architectury.forge.get()))
    } else {
        runtimeOnly(fg.deobf(libs.jei.forge.run.get(), closureOf<ExternalModuleDependency> {
            isTransitive = false
        }))
        compileOnly(fg.deobf(libs.rei.forge.get()))
        compileOnly(fg.deobf(libs.cloth.forge.get()))
        compileOnly(fg.deobf(libs.architectury.forge.get()))
    }
    val slpClassifier =
        if (listOf("RUN_DATA", "RUN_GAME_TEST").any { System.getenv(it).toBoolean() }) "dev" else "with-library"
    runtimeOnly(variantOf(libs.slp.forge) { classifier(slpClassifier) }) {
        isTransitive = false
    }

    val enableFTB = false // Boolean.parseBoolean(System.getenv("USE_FTB"))
    if (enableFTB) { // FTB Chunks stuff
        implementation(fg.deobf(libs.ftb.chunks.forge.get()))
        implementation(fg.deobf(libs.ftb.library.forge.get()))
        runtimeOnly(fg.deobf(libs.ftb.teams.forge.get()))
        implementation(fg.deobf(libs.architectury.forge.get()))
    } else {
        compileOnly(fg.deobf(libs.ftb.chunks.forge.get()))
        compileOnly(fg.deobf(libs.ftb.library.forge.get()))
    }

    // IC2 Classic
    if (false) {
        // if (System.getenv("IGNORE_OTHER_MODS_IN_RUNTIME") == null) {
        compileOnly(fg.deobf(libs.ic2.get()))
        testImplementation(fg.deobf(libs.ic2.get()))
    } else {
        compileOnly(fg.deobf(libs.ic2.get()))
        // testCompileOnly(fg.deobf(group= "curse.maven", name= "ic2-classic-242942", version= project.ic2ClassicId))
    }

    // Test Dependencies.
    // Required these libraries to execute the tests.
    // The library will avoid errors of ForgeRegistry and Capability.
    testImplementation(
        fg.deobf(
            mapOf(
                "group" to "com.kotori316",
                "name" to "test-utility-forge",
                "version" to libs.versions.tu.get()
            ), closureOf<ExternalModuleDependency> {
                isTransitive = false
            }
        )
    )
    testImplementation(libs.bundles.jupiter)
    game_test(libs.bundles.jupiter)
}

tasks.named("jar", Jar::class) {
    finalizedBy("reobfJar", "jksSignJar")
}

tasks.register("jksSignJar", com.kotori316.common.JarSignTask::class) {
    dependsOn("reobfJar")
    jarTask = tasks.jar
}

/*String getShortChangelog(boolean includeVersion) {
    def f = file("temp_changelog.md")
    if (f.exists()) {
        String version = f.readLines().find { s -> s.startsWith("#") }
        def content = f.readLines()
                .drop(4 - 2)
                .takeWhile { s -> !s.isEmpty() }
                .stream() as Stream<String>
        if (includeVersion) {
            return Stream.concat(
                    Stream.of(version, ""),
                    content
            ).collect(Collectors.joining(System.lineSeparator()))
        } else {
            content.collect(Collectors.joining(System.lineSeparator()))
        }

    } else {
        return version.toString()
    }
}*/
