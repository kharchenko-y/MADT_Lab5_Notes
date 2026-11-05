package com.example.madtlab5;

/** Pure-Java, independently testable form validation. */
public final class NoteValidator {
    public enum Result { VALID, EMPTY_TITLE, EMPTY_CONTENT }

    private NoteValidator() { }

    public static Result validate(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            return Result.EMPTY_TITLE;
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.EMPTY_CONTENT;
        }
        return Result.VALID;
    }
}
