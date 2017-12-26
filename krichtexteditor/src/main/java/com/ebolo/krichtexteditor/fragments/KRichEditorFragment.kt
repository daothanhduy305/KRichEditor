package com.ebolo.krichtexteditor.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.ui.layouts.KRichEditorFragmentLayout
import org.jetbrains.anko.AnkoContext

class KRichEditorFragment: Fragment() {
    private lateinit var layout: KRichEditorFragmentLayout

    var formatButtonActivatedColor: Int = R.color.colorAccent
    var formatButtonDeactivatedColor: Int = R.color.tintColor

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        layout = KRichEditorFragmentLayout(this)
        return layout.createView(AnkoContext.Companion.create(context, this))
    }

    override fun onResume() {
        super.onResume()
        layout.hideEditorMenu()
    }
}

fun Context.kRichEditorFragment(
        setup: ((KRichEditorFragment).() -> Unit)? = null
): KRichEditorFragment = with (KRichEditorFragment()) {
    setup?.invoke(this)
    return this
}