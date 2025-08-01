package site.yongfeng.iamworking

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class FileBrowserSimulator(private val project: Project) {
    private var fileScheduler: ScheduledExecutorService? = null
    private var scrollScheduler: ScheduledExecutorService? = null
    private val random = Random()
    private var isRunning = false
    private var currentEditor: Editor? = null
    private val supportedExtensions = mutableSetOf("java", "kt", "xml", "json", "properties", "md", "txt")
    
    fun start() {
        if (isRunning) return
        
        isRunning = true
        fileScheduler = Executors.newScheduledThreadPool(1)
        scrollScheduler = Executors.newScheduledThreadPool(1)
        
        // 主调度：随机打开文件
        fileScheduler?.scheduleWithFixedDelay({
            if (isRunning) {
                ApplicationManager.getApplication().invokeLater {
                    openRandomFile()
                }
            }
        }, 0, getRandomFileInterval(), TimeUnit.MILLISECONDS)
        
        // 滚动调度：模拟浏览行为
        scrollScheduler?.scheduleWithFixedDelay({
            if (isRunning && currentEditor != null) {
                ApplicationManager.getApplication().invokeLater {
                    simulateScrolling()
                }
            }
        }, 0, getRandomScrollInterval(), TimeUnit.MILLISECONDS)
    }
    
    fun stop() {
        isRunning = false
        fileScheduler?.shutdown()
        scrollScheduler?.shutdown()
        fileScheduler = null
        scrollScheduler = null
        currentEditor = null
    }
    
    fun setSupportedExtensions(extensions: Set<String>) {
        supportedExtensions.clear()
        supportedExtensions.addAll(extensions)
    }
    
    private fun getRandomFileInterval(): Long {
        return (3000 + random.nextInt(12000)).toLong()
    }
    
    private fun getRandomScrollInterval(): Long {
        return (1500 + random.nextInt(3500)).toLong()
    }
    
    private fun openRandomFile() {
        val baseDir = project.baseDir ?: return
        val files = findFiles(baseDir)
        
        if (files.isEmpty()) return
        
        val randomFile = files[random.nextInt(files.size)]
        openFileInEditor(randomFile)
    }
    
    private fun findFiles(dir: VirtualFile): List<VirtualFile> {
        val files = mutableListOf<VirtualFile>()
        
        dir.children?.forEach { child ->
            if (child.isDirectory && !child.name.startsWith(".")) {
                files.addAll(findFiles(child))
            } else if (supportedExtensions.contains(child.extension?.lowercase())) {
                files.add(child)
            }
        }
        
        return files
    }
    
    private fun openFileInEditor(file: VirtualFile) {
        ApplicationManager.getApplication().invokeLater {
            val fileEditorManager = FileEditorManager.getInstance(project)
            fileEditorManager.openFile(file, true)
            
            val editor = fileEditorManager.selectedTextEditor
            editor?.let {
                currentEditor = it
                // 初始定位到文件开头或随机位置
                val document = it.document
                val lineCount = document.lineCount
                if (lineCount > 0) {
                    val initialLine = if (lineCount > 10) random.nextInt(5) else 0
                    scrollToLine(it, initialLine)
                }
            }
        }
    }
    
    private fun simulateScrolling() {
        val editor = currentEditor ?: return
        val document = editor.document
        val lineCount = document.lineCount
        
        if (lineCount <= 1) return
        
        val visibleArea = editor.scrollingModel.visibleArea
        val currentOffset = editor.caretModel.offset
        val currentLine = document.getLineNumber(currentOffset)
        
        // 智能滚动策略：根据文件大小决定滚动行为
        when {
            lineCount <= 10 -> {
                // 小文件：逐行浏览
                val nextLine = (currentLine + 1).coerceAtMost(lineCount - 1)
                scrollToLine(editor, nextLine)
            }
            lineCount <= 30 -> {
                // 中等文件：跳跃式浏览
                val jumpSize = random.nextInt(3) + 1
                val direction = if (random.nextBoolean()) 1 else -1
                val nextLine = (currentLine + direction * jumpSize).coerceIn(0, lineCount - 1)
                scrollToLine(editor, nextLine)
            }
            else -> {
                // 大文件：大范围跳跃 + 区域浏览
                val strategy = random.nextInt(4)
                when (strategy) {
                    0 -> {
                        // 向下浏览几行
                        val nextLine = (currentLine + random.nextInt(5) + 1).coerceAtMost(lineCount - 1)
                        scrollToLine(editor, nextLine)
                    }
                    1 -> {
                        // 向上浏览几行
                        val nextLine = (currentLine - random.nextInt(5) - 1).coerceAtLeast(0)
                        scrollToLine(editor, nextLine)
                    }
                    2 -> {
                        // 跳转到新区域
                        val regionSize = lineCount / 4
                        val region = random.nextInt(4)
                        val startLine = region * regionSize
                        val endLine = (region + 1) * regionSize - 1
                        val nextLine = startLine + random.nextInt(endLine - startLine + 1)
                        scrollToLine(editor, nextLine.coerceIn(0, lineCount - 1))
                    }
                    else -> {
                        // 随机位置
                        val nextLine = random.nextInt(lineCount)
                        scrollToLine(editor, nextLine)
                    }
                }
            }
        }
    }
    
    private fun scrollToLine(editor: Editor, lineNumber: Int) {
        val document = editor.document
        if (lineNumber < 0 || lineNumber >= document.lineCount) return
        
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineLength = lineEnd - lineStart
        
        // 随机定位到行内的某个位置
        val randomColumn = if (lineLength > 0) random.nextInt(lineLength.coerceAtMost(80)) else 0
        val targetOffset = lineStart + randomColumn
        
        editor.caretModel.moveToOffset(targetOffset)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        
        // 添加一些延迟效果，让滚动看起来更自然
        Thread.sleep(100L + random.nextInt(200))
    }
}