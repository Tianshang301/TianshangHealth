<p align="right">
  <a href="../README.md">English/Inggris</a> |
  <a href="./README.zh-CN.md">简体中文/Mandarin (Sederhana)</a> |
  <a href="./README.ja.md">日本語/Jepang</a> |
  <a href="./README.ko.md">한국어/Korea</a> |
  <a href="./README.fr.md">Français/Prancis</a> |
  <a href="./README.es.md">Español/Spanyol</a> |
  <a href="./README.de.md">Deutsch/Jerman</a> |
  <a href="./README.ru.md">Русский/Rusia</a> |
  <a href="./README.it.md">Italiano/Italia</a> |
  <a href="./README.tr.md">Türkçe/Turki</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Thailand</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnam</a> |
  <a href="./README.ms.md">Bahasa Melayu/Melayu</a> |
  <a href="./README.pl.md">Polski/Polandia</a> |
  <a href="./README.pt.md">Português/Portugis</a> |
  <a href="./README.nl.md">Nederlands/Belanda</a> |
  <a href="./README.sv.md">Svenska/ Swedia</a> |
  <a href="./README.uk.md">Українська/Ukraina</a> |
  <a href="./README.ar.md">العربية/Arab</a>
</p>

# TianshangHealth(天殇·悦康)

> **Pendamping kesehatan Android yang sepenuhnya offline dan mengutamakan privasi.**
> Menggabungkan pelacakan siklus menstruasi, kebugaran, tidur, nutrisi, dan penghitungan langkah ke dalam satu database lokal terenkripsi — dengan analisis lintas dimensi berbasis AI di perangkat.

---

## Sorotan

- **Izin jaringan nol** — Semua data tetap di perangkat Anda. Tanpa cloud, tanpa analitik, tanpa Firebase.
- **Enkripsi kelas militer** — Database SQLCipher AES-256 diamankan oleh kunci hardware Android Keystore.
- **Arsitektur modular** — 12 modul Gradle mengikuti MVVM + Repository + Hilt + Compose.
- **Inferensi AI lokal** — Model TensorFlow Lite meningkatkan prediksi siklus dan analisis suasana hati tanpa meninggalkan perangkat.
- **21 bahasa** — Termasuk dukungan RTL untuk bahasa Arab; dilokalisasi sejak hari pertama.
- **UI adaptif gender** — Navigasi bawah dan fitur menyesuaikan berdasarkan gender yang dipilih pengguna.

---

## Fitur

| Domain | Kemampuan |
|--------|-----------|
| **Pelacakan siklus** | Tampilan kalender, mesin prediksi siklus (filter IQR + peluruhan eksponensial), pencatatan gejala, pengingat |
| **Langkah** | Layanan latar depan penghitung langkah hardware + akselerometer cadangan, sinkronisasi WorkManager, analisis langkah berdasarkan fase siklus |
| **Kebugaran** | 17 jenis olahraga, kalkulator kalori MET, rekomendasi latihan sesuai fase siklus |
| **Tidur** | Pencatatan tidur manual, penilaian kualitas, grafik tren Canvas |
| **Nutrisi** | Pencatatan makanan (sarapan/makan siang/makan malam/camilan), pelacakan makro, asupan air |
| **Analisis** | Mesin analitik lintas dimensi, saran kesehatan, ekspor laporan medis, peningkatan prediksi TFLite |
| **Keamanan** | PIN (Argon2id) + kunci biometrik, cadangan/pemulihan ZIP terenkripsi, perlindungan tangkapan layar di layar sensitif |

---

## Tumpukan Teknologi

| Lapisan | Teknologi | Versi |
|---------|-----------|-------|
| Bahasa | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Arsitektur | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigasi | Navigation Compose | 2.7.6 |
| Database | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Latar belakang | WorkManager | 2.9.0 |
| Kripto | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Grafik | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Analisis statis | Detekt | 1.23.6 |

---

## Arsitektur

