package com.ebolo.krichtexteditor

import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.BOLD
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
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.STRIKETHROUGH
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SUBSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SUPERSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNDERLINE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNORDERED
import com.ebolo.krichtexteditor.utils.FontStyle
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder

/**
 * Rich Editor = Rich Editor Action + Rich Editor Callback
 * Created by even.wu on 8/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class RichEditor(private val mWebView: WebView, private val callback: ((type: Int, value: String) -> Unit)?) {
    private val gson by lazy { GsonBuilder().setPrettyPrinting().create() }
    private var mFontStyle = FontStyle()
    var html: String? = null

    private val mFontBlockGroup by lazy { listOf(NORMAL, H1, H2, H3, H4, H5, H6) }
    private val mTextAlignGroup by lazy { listOf(JUSTIFY_LEFT, JUSTIFY_CENTER, JUSTIFY_RIGHT, JUSTIFY_FULL) }
    private val mListStyleGroup by lazy{ listOf(ORDERED, UNORDERED) }

    var placeHolder = "Start writing..."

    @JavascriptInterface
    fun returnHtml(html: String) { this.html = html }

    @JavascriptInterface
    fun updateCurrentStyle(currentStyle: String) = try {
        updateStyle(gson.fromJson(currentStyle))
    } catch (e: Exception) {} // ignored

    @JavascriptInterface
    fun getInitText() = placeHolder

    private fun updateStyle(fontStyle: FontStyle) {
        Log.d("FontStyle", gson.toJson(fontStyle))

        if (mFontStyle.fontFamily == null || mFontStyle.fontFamily != fontStyle.fontFamily) {
            if (fontStyle.fontFamily!!.isNotBlank()) {
                notifyFontStyleChange(
                        ActionImageView.FAMILY,
                        fontStyle.fontFamily!!
                                .split(",")[0]
                                .replace("\"", "")
                )
            }
        }

        if (mFontStyle.fontForeColor == null || mFontStyle.fontForeColor == fontStyle.fontForeColor) {
            if (fontStyle.fontForeColor!!.isNotBlank()) {
                notifyFontStyleChange(ActionImageView.FORE_COLOR, fontStyle.fontForeColor!!)
            }
        }

        if (mFontStyle.fontBackColor == null || mFontStyle.fontBackColor != fontStyle.fontBackColor) {
            if (fontStyle.fontBackColor!!.isNotBlank()) {
                notifyFontStyleChange(ActionImageView.BACK_COLOR, fontStyle.fontBackColor!!)
            }
        }

        if (mFontStyle.fontSize != fontStyle.fontSize) {
            notifyFontStyleChange(ActionImageView.SIZE, fontStyle.fontSize.toString())
        }

        if (mFontStyle.getTextAlign() != fontStyle.getTextAlign()) {
            mTextAlignGroup.forEach {
                notifyFontStyleChange(it, (it == fontStyle.getTextAlign()).toString())
            }
        }

        if (mFontStyle.getLineHeight() != fontStyle.getLineHeight()) {
            notifyFontStyleChange(
                    ActionImageView.LINE_HEIGHT,
                    fontStyle.getLineHeight().toString()
            )
        }

        if (mFontStyle.isBold() != fontStyle.isBold()) {
            notifyFontStyleChange(BOLD, fontStyle.isBold().toString())
        }

        if (mFontStyle.isItalic() != fontStyle.isItalic()) {
            notifyFontStyleChange(ITALIC, fontStyle.isItalic().toString())
        }

        if (mFontStyle.isUnderline() != fontStyle.isUnderline()) {
            notifyFontStyleChange(UNDERLINE, fontStyle.isUnderline().toString())
        }

        if (mFontStyle.isSubscript() != fontStyle.isSubscript()) {
            notifyFontStyleChange(SUBSCRIPT, fontStyle.isSubscript().toString())
        }

        if (mFontStyle.isSuperscript() != fontStyle.isSuperscript()) {
            notifyFontStyleChange(SUPERSCRIPT, fontStyle.isSuperscript().toString())
        }

        if (mFontStyle.isStrikethrough() != fontStyle.isStrikethrough()) {
            notifyFontStyleChange(STRIKETHROUGH, fontStyle.isStrikethrough().toString())
        }

        if (mFontStyle.getFontBlock() != fontStyle.getFontBlock()) {
            mFontBlockGroup.forEach {
                notifyFontStyleChange(it, (it == fontStyle.getFontBlock()).toString())
            }
        }

        if (mFontStyle.getListStyle() != fontStyle.getListStyle()) {
            mListStyleGroup.forEach {
                notifyFontStyleChange(it, (it == fontStyle.getListStyle()).toString())
            }
        }

        mFontStyle = fontStyle
    }

    private fun notifyFontStyleChange(
            @ActionImageView.Companion.ActionType type: Int,
            value: String
    ) { callback?.invoke(type, value) }

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
    fun superscript() = load("javascript:superscript()")
    fun subscript() = load("javascript:subscript()")
    fun backColor(color: String) = load("javascript:backColor('$color')")
    fun foreColor(color: String) = load("javascript:foreColor('$color')")
    fun fontName(fontName: String) = load("javascript:fontName('$fontName')")
    fun fontSize(foreSize: Double) = load("javascript:fontSize($foreSize)")

    // Paragraph
    fun justifyLeft() = load("javascript:justifyLeft()")
    fun justifyRight() = load("javascript:justifyRight()")
    fun justifyCenter() = load("javascript:justifyCenter()")
    fun justifyFull() = load("javascript:justifyFull()")
    fun insertOrderedList() = load("javascript:insertOrderedList()")
    fun insertUnorderedList() = load("javascript:insertUnorderedList()")
    fun indent() = load("javascript:indent()")
    fun outdent() = load("javascript:outdent()")
    fun formatPara() = load("javascript:formatPara()")
    fun formatH1() = load("javascript:formatH1()")
    fun formatH2() = load("javascript:formatH2()")
    fun formatH3() = load("javascript:formatH3()")
    fun formatH4() = load("javascript:formatH4()")
    fun formatH5() = load("javascript:formatH5()")
    fun formatH6() = load("javascript:formatH6()")
    fun lineHeight(lineHeight: Double) = load("javascript:lineHeight($lineHeight)")
    fun insertImageUrl(imageUrl: String) = load("javascript:insertImageUrl('$imageUrl')")
    fun insertImageData(fileName: String, base64Str: String) {
        val imageUrl = "data:image/" + fileName
                .split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()[1] + ";base64," + base64Str
        load("javascript:insertImageUrl('$imageUrl')")
    }
    fun insertText(text: String) = load("javascript:insertText('$text')")
    fun createLink(linkText: String, linkUrl: String) = load("javascript:createLink('$linkText','$linkUrl')")
    fun unlink() = load("javascript:unlink()")
    fun codeView() = load("javascript:codeView()")
    fun insertTable(colCount: Int, rowCount: Int) = load("javascript:insertTable('" + colCount + "x" + rowCount + "')")
    fun insertHorizontalRule() = load("javascript:insertHorizontalRule()")
    fun formatBlockquote() = load("javascript:formatBlock('blockquote')")
    fun formatBlockCode() = load("javascript:formatBlock('pre')")
    fun insertHtml(html: String) = load("javascript:pasteHTML('$html')")
    // fun refreshHtml(callback: RichEditorCallback) = load("javascript:refreshHTML()")

    private fun load(trigger: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.mWebView.evaluateJavascript(trigger, null)
        } else {
            this.mWebView.loadUrl(trigger)
        }
    }

    fun command(@ActionImageView.Companion.ActionType mActionType: Int) {
        when (mActionType) {
            ActionImageView.BOLD -> bold()
            ActionImageView.ITALIC -> italic()
            ActionImageView.UNDERLINE -> underline()
            ActionImageView.SUBSCRIPT -> subscript()
            ActionImageView.SUPERSCRIPT -> superscript()
            ActionImageView.STRIKETHROUGH -> strikethrough()
            ActionImageView.NORMAL -> formatPara()
            ActionImageView.H1 -> formatH1()
            ActionImageView.H2 -> formatH2()
            ActionImageView.H3 -> formatH3()
            ActionImageView.H4 -> formatH4()
            ActionImageView.H5 -> formatH5()
            ActionImageView.H6 -> formatH6()
            ActionImageView.JUSTIFY_LEFT -> justifyLeft()
            ActionImageView.JUSTIFY_CENTER -> justifyCenter()
            ActionImageView.JUSTIFY_RIGHT -> justifyRight()
            ActionImageView.JUSTIFY_FULL -> justifyFull()
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
}