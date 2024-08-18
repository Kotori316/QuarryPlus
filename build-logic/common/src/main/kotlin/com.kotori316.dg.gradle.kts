plugins {
    id("java")
    id("scala")
}

sourceSets {
    create("dataGen") {
        val s = this
        java {
            srcDir("src/dataGen/java")
        }
        scala {
            srcDir("src/dataGen/scala")
        }
        resources {
            srcDir("src/dataGen/resources")
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
    create("dataGenRuntime")
}

tasks.named("processDataGenResources", ProcessResources::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val catalog = project.versionCatalogs.named("libs")
dependencies {
    "dataGenImplementation"(project.sourceSets.main.get().output)
    "dataGenImplementation"(catalog.findLibrary("scala").get().get())
    if (project.name != "common") {
        "dataGenImplementation"(project.project(":common").sourceSets["dataGen"].output)
    }
}
