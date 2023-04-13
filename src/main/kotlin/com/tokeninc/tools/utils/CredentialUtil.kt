/*
 *
 *  * Copyright (C) 2023 Token Financial Technologies
 *
 */

package com.tokeninc.tools.utils

import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.*

object CredentialUtil {


    fun isCredentialValid(usr: String, pwd: CharArray, urlPath: String): Int {
        val logger = LoggerFactory.getLogger("token-logger")
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
            logger.info("ping result: ${connection.responseCode}")
            return connection.responseCode
        } catch (e: Exception) {
            return when (e) {
                is MalformedURLException -> {
                    logger.info("Invalid URL ${e.message}")
                    1
                }
                is IOException -> {
                    logger.info("IO Exception reading data ${e.message}")
                    2
                }
                is SecurityException -> {
                    logger.info("Security Exception, Missing permissions ${e.message}")
                    e.printStackTrace()
                    3
                }
                else -> {
                    logger.info("Unknown Error")
                    4
                }
            }
        }
    }

    fun isConsumeCredentialProvided(properties: Map<String, *>, i: Int): Boolean {
        return properties.getOrDefault("consume-repo-url-$i", "").toString().run {
            this.isNotEmpty()
        } &&
                properties.getOrDefault("consume-repo-usr-$i", "").toString().isNotEmpty() &&
                properties.getOrDefault("consume-repo-pwd-$i", "").toString().isNotEmpty()
    }

    fun isPublishCredentialProvided(properties: Map<String, *>, i: Int): Boolean {
        return properties.getOrDefault("publish-repo-url-$i", "").toString().run {
            this.isNotEmpty()
        } &&
                properties.getOrDefault("publish-repo-usr-$i", "").toString().isNotEmpty() &&
                properties.getOrDefault("publish-repo-pwd-$i", "").toString().isNotEmpty()
    }
}