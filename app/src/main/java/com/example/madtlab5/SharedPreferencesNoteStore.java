package com.example.madtlab5;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Secondary local repository: one JSON array written atomically to SharedPreferences. */
final class SharedPreferencesNoteStore implements NoteStore {
    private static final String FILE_NAME = "notes_secondary";
    private static final String KEY_NOTES = "notes_json_v1";
    private final SharedPreferences preferences;

    SharedPreferencesNoteStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    private List<Note> read() {
        List<Note> notes = new ArrayList<>();
        String raw = preferences.getString(KEY_NOTES, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                notes.add(new Note(item.getString("id"), item.getString("title"),
                        item.getString("content"), item.getLong("updatedAt")));
            }
        } catch (JSONException | IllegalArgumentException exception) {
            // Do not silently discard corrupt data; that could overwrite valid notes.
            throw new IllegalStateException("Cannot read secondary note storage", exception);
        }
        return notes;
    }

    private void write(List<Note> notes) {
        JSONArray array = new JSONArray();
        try {
            for (Note note : notes) {
                JSONObject entry = new JSONObject();
                entry.put("id", note.getId());
                entry.put("title", note.getTitle());
                entry.put("content", note.getContent());
                entry.put("updatedAt", note.getUpdatedAt());
                array.put(entry);
            }
        } catch (JSONException exception) {
            throw new IllegalStateException("Cannot serialize notes", exception);
        }
        // commit() reports persistence failure synchronously instead of returning early.
        if (!preferences.edit().putString(KEY_NOTES, array.toString()).commit()) {
            throw new IllegalStateException("Cannot save secondary note storage");
        }
    }

    @Override public List<Note> getAll() {
        List<Note> notes = read();
        notes.sort(Comparator.comparingLong(Note::getUpdatedAt).reversed()
                .thenComparing(Note::getId));
        return notes;
    }

    @Override public Note getById(String id) {
        for (Note note : read()) {
            if (note.getId().equals(id)) return note;
        }
        return null;
    }

    @Override public void save(Note note) {
        List<Note> notes = read();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(note.getId())) {
                notes.set(i, note);
                write(notes);
                return;
            }
        }
        notes.add(note);
        write(notes);
    }

    @Override public boolean delete(String id) {
        List<Note> notes = read();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(id)) {
                notes.remove(i);
                write(notes);
                return true;
            }
        }
        return false;
    }

    NoteSyncPlanner.Result synchronizeFromPrimary(List<Note> primaryNotes) {
        NoteSyncPlanner.Result plan = NoteSyncPlanner.merge(primaryNotes, read());
        if (plan.getChangedCount() > 0) {
            write(plan.getMergedNotes());
        }
        return plan;
    }
}
