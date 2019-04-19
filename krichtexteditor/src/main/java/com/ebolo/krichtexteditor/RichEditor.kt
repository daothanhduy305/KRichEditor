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
        // Allow the webview layer to access the placeholder string
        @JavascriptInterface get

    lateinit var mWebView: WebView
    var onInitialized: (() -> Unit)? = null
    var styleUpdatedCallback: ((type: Int, value: Any) -> Unit)? = null

    // region Low level function access

    /**
     * Method to allow the lower webview layer to set the html content
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param html String
     */
    @JavascriptInterface
    fun returnHtml(html: String) { this.html = html }

    /**
     * Method to allow the lower webview layer to be able to set the current style data
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param currentStyle String
     */
    @JavascriptInterface
    fun updateCurrentStyle(currentStyle: String) = try {
        Log.d("FontStyle", currentStyle)
        updateStyle(gson.fromJson(currentStyle))
    } catch (e: Exception) {} // ignored

    /**
     * Method to allow the lower webview layer to be able to log the message to Android logging
     * interface
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param message String
     * @return Int
     */
    @JavascriptInterface
    fun debugJs(message: String) = Log.d("JS", message)

    /**
     * Method to allow the lower webview layer to invoke the code block set to be run on init
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @return Unit?
     */
    @JavascriptInterface
    fun onInitialized() = onInitialized?.invoke()

    // endregion

    // region Inner interfaces

    /**
     * Interface as the callback for Java API on HTML returned
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    interface OnHtmlReturned { fun process(html: String) }

    /**
     * Interface as the callback for Java API on text returned
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    interface OnTextReturned { fun process(text: String) }

    /**
     * Interface as the callback for Java API on contents (delta) returned
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    interface OnContentsReturned { fun process(contents: String) }

    // endregion

    /**
     * Method to refresh the current style
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    fun refreshStyle() = getStyle( ValueCallback {
        try { updateStyle(gson.fromJson(it)) } catch (e: Exception) {} // ignored
    } )

    /**
     * Private function to update the current style of the editor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param quillFormat QuillFormat
     */
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

    /**
     * Private method to notify when the style has been changed
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param type Int of the action button
     * @param value Any data for the action
     */
    private fun notifyFontStyleChange(@EditorButton.Companion.ActionType type: Int, value: Any) {
        when (styleUpdatedCallback) {
            null -> EventBus.getInstance().post("style", "style_$type", value)
            else -> styleUpdatedCallback!!.invoke(type, value)
        }
    }

    // region Js wrapper

    // region General commands

    /**
     * Method to undo the last change
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun undo() = load("javascript:undo()")

    /**
     * Method to redo the last undo
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun redo() = load("javascript:redo()")

    /**
     * Method to make the cursor to focus into view
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    fun focus() = load("javascript:focus()")

    /**
     * Method to disable the editor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    fun disable() = load("javascript:disable()")

    /**
     * Method to enable back the editor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    fun enable() = load("javascript:enable()")

    // endregion

    // region Font
    /**
     * Method to bold to current text or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun bold() = load("javascript:bold()")

    /**
     * Method to italic to current text or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun italic() = load("javascript:italic()")

    /**
     * Method to underline to current text or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun underline() = load("javascript:underline()")

    /**
     * Method to strike to current text or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun strikethrough() = load("javascript:strikethrough()")

    /**
     * Method to apply sub/superscript to current text or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param style String available styles ['sub', 'super', '']
     */
    private fun script(style: String) = load("javascript:script('$style')")

    /**
     * Method to change the background color of current text selection or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param color String
     */
    private fun backColor(color: String) = load("javascript:background('$color')")

    /**
     * Method to change the foreground color of current text selection or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param color String
     */
    private fun foreColor(color: String) = load("javascript:color('$color')")

    /**
     * Method to change font size to current text or the next at the cursor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param size String Available font sizes ['small', 'large', 'huge', '']
     */
    private fun fontSize(size: String) = load("javascript:fontSize('$size')")

    // endregion

    // region Paragraph

    /**
     * Method to align the current paragraph
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param style String Available styles ['center', 'right', 'justify', '']
     */
    private fun align(style: String) = load("javascript:align('$style')")

    /**
     * Method to insert an ordered list
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun insertOrderedList() = load("javascript:insertOrderedList()")

    /**
     * Method to insert an un-ordered list
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun insertUnorderedList() = load("javascript:insertUnorderedList()")

    /**
     * Method to insert a check list
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun insertCheckList() = load("javascript:insertCheckList()")

    /**
     * Method to indent the current paragraph
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun indent() = load("javascript:indent()")

    /**
     * Method to outdent the current paragraph
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun outdent() = load("javascript:outdent()")

    /**
     * Method to format the current paragraph into a blockquote
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun formatBlockquote() = load("javascript:formatBlock('blockquote')")

    /**
     * Method to format the current paragraph into a code block
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun formatBlockCode() = load("javascript:formatBlock('pre')")

    /**
     * Method to get the current selection and do (any) actions on the result
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String>? action to do
     */
    fun getSelection(callback: ValueCallback<String>? = null) = load("javascript:getSelection()", callback)

    /**
     * Method to get the current style at the cursor and do (any) actions on it
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String>? action to do
     */
    private fun getStyle(callback: ValueCallback<String>? = null) = load("javascript:getStyle()", callback)

    /**
     * Private method to get the HTML content and do (any) actions on the result
     * This method is to be utilized by the public ones
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callBack ValueCallback<String> action to do
     */
    // Correcting javascript method name from `getHtmlContent()' to 'getHtml()'
    // private fun getHtmlContent(callBack: ValueCallback<String>) = load("javascript:getHtmlContent()", callBack)
    private fun getHtmlContent(callBack: ValueCallback<String>) = load("javascript:getHtml()", callBack)

    /**
     * Method to get the HTML content and do (any) actions on the result
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    fun getHtmlContent(callback: ((html: String) -> Unit)? = null) = getHtmlContent( ValueCallback { html ->
        val escapedData = html
                // There a bug? that the returned result has the unicode for < instead of the  char
                // and has double \\. So we are escaping them here
                .replace(oldValue = "\\u003C", newValue = "<")
                .replace(oldValue = "\\\"", newValue = "\"")
        callback?.invoke(escapedData.substring(startIndex = 1, endIndex = escapedData.length - 1))
    } )

    /**
     * Method to allow setting the HTML content to the editor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param htmlContent String
     * @param replaceCurrentContent Boolean set to true replace the whole content, false to concatenate
     */
    fun setHtmlContent(
            htmlContent: String,
            replaceCurrentContent: Boolean = true
    ) = load("javascript:setHtml('$htmlContent', $replaceCurrentContent)")

    /**
     * Method to get the HTML content and do (any) actions on the result - Java version
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    fun getHtmlContent(callback: OnHtmlReturned) = getHtmlContent { callback.process(it) }

    /**
     * Private method to get the HTML content and do (any) actions on the result
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    private fun getText(callback: ValueCallback<String>) = load("javascript:getText()", callback)

    /**
     * Method to get the text content and do (any) actions on the result
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    fun getText(callback: ((text: String) -> Unit)?) = getText( ValueCallback {
        callback?.invoke(it.substring(1, it.length - 1).replace("\\n", "\n"))
    } )

    /**
     * Method to get the text content and do (any) actions on the result - Java version
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    fun getText(callback: OnTextReturned) = getText { callback.process(it) }

    /**
     * Private method to get the delta content and do (any) actions on the result
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    private fun getContents(callback: ValueCallback<String>) = load("javascript:getContents()", callback)

    /**
     * Method to get the delta content and do (any) actions on the result
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    fun getContents(callback: ((text: String) -> Unit)?) = getContents( ValueCallback { callback?.invoke(it) } )

    /**
     * Method to get the delta content and do (any) actions on the result - Java version
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param callback ValueCallback<String> action to do
     */
    fun getContents(callback: OnContentsReturned) = getContents { callback.process(it) }

    /**
     * Method to set the delta content to the editor
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param data String delta content as string
     */
    fun setContents(data: String) = load("javascript:setContents($data)")

    /**
     * Method to format the current paragraph to the header styles
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param level Int 1 -> 6
     */
    private fun header(level: Int) = load("javascript:header($level)")

    /**
     * Method to toggle the code view to the current paragraph
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun codeView() = load("javascript:codeView()")

    // endregion

    // region Advanced formatting

    /**
     * Method to allow inserting an image by url to a specific selection index
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param index Int
     * @param url String
     */
    private fun insertImage(index: Int, url: String) = load("javascript:insertEmbed($index, 'image', '$url')")

    /**
     * Method to allow inserting an image as base 64 data to a specific selection index
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param index Int
     * @param path String path of the image to be converted to base 64
     */
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

    /**
     * Method to allow inserting a hyperlink
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param linkUrl String
     */
    private fun createLink(linkUrl: String) = load("javascript:createLink('$linkUrl')")

    // fun insertTable(colCount: Int, rowCount: Int) = load("javascript:insertTable('${colCount}x$rowCount')")

    /**
     * Method to insert a horizontal line
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     */
    private fun insertHorizontalRule() = load("javascript:insertHorizontalRule()")

    // endregion

    // endregion

    /**
     * Method to evaluate a script and do action on result via the callback
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param trigger String script to be evaluated
     * @param callBack ValueCallback<String>?
     */
    private fun load(trigger: String, callBack: ValueCallback<String>? = null) = mWebView.context.runOnUiThread {
        // Make sure every calls would be run on ui thread
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(trigger, callBack)
        } else {
            mWebView.loadUrl(trigger)
        }
    }

    /**
     * A bridge between Jvm api and JS api
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @param mActionType Int type of calling action
     * @param options Array<out Any>
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

    /**
     * Method to return if the current selection is a link
     *
     * @author ebolo (daothanhduy305@gmail.com)
     * @since 0.0.1
     *
     * @return Boolean
     */
    fun selectingLink() = !currentFormat.link.isNullOrBlank()
}
