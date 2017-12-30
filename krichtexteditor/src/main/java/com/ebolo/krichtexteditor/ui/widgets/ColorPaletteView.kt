package com.ebolo.krichtexteditor.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.padding

/**
 * Color PaletteView
 * Created by even.wu on 10/8/17.
 * Ported by ebolo(daothanhduy305) on 20/12/2017
 */

class ColorPaletteView(context: Context): HorizontalScrollView(context) {
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

    private lateinit var colorViews: Map<String, RoundView>

    var selectedColor: String = ""
        set(value) {
            require(!value.isBlank()) { return }
            field = value.toUpperCase()

            colorViews.forEach { it.value.isSelected = it.key == field }
        }

    private var mOnColorChangeListener: ((String) -> Unit)? = null

    init {
        linearLayout {
            gravity = Gravity.CENTER
            padding = dip(16)

            val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f,
                    resources.displayMetrics).toInt()
            val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                    resources.displayMetrics).toInt()
            colorViews = mColorList.map { color ->
                val roundView = RoundView(context)
                val params = LinearLayout.LayoutParams(width, width)
                params.setMargins(margin, 0, margin, 0)
                roundView.layoutParams = params
                roundView.tag = color
                roundView.setBgColor(Color.parseColor(color))
                roundView.setOnClickListener {
                    selectedColor = color
                    mOnColorChangeListener?.invoke(roundView.getBgColor())
                }
                addView(roundView)
                color to roundView
            }.toMap()
        }
    }

    fun onColorChange(callback: (String) -> Unit) {
        this.mOnColorChangeListener = callback
    }
}