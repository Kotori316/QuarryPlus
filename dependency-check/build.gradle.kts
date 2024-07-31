plugins {
    id("com.kotori316.common")
}

dependencies {
    // JEI
    /*implementation(
        group = "mezz.jei",
        name = "jei-1.21-forge",
        version = project.property("jei_forge_version").toString()
    )
    implementation(
        group = "mezz.jei",
        name = "jei-1.21-fabric",
        version = project.property("jei_fabric_version").toString()
    )
    implementation(
        group = "mezz.jei",
        name = "jei-1.21-neoforge",
        version = project.property("jei_neoforge_version").toString()
    )*/
    // Parchment
    implementation(
        group = "org.parchmentmc.data",
        name = "parchment-${project.property("parchment.minecraft")}",
        version = project.property("parchment.mapping").toString(),
        ext = "zip"
    )
}
