package com.ebolo.krichtexteditor

import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.BOLD
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.CODE_VIEW
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H1
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H2
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H3
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H4
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H5
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H6
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.ITALIC
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_CENTER
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_FULL
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_LEFT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_RIGHT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.NORMAL
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.ORDERED
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SIZE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.STRIKETHROUGH
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SUBSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SUPERSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNDERLINE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNORDERED
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

        if (currentFormat.size != quillFormat.size) {
            notifyFontStyleChange(SIZE, quillFormat.size ?: "normal")
        }

        currentFormat = quillFormat
    }

    private fun notifyFontStyleChange(
            @ActionImageView.Companion.ActionType type: Int,
            value: String
    ) { styleUpdatedCallback?.invoke(type, value) }

    // Start of Js wrapper
    fun undo() = load("javascript:undo()")
    fun redo() = load("javascript:redo()")
    fun focus() = load("javascript:focus()")
    fun disable() = load("javascript:disable()")
    fun enable() = load("javascript:enable()")

    // Font
    fun bold() = load("javascript:bold()")
    fun italic() = load("javascript:italic()")
    fun underline() = load("javascript:underline()")
    fun strikethrough() = load("javascript:strikethrough()")
    fun script(style: String) = load("javascript:script('$style')")
    fun backColor(color: String) = load("javascript:background('$color')")
    fun foreColor(color: String) = load("javascript:color('$color')")
    fun fontName(fontName: String) = load("javascript:fontName('$fontName')")
    fun fontSize(size: String) = load("javascript:fontSize('$size')")

    // Paragraph
    fun align(style: String) = load("javascript:align('$style')")
    fun insertOrderedList() = load("javascript:insertOrderedList()")
    fun insertUnorderedList() = load("javascript:insertUnorderedList()")
    fun indent() = load("javascript:indent()")
    fun outdent() = load("javascript:outdent()")
    fun header(level: Int) = load("javascript:header($level)")
    fun lineHeight(lineHeight: Double) = load("javascript:lineHeight($lineHeight)")
    fun insertImageUrl(imageUrl: String) = load("javascript:insertImageUrl('$imageUrl')")
    fun insertImageData(fileName: String, base64Str: String) {
        val imageUrl = "data:image/${
        fileName.split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        };base64,$base64Str"
        load("javascript:insertImageUrl('$imageUrl')")
    }
    fun insertText(text: String) = load("javascript:insertText('$text')")
    fun createLink(linkUrl: String) = load("javascript:createLink('$linkUrl')")
    fun codeView() = load("javascript:codeView()")
    fun insertTable(colCount: Int, rowCount: Int) = load("javascript:insertTable('${colCount}x$rowCount')")
    fun insertHorizontalRule() = load("javascript:insertHorizontalRule()")
    fun formatBlockquote() = load("javascript:formatBlock('blockquote')")
    fun formatBlockCode() = load("javascript:formatBlock('pre')")
    fun insertHtml(html: String) = load("javascript:pasteHTML('$html')")
    fun updateStyle() = load("javascript:updateCurrentStyle()")
    fun getSelection(callBack: ValueCallback<String>? = null) = load("javascript:getSelection()", callBack)
    fun getHtml(callBack: ValueCallback<String>) = load("javascript:getHtml()", callBack)

    private fun load(trigger: String, callBack: ValueCallback<String>? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.mWebView?.evaluateJavascript(trigger, callBack)
        } else {
            this.mWebView?.loadUrl(trigger)
        }
    }

    fun command(@ActionImageView.Companion.ActionType mActionType: Int) {
        when (mActionType) {
            ActionImageView.BOLD -> bold()
            ActionImageView.ITALIC -> italic()
            ActionImageView.UNDERLINE -> underline()
            ActionImageView.SUBSCRIPT -> script("sub")
            ActionImageView.SUPERSCRIPT -> script("super")
            ActionImageView.STRIKETHROUGH -> strikethrough()
            ActionImageView.NORMAL -> header(0)
            ActionImageView.H1 -> header(1)
            ActionImageView.H2 -> header(2)
            ActionImageView.H3 -> header(3)
            ActionImageView.H4 -> header(4)
            ActionImageView.H5 -> header(5)
            ActionImageView.H6 -> header(6)
            ActionImageView.JUSTIFY_LEFT -> align("")
            ActionImageView.JUSTIFY_CENTER -> align("center")
            ActionImageView.JUSTIFY_RIGHT -> align("right")
            ActionImageView.JUSTIFY_FULL -> align("justify")
            ActionImageView.ORDERED -> insertOrderedList()
            ActionImageView.UNORDERED -> insertUnorderedList()
            ActionImageView.INDENT -> indent()
            ActionImageView.OUTDENT -> outdent()
            ActionImageView.LINE -> insertHorizontalRule()
            ActionImageView.BLOCK_QUOTE -> formatBlockquote()
            ActionImageView.BLOCK_CODE -> formatBlockCode()
            ActionImageView.CODE_VIEW -> codeView()
        }
    }

    fun selectingLink() = !currentFormat.link.isNullOrBlank()
}