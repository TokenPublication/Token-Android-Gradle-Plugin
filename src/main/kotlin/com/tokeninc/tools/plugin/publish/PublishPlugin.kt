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
import org.slf4j.LoggerFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import java.net.URI

class PublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val logger = LoggerFactory.getLogger("token-logger")

        target.tasks.register("modifyPublisherCredentials") {
            it.group = "tokeninc"
            it.description = "Shows the Java GUI to modify the publisher credentials"
            it.doLast {
                PublisherCredentialManager.showPanel()
                logger.info("modifying publisher credentials...")
            }
        }

        if (hasMinimumCredentialArguments(target.properties)) {
            logger.info("Detected valid credential arguments")

            target.afterEvaluate {

                var credentialIndex = 1
                while (hasCredentialArguments(target.properties, credentialIndex)) {
                    val repoUrl = target.properties["publish-repo-url-$credentialIndex"].toString()
                    val repoUsr = target.properties["publish-repo-usr-$credentialIndex"].toString()
                    val repoPwd = target.properties["publish-repo-pwd-$credentialIndex"].toString()
                    try {
                        target.plugins.getPlugin("com.android.library")
                    } catch (e: Exception) {
                        logger.warn("project ${target.name} is not an android library")
                        return@afterEvaluate
                    }
                    logger.info("Configuring release publish tasks for ${target.name} in build.gradle")
                    var packageAppVersion = ""
                    var packageGroupId = ""
                    var packageArtifactId = ""
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
                            logger.info("Package version name: $packageAppVersion, group Id: $packageGroupId, artifact Id: $packageArtifactId")
                        }
                    }

                    target.pluginManager.apply(MavenPublishPlugin::class.java)
                    target.extensions.configure(PublishingExtension::class.java) { publishing ->
                        publishing.publications { container ->
                            container.create("release$credentialIndex", MavenPublication::class.java) { publication ->
                                publication.groupId = packageGroupId
                                publication.artifactId = packageArtifactId
                                publication.version = packageAppVersion
                                publication.from(target.components.findByName("release"))
                            }
                        }
                        publishing.repositories { repos ->
                            repos.maven { repo ->
                                repo.name = "mavenRelease$credentialIndex"
                                repo.url = URI(repoUrl)
                                repo.credentials { cred ->
                                    cred.username = repoUsr
                                    cred.password = repoPwd
                                }
                            }
                        }
                    }
                    if (target.properties.getOrDefault("save", "").toString().isNotEmpty()) {
                        PublisherCredentialManager
                                .saveCredentialsFromArgument(repoUsr,
                                        repoPwd,
                                        repoUrl)
                    }
                    credentialIndex++
                }
                target.tasks.register("publishReleasePublicationsWithTokenPlugin") {
                    it.group = "tokeninc"
                    it.description = "Publishes the artifact to the specified Maven Repositories"
                    for (i in 1 until credentialIndex) {
                        it.dependsOn("publishRelease${i}PublicationToMavenRelease${i}Repository")
                    }
                }
            }
        } else if (PublisherCredentialManager.checkCredentials()) {
            logger.info("Found valid credentials")

            val snapshotRepoUrl = PublisherCredentialManager.getCredentials()[0].getUrl()
            val snapshotPublisherUsr = PublisherCredentialManager.getCredentials()[0].getUserName()
            val snapshotPublisherPwd = PublisherCredentialManager.getCredentials()[0].getPwd()

            target.afterEvaluate {
                try {
                    target.plugins.getPlugin("com.android.library")
                } catch (e: Exception) {
                    logger.warn("project ${target.name} is not an android library")
                    return@afterEvaluate
                }
                logger.info("Configuring snapshot publish tasks for ${target.name} in build.gradle")
                var packageAppVersion = ""
                var packageGroupId = ""
                var packageArtifactId = ""
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
                        logger.info("Package version name: $packageAppVersion, group Id: $packageGroupId, artifact Id: $packageArtifactId")
                    }
                }

                target.pluginManager.apply(MavenPublishPlugin::class.java)
                target.extensions.configure(PublishingExtension::class.java) { publishing ->
                    publishing.publications { container ->
                        container.create("snapshot", MavenPublication::class.java) { publication ->
                            publication.groupId = packageGroupId
                            publication.artifactId = packageArtifactId
                            publication.version = "$packageAppVersion-SNAPSHOT"
                            publication.from(target.components.findByName("release"))
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
                target.tasks.register("publishSnapshotPublicationWithTokenPlugin") {
                    it.group = "tokeninc"
                    it.description = "Publishes the artifact to the specified Maven Repository"
                    it.dependsOn("publishSnapshotPublicationToMavenRepository")
                }
            }
        } else {
            logger.info("Excepting user input on the Java GUI")
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