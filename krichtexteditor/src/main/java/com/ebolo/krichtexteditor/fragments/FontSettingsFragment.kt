package com.ebolo.krichtexteditor.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ebolo.krichtexteditor.ui.layouts.FontSettingsFragmentLayout
import org.jetbrains.anko.AnkoContext

/**
 * Font Setting Fragment
 * Created by even.wu on 9/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class FontSettingsFragment: DialogFragment() {
    private var type: Int = 2
    private var onResultCallback: ((String) -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = FontSettingsFragmentLayout(type, onResultCallback)
            .createView(AnkoContext.Companion.create(context, this))

    fun onItemSet(callback: (String) -> Unit) {
        this.onResultCallback = callback
    }

    companion object {
        val TYPE_SIZE = 0
        val TYPE_LINE_HEIGHT = 1
        val TYPE_FONT_FAMILY = 2

        fun createInstance(type: Int, setup: FontSettingsFragment.() -> Unit): FontSettingsFragment {
            val fragment = FontSettingsFragment()
            fragment.type = type
            setup.invoke(fragment)
            return fragment
        }
    }
}