<p align="right">
  <a href="../README.md">English/Angielski</a> |
  <a href="./README.zh-CN.md">简体中文/Chiński (uproszczony)</a> |
  <a href="./README.ja.md">日本語/Japoński</a> |
  <a href="./README.ko.md">한국어/Koreański</a> |
  <a href="./README.fr.md">Français/Francuski</a> |
  <a href="./README.es.md">Español/Hiszpański</a> |
  <a href="./README.de.md">Deutsch/Niemiecki</a> |
  <a href="./README.ru.md">Русский/Rosyjski</a> |
  <a href="./README.it.md">Italiano/Włoski</a> |
  <a href="./README.tr.md">Türkçe/Turecki</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Tajski</a> |
  <a href="./README.vi.md">Tiếng Việt/Wietnamski</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonezyjski</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malezyjski</a> |
  <a href="./README.pt.md">Português/Portugalski</a> |
  <a href="./README.nl.md">Nederlands/Holenderski</a> |
  <a href="./README.sv.md">Svenska/Szwedzki</a> |
  <a href="./README.uk.md">Українська/Ukraiński</a> |
  <a href="./README.ar.md">العربية/Arabski</a>
</p>

# TianshangHealth(天殇·悦康)

> **Przyjazny prywatności, w pełni offline, asystent zdrowia na Androida.**
> Łączy śledzenie cyklu miesiączkowego, fitness, sen, odżywianie i liczenie kroków w jednej zaszyfrowanej lokalnej bazie danych — z wielowymiarową analizą opartą na lokalnym AI.

---

## Najważniejsze cechy

- **Zero uprawnień sieciowych** — Wszystkie dane pozostają na Twoim urządzeniu. Brak chmury, analityki, Firebase.
- **Szyfrowanie klasy wojskowej** — Baza danych SQLCipher AES-256 zabezpieczona kluczami sprzętowymi Android Keystore.
- **Modułowa architektura** — 12 modułów Gradle zgodnie z MVVM + Repository + Hilt + Compose.
- **Lokalne wnioskowanie AI** — Modele TensorFlow Lite usprawniają przewidywanie cyklu i analizę nastroju bez opuszczania urządzenia.
- **21 języków** — W tym wsparcie RTL dla arabskiego; zlokalizowany od pierwszego dnia.
- **Interfejs adaptowalny do płci** — Dolna nawigacja i funkcje dostosowują się do wybranej płci.

---

## Funkcje

| Dziedzina | Możliwości |
|-----------|------------|
| **Śledzenie cyklu** | Widok kalendarza, silnik przewidywania cyklu (filtracja IQR + zanik wykładniczy), rejestrowanie objawów, przypomnienia |
| **Kroki** | Usługa pierwszoplanowa licznika kroków + akcelerometr zapasowy, synchronizacja WorkManager, analiza kroków według fazy cyklu |
| **Fitness** | 17 rodzajów ćwiczeń, kalkulator kalorii MET, zalecenia treningowe zależne od fazy cyklu |
| **Sen** | Ręczne rejestrowanie snu, ocena jakości, wykres trendów Canvas |
| **Odżywianie** | Rejestrowanie posiłków (śniadanie/obiad/kolacja/przekąska), śledzenie makroskładników, spożycie wody |
| **Analiza** | Wielowymiarowy silnik analityczny, sugestie zdrowotne, eksport raportów medycznych, ulepszanie przewidywań TFLite |
| **Bezpieczeństwo** | PIN (Argon2id) + blokada biometryczna, zaszyfrowana kopia ZIP/ przywracanie, ochrona zrzutów ekranu na wrażliwych ekranach |

---

## Stos technologiczny

| Warstwa | Technologia | Wersja |
|---------|------------|--------|
| Język | Kotlin | 1.9.24 |
| Budowanie | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Architektura | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Nawigacja | Navigation Compose | 2.7.6 |
| Baza danych | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Tło | WorkManager | 2.9.0 |
| Kryptografia | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Wykresy | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Analiza statyczna | Detekt | 1.23.6 |

---

## Architektura

