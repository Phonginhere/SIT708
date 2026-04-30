# Task 7.1P — Lost & Found App

A hybrid Fragment + Jetpack Compose Android app for posting and browsing **lost or found items**, backed by a local **Room** database. Users add a posting with a photo (gallery or camera), category, date and contact details, then other users browse the list, filter by category and view full details. Built in Kotlin with Material 3 and dynamic colour on Android 12+.

---

## Features
- Four-screen flow: Home → Create Advert → Items List → Item Detail
- **Lost / Found** post type toggle on every advert (radio group)
- Image capture from **gallery** or **camera** — camera output goes through an Android `FileProvider` URI
- Material 3 `DatePicker` for the date the item was lost or found, formatted as `dd/MM/yyyy`
- Category **filter chips** (All / Electronics / Pets / Wallets) on the list screen
- Reactive list driven by Room `Flow<List<Item>>` + `flatMapLatest` in the ViewModel — selecting a category instantly re-queries the DB
- **Relative time** labels on cards ("Just now", "5m ago", "2h ago", "3 days ago", "2 weeks ago", "1 month ago")
- Two-step **delete confirmation** (`AlertDialog` → success dialog with check-circle icon) before removing a posting
- Material 3 theme with **dynamic colour** on Android 12+ and a static fallback palette on older releases

---

## Screens

### 1) Home — [HomeScreen.kt](app/src/main/java/com/example/p71/ui/home/HomeScreen.kt) / [HomeFragment.kt](app/src/main/java/com/example/p71/ui/home/HomeFragment.kt)
- Title: **Lost & Found**
- Two large action buttons: `Create a New Advert` and `Show All Lost & Found Items`
- Routes to the create flow or the list, respectively, via `findNavController().navigate(...)`

### 2) Create Advert — [CreateAdvertScreen.kt](app/src/main/java/com/example/p71/ui/create/CreateAdvertScreen.kt) / [CreateAdvertFragment.kt](app/src/main/java/com/example/p71/ui/create/CreateAdvertFragment.kt)
- **Post type** radio group: `Lost` / `Found` (defaults to `Lost`)
- Text fields: `Name`, `Phone` (with `KeyboardType.Phone`), `Description` (3 lines), `Location`
- **Category** dropdown: Electronics / Pets / Wallets
- **Date** field opens a Material 3 `DatePicker` dialog
- **Image** picker: tap the placeholder → choose `Gallery` (`ActivityResultContracts.GetContent`) or `Camera` (`ActivityResultContracts.TakePicture` + `FileProvider`)
- `Save` button validates, inserts an `Item` via the ViewModel, and pops back to Home on success

### 3) Items List — [ItemsListScreen.kt](app/src/main/java/com/example/p71/ui/list/ItemsListScreen.kt) / [ItemsListFragment.kt](app/src/main/java/com/example/p71/ui/list/ItemsListFragment.kt)
- Top row of `FilterChip`s — `All` plus the three categories from `ItemViewModel.categories`
- `LazyColumn` of cards: thumbnail (decoded from the stored `ByteArray`), `Lost`/`Found` badge, item name, category, relative time
- Tapping a card navigates to the detail screen, passing `itemId` as a nav argument

### 4) Item Detail — [ItemDetailScreen.kt](app/src/main/java/com/example/p71/ui/detail/ItemDetailScreen.kt) / [ItemDetailFragment.kt](app/src/main/java/com/example/p71/ui/detail/ItemDetailFragment.kt)
- Hero image (`Image(bitmap.asImageBitmap())`)
- Post-type badge, item name as the headline
- Detail rows: category, date, location, phone
- Description block
- Red `Remove` button → confirmation dialog → success dialog → `popBackStack()` to the list

---

## Data Layer

All persistence is local to the device — no network calls.

- Entity: [Item.kt](app/src/main/java/com/example/p71/data/local/Item.kt)
  - `id: Int` (PK, auto-generated), `postType`, `name`, `phone`, `description`, `category`, `date`, `location`, `imageData: ByteArray?`, `createdAt: Long` (defaults to `System.currentTimeMillis()`)
- DAO: [ItemDao.kt](app/src/main/java/com/example/p71/data/local/ItemDao.kt)
  - `insert(item)` (suspend)
  - `getAllItems(): Flow<List<Item>>` — `ORDER BY createdAt DESC`
  - `getItemsByCategory(category): Flow<List<Item>>`
  - `getItemById(id): Item?` (suspend)
  - `deleteById(id)` (suspend)
- Type converters: [Converters.kt](app/src/main/java/com/example/p71/data/local/Converters.kt) — Base64-encodes the image `ByteArray` to a `String` for storage and back
- Database: [ItemDatabase.kt](app/src/main/java/com/example/p71/data/local/ItemDatabase.kt) — singleton, name `lost_found_database`, version 1
- Repository: [ItemRepository.kt](app/src/main/java/com/example/p71/data/local/ItemRepository.kt) — thin pass-through over the DAO
- ViewModel: [ItemViewModel.kt](app/src/main/java/com/example/p71/viewmodel/ItemViewModel.kt) — extends `AndroidViewModel`, exposes `allItems: StateFlow<List<Item>>` and `selectedCategory: StateFlow<String?>`, plus `setCategory`, `insertItem`, `deleteItem` and `getItemById`

---

