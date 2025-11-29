plugins {
    id("com.kotori316.common")
    alias(libs.plugins.neoforge.gradle)
    id("com.kotori316.publish")
    id("com.kotori316.gt")
    id("com.kotori316.dg")
}

val modId = "QuarryPlus".lowercase()

subsystems {
    parchment {
        minecraftVersion = project.property("parchment.minecraft").toString()
        mappingsVersion = project.property("parchment.mapping").toString()
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        // Hack for NeoForge, conflicts with Mockito?
        if (requested.group == "org.ow2.asm" && requested.name.startsWith("asm")) {
            useVersion("9.7")
        }
    }
}

// Common data gen
sourceSets {
    create("commonDataGen") {
        val s = this
        project.configurations {
            named(s.compileClasspathConfigurationName) {
                extendsFrom(project.configurations.dataGenCompileClasspath.get())
            }
            named(s.runtimeClasspathConfigurationName) {
                extendsFrom(project.configurations.dataGenRuntimeClasspath.get())
            }
        }
    }
}
tasks.named("processCommonDataGenResources", ProcessResources::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
// End Common data gen

runs {
    configureEach {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        systemProperty("mixin.debug.export", "true")
        modSources.add(modId, sourceSets["main"])
    }

    create("client") {
        workingDirectory = project.file("run")
        systemProperties.put("neoforge.enabledGameTestNamespaces", "$modId,minecraft")
        arguments("--username", "Kotori")
        modSources.add(modId, sourceSets["gameTest"])
        dependencies {
            runtime(project.configurations.gameTestRuntime.get())
        }
        isClient = true
    }

    create("gameTestServer") {
        workingDirectory = project.file("game-test")
        systemProperties.put("neoforge.enabledGameTestNamespaces", "$modId,minecraft")
        // systemProperties.put("bsl.debug", "true")
        jvmArguments("-ea")
        modSources.add(modId, sourceSets["gameTest"])
        dependencies {
            runtime(project.configurations.gameTestRuntime.get())
        }
        isGameTest = true
    }

    create("clientData") {
        workingDirectory.set(project.file("runs/data"))
        val dataGenModId = "${modId}_data"
        arguments.addAll(
            "--mod",
            dataGenModId,
            "--output",
            file("src/generated/resources/").toString(),
            "--existing",
            file("src/main/resources/").toString()
        )

        modSources.add(dataGenModId, sourceSets["dataGen"])
    }

    create("commonData") {
        runType("clientData")
        val dataGenModId = "${modId}_common_data"
        isDataGenerator = true
        workingDirectory.set(project.file("runs/commonData"))
        arguments.addAll(
            "--mod",
            dataGenModId,
            "--output",
            project(":common").file("src/generated/resources/").toString(),
            "--existing",
            project(":common").file("src/main/resources/").toString()
        )

        modSources.add(dataGenModId, sourceSets["commonDataGen"])
    }
}

dependencies {
    implementation(libs.neoforge)
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.neoforge) { classifier("with-library") }) {
        isTransitive = false
    }
    implementation(libs.du.neoforge) {
        isTransitive = false
    }
    // localRuntime(libs.jei.neoforge)

    gameTestRuntime(platform(libs.junit))
    gameTestRuntime(libs.jupiter.core)
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

tasks.compileGameTestJava {
    source(project(":common").sourceSets["gameTest"].allSource)
}

tasks.compileDataGenScala {
    source(project(":common").sourceSets["dataGen"].allSource)
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
