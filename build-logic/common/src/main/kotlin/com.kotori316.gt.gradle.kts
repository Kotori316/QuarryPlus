plugins {
    id("java")
}

sourceSets {
    create("gameTest") {
        val s = this
        java {
            srcDir("src/gameTest/java")
        }
        resources {
            srcDir("src/gameTest/resources")
        }
        project.configurations {
            named(s.compileClasspathConfigurationName) {
                extendsFrom(project.configurations.compileClasspath.get())
            }
            named(s.runtimeClasspathConfigurationName) {
                extendsFrom(project.configurations.runtimeClasspath.get())
            }
        }
    }
}

configurations {
    create("gameTestRuntime")
}

tasks.named("processGameTestResources", ProcessResources::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val catalog = project.versionCatalogs.named("libs")
dependencies {
    "gameTestImplementation"(project.sourceSets.main.get().output)
    "gameTestImplementation"(project.project(":common").sourceSets["gameTest"].output)
    "gameTestImplementation"(platform(catalog.findLibrary("junit").get().get()))
    "gameTestImplementation"(catalog.findLibrary("jupiter").get().get())
}
