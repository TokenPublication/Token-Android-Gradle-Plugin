/*
 *
 *  * Copyright (C) 2023 Token Financial Technologies
 *
 */

package com.tokeninc.tools.plugin.build

import com.tokeninc.tools.controller.ConsumerCredentialManager
import com.tokeninc.tools.utils.CredentialUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.api.internal.GradleInternal


class BuildPlugin : Plugin<Project> {

    private val Project.settings: Settings
        get() = (gradle as GradleInternal).settings

    override fun apply(target: Project) {

        target.tasks.register("modifyConsumerCredentials") {
            it.group = "tokeninc"
            it.description = "Shows the Java GUI to modify the consumer credentials"
            it.doLast {
                ConsumerCredentialManager.showPanel()
                println("modifying consumer credentials...")
            }
        }

        with(target.settings.dependencyResolutionManagement.repositoriesMode.get()) {
            if(this == RepositoriesMode.FAIL_ON_PROJECT_REPOS || this == RepositoriesMode.PREFER_SETTINGS) {
                return
            }
        }

        println("Adding Token Maven Repositories to all projects in build.gradle")

        if (hasMinimumCredentialArguments(target.properties)) {
            println("Detected valid credential arguments")

            var credentialIndex = 1
            while(hasCredentialArguments(target.properties, credentialIndex)) {
                val repoUrl = target.properties["consume-repo-url-$credentialIndex"].toString()
                val repoUsr = target.properties["consume-repo-usr-$credentialIndex"].toString()
                val repoPwd = target.properties["consume-repo-pwd-$credentialIndex"].toString()
                credentialIndex++

                target.allprojects { project ->
                    println("Configuring " + project.name)
                    project.repositories.maven { repo ->
                        repo.url = URI(repoUrl)
                        repo.credentials {
                            it.username = repoUsr
                            it.password = repoPwd
                        }
                    }
                }
                if (target.properties.getOrDefault("save", "").toString().isNotEmpty()) {
                ConsumerCredentialManager
                        .saveCredentialsFromArgument(repoUsr,
                                repoPwd,
                                repoUrl)
                }
            }
        } else if (ConsumerCredentialManager.checkCredentials()) {
            println("Found saved credentials")

            for(credential in ConsumerCredentialManager.getCredentials()){
                target.allprojects { project ->
                    project.beforeEvaluate {
                        println("Configuring " + project.name + " usr: " + credential.getUserName() + " url: " + credential.getUrl())
                        project.repositories.google()
                        project.repositories.maven { repo ->
                            repo.url = URI(credential.getUrl())
                            repo.credentials {
                                it.username = credential.getUserName()
                                it.password = credential.getPwd()
                            }
                        }
                    }
                }
            }
            target.allprojects{project ->
                project.configurations.all {
                    it.resolutionStrategy.cacheChangingModulesFor(0, "seconds")
                }
            }

        } else {
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