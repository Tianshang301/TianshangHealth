<p align="right">
  <a href="../README.md">English/Inggeris</a> |
  <a href="./README.zh-CN.md">简体中文/Cina (Ringkas)</a> |
  <a href="./README.ja.md">日本語/Jepun</a> |
  <a href="./README.ko.md">한국어/Korea</a> |
  <a href="./README.fr.md">Français/Perancis</a> |
  <a href="./README.es.md">Español/Sepanyol</a> |
  <a href="./README.de.md">Deutsch/Jerman</a> |
  <a href="./README.ru.md">Русский/Rusia</a> |
  <a href="./README.it.md">Italiano/Itali</a> |
  <a href="./README.tr.md">Türkçe/Turki</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Thai</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnam</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonesia</a> |
  <a href="./README.pl.md">Polski/Poland</a> |
  <a href="./README.pt.md">Português/Portugis</a> |
  <a href="./README.nl.md">Nederlands/Belanda</a> |
  <a href="./README.sv.md">Svenska/Sweden</a> |
  <a href="./README.uk.md">Українська/Ukraine</a> |
  <a href="./README.ar.md">العربية/Arab</a>
</p>

# TianshangHealth(天殇·悦康)

> **Rakan kesihatan Android luar talian sepenuhnya yang mengutamakan privasi.**
> Menggabungkan penjejakan kitaran haid, kecergasan, tidur, pemakanan, dan pengiraan langkah ke dalam satu pangkalan data setempat yang disulitkan — dengan cerapan merentas dimensi berasaskan AI pada peranti.

---

## Sorotan

- **Sifar kebenaran rangkaian** — Semua data kekal pada peranti anda. Tiada awan, tiada analitik, tiada Firebase.
- **Penyulitan peringkat tentera** — Pangkalan data SQLCipher AES-256 dilindungi oleh kunci perkakasan Android Keystore.
- **Seni bina modular** — 12 modul Gradle mengikut MVVM + Repository + Hilt + Compose.
- **Inferens AI setempat** — Model TensorFlow Lite meningkatkan ramalan kitaran dan analisis mood tanpa meninggalkan peranti.
- **21 bahasa** — Termasuk sokongan RTL untuk bahasa Arab; disetempatkan sejak hari pertama.
- **UI adaptif jantina** — Navigasi bawah dan ciri menyesuaikan berdasarkan jantina yang dipilih pengguna.

---

## Ciri

| Domain | Keupayaan |
|--------|-----------|
| **Penjejakan kitaran** | Paparan kalendar, enjin ramalan kitaran (penapisan IQR + pereputan eksponen), pencatatan gejala, peringatan |
| **Langkah** | Perkhidmatan latar depan penghitung langkah perkakasan + pecut meter sandaran, penyegerakan WorkManager, cerapan langkah mengikut fasa kitaran |
| **Kecergasan** | 17 jenis senaman, kalkulator kalori MET, cadangan senaman mengikut fasa kitaran |
| **Tidur** | Pencatatan tidur manual, penilaian kualiti, carta trend Canvas |
| **Pemakanan** | Pencatatan makanan (sarapan/makan tengah/makan malam/snek), penjejakan makro, pengambilan air |
| **Analisis** | Enjin analitik merentas dimensi, cadangan kesihatan, eksport laporan perubatan, peningkatan ramalan TFLite |
| **Keselamatan** | PIN (Argon2id) + kunci biometrik, sandaran/pulihkan ZIP bersulit, perlindungan tangkapan skrin pada skrin sensitif |

---

## Tindanan Teknologi

| Lapisan | Teknologi | Versi |
|---------|-----------|-------|
| Bahasa | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Seni bina | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigasi | Navigation Compose | 2.7.6 |
| Pangkalan data | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Latar belakang | WorkManager | 2.9.0 |
| Kripto | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Carta | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Analisis statik | Detekt | 1.23.6 |

---

## Seni Bina

