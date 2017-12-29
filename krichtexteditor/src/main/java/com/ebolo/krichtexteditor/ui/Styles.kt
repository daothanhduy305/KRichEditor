package com.ebolo.krichtexteditor.ui

import android.widget.ImageView
import org.jetbrains.anko.dip

/**
 * This file declares various of styles using in DSL layouts
 * Created by daothanhduy305(ebolo) on 29-Dec-17.
 */

fun ImageView.actionImageViewStyle() {
    layoutParams.width = dip(40)
    layoutParams.height = dip(40)
}