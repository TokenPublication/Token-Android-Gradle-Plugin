package com.tokeninc.tools.build.plugin.publisher

import com.tokeninc.tools.build.controller.PublisherCredentialManager
import com.tokeninc.tools.build.utils.CredentialUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import java.net.URI

class PublisherPlugin : Plugin<Project> {
    override fun apply(target: Project) {

        if (hasCredentialArguments(target.properties)) {
            println("Detected valid credential arguments")

            val snapshotRepoUrl = target.properties["snapshot-repo-url"].toString()
            val snapshotPublisherUsr = target.properties["snapshot-publisher-usr"].toString()
            val snapshotPublisherPwd = target.properties["snapshot-publisher-pwd"].toString()

            target.afterEvaluate {
                try {
                    target.plugins.getPlugin("com.android.library")
                } catch (e: Exception) {
                    println("project ${target.name} is not an android library")
                    return@afterEvaluate
                }
                println("Configuring snapshot publish tasks for ${target.name} in build.gradle")
                var packageAppVersion: String = ""
                var packageGroupId: String = ""
                var packageArtifactId: String = ""
                with(target.pluginManager) {
                    this.withPlugin("com.android.library") {
                        target.version.toString().let { version ->
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
                            repo.url = URI(snapshotRepoUrl)
                            repo.credentials { cred ->
                                cred.username = snapshotPublisherUsr
                                cred.password = snapshotPublisherPwd
                            }
                        }
                    }
                }
            }

            if (target.properties.getOrDefault("save", "").toString().isNotEmpty()) {
                PublisherCredentialManager
                        .saveCredentialsFromArgument(snapshotPublisherUsr,
                                snapshotPublisherPwd,
                                snapshotRepoUrl)
            }
        } else if (PublisherCredentialManager.checkCredentials()) {
            println("Found valid credentials")

            val snapshotRepoUrl = PublisherCredentialManager.getURL()
            val snapshotPublisherUsr = PublisherCredentialManager.getUserName()
            val snapshotPublisherPwd = PublisherCredentialManager.getPassword()

            target.afterEvaluate {
                try {
                    target.plugins.getPlugin("com.android.library")
                } catch (e: Exception) {
                    println("project ${target.name} is not an android library")
                    return@afterEvaluate
                }
                println("Configuring snapshot publish tasks for ${target.name} in build.gradle")
                var packageAppVersion: String = ""
                var packageGroupId: String = ""
                var packageArtifactId: String = ""
                with(target.pluginManager) {
                    this.withPlugin("com.android.library") {
                        target.version.toString().let { version ->
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
                            repo.url = URI(snapshotRepoUrl)
                            repo.credentials { cred ->
                                cred.username = snapshotPublisherUsr
                                cred.password = snapshotPublisherPwd
                            }
                        }
                    }
                }
            }
        } else {
            PublisherCredentialManager.showPanel()
        }
    }

    private fun hasCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isSnapshotPublisherProvided(projectProperties)
    }

    private fun hasValidCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isSnapshotPublishable(projectProperties)
    }
}