package com.tokeninc.tools.build.plugin.consumer

import com.tokeninc.tools.build.controller.ConsumerCredentialManager
import com.tokeninc.tools.build.controller.PublisherCredentialManager
import com.tokeninc.tools.build.utils.CredentialUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI


class ConsumerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        println("Adding Token Maven Repositories to all projects in build.gradle")

        if (hasCredentialArguments(target.properties)) {
            println("Detected valid credential arguments")

            val snapshotRepoUrl = target.properties["snapshot-repo-url"].toString()
            val snapshotConsumerUsr = target.properties["snapshot-consumer-usr"].toString()
            val snapshotConsumerPwd = target.properties["snapshot-consumer-pwd"].toString()

            val releaseRepoUrl = target.properties["release-repo-url"].toString()
            val releaseConsumerUsr = target.properties["release-consumer-usr"].toString()
            val releaseConsumerPwd = target.properties["release-consumer-pwd"].toString()

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

            if (target.properties.getOrDefault("save", "").toString().isNotEmpty()) {
                ConsumerCredentialManager
                        .saveCredentialsFromArgument(snapshotConsumerUsr,
                                snapshotConsumerPwd,
                                snapshotRepoUrl,
                                releaseConsumerUsr,
                                releaseConsumerPwd,
                                releaseRepoUrl)
            }
            return
        } else if (ConsumerCredentialManager.checkCredentials()) {
            println("Found saved credentials")

            val snapshotRepoUrl = ConsumerCredentialManager.getURL1()
            val snapshotConsumerUsr = ConsumerCredentialManager.getUserName1()
            val snapshotConsumerPwd = ConsumerCredentialManager.getPassword1()

            val releaseRepoUrl = ConsumerCredentialManager.getURL2()
            val releaseConsumerUsr = ConsumerCredentialManager.getUserName2()
            val releaseConsumerPwd = ConsumerCredentialManager.getPassword2()

            target.allprojects { project ->
                project.beforeEvaluate {
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
                }
                project.configurations.all {
                    it.resolutionStrategy.cacheChangingModulesFor(0, "seconds")
                }
            }
        } else {
            ConsumerCredentialManager.showPanel()
        }
    }

    private fun hasCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isSnapshotConsumerProvided(projectProperties) &&
                CredentialUtil.isReleaseConsumerProvided(projectProperties)
    }

    private fun hasValidCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isSnapshotReachable(projectProperties) && CredentialUtil.isReleaseReachable(projectProperties)
    }


}