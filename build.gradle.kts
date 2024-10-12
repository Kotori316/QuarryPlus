import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.idea.ext)
    alias(libs.plugins.github.release)
}

allprojects {
    // Required here, as setting buildscript from plugin doesn't work
    buildscript {
        configurations.all {
            resolutionStrategy.force("commons-io:commons-io:2.17.0")
        }
    }
}

val changelog: Provider<String> = provider {
    val time = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val fromFile = project.file(project.property("changelog_file")!!).readText()
    val shortFormat = fromFile.split(Regex("^# ", RegexOption.MULTILINE), limit = 3)[1]
    """
        QuarryPlus for Minecraft ${libs.versions.minecraft.get()}
        
        Released at $time
        
        | Name | Version |
        | -- | -- |
        | Minecraft | ${libs.versions.minecraft.get()} |
        | Forge | ${libs.versions.forge.get()} |
        | Fabric | ${libs.versions.fabric.api.get()} |
        | NeoForge | ${libs.versions.neoforge.get()} |
        
    """.trimIndent() + System.lineSeparator() + shortFormat
}

githubRelease {
    owner = "Kotori316"
    repo = "QuarryPlus"
    token(project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: "")
    targetCommitish = project.property("mod.branch") as String
    tagName = provider { "v${project.project(":common").version}" }
    releaseName = provider { "v${project.project(":common").version} for Minecraft ${libs.versions.minecraft.get()}" }
    prerelease = false
    body = changelog
    dryRun = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
}
