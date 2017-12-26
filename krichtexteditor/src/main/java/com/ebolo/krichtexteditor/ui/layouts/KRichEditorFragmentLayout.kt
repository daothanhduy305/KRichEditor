package com.ebolo.krichtexteditor.ui.layouts

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.RichEditor
import com.ebolo.krichtexteditor.fragments.EditHyperlinkFragment
import com.ebolo.krichtexteditor.fragments.KRichEditorFragment
import com.ebolo.krichtexteditor.fragments.editHyperlinkDialog
import com.ebolo.krichtexteditor.fragments.editTableDialog
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.BACK_COLOR
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.BLOCK_CODE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.BLOCK_QUOTE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.BOLD
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.CODE_VIEW
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.FAMILY
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.FORE_COLOR
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H1
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H2
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H3
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H4
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H5
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H6
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.IMAGE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.INDENT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.ITALIC
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_CENTER
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_FULL
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_LEFT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_RIGHT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.LINE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.LINE_HEIGHT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.LINK
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.NORMAL
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.ORDERED
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.OUTDENT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.REDO
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SIZE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.STRIKETHROUGH
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SUBSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.SUPERSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.TABLE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNDERLINE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNDO
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNORDERED
import com.ebolo.krichtexteditor.ui.widgets.ColorPaletteView
import com.ebolo.krichtexteditor.ui.widgets.TextEditorWebView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onTouch
import java.util.regex.Pattern


