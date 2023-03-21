package com.tokeninc.tools.build.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import java.net.URI

class PublisherPlugin: Plugin<Project> {
    override fun apply(target: Project) {

        val releaseRepoUrl = target.properties.getOrDefault("release-repo-url", "https://google.com").toString()
        val releaseConsumerUsr = target.properties.getOrDefault("release-consumer-usr", "not-found").toString()
        val releaseConsumerPwd = target.properties.getOrDefault("release-consumer-pwd", "not-found").toString()

        target.afterEvaluate {
            try {
                target.plugins.getPlugin("com.android.library")
            }  catch (e: Exception) {
                println("project ${target.name} is not an android library")
                return@afterEvaluate
            }
            println("Configuring snapshot publish tasks for ${target.name} in build.gradle")
            var packageAppVersion: String = ""
            var packageGroupId: String = ""
            var packageArtifactId: String = ""
            with(target.pluginManager) {
                this.withPlugin("com.android.library"){
                    target.version.toString().let{ version ->
                        packageAppVersion = if (version != "unspecified")
                            version
                        else
                            "0.0.0"
                    }
                    packageGroupId = target.group.toString()
                    packageArtifactId = target.name
                    println("Package version name: $packageAppVersion, group Id: $packageGroupId, artifact Id: $packageArtifactId")
                }
            }

            target.pluginManager.apply(MavenPublishPlugin::class.java)
            target.extensions.configure(PublishingExtension::class.java) { publishing ->
                publishing.publications { container ->
                    container.create("snapshot", MavenPublication::class.java) { publication ->
                        publication.groupId = packageGroupId
                        publication.artifactId = packageArtifactId
                        publication.version = "$packageAppVersion-SNAPSHOT"
                    }
                }
                publishing.repositories { repos ->
                    repos.maven { repo ->
                        repo.url = URI(releaseRepoUrl)
                        repo.credentials { cred ->
                            cred.username = releaseConsumerUsr
                            cred.password = releaseConsumerPwd
                        }
                    }
                }
            }
        }
    }


}