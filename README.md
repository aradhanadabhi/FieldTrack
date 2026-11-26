# FieldTrack — Sem 9 M.Sc.(CA & IT) Mini Project

An Android field-visit logging app built to satisfy the KS_C_CC-598 / KS_C_CC-595
project guidelines. It genuinely implements a working slice of most of the listed
"striking features," wired into one coherent, defensible app rather than 15
disconnected demos.

## 1. Project idea (one line)

A field agent (surveyor / delivery / inspection staff) opens the app at a site,
scans a QR code or types the site name, captures a photo, grabs GPS coordinates,
dictates notes by voice, saves everything locally in SQLite, optionally SMS's a
supervisor, and later syncs pending records to a remote server as JSON over the
internet — all visible on a map and a home-screen widget.

## 2. Guideline features implemented, and where

| Guideline feature | Where it lives |
|---|---|
| Location based APIs incl. Google Maps | `ui/MapActivity.kt`, `activity_map.xml` |
| GPS | `utils/LocationHelper.kt` (FusedLocationProviderClient) |
| SQLite Database (Insert/Update/Delete/Select) | `db/DatabaseHelper.kt` |
| Speech to Text APIs | `ui/AddVisitActivity.kt` → `RecognizerIntent` |
| Multi-touch | `utils/ZoomableImageView.kt` (pinch-zoom on photo) |
| Creating Android Background Services | `network/SyncService.kt` |
| Calling a Web-Service + JSON encode/decode | `network/ApiClient.kt` |
| Using Local File Storage | Photo saved to `getExternalFilesDir("photos")` |
| Creating Widgets | `widget/VisitCountWidgetProvider.kt` |
| Using Camera | `ui/AddVisitActivity.kt` → `MediaStore.ACTION_IMAGE_CAPTURE` |
| Using Bluetooth | `utils/BluetoothHelper.kt` |
| Using Wifi | `utils/WifiHelper.kt` |
| Using Internet | `network/ApiClient.kt` (HttpURLConnection) |
| Using Telephony or SMS APIs | `utils/SmsHelper.kt` |
| Scanning the QR Code | `ui/AddVisitActivity.kt` → ZXing embedded scanner |

Not wired in (call these "future scope" in your report if your guide asks about
the full checklist): 3D Graphics, "client-service application" as a separate
protocol demo, "accessing data of other applications" (content-provider sharing),
and "Web API in PHP" (the demo instead targets a public JSON test endpoint —
swap in your own PHP/Node backend, see §6).

## 3. Architecture

```
MainActivity (dashboard)
 ├── AddVisitActivity  → Camera, GPS, Speech-to-text, QR scan, SQLite insert, SMS
 ├── VisitListActivity → SQLite select / update / delete, RecyclerView
 ├── MapActivity       → Google Maps markers from SQLite data
 └── SyncService       → background push of unsynced rows to a REST API (JSON)

VisitCountWidgetProvider → home-screen widget reading the same SQLite DB
```

Single local SQLite database (`fieldtrack.db`), one table `visits`. No Room is
used deliberately — the raw `SQLiteOpenHelper` + `ContentValues`/`Cursor` calls
map directly onto "Insert Update Delete Select" from the guideline sheet, which
is what most examiners expect to see explicitly in a viva.

### ER / table design

```
visits
------------------------------------------------
id           INTEGER  PK AUTOINCREMENT
site_name    TEXT     NOT NULL
notes        TEXT
latitude     REAL
longitude    REAL
photo_path   TEXT
timestamp    INTEGER
synced       INTEGER  DEFAULT 0   -- 0 = pending, 1 = pushed to server
```

One table is enough for a mini project; if your guide wants a "database design"
diagram, draw this single-entity ER diagram with the columns above — that alone
satisfies point 3 of the guideline sheet (technical SRS + DB design due the
week after project definition).

### Mini SRS (paste into your submitted document, expand as needed)

- **Purpose**: Reduce paperwork in field visit reporting by digitizing capture
  of location, photo, and notes at the point of visit.
- **Users**: Single-role field agent (extend to agent + supervisor roles as a
  stretch goal, since 2–3 students can each own a role).
- **Functional requirements**: FR1 capture GPS on demand, FR2 capture photo,
  FR3 dictate notes, FR4 scan QR to auto-fill site name, FR5 persist visit
  locally, FR6 list/edit/delete visits, FR7 show visits on a map, FR8 sync
  unsynced visits to a server, FR9 notify a phone number by SMS, FR10 show a
  home-screen widget with total visit count.
