package com.ebolo.krichtexteditor.ui.widgets

import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.ImageView
import com.bitbucket.eventbus.EventBus
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.RichEditor
import com.ebolo.krichtexteditor.ui.actionImageViewStyle
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class EditorToolbar(private val editor: RichEditor, private val buttonsLayout: List<Int>) {
    var linkButtonAction: (() -> Unit)? = null
    var imageButtonAction: (() -> Unit)? = null

    private lateinit var buttons: Map<Int, ImageView>

    var buttonActivatedColorId: Int = R.color.colorAccent
    var buttonDeactivatedColorId: Int = R.color.tintColor

    fun createToolbar(parent: ViewGroup) = parent.horizontalScrollView {
        linearLayout {
            val eventBus = EventBus.getInstance()

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

            buttons = buttonsLayout.map {
                val button = createButton(it)
                eventBus.on("style_$it") {
                    val state = it as Boolean
                    context.runOnUiThread {
                        button.setColorFilter( ContextCompat.getColor(
                                context,
                                when {
                                    state -> buttonActivatedColorId
                                    else -> buttonDeactivatedColorId
                                }
                        ) )
                    }
                }
                it to button
            }.toMap()

        }.lparams(width = wrapContent, height = dip(40))
    }

}