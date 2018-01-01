package com.ebolo.krichtexteditor.ui.layouts

import android.text.InputType
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.fragments.EditTableFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

@Deprecated(message = "Not officially supported yet")
class EditTableFragmentLayout: AnkoComponent<EditTableFragment> {
    var callback: ((row: Int, column: Int) -> Unit)? = null

    override fun createView(ui: AnkoContext<EditTableFragment>) = with(ui) {
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

            textView(R.string.columns)

            val columnInput = editText {
                inputType = InputType.TYPE_CLASS_NUMBER
                maxLines = 1
            }.lparams(width = matchParent, height = wrapContent)

            textView(R.string.rows).lparams { topMargin = dip(32) }

            val rowInput = editText {
                inputType = InputType.TYPE_CLASS_NUMBER
                maxLines = 1
            }.lparams(width = matchParent, height = wrapContent)

            button(R.string.ok) {
                backgroundResource = R.drawable.btn_colored_primary
                textColor = R.color.white

                onClick { callback?.invoke(
                        rowInput.text.toString().toInt(),
                        columnInput.text.toString().toInt()
                ) }
            }.lparams(width = matchParent, height = wrapContent) {
                topMargin = dip(32)
            }
        }
    }
}