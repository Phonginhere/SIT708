# SIT708 Credit Task 8.1 — LLM ChatBot

A Jetpack Compose Android chatbot. Users sign in with a username, then hold a multi-turn conversation with **Google Gemini 2.5 Flash**. Every message is persisted in a Room database and scoped per username, so chats survive app restarts and each account sees only its own thread. Built in Kotlin with Material 3.

---

## Task Requirements Coverage

The five requirements from the brief, each mapped to where it lives in the code:

| # | Brief requirement | Where it is implemented |
| --- | --- | --- |
| 1 | Authentication screen — user enters a username to proceed | [LoginScreen.kt](app/src/main/java/com/example/c81/ui/login/LoginScreen.kt) + [LoginViewModel.kt](app/src/main/java/com/example/c81/ui/login/LoginViewModel.kt). The `Go` button is enabled only when `username.isNotBlank()`. |
| 2 | Chat interface — send and receive messages from the chatbot | [ChatScreen.kt](app/src/main/java/com/example/c81/ui/chat/ChatScreen.kt) + [ChatViewModel.kt](app/src/main/java/com/example/c81/ui/chat/ChatViewModel.kt) → [ChatRepository.kt](app/src/main/java/com/example/c81/data/repository/ChatRepository.kt) → [GeminiClient.kt](app/src/main/java/com/example/c81/data/remote/GeminiClient.kt). |
| 3 | UI matches the supplied wireframe | Login: cyan vertical gradient, "Welcome, Lets Chat!" heading, white username field, lime-green `Go` button. Chat: bot avatar (left) + user-initial avatar (right), grey rounded bubbles, send button on the right of the input. Wireframe palette in [Color.kt](app/src/main/java/com/example/c81/ui/theme/Color.kt). |
| 4 | Chat history persisted using SQLite/Room | Room database `chatbot.db` — [AppDatabase.kt](app/src/main/java/com/example/c81/data/local/AppDatabase.kt), [MessageEntity.kt](app/src/main/java/com/example/c81/data/local/MessageEntity.kt), [MessageDao.kt](app/src/main/java/com/example/c81/data/local/MessageDao.kt). Every row carries a `username` column, so each account has an isolated history. |
| 5 | Timestamps on every message bubble | [MessageBubble.kt](app/src/main/java/com/example/c81/ui/chat/components/MessageBubble.kt) renders the `timestamp: Long` field of [MessageEntity.kt](app/src/main/java/com/example/c81/data/local/MessageEntity.kt) as 11sp gray `HH:mm` text directly under the bubble. |

---

## Features

- Two-screen flow: Login → Chat
- Username-only authentication; per-user message isolation
- Multi-turn chat with Google Gemini 2.5 Flash via the official Google Generative AI Kotlin SDK
- Full prior conversation replayed on every turn so the model retains context across messages
- Persistent Room storage — chats survive app restarts and process death
- Timestamp shown beneath every bubble (`HH:mm`, gray, 11sp)
- "Typing…" indicator while waiting on Gemini
- Clear-chat action (top-bar trash icon) with an `AlertDialog` confirmation
- Auto-scroll to the newest message
- Material 3 theme with a custom cyan/green palette matching the wireframe
- Top-bar back arrow returns to Login with the chat removed from the back stack (acts as a logout)

---

## Screens

### 1) Login — [LoginScreen.kt](app/src/main/java/com/example/c81/ui/login/LoginScreen.kt)
- Vertical `Brush.verticalGradient(CyanTop → CyanBottom)` background
- White heading: **"Welcome, Lets Chat!"**
- White pill-shaped `TextField` for username (rounded 8.dp, transparent borders)
- Lime-green `Go` button — disabled until `username.isNotBlank()`
- On success, [AppNavHost.kt](app/src/main/java/com/example/c81/ui/navigation/AppNavHost.kt) navigates to `chat/{username}` with `popUpTo(LOGIN) { inclusive = true }`, so back from Chat returns to Login (logout)

### 2) Chat — [ChatScreen.kt](app/src/main/java/com/example/c81/ui/chat/ChatScreen.kt)
- `Scaffold` with a cyan `TopAppBar` showing the active `username`
- Top-bar actions: back-to-login icon (logout) and a delete-chat icon that opens a confirmation `AlertDialog`
- `LazyColumn` of [MessageBubble.kt](app/src/main/java/com/example/c81/ui/chat/components/MessageBubble.kt):
  - User messages right-aligned, `UserBubble` grey, with a 36dp circular avatar showing the uppercase first letter of the username
  - Bot messages left-aligned, `BotBubble` grey, with a 36dp circular avatar showing the `SmartToy` (robot) icon
  - First bubble is the welcome message ("Welcome {username}!")
  - Each bubble has a 11sp gray `HH:mm` timestamp directly beneath it
