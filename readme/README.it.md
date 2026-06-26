<p align="right">
  <a href="../README.md">English/Inglese</a> |
  <a href="./README.zh-CN.md">简体中文/Cinese (semplificato)</a> |
  <a href="./README.ja.md">日本語/Giapponese</a> |
  <a href="./README.ko.md">한국어/Coreano</a> |
  <a href="./README.fr.md">Français/Francese</a> |
  <a href="./README.es.md">Español/Spagnolo</a> |
  <a href="./README.de.md">Deutsch/Tedesco</a> |
  <a href="./README.ru.md">Русский/Russo</a> |
  <a href="./README.tr.md">Türkçe/Turco</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Tailandese</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamita</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonesiano</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malese</a> |
  <a href="./README.pl.md">Polacco/Polacco</a> |
  <a href="./README.pt.md">Português/Portoghese</a> |
  <a href="./README.nl.md">Nederlands/Olandese</a> |
  <a href="./README.sv.md">Svedese/Svedese</a> |
  <a href="./README.uk.md">Українська/Ucraino</a> |
  <a href="./README.ar.md">العربية/Arabo</a>
</p>

# TianshangHealth(天殇·悦康)

> **Compagno sanitario Android completamente offline incentrato sulla privacy.**
> Combina monitoraggio del ciclo, fitness, sonno, alimentazione e conteggio passi in un unico database locale crittografato — con analisi multidimensionali basate su intelligenza artificiale locale.

---

## Punti salienti

- **Zero permessi di rete** — Tutti i dati rimangono sul tuo dispositivo. Nessun cloud, nessuna analisi, nessun Firebase.
- **Crittografia militare** — Database SQLCipher AES-256 protetto da chiavi hardware Android Keystore.
- **Architettura modulare** — 12 moduli Gradle secondo MVVM + Repository + Hilt + Compose.
- **Inferenza AI locale** — I modelli TensorFlow Lite migliorano la previsione del ciclo e l'analisi dell'umore senza lasciare il dispositivo.
- **21 lingue** — Incluso supporto RTL per l'arabo; localizzato dal primo giorno.
- **Interfaccia adattiva al genere** — La navigazione inferiore e le funzionalità si adattano in base al genere selezionato.

---

## Funzionalità

| Dominio | Capacità |
|---------|----------|
| **Monitoraggio ciclo** | Vista calendario, motore di previsione (filtraggio IQR + decadimento esponenziale), registro sintomi, promemoria |
| **Passi** | Servizio in primo piano contapassi hardware + accelerometro di riserva, sincronizzazione WorkManager, analisi passi per fase del ciclo |
| **Fitness** | 17 tipi di esercizi, calcolo calorico MET, raccomandazioni allenamento in base alla fase del ciclo |
| **Sonno** | Registrazione manuale del sonno, valutazione qualità, grafico tendenze Canvas |
| **Alimentazione** | Registrazione pasti (colazione/pranzo/cena/spuntino), monitoraggio nutrienti, assunzione acqua |
| **Analisi** | Motore analitico multidimensionale, suggerimenti salute, esportazione referti medici, miglioramento previsioni TFLite |
| **Sicurezza** | PIN (Argon2id) + blocco biometrico, backup/ripristino ZIP crittografato, protezione screenshot su schermate sensibili |

---

## Stack tecnologico

| Livello | Tecnologia | Versione |
|---------|-----------|----------|
| Linguaggio | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Architettura | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigazione | Navigation Compose | 2.7.6 |
| Database | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Sfondo | WorkManager | 2.9.0 |
| Crittografia | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Grafici | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Analisi statica | Detekt | 1.23.6 |

---

## Architettura

```
app/                    # Shell app: singolo NavHost, tema, MainActivity
├── navigation/MainNavigation.kt        # Navigazione inferiore condizionale + rotte laterali
core/
├── common/             # Componenti UI condivisi, utility, tutte le risorse stringa (21 lingue)
├── database/           # Entità Room, DAO, repository, migrazioni
└── security/           # Keystore, SQLCipher, Argon2id, autenticazione biometrica, blocco app
feature/
├── onboarding/         # Selezione genere al primo avvio
├── dashboard/          # Panoramica giornaliera, approfondimenti salute
├── period/             # Monitoraggio ciclo, previsione, promemoria, backup, impostazioni
├── steps/              # Servizio contapassi + sincronizzazione WorkManager
├── fitness/            # Registrazioni allenamenti + raccomandazioni per fase ciclo
├── sleep/              # Registrazione sonno + grafico tendenze
├── nutrition/          # Monitoraggio pasti + acqua
└── analysis/           # Analisi multidimensionali + inferenza ML TFLite
```

---

## Sicurezza e privacy

- **Dati inattivi**: SQLCipher AES-256 crittografa l'intero `tianshang_health.db`. La password del database è una stringa casuale di 32 caratteri crittografata da Android Keystore e mai archiviata in chiaro.
- **Autenticazione**: I PIN sono hash con Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) è disponibile come alternativa.
- **Rete**: `AndroidManifest.xml` **non contiene permesso `INTERNET`**. Nessun Firebase, nessuna analisi, nessuna segnalazione crash.
- **Blocco app**: Quando l'app va in background, si attiva un ritardo configurabile (immediato / 30s / 1min / 5min). La schermata di blocco sovrappone l'intera app a `zIndex(10f)`.
- **Backup**: Esportazione/ripristino ZIP crittografato con password fornita dall'utente. Supportata anche esportazione CSV.
- **Rafforzamento**: `FLAG_SECURE` su schermate sensibili; controllo `Debug.isDebuggerConnected()` all'avvio.

---

## Per iniziare

### Prerequisiti

- JDK 17
- Android Studio (consigliato Ladybug o successivo)
- Android SDK con API 35 installata

### Compilare APK Debug

```bash
./gradlew :app:assembleDebug
```

L'APK si troverà in:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installare su un dispositivo

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Compilare APK Release

Le build Release richiedono la configurazione di firma in `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Quindi esegui:

```bash
./gradlew :app:assembleRelease
```

L'APK firmato sarà nominato:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Controllo sicurezza pre-release

```bash
python check_release_security.py
```

---

## Qualità e test

- **Detekt**: Analisi statica a tolleranza zero (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Test unitari**:
  ```bash
  ./gradlew test
  ```
- **Test strumentali**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Validazione stringhe**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Supporto multilingua

Tutte le risorse stringa in 21 lingue sono centralizzate in `core/common/src/main/res/values[-lang]/strings.xml`:

English/Inglese, 简体中文/Cinese (semplificato), 日本語/Giapponese, 한국어/Coreano, Français/Francese, Español/Spagnolo, Deutsch/Tedesco, Русский/Russo, Italiano/Italiano, Türkçe/Turco, हिन्दी/Hindi, ภาษาไทย/Tailandese, Tiếng Việt/Vietnamita, Bahasa Indonesia/Indonesiano, Bahasa Melayu/Malese, Polski/Polacco, Português/Portoghese, Nederlands/Olandese, Svenska/Svedese, Українська/Ucraino, العربية/Arabo

---

## Licenza

Questo progetto è concesso in licenza MIT. Consulta i file del progetto per i dettagli.

---

<p align="center">
  <b>La sovranità dei dati appartiene all'utente. Lo sviluppatore non può tecnicamente accedere ai tuoi dati.</b>
</p>
