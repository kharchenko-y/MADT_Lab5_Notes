package com.example.madtlab5;

import org.junit.Test;
import static org.junit.Assert.*;

public class NoteTest {
    @Test public void trimsFieldsOnCreation() {
        Note note = new Note("id-1", "  Title  ", "  My content  ", 123L);
        assertEquals("id-1", note.getId());
        assertEquals("Title", note.getTitle());
        assertEquals("My content", note.getContent());
        assertEquals(123L, note.getUpdatedAt());
    }
    @Test public void contentAndIdentityAreCompared() {
        Note a = new Note("a", "Title", "Body", 15L);
        assertTrue(a.sameDataAs(new Note("a", "Title", "Body", 15L)));
        assertFalse(a.sameDataAs(new Note("b", "Title", "Body", 15L)));
        assertFalse(a.sameDataAs(new Note("a", "Title", "Edited", 15L)));
        assertFalse(a.sameDataAs(new Note("a", "Title", "Body", 16L)));
        assertFalse(a.sameDataAs(null));
    }
    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingId() { new Note("", "Title", "Body", 0L); }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsWhitespaceTitle() { new Note("id", "  ", "Body", 0L); }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsWhitespaceContent() { new Note("id", "Title", "  ", 0L); }
}
