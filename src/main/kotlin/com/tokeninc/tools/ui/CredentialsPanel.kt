package com.tokeninc.tools.ui

import com.tokeninc.tools.store.CredentialStore
import com.tokeninc.tools.utils.CredentialUtil
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*


class CredentialsPanel(private val store: CredentialStore,private val mod:String) : JFrame()  {
    private val addButton  = JButton("Add")
    private val removeButton  = JButton("Remove")
    private val userNameLabel = JLabel("Username")
    private val urlLabel = JLabel("URL")
    private val pwLabel = JLabel("Password")
    private val urlField = JTextField()
    private val userNameField = JTextField()
    private val pwField = JPasswordField()
    private val panel = JPanel()
    private val urlPanel = JPanel()
    private val pwPanel = JPanel()
    private val usernamePanel = JPanel()
    private val buttonPanel = JPanel()
    private val model = DefaultListModel<String>()
    private val list = JList<String>(model)
    init {
        isResizable = false
        val credentialList = store.getCredentials()
        for(credential in credentialList){
            model.addElement(credential.getUserName())
        }
    }
    private fun generateUI(){
        title = "Enter $mod credentials"
        panel.layout = FlowLayout(FlowLayout.CENTER)
        layout = FlowLayout()

        val scroll = JScrollPane(list)
        scroll.preferredSize = Dimension(500,200)
        panel.preferredSize = Dimension(500,200)
        panel.add(scroll)
        urlPanel.preferredSize = Dimension(500,30)
        urlField.preferredSize = Dimension(350,30)
        urlLabel.preferredSize = Dimension(70,30)
        usernamePanel.preferredSize = Dimension(500,30)
        userNameField.preferredSize = Dimension(350,30)
        userNameLabel.preferredSize = Dimension(70,30)
        pwPanel.preferredSize = Dimension(500,30)
        pwField.preferredSize = Dimension(350,30)
        pwLabel.preferredSize = Dimension(70,30)
        urlPanel.add(urlLabel)
        urlPanel.add(urlField)
        usernamePanel.add(userNameLabel)
        usernamePanel.add(userNameField)
        pwPanel.add(pwLabel)
        pwPanel.add(pwField)
        buttonPanel.add(addButton)
        //buttonPanel.add(removeButton)//TODO:Implement remove functionality
        buttonPanel.preferredSize = Dimension(500,30)
        contentPane.add(panel)
        contentPane.add(urlPanel)
        contentPane.add(usernamePanel)
        contentPane.add(pwPanel)
        contentPane.add(buttonPanel)
        defaultCloseOperation  = EXIT_ON_CLOSE
        preferredSize = Dimension(550,400)
    }
    fun showCredentialPanel(){
        generateUI()
        setButtonListener()
        setLocationRelativeTo(null)
        isVisible = true
        toFront()
        pack()

    }
    private fun setButtonListener() {
        addButton.addActionListener {
            validateCredentials()

        }
    }
    private fun reloadCredentials(){
        val credentialList = store.getCredentials()
        model.clear()
        for(credential in credentialList){
            model.addElement(credential.getUserName())
        }
        revalidate()
        repaint()
    }

    private fun validateCredentials() {
        if (userNameField.text.isNotEmpty() && pwField.password.isNotEmpty() && urlField.text.isNotEmpty()) {
            Thread {
                val result = CredentialUtil.isCredentialValid(userNameField.text, pwField.password, urlField.text)
                if (result == 200 || result == 403) {
                    store.saveCredentials(userNameField.text,String(pwField.password),urlField.text)
                    reloadCredentials()
                } else {
                    addButton.text = "server response: $result"
                }
            }.start()
        } else {
            addButton.text = "Enter ALL fields"
        }
    }
}