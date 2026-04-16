# Pass5.1 - Sports News App

A modern Android sports news application built with Kotlin that delivers curated sports stories across multiple categories with a clean, Material Design interface.

## Features

- **Top Stories Carousel** - Horizontally scrollable featured stories section
- **Latest News Feed** - Vertical list of the most recent sports articles
- **Category Filtering** - Filter news by sport: Cricket, Basketball, Football, Tennis, Horse Racing
- **Real-Time Search** - Instantly search articles by title as you type
- **Bookmarks** - Save favorite articles locally for quick access later
- **Article Detail View** - Full article display with related story recommendations
- **Bottom Navigation** - Seamless switching between Home and Bookmarks screens

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0 Nougat) |
| Target SDK | 36 (Android 15) |
| UI | XML Layouts + Material Design 3 |
| Navigation | Fragment-based with bottom navigation |
| Persistence | SharedPreferences (bookmarks) |
| Build System | Gradle (Kotlin DSL) with Version Catalog |

### Key Dependencies

- AndroidX Core 1.18.0
- AndroidX AppCompat 1.7.1
- AndroidX Fragment 1.8.5
- Material Components 1.12.0
- Material Design 3 1.13.0
- RecyclerView 1.3.2
- ConstraintLayout 2.2.1

## Architecture

The app follows a **single-Activity, multi-Fragment** architecture:

```
MainActivity
├── HomeFragment          (news feed with search, categories, and stories)
├── BookmarksFragment     (saved articles)
└── DetailFragment        (full article view with related stories)
```

### Project Structure

```
app/src/main/java/com/example/pass51/
├── MainActivity.kt                  # Host activity & navigation controller
├── fragments/
│   ├── HomeFragment.kt              # Home screen with news feed
│   ├── BookmarksFragment.kt         # Saved articles screen
│   └── DetailFragment.kt            # Article detail view
├── adapters/
│   ├── NewsAdapter.kt               # Latest news RecyclerView adapter
│   ├── FeaturedMatchAdapter.kt      # Top stories carousel adapter
│   ├── CategoryAdapter.kt           # Category filter chips adapter
│   └── RelatedStoriesAdapter.kt     # Related articles adapter
├── data/
│   ├── NewsItem.kt                  # Data model
│   ├── DummyData.kt                 # Mock data source (12 articles)
│   └── BookmarkManager.kt           # SharedPreferences bookmark storage
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 11+
- Android SDK 36

### Build & Run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Pass51
   ```

2. Open the project in Android Studio.

3. Sync Gradle and let dependencies download.

4. Run on an emulator or physical device (API 24+):
   ```bash
   ./gradlew installDebug
   ```

## How It Works

### Home Screen
The home screen displays a search bar at the top, followed by category filter chips, a horizontally scrollable top stories section, and a vertical latest news list. Selecting a category or typing in the search bar filters both sections in real time.

### Article Detail
Tapping any article opens a detail view showing the full title, category badge, article content, and up to 4 related stories from the same category. Users can bookmark/unbookmark articles with the star button.

### Bookmarks
Bookmarked article IDs are persisted in SharedPreferences. The Bookmarks tab shows all saved articles and updates automatically when returning from the detail screen.

## Sports Categories

| Category | Coverage |
|---|---|
| Cricket | Match results, tournament updates, player highlights |
| Basketball | NBA news, game recaps, player transfers |
| Football | League standings, match previews, transfer news |
| Tennis | Grand Slam updates, rankings, match results |
| Horse Racing | Race results, upcoming events, industry news |

## Build Configuration

```
compileSdk = 36
minSdk = 24
targetSdk = 36
Kotlin = 2.2.10
AGP = 9.1.1
Java compatibility = 11
```
