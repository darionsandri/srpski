# Srpski — Serbian learning app (v0.1)

Duolingo-style Serbian learning app. Latin script (latinica) only. Offline, no accounts.

## Open and run
1. Open this folder in Android Studio (Ladybug or newer).
2. Let Gradle sync. If your Android Studio bundles different versions, accept its suggested
   upgrades for AGP/Kotlin in `gradle/libs.versions.toml` — the code doesn't depend on exact versions.
3. Run on a device/emulator with Google TTS installed (Settings > Accessibility > Text-to-speech;
   install the Serbian voice for listening exercises).

## What's implemented (milestones M1–M3 + parts of M4/M5)
- Content schema + unit 1 (Osnove 1): 3 lessons, 28 vocab items, 36 exercises
- Exercise types: multiple choice, word bank, typing, listening (TTS), fill-in-the-blank
- Grader with strict/lenient diacritic modes, đ→dj handling, and 1-letter typo tolerance
- Wrong answers re-queued once at the end of the session (Duolingo-style)
- XP, daily streak, lesson completion, unit gating
- Leitner spaced repetition: due vocab appended to sessions as review exercises
- Settings toggle: strict accents on/off (your default: strict)

## Where things live
- `app/src/main/assets/content/` — all lesson content (JSON). Add units here; no code changes needed
  beyond listing the unit in `course.json` (file naming: `unitNN.json`).
- `domain/` — grader, SRS, session builder. Pure Kotlin; unit tests in `app/src/test/`.
- `data/` — Room (progress, SRS cards, daily XP) + JSON content loading.
- `ui/` — Compose screens.

## Run the domain tests
./gradlew testDebugUnitTest

## Roadmap (from the planning docs)
- M5 remainder: weak-words practice mode, grammar-targeted drills
- M6: "Real Serbian" YouTube clip exercises (schema fields already present on ExerciseDef)
- M7: backup/restore, polish

## Note on the Gradle wrapper
This zip ships without the wrapper binary. Android Studio will offer to use its bundled Gradle on first open — accept that, or run `gradle wrapper` once if you have Gradle installed.
