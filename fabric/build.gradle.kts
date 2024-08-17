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
            property("mixin.debug.export", "true")
        }
        getByName("client") {
            configName = "Client"
            runDir = "Minecraft"
            programArgs("--username", "Kotori")
            property("fabric-tag-conventions-v2.missingTagTranslationWarning", "VERBOSE")
        }
        getByName("server") {
            configName = "Server"
            runDir = "run-server"
        }
        create("data") {
            client()
            configName = "Data"
            runDir = "run-server"
            property("fabric-api.datagen")
            property("fabric-api.datagen.output-dir", "${file("src/generated/resources")}")
            property("fabric-api.datagen.strict-validation")

            isIdeConfigGenerated = true
            source(sourceSets["test"])
        }
        create("gameTestServer") {
            configName = "GameTest"
            runDir = "game-test"
            server()
            vmArg("-ea")
            property("fabric-api.gametest")
            property(
                "fabric-api.gametest.report-file",
                "${project.layout.buildDirectory.dir("test-results/test/game_test.xml").get()}"
            )
            property(
                "fabric-tag-conventions-v2.missingTagTranslationWarning",
                "SILENCED",
            )
            source(sourceSets["gameTest"])
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
    modLocalRuntime(libs.jei.fabric)
    modLocalRuntime(libs.du.fabric)
    modLocalRuntime(libs.tu.fabric)

    implementation(libs.config.toml)
    include(libs.config.core)
    include(libs.config.toml)

    testImplementation(libs.fabric.junit)
    testImplementation(platform(libs.junit))
    testImplementation(libs.jupiter)
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
