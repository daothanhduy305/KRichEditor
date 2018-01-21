package com.ebolo.krichtexteditor.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import org.jetbrains.anko.backgroundColor

/**
 * Round View
 * Created by even.wu on 8/8/17.
 * Ported by ebolo(daothanhduy305) on 20/12/2017
 */

class RoundView(context: Context): View(context) {
    private var mPaint: Paint
    private var bgColor = 0xFFFFFF

    init {
        backgroundColor = Color.WHITE

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = canvas.width
        val height = canvas.height
        mPaint.style = Paint.Style.FILL
        mPaint.color = bgColor
        canvas.drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                (width / 2).toFloat(),
                mPaint
        )

        if (isSelected) {
            mPaint.color = Color.WHITE
            canvas.drawLine(
                    (5 * width / 16).toFloat(),
                    (height / 2).toFloat(),
                    (7 * width / 16).toFloat(),
                    (5 * height / 8).toFloat(),
                    mPaint
            )
            canvas.drawLine(
                    (7 * width / 16).toFloat(),
                    (5 * height / 8).toFloat(),
                    (11 * width / 16).toFloat(),
                    (3 * height / 8).toFloat(),
                    mPaint
            )
        }
    }

    fun getBgColor(): String {
        return String.format("#%06X", 0xFFFFFF and bgColor)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        invalidate()
    }

    fun setBgColor(backgroundColor: Int) {
        this.bgColor = backgroundColor
        mPaint.color = backgroundColor
        invalidate()
    }
}