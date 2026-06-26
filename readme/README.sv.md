<p align="right">
  <a href="../README.md">English/Engelska</a> |
  <a href="./README.zh-CN.md">简体中文/Kinesiska (förenklad)</a> |
  <a href="./README.ja.md">日本語/Japanska</a> |
  <a href="./README.ko.md">한국어/Koreanska</a> |
  <a href="./README.fr.md">Français/Franska</a> |
  <a href="./README.es.md">Español/Spanska</a> |
  <a href="./README.de.md">Deutsch/Tyska</a> |
  <a href="./README.ru.md">Русский/Ryska</a> |
  <a href="./README.it.md">Italiano/Italienska</a> |
  <a href="./README.tr.md">Türkçe/Turkiska</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Thailändska</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamesiska</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonesiska</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malajiska</a> |
  <a href="./README.pl.md">Polski/Polska</a> |
  <a href="./README.pt.md">Português/Portugisiska</a> |
  <a href="./README.nl.md">Nederlands/Nederländska</a> |
  <a href="./README.uk.md">Українська/Ukrainska</a> |
  <a href="./README.ar.md">العربية/Arabiska</a>
</p>

# TianshangHealth(天殇·悦康)

> **Integritetsfokuserad, helt offline Android-hälsoassistent.**
> Kombinerar menstruationscykelspårning, fitness, sömn, nutrition och stegräkning i en krypterad lokal databas — med tvärdimensionella insikter drivna av AI på enheten.

---

## Höjdpunkter

- **Noll nätverksbehörigheter** — All data stannar på din enhet. Inget moln, ingen analys, inget Firebase.
- **Militärklassad kryptering** — SQLCipher AES-256-databas skyddad av Android Keystores hårdvarubaserade nycklar.
- **Modulär arkitektur** — 12 Gradle-moduler enligt MVVM + Repository + Hilt + Compose.
- **Lokal AI-inferens** — TensorFlow Lite-modeller förbättrar cykelprediktion och humöranalys utan att lämna enheten.
- **21 språk** — Inklusive RTL-stöd för arabiska; lokaliserad från dag ett.
- **Könsanpassat gränssnitt** — Bottennavigering och funktioner anpassas baserat på användarens valda kön.

---

## Funktioner

| Domän | Möjligheter |
|-------|-------------|
| **Cykelspårning** | Kalendervy, cykelprediktionsmotor (IQR-filtrering + exponentiellt avklingande), symptomsregistrering, påminnelser |
| **Steg** | Hårdvarustegräknare i förgrundstjänst + accelerometer som reserv, WorkManager-synkronisering, steginsikter per cykelfas |
| **Fitness** | 17 träningstyper, MET-baserad kaloriberäknare, cykelmedvetna träningsrekommendationer |
| **Sömn** | Manuell sömnregistrering, kvalitetspoäng, Canvas trenddiagram |
| **Nutrition** | Måltidsregistrering (frukost/lunch/middag/mellanmål), makrospårning, vattenintag |
| **Analys** | Tvärdimensionell analysmotor, hälsorekommendationer, export av medicinska rapporter, TFLite-prediktionsförbättring |
| **Säkerhet** | PIN (Argon2id) + biometrisk låsning, krypterad ZIP-säkerhetskopiering/återställning, skärmdumpskydd på känsliga skärmar |

---

## Teknikstack

| Lager | Teknik | Version |
|-------|--------|---------|
| Språk | Kotlin | 1.9.24 |
| Bygg | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Arkitektur | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigering | Navigation Compose | 2.7.6 |
| Databas | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Bakgrund | WorkManager | 2.9.0 |
| Kryptering | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Diagram | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Statisk analys | Detekt | 1.23.6 |

---

## Arkitektur

```
app/                    # App-skal: enkel NavHost, tema, MainActivity
├── navigation/MainNavigation.kt        # Villkorlig bottennavigering + sidvägar
core/
├── common/             # Delade UI-komponenter, verktyg, alla strängresurser (21 språk)
├── database/           # Room-entiteter, DAO:er, repositories, migreringar
└── security/           # Keystore, SQLCipher, Argon2id, biometrisk autentisering, app-lås
feature/
├── onboarding/         # Könsval vid första start
├── dashboard/          # Dagens översikt, hälsoinsikter
├── period/             # Menstruationsspårning, prediktion, påminnelser, säkerhetskopiering, inställningar
├── steps/              # Stegräknartjänst + WorkManager-synkronisering
├── fitness/            # Träningsposter + cykelmedvetna rekommendationer
├── sleep/              # Sömnregistrering + trenddiagram
├── nutrition/          # Måltids- + vattenspårning
└── analysis/           # Tvärdimensionell analys + TFLite ML-inferens
```

---

## Säkerhet och integritet

- **Data i vila**: SQLCipher AES-256 krypterar hela `tianshang_health.db`-databasen. Databaslösenordet är en slumpmässig 32-teckensträng krypterad av Android Keystore och lagras aldrig i klartext.
- **Autentisering**: PIN-hashar med Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) finns som reserv.
- **Nätverk**: `AndroidManifest.xml` **innehåller inget `INTERNET`-tillstånd**. Inget Firebase, ingen analys, ingen krashrapportering.
- **App-lås**: Vid bakgrundsläggning aktiveras en konfigurerbar fördröjning (omedelbart / 30s / 1min / 5min). Låsskärmen täcker hela appen med `zIndex(10f)`.
- **Säkerhetskopiering**: Krypterad ZIP-export/återställning med användarangivet lösenord. CSV-export stöds också.
- **Härdning**: `FLAG_SECURE` på känsliga skärmar; `Debug.isDebuggerConnected()`-kontroll vid start.

---

## Kom igång

### Förutsättningar

- JDK 17
- Android Studio (Ladybug eller nyare rekommenderas)
- Android SDK med API 35 installerat

### Bygg Debug APK

```bash
./gradlew :app:assembleDebug
```

APK:n finns på:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installera på en enhet

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Bygg Release APK

Release-byggen kräver signeringskonfiguration i `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Kör sedan:

```bash
./gradlew :app:assembleRelease
```

Den signerade APK:n heter:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Säkerhetskontroll före release

```bash
python check_release_security.py
```

---

## Kvalitet och testning

- **Detekt**: Statisk analys med nolltolerans (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Enhetstester**:
  ```bash
  ./gradlew test
  ```
- **Instrumenteringstester**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Strängvalidering**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Flerspråkigt stöd

Alla 21 språks strängresurser är centraliserade i `core/common/src/main/res/values[-lang]/strings.xml`:

English/Engelska, 简体中文/Kinesiska (förenklad), 日本語/Japanska, 한국어/Koreanska, Français/Franska, Español/Spanska, Deutsch/Tyska, Русский/Ryska, Italiano/Italienska, Türkçe/Turkiska, हिन्दी/Hindi, ภาษาไทย/ Thailändska, Tiếng Việt/Vietnamesiska, Bahasa Indonesia/Indonesiska, Bahasa Melayu/Malajiska, Polski/Polska, Português/Portugisiska, Nederlands/Nederländska, Svenska/Svenska, Українська/Ukrainska, العربية/Arabiska

---

## Licens

Detta projekt är licensierat under MIT-licensen. Se projektfilerna för detaljer.

---

<p align="center">
  <b>Datasuveränitet tillhör användaren. Utvecklaren kan tekniskt sett inte komma åt dina data.</b>
</p>
