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
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bitbucket.eventbus.EventBus
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.fragments.EditHyperlinkFragment
import com.ebolo.krichtexteditor.fragments.KRichEditorFragment
import com.ebolo.krichtexteditor.fragments.editHyperlinkDialog
import com.ebolo.krichtexteditor.ui.actionImageViewStyle
import com.ebolo.krichtexteditor.ui.widgets.ColorPaletteView
import com.ebolo.krichtexteditor.ui.widgets.EditorButton
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.BACK_COLOR
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.BLOCK_CODE
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.BLOCK_QUOTE
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.BOLD
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.CODE_VIEW
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.FORE_COLOR
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H1
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H2
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H3
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H4
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H5
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.H6
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.IMAGE
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.INDENT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.ITALIC
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_CENTER
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_FULL
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_LEFT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.JUSTIFY_RIGHT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.LINK
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.NORMAL
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.ORDERED
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.OUTDENT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.SIZE
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.STRIKETHROUGH
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.SUBSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.SUPERSCRIPT
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.UNDERLINE
import com.ebolo.krichtexteditor.ui.widgets.EditorButton.Companion.UNORDERED
import com.ebolo.krichtexteditor.ui.widgets.EditorToolbar
import com.ebolo.krichtexteditor.ui.widgets.TextEditorWebView
import com.ebolo.krichtexteditor.utils.rgbToHex
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.onUiThread
import org.jetbrains.anko.support.v4.toast
import ru.whalemare.sheetmenu.SheetMenu


class KRichEditorFragmentLayout : AnkoComponent<KRichEditorFragment> {
    private val eventBus by lazy { EventBus.getInstance() }
    private val menuFormatButtons = mutableMapOf<Int, ImageView>()
    private val menuFormatHeadingBlocks = mutableMapOf<Int, View>()

    private lateinit var webView: WebView
    private lateinit var fontSizeTextView: TextView
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
    var placeHolder = "Start writing..."
    var imageButtonAction: (() -> Unit)? = null

