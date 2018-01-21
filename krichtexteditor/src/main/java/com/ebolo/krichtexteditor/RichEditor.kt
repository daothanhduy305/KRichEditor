package com.ebolo.krichtexteditor

import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
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
import com.google.gson.GsonBuilder

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

    var mWebView: WebView? = null
    var styleUpdatedCallback: ((type: Int, value: String) -> Unit)? = null

    @JavascriptInterface
    fun returnHtml(html: String) { this.html = html }

    @JavascriptInterface
    fun updateCurrentStyle(currentStyle: String) = try {
        Log.d("Format", currentStyle)
        updateStyle(gson.fromJson(currentStyle))
    } catch (e: Exception) {} // ignored

    @JavascriptInterface
    fun getInitText() = placeHolder

    @JavascriptInterface
    fun debugJs(message: String) {
        Log.d("JS", message)
    }

    fun updateStyle() = getStyle( ValueCallback {
        try { updateStyle(gson.fromJson(it)) } catch (e: Exception) {} // ignored
    } )

    private fun updateStyle(quillFormat: QuillFormat) {
        // Log.d("FontStyle", gson.toJson(fontStyle))

        if (currentFormat.isBold != quillFormat.isBold) {
            notifyFontStyleChange(BOLD, quillFormat.isBold.toString())
        }

        if (currentFormat.isItalic != quillFormat.isItalic) {
            notifyFontStyleChange(ITALIC, quillFormat.isItalic.toString())
        }

        if (currentFormat.isUnderline != quillFormat.isUnderline) {
            notifyFontStyleChange(UNDERLINE, quillFormat.isUnderline.toString())
        }

        if (currentFormat.isStrike != quillFormat.isStrike) {
            notifyFontStyleChange(STRIKETHROUGH, quillFormat.isStrike.toString())
        }

        if (currentFormat.isCode != quillFormat.isCode) {
            notifyFontStyleChange(CODE_VIEW, quillFormat.isCode.toString())
        }

        quillFormat.header = quillFormat.header ?: 0

        if (currentFormat.header != quillFormat.header) {
            mFontBlockGroup.indices.forEach {
                notifyFontStyleChange(
                        mFontBlockGroup[it],
                        (quillFormat.header == it).toString()
                )
            }
        }

        if (currentFormat.script != quillFormat.script) {
            notifyFontStyleChange(SUBSCRIPT, (quillFormat.script == "sub").toString())
            notifyFontStyleChange(SUPERSCRIPT, (quillFormat.script == "super").toString())
        }

        quillFormat.align = quillFormat.align ?: ""

        if (currentFormat.align != quillFormat.align) {
            mTextAlignGroup.forEach {
                notifyFontStyleChange(
                        it.key,
                        (quillFormat.align == it.value).toString()
                )
            }
        }

        if (currentFormat.list != quillFormat.list) {
            mListStyleGroup.forEach {
                notifyFontStyleChange(
                        it.key,
                        (quillFormat.list == it.value).toString()
                )
            }
        }

        notifyFontStyleChange(SIZE, quillFormat.size)

        currentFormat = quillFormat
    }

    private fun notifyFontStyleChange(
            @EditorButton.Companion.ActionType type: Int,
            value: String
    ) { styleUpdatedCallback?.invoke(type, value) }

    // Start of Js wrapper
    fun undo() = load("javascript:undo()")
    fun redo() = load("javascript:redo()")
    fun focus() = load("javascript:focus()")
    fun disable() = load("javascript:disable()")
    fun enable() = load("javascript:enable()")

    // Font
    fun bold(state: Boolean = true) = load("javascript:bold($state)")
    fun italic(state: Boolean = true) = load("javascript:italic($state)")
    fun underline(state: Boolean = true) = load("javascript:underline($state)")
    fun strikethrough(state: Boolean = true) = load("javascript:strikethrough($state)")
    fun script(style: String, state: Boolean = true) = load("javascript:script('$style', $state)")
    fun backColor(color: String) = load("javascript:background('$color')")
    fun foreColor(color: String) = load("javascript:color('$color')")
    fun fontName(fontName: String) = load("javascript:fontName('$fontName')")
    fun fontSize(size: String) = load("javascript:fontSize('$size')")

    // Paragraph
    fun align(style: String, state: Boolean = true) = load("javascript:align('$style', $state)")
    fun insertOrderedList() = load("javascript:insertOrderedList()")
    fun insertUnorderedList() = load("javascript:insertUnorderedList()")
    fun indent() = load("javascript:indent()")
    fun outdent() = load("javascript:outdent()")
    fun header(level: Int) = load("javascript:header($level)")
    fun lineHeight(lineHeight: Double) = load("javascript:lineHeight($lineHeight)")
    /*fun insertImageData(fileName: String, base64Str: String) {
        val imageUrl = "data:image/${
        fileName.split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        };base64,$base64Str"
        load("javascript:insertImageUrl('$imageUrl')")
    }*/
    fun insertImage(index: Int, url: String) = load("javascript:insertEmbed($index, 'image', '$url')")
    fun createLink(linkUrl: String) = load("javascript:createLink('$linkUrl')")
    fun codeView() = load("javascript:codeView()")
    // fun insertTable(colCount: Int, rowCount: Int) = load("javascript:insertTable('${colCount}x$rowCount')")
    fun insertHorizontalRule() = load("javascript:insertHorizontalRule()")
    fun formatBlockquote() = load("javascript:formatBlock('blockquote')")
    fun formatBlockCode() = load("javascript:formatBlock('pre')")
    fun getSelection(callback: ValueCallback<String>? = null) = load("javascript:getSelection()", callback)
    fun getStyle(callback: ValueCallback<String>? = null) = load("javascript:getStyle()", callback)

    private fun getHtml(callBack: ValueCallback<String>) = load("javascript:getHtml()", callBack)
    fun getHtml(callback: ((html: String) -> Unit)? = null) = getHtml( ValueCallback { html ->
        val escapedData = html
                .replace(oldValue = "\\u003C", newValue = "<")
                .replace(oldValue = "\\\"", newValue = "\"")
        callback?.invoke(escapedData.substring(startIndex = 1, endIndex = escapedData.length - 1))
    } )

    private fun getText(callback: ValueCallback<String>) = load("javascript:getText()", callback)
    fun getText(callback: ((text: String) -> Unit)?) = getText( ValueCallback {
        callback?.invoke(it.substring(1, it.length - 1).replace("\\n", "\n"))
    } )

    private fun load(trigger: String, callBack: ValueCallback<String>? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.mWebView?.evaluateJavascript(trigger, callBack)
        } else {
            this.mWebView?.loadUrl(trigger)
        }
    }

    /**
     * Function:    command
     * Description: A bridge between Jvm api and JS api
     * @param   mActionType type of calling action
     * @param   reFocus     we disable the editor as a workaround when menu is shown,
     *                      by setting this to true would make the editor have the focus again
     */
    fun command(@EditorButton.Companion.ActionType mActionType: Int, reFocus: Boolean = true) {
        when (mActionType) {
            EditorButton.BOLD -> bold(reFocus)
            EditorButton.ITALIC -> italic(reFocus)
            EditorButton.UNDERLINE -> underline(reFocus)
            EditorButton.SUBSCRIPT -> script("sub", reFocus)
            EditorButton.SUPERSCRIPT -> script("super", reFocus)
            EditorButton.STRIKETHROUGH -> strikethrough(reFocus)
            EditorButton.NORMAL -> header(0)
            EditorButton.H1 -> header(1)
            EditorButton.H2 -> header(2)
            EditorButton.H3 -> header(3)
            EditorButton.H4 -> header(4)
            EditorButton.H5 -> header(5)
            EditorButton.H6 -> header(6)
            EditorButton.JUSTIFY_LEFT -> align("", reFocus)
            EditorButton.JUSTIFY_CENTER -> align("center", reFocus)
            EditorButton.JUSTIFY_RIGHT -> align("right", reFocus)
            EditorButton.JUSTIFY_FULL -> align("justify", reFocus)
            EditorButton.ORDERED -> insertOrderedList()
            EditorButton.UNORDERED -> insertUnorderedList()
            EditorButton.INDENT -> indent()
            EditorButton.OUTDENT -> outdent()
            EditorButton.LINE -> insertHorizontalRule()
            EditorButton.BLOCK_QUOTE -> formatBlockquote()
            EditorButton.BLOCK_CODE -> formatBlockCode()
            EditorButton.CODE_VIEW -> codeView()
        }
    }

    fun selectingLink() = !currentFormat.link.isNullOrBlank()
}