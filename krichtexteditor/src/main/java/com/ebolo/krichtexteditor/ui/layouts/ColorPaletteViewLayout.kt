package com.ebolo.krichtexteditor.ui.layouts

import android.view.Gravity.CENTER
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.ui.widgets.ColorPaletteView
import org.jetbrains.anko.*

class ColorPaletteViewLayout: AnkoComponent<ColorPaletteView> {
    override fun createView(ui: AnkoContext<ColorPaletteView>) = with(ui) {
        horizontalScrollView {
            // layoutParams.width = matchParent
            // layoutParams.height = matchParent

            linearLayout {
                id = R.id.ll_color_container
                gravity = CENTER
                padding = dip(16)
            }.lparams(width = wrapContent, height = wrapContent)
        }
    }
}