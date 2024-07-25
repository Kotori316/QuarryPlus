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

val minecraft: String = catalog.findVersion("minecraft").map { it.requiredVersion }.get()
val curseProjectId = "282837"
val modrinthProjectId = "jhxX1zVW"
val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

publishMods {
    dryRun = releaseDebug
    type = ALPHA
    modLoaders = listOf(project.name)
    displayName = "${project.version}-${project.name}"
    afterEvaluate {
        file = provider {
            project.tasks.named(ext["publishJarTaskName"].toString(), org.gradle.jvm.tasks.Jar::class)
        }.flatMap { it }.flatMap { it.archiveFile }
        changelog = provider {
            "FluidTank for Minecraft $minecraft with ${mapPlatformToCamel(project.name)}"
        }
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
        if (project.name == "fabric") {
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
        if (project.name == "fabric") {
            requires {
                slug = "automatic-potato"
            }
        }
    }
}
