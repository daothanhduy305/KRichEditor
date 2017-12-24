package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.ui.layouts.EditTableFragmentLayout
import org.jetbrains.anko.AnkoContext

/**
 * Edit Table Fragment
 * Created by even.wu on 10/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class EditTableFragment: DialogFragment() {
    private var callback: ((row: Int, column: Int) -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val layout = EditTableFragmentLayout()
        layout.callback = this.callback
        return layout.createView(AnkoContext.Companion.create(context, this))
    }

    fun onTableSet(callback: (row: Int, column: Int) -> Unit) {
        this.callback = callback
    }
}

fun editTableDialog(setup: EditTableFragment.() -> Unit): EditTableFragment {
    val dialog = EditTableFragment()
    setup.invoke(dialog)
    return dialog
}