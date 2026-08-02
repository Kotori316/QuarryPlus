plugins {
    id("com.kotori316.common")
    alias(libs.plugins.fabric.loom)
    id("com.kotori316.publish")
    id("com.kotori316.gt")
    id("com.kotori316.dg")
}

loom {
    knownIndyBsms.add("scala/runtime/LambdaDeserialize")
    knownIndyBsms.add("java/lang/runtime/SwitchBootstraps/typeSwitch")

    runs {
        configureEach {
            systemProperties.put("mixin.debug.export", "true")
        }
        getByName("client") {
            displayName = "Client"
            runDirectory = project.file("Minecraft")
            programArguments.addAll("--username", "Kotori")
            systemProperties.put("fabric-tag-conventions-v2.missingTagTranslationWarning", "VERBOSE")
            sourceSet = "gameTest"
        }
        getByName("server") {
            displayName = "Server"
            runDirectory = project.file("run-server")
        }
        create("data") {
            client()
            displayName = "Data"
            runDirectory = project.file("run-server")
            systemProperties.put("fabric-api.datagen", "")
            systemProperties.put("fabric-api.datagen.output-dir", "${file("src/generated/resources")}")
            systemProperties.put("fabric-api.datagen.strict-validation", "")
            generateRunConfig = true
            sourceSet = "dataGen"
        }
        create("gameTestServer") {
            displayName = "GameTest"
            runDirectory = project.file("game-test")
            server()
            jvmArguments.add("-ea")
            systemProperties.put("fabric-api.gametest", "")
            systemProperties.put(
                "fabric-api.gametest.report-file",
                "${project.layout.buildDirectory.dir("test-results/test/game_test.xml").get()}"
            )
            systemProperties.put(
                "fabric-tag-conventions-v2.missingTagTranslationWarning",
                "SILENCED",
            )
            sourceSet = "gameTest"
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        val parchmentMC = project.property("parchment.minecraft")
        val parchmentDate = project.property("parchment.mapping")
        parchment("org.parchmentmc.data:parchment-$parchmentMC:$parchmentDate@zip")
    })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.version.checker)
    compileOnly(project(":common"))

    modImplementation(libs.teamreborn.energy)
    // modLocalRuntime(libs.jei.fabric)
    modImplementation(libs.du.fabric) {
        isTransitive = false
    }
    modLocalRuntime(libs.slp.fabric)

    implementation(libs.config.toml)
    include(libs.config.core)
    include(libs.config.toml)

    testImplementation(libs.fabric.junit)
    testImplementation(platform(libs.junit))
    testImplementation(libs.jupiter.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(project(":common"))
    testImplementation(project.project(":common").sourceSets.test.get().output)
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

ext {
    set("publishJarTaskName", "remapJar")
}

// Workaround?
tasks.remapJar {
    mustRunAfter("compileTestScala")
}
