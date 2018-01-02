package com.ebolo.krichtexteditor.ui.layouts

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.Gravity.CENTER_VERTICAL
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.RichEditor
import com.ebolo.krichtexteditor.fragments.EditHyperlinkFragment
import com.ebolo.krichtexteditor.fragments.KRichEditorFragment
import com.ebolo.krichtexteditor.fragments.editHyperlinkDialog
import com.ebolo.krichtexteditor.ui.actionImageViewStyle
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
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNDERLINE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNDO
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNORDERED
import com.ebolo.krichtexteditor.ui.widgets.ColorPaletteView
import com.ebolo.krichtexteditor.ui.widgets.TextEditorWebView
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.toast
import ru.whalemare.sheetmenu.SheetMenu
import java.util.regex.Pattern


class KRichEditorFragmentLayout : AnkoComponent<KRichEditorFragment> {
    lateinit var editorFragment: KRichEditorFragment
    lateinit var editor: RichEditor
    var imageCallback: (() -> String)? = null
    private val formatButtonIds = listOf(
            BOLD, ITALIC, UNDERLINE, SUBSCRIPT, SUPERSCRIPT,
            STRIKETHROUGH, JUSTIFY_LEFT, JUSTIFY_CENTER,
            JUSTIFY_RIGHT, JUSTIFY_FULL, ORDERED,
            UNORDERED, NORMAL, H1, H2, H3, H4, H5, H6,
            INDENT, OUTDENT, BLOCK_QUOTE, BLOCK_CODE, /*LINE,*/ CODE_VIEW
    )
    private lateinit var barFormatButtons: Map<Int, ImageView>
    private val menuFormatButtons = mutableMapOf<Int, ImageView>()
    private val menuFormatHeadingBlocks = mutableMapOf<Int, View>()

    private lateinit var webView: WebView
    private lateinit var fontFamilyTextView: TextView
    private lateinit var fontSizeTextView: TextView
    private lateinit var lineHeightTextView: TextView
    private lateinit var textColorPalette: ColorPaletteView
    private lateinit var highlightColorPalette: ColorPaletteView
    private lateinit var editorMenu: LinearLayout
    private lateinit var webViewHolder: LinearLayout
    private lateinit var editorToolbar: LinearLayout
    private lateinit var rootView: LinearLayout

    private lateinit var menuButton: ImageView

    private val fullLayoutParams = LinearLayout.LayoutParams(matchParent, 0, 2f)
    private val halfLayoutParams = LinearLayout.LayoutParams(matchParent, 0, 1f)

    // Customizable settings
    var buttonActivatedColorId: Int = R.color.colorAccent
    var buttonDeactivatedColorId: Int = R.color.tintColor
    var placeHolder = "Start writing..."

