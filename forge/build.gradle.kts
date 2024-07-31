plugins {
    id("com.kotori316.common")
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.forge.parchment)
    id("com.kotori316.publish")
}

val modId = "QuarryPlus".lowercase()

minecraft {
    mappings(
        mapOf(
            "channel" to "parchment",
            "version" to "${project.property("parchment.minecraft")}-${project.property("parchment.mapping")}-${libs.versions.minecraft.get()}",
        )
    )
    reobf = false

    runs {
        configureEach {
            property("forge.logging.markers", "")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            property("mixin.debug.export", "true")
            property("forge.logging.console.level", "debug")
        }

        create("client") {
            workingDirectory(project.file("Minecraft"))
            property("forge.enabledGameTestNamespaces", modId)
            args("--accessToken", "0")
            jvmArgs("-EnableAssertions".lowercase())

            mods {
                create(modId) {
                    source(sourceSets["main"])
                    // source(sourceSets.test)
                }
            }
        }
    }
}

dependencies {
    minecraft(libs.forge)
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.forge) { classifier("with-library") }) {
        isTransitive = false
    }
    runtimeOnly(libs.jei.forge)
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }
}

// Share with common
tasks.compileJava {
    options.encoding = "UTF-8"
    source(project(":common").sourceSets.main.get().allSource)
}
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(project(":common").sourceSets.main.get().resources)
}

// Forge stuff
sourceSets.forEach {
    val dir = layout.buildDirectory.dir("forgeSourcesSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
}

tasks.named("jar", Jar::class) {
    finalizedBy("jksSignJar")
}

tasks.register("jksSignJar", com.kotori316.common.JarSignTask::class) {
    dependsOn(tasks.jar)
    jarTask = tasks.jar
}

ext {
    set("publishJarTaskName", "jar")
}
