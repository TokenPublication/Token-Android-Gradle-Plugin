package com.tokeninc.tools.build.utils

import java.io.IOException
import java.net.*

object CredentialUtil {


    public fun isCredentialValid(usr: String, pwd: CharArray, urlPath: String): Int {
        try {
            val url = URL(urlPath)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Connection", "close")
            connection.setAuthenticator(object: Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(usr, pwd)
                }
            })
            connection.connectTimeout = 3000
            connection.connect()
            println("ping result: ${connection.responseCode}")
            return connection.responseCode
        } catch (e: Exception) {
            return when (e) {
                is MalformedURLException -> {
                    println("Invalid URL ${e.message}")
                    1
                }
                is IOException -> {
                    println("IO Exception reading data ${e.message}")
                    2
                }
                is SecurityException -> {
                    println("Security Exception, Missing permissions ${e.message}")
                    e.printStackTrace()
                    3
                }
                else -> {
                    println("Unknown Error")
                    4
                }
            }
        }
    }

    /*Unused*/
    fun isSnapshotReachable(properties: Map<String, *>): Boolean {
        if (isSnapshotConsumerProvided(properties)) {
            val returnValue = isCredentialValid(properties["snapshot-repo-url"].toString(),
                    properties["snapshot-consumer-usr"].toString().toCharArray(), properties["snapshot-consumer-pwd"].toString())
            println("snapshot consume ping result: $returnValue")
            return returnValue == 200 || returnValue == 403
        }
        return false
    }

    fun isSnapshotConsumerProvided(properties: Map<String, *>): Boolean {
        return properties.getOrDefault("snapshot-repo-url", "").toString().run {
            this.isNotEmpty()
        } &&
                properties.getOrDefault("snapshot-consumer-usr", "").toString().isNotEmpty() &&
                properties.getOrDefault("snapshot-consumer-pwd", "").toString().isNotEmpty()
    }

    /*Unused*/
    fun isReleaseReachable(properties: Map<String, *>): Boolean {
        if (isReleaseConsumerProvided(properties)) {
            val returnValue = isCredentialValid(properties["release-repo-url"].toString(),
                    properties["release-consumer-usr"].toString().toCharArray(), properties["release-consumer-pwd"].toString())
            println("release consume ping result: $returnValue")
            return returnValue == 200 || returnValue == 403
        }
        return false
    }

    fun isReleaseConsumerProvided(properties: Map<String, *>): Boolean {
        return properties.getOrDefault("snapshot-repo-url", "").toString().run {
            this.isNotEmpty()
        } &&
                properties.getOrDefault("snapshot-consumer-usr", "").toString().isNotEmpty() &&
                properties.getOrDefault("snapshot-consumer-pwd", "").toString().isNotEmpty()
    }

    /*Unused*/
    fun isSnapshotPublishable(properties: Map<String, *>): Boolean {
        if (isSnapshotPublisherProvided(properties)) {
            val returnValue = isCredentialValid(properties["snapshot-repo-url"].toString(),
                    properties["snapshot-publisher-usr"].toString().toCharArray(), properties["snapshot-publisher-pwd"].toString())
            println("snapshot publish ping result: $returnValue")
            return returnValue == 200 || returnValue == 403
        }
        return false
    }

    fun isSnapshotPublisherProvided(properties: Map<String, *>): Boolean {
        return properties.getOrDefault("snapshot-repo-url", "").toString().run {
            this.isNotEmpty()
        } &&
                properties.getOrDefault("snapshot-publisher-usr", "").toString().isNotEmpty() &&
                properties.getOrDefault("snapshot-publisher-pwd", "").toString().isNotEmpty()
    }
}