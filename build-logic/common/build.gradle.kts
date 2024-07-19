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
        "com.kotori316.plugin.cf" to libs.versions.plugin.cf.get(),
    ).forEach { (name, version) ->
        implementation(group = name, name = "${name}.gradle.plugin", version = version)
    }
}
