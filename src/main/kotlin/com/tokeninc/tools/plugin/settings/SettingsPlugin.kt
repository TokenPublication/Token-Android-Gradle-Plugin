/*
 *
 *  * Copyright (C) 2023 Token Financial Technologies
 *
 */

package com.tokeninc.tools.plugin.settings


import com.tokeninc.tools.controller.ConsumerCredentialManager
import com.tokeninc.tools.utils.CredentialUtil
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.slf4j.LoggerFactory
import java.net.URI

@Suppress("UnstableApiUsage")
class SettingsPlugin: Plugin<Settings> {
    override fun apply(target: Settings) {
        val logger = LoggerFactory.getLogger("token-logger")
        logger.info("Adding Token Maven Repositories in settings.gradle")

        if (hasMinimumCredentialArguments(target.startParameter.projectProperties)) {
            logger.info("Detected valid credential arguments")

            var credentialIndex = 1
            while(hasCredentialArguments(target.startParameter.projectProperties, credentialIndex)) {
                val repoUrl = target.startParameter.projectProperties["consume-repo-url-$credentialIndex"].toString()
                val repoUsr = target.startParameter.projectProperties["consume-repo-usr-$credentialIndex"].toString()
                val repoPwd = target.startParameter.projectProperties["consume-repo-pwd-$credentialIndex"].toString()
                credentialIndex++

                target.dependencyResolutionManagement { management ->
                    management.repositories { config ->
                        config.maven { repo ->
                            repo.url = URI(repoUrl)
                            repo.credentials {
                                it.username = repoUsr
                                it.password = repoPwd
                            }
                        }
                    }
                }
                if (target.startParameter.projectProperties.getOrDefault("save", "").toString().isNotEmpty()) {
                ConsumerCredentialManager
                        .saveCredentialsFromArgument(repoUsr,
                                repoPwd,
                                repoUrl)
                }
            }

        } else if (ConsumerCredentialManager.checkCredentials()){
            logger.info("Found saved credentials")
            for(credential in ConsumerCredentialManager.getCredentials()){
                target.dependencyResolutionManagement { management ->
                    management.repositories { config ->
                        config.maven { repo ->
                            repo.url = URI(credential.getUrl())
                            repo.credentials {
                                it.username = credential.getUserName()
                                it.password = credential.getPwd()
                            }
                        }
                    }
                }
            }

        } else {
            logger.info("Excepting user input on the Java GUI")
            ConsumerCredentialManager.showPanel()
        }
    }

    private fun hasMinimumCredentialArguments(projectProperties: Map<String, *>): Boolean {
        return CredentialUtil.isConsumeCredentialProvided(projectProperties, 1)
    }

    private fun hasCredentialArguments(projectProperties: Map<String, *>, i: Int): Boolean {
        return CredentialUtil.isConsumeCredentialProvided(projectProperties, i)
    }


}