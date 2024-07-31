import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("com.kotori316.common")
    alias(libs.plugins.fabric.loom)
}

loom {
    knownIndyBsms.add("scala/runtime/LambdaDeserialize")
    knownIndyBsms.add("java/lang/runtime/SwitchBootstraps/typeSwitch")
}

tasks.withType(AbstractRemapJarTask::class) {
    enabled = false
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
            srcDir("src/generated/resources")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        val parchmentMC = project.property("parchment.minecraft")
        val parchmentDate = project.property("parchment.mapping")
        parchment("org.parchmentmc.data:parchment-$parchmentMC:$parchmentDate@zip")
    })
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modLocalRuntime(libs.fabric.loader)

    testImplementation(platform(libs.junit))
    testImplementation(libs.jupiter)
}
