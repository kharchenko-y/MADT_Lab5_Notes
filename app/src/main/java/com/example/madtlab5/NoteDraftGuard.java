package com.example.madtlab5;

/** Pure-Java check for editor changes that would be lost when exiting. */
public final class NoteDraftGuard {
    private NoteDraftGuard() { }

    public static boolean hasUnsavedChanges(Note original, String title, String content) {
        String currentTitle = normalize(title);
        String currentContent = normalize(content);
        if (original == null) {
            return !currentTitle.isEmpty() || !currentContent.isEmpty();
        }
        return !original.getTitle().equals(currentTitle)
                || !original.getContent().equals(currentContent);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