```
app/                    # Powłoka aplikacji: pojedynczy NavHost, motyw, MainActivity
├── navigation/MainNavigation.kt        # Warunkowa nawigacja dolna + trasy boczne
core/
├── common/             # Wspólne komponenty UI, narzędzia, wszystkie zasoby językowe (21 języków)
├── database/           # Encje Room, DAO, repozytoria, migracje
└── security/           # Keystore, SQLCipher, Argon2id, uwierzytelnianie biometryczne, blokada aplikacji
feature/
├── onboarding/         # Wybór płci przy pierwszym uruchomieniu
├── dashboard/          # Dzisiejszy przegląd, wskazówki zdrowotne
├── period/             # Śledzenie cyklu, przewidywanie, przypomnienia, kopie zapasowe, ustawienia
├── steps/              # Usługa licznika kroków + synchronizacja WorkManager
├── fitness/            # Rekordy treningów + zalecenia zależne od fazy cyklu
├── sleep/              # Rejestrowanie snu + wykres trendów
├── nutrition/          # Śledzenie posiłków + wody
└── analysis/           # Wielowymiarowa analiza + wnioskowanie ML TFLite
```

---

## Bezpieczeństwo i prywatność

- **Dane w spoczynku**: SQLCipher AES-256 szyfruje całą bazę `tianshang_health.db`. Hasło bazy danych to losowy 32-znakowy ciąg zaszyfrowany przez Android Keystore i nigdy nie przechowywany w czystym tekście.
- **Uwierzytelnianie**: PIN-y są haszowane za pomocą Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) jest dostępny jako zapasowy.
- **Sieć**: `AndroidManifest.xml` **nie zawiera uprawnienia `INTERNET`**. Brak Firebase, analityki, raportowania awarii.
- **Blokada aplikacji**: Po przejściu do tła aktywowane jest konfigurowalne opóźnienie (natychmiast / 30s / 1min / 5min). Ekran blokady nakłada się na całą aplikację z `zIndex(10f)`.
- **Kopia zapasowa**: Zaszyfrowany eksport/przywracanie ZIP z hasłem podanym przez użytkownika. Obsługiwany jest również eksport CSV.
- **Wzmocnienie**: `FLAG_SECURE` na wrażliwych ekranach; sprawdzanie `Debug.isDebuggerConnected()` przy starcie.

---

## Pierwsze kroki

### Wymagania

- JDK 17
- Android Studio (zalecane Ladybug lub nowsze)
- Android SDK z zainstalowanym API 35

### Zbuduj APK Debug

```bash
./gradlew :app:assembleDebug
```

APK będzie znajdować się w:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Zainstaluj na urządzeniu

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Zbuduj APK Release

Wersje Release wymagają konfiguracji podpisu w `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Następnie uruchom:

```bash
./gradlew :app:assembleRelease
```

Podpisany APK będzie nazwany:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Przedwydaniowe sprawdzenie bezpieczeństwa

```bash
python check_release_security.py
```

---

## Jakość i testowanie

- **Detekt**: Analityka statyczna z zerową tolerancją (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Testy jednostkowe**:
  ```bash
  ./gradlew test
  ```
- **Testy instrumentalne**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Walidacja ciągów**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Wsparcie wielojęzykowe

Wszystkie zasoby językowe w 21 językach są scentralizowane w `core/common/src/main/res/values[-lang]/strings.xml`:

English/Angielski, 简体中文/Chiński (uproszczony), 日本語/Japoński, 한국어/Koreański, Français/Francuski, Español/Hiszpański, Deutsch/Niemiecki, Русский/Rosyjski, Italiano/Włoski, Türkçe/Turecki, हिन्दी/Hindi, ภาษาไทย/Tajski, Tiếng Việt/Wietnamski, Bahasa Indonesia/Indonezyjski, Bahasa Melayu/Malezyjski, Polski/Polski, Português/Portugalski, Nederlands/Holenderski, Svenska/Szwedzki, Українська/Ukraiński, العربية/Arabski

---

## Licencja

Ten projekt jest licencjonowany na licencji MIT. Szczegóły w plikach projektu.

---

<p align="center">
  <b>Suwerenność danych należy do użytkownika. Deweloper technicznie nie ma dostępu do Twoich danych.</b>
</p>
