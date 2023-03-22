package com.tokeninc.tools.build.controller

import com.tokeninc.tools.build.store.PublisherCredentialStore
import com.tokeninc.tools.build.ui.PublisherCredentialsPanel

object PublisherCredentialManager {

    private val userPath: String = System.getProperty("user.home")
    private const val secretKeyAlias = "PublishMavenCredentialKey"
    private val folderPath = "$userPath/.gradleHelper/publish"
    private val pwdArray = "".toCharArray()
    private val keyStoreName = "$folderPath/PublishMavenHelperKeyStore.jks"
    private val credentialFilePath = "$folderPath/publisherCredentials"

    private val store = PublisherCredentialStore(secretKeyAlias, folderPath, pwdArray, keyStoreName, credentialFilePath)
    private lateinit var credentialsPanel: PublisherCredentialsPanel

    init {
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            credentialsPanel = PublisherCredentialsPanel(store)
        } else {
            println("Cannot instantiate credentials panel, current graphics environment does not support it!")
        }
    }

    fun showPanel() {
        if (PublisherCredentialManager::credentialsPanel.isInitialized && !credentialsPanel.isVisible)
            credentialsPanel.showCredentialPanel()
    }

    fun saveCredentialsFromArgument(usrName: String, pwd: String, url: String) {
        store.saveCredentials(usrName, pwd, url)
    }

    fun getUserName(): String = store.getUserName()
    fun getPassword(): String = store.getPassword()
    fun getURL(): String = store.getURL()
    fun checkCredentials() = store.checkCredentials()
}