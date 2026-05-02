plugins {
    id("com.kotori316.common")
    alias(libs.plugins.neoforge.gradle)
    id("com.kotori316.publish")
    id("com.kotori316.gt")
    id("com.kotori316.dg")
}

val modId = "QuarryPlus".lowercase()

subsystems {
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

// Game test merged
sourceSets {
    create("gameTestMerged") {
        val s = this
        java {
            srcDirs(sourceSets.main.get().java.srcDirs)
            srcDirs(sourceSets["gameTest"].java.srcDirs)
        }
        resources {
            srcDirs(sourceSets.main.get().resources.srcDirs)
            srcDirs(sourceSets["gameTest"].resources.srcDirs)
        }
        project.configurations {
            named(s.compileClasspathConfigurationName) {
                extendsFrom(configurations["gameTestCompileClasspath"])
            }
            named(s.runtimeClasspathConfigurationName) {
                extendsFrom(configurations["gameTestRuntimeClasspath"])
            }
        }
    }
}
tasks.named("compileGameTestMergedJava", JavaCompile::class) {
    source(project(":common").sourceSets.main.get().allSource)
    source(project(":common").sourceSets["gameTest"].allSource)
}
tasks.named("processGameTestMergedResources", ProcessResources::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(project(":common").sourceSets.main.get().resources)
    from(project(":common").sourceSets["gameTest"].resources)
}
// End Game test merged

runs {
    configureEach {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        systemProperty("mixin.debug.export", "true")
    }

    create("client") {
        workingDirectory = project.file("run")
        systemProperties.put("neoforge.enabledGameTestNamespaces", "$modId,minecraft")
        environmentVariable("TEST_UTILITY_NO_REGISTRATION", "false")
        arguments("--username", "Kotori")
        modSources.add(modId, sourceSets["main"])
        modSources.add(modId, sourceSets["gameTest"])
        dependencies {
            runtime(project.configurations.gameTestRuntime.get())
        }
        isClient = true
    }

    create("gameTestServer") {
        workingDirectory = project.file("game-test")
        systemProperty("mixin.debug.export", "false")
        environmentVariable("TEST_UTILITY_NO_REGISTRATION", "false")
        // systemProperties.put("bsl.debug", "true")
        jvmArguments("-ea")
        modSources.add(modId, sourceSets["gameTestMerged"])
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

        modSources.add(modId, sourceSets["main"])
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

        modSources.add(modId, sourceSets["main"])
        modSources.add(dataGenModId, sourceSets["commonDataGen"])
    }
}

dependencies {
    implementation(libs.neoforge)
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.neoforge) { classifier("all") }) {
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
    description = "Add jks signature to NeoForge jar"
    dependsOn(tasks.jar)
    jarTask = tasks.jar
}

ext {
    set("publishJarTaskName", "jar")
}
