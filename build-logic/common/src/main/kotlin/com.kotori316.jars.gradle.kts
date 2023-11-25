import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

configurations {
    create("game_test")
}

fun getCommit(branch: String): String {
    return try {
        val builder = ProcessBuilder("git", "rev-parse", branch)
        val process = builder.start()
        process.waitFor(1L, TimeUnit.SECONDS)
        BufferedReader(InputStreamReader(process.inputStream)).use { it.readLine() }
    } catch (e: IOException) {
        println("Caught $e")
        ""
    }
}

tasks.register("checkJarSetting") {
    description = "Verify the jar setting"
    group = "help"
    doLast {
        println("Archive: ${base.archivesName}")
        println("Group: $group")
        println("Version: $version")
        println("*".repeat(30))
        println("jar: ${tasks.jar.get().archiveFile.get()}")
        println("deobfJar: ${tasks.named("deobfJar", Jar::class).get().archiveFile.get()}")
        println("sourcesJar: ${tasks.named("sourcesJar", Jar::class).get().archiveFile.get()}")
        println("*".repeat(30))
        println("Repositories")
        repositories.forEach { r ->
            val urlOrClass = when (r) {
                is MavenArtifactRepository -> r.url
                else -> "Not maven" + r.javaClass
            }
            println("${r.name} $r $urlOrClass")
        }
    }
}

val jarAttributeMap = mapOf(
    "Specification-Title" to "QuarryPlus",
    "Specification-Vendor" to "Kotori316",
    "Specification-Version" to "1",
    "Implementation-Title" to "QuarryPlus",
    "Implementation-Vendor" to "Kotori316",
    "Implementation-Version" to project.version as String,
    "Implementation-Timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
    "Commit" to getCommit(project.findProperty("branch") as? String ?: "main"),
)

tasks.jar {
    manifest {
        attributes(jarAttributeMap)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named("sourcesJar", Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register("deobfJar", Jar::class) {
    from(project.sourceSets["main"].output)
    archiveClassifier.set("deobf")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(jarAttributeMap)
    }
}

artifacts {
    archives(tasks["deobfJar"])
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.test {
    useJUnitPlatform()
}

signing {
    sign(publishing.publications)
    sign(tasks.jar.get())
    sign(tasks.named("deobfJar").get())
    sign(tasks.named("sourcesJar").get())
}

// sign task creation is in `com.kotori316.jars.gradle.kts`
val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class) {
    onlyIf("runs only with signing keys") { hasGpgSignature }
}

tasks.withType(AbstractPublishToMaven::class) {
    if (hasGpgSignature) {
        dependsOn(*tasks.filterIsInstance<Sign>().toTypedArray())
    }
}
