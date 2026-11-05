package com.example.madtlab5;

import java.util.List;

interface NoteStore {
    List<Note> getAll();
    Note getById(String id);
    void save(Note note);
    boolean delete(String id);
}