```
app/                    # Shell aplikasi: NavHost tunggal, tema, MainActivity
├── navigation/MainNavigation.kt        # Navigasi bawah bersyarat + laluan sisi
core/
├── common/             # Komponen UI dikongsi, utiliti, semua sumber rentetan (21 bahasa)
├── database/           # Entiti Room, DAO, repositori, migrasi
└── security/           # Keystore, SQLCipher, Argon2id, pengesahan biometrik, kunci apl
feature/
├── onboarding/         # Pemilihan jantina kali pertama dijalankan
├── dashboard/          # Gambaran keseluruhan hari ini, cerapan kesihatan
├── period/             # Penjejakan kitaran haid, ramalan, peringatan, sandaran, tetapan
├── steps/              # Perkhidmatan penghitung langkah + penyegerakan WorkManager
├── fitness/            # Rekod senaman + cadangan mengikut fasa kitaran
├── sleep/              # Pencatatan tidur + carta trend
├── nutrition/          # Penjejakan makanan + air
└── analysis/           # Analisis merentas dimensi + inferens ML TFLite
```

---

## Keselamatan dan privasi

- **Data semasa rehat**: SQLCipher AES-256 menyulitkan keseluruhan pangkalan data `tianshang_health.db`. Kata laluan pangkalan data adalah rentetan rawak 32 aksara yang disulitkan oleh Android Keystore dan tidak pernah disimpan dalam teks biasa.
- **Pengesahan**: PIN di-hash dengan Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) tersedia sebagai sandaran.
- **Rangkaian**: `AndroidManifest.xml` **tidak mengandungi kebenaran `INTERNET`**. Tiada Firebase, tiada analitik, tiada pelaporan ranap.
- **Kunci apl**: Apabila apl masuk ke latar belakang, kelewatan boleh dikonfigurasi akan diaktifkan (serta-merta / 30s / 1min / 5min). Skrin kunci menutupi seluruh apl pada `zIndex(10f)`.
- **Sandaran**: Eksport/pulihkan ZIP bersulit dengan kata laluan yang diberikan pengguna. Eksport CSV juga disokong.
- **Pengukuhan**: `FLAG_SECURE` pada skrin sensitif; pemeriksaan `Debug.isDebuggerConnected()` semasa permulaan.

---

## Memulakan

### Prasyarat

- JDK 17
- Android Studio (disyorkan Ladybug atau lebih baru)
- Android SDK dengan API 35 dipasang

### Bina APK Debug

```bash
./gradlew :app:assembleDebug
```

APK akan terletak di:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Pasang pada peranti

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Bina APK Release

Bina Release memerlukan konfigurasi tandatangan dalam `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Kemudian jalankan:

```bash
./gradlew :app:assembleRelease
```

APK yang ditandatangani akan dinamakan:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Pemeriksaan keselamatan pra-keluaran

```bash
python check_release_security.py
```

---

## Kualiti dan pengujian

- **Detekt**: Analisis statik toleransi sifar (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Ujian unit**:
  ```bash
  ./gradlew test
  ```
- **Ujian instrumentasi**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Pengesahan rentetan**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Sokongan pelbagai bahasa

Semua sumber rentetan dalam 21 bahasa dipusatkan di `core/common/src/main/res/values[-lang]/strings.xml`:

English/Inggeris, 简体中文/Cina (Ringkas), 日本語/Jepun, 한국어/Korea, Français/Perancis, Español/Sepanyol, Deutsch/Jerman, Русский/Rusia, Italiano/Itali, Türkçe/Turki, हिन्दी/Hindi, ภาษาไทย/Thai, Tiếng Việt/Vietnam, Bahasa Indonesia/Indonesia, Bahasa Melayu/Melayu, Polski/Poland, Português/Portugis, Nederlands/Belanda, Svenska/Sweden, Українська/Ukraine, العربية/Arab

---

## Lesen

Projek ini dilesenkan di bawah Lesen MIT. Lihat fail projek untuk butiran.

---

<p align="center">
  <b>Kedaulatan data adalah milik pengguna. Pemaju secara teknikal tidak boleh mengakses data anda.</b>
</p>
