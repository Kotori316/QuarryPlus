plugins {
    id("com.kotori316.common")
    alias(libs.plugins.fabric.loom)
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
            runDir = "build/game_test"
            server()
            vmArg("-ea")
            property("fabric-api.gametest")
            property(
                "fabric-api.gametest.report-file",
                "${project.layout.buildDirectory.dir("test-results/test/game_test.xml").get()}"
            )
            source(sourceSets["test"])
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        val parchmentMC = libs.versions.parchment.minecraft.get()
        val parchmentDate = libs.versions.parchment.mapping.get()
        parchment("org.parchmentmc.data:parchment-$parchmentMC:$parchmentDate@zip")
    })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.version.checker)
    compileOnly(project(":common"))
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
