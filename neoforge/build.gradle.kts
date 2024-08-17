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

runs {
    configureEach {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        systemProperty("mixin.debug.export", "true")
        modSources.add(modId, sourceSets["main"])
    }

    create("client") {
        workingDirectory = project.file("run")
        arguments("--username", "Kotori")
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

    create("data") {
        workingDirectory.set(project.file("runs/data"))
        arguments.addAll(
            "--mod",
            "quarryplus",
            "--all",
            "--output",
            file("src/generated/resources/").toString(),
            "--existing",
            file("src/main/resources/").toString()
        )

        modSources.add(modId, sourceSets["dataGen"])
    }
}

dependencies {
    implementation(libs.neoforge)
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.neoforge) { classifier("with-library") }) {
        isTransitive = false
    }
    runtimeOnly(libs.du.neoforge)
    implementation(libs.tu.neoforge)
    localRuntime(libs.jei.neoforge)

    gameTestRuntime(platform(libs.junit))
    gameTestRuntime(libs.jupiter)
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
