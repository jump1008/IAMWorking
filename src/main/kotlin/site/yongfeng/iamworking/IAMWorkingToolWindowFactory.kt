package site.yongfeng.iamworking

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JComponent

class IAMWorkingToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val iamWorkingPanel = IAMWorkingPanel(project)
        val content = ContentFactory.getInstance().createContent(iamWorkingPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}