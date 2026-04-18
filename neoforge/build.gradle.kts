plugins {
    id("com.gradleup.shadow")
//    id("com.hypherionmc.modutils.modpublisher") version "2.+"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    "common" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    create("shadowBundle")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentNeoForge").extendsFrom(configurations["common"])

    "shadowBundle" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    // Forge Datagen Gradle config.  Remove if not using Forge datagen
    runs.create("datagen") {
        data()
        programArgs("--all", "--mod", "examplemod")
        programArgs("--output", project(":common").file("src/main/generated/resources").absolutePath)
        programArgs("--existing", project(":common").file("src/main/resources").absolutePath)
    }
}

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    mavenCentral()
}

dependencies {
    neoForge("net.neoforged:neoforge:${project.properties["neoforge_version"]}")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":common", "transformProductionNeoForge"))

//    modRuntimeOnly("me.djtheredstoner:DevAuth-forge-latest:${project.properties["devauth_version"]}")

    "shadowBundle"(implementation(localLib("rarecandy", "${project.properties["rarecandy"]}"))!!)

    //Cobblemon
    implementation("thedarkcolour:kotlinforforge-neoforge:5.8.0")
    modImplementation("com.cobblemon:neoforge:${project.properties["cobblemon_version"]}")

    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.5")
    include("org.jetbrains.kotlinx:kotlinx-io-core:0.3.5")
    include(implementation("com.moulberry:mixinconstraints:1.0.9")!!)
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mutableMapOf(
                "version" to project.version,
                "neoforge" to project.properties["neoforge_version"],
                "minecraft" to project.properties["minecraft_version"],
                "cobblemon" to project.properties["cobblemon_version"],
                "wthit" to project.properties["WTHIT"],
                "description" to project.properties["description"]
            ))
        }
    }

    shadowJar {
        exclude(
            "org/lwjgl/system//**",
            "org/lwjgl/BufferUtils.class",
            "org/lwjgl/CLongBuffer.class",
            "org/lwjgl/PointerBuffer.class",
            "org/lwjgl/Version\$BuildType.class",
            "org/lwjgl/Version.class",
            "org/lwjgl/VersionImpl.class",
            "org/lwjgl/package-info.class",
            "architectury.common.json",
            ".cache/**")
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }
}

tasks.remapJar {
    atAccessWideners.add("rarecandylib.accesswidener")
}

private fun DependencyHandlerScope.localLib(name: String, version: String): Any {
    return files("../libs/$name-$version.jar")
}
