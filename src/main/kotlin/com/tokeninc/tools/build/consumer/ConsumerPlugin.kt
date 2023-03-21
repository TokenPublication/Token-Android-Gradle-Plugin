package com.tokeninc.tools.build.consumer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import java.net.URI


class ConsumerPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        println("Adding Token Maven Repositories to all projects in build.gradle")

        val snapshotRepoUrl = target.properties.getOrDefault("snapshot-repo-url", "https://google.com").toString()
        val snapshotConsumerUsr = target.properties.getOrDefault("snapshot-consumer-usr", "not-found").toString()
        val snapshotConsumerPwd = target.properties.getOrDefault("snapshot-consumer-pwd", "not-found").toString()

        val releaseRepoUrl = target.properties.getOrDefault("release-repo-url", "https://google.com").toString()
        val releaseConsumerUsr = target.properties.getOrDefault("release-consumer-usr", "not-found").toString()
        val releaseConsumerPwd = target.properties.getOrDefault("release-consumer-pwd", "not-found").toString()

        target.allprojects { project ->
            println("Configuring " + project.name)
            project.repositories.google()
            project.repositories.maven { repo ->
                repo.url = URI(snapshotRepoUrl)
                repo.credentials {
                    it.username = snapshotConsumerUsr
                    it.password = snapshotConsumerPwd
                }
            }
            project.repositories.maven { repo ->
                repo.url = URI(releaseRepoUrl)
                repo.credentials {
                    it.username = releaseConsumerUsr
                    it.password = releaseConsumerPwd
                }
            }
            project.configurations.all {
                it.resolutionStrategy.cacheChangingModulesFor(0, "seconds")
            }
        }
    }
}