package piu.utils

import java.io.File

sealed interface TextDocument {
    val title: String
    val content: String
}

interface DocumentParser<T : TextDocument> {
    fun parse(file: File): T
}

data class PlainTextDoc(override val title: String, override val content: String) : TextDocument
data class MarkdownDoc(override val title: String, override val content: String, val hasFrontMatter: Boolean) :
    TextDocument

data class JsonDoc(override val title: String, override val content: String, val isValidJson: Boolean) : TextDocument

class DocumentHandler<T : TextDocument>(val document: T) {
    fun getContent(): String {
        return document.content
    }
}


class PlainTextParser : DocumentParser<PlainTextDoc> {
    override fun parse(file: File): PlainTextDoc {
        return PlainTextDoc(file.name, file.readText())
    }
}

class MarkdownParser : DocumentParser<MarkdownDoc> {
    override fun parse(file: File): MarkdownDoc {
        val content = file.readText()
        val hasFrontMatter = content.startsWith("---")
        return MarkdownDoc(file.name, content, hasFrontMatter)
    }
}

class JsonParser : DocumentParser<JsonDoc> {
    override fun parse(file: File): JsonDoc {
        val content = file.readText()
        val isValid = content.trim().startsWith("{") && content.trim().endsWith("}")
        return JsonDoc(file.name, content, isValid)
    }
}
