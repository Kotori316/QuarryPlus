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
        "com.matthewprenger.cursegradle" to "1.4.0",
        "com.modrinth.minotaur" to "2.+",
        "com.kotori316.plugin.cf" to "1.7",
    ).forEach { (name, version) ->
        implementation(group = name, name = "${name}.gradle.plugin", version = version)
    }
}
