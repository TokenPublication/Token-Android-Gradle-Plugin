package com.tokeninc.tools.build.ui

import com.tokeninc.tools.build.store.ConsumerCredentialStore
import com.tokeninc.tools.build.utils.CredentialUtil.isCredentialValid
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.event.WindowEvent
import javax.swing.*

class ConsumerCredentialsPanel(private val store: ConsumerCredentialStore) :JFrame() {
    private val userNameLabel = JLabel("Username")
    private val urlLabel = JLabel("URL")
    private val pwLabel = JLabel("Password")
    private val userName2Label = JLabel("Username2")
    private val url2Label = JLabel("URL2")
    private val pw2Label = JLabel("Password2")
    private val urlField = JTextField()
    private val userNameField = JTextField()
    private val pwField = JPasswordField()
    private val url2Field = JTextField()
    private val userName2Field = JTextField()
    private val pw2Field = JPasswordField()
    private val submitButton  = JButton("Save")
    private val myPanel = JPanel(GridBagLayout())

    private fun generateUI(){
        title = "Enter credentials to consume from"
        myPanel.setBounds(10,0,400,400)
        userNameLabel.setBounds(10,0,100,30)
        userNameField.setBounds(100,0,100,30)
        pwLabel.setBounds(10,40,100,30)
        pwField.setBounds(100,40,100,30)
        urlLabel.setBounds(10,80,100,30)
        urlField.setBounds(100,80,300,30)

        userName2Label.setBounds(10,120,100,30)
        userName2Field.setBounds(100,120,100,30)
        pw2Label.setBounds(10,160,100,30)
        pw2Field.setBounds(100,160,100,30)
        url2Label.setBounds(10, 200,100,30)
        url2Field.setBounds(100,200,300,30)
        submitButton.setBounds(250,240,200,30)

        add(userNameField)
        add(userNameLabel)
        add(pwLabel)
        add(pwField)
        add(urlLabel)
        add(urlField)

        add(userName2Field)
        add(userName2Label)
        add(pw2Label)
        add(pw2Field)
        add(url2Label)
        add(url2Field)

        add(submitButton)
        add(myPanel)
        defaultCloseOperation  = JFrame.EXIT_ON_CLOSE
        preferredSize = Dimension(450,300)
    }
    fun showCredentialPanel(){
        generateUI()
        setButtonListener()
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        toFront()

    }
    private fun setButtonListener() {
        submitButton.addActionListener {
            validateFirstCredential()
        }
    }

    private fun validateSecondCredential() {
        if (userName2Field.text.isNotEmpty() && pw2Field.password.isNotEmpty() && url2Field.text.isNotEmpty()) {
            val result = isCredentialValid(userName2Field.text, pw2Field.password, url2Field.text)
            if (result == 200 || result == 403) {
                store.saveCredentials(userNameField.text,String(pwField.password),urlField.text, userName2Field.text,String(pw2Field.password),url2Field.text)
                dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
            } else {
                submitButton.text = "server response for 2: $result"
            }
        } else {
            submitButton.text = "Enter ALL fields"
        }
    }

    private fun validateFirstCredential() {
        if (userNameField.text.isNotEmpty() && pwField.password.isNotEmpty() && urlField.text.isNotEmpty()) {
            Thread {
                val result = isCredentialValid(userNameField.text, pwField.password, urlField.text)
                if (result == 200 || result == 403) {
                    validateSecondCredential()
                } else {
                    submitButton.text = "server response for 1: $result"
                }
            }.start()
        } else {
            submitButton.text = "Enter ALL fields"
        }
    }
}