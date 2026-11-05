package com.example.madtlab5;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Build;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

/** Separate activity demonstrates Intent-based data transfer and full note CRUD. */
public final class NoteEditorActivity extends Activity {
    public static final String EXTRA_SOURCE = "com.example.madtlab5.EXTRA_SOURCE";
    public static final String EXTRA_NOTE_ID = "com.example.madtlab5.EXTRA_NOTE_ID";
    private static final String LOG_TAG = "MADT_Lab5";

    private EditText titleField;
    private EditText contentField;
    private NoteStore store;
    private SQLiteNoteStore sqliteStore;
    private Note original;
    private OnBackInvokedCallback backCallback;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        InsetsHelper.apply(findViewById(R.id.editorRoot));

        titleField = findViewById(R.id.titleInput);
        contentField = findViewById(R.id.contentInput);
        TextView editorHeading = findViewById(R.id.editorHeading);
        TextView destinationLabel = findViewById(R.id.editorStorage);
        Button save = findViewById(R.id.saveButton);
        Button delete = findViewById(R.id.deleteButton);
        Button back = findViewById(R.id.backButton);

        StorageChoice choice = StorageChoice.fromPosition(
                getIntent().getIntExtra(EXTRA_SOURCE, 0));
        if (choice == StorageChoice.SQLITE) {
            sqliteStore = new SQLiteNoteStore(this);
            store = sqliteStore;
            destinationLabel.setText(R.string.sqlite_target);
        } else {
            store = new SharedPreferencesNoteStore(this);
            destinationLabel.setText(R.string.preferences_target);
        }

        String noteId = getIntent().getStringExtra(EXTRA_NOTE_ID);
        if (noteId != null) {
            try {
                original = store.getById(noteId);
                if (original == null) {
                    Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                editorHeading.setText(R.string.edit_note);
                titleField.setText(original.getTitle());
                contentField.setText(original.getContent());
                delete.setVisibility(View.VISIBLE);
            } catch (RuntimeException exception) {
                Log.e(LOG_TAG, "Unable to open note", exception);
                Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            editorHeading.setText(R.string.create_note);
            delete.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+: intercept system Back / predictive Back as well as the UI button.
            backCallback = this::exitWithDraftCheck;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, backCallback);
        }
        back.setOnClickListener(v -> exitWithDraftCheck());
        save.setOnClickListener(v -> saveNote());
        delete.setOnClickListener(v -> confirmDelete());
    }

    @Override public void onBackPressed() {
        exitWithDraftCheck();
    }

    private void exitWithDraftCheck() {
        if (!NoteDraftGuard.hasUnsavedChanges(original,
                titleField.getText().toString(), contentField.getText().toString())) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.discard_changes_title)
                .setMessage(R.string.discard_changes_message)
                .setPositiveButton(R.string.discard_changes, (dialog, which) -> finish())
                .setNegativeButton(R.string.keep_editing, null)
                .show();
    }

    private void saveNote() {
        String title = titleField.getText().toString();
        String content = contentField.getText().toString();
        NoteValidator.Result validation = NoteValidator.validate(title, content);
        if (validation == NoteValidator.Result.EMPTY_TITLE) {
            titleField.setError(getString(R.string.title_required));
            titleField.requestFocus();
            return;
        }
        if (validation == NoteValidator.Result.EMPTY_CONTENT) {
            contentField.setError(getString(R.string.content_required));
            contentField.requestFocus();
            return;
        }

        String id = original == null ? UUID.randomUUID().toString() : original.getId();
        Note note = new Note(id, title, content, System.currentTimeMillis());
        try {
            store.save(note);
            Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (RuntimeException exception) {
            Log.e(LOG_TAG, "Unable to save note", exception);
            Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDelete() {
        if (original == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_body)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteNote())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteNote() {
        if (original == null) return;
        try {
            if (store.delete(original.getId())) {
                Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.note_not_found, Toast.LENGTH_SHORT).show();
            }
            setResult(RESULT_OK);
            finish();
        } catch (RuntimeException exception) {
            Log.e(LOG_TAG, "Unable to delete note", exception);
            Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && backCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback);
            backCallback = null;
        }
        if (sqliteStore != null) sqliteStore.close();
        super.onDestroy();
    }
}
