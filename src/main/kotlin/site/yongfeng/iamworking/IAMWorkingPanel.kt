package site.yongfeng.iamworking

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.BoxLayout
import javax.swing.Box
import javax.swing.border.EmptyBorder
import javax.swing.JLabel

class IAMWorkingPanel(private val project: Project) : SimpleToolWindowPanel(true) {
    private val fileBrowser = FileBrowserSimulator(project)
    private val settings = IAMWorkingSettings.getInstance(project)
    
    private val startButton = JButton("Start")
    private val stopButton = JButton("Stop")
    
    init {
        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = EmptyBorder(10, 10, 10, 10)
            
            // Control panel
            val controlPanel = JPanel().apply {
                add(startButton)
                add(stopButton)
            }
            add(controlPanel)
            add(Box.createVerticalStrut(10))
            
            // Add hint label with specified text
            add(JLabel("Select the file type to open and save it.").apply {
                alignmentX = java.awt.Component.CENTER_ALIGNMENT
            })
            add(Box.createVerticalStrut(5))
            
            // File types panel
            add(createFileTypesPanel())
        }
        
        setContent(mainPanel)
        
        stopButton.isEnabled = false
        
        startButton.addActionListener {
            startBrowsing()
        }
        stopButton.addActionListener {
            stopBrowsing()
        }
    }
    
    private fun createFileTypesPanel(): JPanel {
        val extensions = listOf(
            "Java" to listOf("java", "kt", "kts", "groovy"),
            "Web" to listOf("js", "jsx", "ts", "tsx", "vue", "html", "css"),
            "Backend" to listOf("py", "php", "go", "rs", "cpp", "c"),
            "Config" to listOf("xml", "json", "yaml", "properties", "gradle"),
            "Data" to listOf("sql", "txt", "md", "log", "csv"),
            "Script" to listOf("sh", "bat", "cmd", "ps1")
        )
        
        val checkBoxes = mutableMapOf<String, JBCheckBox>()
        
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            
            extensions.forEach { (category, extList) ->
                add(JLabel("$category:"))
                
                val checkPanel = JPanel().apply {
                    layout = java.awt.GridLayout(0, 4, 5, 2)
                    extList.forEach { ext ->
                        val checkBox = JBCheckBox(".$ext", settings.supportedExtensions.contains(ext))
                        checkBoxes[ext] = checkBox
                        add(checkBox)
                    }
                }
                add(checkPanel)
                add(Box.createVerticalStrut(5))
            }
            
            val buttonPanel = JPanel().apply {
                add(JButton("Select All").apply {
                    addActionListener { checkBoxes.values.forEach { it.isSelected = true } }
                })
                add(JButton("Clear All").apply {
                    addActionListener { checkBoxes.values.forEach { it.isSelected = false } }
                })
                add(JButton("Default").apply {
                    addActionListener {
                        val defaultExts = setOf("java", "kt", "xml", "json", "properties", "md", "txt")
                        checkBoxes.forEach { (ext, checkBox) ->
                            checkBox.isSelected = ext in defaultExts
                        }
                    }
                })
                add(JButton("Save").apply {
                    addActionListener {
                        saveSettings(checkBoxes)
                        Messages.showInfoMessage(project, "File type settings saved", "IAMWorking")
                    }
                })
            }
            add(buttonPanel)
        }
    }
    
    private fun saveSettings(checkBoxes: Map<String, JBCheckBox>) {
        settings.supportedExtensions.clear()
        checkBoxes.forEach { (ext, checkBox) ->
            if (checkBox.isSelected) settings.supportedExtensions.add(ext)
        }
    }
    
    private fun startBrowsing() {
        fileBrowser.start()
        startButton.isEnabled = false
        stopButton.isEnabled = true
        Messages.showInfoMessage(project, "Started working!", "IAMWorking")
    }
    
    private fun stopBrowsing() {
        fileBrowser.stop()
        startButton.isEnabled = true
        stopButton.isEnabled = false
        Messages.showInfoMessage(project, "Stopped working!", "IAMWorking")
    }
}