package com.example.madtlab5;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Plans one-way synchronization without Android dependencies.
 * SQLite is the authoritative source for newer versions, but never overwrites
 * a matching secondary note with an older or equally-timestamped version.
 * Secondary-only notes remain intact. Conflicting newer secondary copies are
 * left in place for the user to reconcile manually.
 */
public final class NoteSyncPlanner {
    private NoteSyncPlanner() { }

    public static final class Result {
        private final List<Note> mergedNotes;
        private final int inserted;
        private final int updated;
        private final int unchanged;
        private final int conflicts;

        private Result(List<Note> mergedNotes, int inserted, int updated, int unchanged, int conflicts) {
            this.mergedNotes = mergedNotes;
            this.inserted = inserted;
            this.updated = updated;
            this.unchanged = unchanged;
            this.conflicts = conflicts;
        }

        public List<Note> getMergedNotes() { return new ArrayList<>(mergedNotes); }
        public int getInserted() { return inserted; }
        public int getUpdated() { return updated; }
        public int getUnchanged() { return unchanged; }
        public int getConflicts() { return conflicts; }
        public int getChangedCount() { return inserted + updated; }
    }

    public static Result merge(List<Note> primary, List<Note> secondary) {
        if (primary == null || secondary == null) {
            throw new IllegalArgumentException("Both sources are required");
        }
        LinkedHashMap<String, Note> notesById = new LinkedHashMap<>();
        for (Note note : secondary) {
            notesById.put(note.getId(), note);
        }
        int inserted = 0;
        int updated = 0;
        int unchanged = 0;
        int conflicts = 0;
        for (Note note : primary) {
            Note existing = notesById.get(note.getId());
            if (existing == null) {
                notesById.put(note.getId(), note);
                inserted++;
            } else if (existing.sameDataAs(note)) {
                unchanged++;
            } else if (existing.getUpdatedAt() >= note.getUpdatedAt()) {
                // A local secondary edit may be newer; do not destroy it.
                // A tie is ambiguous, so also preserve the secondary version.
                conflicts++;
            } else {
                // The primary version is strictly newer and can be copied.
                notesById.put(note.getId(), note);
                updated++;
            }
        }
        return new Result(new ArrayList<>(notesById.values()),
                inserted, updated, unchanged, conflicts);
    }
}
