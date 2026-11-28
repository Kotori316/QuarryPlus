package com.kotori316.common

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class JarSignTask @Inject constructor(private val execOperations: ExecOperations) : DefaultTask() {

    @get:InputFile
    abstract val jarFile: RegularFileProperty

    @get:Input
    abstract val keyAlias: Property<String>

    @get:Input
    abstract val keyStore: Property<String>

    @get:Internal
    abstract val storePass: Property<String>

    @get:Input
    abstract val tsaUrl: Property<String>

    @get:Input
    abstract val sigAlg: Property<String>

    @get:Input
    abstract val digestAlg: Property<String>

    init {
        tsaUrl.convention("http://timestamp.digicert.com")
        sigAlg.convention("Ed25519")
        digestAlg.convention("SHA-256")
    }

    @TaskAction
    fun sign() {
        execOperations.exec {
            commandLine(
                "jarsigner",
                "-keystore", keyStore.get(),
                "-storepass", storePass.get(),
                "-tsa", tsaUrl.get(),
                "-sigalg", sigAlg.get(),
                "-digestalg", digestAlg.get(),
                jarFile.get().asFile.absolutePath,
                keyAlias.get(),
            )
        }
    }
}