    @SuppressLint("SetJavaScriptEnabled")
    override fun createView(ui: AnkoContext<KRichEditorFragment>) = with(ui) {
        this@KRichEditorFragmentLayout.editorFragment = ui.owner
        this@KRichEditorFragmentLayout.editor = editorFragment.editor

        rootView = verticalLayout {
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
            weightSum = 2f

            webViewHolder = verticalLayout {

                webView = ankoView(::TextEditorWebView, 0) {
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE

                    isFocusable = true
                    isFocusableInTouchMode = true

                    editor.apply {
                        mWebView = this@ankoView
                        styleUpdatedCallback = { type, value -> updateActionStates(type, value) }
                        placeHolder = this@KRichEditorFragmentLayout.placeHolder
                    }
                    addJavascriptInterface(editor, "KRichEditor")
                    loadUrl("file:///android_asset/richEditor.html")
                }.lparams(width = matchParent, height = matchParent)

            }.lparams(width = matchParent, height = 0) { weight = 2f }

            // Outer toolbar holder
            editorToolbar = linearLayout {
                backgroundColorResource = R.color.editor_toolbar_bg_color
                gravity = CENTER_VERTICAL

                menuButton = imageView(R.drawable.ic_action) {
                    id = R.id.iv_action
                    padding = dip(10)

                    onClick { toggleMenuEditor() }
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

                        /*imageView(R.drawable.ic_table) {
                            id = R.id.iv_action_table
                            padding = dip(11)
                            backgroundResource = R.drawable.btn_colored_material

                            onClick { onActionPerform(TABLE) }
                        }.apply { actionImageViewStyle() }*/

                        // Add format buttons
                        barFormatButtons = formatButtonIds.map { type ->
                            type to imageView(ActionImageView.actionButtonDrawables[type]!!) {
                                padding = dip(9)
                                backgroundResource = R.drawable.btn_colored_material

                                onClick { editor.command(type) }
                            }.apply { actionImageViewStyle() }
                        }.toMap()

                    }.lparams(width = wrapContent, height = dip(40))

                }.lparams(width = matchParent, height = dip(40))

            }.lparams(width = matchParent, height = wrapContent)

            // Editor menu
            editorMenu = verticalLayout {
                visibility = View.GONE

                scrollView {
                    verticalLayout {
                        backgroundColorResource = R.color.gray_100

                        // First box: font size, alignment, basic text format
                        linearLayout {
                            backgroundColorResource = R.color.white
                            padding = dip(16)
                            weightSum = 10f

                            // Font size box
                            verticalLayout {
                                id = R.id.ll_font_size
                                gravity = Gravity.CENTER
                                backgroundResource = R.drawable.btn_white_round_rectangle

                                textView(R.string.font_size) {
                                    textSize = 10f
                                    gravity = Gravity.CENTER
                                }

                                fontSizeTextView = textView("normal") {
                                    textSize = 18f
                                    textColorResource = R.color.light_blue_500
                                    gravity = Gravity.CENTER

                                    onClick {
                                        //val menu = PopupMenu(ui.ctx, this@textView)
                                        SheetMenu().apply {
                                            titleId = R.string.font_sizes_title
                                            menu = R.menu.font_sizes_menu
                                            showIcons = false // true, by default

                                            click = MenuItem.OnMenuItemClickListener {
                                                onActionPerform(SIZE,  when (it.itemId) {
                                                    R.id.font_size_small -> "small"
                                                    R.id.font_size_large -> "large"
                                                    R.id.font_size_huge -> "huge"
                                                    else -> ""
                                                } )
                                                true
                                            }
                                        }.show(ui.ctx)
                                    }
                                }.lparams { topMargin = dip(8) }

                            }.lparams(width = dip(0), height = dip(100)) { weight = 3f }

                            verticalLayout {
                                gravity = Gravity.CENTER

                                // Justify(alignment) buttons
                                linearLayout {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = Gravity.CENTER
                                    setPadding(16, dip(6), 16, dip(6))

                                    fun justifyButton(
                                            @ActionImageView.Companion.ActionType type: Int,
                                            drawable: Int,
                                            neighbor: Boolean = false) = menuFormatButtons.put(type, imageView(drawable) {
                                        padding = dip(8)
                                        backgroundResource = R.drawable.btn_white_material

                                        onClick { onActionPerform(type) }
                                    }.lparams {
                                        if (neighbor) marginStart = dip(16)
                                    }.apply { actionImageViewStyle() })

                                    justifyButton(JUSTIFY_LEFT, R.drawable.ic_format_align_left)
                                    justifyButton(JUSTIFY_CENTER, R.drawable.ic_format_align_center, true)
                                    justifyButton(JUSTIFY_RIGHT, R.drawable.ic_format_align_right, true)
                                    justifyButton(JUSTIFY_FULL, R.drawable.ic_format_align_justify, true)

                                }.lparams(width = matchParent, height = dip(46))

                                // Basic formats: bold, italic, underline, strike
                                linearLayout {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = Gravity.CENTER
                                    setPadding(dip(16), dip(6), dip(16), dip(6))

                                    fun formatButton(
                                            @ActionImageView.Companion.ActionType type: Int,
                                            drawable: Int
                                    ) = menuFormatButtons.put(type, imageView(drawable) {
                                        padding = dip(8)
                                        backgroundResource = R.drawable.btn_white_material

                                        onClick { onActionPerform(type) }
                                    }
                                            .lparams { weight = 1f }
                                            .apply { actionImageViewStyle() })

                                    formatButton(BOLD, R.drawable.ic_format_bold)
                                    formatButton(ITALIC, R.drawable.ic_format_italic)
                                    formatButton(UNDERLINE, R.drawable.ic_format_underlined)
                                    formatButton(STRIKETHROUGH, R.drawable.ic_format_strikethrough)

                                }.lparams(width = matchParent, height = dip(46)) { topMargin = dip(8) }

                            }.lparams(width = dip(0), height = dip(100)) {
                                marginStart = dip(8)
                                weight = 7f
                            }

                        }.lparams(width = matchParent, height = wrapContent)

                        // Second box: text color and highlight
                        verticalLayout {
                            backgroundColorResource = R.color.white
                            padding = dip(16)

                            textView(R.string.font_color) {
                                textSize = 10f
                            }.lparams(width = matchParent)

                            linearLayout {
                                gravity = Gravity.CENTER
                                backgroundResource = R.drawable.round_rectangle_white

                                textColorPalette = ankoView(::ColorPaletteView, 0){
                                    onColorChange { onActionPerform(FORE_COLOR, this.selectedColor) }
                                }.lparams(width = matchParent, height = wrapContent)

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

                                    onColorChange { onActionPerform(BACK_COLOR, this.selectedColor) }
                                }.lparams(width = wrapContent, height = wrapContent) { weight = 1f }

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        // Third box: headings
                        horizontalScrollView {
                            id = R.id.hsv_action_bar

                            linearLayout {
                                padding = dip(16)
                                backgroundColorResource = R.color.white

                                fun headingBlock(
                                        @ActionImageView.Companion.ActionType type: Int,
                                        previewText: Pair<String, Float>,
                                        text: Int,
                                        neighbor: Boolean = false
                                ) = menuFormatHeadingBlocks.put(type, verticalLayout {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = Gravity.CENTER
                                    setPadding(0, 0, 0, dip(8))

                                    onClick { onActionPerform(type) }

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

                                    textView(text) {
                                        textSize = 10f
                                        gravity = Gravity.CENTER
                                    }

                                }.lparams(width = dip(80), height = matchParent) {
                                    if (neighbor) marginStart = dip(8)
                                })

                                headingBlock(
                                        NORMAL, previewText = "AaBbCcDd" to 10f,
                                        text = R.string.font_style_normal
                                )

                                headingBlock(
                                        H1, previewText = "AaBb" to 18f,
                                        text = R.string.font_style_heading_1, neighbor = true
                                )

                                headingBlock(
                                        H2, previewText = "AaBbC" to 14f,
                                        text = R.string.font_style_heading_2, neighbor = true
                                )

                                headingBlock(
                                        H3, previewText = "AaBbCcD" to 12f,
                                        text = R.string.font_style_heading_3, neighbor = true
                                )

                                headingBlock(
                                        H4, previewText = "AaBbCcDd" to 12f,
                                        text = R.string.heading_4, neighbor = true
                                )

                                headingBlock(
                                        H5, previewText = "AaBbCcDd" to 12f,
                                        text = R.string.heading_5, neighbor = true
                                )

                                headingBlock(
                                        H6, previewText = "AaBbCcDd" to 12f,
                                        text = R.string.heading_6, neighbor = true
                                )

                            }.lparams { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent)

                        /**
                         * Inner function:  additionalFormatBox
                         * Description:     Create a box with 4 buttons divided into two
                         *                  smaller ones.
                         * Param pattern:   a pair mapping ActionType Int to Drawable Res Id
                         * @param item1 first button
                         * @param item2 second button
                         * @param item3 third button
                         * @param item4 fourth button
                         */
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

                                fun formatButton(item: Pair<Int, Int>, isSecond: Boolean = false) =
                                        menuFormatButtons.put(
                                                item.first,
                                                imageView(item.second) {
                                                    backgroundResource = R.drawable.btn_white_material
                                                    padding = dip(10)

                                                    onClick { onActionPerform(item.first) }
                                                }
                                                        .lparams { if (isSecond) marginStart = dip(32) }
                                                        .apply { actionImageViewStyle() }
                                        )

                                formatButton(item1)
                                formatButton(item2, true)
                            }.lparams(width = wrapContent, height = wrapContent) {
                                weight = 1f
                                if (isSecond) marginStart = dip(8)
                            }

                            innerBox(item1, item2)
                            innerBox(item3, item4, true)

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        additionalFormatBox(
                                item1 = INDENT to R.drawable.ic_format_indent_increase,
                                item2 = OUTDENT to R.drawable.ic_format_indent_decrease,
                                item3 = UNORDERED to R.drawable.ic_format_list_bulleted,
                                item4 = ORDERED to R.drawable.ic_format_list_numbered
                        )

                        additionalFormatBox(
                                item1 = SUBSCRIPT to R.drawable.ic_format_subscript,
                                item2 = SUPERSCRIPT to R.drawable.ic_format_superscript,
                                item3 = BLOCK_QUOTE to R.drawable.ic_format_quote,
                                item4 = BLOCK_CODE to R.drawable.ic_code_block
                        )

                        // Sixth box: insert buttons - image, link, table, code
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
                                // insertButton(R.drawable.ic_table, R.id.iv_action_table)
                                insertButton(R.drawable.ic_code_review, R.id.iv_action_code_view)

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                    }.lparams(width = matchParent, height = wrapContent)

                }.lparams(width = matchParent, height = wrapContent)

            }.lparams(width = matchParent, height = 0) { weight = 1f }
        }

        rootView
    }

