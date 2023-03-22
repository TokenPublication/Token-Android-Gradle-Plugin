package com.tokeninc.tools.build.ui

import com.tokeninc.tools.build.store.PublisherCredentialStore
import com.tokeninc.tools.build.utils.CredentialUtil.isCredentialValid
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.event.WindowEvent
import javax.swing.*

class PublisherCredentialsPanel(private val credHelper: PublisherCredentialStore) :JFrame() {
    private val userNameLabel = JLabel("Username")
    private val urlLabel = JLabel("URL")
    private val pwLabel = JLabel("Password")
    private val urlField = JTextField()
    private val userNameField = JTextField()
    private val pwField = JPasswordField()
    private val submitButton  = JButton("Save")
    private val myPanel = JPanel(GridBagLayout())

    private fun generateUI(){
        title = "Enter credentials to publish"
        myPanel.setBounds(10,0,400,400)
        userNameLabel.setBounds(10,0,100,30)
        userNameField.setBounds(100,0,100,30)
        pwLabel.setBounds(10,40,100,30)
        pwField.setBounds(100,40,100,30)
        urlLabel.setBounds(10,80,100,30)
        urlField.setBounds(100,80,300,30)
        submitButton.setBounds(300,120,150,30)

        add(userNameField)
        add(userNameLabel)
        add(pwLabel)
        add(pwField)
        add(urlLabel)
        add(urlField)
        add(submitButton)
        add(myPanel)
        defaultCloseOperation  = JFrame.EXIT_ON_CLOSE
        preferredSize = Dimension(450,200)
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

    private fun validateFirstCredential() {
        if (userNameField.text.isNotEmpty() && pwField.password.isNotEmpty() && urlField.text.isNotEmpty()) {
            Thread {
                val result = isCredentialValid(userNameField.text, pwField.password, urlField.text)
                if (result == 200 || result == 403) {
                    credHelper.saveCredentials(userNameField.text,String(pwField.password),urlField.text)
                    dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
                } else {
                    submitButton.text = "server response: $result"
                }
            }.start()
        } else {
            submitButton.text = "Enter ALL fields"
        }
    }
}