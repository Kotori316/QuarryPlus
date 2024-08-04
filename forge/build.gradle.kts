plugins {
    id("com.kotori316.common")
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.forge.parchment)
    id("com.kotori316.publish")
    id("com.kotori316.gt")
}

val modId = "QuarryPlus".lowercase()

sourceSets {
    val mainSourceSet by main
    val gameTestSourceSet by gameTest
    create("runGame") {
        val sourceSet = this
        project.configurations {
            named(sourceSet.compileClasspathConfigurationName) {
                extendsFrom(
                    project.configurations.named(mainSourceSet.compileClasspathConfigurationName).get(),
                    project.configurations.named(gameTestSourceSet.compileClasspathConfigurationName).get(),
                )
            }
            named(sourceSet.runtimeClasspathConfigurationName) {
                extendsFrom(
                    project.configurations.named(mainSourceSet.runtimeClasspathConfigurationName).get(),
                )
            }
        }
    }
}

minecraft {
    mappings(
        mapOf(
            "channel" to "parchment",
            "version" to "${project.property("parchment.minecraft")}-${project.property("parchment.mapping")}-${libs.versions.minecraft.get()}",
        )
    )
    reobf = false

    runs {
        configureEach {
            property("forge.logging.markers", "")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            property("mixin.debug.export", "true")
            property("forge.logging.console.level", "debug")
        }

        create("client") {
            workingDirectory(project.file("Minecraft"))
            property("forge.enabledGameTestNamespaces", modId)
            args("--accessToken", "0")
            jvmArgs("-EnableAssertions".lowercase())

            mods {
                create(modId) {
                    source(sourceSets["main"])
                    // source(sourceSets.test)
                }
            }
        }

        create("gameTestServer") {
            property("forge.enabledGameTestNamespaces", modId)
            workingDirectory(project.file("game-test"))
            property("bsl.debug", "true")
            jvmArgs("-ea")
            mods {
                create("main") {
                    source(sourceSets["runGame"])
                }
            }
        }
    }
}

dependencies {
    minecraft(libs.forge)
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.forge) { classifier("with-library") }) {
        isTransitive = false
    }
    runtimeOnly(libs.jei.forge)
    implementation("com.kotori316:test-utility-forge:21.0.5") {
        isTransitive = false
    }
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }

    "runGameRuntimeOnly"(platform(libs.junit))
    "runGameRuntimeOnly"(libs.jupiter)
}

// Share with common
tasks.compileJava {
    options.encoding = "UTF-8"
    source(project(":common").sourceSets.main.get().allSource)
}
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(project(":common").sourceSets.main.get().resources)
}

// Forge stuff

tasks.named("compileRunGameJava", JavaCompile::class) {
    project.findProject(":common")?.let {
        source(it.sourceSets.main.get().java)
        source(it.sourceSets.gameTest.get().java)
    }
    source(project.sourceSets.main.get().java)
    source(project.sourceSets.gameTest.get().java)
}
tasks.named("processRunGameResources", ProcessResources::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    project.findProject(":common")?.let {
        from(it.sourceSets.main.get().resources)
        from(it.sourceSets.gameTest.get().resources)
    }
    from(project.sourceSets.main.get().resources)
    from(project.sourceSets.gameTest.get().resources)

    val projectVersion = project.version.toString()
    val minecraft = "1.21"
    inputs.property("version", projectVersion)
    inputs.property("minecraftVersion", minecraft)
    listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml").forEach { fileName ->
        filesMatching(fileName) {
            expand(
                "version" to projectVersion,
                "update_url" to "https://version.kotori316.com/get-version/${minecraft}/${project.name}/${modId}",
                "mc_version" to minecraft,
            )
        }
    }
}

sourceSets.forEach {
    val dir = layout.buildDirectory.dir("forgeSourcesSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
}

tasks.named("jar", Jar::class) {
    finalizedBy("jksSignJar")
}

tasks.register("jksSignJar", com.kotori316.common.JarSignTask::class) {
    dependsOn(tasks.jar)
    jarTask = tasks.jar
}

ext {
    set("publishJarTaskName", "jar")
}
