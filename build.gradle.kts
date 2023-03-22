plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.12.0"
    `maven-publish`
    `java-gradle-plugin`
}

group = "com.tokeninc.tools.build"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
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
    plugins {
        create("consumer") {
            id = "com.tokeninc.tools.build.consumer"
            implementationClass = "com.tokeninc.tools.build.plugin.consumer.ConsumerPlugin"
        }
        create("publisher") {
            id = "com.tokeninc.tools.build.publisher"
            implementationClass = "com.tokeninc.tools.build.plugin.publisher.PublisherPlugin"
        }
        create("settings") {
            id = "com.tokeninc.tools.build.settings"
            implementationClass = "com.tokeninc.tools.build.plugin.settings.SettingsPlugin"
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
