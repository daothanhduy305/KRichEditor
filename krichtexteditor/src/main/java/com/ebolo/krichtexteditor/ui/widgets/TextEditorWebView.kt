package com.ebolo.krichtexteditor.ui.widgets

import android.content.Context
import android.webkit.WebView

class TextEditorWebView(context: Context): WebView(context) {
    override fun onCheckIsTextEditor() = true
}