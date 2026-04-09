# SIT305 Quiz App - Android Multiple-Choice Quiz

A Jetpack Compose Android app that lets users attempt a 5-question multiple-choice quiz on Android development topics, with real-time progress tracking, visual answer feedback, and a final results screen.

---

## Features
- 5 Android knowledge questions with 3 answer options each
- Visual answer feedback: blue highlight on selection, green (correct) / red (wrong) on submit
- Locked answers after submission — cannot re-select for the current question
- Real-time progress bar + question counter (e.g., `2/5`)
- Final score screen displaying `score/total` after the last question
- Session persistence: user's name is retained when starting a new quiz
- Dark Mode / Light Mode toggle accessible on all three screens

---

## Quiz Question Categories

### 1) Android Basics
- Question: "Which language is officially recommended for Android development?"
- Options: Java | **Kotlin** | Swift
- Correct answer: Kotlin

### 2) Activity Lifecycle
- Question: "Which callback is called when an Activity first becomes visible to the user?"
- Options: onCreate() | onResume() | **onStart()**
- Correct answer: onStart()

### 3) Layouts
- Question: "Which layout arranges children in a single row or column?"
- Options: **LinearLayout** | ConstraintLayout | FrameLayout
- Correct answer: LinearLayout

### 4) Intents
- Question: "Which type of Intent explicitly names the target component?"
- Options: Implicit Intent | **Explicit Intent** | Broadcast Intent
- Correct answer: Explicit Intent

### 5) Storage
- Question: "Which Android component is best for storing simple key-value pairs?"
- Options: SQLite | **SharedPreferences** | ContentProvider
- Correct answer: SharedPreferences

---

## Input & Validation Rules
- **Name field**: Empty or blank names are rejected; an inline error message is shown beneath the field
- **Name persistence**: When returning from the Results screen via "Take New Quiz", the name field is pre-filled with the previous value
- **Answer selection**: Tapping Submit with no option selected is a no-op (returns early, no state change)
- **Post-submit lock**: All answer buttons are disabled (`enabled = false`) after submission — the user cannot change their answer for the current question
- **State reset per question**: `selectedIndex` and `hasSubmitted` reset to defaults when advancing to the next question

---

## Output Formatting
- **Progress counter**: `{currentIndex + 1}/{total}` — e.g., `1/5`, `3/5` (increments after submit)
- **Progress bar**: Float fraction — `(currentIndex + 1) / total` after submitting, `currentIndex / total` before
- **Final score**: Displayed as `score/total` at 48sp — e.g., `4/5`
- **Submit button label**: Cycles through `Submit` → `Next` → `Finish` (on the last question after submitting)

---

## UI Notes
- **Pre-submit selected**: Light Blue `#BBDEFB` with dark text `#1A1A1A`
- **Post-submit correct**: Green `#4CAF50` with white text
- **Post-submit wrong**: Red `#F44336` with white text
- **Unselected / neutral**: Material3 `surfaceVariant` color (adapts to theme)
- **Theme toggle**: Sun ☀️ / Moon 🌙 emoji paired with a Switch component, top-right corner of every screen
- **Dark mode state**: Hoisted in `MainActivity`, passed as `isDarkMode` + `onThemeToggle` lambda to all composables — theme applies instantly and persists across navigation
- **Navigation**: Welcome → Quiz → Results; "Take New Quiz" returns to Welcome with username pre-filled; "Finish" calls `finish()` to close the app

---

## Project Structure
```
app/src/main/java/com/example/credittask31/
├── MainActivity.kt          # NavHost setup, dark mode state, route definitions
├── WelcomeScreen.kt         # Name input, validation, START button
├── QuizScreen.kt            # Quiz logic, answer buttons, progress bar, visual feedback
├── ResultScreen.kt          # Score display, Take New Quiz / Finish buttons
├── Question.kt              # Data class: title, detail, options, correctIndex
├── QuizData.kt              # Hardcoded list of 5 quiz questions
└── ui/theme/
    ├── Theme.kt             # CreditTask31Theme composable, light/dark color schemes
    ├── Color.kt             # Color definitions
    └── Type.kt              # Typography configuration
```

---

## How to Run
1. Open the project in Android Studio (Hedgehog or later recommended)
2. Select a device or emulator running API 24+
3. Run the `app` module
