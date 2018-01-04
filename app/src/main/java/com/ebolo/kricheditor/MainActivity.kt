package com.ebolo.kricheditor

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.ebolo.krichtexteditor.fragments.KRichEditorFragment
import com.ebolo.krichtexteditor.fragments.kRichEditorFragment
import org.jetbrains.anko.alert
import org.jetbrains.anko.setContentView

class MainActivity : AppCompatActivity() {
    private lateinit var editorFragment: KRichEditorFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityLayout().setContentView(this)

        editorFragment = kRichEditorFragment {
            // This is just a dummy callback
            imageCallback = {
                "https://beebom-redkapmedia.netdna-ssl.com/wp-content/uploads/2016/01/" +
                        "Reverse-Image-Search-Engines-Apps-And-Its-Uses-2016.jpg"
            }
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_holder, editorFragment)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_html -> {
                editorFragment.editor.getHtml { html ->
                    runOnUiThread {
                        alert(
                                message = html,
                                title = "HTML"
                        ).show()
                    }
                }
                true
            }
            R.id.action_text -> {
                editorFragment.editor.getText {
                    runOnUiThread {
                        alert(
                                message = it,
                                title = "Text"
                        ).show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
