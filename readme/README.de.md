<p align="right">
  <a href="../README.md">English/Englisch</a> |
  <a href="./README.zh-CN.md">简体中文/Chinesisch (vereinfacht)</a> |
  <a href="./README.ja.md">日本語/Japanisch</a> |
  <a href="./README.ko.md">한국어/Koreanisch</a> |
  <a href="./README.fr.md">Français/Französisch</a> |
  <a href="./README.es.md">Español/Spanisch</a> |
  <a href="./README.ru.md">Русский/Russisch</a> |
  <a href="./README.it.md">Italiano/Italienisch</a> |
  <a href="./README.tr.md">Türkçe/Türkisch</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Thailändisch</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamesisch</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonesisch</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malaiisch</a> |
  <a href="./README.pl.md">Polski/Polnisch</a> |
  <a href="./README.pt.md">Português/Portugiesisch</a> |
  <a href="./README.nl.md">Nederlands/Niederländisch</a> |
  <a href="./README.sv.md">Svenska/Schwedisch</a> |
  <a href="./README.uk.md">Українська/Ukrainisch</a> |
  <a href="./README.ar.md">العربية/Arabisch</a>
</p>

# TianshangHealth(天殇·悦康)

> **Privater, vollständig offline Android-Gesundheitsbegleiter.**
> Kombiniert Zyklus-Tracking, Fitness, Schlaf, Ernährung und Schrittzählen in einer verschlüsselten lokalen Datenbank – mit geräteinterner KI-gestützter mehrdimensionaler Analyse.

---

## Highlights

- **Keine Netzwerkberechtigungen** — Alle Daten bleiben auf deinem Gerät. Keine Cloud, keine Analytics, kein Firebase.
- **Militärgrad-Verschlüsselung** — SQLCipher AES-256-Datenbank, gesichert durch hardwaregestützte Android-Keystore-Schlüssel.
- **Modulare Architektur** — 12 Gradle-Module nach MVVM + Repository + Hilt + Compose.
- **Lokale KI-Inferenz** — TensorFlow-Lite-Modelle verbessern Zyklusvorhersage und Stimmungsanalyse, ohne das Gerät zu verlassen.
- **21 Sprachen** — Inklusive RTL-Unterstützung für Arabisch; von Beginn an lokalisiert.
- **Geschlechtsadaptive UI** — Die untere Navigation und Funktionen passen sich der vom Benutzer gewählten Geschlechtsauswahl an.

---

## Funktionen

| Bereich | Funktionen |
|--------|-------------|
| **Zyklus-Tracking** | Kalenderansicht, Zyklusvorhersage-Engine (IQR-Filterung + exponentieller Zerfall), Symptomprotokoll, Erinnerungen |
| **Schritte** | Hardware-Schrittzähler-Vordergrundservice + Beschleunigungsmesser-Fallback, WorkManager-Sync, Schrittanalysen nach Zyklusphase |
| **Fitness** | 17 Übungstypen, MET-basierte Kalorienberechnung, zyklusphasenbezogene Trainings Empfehlungen |
| **Schlaf** | Manuelle Schlafeingabe, Qualitätsbewertung, Canvas-Trenddiagramm |
| **Ernährung** | Mahlzeitenprotokoll (Frühstück/Mittagessen/Abendessen/Snack), Nährstoff-Tracking, Wasseraufnahme |
| **Analyse** | Mehrdimensionale Analyse-Engine, Gesundheitsempfehlungen, medizinischer Berichtsexport, TFLite-Vorhersageverbesserung |
| **Sicherheit** | PIN (Argon2id) + biometrische Sperre, verschlüsseltes ZIP-Backup/Wiederherstellung, Screenshot-Schutz auf sensiblen Seiten |

---

## Tech-Stack

| Ebene | Technologie | Version |
|-------|-----------|---------|
| Sprache | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Architektur | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigation | Navigation Compose | 2.7.6 |
| Datenbank | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Hintergrund | WorkManager | 2.9.0 |
| Krypto | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Diagramme | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Statische Analyse | Detekt | 1.23.6 |

---

## Architektur

