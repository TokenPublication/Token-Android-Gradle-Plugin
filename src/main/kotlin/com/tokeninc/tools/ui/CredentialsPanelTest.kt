package com.tokeninc.tools.ui

import com.tokeninc.tools.store.CredentialStore
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch


class CredentialsPanelTest {

    @Test
    fun showCredentialPanel() {
        val userPath: String = System.getProperty("user.home")
        val secretKeyAlias = "ConsumeMavenCredentialKey"
        val folderPath = userPath + File.separator + ".gradle" + File.separator + "tokeninc" + File.separator + "consume"
        val pwdArray = folderPath.toCharArray()
        val keyStoreName = folderPath + File.separator + "ConsumeMavenHelperKeyStore.jks"
        val credentialFilePath = folderPath + File.separator + "consumerCredentials"
        val latch = CountDownLatch(1)
        val store = CredentialStore(secretKeyAlias,folderPath,keyStoreName,credentialFilePath)
        val credPanel = CredentialsPanel(store,"consume")
        credPanel.showCredentialPanel()
        //store.checkCredentials()
        latch.await()

    }
}