package com.ebolo.krichtexteditor.utils

import com.google.gson.annotations.SerializedName

class QuillFormat {
    @SerializedName("bold") var isBold: Boolean? = null
    @SerializedName("italic") var isItalic: Boolean? = null
    @SerializedName("underline") var isUnderline: Boolean? = null
    @SerializedName("strike") var isStrike: Boolean? = null
    @SerializedName("header") var header: Int? = null
    @SerializedName("script") var script: String? = null
    @SerializedName("align") var align: String? = null
    @SerializedName("list") var list: String? = null
    @SerializedName("code") var isCode: Boolean? = null
    @SerializedName("size") var size: String = "normal"
    @SerializedName("link") var link: String? = null
}