plugins {
    java
    alias(libs.plugins.fabric.loom)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
    maven("https://repo.bluecolored.de/releases")
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
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "utf-8"
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.named<ProcessResources>("processResources") {
    from("src/main/resources") {
        include("fabric.mod.json")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        expand(
            "version" to project.version,
            "fabric_loader_version" to libs.versions.fabric.loader.get(),
            "fabric_api_version" to libs.versions.fabric.api.get().substringBefore("+"),
        )
    }
}

tasks.named("build") {
    dependsOn(tasks.named("jar"))
}
