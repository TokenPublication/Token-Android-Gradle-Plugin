package com.tokeninc.tools.store

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class CredentialStore (private val secretKeyAlias: String,
                       private val folderPath: String,
                       private val keyStoreName: String,
                       private val credentialFilePath: String) {

    private val pwdArray = "".toCharArray()
    private var loadedCredentials: String? = null
    private val ks = KeyStore.getInstance(KeyStore.getDefaultType())
    private var credentialsList:MutableList<Credential> = mutableListOf()

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

    private fun loadCredentials(){
        credentialsList.clear()
        if(loadedCredentials.isNullOrEmpty())
            return
        for(index in 0 until loadedCredentials!!.lines().size-1 step 3){
            with(loadedCredentials!!.lines()){
                credentialsList.add(Credential(this[index],this[index+1],this[index+2]))
            }
        }
    }

    fun saveCredentials(usrName: String, pwd: String, url: String) {
        decipherCredentials()
        loadCredentials()
        addCredential(Credential(usrName,pwd,url))
        cipherCredentials()
    }

    private fun cipherCredentials() {
        val c = Cipher.getInstance("AES/ECB/PKCS5Padding")
        c.init(Cipher.ENCRYPT_MODE, loadSecretKey())
        val credentialString = StringBuilder()
        for(credential in credentialsList){
            credentialString.append(credential.getUserName(),"\n",credential.getPwd(),"\n",credential.getUrl(),"\n")
        }
        val encryptedData = c.doFinal(credentialString.toString().toByteArray(Charsets.UTF_8))
        File(credentialFilePath).writeBytes(encryptedData)
        return
    }

    private fun decipherCredentials() {
        val c = Cipher.getInstance("AES/ECB/PKCS5Padding")
        c.init(Cipher.DECRYPT_MODE, loadSecretKey())
        val encryptedData = File(credentialFilePath).readBytes()
        loadedCredentials = String(c.doFinal(encryptedData))
    }

    private fun addCredential(newCredential:Credential){
        for(credential in credentialsList){
            if(credential == newCredential)
                return
        }
        credentialsList.add(newCredential)
    }

    fun checkCredentials(): Boolean {
        checkIfFilesExists()
        decipherCredentials()
        return !loadedCredentials.isNullOrEmpty()
    }

    fun getCredentials():List<Credential>{
        decipherCredentials()
        loadCredentials()
        return credentialsList
    }

    private fun checkIfFilesExists() {
        createFolder()
        createCredentialFile()
        if (!File(keyStoreName).exists()) {
            createKeyStore()
            createSecretKey()
        }
    }

    data class Credential(private val userName: String,private val pwd:String, private val url:String){
        fun getUserName() = userName
        fun getPwd() = pwd
        fun getUrl() = url

    }
}