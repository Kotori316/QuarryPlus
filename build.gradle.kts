import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext") version ("1.1.8")
    id("com.github.breadmoirai.github-release") version ("2.5.2")
}

val minecraft: String by project
val mcVersionSplit = minecraft.split(".").drop(1)
val major = mcVersionSplit[0]
val minor = mcVersionSplit.getOrElse(1) { _ -> "0" }
val versionMinor: String? by project
val patch = System.getenv("GITHUB_RUN_NUMBER") ?: versionMinor ?: "0"
version = "${major}.${minor}.${patch}"

githubRelease {
    owner = "Kotori316"
    repo = "QuarryPlus"
    token(project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: "")
    val branch: String by project
    targetCommitish = branch
    prerelease = false
    val time = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    body = """
        QuarryPlus for Minecraft ${libs.versions.minecraft.get()}
        
        Released at $time
        
        | Name | Version |
        | -- | -- |
        | Minecraft | ${libs.versions.minecraft.get()} |
        | Forge | ${libs.versions.forge.get()} |
        | Fabric | ${libs.versions.fabric.api.get()} |
        | NeoForge | ${libs.versions.neoforge.get()} |
        | Scalable Cat's Force for Forge and NeoForge | ${libs.versions.slp.forge.get()} |
        | Scalable Cat's Force for Fabric | ${libs.versions.slp.fabric.get()} |
    """.trimIndent()
    dryRun = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
    releaseAssets = files(
        *listOf("forge", "fabric", "neoforge").mapNotNull { name ->
            findProject(":$name")?.layout?.buildDirectory?.dir("libs")?.get()?.let { fileTree(it) }
        }.toTypedArray()
    )
}
