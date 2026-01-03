plugins {
    id("com.kotori316.common")
    id("scala")
}

dependencies {
    // Parchment
    implementation(
        group = "org.parchmentmc.data",
        name = "parchment-${project.property("parchment.minecraft")}",
        version = project.property("parchment.mapping").toString(),
        ext = "zip",
    )
    // Scala 3
    implementation(libs.scala)
}
