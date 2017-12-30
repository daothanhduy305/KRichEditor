package com.ebolo.krichtexteditor.ui.layouts

import android.support.design.widget.TextInputEditText
import android.text.InputType
import android.view.ViewGroup
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.fragments.EditHyperlinkFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.sdk25.coroutines.onClick

class EditHyperlinkFragmentLayout: AnkoComponent<EditHyperlinkFragment> {
    var callback: ((address: String) -> Unit)? = null

    private lateinit var addressInput: TextInputEditText

    override fun createView(ui: AnkoContext<EditHyperlinkFragment>) = with(ui) {
        linearLayout {
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
            padding = dip(10)

            weightSum = 5f

            textInputLayout {

                addressInput = textInputEditText {
                    inputType = InputType.TYPE_CLASS_TEXT
                    maxLines = 1
                    hintResource = R.string.address
                }

            }.lparams(width = dip(0), height = wrapContent) { weight = 4f }

            button(R.string.ok) {
                onClick {
                    val urlValue = addressInput.text.toString()
                    if (urlValue.startsWith("http://", true)
                            || urlValue.startsWith("https://", true)) {
                        callback?.invoke(addressInput.text.toString())
                        ui.owner.dismiss()
                    }
                    else toast(ui.ctx.getString(R.string.link_missing_protocol))

                }
            }.lparams(width = dip(0), height = wrapContent) {
                marginStart = dip(10)
                weight = 1f
            }
        }
    }
}