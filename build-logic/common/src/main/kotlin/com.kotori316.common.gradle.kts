plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

fun getPlatform(project: Project): String {
    return project.name
}

base {
    group = "com.kotori316"
    val minecraft: String by project
    archivesName.set("AdditionalEnchantedMiner-${minecraft}-${getPlatform(project)}")
    val mcVersionSplit = minecraft.split(".").drop(1)
    val major = mcVersionSplit[0]
    val minor = mcVersionSplit.getOrElse(1) { _ -> "0" }
    val versionMinor: String? by project
    val patch = System.getenv("GITHUB_RUN_NUMBER") ?: versionMinor ?: "0"
    version = "${major}.${minor}.${patch}"
}

println(
    "Java: ${System.getProperty("java.version")} " +
            "JVM: ${System.getProperty("java.vm.version")}(${System.getProperty("java.vendor")}) " +
            "Arch: ${System.getProperty("os.arch")} " +
            "Version: ${project.version} Platform: ${getPlatform(project)}"
)

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
            srcDir("src/generated/resources")
        }
    }
}

repositories {
    maven {
        name = "Kotori316-main"
        url = uri("https://maven.kotori316.com/")
        content {
            includeGroup("com.kotori316")
        }
    }
    maven {
        name = "Azure-SLP"
        url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
        content {
            includeGroup("com.kotori316")
            includeModule("org.typelevel", "cats-core_3")
            includeModule("org.typelevel", "cats-kernel_3")
            includeModule("org.typelevel", "cats-free_3")
        }
    }
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        name = "architectury"
        url = uri("https://maven.architectury.dev/")
        content {
            includeGroup("dev.architectury")
            includeGroup("me.shedaniel")
            includeGroup("me.shedaniel.cloth")
        }
    }
    maven {
        name = "CraftTweaker and JEI"
        url = uri("https://maven.blamejared.com")
        content {
            includeGroup("mezz.jei")
        }
    }
    maven {
        name = "ModMenu"
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeModule("com.terraformersmc", "modmenu")
        }
    }
    maven {
        name = "What The Hell Is That"
        url = uri("https://maven.bai.lol")
        content {
            includeModule("mcp.mobius.waila", "wthit-api")
            includeModule("mcp.mobius.waila", "wthit")
            includeModule("lol.bai", "badpackets")
        }
    }
    maven {
        name = "FTB NEW"
        url = uri("https://maven.saps.dev/releases/")
        content {
            includeGroup("dev.ftb.mods")
            includeGroup("dev.latvian.mods")
        }
    }
    mavenCentral()
    mavenLocal()
}
