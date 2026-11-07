# Local Notes — Android App

**Local Notes** is an offline Android application built with **Java and XML**. It supports full note CRUD, independent local storage backends, and navigation between a notes list and editor.

Notes can be stored in **SQLite** or **SharedPreferences (JSON)**. Users choose where to work and can manually synchronize from SQLite to SharedPreferences using a conflict-aware merge that preserves newer destination edits.

## Features

- **Full CRUD:** create, view, edit, and delete notes; each note has a title and a body.
- **Independent storage selection:** choose `SQLite · Primary` or `SharedPreferences · Secondary` from the home-screen spinner. The list and subsequent create/edit/delete actions operate on the chosen store.
- **Offline persistence:** notes survive normal app restarts in the selected store; no network connection or account is required.
- **Manual one-way synchronization:** copy notes from SQLite to SharedPreferences with a confirmation dialog and a result summary.
- **Conflict-aware merging:** match notes by stable UUID and compare modification timestamps before replacing a destination note.
- **Validation:** reject missing, empty, or whitespace-only titles and bodies; show field-specific validation errors.
- **Unsaved-change protection:** an edited or unfinished draft triggers a **Discard / Keep editing** dialog when leaving with the in-app or system Back control.
- **Deletion confirmation**, human-readable storage errors, and a note count/empty state.
- **Localizable UI:** user-visible labels, button captions, errors, and dialog messages live in Android string resources rather than being embedded in Java UI code.

## How to use

### Create and view a note

1. Open the app. The current storage is displayed under **CURRENT STORAGE**.
2. Choose either **SQLite · Primary** or **SharedPreferences · Secondary**.
3. Tap **+ New note**, enter a non-empty **Title** and **Content**, and tap **Save note**.
4. The note appears in the list for that storage method. Tap its card to open it again.

### Edit or delete

- Open a note, change the title or content, and tap **Save note**. The note keeps its ID and receives a new modification timestamp.
- Open a note and tap **Delete**; confirm to remove it **only from the selected store**.
- If you type changes and try to leave without saving, choose **Keep editing** or **Discard**. An empty new note or an unchanged existing note can close without a warning.

### Switch storage

Switching the spinner changes which set of notes you see and edit; it **does not transfer notes**. The chosen storage selection is remembered separately in UI preferences. You can create a secondary-only note without adding it to SQLite.

## Synchronization: SQLite → SharedPreferences

Tap **Sync →** on the main screen and confirm. The direction is always **SQLite (primary) to SharedPreferences (secondary)**, regardless of which store is currently selected in the spinner.

```text
SQLite (primary)  ── manual Sync → ──>  SharedPreferences (secondary)
```

The application matches notes using their stable UUIDs and applies the following rules:

| Situation | Result in SharedPreferences |
| --- | --- |
| SQLite note ID is not present | Insert the SQLite note. |
| Both copies are identical (including timestamp) | Leave it unchanged. |
| Same ID; SQLite copy has a strictly later `updatedAt` | Update the destination copy from SQLite. |
| Same ID; destination copy is newer | Preserve the destination copy and count a conflict. |
| Same ID; timestamps tie, but note data differs | Preserve the destination copy and count a conflict. |
| Note exists only in SharedPreferences | Preserve it. |
| Note was deleted from SQLite | **Do not** automatically delete the destination copy. |

After synchronization, the app reports numbers of **added**, **updated**, **unchanged**, and **conflicting** notes. Conflicts are not resolved automatically. There is **no reverse sync**, automatic synchronization, cloud backup, or per-note conflict-resolution screen.

**Practical example:** Create a note in SQLite and synchronize it. Edit the copied note in SharedPreferences. Synchronize again: if its destination timestamp is newer than the SQLite timestamp, the secondary edit remains and a conflict is reported. If SQLite is later edited and becomes strictly newer, a later sync can update that secondary copy.

> **Timestamp limitation:** modification times come from the device clock (`System.currentTimeMillis()`). Timestamp ties or clock adjustments may create ambiguous conflicts. The app conservatively retains the secondary copy instead of overwriting it in those cases.

## Architecture

The project has a deliberately small Java/XML structure and uses Android's built-in persistence APIs (no ORM and no third-party calculation/storage framework).

| Component | Responsibility |
| --- | --- |
| `MainActivity` | Show the current store's list, handle storage selection, open the editor, confirm and trigger synchronization. |
| `NoteEditorActivity` | Create/edit/delete forms, validation, Intent extras, Back navigation with unsaved-change checks. |
| `Note` | Immutable entity: `id`, `title`, `content`, and `updatedAt`. |
| `NoteStore` | Shared CRUD interface (`getAll`, `getById`, `save`, `delete`). |
| `SQLiteNoteStore` | Primary repository using `SQLiteOpenHelper` and the `notes_primary.db` database. |
| `SharedPreferencesNoteStore` | Secondary repository storing a JSON array under the `notes_json_v1` key in private preferences. |
| `NoteSyncPlanner` | Pure-Java, independently unit-tested synchronization/merge policy and result counts. |
| `NoteValidator` | Pure-Java, field-specific validation for title and content. |
| `NoteDraftGuard` | Pure-Java comparison of current form values with the original saved note. |
| `NoteListAdapter` | Render note cards in the Android `ListView`. |
| `StorageChoice` | Represent the two storage choices consistently. |
| `InsetsHelper` | Adjust UI padding for Android system bars. |

### Activity flow and data model

