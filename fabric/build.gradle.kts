import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("com.kotori316.common")
    // id("scala")
    id("idea")
    // https://maven.fabricmc.net/net/fabricmc/fabric-loom/
    id("fabric-loom") version ("1.7.2")

    alias(libs.plugins.cursegradle)
    alias(libs.plugins.minotaur)
    alias(libs.plugins.cf)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.named("sourcesJar", Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    val mcVersion = project.property("minecraft").toString()
    val modId = "quarryplus"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    inputs.property("version", project.version)

    listOf("fabric.mod.json", "META-INF/mods.toml").forEach { fileName ->
        filesMatching(fileName) {
            expand(
                "version" to project.version,
                "update_url" to "https://version.kotori316.com/get-version/${mcVersion}/${project.name}/${modId}",
                "mc_version" to mcVersion,
            )
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

signing {
    sign(publishing.publications)
}

// sign task creation is in `com.kotori316.jars.gradle.kts`
val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class) {
    onlyIf("runs only with signing keys") { hasGpgSignature }
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

afterEvaluate {
    rootProject.tasks.named("githubRelease") {
        dependsOn(":${platformName}:assemble")
        mustRunAfter(":${platformName}:signMavenJavaPublication")
    }
}

val minecraft: String by project
val platformName: String = "fabric"
tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = minecraft
    platform = platformName
    platformVersion = libs.fabric.api.get().version
    modName = "QuarryPlus".lowercase()
    changelog = "Fabric release"
    homepage.set(
        if (platformName == "forge") "https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner"
        else "https://modrinth.com/mod/additional-enchanted-miner"
    )
    isDryRun = releaseDebug
}

tasks.register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
    gameVersion = minecraft
    platform = platformName
    modName = "QuarryPlus".lowercase()
    version = project.version.toString()
    failIfExists = !releaseDebug
}

val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

curseforge {
    apiKey = project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN") ?: ""
    project(closureOf<CurseProject> {
        id = "282837"
        changelogType = "markdown"
        changelog = "Fabric release"
        releaseType = "release"
        addGameVersion(minecraft)
        addGameVersion(
            when (platformName) {
                "forge" -> "Forge"
                "fabric" -> "Fabric"
                "neoforge" -> "NeoForge"
                else -> throw IllegalArgumentException("Unknown platform $platformName")
            }
        )
        mainArtifact(
            tasks.named("remapJar", org.gradle.jvm.tasks.Jar::class).flatMap { it.archiveFile }.get(),
            closureOf<CurseArtifact> {
                displayName = "v${project.version}-${platformName} [$minecraft]"
            })
        relations(closureOf<CurseRelation> {
            requiredDependency("scalable-cats-force")
            if (platformName == "fabric") {
                requiredDependency("fabric-api")
                requiredDependency("cloth-config")
                requiredDependency("automatic-potato")
            }
        })
    })
    options(closureOf<Options> {
        curseGradleOptions.debug = releaseDebug
        curseGradleOptions.javaVersionAutoDetect = false
        curseGradleOptions.forgeGradleIntegration = false
    })
}

modrinth {
    token = (project.findProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN") ?: "") as String
    projectId = "additional-enchanted-miner"
    versionType = "release"
    versionName = "${project.version}-${platformName}"
    versionNumber = project.version.toString()
    afterEvaluate {
        uploadFile = if (platformName == "fabric") {
            tasks.named("remapJar", org.gradle.jvm.tasks.Jar::class).flatMap { it.archiveFile }
        } else {
            tasks.jar.get()
        }
    }

    gameVersions = listOf(minecraft)
    loaders = listOf(platformName)
    changelog = "Fabric release"
    debugMode = releaseDebug
    dependencies {
        required.project("scalable-cats-force")
        if (platformName == "fabric") {
            required.project("fabric-api")
            required.project("cloth-config")
            required.project("automatic-potato")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Kotori316/QuarryPlus")
            credentials {
                username = project.findProperty("gpr.user") as? String ?: System.getenv("GITHUB_ACTOR") ?: ""
                password = project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: ""
            }
        }
        val u = project.findProperty("maven_username") as? String ?: System.getenv("MAVEN_USERNAME") ?: ""
        val p = project.findProperty("maven_password") as? String ?: System.getenv("MAVEN_PASSWORD") ?: ""
        if (u != "" && p != "") {
            maven {
                name = "kotori316-maven"
                // For users: Use https://maven.kotori316.com to get artifacts
                url = uri("https://maven2.kotori316.com/production/maven")
                credentials {
                    username = u
                    password = p
                }
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            val baseName: String = if (platformName == "forge")
                "AdditionalEnchantedMiner"
            else
                "AdditionalEnchantedMiner-$platformName"
            artifactId = baseName.lowercase()
            from(components["java"])
            pom {
                description = "QuarryPlus for Minecraft $minecraft with $platformName"
                url = "https://github.com/Kotori316/QuarryPlus"
                packaging = "jar"
                withXml {
                    val dependencyNode = asNode()["dependencies"] as NodeList
                    dependencyNode.filterIsInstance<Node>().forEach { node ->
                        // remove all dependencies
                        node.parent().remove(node)
                    }
                }
            }
        }
    }
}