    // Default buttons layout
    var buttonsLayout = listOf(
            EditorButton.UNDO,
            EditorButton.REDO,
            EditorButton.IMAGE,
            EditorButton.LINK,
            EditorButton.BOLD,
            EditorButton.ITALIC,
            EditorButton.UNDERLINE,
            EditorButton.SUBSCRIPT,
            EditorButton.SUPERSCRIPT,
            EditorButton.STRIKETHROUGH,
            EditorButton.JUSTIFY_LEFT,
            EditorButton.JUSTIFY_CENTER,
            EditorButton.JUSTIFY_RIGHT,
            EditorButton.JUSTIFY_FULL,
            EditorButton.ORDERED,
            EditorButton.UNORDERED,
            EditorButton.NORMAL,
            EditorButton.H1,
            EditorButton.H2,
            EditorButton.H3,
            EditorButton.H4,
            EditorButton.H5,
            EditorButton.H6,
            EditorButton.INDENT,
            EditorButton.OUTDENT,
            EditorButton.BLOCK_QUOTE,
            EditorButton.BLOCK_CODE,
            EditorButton.CODE_VIEW
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun createView(ui: AnkoContext<KRichEditorFragment>) = with(ui) {
        val editor = ui.owner.editor
        // Preparation
        fun hideMenu(){
            editor.enable()
            menuButton.setColorFilter(ContextCompat.getColor(ui.ctx, buttonDeactivatedColorId))
            webViewHolder.layoutParams = fullLayoutParams
            editorMenu.visibility = View.GONE
        }

        fun showMenu() {
            // Hide Keyboard
            with(ui.ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
                hideSoftInputFromWindow(ui.owner.act.currentFocus.windowToken, 0)
            }
            editor.disable()
            menuButton.setColorFilter(ContextCompat.getColor(ui.ctx, buttonActivatedColorId))

            webViewHolder.layoutParams = halfLayoutParams
            editorMenu.visibility = View.VISIBLE
            editor.updateStyle()
        }

        /**
         * Function:    onMenuButtonClicked
         * Description: Declare sets of actions of formatting buttonsLayout
         * @param type type of action defined in EditorButton class
         * @param param param of action if necessary
         * @see EditorButton
         */
        fun onMenuButtonClicked(@EditorButton.Companion.ActionType type: Int, param: String? = null) {
            when (type) {
                SIZE -> editor.command(SIZE, false, param!!)
                FORE_COLOR -> editor.command(FORE_COLOR, false, param!!)
                BACK_COLOR -> editor.command(BACK_COLOR, false, param!!)
                IMAGE -> when (imageButtonAction) {
                    null -> ui.owner.toast("Image handler not implemented!")
                    else -> imageButtonAction!!.invoke()
                }
                LINK -> {
                    editor.getSelection( ValueCallback {
                        val selection = Gson().fromJson<Map<String, Int>>(it)
                        if (selection["length"]!! > 0) {
                            if (!editor.selectingLink()) {
                                editHyperlinkDialog {
                                    onLinkSet {
                                        hideMenu()
                                        editor.command(LINK, true, it)
                                    }
                                }.show(
                                        ui.owner.fragmentManager,
                                        EditHyperlinkFragment::class.java.simpleName
                                )
                            }
                            else editor.command(LINK, editorMenu.visibility != View.VISIBLE ,"")
                        } else longSnackbar(rootView, R.string.link_empty_warning).show()
                    } )
                }
                else -> editor.command(type, false)
            }
        }

        // Start constructing views
        rootView = verticalLayout {
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
            weightSum = 2f

            webViewHolder = verticalLayout {

                webView = ankoView(::TextEditorWebView, 0) {
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    // settings.cacheMode = WebSettings.LOAD_NO_CACHE

                    loadUrl("file:///android_asset/richEditor.html")

                    isFocusable = true
                    isFocusableInTouchMode = true

                    editor.apply {
                        mWebView = this@ankoView
                        placeHolder = this@KRichEditorFragmentLayout.placeHolder


                    }
                    addJavascriptInterface(editor, "KRichEditor")
                }.lparams(width = matchParent, height = matchParent)

            }.lparams(width = matchParent, height = 0) { weight = 2f }

            // Outer toolbar holder
            editorToolbar = linearLayout {
                backgroundColorResource = R.color.editor_toolbar_bg_color
                gravity = CENTER_VERTICAL

                menuButton = imageView(R.drawable.ic_action) {
                    padding = dip(10)

                    onClick {
                        // Toggle editor menu
                        when (editorMenu.visibility) {
                            View.VISIBLE -> hideMenu()
                            else -> showMenu()
                        }
                    }
                }.apply { actionImageViewStyle() }

                // Separator
                view {
                    backgroundColor = 0x9e9e9e.opaque
                }.lparams(width = dip(0.5f), height = dip(24))

                EditorToolbar(editor, buttonsLayout).apply {
                    if (LINK in buttonsLayout)
                        linkButtonAction = { onMenuButtonClicked(LINK) }
                    if (IMAGE in buttonsLayout)
                        imageButtonAction = { onMenuButtonClicked(IMAGE) }
                }.createToolbar(this)

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
                                                onMenuButtonClicked(SIZE,  when (it.itemId) {
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

                                // Justify(alignment) buttonsLayout
                                linearLayout {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = Gravity.CENTER
                                    setPadding(16, dip(6), 16, dip(6))

                                    fun justifyButton(
                                            @EditorButton.Companion.ActionType type: Int,
                                            drawable: Int,
                                            neighbor: Boolean = false) = menuFormatButtons.put(type, imageView(drawable) {
                                        padding = dip(8)
                                        backgroundResource = R.drawable.btn_white_material

                                        onClick { onMenuButtonClicked(type) }
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
                                            @EditorButton.Companion.ActionType type: Int,
                                            drawable: Int
                                    ) = menuFormatButtons.put(type, imageView(drawable) {
                                        padding = dip(8)
                                        backgroundResource = R.drawable.btn_white_material

                                        onClick { onMenuButtonClicked(type) }
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
                                    onColorChange { onMenuButtonClicked(FORE_COLOR, this.selectedColor) }
                                }.lparams(width = matchParent, height = wrapContent)

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                            textView(R.string.font_highlight_color) {
                                textSize = 10f
                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(16) }

                            linearLayout {
                                gravity = Gravity.CENTER

                                highlightColorPalette = ankoView(::ColorPaletteView, 0) {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = android.view.Gravity.CENTER

                                    onColorChange { onMenuButtonClicked(BACK_COLOR, this.selectedColor) }
                                }.lparams(width = wrapContent, height = wrapContent) { weight = 1f }

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        // Third box: headings
                        horizontalScrollView {
                            linearLayout {
                                padding = dip(16)
                                backgroundColorResource = R.color.white

                                fun headingBlock(
                                        @EditorButton.Companion.ActionType type: Int,
                                        previewText: Pair<String, Float>,
                                        text: Int,
                                        neighbor: Boolean = false
                                ) = menuFormatHeadingBlocks.put(type, verticalLayout {
                                    backgroundResource = R.drawable.round_rectangle_white
                                    gravity = Gravity.CENTER
                                    setPadding(0, 0, 0, dip(8))

                                    onClick { onMenuButtonClicked(type) }

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
                         * Description:     Create a box with 4 buttonsLayout divided into two
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

                                                    onClick { onMenuButtonClicked(item.first) }
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

                        // Sixth box: insert buttonsLayout - image, link, table, code
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

                                fun insertButton(@EditorButton.Companion.ActionType type: Int, drawable: Int) =
                                        themedImageView(
                                                drawable,
                                                R.style.ActionImageView
                                        ) {
                                            this.id = id
                                            backgroundResource = R.drawable.btn_white_material
                                            padding = dip(8)

                                            onClick { onMenuButtonClicked(type) }
                                        }.lparams { weight = 1f }.apply { actionImageViewStyle() }

                                insertButton(IMAGE, R.drawable.ic_insert_photo)
                                insertButton(LINK, R.drawable.ic_insert_link)
                                // insertButton(R.drawable.ic_table, R.id.iv_action_table)
                                insertButton(CODE_VIEW, R.drawable.ic_code_review)

                            }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                        }.lparams(width = matchParent, height = wrapContent) { topMargin = dip(8) }

                    }.lparams(width = matchParent, height = wrapContent)

                }.lparams(width = matchParent, height = wrapContent)

            }.lparams(width = matchParent, height = 0) { weight = 1f }
        }

        // Setup ui handlers for editor menu
        eventBus.on("style_$SIZE") {
            ui.owner.onUiThread { fontSizeTextView.text = (it as String) }
        }

        eventBus.on("style_$FORE_COLOR") {
            val selectedColor = rgbToHex(it as String)
            if (selectedColor != null)
                ui.owner.onUiThread { textColorPalette.selectedColor = selectedColor }
        }

        eventBus.on("style_$BACK_COLOR") {
            val selectedColor = rgbToHex(it as String)
            if (selectedColor != null)
                ui.owner.onUiThread { highlightColorPalette.selectedColor = selectedColor }
        }

        listOf(NORMAL, H1, H2, H3, H4, H5, H6).forEach { style ->
            eventBus.on("style_$style") {
                val state = it as Boolean
                ui.owner.onUiThread {
                    menuFormatHeadingBlocks[style]?.backgroundResource = when {
                        state -> R.drawable.round_rectangle_blue
                        else -> R.drawable.round_rectangle_white
                    }
                }
            }
        }

        listOf(
                BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, JUSTIFY_CENTER, JUSTIFY_FULL, JUSTIFY_LEFT,
                JUSTIFY_RIGHT, SUBSCRIPT, SUPERSCRIPT, CODE_VIEW, BLOCK_CODE, BLOCK_QUOTE
        ).forEach { style ->
            eventBus.on("style_$style") {
                val state = it as Boolean
                ui.owner.onUiThread {
                    menuFormatButtons[style]?.setColorFilter(ContextCompat.getColor(
                            ui.ctx,
                            when {
                                state -> buttonActivatedColorId
                                else -> buttonDeactivatedColorId
                            }
                    ) )
                }
            }
        }

        rootView
    }

    companion object {
        var buttonActivatedColorId: Int = R.color.colorAccent
        var buttonDeactivatedColorId: Int = R.color.tintColor
    }
}