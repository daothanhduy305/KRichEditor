package com.ebolo.krichtexteditor.utils

import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H1
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H2
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H3
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H4
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H5
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.H6
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_CENTER
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_FULL
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_LEFT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.JUSTIFY_RIGHT
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.NONE
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.NORMAL
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.ORDERED
import com.ebolo.krichtexteditor.ui.widgets.ActionImageView.Companion.UNORDERED
import com.google.gson.annotations.SerializedName

/**
 * Font Style
 * Created by even.wu on 9/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class FontStyle {

    @SerializedName("font-family") var fontFamily: String? = null
    @SerializedName("font-size") var fontSize: Int = 0
    @SerializedName("font-backColor") var fontBackColor: String? = null
    @SerializedName("font-foreColor") var fontForeColor: String? = null
    @SerializedName("text-align") var textAlign: String? = null
    @SerializedName("list-style-type") var listStyleType: String? = null
    @SerializedName("line-height") var lineHeight: String? = null
    @SerializedName("font-bold") var fontBold: String? = null
    @SerializedName("font-italic") var fontItalic: String? = null
    @SerializedName("font-underline") var fontUnderline: String? = null
    @SerializedName("font-subscript") var fontSubscript: String? = null
    @SerializedName("font-superscript") var fontSuperscript: String? = null
    @SerializedName("font-strikethrough") var fontStrikethrough: String? = null
    @SerializedName("font-block") var fontBlock: String? = null
    @SerializedName("list-style") var listStyle: String? = null

    fun getTextAlign() = when (textAlign.isNullOrBlank()) {
        true -> null
        else -> {
            when (textAlign) {
                "left" -> JUSTIFY_LEFT
                "center" -> JUSTIFY_CENTER
                "right" -> JUSTIFY_RIGHT
                else -> JUSTIFY_FULL
            }
        }
    }

    fun getLineHeight() = try {
        if (lineHeight.isNullOrBlank()) 0.0
        else lineHeight!!.toDouble()
    } catch (e: Exception) { 0.0 }


    fun getFontBlock() = when {
        fontBlock.isNullOrBlank() -> NONE
        else -> when (fontBlock) {
            "h1" -> H1
            "h2" -> H2
            "h3" -> H3
            "h4" -> H4
            "h5" -> H5
            "h6" -> H6
            else -> NORMAL
        }
    }

    fun isBold() = "bold" == fontBold
    fun isItalic() = "italic" == fontItalic
    fun isUnderline() = "underline" == fontUnderline
    fun isSubscript() = "subscript" == fontSubscript
    fun isSuperscript() = "superscript" == fontSuperscript
    fun isStrikethrough() = "strikethrough" == fontStrikethrough

    fun getListStyle() = when {
        listStyle.isNullOrBlank() -> null
        else -> when (listStyle) {
            "ordered" -> ORDERED
            "unordered" -> UNORDERED
            else -> NONE
        }
    }
}