package com.ebolo.krichtexteditor.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.ui.layouts.KRichEditorFragmentLayout
import org.jetbrains.anko.AnkoContext

class KRichEditorFragment: Fragment() {
    private lateinit var layout: KRichEditorFragmentLayout
    var settings: ((KRichEditorFragmentLayout).() -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        layout = KRichEditorFragmentLayout(this)
        settings?.invoke(layout)
        return layout.createView(AnkoContext.Companion.create(context, this))
    }

    override fun onResume() {
        super.onResume()
        layout.hideEditorMenu()
    }
}

fun Context.kRichEditorFragment(
        settings: ((KRichEditorFragmentLayout).() -> Unit)? = null
): KRichEditorFragment = KRichEditorFragment().apply { this.settings = settings }