package com.ebolo.krichtexteditor.ui.widgets

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bitbucket.eventbus.EventBus
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.RichEditor
import com.ebolo.krichtexteditor.ui.actionImageViewStyle
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class EditorToolbar(private val editor: RichEditor, private val buttonsLayout: List<Int>) {
    var linkButtonAction: (() -> Unit)? = null
    var imageButtonAction: (() -> Unit)? = null

    private lateinit var buttons: Map<Int, ImageView>

    var buttonActivatedColorId: Int = R.color.colorAccent
    var buttonDeactivatedColorId: Int = R.color.tintColor

    fun createToolbar(parent: ViewGroup) = parent.horizontalScrollView {
        linearLayout {
            fun createButton(@EditorButton.Companion.ActionType actionType: Int) = imageView(
                    EditorButton.actionButtonDrawables[actionType]!!
            ) {
                padding = dip(8)
                backgroundResource = R.drawable.btn_colored_material

                onClick {
                    when (actionType) {
                        EditorButton.IMAGE ->
                            if (imageButtonAction != null) imageButtonAction!!.invoke()
                            else this@imageView.context.toast("Not implemented!")
                        EditorButton.LINK ->
                            if (linkButtonAction != null) linkButtonAction!!.invoke()
                            else this@imageView.context.toast("Not implemented!")
                        else -> editor.command(actionType)
                    }
                }
            }.apply { actionImageViewStyle() }

            buttons = buttonsLayout.map { it to createButton(it) }.toMap()

        }.lparams(width = wrapContent, height = dip(40))
    }

    fun setupListeners(context: Context) {
        val eventBus = EventBus.getInstance()
        buttonsLayout.forEach { buttonId ->
            eventBus.on("style", "style_$buttonId") {
                val state = it as Boolean
                context.runOnUiThread {
                    buttons[buttonId]?.setColorFilter( ContextCompat.getColor(
                            context,
                            when {
                                state -> buttonActivatedColorId
                                else -> buttonDeactivatedColorId
                            }
                    ) )
                }
            }
        }
    }
}