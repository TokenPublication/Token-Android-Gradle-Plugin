package com.tokeninc.tools.build.settings


import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.net.URI

class SettingsPlugin: Plugin<Settings> {
    override fun apply(target: Settings) {
        println("Adding Token Maven Repositories in settings.gradle")
        target.pluginManagement { management ->
            management.repositories { handler ->
                handler.mavenLocal()
                handler.google()
                handler.gradlePluginPortal()
            }
        }

        val snapshotRepoUrl = target.startParameter.projectProperties.getOrDefault("snapshot-repo-url", "https://google.com")
        val snapshotConsumerUsr = target.startParameter.projectProperties.getOrDefault("snapshot-consumer-usr", "not-found")
        val snapshotConsumerPwd = target.startParameter.projectProperties.getOrDefault("snapshot-consumer-pwd", "not-found")

        val releaseRepoUrl = target.startParameter.projectProperties.getOrDefault("release-repo-url", "https://google.com")
        val releaseConsumerUsr = target.startParameter.projectProperties.getOrDefault("release-consumer-usr", "not-found")
        val releaseConsumerPwd = target.startParameter.projectProperties.getOrDefault("release-consumer-pwd", "not-found")

        target.dependencyResolutionManagement { management ->
            management.repositories { config ->
                config.maven { repo ->
                    repo.url = URI(snapshotRepoUrl)
                    repo.credentials {
                        it.username = snapshotConsumerUsr
                        it.password = snapshotConsumerPwd
                    }
                }

                config.maven { repo ->
                    repo.url = URI(releaseRepoUrl)
                    repo.credentials {
                        it.username = releaseConsumerUsr
                        it.password = releaseConsumerPwd
                    }
                }
            }
        }
    }
}