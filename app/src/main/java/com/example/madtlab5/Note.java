package com.example.madtlab5;

import java.util.Objects;

/** Immutable note with a stable identity across both storage methods. */
public final class Note {
    private final String id;
    private final String title;
    private final String content;
    private final long updatedAt;

    public Note(String id, String title, String content, long updatedAt) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("A note must have an ID");
        }
        if (NoteValidator.validate(title, content) != NoteValidator.Result.VALID) {
            throw new IllegalArgumentException("Both title and content are required");
        }
        this.id = id;
        this.title = title.trim();
        this.content = content.trim();
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getUpdatedAt() { return updatedAt; }

    public boolean sameDataAs(Note other) {
        return other != null
                && Objects.equals(id, other.id)
                && Objects.equals(title, other.title)
                && Objects.equals(content, other.content)
                && updatedAt == other.updatedAt;
    }
}
