# SIT708 Pass Task 9.1 – Lost and Found Map Mobile App

A Jetpack Compose Android app that lets users post and discover lost or found items, with full geo features built on OpenStreetMap. Items are stored in a Room database with latitude/longitude; the map screen shows all posts as colour-coded pins and filters them by radius around the user's current location. Built in Kotlin with Material 3 and no Google Maps API key required.

---

## Task Requirements Coverage

| # | Brief requirement | Where it is implemented |
|---|---|---|
| 1 | Location text box with **autocomplete** on the Create form | [LocationPickerField.kt](app/src/main/java/com/example/p71/ui/components/LocationPickerField.kt) — debounced Nominatim search, dropdown of suggestions |
| 2 | **GET CURRENT LOCATION** button populates the location field | [LocationPickerField.kt](app/src/main/java/com/example/p71/ui/components/LocationPickerField.kt) — `FusedLocationProviderClient` + reverse-geocode via Nominatim |
| 3 | **SHOW ON MAP** button on the home page | [HomeScreen.kt](app/src/main/java/com/example/p71/ui/home/HomeScreen.kt) — third button navigates to the map screen |
| 4 | All lost/found items shown on the map | [MapScreen.kt](app/src/main/java/com/example/p71/ui/map/MapScreen.kt) — red pins for Lost, green pins for Found, blue pin for the user |
| 5 | **Radius-based search** (subtask) — only items within X km | [MapScreen.kt](app/src/main/java/com/example/p71/ui/map/MapScreen.kt) slider 1–50 km, [GeoUtils.kt](app/src/main/java/com/example/p71/util/GeoUtils.kt) Haversine filter |

---

## Features

- Three-screen home: **Create a New Advert → Items List → Map**
- Post type toggle (Lost / Found) stored and colour-coded throughout the UI
- Location entry via autocomplete (Nominatim/OSM) or one-tap GPS fix
- Mini read-only map preview shown immediately after a location is confirmed on the Create form
- Category filter chips on the list screen (Electronics, Pets, Wallets)
- Full item detail with photo, remove action, and two-step confirmation dialog
- Persistent Room storage — items survive app restarts
- OSM tile map (osmdroid) with:
  - Blue pin for the user's current location
  - Red pins for Lost items, green pins for Found items
  - Semi-transparent radius circle centred on the user
  - Slider to adjust search radius from 1 to 50 km
  - Live item count showing how many posts are in range
  - Recenter FAB + bouncing pin animation on recenter
- Runtime location permission request with a graceful fallback UI
- Image picker — camera or gallery, auto-scaled and stored as a BLOB

---

## Screens

### 1) Home — [HomeScreen.kt](app/src/main/java/com/example/p71/ui/home/HomeScreen.kt)
Three full-width buttons: **Create a New Advert**, **Show All Lost & Found Items**, **Show on Map**.

### 2) Create Advert — [CreateAdvertScreen.kt](app/src/main/java/com/example/p71/ui/create/CreateAdvertScreen.kt)
- Post type radio (Lost / Found)
- Name, Phone, Description text fields
- Category `ExposedDropdownMenuBox`
- Date picker (past dates only)
- [LocationPickerField](app/src/main/java/com/example/p71/ui/components/LocationPickerField.kt): autocomplete + GET CURRENT LOCATION button; shows a ✓ icon when coords are confirmed
- [MapPreview](app/src/main/java/com/example/p71/ui/components/MapPreview.kt): 160 dp read-only tile map that appears once a location is confirmed
- Image picker (camera / gallery, max 1024 px, JPEG 80 quality)
- Inline validation on every field before saving

### 3) Items List — [ItemsListScreen.kt](app/src/main/java/com/example/p71/ui/list/ItemsListScreen.kt)
Scrollable `LazyColumn` of cards with thumbnail, post-type chip, category, and relative timestamp. Horizontally scrollable category filter chips at the top.

### 4) Item Detail — [ItemDetailScreen.kt](app/src/main/java/com/example/p71/ui/detail/ItemDetailScreen.kt)
Full-size photo, post type, name, category, date, location, phone, description. **Remove** button with a confirmation `AlertDialog` and a success dialog on completion.

### 5) Map — [MapScreen.kt](app/src/main/java/com/example/p71/ui/map/MapScreen.kt)
- osmdroid `MapView` in a `AndroidView` Compose wrapper
- Requests `ACCESS_FINE_LOCATION` at runtime; shows a permission prompt UI if denied
- Blue pin + translucent circle for the user; red/green pins for items
- Radius slider (1–50 km) and item count label at the bottom
- Recenter FAB + bouncing `LocationOn` icon overlay after camera animation

---

## Geo Integration

### Location sensing
[LocationProvider.kt](app/src/main/java/com/example/p71/data/location/LocationProvider.kt) wraps `FusedLocationProviderClient.getCurrentLocation()` as a suspend function, bridged via `kotlinx-coroutines-play-services`.

### Geocoding
[NominatimApi.kt](app/src/main/java/com/example/p71/data/remote/NominatimApi.kt) / [NominatimClient.kt](app/src/main/java/com/example/p71/data/remote/NominatimClient.kt) — Retrofit + OkHttp client calling the OpenStreetMap Nominatim API for both forward (search by text) and reverse (lat/lng → address) geocoding. A custom `User-Agent` header is required by Nominatim's usage policy; the client sets `P71-LostFoundApp/1.0`.

