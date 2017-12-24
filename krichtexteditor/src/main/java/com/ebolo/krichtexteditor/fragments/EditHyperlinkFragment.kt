package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.ui.layouts.EditHyperlinkFragmentLayout
import org.jetbrains.anko.AnkoContext

/**
 * Edit Hyperlink Activity
 * Created by even.wu on 10/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class EditHyperlinkFragment: DialogFragment() {
    private var callback: ((address: String, text: String) -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val layout = EditHyperlinkFragmentLayout()
        layout.callback = this.callback
        return layout.createView(AnkoContext.Companion.create(context, this))
    }

    fun onLinkSet(callback: ((address: String, text: String) -> Unit)?) {
        this.callback = callback
    }
}

fun editHyperlinkDialog(setup: EditHyperlinkFragment.() -> Unit): EditHyperlinkFragment {
    val dialog = EditHyperlinkFragment()
    setup.invoke(dialog)
    return dialog
}