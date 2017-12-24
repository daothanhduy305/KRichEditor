package com.ebolo.krichtexteditor.ui.layouts

import android.text.InputType
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.fragments.EditHyperlinkFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class EditHyperlinkFragmentLayout: AnkoComponent<EditHyperlinkFragment> {
    var callback: ((address: String, text: String) -> Unit)? = null

    override fun createView(ui: AnkoContext<EditHyperlinkFragment>) = with(ui) {
        verticalLayout {
            // Title bar
            relativeLayout {
                backgroundColor = R.color.colorPrimary

                textView(R.string.edit_hyperlink) {
                    textColor = R.color.white
                }.lparams {
                    centerInParent()
                }
            }.lparams(width = matchParent, height = dip(48))

            textView(R.string.address)

            val adressInput = editText {
                inputType = InputType.TYPE_CLASS_TEXT
                maxLines = 1
            }.lparams(width = matchParent, height = wrapContent)

            textView(R.string.text_to_display).lparams { topMargin = dip(32) }

            val textInput = editText {
                inputType = InputType.TYPE_CLASS_TEXT
                maxLines = 1
            }.lparams(width = matchParent, height = wrapContent)

            button(R.string.ok) {
                backgroundResource = R.drawable.btn_colored_primary
                textColor = R.color.white

                onClick { callback?.invoke(adressInput.text.toString(), textInput.text.toString()) }
            }.lparams(width = matchParent, height = wrapContent) {
                topMargin = dip(32)
            }
        }
    }
}