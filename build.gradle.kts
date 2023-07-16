import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("fabric-loom") version "1.2-SNAPSHOT"
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

val hajlibShadowJar = tasks.register<ShadowJar>("hajlibShadowJar") {
    dependsOn(tasks.remapJar)
    from(tasks.remapJar.get().outputs.files)

    configurations = listOf(project.configurations.shadow.get())
    archiveBaseName.set("hajlib")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")

    dependencies {
        exclude(dependency("org.lwjgl:lwjgl"))
        exclude(dependency("org.lwjgl:lwjgl-glfw"))
        exclude(dependency("org.lwjgl:lwjgl-opengl"))
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    remapJar {
        archiveBaseName.set("hajlib")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("remapped")

        finalizedBy(hajlibShadowJar)
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
