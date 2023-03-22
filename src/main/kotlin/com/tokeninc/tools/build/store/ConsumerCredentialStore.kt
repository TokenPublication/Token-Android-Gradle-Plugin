package com.tokeninc.tools.build.store

import com.tokeninc.tools.build.utils.CredentialUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator


class ConsumerCredentialStore(private val secretKeyAlias: String,
                              private val folderPath: String,
                              private val pwdArray: CharArray,
                              private val keyStoreName: String,
                              private val credentialFilePath: String) {

    private var loadedCredentials: String? = null
    private val ks = KeyStore.getInstance(KeyStore.getDefaultType())
    init {
        checkIfFilesExists()
    }

    private fun createFolder() {
        if (File(folderPath).exists())
            return
        Files.createDirectories(Paths.get(folderPath))
    }

    private fun createCredentialFile() {
        val file = File(credentialFilePath)
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    private fun createKeyStore() {
        ks.load(null)
        ks.store(FileOutputStream(keyStoreName), pwdArray)
    }

    private fun createSecretKey() {
        ks.load( /* stream = */ FileInputStream(keyStoreName), /* password = */ pwdArray)
        val keyGen: KeyGenerator = KeyGenerator.getInstance("AES")
        keyGen.init(128)
        val key: Key = keyGen.generateKey()
        ks.setKeyEntry(secretKeyAlias, key, pwdArray, null)
        ks.store(FileOutputStream(keyStoreName), pwdArray)
    }

    private fun loadSecretKey(): Key {
        ks.load(FileInputStream(keyStoreName), pwdArray)
        return ks.getKey(secretKeyAlias, pwdArray)
    }

    fun getUserName1(): String {
        if (loadedCredentials.isNullOrEmpty())
            decipherCredentials()
        return loadedCredentials!!.lines()[0]
    }

    fun getPassword1(): String {
        if (loadedCredentials.isNullOrEmpty())
            decipherCredentials()
        return loadedCredentials!!.lines()[1]
    }

    fun getURL1(): String {
        if (loadedCredentials.isNullOrEmpty())
            decipherCredentials()
        return loadedCredentials!!.lines()[2]
    }

    fun getUserName2(): String {
        if (loadedCredentials.isNullOrEmpty())
            decipherCredentials()
        return loadedCredentials!!.lines()[3]
    }

    fun getPassword2(): String {
        if (loadedCredentials.isNullOrEmpty())
            decipherCredentials()
        return loadedCredentials!!.lines()[4]
    }

    fun getURL2(): String {
        if (loadedCredentials.isNullOrEmpty())
            decipherCredentials()
        return loadedCredentials!!.lines()[5]
    }

    fun saveCredentials(usrName: String, pwd: String, url: String, usrName2: String, pwd2: String, url2: String) {
        val credentials = buildString {
            append(usrName)
            append("\n")
            append(pwd)
            append("\n")
            append(url)
            append("\n")
            append(usrName2)
            append("\n")
            append(pwd2)
            append("\n")
            append(url2)
        }
        cipherCredentials(credentials)
    }

    private fun cipherCredentials(credentials: String) {
        val c = Cipher.getInstance("AES/ECB/PKCS5Padding")
        c.init(Cipher.ENCRYPT_MODE, loadSecretKey())
        val encryptedData = c.doFinal(credentials.toByteArray(Charsets.UTF_8))
        File(credentialFilePath).writeBytes(encryptedData)
    }

    private fun decipherCredentials() {
        val c = Cipher.getInstance("AES/ECB/PKCS5Padding")
        c.init(Cipher.DECRYPT_MODE, loadSecretKey())
        val encryptedData = File(credentialFilePath).readBytes()
        loadedCredentials = String(c.doFinal(encryptedData))
    }

    fun checkCredentials():Boolean{
        checkIfFilesExists()
        decipherCredentials()
        //validateCredentials()
        return loadedCredentials!!.lines().size == 6
    }

    private fun validateCredentials(): Boolean{
        with(CredentialUtil.isCredentialValid(getUserName1(), getPassword1().toCharArray(), getURL1())) {
            if(!(this == 200 || this == 403)) {
                return false
            }
        }
        with(CredentialUtil.isCredentialValid(getUserName2(), getPassword2().toCharArray(), getURL2())) {
            if(!(this == 200 || this == 403)) {
                return false
            }
        }
        return true
    }

    private fun checkIfFilesExists() {
        createFolder()
        createCredentialFile()
        if (!File(keyStoreName).exists()) {
            createKeyStore()
            createSecretKey()
        }
    }
}