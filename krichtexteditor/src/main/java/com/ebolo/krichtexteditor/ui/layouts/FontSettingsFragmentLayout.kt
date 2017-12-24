package com.ebolo.krichtexteditor.ui.layouts

import android.support.v7.widget.LinearLayoutManager
import com.ebolo.krichtexteditor.fragments.FontSettingsFragment
import com.ebolo.krichtexteditor.fragments.FontSettingsFragment.Companion.TYPE_LINE_HEIGHT
import com.ebolo.krichtexteditor.fragments.FontSettingsFragment.Companion.TYPE_SIZE
import com.ebolo.krichtexteditor.ui.adapters.FontSettingsAdapter
import com.ebolo.krichtexteditor.ui.adapters.fontSettingsAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView

class FontSettingsFragmentLayout(
        private val type: Int,
        private var callback: ((String) -> Unit)? = null
): AnkoComponent<FontSettingsFragment> {
    private val fontFamilyList by lazy {
        listOf(
                "Arial", "Arial Black", "Comic Sans MS", "Courier New", "Helvetica Neue",
                "Helvetica", "Impact", "Lucida Grande", "Tahoma", "Times New Roman", "Verdana"
        )
    }
    private val fontSizeList by lazy {
        listOf("12", "14", "16", "18", "20", "22", "24", "26", "28", "36")
    }
    private val fontLineHeightList by lazy {
        listOf("1.0", "1.2", "1.4", "1.6", "1.8", "2.0", "3.0")
    }

    override fun createView(ui: AnkoContext<FontSettingsFragment>) = with(ui) {
        frameLayout {
            layoutParams.width = matchParent
            layoutParams.height = matchParent

            recyclerView {
                layoutManager = LinearLayoutManager(ui.ctx)
                adapter = fontSettingsAdapter(
                        when(type) {
                            TYPE_SIZE -> fontSizeList
                            TYPE_LINE_HEIGHT -> fontLineHeightList
                            else -> fontFamilyList
                        }
                ) {
                    setOnItemClickListener { adapter, _, position ->
                        callback?.invoke((adapter as FontSettingsAdapter).items[position])
                        val editorMenuFragment = ui.owner
                                .fragmentManager
                                .findFragmentByTag("Host")
                        ui.owner.fragmentManager
                                .beginTransaction()
                                .remove(ui.owner)
                                .show(editorMenuFragment)
                                .commit()
                    }
                }
            }.lparams(width = matchParent, height = wrapContent)
        }
    }
}