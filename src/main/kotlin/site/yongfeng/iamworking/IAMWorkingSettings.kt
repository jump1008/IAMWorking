package site.yongfeng.iamworking

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "IAMWorkingSettings",
    storages = [Storage("iam_working.xml")]
)
class IAMWorkingSettings : PersistentStateComponent<IAMWorkingSettings> {
    var supportedExtensions: MutableSet<String> = mutableSetOf(
        // Java生态
        "java", "kt", "kts", "groovy", "scala", "clj", "cljs", 
        
        // Web前端
        "js", "jsx", "ts", "tsx", "vue", "html", "htm", "css", "scss", "sass", "less",
        
        // 后端开发
        "py", "php", "rb", "go", "rs", "swift", "dart", "cs", "cpp", "c", "h", "hpp",
        
        // 配置文件
        "xml", "json", "yaml", "yml", "toml", "ini", "cfg", "conf", "env", 
        "properties", "gradle", "kts", "cmake", "makefile", 
        
        // 数据格式
        "sql", "csv", "tsv", "log", "txt", "md", "rst", 
        
        // 脚本文件
        "sh", "bash", "zsh", "fish", "ps1", "bat", "cmd",
        
        // 容器和部署
        "dockerfile", "dockerignore", "gitignore", "gitattributes", 
        
        // 文档和标记
        "md", "markdown", "adoc", "asciidoc", "tex", "rst",
        
        // 其他常见类型
        "svg", "json5", "proto", "thrift", "avro", "graphql"
    )
    
    override fun getState(): IAMWorkingSettings {
        return this
    }

    override fun loadState(state: IAMWorkingSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    companion object {
        fun getInstance(project: Project): IAMWorkingSettings {
            return project.getService(IAMWorkingSettings::class.java)
        }
    }
}