package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.RichEditor
import com.ebolo.krichtexteditor.ui.layouts.KRichEditorFragmentLayout
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.ctx

class KRichEditorFragment: Fragment() {
    private lateinit var layout: KRichEditorFragmentLayout
    val editor = RichEditor()
    var settings: ((KRichEditorFragmentLayout).() -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        layout = KRichEditorFragmentLayout().apply { settings?.invoke(this) }
        return layout.createView(AnkoContext.Companion.create(ctx, this))
    }
}

fun kRichEditorFragment(
        settings: ((KRichEditorFragmentLayout).() -> Unit)? = null
): KRichEditorFragment = KRichEditorFragment().apply { this.settings = settings }