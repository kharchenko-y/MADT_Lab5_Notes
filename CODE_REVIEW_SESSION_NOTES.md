# MADT Lab #5 — Independent Code Review Session Notes

**Date:** 2026-11-05  
**AI session:** Independent Code Review (separate from Code Generation, README and GitHub)  
**Input:** `MADT_Lab5_Notes_CodeGeneration (1).zip`  
**App:** Java/XML Android Notes Storage; SQLite primary, SharedPreferences JSON secondary

## Student input and boundaries

The student reported a successful Android build and functional checks on **Pixel 4 API 34**, and explicitly requested an independent inspection of architecture, CRUD, both stores, synchronization, form validation, error handling, lifecycle and tests. The student authorized **exactly two highest-priority fixes**, asked to preserve existing functionality, prohibited manual code editing, and reserved README/GitHub work for separate AI sessions.

## Review findings

- **Architecture / CRUD:** `NoteStore` is shared by `SQLiteNoteStore` and `SharedPreferencesNoteStore`. List and editor activities exchange storage choice and note ID through an explicit Intent. Both backends implement read/create/update/delete; SQLite writes use a primary-key conflict strategy, and SharedPreferences writes use synchronous `commit()` so write failure can be surfaced.
- **Validation:** `NoteValidator` rejects blank/whitespace-only titles and bodies, and `Note` validates the same invariant. This is appropriate for the assignment.
- **Data integrity:** Both stores retain independent notes; the sync planner previously overwrote a matching secondary note regardless of whether the secondary copy had been edited more recently.
- **Editor UX / lifecycle:** Back button formerly called `finish()` unconditionally. System Back likewise had no save/discard confirmation. Android edit fields already request state freezing for configuration changes, but closing an editor could discard an unsaved draft.
- **Error handling:** Storage calls and sync are caught and reported. There is no recovery UI for corrupted secondary JSON; the code deliberately raises an error rather than wiping data.
- **Tests:** Original ZIP contains 23 pure-Java JUnit tests covering the model, validator and merging policy. No Android instrumentation tests for real persistence were included; this remains a test gap.

## Highest-priority fix 1 — avoid stale synchronization overwriting newer notes

**Files:** `NoteSyncPlanner.java`, `MainActivity.java`, `strings.xml`, `NoteSyncPlannerTest.java`.

**Before:** A SQLite note always overwrote the corresponding SharedPreferences record, even when its `updatedAt` was earlier. This could destroy a recently edited secondary note. Although the old confirmation dialog described SQLite precedence, the behavior posed a material data-loss risk.

**After:** Sync remains **one-directional SQLite → SharedPreferences**. It inserts missing primary notes, updates matching secondary notes only when SQLite is **strictly newer**, retains identical notes, and preserves more-recent or same-timestamp secondary versions as **conflicts**. The confirmation and resulting Toast disclose this policy and the number of conflicts. Secondary-only notes are still retained. Nothing is copied back to SQLite. Conflicts are not resolved automatically.

**Regression tests:** Newer secondary wins; newer SQLite replaces older secondary; equal-time differing data is preserved as a conflict; an unrelated insert proceeds even alongside a conflict. Existing merge tests remain.

## Highest-priority fix 2 — prevent accidental unsaved editor data loss

**Files:** `NoteEditorActivity.java`, new `NoteDraftGuard.java`, `strings.xml`, new `NoteDraftGuardTest.java`.

**Before:** Editor Back closed immediately, discarding typed new notes and unsaved changes to existing notes without prompting.

**After:** New pure-Java `NoteDraftGuard` compares current trimmed inputs to original saved content. A modified or unfinished draft triggers **Discard / Keep editing** confirmation. An empty new editor or unchanged existing note can close directly. Both the in-app Back button and system Back are covered: Android 13+ uses platform `OnBackInvokedCallback` with unregister on Activity destruction; earlier Android versions retain `onBackPressed()` fallback. Save and delete behavior are unchanged.

**Regression tests:** 8 new tests for new draft, untouched edit, changed title/body, cleared text, null and whitespace handling.

## Verification performed in this review environment

- **34/34 actual Java unit-test methods passed**, compiled and executed against minimal temporary JUnit-compatible assertion/annotation shims. The shims and runner are **not** added to the submitted Android project. This is a host-side execution of the JUnit test source, not a Gradle/JUnit or Android emulator run.
- Java domain classes (`Note`, `NoteValidator`, `NoteSyncPlanner`, `NoteDraftGuard`) compiled with JDK 21.
- Android resource XML and Java resource ID references statically validated.
- Only the two changes above were implemented. README, Gradle build scripts, manifest, storage implementations and unrelated application logic were not changed.

**Not independently verified:** Full Android SDK/Gradle build and execution on Pixel 4 API 34, real SQLite/SharedPreferences CRUD/integration, Back navigation dialogs on device, UI screenshots. The student's previous successful build covered the **pre-review** version, not these newly changed files.

## Important residual risks (reviewed but deliberately not fixed)

1. SQLite queries, SharedPreferences JSON serialization and sync currently run on the main thread; very large datasets could cause UI pauses/ANRs. An asynchronous repository design is a separate, broader change.
2. The two backing stores are not covered by Android integration tests. Pure Java merge-model tests are not a substitute for real persistence checks.
3. Timestamps derive from `System.currentTimeMillis()`: ties and clock changes can produce unresolved conflicts. The safer policy is to preserve ambiguous secondary data and report it rather than overwrite it.
4. Conflict handling does not offer a per-note manual resolution screen; users can inspect/edit notes in the selected store.

## Mandatory on-device validation before documentation and GitHub

On **Pixel 4 API 34** (or Pixel 6 API 35):

1. Run `:app:assembleDebug :app:testDebugUnitTest` with the standard Gradle Wrapper.
2. Create, edit, view, delete and relaunch notes in each storage method; verify persisted contents after force-stop.
3. SQLite `id=a` at timestamp older than a modified SharedPreferences copy cannot be crafted via UI IDs directly; for an ordinary UI regression, create SQLite note, sync, edit the secondary copy, then sync again: check secondary edit is **preserved** and conflict count increments.
4. Edit an existing note and press in-app Back, choose **Keep editing**, then save; confirm changes persisted. Repeat with the **system Back** gesture/button, and test a new unfinished draft.
5. Verify sync still copies fresh SQLite notes to secondary, preserving unrelated secondary-only notes.
6. Check rotation and reopening after a save; confirm no overlaps with Android system bars.

## Session handoff

The resulting Android project ZIP is the handoff to a separate Documentation Generation AI session. Create `README.md` there; publication and final `@ListOfsessions` report are still later steps. The original generation records remain historical descriptions; their earlier SQLite-always-wins policy is **superseded** by this review.
