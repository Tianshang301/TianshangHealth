<p align="right">
  <a href="../README.md">English/Engels</a> |
  <a href="./README.zh-CN.md">简体中文/Chinees (vereenvoudigd)</a> |
  <a href="./README.ja.md">日本語/Japans</a> |
  <a href="./README.ko.md">한국어/Koreaans</a> |
  <a href="./README.fr.md">Français/Frans</a> |
  <a href="./README.es.md">Español/Spaans</a> |
  <a href="./README.de.md">Deutsch/Duits</a> |
  <a href="./README.ru.md">Русский/Russisch</a> |
  <a href="./README.it.md">Italiano/Italiaans</a> |
  <a href="./README.tr.md">Türkçe/Turks</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Thai</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamees</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonesisch</a> |
  <a href="./README.ms.md">Bahasa Melayu/Maleis</a> |
  <a href="./README.pl.md">Polski/Pools</a> |
  <a href="./README.pt.md">Português/Portugees</a> |
  <a href="./README.sv.md">Svenska/Zweeds</a> |
  <a href="./README.uk.md">Українська/Oekraïens</a> |
  <a href="./README.ar.md">العربية/Arabisch</a>
</p>

# TianshangHealth(天殇·悦康)

> **Privacy-first, volledig offline Android gezondheidsassistent.**
> Combineert menstruatiecyclus tracking, fitness, slaap, voeding en stappentelling in één versleutelde lokale database — met cross-dimensionale inzichten op basis van AI op het apparaat.

---

## Hoogtepunten

- **Geen netwerktoestemmingen** — Alle gegevens blijven op uw apparaat. Geen cloud, geen analytics, geen Firebase.
- **Militaire encryptie** — SQLCipher AES-256 database beveiligd door Android Keystore hardware-ondersteunde sleutels.
- **Modulaire architectuur** — 12 Gradle-modules volgens MVVM + Repository + Hilt + Compose.
- **Lokale AI-inferentie** — TensorFlow Lite modellen verbeteren cyclusvoorspelling en stemmingsanalyse zonder het apparaat te verlaten.
- **21 talen** — inclusief RTL-ondersteuning voor Arabisch; vanaf dag één gelokaliseerd.
- **Gender-adaptieve UI** — Onderste navigatie en functies passen zich aan op basis van de geselecteerde gender.

---

## Functies

| Domein | Mogelijkheden |
|--------|---------------|
| **Cyclustracking** | Kalenderweergave, cyclusvoorspellingsmotor (IQR-filtering + exponentieel verval), symptoomregistratie, herinneringen |
| **Stappen** | Hardware stappenteller voorgrondservice + accelerometer fallback, WorkManager-sync, stapinzichten per cyclusfase |
| **Fitness** | 17 oefentypes, MET-gebaseerde calorieteller, cyclusbewuste trainingsaanbevelingen |
| **Slaap** | Handmatige slaapregistratie, kwaliteitsscore, Canvas trendgrafiek |
| **Voeding** | Maaltijdregistratie (ontbijt/lunch/diner/snack), macro-tracking, waterinname |
| **Analyse** | Cross-dimensionale analysemotor, gezondheidsadviezen, export medisch rapport, TFLite-voorspellingsverbetering |
| **Beveiliging** | PIN (Argon2id) + biometrische vergrendeling, versleutelde ZIP-backup/restore, screenshotbeveiliging op gevoelige schermen |

---

## Technologie stack

| Laag | Technologie | Versie |
|------|------------|--------|
| Taal | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Architectuur | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigatie | Navigation Compose | 2.7.6 |
| Database | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Achtergrond | WorkManager | 2.9.0 |
| Crypto | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Grafieken | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Statische analyse | Detekt | 1.23.6 |

---

## Architectuur

