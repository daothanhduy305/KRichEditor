package com.ebolo.kricheditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ebolo.krichtexteditor.fragments.KRichEditorFragment;
import com.ebolo.krichtexteditor.fragments.Options;
import com.ebolo.krichtexteditor.ui.widgets.EditorButton;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.util.Arrays;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class JavaActivity extends AppCompatActivity {
    private KRichEditorFragment editorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editorFragment = KRichEditorFragment.getInstance(
                new Options()
                        .placeHolder("Write something cool...")
                        .onImageButtonClicked(new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                ImagePicker.create(JavaActivity.this).start();
                                return null;
                            }
                        })
                        .buttonLayout( Arrays.asList(
                                EditorButton.UNDO,
                                EditorButton.REDO,
                                EditorButton.IMAGE,
                                EditorButton.LINK,
                                EditorButton.BOLD,
                                EditorButton.ITALIC,
                                EditorButton.UNDERLINE,
                                EditorButton.SUBSCRIPT,
                                EditorButton.SUPERSCRIPT,
                                EditorButton.STRIKETHROUGH,
                                EditorButton.JUSTIFY_LEFT,
                                EditorButton.JUSTIFY_CENTER,
                                EditorButton.JUSTIFY_RIGHT,
                                EditorButton.JUSTIFY_FULL,
                                EditorButton.ORDERED,
                                EditorButton.UNORDERED,
                                EditorButton.NORMAL,
                                EditorButton.H1,
                                EditorButton.H2,
                                EditorButton.H3,
                                EditorButton.H4,
                                EditorButton.H5,
                                EditorButton.H6,
                                EditorButton.INDENT,
                                EditorButton.OUTDENT,
                                EditorButton.BLOCK_QUOTE,
                                EditorButton.BLOCK_CODE,
                                EditorButton.CODE_VIEW
                        ) )
        );

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, editorFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_html:
                editorFragment.getEditor().getHtml(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(final String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(JavaActivity.this, s, Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    }
                });
                return true;
            case R.id.action_text:
                editorFragment.getEditor().getText(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(final String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(JavaActivity.this, s, Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                // The second param (true/false) would not reflect BASE64 mode or not
                // Normal URL mode would pass the URL
                editorFragment.getEditor().command(EditorButton.IMAGE, false, "https://" +
                        "beebom-redkapmedia.netdna-ssl.com/wp-content/uploads/2016/01/" +
                        "Reverse-Image-Search-Engines-Apps-And-Its-Uses-2016.jpg");
                // For BASE64, image file path would be passed instead
                editorFragment.getEditor().command(EditorButton.IMAGE, true, image.getPath());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
