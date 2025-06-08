architectury {
    common("neoforge", "fabric")
    platformSetupLoomIde()
}

val minecraftVersion = project.properties["minecraft_version"] as String

loom.accessWidenerPath.set(file("src/main/resources/rarecandylib.accesswidener"))

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modCompileOnly(fileTree(mapOf("dir" to "libs", "include" to "*.jar")))

    implementation("gg.generations:RareCandy:${project.properties["rarecandy"]}"){isTransitive = false}
    modApi("dev.architectury:architectury:${project.properties["architectury_version"]}")

    //Cobblemon
    modCompileOnly("com.cobblemon:mod:${project.properties["cobblemon_version"]}")
}
