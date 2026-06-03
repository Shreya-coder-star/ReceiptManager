# Receipt Manager — Android (Kotlin)

A native Android Kotlin conversion of the Flutter Receipt Manager app.

## Features (mirrors the original Flutter app exactly)
- **Receipt Tab** — view receipts filtered by selected date (calendar picker)
- **Search Tab** — search all receipts by title with live filtering
- **Add Receipt** — pick a file (jpg/png/pdf/doc) or take a photo, enter a title & date, save
- **Open** — tap the file icon to open a receipt with the appropriate system app
- **Share** — share any receipt file via Android share sheet
- **Delete** — delete receipt with confirmation dialog (same alert style as Flutter)
- **SQLite** — `recep.db` with `receipt_bill` table (identical schema to original)

## Project Structure
```
app/src/main/java/com/receiptmanager/
├── db/
│   └── DatabaseHelper.kt       ← mirrors lib/dbhandlers/sql.dart
├── model/
│   └── Receipt.kt              ← data class
├── ui/
│   ├── MainActivity.kt         ← mirrors lib/views/home_page.dart
│   └── AddReceiptActivity.kt   ← mirrors lib/views/add_receipt_form.dart
├── adapter/
│   └── ReceiptAdapter.kt       ← RecyclerView adapter for receipt list
└── util/
    ├── FileManager.kt          ← mirrors lib/tools/file_manager.dart
    └── AppTheme.kt             ← mirrors lib/service/theme_color_service.dart
```

## Setup in Android Studio
1. Open Android Studio → **File → Open** → select this `ReceiptManager` folder
2. Wait for Gradle sync to complete
3. Add JitPack to your `settings.gradle` if using `imagepicker` dependency:
   ```
   maven { url 'https://jitpack.io' }
   ```
4. Run on a device or emulator (minSdk 21 / Android 5.0+)

## Permissions
- `CAMERA` — for taking receipt photos
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES` — for file picker
- Files are stored in the app's **private internal storage** (no external storage needed)

## Color Scheme (same as Flutter original)
| Flutter HexColor | Kotlin | Usage |
|---|---|---|
| `#6737b8` | `colorPrimary` | AppBar, header background |
| `#03dac5` | `colorAccent` | Calendar header, buttons |
| `#f3558e` | `colorShareIcon` | Delete icon |
| `#424242` | `colorBillTitle` | Receipt title text |
