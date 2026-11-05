package com.example.madtlab5;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class NoteSyncPlannerTest {
    private static Note note(String id, String title, long timestamp) {
        return new Note(id, title, "Content", timestamp);
    }

    @Test public void insertsPrimaryNotesIntoEmptySecondary() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Arrays.asList(note("a", "A", 1L), note("b", "B", 2L)),
                Collections.emptyList());
        assertEquals(2, result.getInserted());
        assertEquals(0, result.getUpdated());
        assertEquals(0, result.getUnchanged());
        assertEquals(2, result.getMergedNotes().size());
    }

    @Test public void keepsSecondaryOnlyNotes() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Collections.singletonList(note("a", "Primary", 2L)),
                Collections.singletonList(note("secondary", "Only here", 4L)));
        assertEquals(2, result.getMergedNotes().size());
        assertEquals("secondary", result.getMergedNotes().get(0).getId());
        assertEquals("a", result.getMergedNotes().get(1).getId());
    }

    @Test public void newerSecondaryEditIsNotLost() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Collections.singletonList(note("a", "Older SQLite", 3L)),
                Collections.singletonList(note("a", "Newer secondary", 9L)));
        assertEquals(0, result.getInserted());
        assertEquals(0, result.getUpdated());
        assertEquals(1, result.getConflicts());
        assertEquals(0, result.getChangedCount());
        assertEquals("Newer secondary", result.getMergedNotes().get(0).getTitle());
    }

    @Test public void newerPrimaryUpdatesSecondaryCopy() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Collections.singletonList(note("a", "Fresh SQLite", 10L)),
                Collections.singletonList(note("a", "Old secondary", 2L)));
        assertEquals(1, result.getUpdated());
        assertEquals(0, result.getConflicts());
        assertEquals("Fresh SQLite", result.getMergedNotes().get(0).getTitle());
    }

    @Test public void equalTimestampsWithDifferentContentAreConflicts() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Collections.singletonList(note("a", "Primary", 4L)),
                Collections.singletonList(note("a", "Secondary", 4L)));
        assertEquals(1, result.getConflicts());
        assertEquals("Secondary", result.getMergedNotes().get(0).getTitle());
    }

    @Test public void severalConflictsDoNotBlockNewInserts() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Arrays.asList(note("same", "From SQLite", 5L), note("new", "New", 9L)),
                Collections.singletonList(note("same", "Keep local edit", 6L)));
        assertEquals(1, result.getInserted());
        assertEquals(1, result.getConflicts());
        assertEquals(2, result.getMergedNotes().size());
    }

    @Test public void sameNotesAreUnchanged() {
        Note a = note("a", "Original", 1L);
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Collections.singletonList(a), Collections.singletonList(a));
        assertEquals(1, result.getUnchanged());
        assertEquals(0, result.getChangedCount());
    }

    @Test public void emptyPrimaryDoesNotDeleteSecondary() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Collections.emptyList(), Collections.singletonList(note("a", "A", 1L)));
        assertEquals(0, result.getChangedCount());
        assertEquals(1, result.getMergedNotes().size());
    }

    @Test public void inputListsAreNotMutated() {
        List<Note> primary = new ArrayList<>(Collections.singletonList(note("a", "A", 1L)));
        List<Note> secondary = new ArrayList<>(Collections.singletonList(note("b", "B", 2L)));
        NoteSyncPlanner.merge(primary, secondary);
        assertEquals(1, primary.size());
        assertEquals(1, secondary.size());
    }

    @Test public void repeatedSynchronizationIsIdempotent() {
        List<Note> primary = Collections.singletonList(note("a", "A", 1L));
        NoteSyncPlanner.Result first = NoteSyncPlanner.merge(primary, Collections.emptyList());
        NoteSyncPlanner.Result second = NoteSyncPlanner.merge(primary, first.getMergedNotes());
        assertEquals(1, first.getInserted());
        assertEquals(0, second.getChangedCount());
        assertEquals(1, second.getUnchanged());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullPrimary() { NoteSyncPlanner.merge(null, Collections.emptyList()); }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullSecondary() { NoteSyncPlanner.merge(Collections.emptyList(), null); }

    @Test public void returnedListCannotAlterOriginalResult() {
        NoteSyncPlanner.Result result = NoteSyncPlanner.merge(
                Collections.singletonList(note("a", "A", 1L)), Collections.emptyList());
        result.getMergedNotes().clear();
        assertEquals(1, result.getMergedNotes().size());
    }
}
