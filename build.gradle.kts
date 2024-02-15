plugins {
    id("fabric-loom") version "1.5.7"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    val imguiVersion = property("imgui_version")!!
    implementation(shadow("io.github.spair:imgui-java-binding:$imguiVersion")!!)
    implementation(shadow("io.github.spair:imgui-java-lwjgl3:$imguiVersion")!!)
    implementation(shadow("io.github.spair:imgui-java-natives-windows:$imguiVersion")!!)
    implementation(shadow("io.github.spair:imgui-java-natives-linux:$imguiVersion")!!)
    implementation(shadow("io.github.spair:imgui-java-natives-macos:$imguiVersion")!!)
}

loom {
    accessWidenerPath.set(file("src/main/resources/hajlib.accesswidener"))
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    jar {
        enabled = false
    }

    shadowJar {
        finalizedBy(remapJar)

        from(sourceSets.main.get().output)

        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set(jar.get().archiveClassifier)
        destinationDirectory.set(jar.get().destinationDirectory)

        dependencies {
            exclude(dependency("org.lwjgl:lwjgl"))
            exclude(dependency("org.lwjgl:lwjgl-glfw"))
            exclude(dependency("org.lwjgl:lwjgl-opengl"))
        }
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/n2pm/hajlib")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
