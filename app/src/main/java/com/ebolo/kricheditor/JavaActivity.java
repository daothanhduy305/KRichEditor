package com.ebolo.kricheditor;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ebolo.krichtexteditor.RichEditor;
import com.ebolo.krichtexteditor.fragments.KRichEditorFragment;
import com.ebolo.krichtexteditor.fragments.Options;
import com.ebolo.krichtexteditor.ui.widgets.EditorButton;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import io.paperdb.Paper;

public class JavaActivity extends AppCompatActivity {
    private KRichEditorFragment editorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editorFragment = (KRichEditorFragment) getSupportFragmentManager().findFragmentByTag("EDITOR");

        if (editorFragment == null)
            editorFragment = KRichEditorFragment.getInstance(
                    new Options()
                            .placeHolder("Write something cool...")
                            .onImageButtonClicked(new Runnable() {
                                @Override
                                public void run() {
                                    ImagePicker.create(JavaActivity.this).start();
                                }
                            })
                            // Un-comment this line and comment out the layout below to
                            // disable the toolbar
                            // .showToolbar(false)
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
                                    EditorButton.CHECK,
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
                            .onInitialized(new Runnable() {
                                @Override
                                public void run() {
                                    // Simulate loading saved contents action
                                    editorFragment.getEditor().setContents(
                                            Paper.book("demo").read("content", "")
                                    );
                                }
                            })
            );

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, editorFragment, "EDITOR")
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
                editorFragment.getEditor().getHtmlContent(new RichEditor.OnHtmlReturned() {
                    @Override
                    public void process(@NotNull final String html) {
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(JavaActivity.this, html, Toast.LENGTH_LONG).show();
                            }
                        } );
                    }
                } );
                return true;
            case R.id.action_text:
                editorFragment.getEditor().getText(new RichEditor.OnTextReturned() {
                    @Override
                    public void process(@NotNull final String text) {
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(JavaActivity.this, text, Toast.LENGTH_LONG).show();
                            }
                        } );
                    }
                });
                return true;
            case R.id.action_set_html:
                editorFragment.getEditor().setHtmlContent("<strong>This is a test HTML content</strong>", true);
                return true;
            case R.id.action_save_content:
                editorFragment.getEditor().getContents(new RichEditor.OnContentsReturned() {
                    @Override
                    public void process(@NotNull String contents) {
                        Paper.book("demo").write("content", contents);
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
                /*editorFragment.getEditor().command(EditorButton.IMAGE, false, "https://" +
                        "beebom-redkapmedia.netdna-ssl.com/wp-content/uploads/2016/01/" +
                        "Reverse-Image-Search-Engines-Apps-And-Its-Uses-2016.jpg");*/

                // For BASE64, image file path would be passed instead
                editorFragment.getEditor().command(EditorButton.IMAGE, true, image.getPath());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        // Simulate saving action
        editorFragment.getEditor().getContents(new RichEditor.OnContentsReturned() {
            @Override
            public void process(@NotNull String contents) {
                Paper.book("demo").write("content", contents);
            }
        });
        super.onPause();
    }
}