```
app/                    # Shell aplikasi: NavHost tunggal, tema, MainActivity
├── navigation/MainNavigation.kt        # Navigasi bawah bersyarat + rute samping
core/
├── common/             # Komponen UI bersama, utilitas, semua sumber daya string (21 bahasa)
├── database/           # Entitas Room, DAO, repositori, migrasi
└── security/           # Keystore, SQLCipher, Argon2id, autentikasi biometrik, kunci aplikasi
feature/
├── onboarding/         # Pemilihan gender saat pertama kali dijalankan
├── dashboard/          # Ikhtisar hari ini, wawasan kesehatan
├── period/             # Pelacakan siklus menstruasi, prediksi, pengingat, cadangan, pengaturan
├── steps/              # Layanan penghitung langkah + sinkronisasi WorkManager
├── fitness/            # Catatan olahraga + rekomendasi sesuai fase siklus
├── sleep/              # Pencatatan tidur + grafik tren
├── nutrition/          # Pelacakan makanan + air
└── analysis/           # Analisis lintas dimensi + inferensi ML TFLite
```

---

## Keamanan dan privasi

- **Data saat diam**: SQLCipher AES-256 mengenkripsi seluruh database `tianshang_health.db`. Kata sandi database adalah string acak 32 karakter yang dienkripsi oleh Android Keystore dan tidak pernah disimpan dalam teks biasa.
- **Autentikasi**: PIN di-hash dengan Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) tersedia sebagai cadangan.
- **Jaringan**: `AndroidManifest.xml` **tidak mengandung izin `INTERNET`**. Tanpa Firebase, tanpa analitik, tanpa pelaporan crash.
- **Kunci aplikasi**: Saat aplikasi masuk ke latar belakang, penundaan yang dapat dikonfigurasi akan aktif (segera / 30d / 1m / 5m). Layar kunci menutupi seluruh aplikasi dengan `zIndex(10f)`.
- **Cadangan**: Ekspor/pemulihan ZIP terenkripsi dengan kata sandi yang diberikan pengguna. Ekspor CSV juga didukung.
- **Penguatan**: `FLAG_SECURE` di layar sensitif; pemeriksaan `Debug.isDebuggerConnected()` saat startup.

---

## Memulai

### Prasyarat

- JDK 17
- Android Studio (disarankan Ladybug atau lebih baru)
- Android SDK dengan API 35 terinstal

### Build APK Debug

```bash
./gradlew :app:assembleDebug
```

APK akan berada di:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Instal di perangkat

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build APK Release

Build Release memerlukan konfigurasi tanda tangan di `local.properties`:

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

APK yang ditandatangani akan diberi nama:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Pemeriksaan keamanan pra-rilis

```bash
python check_release_security.py
```

---

## Kualitas dan pengujian

- **Detekt**: Analisis statis tanpa toleransi (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Tes unit**:
  ```bash
  ./gradlew test
  ```
- **Tes instrumentasi**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Validasi string**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Dukungan multi-bahasa

Semua sumber daya string dalam 21 bahasa terpusat di `core/common/src/main/res/values[-lang]/strings.xml`:

English/Inggris, 简体中文/Mandarin (Sederhana), 日本語/Jepang, 한국어/Korea, Français/Prancis, Español/Spanyol, Deutsch/Jerman, Русский/Rusia, Italiano/Italia, Türkçe/Turki, हिन्दी/Hindi, ภาษาไทย/Thailand, Tiếng Việt/Vietnam, Bahasa Indonesia/Indonesia, Bahasa Melayu/Melayu, Polski/Polandia, Português/Portugis, Nederlands/Belanda, Svenska/Swedia, Українська/Ukraina, العربية/Arab

---

## Lisensi

Proyek ini dilisensikan di bawah Lisensi MIT. Lihat file proyek untuk detailnya.

---

<p align="center">
  <b>Kedaulatan data milik pengguna. Pengembang secara teknis tidak dapat mengakses data Anda.</b>
</p>
