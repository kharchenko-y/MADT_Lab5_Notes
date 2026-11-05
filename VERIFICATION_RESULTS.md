# Code Generation — Verification Results (5 November 2026)

## Completed locally

- Java/XML/Gradle project files generated.
- **12/12 Android XML files** parsed successfully as well-formed XML.
- Android resource references in Java checked against generated resources; **no missing references found**.
- Every `android:text`/`android:hint` attribute in layouts points to `@string/...` rather than a hard-coded UI label.
- **23/23 host-side JUnit-compatible tests passed** using a temporary local test runner (validation, note model, one-way merge semantics). The actual JUnit files are under `app/src/test/` and use JUnit 4 syntax for Gradle.
- All Android Java source files compiled with `javac` **against temporary handwritten API stubs** for syntax/type checks. This is *not* Android SDK compilation or Gradle validation.
- Gradle wrapper JAR/scripts and plugin versions copied from the student's previously working MADT Lab #4 setup (AGP 8.9.2, Gradle 8.11.1; compileSdk 35; minSdk 23; targetSdk 35; Java 17).

## Not completed in this environment

- No Android SDK present: **`:app:assembleDebug` has NOT been run here**.
- No Android emulator present: **no on-device run, runtime UI verification or instrumentation tests** have been performed here.
- No independent AI Code Review, documentation README, GitHub commit/push, or Moodle upload was performed in this session.

## Required on student's Windows machine

From the project root, run:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

Then run the app on Pixel 6 API 35 and verify manually:

1. SQLite: create, open, edit and delete a note.
2. SharedPreferences: create, open, edit and delete a note.
3. Invalid blank/whitespace-only title and content are rejected.
4. Create a SQLite note, switch to SharedPreferences, sync, then confirm the note appears there.
5. Edit the SharedPreferences copy, sync again, and confirm the SQLite copy takes precedence.
6. Create a SharedPreferences-only note, sync again, and confirm that note is preserved.
7. Force-stop and reopen app: each storage still contains its notes.
8. Rotate screen and check draft preservation, the status/navigation bars, and the keyboard.

If build/run fails, record the exact Gradle error and pass it to the independent Code Review session. Do not manually modify source code.
