package com.example.madtlab5;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** Main list, explicit storage selection and a manually triggered one-way sync. */
public final class MainActivity extends Activity {
    private static final String LOG_TAG = "MADT_Lab5";
    private static final String UI_PREF_FILE = "notes_ui_preferences";
    private static final String UI_PREF_KEY = "selected_source";

    private SQLiteNoteStore primary;
    private SharedPreferencesNoteStore secondary;
    private NoteListAdapter notesAdapter;
    private Spinner storageSpinner;
    private TextView noteCount;
    private TextView storageExplanation;
    private StorageChoice selected = StorageChoice.SQLITE;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InsetsHelper.apply(findViewById(R.id.mainRoot));

        primary = new SQLiteNoteStore(this);
        secondary = new SharedPreferencesNoteStore(this);
        noteCount = findViewById(R.id.noteCount);
        storageExplanation = findViewById(R.id.storageExplanation);
        storageSpinner = findViewById(R.id.storageSpinner);

        ArrayAdapter<CharSequence> choices = ArrayAdapter.createFromResource(this,
                R.array.storage_options, android.R.layout.simple_spinner_item);
        choices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storageSpinner.setAdapter(choices);

        ListView list = findViewById(R.id.notesList);
        notesAdapter = new NoteListAdapter(this);
        list.setAdapter(notesAdapter);
        list.setEmptyView(findViewById(R.id.emptyNotes));
        list.setOnItemClickListener((parent, view, position, id) ->
                launchEditor(notesAdapter.getItem(position).getId()));

        Button add = findViewById(R.id.addNoteButton);
        Button sync = findViewById(R.id.syncButton);
        add.setOnClickListener(view -> launchEditor(null));
        sync.setOnClickListener(view -> confirmSync());

        selected = StorageChoice.fromPosition(getSharedPreferences(UI_PREF_FILE, MODE_PRIVATE)
                .getInt(UI_PREF_KEY, 0));
        storageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                 int position, long id) {
                selected = StorageChoice.fromPosition(position);
                getSharedPreferences(UI_PREF_FILE, MODE_PRIVATE).edit()
                        .putInt(UI_PREF_KEY, selected.position()).apply();
                refreshNotes();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        storageSpinner.setSelection(selected.position());
        refreshNotes();
    }

    @Override protected void onResume() {
        super.onResume();
        if (notesAdapter != null) refreshNotes();
    }

    private NoteStore currentStore() {
        return selected == StorageChoice.SQLITE ? primary : secondary;
    }

    private void refreshNotes() {
        if (primary == null || secondary == null || notesAdapter == null) return;
        storageExplanation.setText(selected == StorageChoice.SQLITE
                ? R.string.sqlite_description : R.string.preferences_description);
        try {
            List<Note> notes = currentStore().getAll();
            notesAdapter.setNotes(notes);
            noteCount.setText(getResources().getQuantityString(
                    R.plurals.notes_count, notes.size(), notes.size()));
        } catch (RuntimeException exception) {
            Log.e(LOG_TAG, "Unable to load notes", exception);
            notesAdapter.setNotes(java.util.Collections.emptyList());
            noteCount.setText(R.string.storage_error);
            Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
        }
    }

    private void launchEditor(String noteId) {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        intent.putExtra(NoteEditorActivity.EXTRA_SOURCE, selected.position());
        if (noteId != null) intent.putExtra(NoteEditorActivity.EXTRA_NOTE_ID, noteId);
        startActivity(intent);
    }

    private void confirmSync() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sync_dialog_title)
                .setMessage(R.string.sync_dialog_message)
                .setPositiveButton(R.string.sync_confirm, (dialog, which) -> syncFromPrimary())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void syncFromPrimary() {
        try {
            List<Note> primaryNotes = primary.getAll();
            NoteSyncPlanner.Result result = secondary.synchronizeFromPrimary(primaryNotes);
            if (primaryNotes.isEmpty()) {
                Toast.makeText(this, R.string.sync_no_primary_notes, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.sync_result,
                        result.getInserted(), result.getUpdated(), result.getUnchanged(),
                        result.getConflicts()),
                        Toast.LENGTH_LONG).show();
            }
            refreshNotes();
        } catch (RuntimeException exception) {
            Log.e(LOG_TAG, "Unable to synchronize notes", exception);
            Toast.makeText(this, R.string.sync_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onDestroy() {
        if (primary != null) primary.close();
        super.onDestroy();
    }
}
