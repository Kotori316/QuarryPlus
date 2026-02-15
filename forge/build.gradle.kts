plugins {
    id("com.kotori316.common")
    alias(libs.plugins.forge.gradle)
    id("com.kotori316.publish")
    id("com.kotori316.gt")
    id("com.kotori316.dg")
}

val modId = "QuarryPlus".lowercase()
val minecraftVersion = libs.versions.minecraft.get()

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
    val dataGenSourceSet by dataGen
    create("genData") {
        val sourceSet = this
        project.configurations {
            named(sourceSet.compileClasspathConfigurationName) {
                extendsFrom(
                    project.configurations.named(mainSourceSet.compileClasspathConfigurationName).get(),
                    project.configurations.named(dataGenSourceSet.compileClasspathConfigurationName).get(),
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
            "channel" to "official",
            "version" to libs.versions.minecraft.get(),
        )
    )

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "")
            systemProperty("mixin.debug.export", "true")
            systemProperty("forge.logging.console.level", "debug")
            if (System.getProperty("os.name").startsWith("Mac")) {
                jvmArgs("-XstartOnFirstThread")
            }
        }

        create("client") {
            workingDir.convention(layout.projectDirectory.dir("Minecraft"))
            systemProperty("forge.enabledGameTestNamespaces", modId)
            args("--accessToken", "0")
            jvmArgs("-EnableAssertions".lowercase())
        }

        create("gameTestServer") {
            systemProperty("forge.enabledGameTestNamespaces", modId)
            workingDir.convention(layout.projectDirectory.dir("game-test"))
            systemProperty("bsl.debug", "true")
            jvmArgs("-ea")
        }

        create("data") {
            workingDir.convention(layout.projectDirectory.dir("run-server"))
            args(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/"),
                "--existing",
                file("src/main/resources/")
            )
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

dependencies {
    implementation(minecraft.dependency(libs.forge))
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.forge) { classifier("with-library") }) {
        isTransitive = false
    }
    runtimeOnly(libs.jei.forge)
    implementation(libs.du.forge) {
        isTransitive = false
    }
    // Mixin
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }

    "runGameRuntimeOnly"(platform(libs.junit))
    "runGameRuntimeOnly"(libs.jupiter.core)
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "MixinConfigs" to "${modId}.mixins.json"
            )
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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
    val minecraft = minecraftVersion
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

tasks.named("compileGenDataScala", ScalaCompile::class) {
    dependsOn("processGenDataResources")
    project.findProject(":common")?.let {
        source(it.sourceSets.main.get().java)
        source(it.sourceSets.dataGen.get().scala)
    }
    source(project.sourceSets.main.get().java)
    source(project.sourceSets.dataGen.get().scala)
}

tasks.named("processGenDataResources", ProcessResources::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    project.findProject(":common")?.let {
        from(it.sourceSets.main.get().resources)
        from(it.sourceSets.dataGen.get().resources)
    }
    from(project.sourceSets.main.get().resources)
    from(project.sourceSets.dataGen.get().resources)

    val projectVersion = project.version.toString()
    val minecraft = minecraftVersion
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
tasks.named("compileDataGenScala") {
    dependsOn("processDataGenResources")
}

sourceSets.forEach {
    val dir = layout.buildDirectory.dir("forgeSourcesSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
    it.scala.destinationDirectory = dir
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
