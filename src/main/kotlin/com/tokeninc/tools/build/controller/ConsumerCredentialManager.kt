package com.tokeninc.tools.build.controller

import com.tokeninc.tools.build.store.ConsumerCredentialStore
import com.tokeninc.tools.build.ui.ConsumerCredentialsPanel

object ConsumerCredentialManager {

    private val userPath: String = System.getProperty("user.home")
    private const val secretKeyAlias = "ConsumeMavenCredentialKey"
    private val folderPath = "$userPath/.gradleHelper/consume"
    private val pwdArray = "".toCharArray()
    private val keyStoreName = "$folderPath/ConsumeMavenHelperKeyStore.jks"
    private val credentialFilePath = "$folderPath/consumerCredentials"

    private val store = ConsumerCredentialStore(secretKeyAlias, folderPath, pwdArray, keyStoreName, credentialFilePath)
    private lateinit var credentialsPanel : ConsumerCredentialsPanel

    init {
        System.setProperty("java.awt.headless","false")
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            credentialsPanel = ConsumerCredentialsPanel(store)
        } else {
            println("Cannot instantiate credentials panel, current graphics environment does not support it!")
        }
    }

    fun showPanel() {
        if (ConsumerCredentialManager::credentialsPanel.isInitialized && !credentialsPanel.isVisible)
            credentialsPanel.showCredentialPanel()
    }

    fun saveCredentialsFromArgument(usrName: String, pwd: String, url: String, usrName2: String, pwd2: String, url2: String) {
        store.saveCredentials(usrName, pwd, url, usrName2, pwd2, url2)
    }

    fun getUserName1(): String = store.getUserName1()
    fun getPassword1(): String = store.getPassword1()
    fun getURL1(): String = store.getURL1()

    fun getUserName2(): String = store.getUserName2()
    fun getPassword2(): String = store.getPassword2()
    fun getURL2(): String = store.getURL2()
    fun checkCredentials() = store.checkCredentials()
}