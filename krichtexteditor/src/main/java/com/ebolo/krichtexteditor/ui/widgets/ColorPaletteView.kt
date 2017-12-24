package com.ebolo.krichtexteditor.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.ui.layouts.ColorPaletteViewLayout
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.find

/**
 * Color PaletteView
 * Created by even.wu on 10/8/17.
 * Ported by ebolo(daothanhduy305) on 20/12/2017
 */

class ColorPaletteView(context: Context): LinearLayout(context) {
    private lateinit var llColorContainer: _LinearLayout

    var selectedColor: String = ""
        set(value) {
            require(!value.isBlank()) { return }
            field = value.toUpperCase()

            val currentSelectedView = llColorContainer.findViewWithTag<View>(this.selectedColor) as RoundView
            currentSelectedView.isSelected = this.selectedColor.equals(selectedColor, ignoreCase = true)

            if (llColorContainer.findViewWithTag<View>(field) != null) {
                llColorContainer.findViewWithTag<View>(field).isSelected = true
            }
        }

    private lateinit var mOnColorChangeListener: OnColorChangeListener

    private val mColorList = listOf("#000000", "#424242", "#636363", "#9C9C94", "#CEC6CE", "#EFEFEF",
            "#F7F7F7", "#FFFFFF", "#FF0000", "#FF9C00", "#FFFF00", "#00FF00", "#00FFFF", "#0000FF",
            "#9C00FF", "#FF00FF", "#F7C6CE", "#FFE7CE", "#FFEFC6", "#D6EFD6", "#CEDEE7", "#CEE7F7",
            "#D6D6E7", "#E7D6DE", "#E79C9C", "#FFC69C", "#FFE79C", "#B5D6A5", "#A5C6CE", "#9CC6EF",
            "#B5A5D6", "#D6A5BD", "#E76363", "#F7AD6B", "#FFD663", "#94BD7B", "#73A5AD", "#6BADDE",
            "#8C7BC6", "#C67BA5", "#CE0000", "#E79439", "#EFC631", "#6BA54A", "#4A7B8C", "#3984C6",
            "#634AA5", "#A54A7B", "#9C0000", "#B56308", "#BD9400", "#397B21", "#104A5A", "#085294",
            "#311873", "#731842", "#630000", "#7B3900", "#846300", "#295218", "#083139", "#003163",
            "#21104A", "#4A1031"
    )

    init {
        val rootView = ColorPaletteViewLayout().createView(AnkoContext.Companion.create(context, this))
        llColorContainer = rootView.find(R.id.ll_color_container)

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f,
                resources.displayMetrics).toInt()
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                resources.displayMetrics).toInt()
        var i = 0
        val size = mColorList.size
        while (i < size) {
            val roundView = RoundView(context)
            val params = LinearLayout.LayoutParams(width, width)
            params.setMargins(margin, 0, margin, 0)
            roundView.layoutParams = params
            val color = mColorList[i]
            roundView.tag = color
            roundView.setBgColor(Color.parseColor(color))
            roundView.setOnClickListener {
                selectedColor = color
                mOnColorChangeListener.onColorChange(roundView.getBgColor())
            }
            llColorContainer.addView(roundView)
            i++
        }
    }

    fun onColorChange(mOnColorChangeListener: OnColorChangeListener) {
        this.mOnColorChangeListener = mOnColorChangeListener
    }

    interface OnColorChangeListener {
        fun onColorChange(color: String)
    }
}