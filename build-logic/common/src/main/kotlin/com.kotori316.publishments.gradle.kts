import com.kotori316.plugin.cf.CallVersionFunctionTask
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("com.matthewprenger.cursegradle")
    id("com.modrinth.minotaur")
    id("com.kotori316.plugin.cf")
}

val minecraft: String by project
val platformName: String = project.name

fun changelog(): String {
    return ""
}

fun shortChangelog(): String {
    return ""
}

tasks.register("checkChangeLog") {
    description = "Verify the changelog"
    group = "help"
    doLast {
        print("Long changelog in $platformName")
        println("*".repeat(30))
        println(changelog())
        println("*".repeat(30))
        print("Short changelog in $platformName")
        println("*".repeat(30))
        println(shortChangelog())
        println("*".repeat(30))
    }
}

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = minecraft
    platform = platformName
    modName = "QuarryPlus".lowercase()
    changelog = shortChangelog()
    homepage.set(
        if (platformName == "forge") "https://www.curseforge.com/minecraft/mc-mods/additional-enchanted-miner"
        else "https://modrinth.com/mod/additional-enchanted-miner"
    )
}

val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

curseforge {
    apiKey = project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN") ?: ""
    project(closureOf<CurseProject> {
        id = "282837"
        changelogType = "markdown"
        changelog = changelog()
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
        mainArtifact(tasks.jar, closureOf<CurseArtifact> {
            displayName = "v${project.version}-${platformName} [$minecraft]"
        })
        addArtifact(tasks.named("deobfJar"))
        addArtifact(tasks.named("sourcesJar"))
        relations(closureOf<CurseRelation> {
            requiredDependency("scalable-cats-force")
            if (platformName == "fabric") {
                requiredDependency("fabric-api")
                requiredDependency("cloth-config")
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
    uploadFile = tasks.jar.get()
    additionalFiles = listOf(tasks.named("deobfJar"), tasks.named("sourcesJar"))
    gameVersions = listOf(minecraft)
    loaders = listOf(platformName)
    changelog = shortChangelog()
    debugMode = releaseDebug
    dependencies {
        required.project("scalable-cats-force")
        if (platformName == "fabric") {
            required.project("fabric-api")
            required.project("cloth-config")
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
        maven {
            name = "AzureRepository"
            url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
            credentials {
                username = project.findProperty("azureUserName") as? String ?: System.getenv("AZURE_USER_NAME") ?: ""
                password = project.findProperty("azureToken") as? String ?: System.getenv("AZURE_TOKEN") ?: "TOKEN"
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
            artifact(tasks.named("deobfJar"))
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

afterEvaluate {
    rootProject.tasks.named("githubRelease") {
        dependsOn(":${platformName}:assemble")
    }
}
