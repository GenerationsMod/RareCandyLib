architectury {
    common("neoforge", "fabric")
    platformSetupLoomIde()
}

val minecraftVersion = project.properties["minecraft_version"] as String

loom.accessWidenerPath.set(file("src/main/resources/rarecandylib.accesswidener"))

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modCompileOnly(localLib("molang", "1.1.11"))
    implementation(localLib("rarecandy", "${project.properties["rarecandy"]}"))

    //Cobblemon
    modCompileOnly("com.cobblemon:mod:${project.properties["cobblemon_version"]}")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.moulberry:mixinconstraints:1.0.9")
}

private fun DependencyHandlerScope.localLib(name: String, version: String): Any {
    return files("../libs/$name-$version.jar")
}