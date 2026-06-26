<p align="right">
  <a href="./readme/README.zh-CN.md">简体中文</a> |
  <a href="./readme/README.ja.md">日本語</a> |
  <a href="./readme/README.ko.md">한국어</a> |
  <a href="./readme/README.fr.md">Français</a> |
  <a href="./readme/README.es.md">Español</a> |
  <a href="./readme/README.de.md">Deutsch</a> |
  <a href="./readme/README.ru.md">Русский</a> |
  <a href="./readme/README.it.md">Italiano</a> |
  <a href="./readme/README.tr.md">Türkçe</a> |
  <a href="./readme/README.hi.md">हिन्दी</a> |
  <a href="./readme/README.th.md">ภาษาไทย</a> |
  <a href="./readme/README.vi.md">Tiếng Việt</a> |
  <a href="./readme/README.id.md">Bahasa Indonesia</a> |
  <a href="./readme/README.ms.md">Bahasa Melayu</a> |
  <a href="./readme/README.pl.md">Polski</a> |
  <a href="./readme/README.pt.md">Português</a> |
  <a href="./readme/README.nl.md">Nederlands</a> |
  <a href="./readme/README.sv.md">Svenska</a> |
  <a href="./readme/README.uk.md">Українська</a> |
  <a href="./readme/README.ar.md">العربية</a>
</p>

# TianshangHealth(天殇·悦康)

> **Privacy-first, fully offline Android health companion.**
> Combines menstrual tracking, fitness, sleep, nutrition, and step counting into one encrypted local database — with on-device AI-powered cross-dimensional insights.

---

## Highlights

- **Zero network permissions** — All data stays on your device. No cloud, no analytics, no Firebase.
- **Military-grade encryption** — SQLCipher AES-256 database secured by Android Keystore hardware-backed keys.
- **Modular architecture** — 12 Gradle modules following MVVM + Repository + Hilt + Compose.
- **Local AI inference** — TensorFlow Lite models enhance menstrual prediction and mood analysis without leaving the device.
- **21 languages** — Including RTL support for Arabic; localized from day one.
- **Gender-adaptive UI** — Bottom navigation and features adjust based on user gender selection.

---

## Features

| Domain              | Capabilities                                                                                                   |
| ------------------- | -------------------------------------------------------------------------------------------------------------- |
| **Period Tracking** | Calendar view, cycle prediction engine (IQR filtering + exponential decay), symptom logging, reminders         |
| **Steps**           | Hardware step counter foreground service + accelerometer fallback, WorkManager sync, cycle-phase step insights |
| **Fitness**         | 17 exercise types, MET-based calorie calculator, cycle-aware workout recommendations                           |
| **Sleep**           | Manual sleep logging, quality scoring, Canvas trend chart                                                      |
| **Nutrition**       | Meal logging (breakfast/lunch/dinner/snack), macro tracking, water intake                                      |
| **Analysis**        | Cross-dimensional analytics engine, health suggestions, medical report export, TFLite prediction enhancement   |
| **Security**        | PIN (Argon2id) + biometric lock, encrypted ZIP backup/restore, screenshot protection on sensitive screens      |

---

## Tech Stack

| Layer           | Technology                                                 | Version         |
| --------------- | ---------------------------------------------------------- | --------------- |
| Language        | Kotlin                                                     | 1.9.24          |
| Build           | Gradle + Android Gradle Plugin                             | 8.9 / 8.6.0     |
| UI              | Jetpack Compose (Material 3)                               | BOM 2024.09.00  |
| Architecture    | MVVM + Repository + StateFlow                              | —               |
| DI              | Hilt (KSP)                                                 | 2.51.1          |
| Navigation      | Navigation Compose                                         | 2.7.6           |
| Database        | Room + SQLCipher                                           | 2.6.1 / 4.5.4   |
| Background      | WorkManager                                                | 2.9.0           |
| Crypto          | Android Keystore + Bouncy Castle (Argon2id)                | — / 1.78        |
| Charts          | MPAndroidChart + Vico                                      | v3.1.0 / 1.13.1 |
| ML              | TensorFlow Lite (LINEAR + LSTM models; both run on-device) | 2.17.0          |
| Static Analysis | Detekt                                                     | 1.23.6          |

