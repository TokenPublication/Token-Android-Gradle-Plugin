plugins {
    id("com.gradle.plugin-publish") version "1.1.0"
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.12.0"
}

group = "com.tokeninc.tools"
version = "0.1"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.1.4")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("200")
        untilBuild.set("231.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

gradlePlugin {
    website.set("https://github.com/TokenPublication/Token-Android-Gradle-Plugin")
    vcsUrl.set("https://github.com/TokenPublication/Token-Android-Gradle-Plugin")
    plugins {
        create("build") {
            id = "com.tokeninc.tools.build"
            implementationClass = "com.tokeninc.tools.plugin.build.BuildPlugin"
            displayName = "Tokeninc Gradle Build Consumer Tool (Project)"
            description = "Used in project level gradle for consuming maven repositories"
            tags.set(listOf("token", "tokeninc", "android", "maven", "java", "consume", "project", "automation"))
        }
        create("publish") {
            id = "com.tokeninc.tools.publish"
            implementationClass = "com.tokeninc.tools.plugin.publish.PublishPlugin"
            displayName = "Tokeninc Gradle Publisher Tool (Module Config)"
            description = "Used in module level gradle for publishing to maven repositories"
            tags.set(listOf("token", "tokeninc", "android", "maven", "java", "publish", "library", "aar", "project", "automation"))
        }
        create("settings") {
            id = "com.tokeninc.tools.settings"
            implementationClass = "com.tokeninc.tools.plugin.settings.SettingsPlugin"
            displayName = "Tokeninc Gradle Build Consumer Tool (Settings)"
            description = "Used in settings.gradle for consuming maven repositories"
            tags.set(listOf("token", "tokeninc", "android", "maven", "java", "consume", "settings", "automation"))
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("maven-repo"))
        }
        ivy {
            url = uri(layout.buildDirectory.dir("ivy-repo"))
        }
    }
}
