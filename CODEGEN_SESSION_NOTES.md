# MADT Lab #5 — Code Generation Session Notes

**Date:** 5 November 2026  
**Session:** Code Generation (separate from Code Review / README / GitHub)  
**Assignment:** Notes Storage App — Viber variant  
**Source of requirements:** `MADT_5_ActivitiesInteractionsAndStorage_Viber.pdf`

## Student input

- The student confirmed Lab #4 had been submitted, then explicitly requested starting Lab #5.
- The student expects the same separated AI-session workflow as Lab #4 and does **not** manually edit generated Java/XML.
- The existing working Android Studio setup is on Windows, with the Pixel 6 API 35 emulator used for previous labs.

## Plan / `/plan`-equivalent design decisions before coding

1. Implement a Notes Android application using **Java/XML**, built with Gradle.
2. Use **SQLite** as primary storage and **SharedPreferences JSON** as secondary storage.
3. Put CRUD in two activities: list/storage selection and note editor (via explicit Intent).
4. Require **both title and content** to contain non-whitespace characters.
5. Keep stores independent; a storage spinner determines where CRUD actions operate.
6. Implement **explicit one-way** sync from SQLite to SharedPreferences. Stable UUIDs identify notes. Matching IDs take the SQLite version; notes only in SharedPreferences remain.
7. Keep every user-visible UI string in `strings.xml` and storage chooser strings in a string-array resource.
8. Add host-side JUnit tests for the pure-Java domain and merge algorithm, with Android Studio compilation and emulator verification left to student and Code Review sessions.

## AI-produced code

- `MainActivity`: list, storage selection, CRUD navigation, confirmation and sync feedback.
- `NoteEditorActivity`: create/edit/delete with validation; explicit source and note ID passed via Intent.
- `SQLiteNoteStore`: built-in SQLiteOpenHelper database and CRUD implementation.
- `SharedPreferencesNoteStore`: JSON serialization, CRUD and a single-write sync merge.
- `Note`, `NoteValidator`, `NoteSyncPlanner`: pure Java, independently testable.
- `NoteListAdapter`, `StorageChoice`, `InsetsHelper`: presentation helpers, source enum, API 23–35 system-bar inset handling.
- XML layouts/resources/theme/drawables/manifest, Gradle 8.11.1 wrapper and JUnit tests.

## Important limitations and future steps

- This environment **does not have Android SDK or an emulator**. Do not claim that the APK has been built or demonstrated here.
- Pure-Java tests and XML static checks are performed in this Code Generation environment; actual Gradle `:app:assembleDebug` and `:app:testDebugUnitTest` must be run in Android Studio on the student's machine.
- A **separate independent Code Review AI session** must inspect the generated ZIP and implement the two highest-priority fixes.
- **Do not create README or publish to GitHub in this session.** These belong to later sessions.
- The final `ListOfsessions.md` and GitHub URL must be uploaded to Moodle after all four sessions.
