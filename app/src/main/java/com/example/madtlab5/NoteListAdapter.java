package com.example.madtlab5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Simple note-list UI; title and content come exclusively from the selected store. */
final class NoteListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<Note> notes = new ArrayList<>();

    NoteListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    void setNotes(List<Note> updated) {
        notes.clear();
        notes.addAll(updated);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return notes.size(); }
    @Override public Note getItem(int position) { return notes.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override public View getView(int position, View reusable, ViewGroup parent) {
        View view = reusable;
        if (view == null) {
            view = inflater.inflate(R.layout.item_note, parent, false);
        }
        Note note = getItem(position);
        TextView title = view.findViewById(R.id.noteTitle);
        TextView content = view.findViewById(R.id.notePreview);
        title.setText(note.getTitle());
        content.setText(note.getContent());
        return view;
    }
}