```
app/                    # App-schaal: enkele NavHost, thema, MainActivity
├── navigation/MainNavigation.kt        # Conditionele onderste navigatie + zijroutes
core/
├── common/             # Gedeelde UI-componenten, hulpprogramma's, alle stringresources (21 talen)
├── database/           # Room-entiteiten, DAO's, repositories, migraties
└── security/           # Keystore, SQLCipher, Argon2id, biometrische authenticatie, app-vergrendeling
feature/
├── onboarding/         # Genderkeuze bij eerste gebruik
├── dashboard/          # Overzicht van vandaag, gezondheidsinzichten
├── period/             # Menstruatiecyclus tracking, voorspelling, herinneringen, backup, instellingen
├── steps/              # Stappentellerservice + WorkManager-sync
├── fitness/            # Trainingsrecords + cyclusbewuste aanbevelingen
├── sleep/              # Slaapregistratie + trendgrafiek
├── nutrition/          # Maaltijd + water tracking
└── analysis/           # Cross-dimensionale analyse + TFLite ML-inferentie
```

---

## Beveiliging en privacy

- **Gegevens in rust**: SQLCipher AES-256 versleutelt de hele `tianshang_health.db` database. Het databasewachtwoord is een willekeurige string van 32 tekens, versleuteld door Android Keystore en nooit opgeslagen in platte tekst.
- **Authenticatie**: PIN's worden gehasht met Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) is beschikbaar als fallback.
- **Netwerk**: `AndroidManifest.xml` **bevat geen `INTERNET`-machtiging**. Geen Firebase, geen analytics, geen crashrapportage.
- **App-vergrendeling**: Bij naar de achtergrond gaan wordt een configureerbare vertraging geactiveerd (onmiddellijk / 30s / 1min / 5min). Het vergrendelingsscherm bedekt de hele app op `zIndex(10f)`.
- **Backup**: Versleutelde ZIP-export/restore met door gebruiker opgegeven wachtwoord. CSV-export wordt ook ondersteund.
- **Hardening**: `FLAG_SECURE` op gevoelige schermen; `Debug.isDebuggerConnected()`-controle bij het opstarten.

---

## Aan de slag

### Vereisten

- JDK 17
- Android Studio (Ladybug of nieuwer aanbevolen)
- Android SDK met API 35 geïnstalleerd

### Debug APK bouwen

```bash
./gradlew :app:assembleDebug
```

De APK bevindt zich op:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installeren op een apparaat

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release APK bouwen

Release-builds vereisen handtekeningconfiguratie in `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Voer vervolgens uit:

```bash
./gradlew :app:assembleRelease
```

De ondertekende APK wordt genoemd:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Pre-release beveiligingscontrole

```bash
python check_release_security.py
```

---

## Kwaliteit en testen

- **Detekt**: Statische analyse met nultolerantie (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Unittests**:
  ```bash
  ./gradlew test
  ```
- **Instrumentatietests**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Stringvalidatie**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Meertalige ondersteuning

Alle 21 talen stringresources zijn gecentraliseerd in `core/common/src/main/res/values[-lang]/strings.xml`:

English/Engels, 简体中文/Chinees (vereenvoudigd), 日本語/Japans, 한국어/Koreaans, Français/Frans, Español/Spaans, Deutsch/Duits, Русский/Russisch, Italiano/Italiaans, Türkçe/Turks, हिन्दी/Hindi, ภาษาไทย/Thai, Tiếng Việt/Vietnamees, Bahasa Indonesia/Indonesisch, Bahasa Melayu/Maleis, Polski/Pools, Português/Portugees, Nederlands/Nederlands, Svenska/Zweeds, Українська/Oekraïens, العربية/Arabisch

---

## Licentie

Dit project is gelicentieerd onder de MIT-licentie. Zie de projectbestanden voor details.

---

<p align="center">
  <b>Gegevenssoevereiniteit is van de gebruiker. De ontwikkelaar heeft technisch geen toegang tot uw gegevens.</b>
</p>
