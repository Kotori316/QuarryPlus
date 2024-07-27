plugins {
    id("com.kotori316.common")
    alias(libs.plugins.forge.gradle)
    alias(libs.plugins.forge.parchment)
    id("com.kotori316.publish")
}

minecraft {
    mappings(
        mapOf(
            "channel" to "parchment",
            "version" to "${libs.versions.parchment.minecraft.get()}-${libs.versions.parchment.mapping.get()}-${libs.versions.minecraft.get()}",
        )
    )
    reobf = false
}

dependencies {
    minecraft(libs.forge)
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.forge) { classifier("with-library") }) {
        isTransitive = false
    }
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
