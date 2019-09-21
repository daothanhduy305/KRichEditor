package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.RichEditor
import com.ebolo.krichtexteditor.ui.layouts.KRichEditorFragmentLayout
import com.ebolo.krichtexteditor.ui.widgets.EditorButton
import org.jetbrains.anko.AnkoContext

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
        return layout.createView(AnkoContext.create(context!!, this))
    }

    override fun onResume() {
        layout.setupListeners(this)
        super.onResume()
    }

    override fun onPause() {
        layout.removeListeners()
        super.onPause()
    }

    companion object {
        @JvmStatic fun getInstance(options: Options) = KRichEditorFragment().apply {
            this.layout.apply {
                placeHolder = options.placeHolder
                imageButtonAction = options.imageButtonAction
                buttonsLayout = options.buttonsLayout
                buttonActivatedColorId = options.buttonActivatedColorId
                buttonDeactivatedColorId = options.buttonDeactivatedColorId
            }
        }
    }
}

fun kRichEditorFragment(
        settings: ((KRichEditorFragmentLayout).() -> Unit)? = null
): KRichEditorFragment = KRichEditorFragment().apply {
    this.settings = settings
    this.retainInstance = true
}

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
    var buttonActivatedColorId: Int = R.color.colorAccent
    var buttonDeactivatedColorId: Int = R.color.tintColor
    var onInitialized: (() -> Unit)? = null
    var showToolbar = true

    fun placeHolder(text: String) = this.apply { placeHolder = text }

    fun onImageButtonClicked(action: Runnable) = this.apply { imageButtonAction = { action.run() } }

    fun buttonLayout(layout: List<Int>) = this.apply { buttonsLayout = layout }

    fun buttonActivatedColorResource(res: Int) = this.apply { buttonActivatedColorId = res }

    fun buttonDeactivatedColorResource(res: Int) = this.apply { buttonDeactivatedColorId = res }

    fun onInitialized(action: Runnable) = this.apply { onInitialized = { action.run() } }

    fun showToolbar(show: Boolean) = this.apply { showToolbar = show }

    companion object {
        @JvmField val DEFAULT = Options()
    }
}