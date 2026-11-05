package com.example.madtlab5;

import org.junit.Test;
import static org.junit.Assert.*;

public class NoteValidatorTest {
    @Test public void validFields() {
        assertEquals(NoteValidator.Result.VALID, NoteValidator.validate("Title", "Content"));
    }
    @Test public void nullTitleIsRejected() {
        assertEquals(NoteValidator.Result.EMPTY_TITLE, NoteValidator.validate(null, "Content"));
    }
    @Test public void emptyTitleIsRejected() {
        assertEquals(NoteValidator.Result.EMPTY_TITLE, NoteValidator.validate("", "Content"));
    }
    @Test public void whitespaceTitleIsRejected() {
        assertEquals(NoteValidator.Result.EMPTY_TITLE, NoteValidator.validate(" \t\n ", "Content"));
    }
    @Test public void nullContentIsRejected() {
        assertEquals(NoteValidator.Result.EMPTY_CONTENT, NoteValidator.validate("Title", null));
    }
    @Test public void emptyContentIsRejected() {
        assertEquals(NoteValidator.Result.EMPTY_CONTENT, NoteValidator.validate("Title", ""));
    }
    @Test public void whitespaceContentIsRejected() {
        assertEquals(NoteValidator.Result.EMPTY_CONTENT, NoteValidator.validate("Title", " \n\t"));
    }
    @Test public void bothBlankPrioritizesTitle() {
        assertEquals(NoteValidator.Result.EMPTY_TITLE, NoteValidator.validate(" ", "  "));
    }
}