class KRichEditorFragmentLayout(
        private val editorFragment: KRichEditorFragment
) : AnkoComponent<KRichEditorFragment> {
    private lateinit var editor: RichEditor
    var imageCallback: (() -> Unit)? = null
    private val formatButtonIds = listOf(
            BOLD, ITALIC, UNDERLINE, SUBSCRIPT, SUPERSCRIPT,
            STRIKETHROUGH, JUSTIFY_LEFT, JUSTIFY_CENTER,
            JUSTIFY_RIGHT, JUSTIFY_FULL, ORDERED,
            UNORDERED, NORMAL, H1, H2, H3, H4, H5, H6
    )
    private val otherButtonIds = listOf(INDENT, OUTDENT, BLOCK_QUOTE, BLOCK_CODE, LINE, CODE_VIEW)
    private lateinit var barFormatButtons: Map<Int, ImageView>
    private var menuFormatButtons = mutableMapOf<Int, View>()

    private lateinit var webView: WebView
    private lateinit var fontFamilyTextView: TextView
    private lateinit var fontSizeTextView: TextView
    private lateinit var lineHeightTextView: TextView
    private lateinit var textColorPalette: ColorPaletteView
    private lateinit var highlightColorPalette: ColorPaletteView
    private lateinit var editorMenu: FrameLayout
    private lateinit var webViewHolder: FrameLayout
    private lateinit var editorToolbar: LinearLayout

    private val fullLayoutParams = LinearLayout.LayoutParams(matchParent, 0, 2f)
    private val halfLayoutParams = LinearLayout.LayoutParams(matchParent, 0, 1f)

    @SuppressLint("SetJavaScriptEnabled")
    override fun createView(ui: AnkoContext<KRichEditorFragment>) = with(ui) {
        verticalLayout {
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
            weightSum = 2f

            webViewHolder = frameLayout {

                webView = ankoView(::TextEditorWebView, 0) {
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()

                    onTouch { _, _ -> hideEditorMenu() }

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    editor = RichEditor(this) { type, value -> updateActionStates(type, value) }
                    addJavascriptInterface(editor, "KRichEditor")
                    loadUrl("file:///android_asset/richEditor.html")
                }.lparams(width = matchParent, height = matchParent)

            }.lparams(width = matchParent, height = 0) { weight = 2f }

            // Outer toolbar holder
            editorToolbar = linearLayout {
                backgroundColorResource = R.color.editor_toolbar_bg_color
                gravity = CENTER_VERTICAL

                imageView(R.drawable.ic_action) {
                    id = R.id.iv_action
                    padding = dip(10)

                    var isShown = false

                    onClick {
                        isShown = !isShown
                        when {
                            isShown -> {
                                // Hide soft keyboard if is shown
                                val imm = ui.ctx
                                        .getSystemService(Context.INPUT_METHOD_SERVICE)
                                        as InputMethodManager
                                imm.hideSoftInputFromWindow(view.windowToken, 0)

                                showEditorMenu()
                            }
                            else -> { hideEditorMenu() }
                        }
                    }
                }.apply { actionImageViewStyle() }

                // Separator
                view {
                    backgroundColor = 0x9e9e9e.opaque
                }.lparams(width = dip(0.5f), height = dip(24))

                horizontalScrollView {
                    id = R.id.hsv_action_bar

                    // Inner Toolbar holder
                    linearLayout {

                        imageView( R.drawable.ic_undo) {
                            id = R.id.iv_action_undo
                            padding = dip(8)
                            backgroundResource = R.drawable.btn_colored_material

                            onClick { onActionPerform(UNDO) }
                        }.apply { actionImageViewStyle() }

                        imageView(R.drawable.ic_redo) {
                            id = R.id.iv_action_redo
                            padding = dip(8)
                            backgroundResource = R.drawable.btn_colored_material

                            onClick { onActionPerform(REDO) }
                        }.apply { actionImageViewStyle() }

                        /*themedImageView(R.style.ActionImageView) {
                            id = R.id.iv_action_txt_color
                            padding = dip(9)
                            imageResource = R.drawable.ic_format_text_color
                            visibility = View.GONE

                        }.lparams(width = wrapContent, height = wrapContent)*/

                        /*themedImageView(R.style.ActionImageView) {
                            id = R.id.iv_action_txt_bg_color
                            padding = dip(9)
                            imageResource = R.drawable.ic_format_text_bg_clolr
                            visibility = View.GONE

                        }.lparams(width = wrapContent, height = wrapContent)*/

                        imageView(R.drawable.ic_insert_photo) {
                            id = R.id.iv_action_insert_image
                            padding = dip(8)
                            backgroundResource = R.drawable.btn_colored_material

                            onClick { onActionPerform(IMAGE) }
                        }.apply { actionImageViewStyle() }

                        imageView(R.drawable.ic_insert_link) {
                            id = R.id.iv_action_insert_link
                            padding = dip(8)
                            backgroundResource = R.drawable.btn_colored_material

                            onClick { onActionPerform(LINK) }
                        }.apply { actionImageViewStyle() }

                        imageView(R.drawable.ic_table) {
                            id = R.id.iv_action_table
                            padding = dip(11)
                            backgroundResource = R.drawable.btn_colored_material

                            onClick { onActionPerform(TABLE) }
                        }.apply { actionImageViewStyle() }

                        /*themedImageView(R.style.ActionImageView) {
                            id = R.id.iv_action_line_height
                            padding = dip(11)
                            imageResource = R.drawable.ic_line_height
                            visibility = View.GONE

                        }.lparams(width = wrapContent, height = wrapContent)*/

                        // Add format buttons
                        barFormatButtons = formatButtonIds.map { type ->
                            type to imageView(ActionImageView.actionButtonDrawables[type]!!) {
                                padding = dip(9)
                                backgroundResource = R.drawable.btn_colored_material

                                onClick { editor.command(type) }
                            }.apply { actionImageViewStyle() }
                        }.toMap()

                        // Add other buttons
                        otherButtonIds.forEach { type ->
                            imageView(ActionImageView.actionButtonDrawables[type]!!) {
                                padding = dip(9)
                                backgroundResource = R.drawable.btn_colored_material

                                onClick { editor.command(type) }
                            }.apply { actionImageViewStyle() }
                        }

                    }.lparams(width = wrapContent, height = dip(40))

                }.lparams(width = matchParent, height = dip(40))

            }.lparams(width = matchParent, height = wrapContent)

            // Editor menu
            editorMenu = frameLayout {
                visibility = View.GONE

                scrollView {
                    verticalLayout {
                        backgroundColorResource = R.color.gray_100

                        linearLayout {
                            backgroundColorResource = R.color.white
                            padding = dip(16)

                            verticalLayout {
                                id = R.id.ll_font_size
                                gravity = Gravity.CENTER
                                backgroundResource = R.drawable.btn_white_round_rectangle

                                textView(R.string.font_size) {
                                    textSize = 10f
                                }

                                fontSizeTextView = textView("16") {
                                    textSize = 18f
                                    textColor = R.color.light_blue_500
                                }.lparams { topMargin = dip(8) }

                            }.lparams(width = dip(100), height = dip(100))


                            verticalLayout {
                                gravity = Gravity.CENTER

                                fontFamilyTextView = textView {
                                    textColor = R.color.textPrimary
                                    textSize = 18f
                                    backgroundResource = R.drawable.btn_white_round_rectangle
                                    gravity = Gravity.CENTER
                                }.lparams(width = matchParent, height = dip(46))

                                linearLayout {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = Gravity.CENTER
                                    setPadding(dip(16), dip(6), dip(16), dip(6))

                                    fun formatButton(drawable: Int, id: Int) =
                                            imageView(drawable) {
                                                this.id = id
                                                padding = dip(8)
                                                backgroundResource = R.drawable.btn_white_material

                                                onClick {  }
                                            }
                                                    .lparams { weight = 1f }
                                                    .apply { actionImageViewStyle() }

                                    val boldButton = formatButton(
                                            R.drawable.ic_format_bold,
                                            R.id.iv_action_bold
                                    )
                                    val italicButton = formatButton(
                                            R.drawable.ic_format_italic,
                                            R.id.iv_action_italic
                                    )
                                    val underlineButton = formatButton(
                                            R.drawable.ic_format_underlined,
                                            R.id.iv_action_underline
                                    )
                                    val strikethroughButton = formatButton(
                                            R.drawable.ic_format_strikethrough,
                                            R.id.iv_action_strikethrough
                                    )

                                    menuFormatButtons.put(BOLD, boldButton)
                                    menuFormatButtons.put(ITALIC, italicButton)
                                    menuFormatButtons.put(UNDERLINE, underlineButton)
                                    menuFormatButtons.put(STRIKETHROUGH, strikethroughButton)

                                }.lparams(width = matchParent, height = dip(46)) { topMargin = dip(8) }

                            }.lparams(width = matchParent, height = dip(100)) { marginStart = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent)

                        verticalLayout {
                            backgroundColorResource = R.color.white
                            padding = dip(16)

                            textView(R.string.font_color) {
                                textSize = 10f
                            }.lparams(width = matchParent)

                            linearLayout {
                                gravity = Gravity.CENTER
                                backgroundResource = R.drawable.round_rectangle_white

                                textColorPalette = ankoView(::ColorPaletteView, 0){}
                                        .lparams(width = matchParent, height = wrapContent)

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                            textView(R.string.font_highlight_color) {
                                id = R.id.textView
                                textSize = 10f
                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(16) }

                            linearLayout {
                                gravity = Gravity.CENTER

                                highlightColorPalette = ankoView(::ColorPaletteView, 0) {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = android.view.Gravity.CENTER
                                }.lparams(width = wrapContent, height = wrapContent) { weight = 1f }

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        linearLayout {
                            backgroundColorResource = R.color.white
                            padding = dip(16)

                            verticalLayout {
                                id = R.id.ll_line_height
                                gravity = Gravity.CENTER
                                backgroundResource = R.drawable.btn_white_round_rectangle

                                textView(R.string.font_spacing) {
                                    textSize = 10f
                                    setPadding(dip(16), 0, dip(16), 0)
                                }

                                lineHeightTextView = textView("16") {
                                    textSize = 18f
                                    textColor = R.color.light_blue_500
                                }.lparams { topMargin = dip(8) }

                            }.lparams(width = wrapContent, height = matchParent) { weight = 1f }

                            linearLayout {
                                backgroundResource = R.drawable.round_rectangle_white
                                gravity = Gravity.CENTER
                                setPadding(0, dip(16), 0, dip(16))

                                fun justifyButton(drawable: Int, id: Int, neighbor: Boolean = false) =
                                        imageView(drawable) {
                                            this.id = id
                                            padding = dip(10)
                                            backgroundResource = R.drawable.btn_white_material

                                            onClick {  }
                                        }.lparams {
                                            if (neighbor) marginStart = dip(16)
                                        }.apply(ImageView::actionImageViewStyle)

                                justifyButton(
                                        R.drawable.ic_format_align_left,
                                        R.id.iv_action_justify_left
                                )
                                justifyButton(
                                        R.drawable.ic_format_align_center,
                                        R.id.iv_action_justify_center,
                                        true
                                )
                                justifyButton(
                                        R.drawable.ic_format_align_right,
                                        R.id.iv_action_justify_right,
                                        true
                                )
                                justifyButton(
                                        R.drawable.ic_format_align_justify,
                                        R.id.iv_action_justify_full,
                                        true
                                )

                            }.lparams(width = wrapContent, height = wrapContent) {
                                marginStart = dip(8)
                                weight = 1f
                            }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        horizontalScrollView {
                            id = R.id.hsv_action_bar

                            linearLayout {
                                padding = dip(16)
                                backgroundColorResource = R.color.white

                                fun headingBlock(
                                        id: Int,
                                        previewText: Pair<String, Float>,
                                        text: Int,
                                        neighbor: Boolean = false
                                ) = verticalLayout {
                                    this.id = id
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = Gravity.CENTER
                                    setPadding(0, 0, 0, dip(8))

                                    textView(previewText.first) {
                                        maxLines = 1
                                        gravity = Gravity.CENTER
                                        textSize = previewText.second
                                    }.lparams(width = wrapContent, height = dip(32))

                                    view {
                                        backgroundColor = 0xe0e0e0.opaque
                                    }.lparams(width = matchParent, height = dip(0.5f)) {
                                        bottomMargin = dip(4)
                                    }

                                    textView(text) { textSize = 10f }

                                }.lparams(width = dip(80), height = matchParent) {
                                    if (neighbor) marginStart = dip(8)
                                }

                                headingBlock(
                                        id = R.id.ll_normal, previewText = "AaBbCcDd" to 10f,
                                        text = R.string.font_style_normal
                                )

                                headingBlock(
                                        id = R.id.ll_h1, previewText = "AaBb" to 18f,
                                        text = R.string.font_style_heading_1, neighbor = true
                                )

                                headingBlock(
                                        id = R.id.ll_h2, previewText = "AaBbC" to 14f,
                                        text = R.string.font_style_heading_2, neighbor = true
                                )

                                headingBlock(
                                        id = R.id.ll_h3, previewText = "AaBbCcD" to 12f,
                                        text = R.string.font_style_heading_3, neighbor = true
                                )

                                headingBlock(
                                        id = R.id.ll_h4, previewText = "AaBbCcDd" to 12f,
                                        text = R.string.heading_4, neighbor = true
                                )

                                headingBlock(
                                        id = R.id.ll_h5, previewText = "AaBbCcDd" to 12f,
                                        text = R.string.heading_5, neighbor = true
                                )

                                headingBlock(
                                        id = R.id.ll_h6, previewText = "AaBbCcDd" to 12f,
                                        text = R.string.heading_6, neighbor = true
                                )

                            }.lparams { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent)

                        fun additionalFormatBox(
                                item1: Pair<Int, Int>,
                                item2: Pair<Int, Int>,
                                item3: Pair<Int, Int>,
                                item4: Pair<Int, Int>
                        ) = linearLayout {
                            backgroundColorResource = R.color.white
                            padding = dip(16)

                            fun innerBox(
                                    item1: Pair<Int, Int>,
                                    item2: Pair<Int, Int>,
                                    isSecond: Boolean = false
                            ) = linearLayout {
                                backgroundResource = R.drawable.round_rectangle_white
                                gravity = Gravity.CENTER
                                setPadding(0, dip(8), 0, dip(8))

                                fun formatButton(item: Pair<Int, Int>) =
                                        imageView(item.first) {
                                            id = item.second
                                            backgroundResource = R.drawable.btn_white_material
                                            padding = dip(10)
                                        }

                                formatButton(item1).apply { actionImageViewStyle() }
                                formatButton(item2)
                                        .lparams { marginStart = dip(32) }
                                        .apply { actionImageViewStyle() }
                            }.lparams(width = wrapContent, height = wrapContent) {
                                weight = 1f
                                if (isSecond) marginStart = dip(8)
                            }

                            innerBox(item1, item2)
                            innerBox(item3, item4, true)

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        additionalFormatBox(
                                item1 = R.drawable.ic_format_indent_increase to R.id.iv_action_indent,
                                item2 = R.drawable.ic_format_indent_decrease to R.id.iv_action_outdent,
                                item3 = R.drawable.ic_format_list_bulleted to R.id.iv_action_insert_bullets,
                                item4 = R.drawable.ic_format_list_numbered to R.id.iv_action_insert_numbers
                        )

                        additionalFormatBox(
                                item1 = R.drawable.ic_format_subscript to R.id.iv_action_subscript,
                                item2 = R.drawable.ic_format_superscript to R.id.iv_action_superscript,
                                item3 = R.drawable.ic_format_quote to R.id.iv_action_blockquote,
                                item4 = R.drawable.ic_code_block to R.id.iv_action_code_block
                        )

                        verticalLayout {
                            backgroundColorResource = R.color.white
                            padding = dip(16)
                            isBaselineAligned = false

                            textView(R.string.font_insert) {
                                textSize = 10f
                            }.lparams(width = matchParent)

                            linearLayout {
                                backgroundResource = R.drawable.round_rectangle_white
                                gravity = Gravity.CENTER
                                padding = dip(8)

                                fun insertButton(drawable: Int, id: Int) =
                                        themedImageView(
                                                drawable,
                                                R.style.ActionImageView
                                        ) {
                                            this.id = id
                                            backgroundResource = R.drawable.btn_white_material
                                            padding = dip(8)
                                        }.lparams { weight = 1f }.apply { actionImageViewStyle() }

                                insertButton(R.drawable.ic_insert_photo, R.id.iv_action_insert_image)
                                insertButton(R.drawable.ic_insert_link, R.id.iv_action_insert_link)
                                insertButton(R.drawable.ic_table, R.id.iv_action_table)
                                insertButton(R.drawable.ic_line, R.id.iv_action_line)

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(4) }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        linearLayout {
                            backgroundColorResource = R.color.white
                            isBaselineAligned = false
                            setPadding(dip(16), dip(8), dip(16), dip(8))

                            linearLayout {
                                backgroundResource = R.drawable.round_rectangle_white
                                gravity = Gravity.CENTER
                                setPadding(dip(16), dip(8), dip(16), dip(8))

                                imageView(R.drawable.ic_code_review) {
                                    id = R.id.iv_action_code_view
                                    backgroundResource = R.drawable.btn_white_material
                                    padding = dip(10)
                                }.apply { actionImageViewStyle() }

                            }.lparams(width = wrapContent, height = wrapContent) { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                    }.lparams(width = matchParent, height = wrapContent)

                }.lparams(width = matchParent, height = wrapContent)

            }.lparams(width = matchParent, height = 0) { weight = 1f }
        }
    }

    private fun onActionPerform(@ActionImageView.Companion.ActionType type: Int, value: String? = null) {
        when (type) {
            UNDO -> editor.undo()
            REDO -> editor.redo()
            SIZE -> editor.fontSize(value!!.toDouble())
            LINE_HEIGHT -> editor.lineHeight(value!!.toDouble())
            FORE_COLOR -> editor.foreColor(value!!)
            BACK_COLOR -> editor.backColor(value!!)
            FAMILY -> editor.fontName(value!!)
            IMAGE -> imageCallback?.invoke()
            LINK -> {
                val dialog = editHyperlinkDialog {
                    onLinkSet { address, text -> editor.createLink(text, address) }
                }
                dialog.show(editorFragment.fragmentManager, EditHyperlinkFragment::class.java.simpleName)
            }
            TABLE -> {
                val dialog = editTableDialog {
                    onTableSet { row, column -> editor.insertTable(column, row) }
                }
                dialog.show(editorFragment.fragmentManager, EditHyperlinkFragment::class.java.simpleName)
            }
            in formatButtonIds -> {
                barFormatButtons[type]?.performClick()
            }
        }
    }

    private fun updateActionStates(@ActionImageView.Companion.ActionType type: Int, value: String) {
        updateActionStateToolbar(type, value)
        updateActionStateMenu(type, value)
    }

    private fun updateActionStateMenu(@ActionImageView.Companion.ActionType type: Int, value: String) {
        if (editorMenu.visibility == View.VISIBLE)
            when (type) {
                FAMILY -> fontFamilyTextView.text = value
                SIZE -> fontSizeTextView.text = value.toDouble().toInt().toString()
                FORE_COLOR, BACK_COLOR -> {
                    val selectedColor = rgbToHex(value)
                    if (selectedColor != null) {
                        if (type == FORE_COLOR) textColorPalette.selectedColor = selectedColor
                        else highlightColorPalette.selectedColor = selectedColor
                    }
                }
                LINE_HEIGHT -> lineHeightTextView.text = value
                in formatButtonIds -> updateActionStates(type, value.toBoolean())
            }

    }

    private fun updateActionStateToolbar(@ActionImageView.Companion.ActionType type: Int, value: String) {
        if (type in formatButtonIds) {
            // Log.d("Toolbar", "Type = $type, value = $value")
            barFormatButtons[type]?.setColorFilter(ContextCompat.getColor(editorFragment.context,
                    when {
                        value.toBoolean() -> editorFragment.formatButtonActivatedColor
                        else -> editorFragment.formatButtonDeactivatedColor
                    }
            ))
        }
    }

    private fun updateActionStates(@ActionImageView.Companion.ActionType type: Int, isActive: Boolean) {
        /*rootView.post(Runnable {
            var view: View? = null
            for (e in mViewTypeMap.entries) {
                val key = e.key
                if (e.value === type) {
                    view = rootView.findViewById(key!!)
                    break
                }
            }

            if (view == null) {
                return@Runnable
            }

            when (type) {
                BOLD, ITALIC, UNDERLINE, SUBSCRIPT, SUPERSCRIPT, STRIKETHROUGH,
                JUSTIFY_LEFT, JUSTIFY_CENTER, JUSTIFY_RIGHT, JUSTIFY_FULL,
                ORDERED, CODE_VIEW, UNORDERED -> if (isActive) {
                    (view as ImageView).setColorFilter(
                            ContextCompat.getColor(getContext(), R.color.colorAccent))
                } else {
                    (view as ImageView).setColorFilter(
                            ContextCompat.getColor(getContext(), R.color.tintColor))
                }
                NORMAL, H1, H2, H3, H4, H5, H6 -> if (isActive) {
                    view.setBackgroundResource(R.drawable.round_rectangle_blue)
                } else {
                    view.setBackgroundResource(R.drawable.round_rectangle_white)
                }
                else -> {
                }
            }
        })*/
    }

    private fun rgbToHex(rgb: String): String? {
        val c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)")
        val m = c.matcher(rgb)
        return if (m.matches()) {
            String.format("#%02x%02x%02x", Integer.valueOf(m.group(1)),
                    Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)))
        } else null
    }

    private fun hideEditorMenu() {
        // Make the webview to own the whole space
        webViewHolder.layoutParams = fullLayoutParams
        editorMenu.visibility = View.GONE
    }

    private fun showEditorMenu() {
        // Share the space among webview and editor menu
        webViewHolder.layoutParams = halfLayoutParams
        editorMenu.visibility = View.VISIBLE
        editor.updateStyle()
    }
}

fun ImageView.actionImageViewStyle() {
    layoutParams.width = dip(40)
    layoutParams.height = dip(40)
}