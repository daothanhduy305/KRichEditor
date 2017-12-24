package com.ebolo.krichtexteditor.ui.adapters

import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ebolo.krichtexteditor.R
import com.ebolo.krichtexteditor.ui.layouts.FontSettingItemLayout
import org.jetbrains.anko.AnkoContext

/**
 * Font Setting Adapter
 * Created by even.wu on 9/8/17.
 * Ported by ebolo(daothanhduy305) on 21/12/2017
 */

class FontSettingsAdapter(val items: List<String>):
        BaseQuickAdapter<String, BaseViewHolder>(items)
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BaseViewHolder(
            FontSettingItemLayout().createView(AnkoContext.Companion.create(parent.context, parent))
    )

    override fun convert(helper: BaseViewHolder, item: String?) {
        helper.setText(R.id.tv_content, item)
    }
}

fun fontSettingsAdapter(items: List<String>, setup: FontSettingsAdapter.() -> Unit): FontSettingsAdapter {
    val adapter = FontSettingsAdapter(items)
    setup.invoke(adapter)
    return adapter
}