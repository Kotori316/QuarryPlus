plugins {
    id("com.kotori316.common")
    alias(libs.plugins.neoforge.gradle)
    id("com.kotori316.publish")
}

val modId = "QuarryPlus".lowercase()

subsystems {
    parchment {
        minecraftVersion = libs.versions.parchment.minecraft.get()
        mappingsVersion = libs.versions.parchment.mapping.get()
    }
}

runs {
    configureEach {
        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        systemProperty("mixin.debug.export", "true")
        modSources.add(modId, sourceSets["main"])
    }

    create("client") {
        workingDirectory = project.file("run")
        programArguments("--username", "Kotori")
    }
}

dependencies {
    implementation(libs.neoforge)
    compileOnly(project(":common"))
    runtimeOnly(variantOf(libs.slp.neoforge) { classifier("with-library") }) {
        isTransitive = false
    }
    runtimeOnly(libs.du.neoforge)
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
