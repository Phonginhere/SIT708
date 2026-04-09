# Pass_Task41 – Personal Event Planner App

A Jetpack Compose Android app for organising upcoming events, trips, and appointments. Supports full CRUD operations with local Room database persistence, bottom navigation, and real-time location autocomplete.

---

## Features

- Add, view, edit, and delete events (full CRUD)
- Dashboard displays all upcoming events sorted chronologically
- Three event categories: **Event**, **Trip**, **Appointment**
- Location field with real-time autocomplete via OpenStreetMap Nominatim API
- Multi-select mode (long-press) for bulk deletion
- Swipe left to delete, swipe right to edit from the event list
- Unsaved-changes exit confirmation dialog
- Input and logic validation (title required, date required, past dates blocked)
- Data persists via Room database — survives app restarts and device reboots

---

## Supported Event Categories

### 1) Event
- Personal events such as birthdays, meetings, or social gatherings
- Full CRUD: Create → Read (list) → Update → Delete

### 2) Trip
- Travel plans and journeys
- Full CRUD: Create → Read (list) → Update → Delete

### 3) Appointment
- Medical, professional, or any scheduled appointments
- Full CRUD: Create → Read (list) → Update → Delete

---

## Input & Validation Rules

| Field | Rules |
|---|---|
| **Title** | Required — inline error shown below field if empty on save |
| **Date/Time** | Required — selected via native DatePickerDialog + TimePickerDialog; past dates are blocked (`minDate = System.currentTimeMillis()`) |
| **Location** | Optional — autocomplete suggestions appear after 3+ characters with a 500 ms debounce; up to 5 results from Nominatim |
| **Category** | Set by the bottom nav tab used to create the event; read-only in edit mode |

On validation failure, an inline error message appears directly below the offending field. No save occurs until all required fields pass validation.

---

## Output Formatting

- **Date/Time**: displayed as `dd MMM yyyy, HH:mm` (e.g. `09 Apr 2026, 14:30`) using `SimpleDateFormat`
- **Event cards** show: Title (bold), Category label, Location, formatted Date/Time
- **Empty state**: placeholder message shown when no events exist

---

## UI Notes

- **Bottom Navigation Bar** with 4 tabs: Home (event list), Event, Trip, Appointment
- Home tab lists all events sorted by datetime ascending
- Tapping Event / Trip / Appointment tab opens the Add Event form pre-set to that category
- Swipe **left** (red background) → delete with confirmation dialog
- Swipe **right** (green background) → navigate to edit screen
- **Long-press** a card → enters multi-select mode; a yellow header shows the selected count and a bulk-delete button appears
- Back press with unsaved form changes → triggers an exit confirmation dialog (Save / Discard / Cancel)
- Compose screens are embedded inside Fragments via `ComposeView` for compatibility with the Jetpack Navigation Component

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Jetpack Navigation Component (Fragments) |
| Database | Room (SQLite) |
| Architecture | MVVM + Repository Pattern |
| Async | Kotlin Coroutines + Flow / StateFlow |
| HTTP Client | OkHttp |
| Location API | OpenStreetMap Nominatim |

---

## Project Structure

```
app/src/main/java/com/example/pass_task41/
├── MainActivity.kt                   # FragmentActivity — Bottom Nav & NavController
├── data/local/
│   ├── Event.kt                     # Room @Entity (id, title, category, location, datetime)
│   ├── EventDao.kt                  # @Dao — Flow queries, insert, update, delete
│   ├── EventDatabase.kt             # Singleton Room database
│   ├── EventRepository.kt           # Repository abstraction layer
│   └── EventViewModel.kt            # AndroidViewModel exposing StateFlow<List<Event>>
└── ui/
    ├── EventListScreen.kt           # Compose — event list, swipe actions, multi-select
    ├── AddEditEventScreen.kt        # Compose — create/edit form, location autocomplete
    ├── fragment/
    │   ├── EventListFragment.kt     # Fragment wrapper for the list screen
    │   └── AddEditEventFragment.kt  # Fragment wrapper for the add/edit screen
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt

app/src/main/res/
├── navigation/nav_graph.xml         # Navigation graph (3 destinations)
├── menu/bottom_nav_menu.xml         # 4-item bottom navigation menu
└── layout/activity_main.xml         # NavHostFragment + BottomNavigationView
```

---

## How to Run

1. Clone this repository and open the project in **Android Studio Hedgehog** (or newer).
2. Let Gradle sync and download dependencies.
3. Run the `app` module on an emulator or physical device running **API 24+**.
4. An internet connection is required for location autocomplete (OpenStreetMap Nominatim).
