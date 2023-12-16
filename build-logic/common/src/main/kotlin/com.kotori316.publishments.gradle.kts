import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import groovy.util.Node
import groovy.util.NodeList
import java.nio.file.Files
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

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

fun changelogHeader(): String {
    val time = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    return """
        QuarryPlus for Minecraft $minecraft with $platformName
        Released at $time
        
        This mod requires Scalable Cat's Force([CurseForge](https://curseforge.com/minecraft/mc-mods/scalable-cats-force), [Modrinth](https://modrinth.com/mod/scalable-cats-force))
    """.trimIndent()
}

fun changelog(): String {
    val file = rootProject.file("temp_changelog.md")
    return if (file.exists()) {
        file.useLines { c ->
            changelogHeader() + System.lineSeparator().repeat(2) + c.joinToString(System.lineSeparator())
        }
    } else {
        changelogHeader() + System.lineSeparator().repeat(2) + "No changelog provided"
    }
}

fun shortChangelog(): String {
    val file = rootProject.file("temp_changelog.md").toPath()
    return if (Files.exists(file)) {
        val content = Files.lines(file)
            .takeWhile { s -> !s.startsWith("-") }
            .collect(Collectors.joining(System.lineSeparator()))
        changelogHeader() + System.lineSeparator().repeat(2) + content
    } else {
        changelogHeader() + System.lineSeparator().repeat(2) + "No changelog provided"
    }
}

tasks.register("checkChangeLog") {
    description = "Verify the changelog"
    group = "help"
    doLast {
        println("Long changelog in $platformName")
        println("*".repeat(30))
        println(changelog())
        println("*".repeat(30))
        println("Short changelog in $platformName")
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
        mainArtifact(tasks.jar.flatMap { it.archiveFile }.get(), closureOf<CurseArtifact> {
            displayName = "v${project.version}-${platformName} [$minecraft]"
        })
        addArtifact(tasks.named("deobfJar", Jar::class).flatMap { it.archiveFile }.get())
        addArtifact(tasks.named("sourcesJar", Jar::class).flatMap { it.archiveFile }.get())
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
        if (System.getenv("CLOUDFLARE_S3_ENDPOINT") != null) {
            val r2AccessKey = (project.findProperty("r2_access_key") ?: System.getenv("R2_ACCESS_KEY") ?: "") as String
            val r2SecretKey = (project.findProperty("r2_secret_key") ?: System.getenv("R2_SECRET_KEY") ?: "") as String
            maven {
                name = "kotori316-maven"
                url = uri("s3://kotori316-maven")
                credentials(AwsCredentials::class) {
                    accessKey = r2AccessKey
                    secretKey = r2SecretKey
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
        mustRunAfter(":${platformName}:signMavenJavaPublication")
    }
}