### Distance calculation
[GeoUtils.kt](app/src/main/java/com/example/p71/util/GeoUtils.kt) — `distanceKm(lat1, lon1, lat2, lon2)` uses the Haversine formula over Earth radius 6371 km. Used by `MapScreen` to filter `allItemsWithCoords` against the current location and selected radius.

### Mapping
[MapScreen.kt](app/src/main/java/com/example/p71/ui/map/MapScreen.kt) uses **osmdroid 6.1.18** (OpenStreetMap tiles, no API key). `marker_default` from `org.osmdroid.library.R` is tinted at runtime with `DrawableCompat.setTint` to produce the three pin colours.

---

## Conversation Storage (Room)

All items live in [data/local/](app/src/main/java/com/example/p71/data/local/).

- **Entity** — [Item.kt](app/src/main/java/com/example/p71/data/local/Item.kt): `id`, `postType`, `name`, `phone`, `description`, `category`, `date`, `location`, `latitude?`, `longitude?`, `imageData` (BLOB), `createdAt`
- **DAO** — [ItemDao.kt](app/src/main/java/com/example/p71/data/local/ItemDao.kt): `getAllItems()`, `getItemsByCategory()`, `getAllItemsWithCoords()` (only rows with non-null lat/lng — used by the map), `getItemById()`, `insert()`, `deleteById()`
- **Database** — [ItemDatabase.kt](app/src/main/java/com/example/p71/data/local/ItemDatabase.kt): singleton, version 2, `fallbackToDestructiveMigration`
- **Converters** — [Converters.kt](app/src/main/java/com/example/p71/data/local/Converters.kt): `ByteArray ↔ Base64 String` for image BLOB storage
- **Repository** — [ItemRepository.kt](app/src/main/java/com/example/p71/data/local/ItemRepository.kt): wraps DAO, exposes `Flow`-based queries to the ViewModel

---

## Project Structure

```
app/src/main/java/com/example/p71/
├── MainActivity.kt                    # Sets osmdroid User-Agent, loads nav host
├── data/
│   ├── local/
│   │   ├── Item.kt                    # @Entity with lat/lng fields
│   │   ├── ItemDao.kt                 # getAllItemsWithCoords(), CRUD
│   │   ├── ItemDatabase.kt            # Room singleton
│   │   ├── ItemRepository.kt          # Flow-based data layer
│   │   └── Converters.kt              # ByteArray ↔ Base64
│   ├── location/
│   │   └── LocationProvider.kt        # FusedLocationProviderClient suspend wrapper
│   └── remote/
│       ├── NominatimApi.kt            # Retrofit interface: search + reverse
│       ├── NominatimClient.kt         # OkHttp + Retrofit singleton
│       └── NominatimModels.kt         # GSON data classes for Nominatim responses
├── viewmodel/
│   └── ItemViewModel.kt               # allItems, allItemsWithCoords, insert, delete
├── ui/
│   ├── home/
│   │   ├── HomeScreen.kt              # Three action buttons
│   │   └── HomeFragment.kt
│   ├── create/
│   │   ├── CreateAdvertScreen.kt      # Full form with LocationPickerField
│   │   └── CreateAdvertFragment.kt
│   ├── list/
│   │   ├── ItemsListScreen.kt         # LazyColumn + category chips
│   │   └── ItemsListFragment.kt
│   ├── detail/
│   │   ├── ItemDetailScreen.kt        # Detail + remove dialogs
│   │   └── ItemDetailFragment.kt
│   ├── map/
│   │   ├── MapScreen.kt               # osmdroid map + radius slider
│   │   └── MapFragment.kt
│   ├── components/
│   │   ├── LocationPickerField.kt     # Autocomplete + Get Current Location
│   │   └── MapPreview.kt              # Read-only 160 dp mini map
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── util/
    └── GeoUtils.kt                    # Haversine distanceKm()
```

---

## Tech Stack

- **Kotlin** 2.2.10 + **Jetpack Compose** (BOM 2026.02.01) + **Material 3**
- **Navigation Component** 2.9.8 — Fragment-based, nav graph with five destinations
- **Lifecycle ViewModel Compose** 2.9.0
- **Room** 2.7.1 — item persistence with type converters for image BLOBs
- **osmdroid** 6.1.18 — OpenStreetMap tile rendering, markers, polygons
- **Google Play Services Location** 21.3.0 — `FusedLocationProviderClient`
- **Retrofit** 2.11.0 + **OkHttp** 4.12.0 — Nominatim geocoding REST client
- **kotlinx-coroutines-play-services** 1.7.3 — coroutine bridge for Play Services
- **KSP** 2.3.2 — annotation processing for Room
- **Min SDK** 35, **Target SDK** 36, **Compile SDK** 36.1, **AGP** 9.2.1

---

## How to Run

1. Clone the repository and open in **Android Studio** (latest stable, JDK 11)
2. Sync Gradle — all dependencies download automatically (no API keys needed)
3. Run the `app` module on an emulator or a device with Android 15+ (API 35+)
4. Grant **Location** permission when prompted on the Create or Map screen
5. The app requires the `INTERNET` permission (declared in [AndroidManifest.xml](app/src/main/AndroidManifest.xml)) for map tiles and Nominatim geocoding

---

## Responsible Use of AI

An LLM (Claude Code) was used as a coding assistant while drafting parts of the source and this README — consistent with the brief's "Responsible Use of AI" clause.

---

## Course

SIT708 — Pass Task 9.1: Lost and Found Map Mobile App.