    /**
     * Function:    onActionPerform
     * Description: Declare sets of actions of formatting buttons
     * @param type type of action defined in ActionImageView class
     * @param param param of action if necessary
     * @see ActionImageView
     */
    private fun onActionPerform(@ActionImageView.Companion.ActionType type: Int, param: String? = null) {
        when (type) {
            UNDO -> editor.undo()
            REDO -> editor.redo()
            SIZE -> editor.fontSize(param!!)
            LINE_HEIGHT -> editor.lineHeight(param!!.toDouble())
            FORE_COLOR -> editor.foreColor(param!!)
            BACK_COLOR -> editor.backColor(param!!)
            FAMILY -> editor.fontName(param!!)
            IMAGE -> { when (imageCallback) {
                null -> editorFragment.toast("Image handler not implemented!")
                else -> editor.getSelection( ValueCallback {
                    try {
                        val selection = Gson().fromJson<Map<String, Int>>(it)
                        editor.insertImage(selection["index"]!!, imageCallback!!.invoke())
                    } catch (e: Exception) { editorFragment.toast("Something went wrong!") }
                } )
            } }
            LINK -> {
                editor.getSelection( ValueCallback {
                    val selection = Gson().fromJson<Map<String, Int>>(it)
                    if (selection["length"]!! > 0) {
                        if (!editor.selectingLink())
                            editHyperlinkDialog {
                                onLinkSet { editor.createLink(it) }
                            }.show(
                                    editorFragment.fragmentManager,
                                    EditHyperlinkFragment::class.java.simpleName
                            )
                        else editor.createLink("")
                    } else longSnackbar(rootView, R.string.link_empty_warning).show()
                } )
            }
            /*TABLE -> {
                val dialog = editTableDialog {
                    onTableSet { row, column -> editor.insertTable(column, row) }
                }
                dialog.show(editorFragment.fragmentManager, EditHyperlinkFragment::class.java.simpleName)
            }*/
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
        // if (editorMenu.visibility == View.VISIBLE)
        when (type) {
            FAMILY -> fontFamilyTextView.text = value
            SIZE -> fontSizeTextView.text = value
            FORE_COLOR, BACK_COLOR -> {
                val selectedColor = rgbToHex(value)
                if (selectedColor != null) {
                    if (type == FORE_COLOR) textColorPalette.selectedColor = selectedColor
                    else highlightColorPalette.selectedColor = selectedColor
                }
            }
            LINE_HEIGHT -> lineHeightTextView.text = value
            in formatButtonIds -> when (type) {
                NORMAL, H1, H2, H3, H4, H5, H6 -> {
                    menuFormatHeadingBlocks[type]?.backgroundResource = when {
                        value.toBoolean() -> R.drawable.round_rectangle_blue
                        else -> R.drawable.round_rectangle_white
                    }
                }
                else -> {
                    menuFormatButtons[type]?.setColorFilter(ContextCompat.getColor(
                            editorFragment.context,
                            when {
                                value.toBoolean() -> buttonActivatedColorId
                                else -> buttonDeactivatedColorId
                            }
                    ))
                }
            }
        }

    }

    private fun updateActionStateToolbar(@ActionImageView.Companion.ActionType type: Int, value: String) {
        if (type in formatButtonIds) {
            // Log.d("Toolbar", "Type = $type, value = $value")
            barFormatButtons[type]?.setColorFilter(ContextCompat.getColor(editorFragment.context,
                    when {
                        value.toBoolean() -> buttonActivatedColorId
                        else -> buttonDeactivatedColorId
                    }
            ))
        }
    }

    private fun rgbToHex(rgb: String): String? {
        val c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)")
        val m = c.matcher(rgb)
        return if (m.matches()) {
            String.format("#%02x%02x%02x", Integer.valueOf(m.group(1)),
                    Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)))
        } else null
    }

    private fun hideKeyboard() = with(
            editorFragment.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
    ) {
        hideSoftInputFromWindow(editorFragment.activity.currentFocus.windowToken, 0)
    }

    fun hideEditorMenu() {
        editor.enable()
        menuButton.setColorFilter(ContextCompat.getColor(editorFragment.context, buttonDeactivatedColorId))
        webViewHolder.layoutParams = fullLayoutParams
        editorMenu.visibility = View.GONE
    }

    private fun showEditorMenu() {
        hideKeyboard()
        editor.disable()
        menuButton.setColorFilter(ContextCompat.getColor(editorFragment.context, buttonActivatedColorId))

        webViewHolder.layoutParams = halfLayoutParams
        editorMenu.visibility = View.VISIBLE
        editor.updateStyle()
    }

    private fun toggleMenuEditor() {
        when (editorMenu.visibility) {
            View.VISIBLE -> hideEditorMenu()
            else -> showEditorMenu()
        }
    }
}