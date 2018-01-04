package com.ebolo.krichtexteditor.ui.layouts

import android.view.Gravity.CENTER_VERTICAL
import android.view.ViewGroup
import com.ebolo.krichtexteditor.R
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.linearLayoutCompat

class FontSettingItemLayout: AnkoComponent<ViewGroup> {
    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayoutCompat {
            layoutParams.width = matchParent
            layoutParams.height = wrapContent
            padding = dip(16)

            textView("10.5") {
                gravity = CENTER_VERTICAL
            }

            imageView(R.drawable.ic_insert_photo) {
            }.lparams(width = dip(24), height = dip(24)) { marginStart = dip(8) }
        }
    }
}