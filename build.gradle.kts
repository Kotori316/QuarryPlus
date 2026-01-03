plugins {
    alias(libs.plugins.idea.ext)
}

allprojects {
    // Required here, as setting buildscript from plugin doesn't work
    buildscript {
        configurations.all {
            resolutionStrategy.force("commons-io:commons-io:2.21.0")
        }
    }
}

buildscript {
    configurations.all {
        resolutionStrategy.force("commons-io:commons-io:2.21.0")
    }
}
