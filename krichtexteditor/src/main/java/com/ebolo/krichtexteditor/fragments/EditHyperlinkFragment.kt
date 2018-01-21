package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.ui.layouts.EditHyperlinkFragmentLayout
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.wrapContent

/**
 * Edit Hyperlink Activity
 * Created by even.wu on 10/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class EditHyperlinkFragment: DialogFragment() {
    private var callback: ((address: String) -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val layout = EditHyperlinkFragmentLayout().apply {
            this.callback = this@EditHyperlinkFragment.callback
        }
        return layout.createView(AnkoContext.Companion.create(ctx, this))
    }

    override fun onStart() {
        super.onStart()
        dialog.window.setLayout(matchParent, wrapContent)
    }

    fun onLinkSet(callback: ((address: String) -> Unit)?) {
        this.callback = callback
    }
}

fun editHyperlinkDialog(setup: EditHyperlinkFragment.() -> Unit): EditHyperlinkFragment {
    val dialog = EditHyperlinkFragment()
    setup.invoke(dialog)
    return dialog
}