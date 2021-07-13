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
    ): View {
        layout.apply { settings?.invoke(this) }
        return layout.createView(AnkoContext.create(requireContext(), this))
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
                showToolbar = options.showToolbar
                readOnly = options.readOnly
                onInitialized = options.onInitialized
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

/**
 * Class serve as a container of options to be transmitted to the editor to set it up
 */
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
    var readOnly = false

    /**
     * Method to setup the placeholder text for the web view text area
     */
    fun placeHolder(text: String) = this.apply { placeHolder = text }

    /**
     * Define the call back to be executed when the image button is clicked
     */
    fun onImageButtonClicked(action: Runnable) = this.apply { imageButtonAction = { action.run() } }

    /**
     * Define the custom layout for the buttons toolbar
     */
    fun buttonLayout(layout: List<Int>) = this.apply { buttonsLayout = layout }

    /**
     * Define the color of the activated buttons in the toolbar
     */
    fun buttonActivatedColorResource(res: Int) = this.apply { buttonActivatedColorId = res }

    /**
     * Define the color of the de-ctivated buttons in the toolbar
     */
    fun buttonDeactivatedColorResource(res: Int) = this.apply { buttonDeactivatedColorId = res }

    /**
     * Define the callback to be executed on editor initialized
     */
    fun onInitialized(action: Runnable) = this.apply { onInitialized = { action.run() } }

    /**
     * Define whether the toolbar must be hidden or not
     */
    fun showToolbar(show: Boolean) = this.apply { showToolbar = show }

    /**
     * Define whether the text view is disabled or not (read only)
     */
    fun readOnly(disable: Boolean) = this.apply { readOnly = disable }

    companion object {
        @JvmField val DEFAULT = Options()
    }
}