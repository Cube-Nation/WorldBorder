plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.wimbli.WorldBorder"
version = "1.19-CN"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.mikeprimm.com/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly(group = "us.dynmap", name = "dynmap-api", version = "3.1")
    implementation(group = "io.papermc", name = "paperlib", version = "1.0.8-SNAPSHOT")
    compileOnly("com.github.yannicklamprecht:worldborderapi:1.211.0:dev")
}

defaultTasks("clean", "build")

tasks {
    processResources {
        val placeholders = mapOf(
            "name" to project.name,
            "group" to project.group,
            "version" to project.version
        )
        filesMatching("plugin.yml") {
            expand(placeholders)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"
            from(components["java"])
        }
    }
}
