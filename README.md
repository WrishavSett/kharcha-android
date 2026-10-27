# Kharcha for Android

A native Android version of Kharcha, the rupee expense tracker. Everything is stored on the phone (Room database), works offline, and follows the web version's design: same palette in light and dark mode, Bricolage Grotesque headings and IBM Plex Mono figures.

## What it does

- **Add, edit and delete expenses**: amount, one of eight categories, note, date (Today / Yesterday / pick a date) and UPI, Card or Cash. Deletes can be undone from the snackbar.
- **Month summary**: total spent, comparison with last month up to the same date, daily average, entry count and top category.
- **Monthly budget**: progress bar with an even-pace marker, rupees left and a per-day allowance, plus On track / Spending ahead of pace / Over budget.
- **Day by day chart**: daily bars with the daily budget as a dashed line. A one-off big day (rent) is drawn broken so the other bars stay readable. Tap or drag to read any day.
- **Where it went**: category totals with share of spending; tap one to filter the transactions.
- **Paid by**: split between UPI, Card and Cash.
- **Transactions**: grouped by day with day totals, searchable by note or category.
- **Example entries** to try it out, cleared in one tap.

## Install on your phone

Every push to the default branch builds a signed APK and publishes it to the **latest** release of this repo.

1. On your phone, open this repo's **Releases** page and download `kharcha.apk` from **latest**.
2. Open the file. Android will ask you to allow installs from your browser or Files app the first time.
3. Later builds install over the old one and keep your data.

Each workflow run also keeps the APK as a build artifact under **Actions**.

## Build it yourself

Open the folder in Android Studio and press **Run**, or from a terminal:

```
./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/release/`. Requirements: JDK 17+, Android SDK 36.

Toolchain: Gradle 8.14.3, Android Gradle Plugin 8.13, Kotlin 2.2.21, Compose BOM 2025.10.01, Room 2.8. Android Studio may offer to upgrade these; that's optional.

## Signing

Builds are signed with `keystore/kharcha.jks` (password `kharcha-shared`), committed so every build, from CI or your own machine, can update the previous one in place. That's fine for installing on your own phones. **Before publishing to the Play Store**, create a private upload key, keep it out of the repo, and point `signingConfigs` in `app/build.gradle.kts` at it.

## Project layout

```
app/src/main/java/com/kharcha/app/
  data/        Room entities, DAO, database, repository, example entries
  model/       Categories and payment methods
  ui/          View model, home state, screens and components
  ui/theme/    Colours, fonts and Material theme
```

## Data

The app's data lives only on the phone and is included in Android's automatic device backup. It does not sync with the Kharcha web page.

## Fonts

Bricolage Grotesque and IBM Plex Mono are bundled (subset to Latin plus ₹) under the SIL Open Font License; see `licenses/`.
