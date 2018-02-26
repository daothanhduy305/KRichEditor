package com.ebolo.krichtexteditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import com.bitbucket.eventbus.EventBus
import com.ebolo.krichtexteditor.ui.widgets.EditorButton
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.BOLD
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.CODE_VIEW
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H1
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H2
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H3
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H4
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H5
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H6
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.ITALIC
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_CENTER
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_FULL
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_LEFT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_RIGHT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.LINK
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.NORMAL
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.ORDERED
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.SIZE
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.STRIKETHROUGH
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.SUBSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.SUPERSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.UNDERLINE
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.UNORDERED
import com.ebolo.krichtexteditor.utils.QuillFormat
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream

/**
 * Rich Editor = Rich Editor Action + Rich Editor Callback
 * Created by even.wu on 8/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class RichEditor {
    private val gson by lazy { GsonBuilder().setPrettyPrinting().create() }
    private var currentFormat = QuillFormat()
    var html: String? = null

    private val mFontBlockGroup by lazy { listOf(NORMAL, H1, H2, H3, H4, H5, H6) }
    private val mTextAlignGroup by lazy {
        mapOf(
                JUSTIFY_LEFT to "",
                JUSTIFY_CENTER to "center",
                JUSTIFY_RIGHT to "right",
                JUSTIFY_FULL to "justify"
        )
    }
    private val mListStyleGroup by lazy{ mapOf(
            ORDERED to "ordered",
            UNORDERED to "bullet"
    ) }

    lateinit var placeHolder: String
        @JavascriptInterface get

    lateinit var mWebView: WebView
    var onInitialized: (() -> Unit)? = null
    var styleUpdatedCallback: ((type: Int, value: Any) -> Unit)? = null

    @JavascriptInterface
    fun returnHtml(html: String) { this.html = html }

    @JavascriptInterface
    fun updateCurrentStyle(currentStyle: String) = try {
        Log.d("FontStyle", currentStyle)
        updateStyle(gson.fromJson(currentStyle))
    } catch (e: Exception) {} // ignored

    @JavascriptInterface
    fun debugJs(message: String) = Log.d("JS", message)

    @JavascriptInterface
    fun onInitialized() = onInitialized?.invoke()

    fun updateStyle() = getStyle( ValueCallback {
        try { updateStyle(gson.fromJson(it)) } catch (e: Exception) {} // ignored
    } )

    private fun updateStyle(quillFormat: QuillFormat) {
        // Log.d("FontStyle", gson.toJson(fontStyle))

        if (currentFormat.isBold != quillFormat.isBold) {
            notifyFontStyleChange(BOLD, quillFormat.isBold ?: false)
        }

        if (currentFormat.isItalic != quillFormat.isItalic) {
            notifyFontStyleChange(ITALIC, quillFormat.isItalic ?: false)
        }

        if (currentFormat.isUnderline != quillFormat.isUnderline) {
            notifyFontStyleChange(UNDERLINE, quillFormat.isUnderline ?: false)
        }

        if (currentFormat.isStrike != quillFormat.isStrike) {
            notifyFontStyleChange(STRIKETHROUGH, quillFormat.isStrike?: false)
        }

        if (currentFormat.isCode != quillFormat.isCode) {
            notifyFontStyleChange(CODE_VIEW, quillFormat.isCode ?: false)
        }

        quillFormat.header = quillFormat.header ?: 0

        if (currentFormat.header != quillFormat.header) {
            mFontBlockGroup.indices.forEach {
                notifyFontStyleChange(mFontBlockGroup[it], (quillFormat.header == it))
            }
        }

        if (currentFormat.script != quillFormat.script) {
            notifyFontStyleChange(SUBSCRIPT, (quillFormat.script == "sub"))
            notifyFontStyleChange(SUPERSCRIPT, (quillFormat.script == "super"))
        }

        quillFormat.align = quillFormat.align ?: ""

        if (currentFormat.align != quillFormat.align) {
            mTextAlignGroup.forEach { notifyFontStyleChange(it.key, (quillFormat.align == it.value)) }
        }

        if (currentFormat.list != quillFormat.list) {
            mListStyleGroup.forEach {
                notifyFontStyleChange(it.key, (quillFormat.list == it.value))
            }
        }

        notifyFontStyleChange(SIZE, quillFormat.size)

        if (currentFormat.link != quillFormat.link)
            notifyFontStyleChange(LINK, !quillFormat.link.isNullOrBlank())

        currentFormat = quillFormat
    }

    private fun notifyFontStyleChange(@EditorButton.Companion.ActionType type: Int, value: Any) {
        when (styleUpdatedCallback) {
            null -> EventBus.getInstance().post("style_$type", value)
            else -> styleUpdatedCallback!!.invoke(type, value)
        }
    }

    // Start of Js wrapper
    private fun undo() = load("javascript:undo()")
    private fun redo() = load("javascript:redo()")
    fun focus() = load("javascript:focus()")
    fun disable() = load("javascript:disable()")
    fun enable() = load("javascript:enable()")

    // Font
    private fun bold() = load("javascript:bold()")
    private fun italic() = load("javascript:italic()")
    private fun underline() = load("javascript:underline()")
    private fun strikethrough() = load("javascript:strikethrough()")
    private fun script(style: String) = load("javascript:script('$style')")
    private fun backColor(color: String) = load("javascript:background('$color')")
    private fun foreColor(color: String) = load("javascript:color('$color')")
    private fun fontSize(size: String) = load("javascript:fontSize('$size')")

    // Paragraph
    private fun align(style: String) = load("javascript:align('$style')")
    private fun insertOrderedList() = load("javascript:insertOrderedList()")
    private fun insertUnorderedList() = load("javascript:insertUnorderedList()")
    private fun insertCheckList() = load("javascript:insertCheckList()")
    private fun indent() = load("javascript:indent()")
    private fun outdent() = load("javascript:outdent()")
    private fun header(level: Int) = load("javascript:header($level)")
    private fun insertImage(index: Int, url: String) = load("javascript:insertEmbed($index, 'image', '$url')")
    private fun insertImageB64(index: Int, path: String) = doAsync {
        val type = path.split('.').last().toUpperCase()
        val bitmap = BitmapFactory.decodeFile(path)
        val stream = ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, this)
        }
        val encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        uiThread {
            load("javascript:insertEmbed($index, 'image', 'data:image/${type.toLowerCase()};base64, $encodedImage')")
        }
    }
    private fun createLink(linkUrl: String) = load("javascript:createLink('$linkUrl')")
    private fun codeView() = load("javascript:codeView()")
    // fun insertTable(colCount: Int, rowCount: Int) = load("javascript:insertTable('${colCount}x$rowCount')")
    private fun insertHorizontalRule() = load("javascript:insertHorizontalRule()")
    private fun formatBlockquote() = load("javascript:formatBlock('blockquote')")
    private fun formatBlockCode() = load("javascript:formatBlock('pre')")
    fun getSelection(callback: ValueCallback<String>? = null) = load("javascript:getSelection()", callback)
    private fun getStyle(callback: ValueCallback<String>? = null) = load("javascript:getStyle()", callback)

    private fun getHtml(callBack: ValueCallback<String>) = load("javascript:getHtml()", callBack)
    fun getHtml(callback: ((html: String) -> Unit)? = null) = getHtml( ValueCallback { html ->
        val escapedData = html
                .replace(oldValue = "\\u003C", newValue = "<")
                .replace(oldValue = "\\\"", newValue = "\"")
        callback?.invoke(escapedData.substring(startIndex = 1, endIndex = escapedData.length - 1))
    } )
    // This is only used in Java interface
    interface OnHtmlReturned { fun process(html: String) }
    fun getHtml(callback: OnHtmlReturned) = getHtml( { callback.process(it) } )

    private fun getText(callback: ValueCallback<String>) = load("javascript:getText()", callback)
    fun getText(callback: ((text: String) -> Unit)?) = getText( ValueCallback {
        callback?.invoke(it.substring(1, it.length - 1).replace("\\n", "\n"))
    } )
    // This is only used in Java interface
    interface OnTextReturned { fun process(text: String) }
    fun getText(callback: OnTextReturned) = getText( { callback.process(it) } )

    /**
     * Function:    getContents
     * Description: Retrieves contents of the editor, with formatting data,
     *              represented by a Delta object (https://quilljs.com/docs/delta/).
     */
    private fun getContents(callback: ValueCallback<String>) = load("javascript:getContents()", callback)
    fun getContents(callback: ((text: String) -> Unit)?) = getContents( ValueCallback { callback?.invoke(it) } )
    // This is only used in Java interface
    interface OnContentsReturned { fun process(contents: String) }
    fun getContents(callback: OnContentsReturned) = getContents( { callback.process(it) } )

    fun setContents(data: String) = load("javascript:setContents($data)")

    private fun load(trigger: String, callBack: ValueCallback<String>? = null) = mWebView.context.runOnUiThread {
        // Make sure every calls would be run on ui thread
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(trigger, callBack)
        } else {
            mWebView.loadUrl(trigger)
        }
    }

    /**
     * Function:    command
     * Description: A bridge between Jvm api and JS api
     * @param   mActionType type of calling action
     * @param   reFocus     we disable the editor as a workaround when menu is shown,
     *                      by setting this to true would make the editor have the focus again
     */
    fun command(@EditorButton.Companion.ActionType mActionType: Int, vararg options: Any) {
        when (mActionType) {
            EditorButton.UNDO -> undo()
            EditorButton.REDO -> redo()
            EditorButton.BOLD -> bold()
            EditorButton.ITALIC -> italic()
            EditorButton.UNDERLINE -> underline()
            EditorButton.SUBSCRIPT -> script("sub")
            EditorButton.SUPERSCRIPT -> script("super")
            EditorButton.STRIKETHROUGH -> strikethrough()
            EditorButton.NORMAL -> header(0)
            EditorButton.H1 -> header(1)
            EditorButton.H2 -> header(2)
            EditorButton.H3 -> header(3)
            EditorButton.H4 -> header(4)
            EditorButton.H5 -> header(5)
            EditorButton.H6 -> header(6)
            EditorButton.JUSTIFY_LEFT -> align("")
            EditorButton.JUSTIFY_CENTER -> align("center")
            EditorButton.JUSTIFY_RIGHT -> align("right")
            EditorButton.JUSTIFY_FULL -> align("justify")
            EditorButton.ORDERED -> insertOrderedList()
            EditorButton.UNORDERED -> insertUnorderedList()
            EditorButton.CHECK -> insertCheckList()
            EditorButton.INDENT -> indent()
            EditorButton.OUTDENT -> outdent()
            EditorButton.LINE -> insertHorizontalRule()
            EditorButton.BLOCK_QUOTE -> formatBlockquote()
            EditorButton.BLOCK_CODE -> formatBlockCode()
            EditorButton.CODE_VIEW -> codeView()
            EditorButton.LINK -> try {
                createLink(options[0] as String)
            } catch (e: Exception) { mWebView.context.toast("Wrong param(s)!") }
            EditorButton.IMAGE -> getSelection( ValueCallback {
                try {
                    // Check params
                    if (options.size < 2) mWebView.context.toast("Missing param(s)!")
                    else {
                        val selection = Gson().fromJson<Map<String, Int>>(it)
                        // BASE64 mode and URL mode
                        if (options[0] as Boolean) insertImageB64(selection["index"]!!, options[1] as String)
                        else insertImage(selection["index"]!!, options[1] as String)
                    }
                } catch (e: Exception) { mWebView.context.toast("Something went wrong! Param?") }
            } )
            EditorButton.SIZE -> fontSize(options[0] as String)
            EditorButton.FORE_COLOR -> foreColor(options[0] as String)
            EditorButton.BACK_COLOR -> backColor(options[0] as String)
        }
    }

    fun selectingLink() = !currentFormat.link.isNullOrBlank()
}