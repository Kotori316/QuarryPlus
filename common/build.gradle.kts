plugins {
    id("com.kotori316.common")
    alias(libs.plugins.fabric.loom)
    id("com.kotori316.dg")
}

loom {
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
            srcDir("src/generated/resources")
        }
    }
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

dependencies {
    minecraft(libs.minecraft)
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    runtimeOnly(libs.fabric.loader)
    compileOnly(libs.config.core)
    testImplementation(libs.config.core)
    testRuntimeOnly(libs.config.toml)

    testImplementation(platform(libs.junit))
    testImplementation(libs.jupiter.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.bundles.mockito)

    "gameTestImplementation"(project.sourceSets.main.get().output)
    "gameTestImplementation"(platform(libs.junit))
    "gameTestImplementation"(libs.jupiter.core)
    "gameTestImplementation"(libs.du.common)
}

tasks.register("runGameTestServer")
