import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("com.kotori316.plugin.cf")
    id("idea")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

fun getPlatform(project: Project): String {
    return project.name
}

val catalog = project.versionCatalogs.named("libs")
val minecraft: String = catalog.findVersion("minecraft").map { it.requiredVersion }.get()
val modId = "quarryplus"
val releaseDebug: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

base {
    group = "com.kotori316"
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
        name = "Minecraft-Manually"
        url = uri("https://libraries.minecraft.net/")
        content {
            includeGroup("org.lwjgl")
            includeGroup("com.mojang")
        }
    }
    maven {
        name = "Kotori316-main"
        url = uri("https://maven.kotori316.com/")
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
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
    maven {
        name = "Mixin"
        url = uri("https://repo.spongepowered.org/maven")
        content {
            includeGroup("org.spongepowered")
        }
    }
    mavenCentral()
    mavenLocal()
    maven {
        name = "Kotori316Mirror"
        url = uri("https://storage.googleapis.com/kotori316-maven-storage/maven/")
        content {
            includeGroup("com.kotori316")
        }
    }
}

dependencies {
    // val catalog = project.versionCatalogs.named("libs")
    // compileOnly(catalog.findLibrary("scala").get().get())
}

val jarAttributeMap = mapOf(
    "Specification-Title" to "QuarryPlus",
    "Specification-Vendor" to "Kotori316",
    "Specification-Version" to "1",
    "Implementation-Title" to "QuarryPlus",
    "Implementation-Vendor" to "Kotori316",
    "Implementation-Version" to project.version as String,
    "Implementation-Timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
)

tasks.jar {
    manifest {
        attributes(jarAttributeMap)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named("sourcesJar", org.gradle.jvm.tasks.Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val projectVersion = project.version.toString()
    inputs.property("version", projectVersion)
    inputs.property("minecraftVersion", minecraft)
    listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml").forEach { fileName ->
        filesMatching(fileName) {
            expand(
                "version" to projectVersion,
                "update_url" to "https://version.kotori316.com/get-version/${minecraft}/${project.name}/${modId}",
                "mc_version" to minecraft,
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

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
