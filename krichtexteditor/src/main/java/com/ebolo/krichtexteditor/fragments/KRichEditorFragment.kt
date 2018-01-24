package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.RichEditor
import com.ebolo.krichtexteditor.ui.layouts.KRichEditorFragmentLayout
import com.ebolo.krichtexteditor.ui.widgets.EditorButton
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.ctx

class KRichEditorFragment: Fragment() {
    private val layout by lazy { KRichEditorFragmentLayout() }
    val editor = RichEditor()
    var settings: ((KRichEditorFragmentLayout).() -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        layout.apply { settings?.invoke(this) }
        return layout.createView(AnkoContext.Companion.create(ctx, this))
    }

    companion object {
        @JvmStatic fun getInstance(options: Options) = KRichEditorFragment().apply {
            this.layout.apply {
                placeHolder = options.placeHolder
                imageButtonAction = options.imageButtonAction
                buttonsLayout = buttonsLayout
            }
        }
    }
}

fun kRichEditorFragment(
        settings: ((KRichEditorFragmentLayout).() -> Unit)? = null
): KRichEditorFragment = KRichEditorFragment().apply { this.settings = settings }

class Options {
    var placeHolder: String = "Start writing..."
    var imageButtonAction: (() -> Unit)? = null
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

    fun placeHolder(text: String) = this.apply { placeHolder = text }

    fun onImageButtonClicked(action: () -> Unit) = this.apply { imageButtonAction = action }

    fun buttonLayout(layout: List<Int>) = this.apply { buttonsLayout = layout }
}