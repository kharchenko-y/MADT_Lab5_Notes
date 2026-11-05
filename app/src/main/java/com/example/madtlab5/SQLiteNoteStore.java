package com.example.madtlab5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Primary local repository. Uses only Android's built-in SQLite API. */
final class SQLiteNoteStore extends SQLiteOpenHelper implements NoteStore {
    private static final String DATABASE_NAME = "notes_primary.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE = "notes";

    SQLiteNoteStore(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes ("
                + "id TEXT PRIMARY KEY NOT NULL, "
                + "title TEXT NOT NULL, "
                + "content TEXT NOT NULL, "
                + "updated_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX notes_updated_at_idx ON notes(updated_at DESC)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // v1 schema. Future upgrades must migrate data, not drop the existing table.
        throw new IllegalStateException("No database migration defined for version " + newVersion);
    }

    private static Note fromCursor(Cursor cursor) {
        return new Note(
                cursor.getString(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("content")),
                cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
    }

    @Override public List<Note> getAll() {
        List<Note> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE,
                new String[]{"id", "title", "content", "updated_at"},
                null, null, null, null, "updated_at DESC, id ASC")) {
            while (cursor.moveToNext()) {
                result.add(fromCursor(cursor));
            }
        }
        return result;
    }

    @Override public Note getById(String id) {
        try (Cursor cursor = getReadableDatabase().query(TABLE,
                new String[]{"id", "title", "content", "updated_at"},
                "id = ?", new String[]{id}, null, null, null, "1")) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    @Override public void save(Note note) {
        ContentValues values = new ContentValues();
        values.put("id", note.getId());
        values.put("title", note.getTitle());
        values.put("content", note.getContent());
        values.put("updated_at", note.getUpdatedAt());
        long result = getWritableDatabase().insertWithOnConflict(TABLE, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
        if (result < 0) {
            throw new IllegalStateException("SQLite write failed");
        }
    }

    @Override public boolean delete(String id) {
        return getWritableDatabase().delete(TABLE, "id = ?", new String[]{id}) > 0;
    }
}