- "…" typing bubble appears below the list while `isSending = true`
- [ChatInput.kt](app/src/main/java/com/example/c81/ui/chat/components/ChatInput.kt): white text field plus a cyan send button — both disabled while the input is blank or while a Gemini request is in flight
- The list auto-animates to the last item via `listState.animateScrollToItem(...)` whenever a new message or the typing indicator appears

---

## LLM Integration

### Model and client
- Model: `gemini-2.5-flash`
- Library: `com.google.ai.client.generativeai:generativeai:0.9.0` (the official Google Generative AI Kotlin SDK — no Retrofit / OkHttp required, the SDK handles HTTP internally)
- Client: [GeminiClient.kt](app/src/main/java/com/example/c81/data/remote/GeminiClient.kt) holds a single `GenerativeModel`, configured with `BuildConfig.GEMINI_API_KEY`

### Prompt strategy
For each user turn, [ChatRepository.kt](app/src/main/java/com/example/c81/data/repository/ChatRepository.kt):
1. Persists the user message immediately (so the UI reflects it without waiting on the model)
2. Reads the prior conversation for that username from Room
3. Maps each prior row to a `content(role = "user" | "model")` block and calls `generativeModel.startChat(history)`
4. Sends the new user text via `chat.sendMessage(...)`
5. Persists the bot reply

This means **every send replays the full per-user history**, giving Gemini the full context and keeping multi-turn conversations coherent.

### Error handling
Any exception inside `GeminiClient.sendMessage` is caught and returned as the string `"Error: {localizedMessage ?: "could not reach Gemini"}"`. The repository persists this string as a normal bot message so failures appear inline in the chat history rather than crashing the UI.

### Async + state flow
`ChatViewModel.onSend()` launches a coroutine that flips `isSending = true`, calls the repository, then flips it back to `false`. The UI observes messages reactively through the DAO's `Flow<List<MessageEntity>>` exposed as a `StateFlow` from the view model.

---

## Conversation Storage (Requirement #4)

Persistence lives entirely in [data/local/](app/src/main/java/com/example/c81/data/local/) and is built lazily in [ChatBotApplication.kt](app/src/main/java/com/example/c81/ChatBotApplication.kt) so the database is a singleton for the lifetime of the process.

- **Database** — [AppDatabase.kt](app/src/main/java/com/example/c81/data/local/AppDatabase.kt): Room database `chatbot.db`, version 1, exporting `messageDao()`
- **Entity** — [MessageEntity.kt](app/src/main/java/com/example/c81/data/local/MessageEntity.kt): `id` (auto-increment primary key), `username`, `content`, `isFromUser` (boolean), `timestamp` (epoch millis)
- **DAO** — [MessageDao.kt](app/src/main/java/com/example/c81/data/local/MessageDao.kt):
  - `observeMessages(username): Flow<List<MessageEntity>>` — reactive stream that drives the chat UI
  - `getMessagesForUser(username): List<MessageEntity>` — one-shot read used to build Gemini's history
  - `insert(message: MessageEntity): Long`
  - `clearForUser(username)` — backs the trash-icon action
- **Repository** — [ChatRepository.kt](app/src/main/java/com/example/c81/data/repository/ChatRepository.kt): the single orchestration entry point, wrapping the DAO and the Gemini client

Because every row is keyed by `username`, two different users on the same device get fully independent threads.

---

## Input & Validation Rules

- **Login**: `Go` is disabled until the username is non-blank (validation lives in [LoginViewModel.kt](app/src/main/java/com/example/c81/ui/login/LoginViewModel.kt) as the `canProceed` derived property). No password.
- **Chat input**: the send button is disabled until the message text is non-blank, and additionally disabled while a request is in flight (`isSending = true`).

---

## Output Formatting

