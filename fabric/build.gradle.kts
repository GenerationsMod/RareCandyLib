import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import com.hypherionmc.modpublisher.properties.ReleaseType
import kotlin.io.path.absolute

plugins {
    id("com.gradleup.shadow")
    id("com.hypherionmc.modutils.modpublisher") version "2.+"
}
architectury {
    platformSetupLoomIde()
    fabric()
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
    getByName("developmentFabric").extendsFrom(configurations["common"])
    "shadowBundle" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

loom.accessWidenerPath.set(project(":common").loom.accessWidenerPath)


// Fabric Datagen Gradle config.  Remove if not using Fabric datagen
fabricApi.configureDataGeneration()

repositories {
    maven("https://maven.terraformersmc.com/releases/")
    mavenCentral()
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}+$minecraftVersion")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":common", "transformProductionFabric"))

    "shadowBundle"(implementation(localLib("rarecandy", "${project.properties["rarecandy"]}"))!!)
    include(implementation("com.moulberry:mixinconstraints:1.0.9")!!)

    //Cobblemon
    modApi("com.cobblemon:fabric:${project.properties["cobblemon_version"]}")
    modRuntimeOnly("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_language_kotlin"]}")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to project.version,
                "fabricloader" to project.properties["fabric_loader_version"],
                "minecraft" to project.properties["minecraft_version"],
                "cobblemon" to project.properties["cobblemon_version"],
                "wthit" to project.properties["WTHIT"],
                "description" to project.properties["description"]
            ))
        }

        from(rootProject.file("common/src/main/resources")) {
            include("**/**")
            duplicatesStrategy = DuplicatesStrategy.WARN
        }
    }

    shadowJar {
        exclude(mutableListOf(
            "generations/gg/generations/core/generationscore/fabric/datagen/**",
            "data/forge/**",
            "data/generations_core/forge/**",
            "architectury.common.json",
            ".cache/**"
        ))
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }
}

private fun DependencyHandlerScope.localLib(name: String, version: String): Any {
    return files("../libs/$name-$version.jar")
}