```
app/                    # App-Shell: Einzelner NavHost, Theme, MainActivity
├── navigation/MainNavigation.kt        # Bedingte untere Navigation + Seitenrouten
core/
├── common/             # Gemeinsame UI-Komponenten, Hilfsprogramme, alle Textressourcen (21 Sprachen)
├── database/           # Room-Entitäten, DAOs, Repositories, Migrationen
└── security/           # Keystore, SQLCipher, Argon2id, Biometrie, App-Sperre
feature/
├── onboarding/         # Erster Start: Geschlechtsauswahl
├── dashboard/          # Start-Dashboard: Tagesübersicht und Gesundheitseinblicke
├── period/             # Menstruations-Tracking: Vorhersage-Engine, Erinnerungen, Backup, Einstellungen
├── steps/              # Hardware-Schrittzähler-Vordergrundservice + WorkManager-Sync
├── fitness/            # Trainingseinheiten + zyklusphasenbezogene Empfehlungen
├── sleep/              # Schlaferfassung + Canvas-Trenddiagramm
├── nutrition/          # Mahlzeiten-Tracking + Wasseraufnahme
└── analysis/           # Mehrdimensionale Analysen + lokale TFLite-KI-Inferenz
```

---

## Sicherheit

- **Daten im Ruhezustand**: Die gesamte `tianshang_health.db` wird mit SQLCipher AES-256 verschlüsselt. Das Datenbankpasswort ist eine zufällige 32-Zeichen-Zeichenkette, die durch Android Keystore verschlüsselt wird und niemals im Klartext gespeichert wird.
- **Authentifizierung**: PINs werden mit Argon2id gehasht. BiometricPrompt (`BIOMETRIC_STRONG`) ist als Fallback verfügbar.
- **Netzwerk**: Die `AndroidManifest.xml` enthält **keine `INTERNET`-Berechtigung**. Kein Firebase, keine Analytics, keine Absturzberichte.
- **App-Sperre**: Wenn die App in den Hintergrund wechselt, wird sie nach einer konfigurierbaren Verzögerung (sofort / 30s / 1min / 5min) automatisch gesperrt. Die `AppLockScreen` überlagert die gesamte App mit `zIndex(10f)`.
- **Backup**: Verschlüsselter ZIP-Export/Wiederherstellung mit benutzerdefiniertem Passwort. CSV-Export wird ebenfalls unterstützt.
- **Härtung**: `FLAG_SECURE` auf sensiblen Seiten; Überprüfung von `Debug.isDebuggerConnected()` beim Start.

---

## Erste Schritte

### Voraussetzungen

- JDK 17
- Android Studio (Ladybug oder neuer empfohlen)
- Android SDK mit installierter API 35

### Debug-APK erstellen

```bash
./gradlew :app:assembleDebug
```

Das APK befindet sich unter:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Auf einem Gerät installieren

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release-APK erstellen

Release-Builds erfordern Signaturkonfiguration in `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Führe dann aus:

```bash
./gradlew :app:assembleRelease
```

Das signierte APK wird benannt:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Sicherheitsprüfung vor der Veröffentlichung

```bash
python check_release_security.py
```

---

## Qualität und Tests

- **Detekt**: Statische Analyse mit Nulltoleranz (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Unit-Tests**:
  ```bash
  ./gradlew test
  ```
- **Instrumentation-Tests**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **String-Validierung**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Mehrsprachige Unterstützung

Alle 21 Sprachressourcen werden zentral in `core/common/src/main/res/values[-lang]/strings.xml` gepflegt:

English/Englisch, 简体中文/Chinesisch (vereinfacht), 日本語/Japanisch, 한국어/Koreanisch, Français/Französisch, Español/Spanisch, Deutsch/Deutsch, Русский/Russisch, Italiano/Italienisch, Türkçe/Türkisch, हिन्दी/Hindi, ภาษาไทย/Thailändisch, Tiếng Việt/Vietnamesisch, Bahasa Indonesia/Indonesisch, Bahasa Melayu/Malaiisch, Polski/Polnisch, Português/Portugiesisch, Nederlands/Niederländisch, Svenska/Schwedisch, Українська/Ukrainisch, العربية/Arabisch

---

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert. Siehe die Projektdateien für Details.

---

<p align="center">
  <b>Die Datensouveränität gehört dem Nutzer. Der Entwickler kann aus technischen Gründen nicht auf deine Daten zugreifen.</b>
</p>
