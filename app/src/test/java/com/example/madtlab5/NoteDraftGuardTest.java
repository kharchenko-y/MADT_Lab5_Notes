package com.example.madtlab5;

import org.junit.Test;
import static org.junit.Assert.*;

public class NoteDraftGuardTest {
    private static Note savedNote() {
        return new Note("id", "Title", "Body", 123L);
    }

    @Test public void emptyNewEditorCanClose() {
        assertFalse(NoteDraftGuard.hasUnsavedChanges(null, "", "  "));
    }

    @Test public void titleOnlyDraftRequiresConfirmation() {
        assertTrue(NoteDraftGuard.hasUnsavedChanges(null, "Draft", ""));
    }

    @Test public void contentOnlyDraftRequiresConfirmation() {
        assertTrue(NoteDraftGuard.hasUnsavedChanges(null, "", "Unfinished content"));
    }

    @Test public void untouchedSavedNoteCanClose() {
        assertFalse(NoteDraftGuard.hasUnsavedChanges(savedNote(), "Title", "Body"));
    }

    @Test public void normalizedWhitespaceDoesNotCountAsChange() {
        assertFalse(NoteDraftGuard.hasUnsavedChanges(savedNote(), "  Title  ", " Body "));
    }

    @Test public void editedTitleIsRecognized() {
        assertTrue(NoteDraftGuard.hasUnsavedChanges(savedNote(), "Edited", "Body"));
    }

    @Test public void editedContentIsRecognized() {
        assertTrue(NoteDraftGuard.hasUnsavedChanges(savedNote(), "Title", "Edited body"));
    }

    @Test public void clearingSavedTextIsUnsavedChange() {
        assertTrue(NoteDraftGuard.hasUnsavedChanges(savedNote(), null, ""));
    }
}