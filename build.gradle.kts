import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext") version ("1.1.7")
    id("com.github.breadmoirai.github-release") version ("2.4.1")
}

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
