/*
 *
 *  * Copyright (C) 2023 Token Financial Technologies
 *
 */

package com.tokeninc.tools.plugin.publish

import com.tokeninc.tools.controller.PublisherCredentialManager
import com.tokeninc.tools.utils.CredentialUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import java.net.URI

class PublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {

        target.tasks.register("modifyPublisherCredentials") {
            it.group = "tokeninc"
            it.description = "Show Shows the Java GUI to modify the publisher credentials"
            it.doLast {
                PublisherCredentialManager.showPanel()
                println("modifying publisher credentials...")
            }
        }

        if (hasMinimumCredentialArguments(target.properties)) {
            println("Detected valid credential arguments")

            var credentialIndex = 1
            while(hasCredentialArguments(target.properties, credentialIndex)) {
                val repoUrl = target.properties["publish-repo-url-$credentialIndex"].toString()
                val repoUsr = target.properties["publish-repo-usr-$credentialIndex"].toString()
                val repoPwd = target.properties["publish-repo-pwd-$credentialIndex"].toString()
                credentialIndex++

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
                                repo.url = URI(repoUrl)
                                repo.credentials { cred ->
                                    cred.username = repoUsr
                                    cred.password = repoPwd
                                }
                            }
                        }
                    }
                    target.tasks.register("publishWithTokenPlugin") {
                        it.dependsOn("publishSnapshotPublicationToMavenRepository")
                    }
                }

                if (target.properties.getOrDefault("save", "").toString().isNotEmpty()) {
                    PublisherCredentialManager
                            .saveCredentialsFromArgument(repoUsr,
                                    repoPwd,
                                    repoUrl)
                }
            }
        } else if (PublisherCredentialManager.checkCredentials()) {
            println("Found valid credentials")

            val snapshotRepoUrl = PublisherCredentialManager.getCredentials()[0].getUrl()
            val snapshotPublisherUsr = PublisherCredentialManager.getCredentials()[0].getUserName()
            val snapshotPublisherPwd = PublisherCredentialManager.getCredentials()[0].getPwd()

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

    private fun hasMinimumCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isPublishCredentialProvided(projectProperties, 1)
    }

    private fun hasCredentialArguments(projectProperties: Map<String, *>, i: Int): Boolean {
        return CredentialUtil.isPublishCredentialProvided(projectProperties, i)
    }
}