---

## Architecture

```
app/                    # Single NavHost, theme, MainActivity
├── navigation/MainNavigation.kt        # Conditional bottom nav + side routes
core/
├── common/             # Shared UI components, utilities, all string resources (21 languages)
├── database/           # Room entities, DAOs, repositories, migrations
└── security/           # Keystore, SQLCipher, Argon2id, biometric auth, app lock
feature/
├── onboarding/         # Gender selection
├── dashboard/          # Today overview, health insights
├── period/             # Menstrual tracking, prediction, reminders, backup, settings
├── steps/              # Step counter service + WorkManager sync
├── fitness/            # Workout records + cycle-aware recommendations
├── sleep/              # Sleep logging + trend chart
├── nutrition/          # Meal + water tracking
└── analysis/           # Cross-dimensional analytics + TFLite ML inference
```

---

## Security

- **Data at rest**: SQLCipher AES-256 encrypts the entire `tianshang_health.db`. The database password is a random 32-character string encrypted by Android Keystore and never stored in plain text.
- **Authentication**: PINs are hashed with Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) is available as a fallback.
- **Network**: `AndroidManifest.xml` contains **no `INTERNET` permission**. No Firebase, no analytics, no crash reporting.
- **App lock**: Backgrounding triggers a configurable lock delay (immediate / 30s / 1min / 5min). The lock screen overlays the entire app at `zIndex(10f)`.
- **Backup**: Encrypted ZIP export/restore with user-provided password. CSV export is also supported.
- **Hardening**: `FLAG_SECURE` on sensitive screens; `Debug.isDebuggerConnected()` check on app startup.

---

## Getting Started

### Prerequisites

- JDK 17
- Android Studio (Ladybug or newer recommended)
- Android SDK with API 35 installed

### Build Debug APK

```bash
./gradlew :app:assembleDebug
```

The APK will be located at:

```
app/build/outputs/apk/debug/app-debug.apk
```

### Install on a device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK

Release builds require signing configuration in `local.properties`:

```properties
KEYSTORE_PASSWORD=your-store-password
KEY_ALIAS=your-alias
KEY_PASSWORD=your-key-password
```

The keystore file `app/tianshang-health-release.jks` must exist.

Then run:

```bash
./gradlew :app:assembleRelease
```

The signed APK (approximately 34 MB) will be named:

```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

> **Note**: The current development build uses a self-signed debug keystore (see `local.properties`). For production release, replace with a real certificate.

### Pre-release security check

```bash
python check_release_security.py
```

---

## Quality & Testing

- **Detekt**: Zero-tolerance static analysis (`maxIssues: 0`).
  
  ```bash
  ./gradlew detektAll
  ```
- **Unit tests** (~300 tests, 31 files, 15/15 ViewModel coverage, 19 instrumentation tests):
  
  ```bash
  ./gradlew test
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Instrumentation tests** (19 TFLite model tests on device):
  
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **String validation**:
  
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Multi-language Support

All 21 language strings are centralized in `core/common/src/main/res/values[-lang]/strings.xml`:

English, 简体中文, 日本語, 한국어, Français, Español, Deutsch, Русский, Italiano, Türkçe, हिन्दी, ภาษาไทย, Tiếng Việt, Bahasa Indonesia, Bahasa Melayu, Polski, Português, Nederlands, Svenska, Українська, العربية

---

## License

This project is licensed under the MIT License. See the project files for details.

---

<p align="center">
  <b>Data sovereignty belongs to the user. The developer cannot access your data by design.</b>
</p>