- **Non-functional requirements**: works offline for capture (sync is best
  effort), minSdk 24 (covers effectively all real devices), no cleartext
  secrets committed (Maps key is a placeholder you must replace).
- **Assumptions/constraints**: single device, single user, no login/auth layer
  (add one if your guide wants multi-user).

## 4. Prerequisites

- Android Studio Iguana (2023.2.1) or newer.
- JDK 17 (bundled with recent Android Studio).
- An Android device or emulator running Android 7.0 (API 24) or higher, with
  Google Play Services (for Maps/Location) — most emulator images include it.
- A free Google Maps SDK for Android API key (only needed if you want the Map
  screen to render — everything else runs without it).

## 5. How to open and run

1. Unzip the project. In Android Studio: **File → Open** → select the
   `FieldTrack` folder (the one containing `settings.gradle`).
2. Let Gradle sync. Android Studio will auto-generate the Gradle wrapper jar on
   first sync if it's missing — if it complains, run **File → Sync Project
   with Gradle Files** again, or install Gradle 8.4 locally and run
   `gradle wrapper` once inside the project folder.
3. Get a Google Maps API key:
   - Go to console.cloud.google.com → create/select a project.
   - Enable **"Maps SDK for Android"**.
   - Create an API key, restrict it to Android apps (package
     `com.finalyear.fieldtrack`).
   - Open `app/src/main/AndroidManifest.xml` and replace
     `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your key.
4. Connect a real device (USB debugging on) or start an emulator with Google
   Play Services (e.g. "Pixel 6, API 34, Google Play" image).
5. Click **Run ▶** in Android Studio, or from a terminal in the project root:
   ```
   ./gradlew installDebug
   ```
6. On first launch, grant the requested permissions (Location, Camera, SMS,
   Bluetooth). If you deny one, that specific feature button will simply fail
   gracefully with a toast — the rest of the app keeps working.

## 6. Testing each feature quickly

- **Add Visit → Scan QR**: point the camera at any QR code (generate one free
  at qr-code-generator.com containing plain text) — it fills the site name.
- **Capture Photo**: uses the system camera app; on an emulator use the
  camera's default test pattern.
- **Get GPS Location**: on an emulator, set a location via the emulator's
  Extended Controls → Location panel first.
- **Speak Notes**: needs Google app / Google Voice Typing available on the
  device or emulator image.
- **Save Visit**: writes a row to SQLite; open **View Visit List** to confirm
  select/update/delete all work (tap Edit or Delete on any card).
- **View Map**: needs the API key from step 5.3; shows a pin per saved visit.
- **Sync Now**: posts each unsynced visit as JSON to
  `https://jsonplaceholder.typicode.com/posts` (a public echo/test API — it
  accepts the POST and returns a fake `id`, proving the encode → send →
  decode round trip). Swap `BASE_URL` in `network/ApiClient.kt` for your own
  backend when you're ready to demo a real "Web API in PHP" as the guideline
  mentions.
- **Wi-Fi / Bluetooth status**: shown directly on the dashboard, updates
  automatically when you return to the app (toggle them from Quick Settings to
  see it change).
- **Widget**: long-press the home screen → Widgets → FieldTrack → drag it out;
  it shows the running count of saved visits.

## 7. Suggested team split (guideline recommends 2–3 students)

- **Student A** — SQLite layer, VisitListActivity, VisitAdapter, SRS/DB design
  document.
- **Student B** — AddVisitActivity: Camera, GPS, Speech-to-text, QR scanning,
  local file storage.
- **Student C** — Networking (SyncService + ApiClient/JSON), Maps, Widget,
  Bluetooth/Wifi/SMS status, and the final project report.

## 8. What to write in your submitted report

Reuse §2 (feature-to-file mapping), §3 (architecture + ER diagram), and the
mini-SRS in §3 almost verbatim — expand each FR/NFR into a couple of sentences
and add screenshots of each screen once you've run it. Add a short "Coding
standards followed" section (meaningful names, one class per file, Kotlin
official code style — already configured via `kotlin.code.style=official` in
`gradle.properties`) since guideline point 4 explicitly calls this out.

## 9. Known limitations / good viva answers

- No login/auth — every visit belongs to "the device," not a specific user.
- Sync is one-way (device → server) and fire-and-forget; a production version
  would need retry/backoff and a real backend with a corresponding GET to pull
  data back down.
- QR scanning and Maps depend on Google Play Services, so they won't work on a
  Play-Services-less emulator image — mention this as a known constraint if
  asked.
