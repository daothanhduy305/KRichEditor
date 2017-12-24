package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.ui.layouts.KRichEditorFragmentLayout
import org.jetbrains.anko.AnkoContext

class KRichEditorFragment: Fragment() {
    var formatButtonActivatedColor: Int = R.color.colorAccent
    var formatButtonDeactivatedColor: Int = R.color.tintColor

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = KRichEditorFragmentLayout(this)
            .createView(AnkoContext.Companion.create(context, this))
}