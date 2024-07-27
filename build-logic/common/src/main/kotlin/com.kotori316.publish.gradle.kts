import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import gradle.kotlin.dsl.accessors._d127c8cf8be369f13d50127c4af720c5.publishing
import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("me.modmuss50.mod-publish-plugin")
}

val catalog = project.versionCatalogs.named("libs")

fun mapPlatformToCamel(platform: String): String {
    return when (platform) {
        "forge" -> "Forge"
        "fabric" -> "Fabric"
        "neoforge" -> "NeoForge"
        else -> throw IllegalArgumentException("Unknown platform $platform")
    }
}

val platformName = project.name
val minecraft: String = catalog.findVersion("minecraft").map { it.requiredVersion }.get()
val curseProjectId = "282837"
val modrinthProjectId = "jhxX1zVW"
val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

val modChangelog: Provider<String> = provider {
    val fromFile = rootProject.file(project.property("changelog_file")!!).readText()
    """
        QuarryPlus(v${project.version}) for Minecraft $minecraft
        ---
    """.trimIndent() + System.lineSeparator() + fromFile
}

publishMods {
    dryRun = releaseDebug
    type = ALPHA
    modLoaders = listOf(platformName)
    displayName = "v${project.version}-$platformName"
    afterEvaluate {
        file = provider {
            project.tasks.named(ext["publishJarTaskName"].toString(), org.gradle.jvm.tasks.Jar::class)
        }.flatMap { it }.flatMap { it.archiveFile }
        changelog = modChangelog
    }

    curseforge {
        projectId = curseProjectId
        accessToken = (
                project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN") ?: ""
                ) as String
        minecraftVersions = listOf(minecraft)
        projectSlug = "additional-enchanted-miner"
        requires {
            slug = "scalable-cats-force"
        }
        if (platformName == "fabric") {
            requires {
                slug = "automatic-potato"
            }
        }
    }
    modrinth {
        projectId = modrinthProjectId
        accessToken = (
                project.findProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN") ?: ""
                ) as String
        minecraftVersions = listOf(minecraft)
        requires {
            slug = "scalable-cats-force"
        }
        if (platformName == "fabric") {
            requires {
                slug = "automatic-potato"
            }
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

fun getPlatformVersion(platform: String): String {
    return when (platform) {
        "forge" -> catalog.findVersion("forge").map { it.requiredVersion }.orElseThrow()
        "neoforge" -> catalog.findVersion("neoforge").map { it.requiredVersion }.orElseThrow()
        "fabric" -> catalog.findVersion("fabric_api").map { it.requiredVersion }.orElseThrow()
        else -> throw IllegalArgumentException("Unsupported platform: $platform")
    }
}

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = minecraft
    platform = platformName
    platformVersion = getPlatformVersion(platformName)
    modName = "QuarryPlus".lowercase()
    changelog = modChangelog
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

tasks.register("data") {
    doLast {
        println(modChangelog.get())
    }
}