- **User bubble** — right-aligned, `UserBubble` `#E2E2E2`, 36dp circular avatar showing the uppercase first letter of the username
- **Bot bubble** — left-aligned, `BotBubble` `#EAEAEA`, 36dp circular avatar showing the Material `SmartToy` icon
- **Timestamp** — 11sp gray `HH:mm` caption directly beneath each bubble, formatted via `SimpleDateFormat("HH:mm", Locale.getDefault())`
- **Typing indicator** — bot-style "…" bubble while waiting on Gemini
- **Errors** — surfaced as a normal bot message prefixed with `Error: …` so they live in the chat history and remain visible after restart

---

## UI Notes

- Material 3 theme defined in [Theme.kt](app/src/main/java/com/example/c81/ui/theme/Theme.kt). Dynamic color (Material You) is disabled (`dynamicColor = false`) so the wireframe palette in [Color.kt](app/src/main/java/com/example/c81/ui/theme/Color.kt) always applies, regardless of device wallpaper.
- Color tokens: `CyanTop` `#00C4F4`, `CyanBottom` `#00A6D6`, `ButtonGreen` `#6BFF45`, `UserBubble` `#E2E2E2`, `BotBubble` `#EAEAEA`.
- `LazyListState.animateScrollToItem(...)` keeps the newest message visible whenever the list grows or the typing indicator appears.
- `contentDescription` is set on the top-bar back and delete icons for screen-reader support.
- No dead ends: the system back gesture and the top-bar back arrow both return to Login with the chat popped from the back stack.

---

## Project Structure

```
app/src/main/java/com/example/c81/
├── ChatBotApplication.kt           # Application class — manual DI container (DB, Gemini client, repository)
├── MainActivity.kt                 # Compose entry point + Material 3 theme
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database (chatbot.db, v1)
│   │   ├── MessageDao.kt           # Flow + suspend message queries
│   │   └── MessageEntity.kt        # @Entity message row
│   ├── remote/
│   │   └── GeminiClient.kt         # Wraps GenerativeModel.startChat
│   └── repository/
│       └── ChatRepository.kt       # send + persist orchestration
└── ui/
    ├── chat/
    │   ├── ChatScreen.kt
    │   ├── ChatViewModel.kt
    │   └── components/
    │       ├── ChatInput.kt
    │       └── MessageBubble.kt
    ├── login/
    │   ├── LoginScreen.kt
    │   └── LoginViewModel.kt
    ├── navigation/
    │   └── AppNavHost.kt           # Two routes: login and chat/{username}
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Tech Stack

- **Kotlin** 2.1.0 + **Jetpack Compose** (BOM 2024.12.01) + **Material 3**
- **Navigation Compose** 2.8.5 — two routes: `login` and `chat/{username}`
- **Lifecycle ViewModel Compose** 2.8.7
- **Room** 2.6.1 (runtime + ktx + KSP compiler) for chat persistence
- **Google Generative AI Kotlin SDK** 0.9.0 (`com.google.ai.client.generativeai`) for the LLM client
- **Kotlin Coroutines** 1.9.0 for async LLM calls and DB writes
- **Material Icons Extended** for the bot, send, back, and delete glyphs
- **AndroidX Core KTX** 1.15.0, **Activity Compose** 1.9.3
- **Min SDK** 26, **Target SDK** 35, **JDK** 17, **AGP** 8.13.2, **KSP** 2.1.0-1.0.29

---

## How to Run

1. Clone the repository and open the project in Android Studio (JDK 17, Android SDK 35 installed)
2. Get a Gemini API key from **Google AI Studio**
3. Open [local.properties](local.properties) and add:
   ```
   GEMINI_API_KEY=your_key_here
   ```
   (`local.properties` is git-ignored via [.gitignore](.gitignore) — keys are not committed)
4. Sync Gradle. The key is plumbed via `buildConfigField("String", "GEMINI_API_KEY", ...)` in [app/build.gradle.kts](app/build.gradle.kts) and read as `BuildConfig.GEMINI_API_KEY` in [ChatBotApplication.kt](app/src/main/java/com/example/c81/ChatBotApplication.kt)
5. Run the `app` module on an emulator or device with Android 8.0+ (API 26+)
6. The app requires the `INTERNET` permission, declared in [AndroidManifest.xml](app/src/main/AndroidManifest.xml)

---

## Responsible Use of AI

This app uses **Google Gemini 2.5 Flash** at runtime to generate every bot reply in the chat screen. In addition, an LLM was used as a coding assistant while drafting parts of the source and this README — consistent with the brief's "Responsible Use of AI" clause.

---

## Course

SIT708 — Credit Task 8.1: LLM ChatBot.
