# Pass5.1 iStream

An Android application for streaming YouTube videos with user authentication and personal playlist management. Built with Kotlin, Room Database, and Fragment-based navigation.

## Features

- **User Authentication** -- Register and log in with username/password credentials. Duplicate username detection on sign-up.
- **YouTube Video Streaming** -- Play YouTube videos in-app via an embedded WebView player. Supports multiple YouTube URL formats.
- **Playlist Management** -- Save videos to a personal playlist, browse saved items, and tap to replay them.
- **Per-User Data** -- Each user has their own isolated playlist, backed by a local Room database with foreign-key relationships.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target / Compile SDK | 36 |
| UI | XML Layouts + Material Design 3 |
| Navigation | Fragment transactions |
| Database | Room (SQLite ORM) |
| Async | Kotlin Coroutines (`lifecycleScope`) |
| Video Playback | WebView with HTML5 iframe |
| Build System | Gradle (Kotlin DSL) with version catalog |

## Project Structure

```
app/src/main/java/com/example/pass51_istream/
├── MainActivity.kt                 # Entry point, hosts fragment container
├── database/
│   ├── AppDatabase.kt              # Room database singleton
│   ├── User.kt                     # User entity (PK: id)
│   ├── PlaylistItem.kt             # Playlist entity (PK: id, FK: userId → User.id)
│   ├── UserDao.kt                  # User data access object
│   └── PlaylistDao.kt              # Playlist data access object
├── fragments/
│   ├── LoginFragment.kt            # Login screen
│   ├── SignUpFragment.kt           # Registration screen
│   ├── HomeFragment.kt             # Video player & controls
│   └── PlaylistFragment.kt         # Saved videos list
├── adapter/
│   └── PlaylistAdapter.kt          # RecyclerView adapter for playlist
└── ui/theme/
    ├── Color.kt                    # Compose color palette
    ├── Theme.kt                    # Material 3 theme
    └── Type.kt                     # Typography
```

## Database Schema

```
┌──────────────────┐         ┌──────────────────────┐
│      users       │         │    playlist_items     │
├──────────────────┤         ├──────────────────────┤
│ id (PK, auto)    │◄───┐    │ id (PK, auto)        │
│ fullName         │    └────│ userId (FK → users.id)│
│ username         │         │ url                   │
│ password         │         └──────────────────────┘
└──────────────────┘         ON DELETE CASCADE
```

### Users Table

| Column | Type | Constraint |
|---|---|---|
| id | Int | Primary Key, Auto-generate |
| fullName | String | |
| username | String | |
| password | String | |

### Playlist Items Table

| Column | Type | Constraint |
|---|---|---|
| id | Int | Primary Key, Auto-generate |
| url | String | YouTube video URL |
| userId | Int | Foreign Key → users.id (CASCADE delete) |

A user can have many playlist items. When a user is deleted, all their playlist items are automatically removed via CASCADE.

## Navigation Flow

```
LoginFragment ──► HomeFragment ──► PlaylistFragment
      │                │                  │
      ▼                ▼                  │
SignUpFragment     (Logout ► Login)       │
                                          │
      HomeFragment ◄── (select video) ────┘
```

1. App launches on **LoginFragment**.
2. New users navigate to **SignUpFragment**, then return to login.
3. After login, **HomeFragment** lets users play videos, add them to a playlist, or view their playlist.
4. **PlaylistFragment** shows saved videos; tapping one returns to HomeFragment with the selected URL loaded.

## Getting Started

### Prerequisites

- Android Studio (latest stable recommended)
- Android SDK 36
- JDK 11 or higher

### Build & Run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Pass51_iStream
   ```

2. Open the project in Android Studio.

3. Sync Gradle and let dependencies download.

4. Run on an emulator or physical device (API 24+):
   ```bash
   ./gradlew installDebug
   ```

### Permissions

The app requires only one permission, declared in `AndroidManifest.xml`:

- `INTERNET` -- needed to load YouTube videos in the WebView.

## Configuration

| File | Purpose |
|---|---|
| `gradle/libs.versions.toml` | Centralized dependency version catalog |
| `app/build.gradle.kts` | App module build configuration |
| `app/proguard-rules.pro` | ProGuard/R8 rules (minification currently disabled) |

## Dependencies

Core libraries used:

- **AndroidX Core KTX** 1.18.0
- **AndroidX AppCompat** 1.7.1
- **Material Components** 1.12.0
- **Room** 2.7.1 (runtime, KTX, compiler)
- **Fragment KTX** 1.8.5
- **RecyclerView** 1.3.2
- **ConstraintLayout** 2.2.1
- **Lifecycle Runtime KTX** 2.10.0

See [libs.versions.toml](gradle/libs.versions.toml) for the complete version catalog.
