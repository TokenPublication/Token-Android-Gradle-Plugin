/*
 *
 *  * Copyright (C) 2023 Token Financial Technologies
 *
 */

package com.tokeninc.tools.controller

import com.tokeninc.tools.store.CredentialStore
import com.tokeninc.tools.ui.CredentialsPanel
import java.io.File

object ConsumerCredentialManager {

    private val userPath: String = System.getProperty("user.home")
    private const val secretKeyAlias = "ConsumeMavenCredentialKey"
    private val folderPath = userPath + File.separator + ".gradle" + File.separator + "tokeninc" + File.separator + "consume"
    private val pwdArray = folderPath.toCharArray()
    private val keyStoreName = folderPath + File.separator + "ConsumeMavenHelperKeyStore.jks"
    private val credentialFilePath = folderPath + File.separator + "consumerCredentials"

    private val store = CredentialStore(secretKeyAlias, folderPath, keyStoreName, credentialFilePath)
    private lateinit var credentialsPanel : CredentialsPanel

    fun showPanel() {
        if (!::credentialsPanel.isInitialized) {
            System.setProperty("java.awt.headless","false")
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                credentialsPanel = CredentialsPanel(store,"consumer")
            } else {
                println("Cannot instantiate credentials panel, current graphics environment does not support it!")
                return
            }
        }
        if(!credentialsPanel.isVisible)
            credentialsPanel.showCredentialPanel()
    }


    fun saveCredentialsFromArgument(usrName: String, pwd: String, url: String) {
        store.saveCredentials(usrName, pwd, url)
    }



    fun getCredentials():List<CredentialStore.Credential> = store.getCredentials()

    fun checkCredentials() = store.checkCredentials()
}