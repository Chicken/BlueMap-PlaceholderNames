plugins {
    java
    alias(libs.plugins.fabric.loom)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.extendedclip.com/releases/") {
        content {
            includeGroup("me.clip")
        }
    }
    maven("https://maven.nucleoid.xyz/") {
        content {
            includeGroup("eu.pb4")
        }
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

group = "codes.antti"
version = "0.1.0"

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    compileOnly(libs.bluemap.api)
    compileOnly(libs.spigot.api)
    compileOnly(libs.spigot.placeholder.api)
    implementation(libs.fabric.placeholder.api)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "utf-8"
    options.release = 25
}

tasks.withType<AbstractArchiveTask>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.FAIL
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.named<ProcessResources>("processResources") {
    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to libs.versions.minecraft.get(),
        "fabric_loader_version" to libs.versions.fabric.loader.get(),
        "fabric_api_version" to libs.versions.fabric.api.get().substringBefore("+"),
    )

    inputs.properties(properties)
    duplicatesStrategy = DuplicatesStrategy.FAIL

    filesMatching(listOf("fabric.mod.json", "plugin.yml")) {
        expand(properties)
    }
}
