/*
 *
 *  * Copyright (C) 2023 Token Financial Technologies
 *
 */

package com.tokeninc.tools.utils

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