package com.ebolo.kricheditor

import android.os.Build
import android.util.TypedValue
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.themedToolbar

class MainActivityLayout: AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        verticalLayout {
            /*
            Set up toolbar
             */
            val toolbar = themedToolbar(R.style.AppTheme_AppBarOverlay) {
                backgroundColorResource = R.color.colorPrimary
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) popupTheme = R.style.AppTheme_PopupOverlay
            }.lparams(width = matchParent) {
                val tv = TypedValue()
                if (ui.owner.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
                    height = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                }
            }
            ui.owner.setSupportActionBar(toolbar)
            /*
            Set up Fragment holder view
             */
            frameLayout {

                relativeLayout {
                    id = R.id.fragment_holder
                }.lparams(width = matchParent, height = matchParent)

            }.lparams(width = matchParent, height = matchParent)
        }
    }
}