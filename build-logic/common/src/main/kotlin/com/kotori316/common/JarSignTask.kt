package com.kotori316.common

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.withGroovyBuilder

abstract class JarSignTask : DefaultTask() {
    @get:Input
    abstract val jarTask: Property<Jar>

    init {
        val canJarSign: Boolean = project.hasProperty("jarSign.keyAlias") &&
                project.hasProperty("jarSign.keyLocation") &&
                project.hasProperty("jarSign.storePass")
        onlyIf("runs only with jar sign keys") { canJarSign }
    }

    @TaskAction
    fun sign() {
        ant.withGroovyBuilder {
            "signjar"(
                "jar" to jarTask.get().archiveFile.get(),
                "alias" to project.findProperty("jarSign.keyAlias"),
                "keystore" to project.findProperty("jarSign.keyLocation"),
                "storepass" to project.findProperty("jarSign.storePass"),
                "sigalg" to "Ed25519",
                "digestalg" to "SHA-256",
                "tsaurl" to "http://timestamp.digicert.com",
            )
        }
    }
}
