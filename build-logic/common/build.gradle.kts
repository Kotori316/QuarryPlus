plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://maven.kotori316.com") }
}

dependencies {
    mapOf(
        "com.matthewprenger.cursegradle" to libs.versions.gradle.curse.get(),
        "com.modrinth.minotaur" to libs.versions.gradle.minotaur.get(),
        "com.kotori316.plugin.cf" to libs.versions.gradle.cf.get(),
    ).forEach { (name, version) ->
        implementation(group = name, name = "${name}.gradle.plugin", version = version)
    }
}