## Categories
Three fixed categories surfaced from `ItemViewModel.categories` and shown as filter chips on the list screen:

- Electronics
- Pets
- Wallets

---

## Input & Validation Rules
- **All Create Advert fields are required** — submission triggers `isBlank()` checks for name, phone, description, category, date, location, plus a non-null check on the image bytes
- The phone field uses `KeyboardType.Phone` for a numeric keypad but does **not** apply any regex/format validation
- Errors surface inline (red border + supporting error text) on every field that fails when `Save` is tapped — the button itself is not disabled

---

## Output Formatting
- **List card time stamp**: relative — `"Just now"`, `"5m ago"`, `"2h ago"`, `"3 days ago"`, `"2 weeks ago"`, `"1 month ago"`
- **Date display in form**: `dd/MM/yyyy` via `SimpleDateFormat`
- **Post-type badge**: styled separately for `Lost` vs `Found` so the two are visually distinguishable in the list
- **Delete flow**: red `Remove` button → `AlertDialog` confirmation → success dialog with a check-circle icon → automatic pop back to the list

---

## UI Notes
- Material 3 theme defined in [Theme.kt](app/src/main/java/com/example/p71/ui/theme/Theme.kt) — uses `dynamicLightColorScheme` / `dynamicDarkColorScheme` on Android 12+ and falls back to a static purple palette on older releases ([Color.kt](app/src/main/java/com/example/p71/ui/theme/Color.kt))
- Hybrid architecture: each fragment hosts a `ComposeView` so navigation is handled by Jetpack Navigation Components while screens are written in Compose
- Navigation graph: [nav_graph.xml](app/src/main/res/navigation/nav_graph.xml) — start destination is `homeFragment`; the detail destination receives an `itemId: integer` argument
- Camera capture relies on a `FileProvider` declared in [AndroidManifest.xml](app/src/main/AndroidManifest.xml) with the authority `${applicationId}.provider`; the cache paths are configured in [provider_paths.xml](app/src/main/res/xml/provider_paths.xml)
- No `<uses-permission>` is declared — gallery picking and the `TakePicture` activity-result contract handle their own consent prompts

---

## Project Structure
```
app/src/main/
├── java/com/example/p71/
│   ├── MainActivity.kt           # AppCompatActivity hosting the NavHostFragment
│   ├── ui/
│   │   ├── home/                 # HomeFragment + HomeScreen
│   │   ├── create/               # CreateAdvertFragment + CreateAdvertScreen
│   │   ├── list/                 # ItemsListFragment + ItemsListScreen
│   │   ├── detail/               # ItemDetailFragment + ItemDetailScreen
│   │   └── theme/                # Color.kt, Theme.kt, Type.kt
│   ├── viewmodel/
│   │   └── ItemViewModel.kt      # categories, allItems, insert/delete/getById
│   └── data/local/
│       ├── Item.kt               # @Entity(tableName = "items")
│       ├── ItemDao.kt            # @Insert / @Query / @Delete
│       ├── ItemDatabase.kt       # Room singleton, name "lost_found_database"
│       ├── ItemRepository.kt     # pass-through over DAO
│       └── Converters.kt         # Base64 ByteArray ↔ String
└── res/
    ├── layout/                   # activity_main.xml, fragment_container.xml
    ├── navigation/               # nav_graph.xml
    ├── values/                   # strings.xml, colors.xml, themes.xml
    ├── drawable/                 # launcher icon vectors
    ├── mipmap-*/                 # launcher WebPs
    └── xml/                      # provider_paths, backup_rules, data_extraction_rules
```

---

## Tech Stack
- **Kotlin** 2.2.10 + **Jetpack Compose** (BOM 2026.02.01) + **Material 3**
- **Android Gradle Plugin** 9.2.0, `compileSdk` 36, `minSdk` 35, `targetSdk` 36, Java 11
- **Room** 2.7.1 (`runtime`, `ktx`, `compiler` via **KSP** 2.2.10-2.0.2) for local persistence
- **Jetpack Navigation Component** 2.9.8 (`navigation-fragment-ktx` + `navigation-ui-ktx`) for screen routing
- **Lifecycle ViewModel Compose** 2.9.0 + `lifecycle-runtime-ktx` 2.6.1
- **Activity Compose** 1.8.0 with `ActivityResultContracts.GetContent` / `ActivityResultContracts.TakePicture` for image input
- **AndroidX Core FileProvider** for camera output URIs
- **Material Icons Extended** + Google **Material Components** 1.12.0 (used by the legacy XML theme)

---

## How to Run
1. Clone the repo and open the `P71/` folder in Android Studio (JDK 17, Android SDK 36 installed)
2. Sync Gradle — KSP will generate the Room DAO implementation on first build
3. Run the `app` module on an emulator or physical device with **Android 14+ (API 35+)** — `minSdk` is 35 in [app/build.gradle.kts](app/build.gradle.kts), so older devices are not supported
4. No API key, no network configuration — the app is fully offline. The first launch creates the Room database `lost_found_database` automatically.
5. Camera and gallery access prompts are raised by the system when you first tap the `Camera` / `Gallery` option on the Create Advert screen — there are no `<uses-permission>` declarations in [AndroidManifest.xml](app/src/main/AndroidManifest.xml) because both flows go through scoped activity-result contracts

---

## Course
SIT708 — Task 7.1P: Lost & Found App.