- `MainActivity` starts `NoteEditorActivity` via an **explicit Intent**, passing the chosen store and (when editing) the note ID. The editor loads and saves through the corresponding repository.
- Notes receive a UUID when created; an edit preserves the ID and updates `updatedAt` in milliseconds.
- Both repositories expose the same `NoteStore` CRUD contract, so the UI can switch between storage implementations without duplicating the editing logic.
- SQLite uses a `notes` table with `id TEXT PRIMARY KEY`, `title`, `content`, and `updated_at`; listing sorts by most recent update.
- SharedPreferences stores all secondary notes as a JSON array in an app-private preferences file and writes updates with synchronous `commit()` to detect persistence failures.
- Storage errors are logged and shown to the user instead of silently resetting stored notes. Unreadable secondary JSON is reported as an error rather than overwritten with an empty list.
- Android UI text is defined in `app/src/main/res/values/strings.xml`; colors, shapes, and layouts are in `res/`.

### Project layout

```text
app/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    java/com/example/madtlab5/
      MainActivity.java
      NoteEditorActivity.java
      Note.java
      NoteStore.java
      SQLiteNoteStore.java
      SharedPreferencesNoteStore.java
      NoteSyncPlanner.java
      NoteValidator.java
      NoteDraftGuard.java
      NoteListAdapter.java
      StorageChoice.java
      InsetsHelper.java
    res/
      layout/         # Main list, editor, note row
      drawable/       # Button, card, and input backgrounds
      values/         # UI strings, colors, theme
  src/test/java/com/example/madtlab5/
    NoteTest.java
    NoteValidatorTest.java
    NoteSyncPlannerTest.java
    NoteDraftGuardTest.java
gradle/wrapper/       # Gradle Wrapper distribution information
README.md
```

## Requirements and setup

- **Android Studio** with Android SDK Platform **35** installed (`compileSdk = 35`).
- **JDK 17** as a compatible Gradle JDK; the app compiles for Java 17.
- Android Gradle Plugin **8.9.2**, Gradle Wrapper **8.11.1** (the included Wrapper is preferred over a system installation).
- Android device or emulator running **API 23 or newer** (`minSdk = 23`). The reviewed application was reported working on **Pixel 4 API 34**.
- Internet access may be needed initially to download Gradle dependencies; running the installed app itself is offline.

### Open and run in Android Studio

1. Extract the project ZIP to an ordinary directory, for example `C:\Users\<you>\AndroidStudioProjects\MADT_Lab5_Notes_Documented`. Avoid special characters such as `&` in the Windows directory path, which can interfere with `gradlew.bat`.
2. Open **Android Studio → Open** and select the folder containing `settings.gradle.kts`.
3. Let **Gradle Sync** complete and confirm that Android SDK Platform 35 is available.
4. Select a device/emulator (the student used **Pixel 4 API 34**).
5. Run the **app** configuration.

### Build and run unit tests

In **PowerShell**, from the project root:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

On macOS/Linux:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

The debug APK is built at `app/build/outputs/apk/debug/app-debug.apk`. The local unit-test results can be found under `app/build/reports/tests/testDebugUnitTest/` after running the test task.

## Testing and verification

The repository includes **34 JUnit 4 unit-test methods** across four test classes:

| Test class | Methods | What it covers |
| --- | ---: | --- |
| `NoteTest` | 5 | Note identity, normalization, and required-field invariants. |
| `NoteValidatorTest` | 8 | Valid input, null/empty/whitespace title and content. |
| `NoteSyncPlannerTest` | 13 | Inserts, preservation of secondary-only notes, timestamp conflicts, updates, idempotence, and input safety. |
| `NoteDraftGuardTest` | 8 | Untouched vs. modified drafts, blank/new drafts, whitespace normalization, and clearing saved text. |

**Validation:** All **34 Java test methods** passed in a temporary host-side runner using JUnit-compatible shims (not an Android Gradle build). Manual checks on **Pixel 4 API 34** were reported successful for CRUD, both storage options, synchronization, and unsaved-change protection. The available successful Gradle build log was from before the final code changes; use the command above to verify a fresh build of the reviewed source.

### Recommended on-device regression checks

1. In SQLite, create a note, reopen it, edit it, and delete a note after confirmation.
2. In SharedPreferences, repeat CRUD and verify that SQLite notes do not change when switching stores.
3. Try blank and whitespace-only values in either required field. Saving must fail with field-specific feedback.
4. Sync a new SQLite note; confirm it appears in SharedPreferences alongside secondary-only notes.
5. Edit the copied SharedPreferences note; sync again and confirm the newer secondary version is retained with a reported conflict.
6. Check **Keep editing** and **Discard** for an unsaved new note and an edited note, using both the in-app and Android system Back controls.
7. Force-stop and relaunch the app. Verify notes persist in each store and the previously selected storage is restored.
8. Rotate the device and inspect layouts/system-bar spacing.

**Test scope:** These are JVM unit tests of domain rules; the repository does not include Android instrumentation/UI tests or real SQLite/SharedPreferences integration tests. Those storage and lifecycle behaviors require an emulator/device check.

## Known limitations

- Sync is deliberately **one-way and user-triggered**; it does not propagate deletions, resolve conflicts automatically, or back up data to a server.
- SQL queries and SharedPreferences JSON reads/writes run on the UI thread. Very large note collections could make the interface less responsive.
- JSON in SharedPreferences is suitable for demonstrating an alternate store, not for high-volume data. Data remains local and is removed if the app is uninstalled or its storage is cleared; Android application backup is disabled in the manifest.
- Conflict timestamps depend on the device clock; equal timestamps are treated cautiously.
- No database migration beyond the initial SQLite schema (`version = 1`) is implemented.
- Automated tests cover domain behavior, not Android persistence and lifecycle integration.

## Engineering notes

The two persistence backends implement the same CRUD interface, while synchronization and unsaved-draft detection live in separate Java classes. This keeps storage decisions and merge rules independently testable instead of coupling them to the Activities.
