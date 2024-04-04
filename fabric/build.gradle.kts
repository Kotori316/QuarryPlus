plugins {
    id("com.kotori316.common")
    id("com.kotori316.jars")
    id("com.kotori316.publishments")
    // id("scala")
    id("idea")
    // https://maven.fabricmc.net/net/fabricmc/fabric-loom/
    id("fabric-loom") version ("1.6.6")
}

loom {
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
    //to change the versions see the gradle.properties file
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${libs.versions.parchment.minecraft.get()}:${libs.versions.parchment.mapping.get()}@zip")
    })
    modImplementation(libs.fabric.loader)

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation(libs.fabric.api)
    modRuntimeOnly(libs.slp.fabric) {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc")
        exclude(group = "org.typelevel")
    }

    // library
    implementation(libs.findbugs)

    modCompileOnly(libs.rei.fabric) {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc")
    }
    modImplementation(libs.fabric.energy)
    modCompileOnly(libs.wthit.fabric.api)
    if (System.getenv("CI").toBoolean()) {
        modCompileOnly(libs.jade.fabric)
        modCompileOnly(libs.reborncore) { isTransitive = false }
        modCompileOnly(libs.techreborn) { isTransitive = false }
    } else {
        modImplementation(libs.jade.fabric)
        modCompileOnly(libs.reborncore) { isTransitive = false }
        modCompileOnly(libs.techreborn) { isTransitive = false }
    }
    modImplementation(libs.cloth.fabric) {
        exclude(group = "net.fabricmc.fabric-api")
        exclude(group = "net.fabricmc")
    }
    modImplementation(libs.modmenu) { isTransitive = false }
    modImplementation(libs.tu.fabric)
    modImplementation(libs.du.fabric)
    modImplementation(libs.vc.fabric) { isTransitive = false }

    // Test Dependencies.
    testImplementation(libs.fabric.junit)
    testImplementation(libs.bundles.jupiter)
}

sourceSets {
    main {
        java {
            srcDir("src/main/scala")
        }
    }
}

tasks.named("remapJar") {
    finalizedBy("jksSignJar", "jksSignRemapJar")
}

tasks.register("jksSignJar", com.kotori316.common.JarSignTask::class) {
    dependsOn("jar", "remapJar")
    jarTask = tasks.named("jar", org.gradle.jvm.tasks.Jar::class)
}

tasks.register("jksSignRemapJar", com.kotori316.common.JarSignTask::class) {
    dependsOn("jar", "remapJar")
    jarTask = tasks.named("remapJar", org.gradle.jvm.tasks.Jar::class)
}

signing {
    sign(tasks.remapJar.get())
}

afterEvaluate {
    tasks.named("signJar") {
        mustRunAfter("jksSignJar", "jksSignRemapJar")
    }
}
