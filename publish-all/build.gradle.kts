import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.publish.all)
}

version = project(":common").version

val changelogProvider: Provider<String> = provider {
    val time = ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val fromFile = rootProject.file(project.property("changelog_file")!!).readText()
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

publishMods {
    changelog = changelogProvider
    dryRun = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
    displayName = "v${project.version} for Minecraft ${libs.versions.minecraft.get()}"
    type = if (project.version.toString().contains("SNAPSHOT")) BETA else STABLE

    val releaseJarFiles = getReleaseJarFiles()
    file = releaseJarFiles.first()
    additionalFiles.from(
        *releaseJarFiles.drop(1).toTypedArray()
    )

    github {
        repository = "Kotori316/QuarryPlus"
        accessToken = project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: ""
        commitish = project.property("mod.branch") as String
        tagName = "v${project.version}"
    }
}

fun getReleaseJarFiles(): List<Provider<RegularFile>> {
    val list = mutableListOf<Provider<RegularFile>>()
    if (!(System.getenv("DISABLE_FORGE") ?: "false").toBoolean()) {
        list.add(project(":forge").tasks.named("jar", AbstractArchiveTask::class).flatMap { it.archiveFile })
        list.add(project(":forge").tasks.named("sourcesJar", AbstractArchiveTask::class).flatMap { it.archiveFile })
    }
    if (!(System.getenv("DISABLE_FABRIC") ?: "false").toBoolean()) {
        list.add(project(":fabric").tasks.named("remapJar", AbstractArchiveTask::class).flatMap({ it.archiveFile }))
        list.add(
            project(":fabric").tasks.named("remapSourcesJar", AbstractArchiveTask::class).flatMap({ it.archiveFile })
        )
    }
    if (!(System.getenv("DISABLE_NEOFORGE") ?: "false").toBoolean()) {
        list.add(project(":neoforge").tasks.named("jar", AbstractArchiveTask::class).flatMap { it.archiveFile })
        list.add(project(":neoforge").tasks.named("sourcesJar", AbstractArchiveTask::class).flatMap { it.archiveFile })
    }
    return list
}
