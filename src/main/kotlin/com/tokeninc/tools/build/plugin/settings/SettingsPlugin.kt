package com.tokeninc.tools.build.plugin.settings


import com.tokeninc.tools.build.controller.ConsumerCredentialManager
import com.tokeninc.tools.build.utils.CredentialUtil
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.net.URI

class SettingsPlugin: Plugin<Settings> {
    override fun apply(target: Settings) {
        println("Adding Token Maven Repositories in settings.gradle")

        if (hasCredentialArguments(target.startParameter.projectProperties)) {
            println("Detected valid credential arguments")

            val snapshotRepoUrl = target.startParameter.projectProperties["snapshot-repo-url"].toString()
            val snapshotConsumerUsr = target.startParameter.projectProperties["snapshot-consumer-usr"].toString()
            val snapshotConsumerPwd = target.startParameter.projectProperties["snapshot-consumer-pwd"].toString()

            val releaseRepoUrl = target.startParameter.projectProperties["release-repo-url"].toString()
            val releaseConsumerUsr = target.startParameter.projectProperties["release-consumer-usr"].toString()
            val releaseConsumerPwd = target.startParameter.projectProperties["release-consumer-pwd"].toString()

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

            if (target.startParameter.projectProperties.getOrDefault("save", "").toString().isNotEmpty()) {
                ConsumerCredentialManager
                        .saveCredentialsFromArgument(snapshotConsumerUsr,
                                snapshotConsumerPwd,
                                snapshotRepoUrl,
                                releaseConsumerUsr,
                                releaseConsumerPwd,
                                releaseRepoUrl)
            }

        } else if (ConsumerCredentialManager.checkCredentials()){
            println("Found saved credentials")

            val snapshotRepoUrl = ConsumerCredentialManager.getURL1()
            val snapshotConsumerUsr = ConsumerCredentialManager.getUserName1()
            val snapshotConsumerPwd = ConsumerCredentialManager.getPassword1()

            val releaseRepoUrl = ConsumerCredentialManager.getURL2()
            val releaseConsumerUsr = ConsumerCredentialManager.getUserName2()
            val releaseConsumerPwd = ConsumerCredentialManager.getPassword2()

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
        } else {
            ConsumerCredentialManager.showPanel()
        }
    }

    private fun hasCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isSnapshotConsumerProvided(projectProperties) &&
                CredentialUtil.isReleaseConsumerProvided(projectProperties)
    }

    /* Unused for now */
    private fun hasValidCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isSnapshotReachable(projectProperties) && CredentialUtil.isReleaseReachable(projectProperties)
    }


}