<p align="right">
  <a href="../README.md">English/İngilizce</a> |
  <a href="./README.zh-CN.md">简体中文/Çince (basitleştirilmiş)</a> |
  <a href="./README.ja.md">日本語/Japonca</a> |
  <a href="./README.ko.md">한국어/Korece</a> |
  <a href="./README.fr.md">Français/Fransızca</a> |
  <a href="./README.es.md">Español/İspanyolca</a> |
  <a href="./README.de.md">Deutsch/Almanca</a> |
  <a href="./README.ru.md">Русский/Rusça</a> |
  <a href="./README.it.md">Italiano/İtalyanca</a> |
  <a href="./README.hi.md">हिन्दी/Hintçe</a> |
  <a href="./README.th.md">ภาษาไทย/Tayca</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamca</a> |
  <a href="./README.id.md">Bahasa Indonesia/Endonezce</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malayca</a> |
  <a href="./README.pl.md">Polski/Lehçe</a> |
  <a href="./README.pt.md">Português/Portekizce</a> |
  <a href="./README.nl.md">Nederlands/Flemenkçe</a> |
  <a href="./README.sv.md">Svenska/İsveççe</a> |
  <a href="./README.uk.md">Українська/Ukraynaca</a> |
  <a href="./README.ar.md">العربية/Arapça</a>
</p>

# TianshangHealth

> **Gizlilik odaklı, tamamen çevrimdışı Android sağlık asistanı.**
> Adet döngüsü takibi, fitness, uyku, beslenme ve adım sayımını tek bir şifrelenmiş yerel veritabanında birleştirir — cihaz üzerinde yapay zeka destekli çok boyutlu analizlerle.

---

## Öne çıkanlar

- **Sıfır ağ izni** — Tüm veriler cihazınızda kalır. Bulut yok, analitik yok, Firebase yok.
- **Askeri düzeyde şifreleme** — SQLCipher AES-256 veritabanı, Android Keystore donanım destekli anahtarlarla korunur.
- **Modüler mimari** — MVVM + Repository + Hilt + Compose ile 12 Gradle modülü.
- **Yerel AI çıkarımı** — TensorFlow Lite modelleri, cihazdan çıkmadan adet tahmini ve ruh hali analizini iyileştirir.
- **21 dil** — Arapça için RTL desteği dahil; ilk günden itibaren yerelleştirilmiştir.
- **Cinsiyete uyumlu arayüz** — Alt navigasyon ve özellikler, seçilen cinsiyete göre uyarlanır.

---

## Özellikler

| Alan | Yetenekler |
|------|------------|
| **Döngü takibi** | Takvim görünümü, döngü tahmin motoru (IQR filtreleme + üstel azalma), semptom kaydı, hatırlatıcılar |
| **Adımlar** | Donanım adım sayar ön plan servisi + ivmeölçer yedekleme, WorkManager senkronizasyonu, döngü evresine göre adım analizi |
| **Fitness** | 17 egzersiz türü, MET bazlı kalori hesaplama, döngü evresine duyarlı egzersiz önerileri |
| **Uyku** | Manuel uyku kaydı, kalite puanlaması, Canvas trend grafiği |
| **Beslenme** | Öğün kaydı (kahvaltı/öğle/akşam/ara öğün), makro takibi, su tüketimi |
| **Analiz** | Çok boyutlu analitik motoru, sağlık önerileri, tıbbi rapor dışa aktarma, TFLite tahmin iyileştirmesi |
| **Güvenlik** | PIN (Argon2id) + biyometrik kilit, şifrelenmiş ZIP yedekleme/geri yükleme, hassas ekranlarda ekran görüntüsü koruması |

---

## Teknoloji yığını

| Katman | Teknoloji | Sürüm |
|--------|-----------|-------|
| Dil | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Mimari | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigasyon | Navigation Compose | 2.7.6 |
| Veritabanı | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Arka plan | WorkManager | 2.9.0 |
| Kripto | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Grafikler | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Statik analiz | Detekt | 1.23.6 |

---

## Mimari

```
app/                    # Uygulama kabuğu: tek NavHost, tema, MainActivity
├── navigation/MainNavigation.kt        # Koşullu alt navigasyon + yan rotalar
core/
├── common/             # Paylaşılan UI bileşenleri, yardımcılar, tüm dize kaynakları (21 dil)
├── database/           # Room varlıkları, DAO'lar, depo birimleri, geçişler
└── security/           # Keystore, SQLCipher, Argon2id, biyometrik kimlik doğrulama, uygulama kilidi
feature/
├── onboarding/         # İlk kullanım: cinsiyet seçimi
├── dashboard/          # Günlük genel bakış, sağlık içgörüleri
├── period/             # Adet döngüsü takibi, tahmin, hatırlatıcılar, yedekleme, ayarlar
├── steps/              # Adım sayar servisi + WorkManager senkronizasyonu
├── fitness/            # Egzersiz kayıtları + döngü evresine göre öneriler
├── sleep/              # Uyku kaydı + trend grafiği
├── nutrition/          # Öğün + su takibi
└── analysis/           # Çok boyutlu analizler + TFLite ML çıkarımı
```

---

## Güvenlik ve gizlilik

- **Bekleyen veriler**: SQLCipher AES-256, tüm `tianshang_health.db` veritabanını şifreler. Veritabanı şifresi, Android Keystore tarafından şifrelenmiş 32 karakterlik rastgele bir dizedir ve asla düz metin olarak saklanmaz.
- **Kimlik doğrulama**: PIN'ler Argon2id ile hashlenir. BiometricPrompt (`BIOMETRIC_STRONG`) yedek olarak kullanılabilir.
- **Ağ**: `AndroidManifest.xml` **`INTERNET` izni içermez**. Firebase, analitik veya crash raporlaması yoktur.
- **Uygulama kilidi**: Arka plana geçildiğinde yapılandırılabilir bir gecikme etkinleşir (hemen / 30s / 1dk / 5dk). Kilit ekranı tüm uygulamayı `zIndex(10f)` ile kaplar.
- **Yedekleme**: Kullanıcı tarafından belirlenen şifre ile şifrelenmiş ZIP dışa/içe aktarma. CSV dışa aktarma da desteklenir.
- **Sertleştirme**: Hassas ekranlarda `FLAG_SECURE`; başlangıçta `Debug.isDebuggerConnected()` kontrolü.

---

## Başlarken

### Ön koşullar

- JDK 17
- Android Studio (Ladybug veya daha yenisi önerilir)
- API 35 yüklü Android SDK

### Debug APK derleme

```bash
./gradlew :app:assembleDebug
```

APK şu konumda olacaktır:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Bir cihaza yükleme

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release APK derleme

Release yapıları `local.properties` içinde imza yapılandırması gerektirir:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Ardından çalıştırın:

```bash
./gradlew :app:assembleRelease
```

İmzalı APK şu şekilde adlandırılacaktır:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Sürüm öncesi güvenlik kontrolü

```bash
python check_release_security.py
```

---

## Kalite ve test

- **Detekt**: Sıfır toleranslı statik analiz (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Birim testleri**:
  ```bash
  ./gradlew test
  ```
- **Enstrümantasyon testleri**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Dize doğrulama**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Çoklu dil desteği

21 dildeki tüm dize kaynakları `core/common/src/main/res/values[-lang]/strings.xml` içinde merkezileştirilmiştir:

English/İngilizce, 简体中文/Çince (basitleştirilmiş), 日本語/Japonca, 한국어/Korece, Français/Fransızca, Español/İspanyolca, Deutsch/Almanca, Русский/Rusça, Italiano/İtalyanca, Türkçe/Türkçe, हिन्दी/Hintçe, ภาษาไทย/Tayca, Tiếng Việt/Vietnamca, Bahasa Indonesia/Endonezce, Bahasa Melayu/Malayca, Polski/Lehçe, Português/Portekizce, Nederlands/Flemenkçe, Svenska/İsveççe, Українська/Ukraynaca, العربية/Arapça

---

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır. Ayrıntılar için proje dosyalarına bakın.

---

<p align="center">
  <b>Veri egemenliği kullanıcıya aittir. Geliştirici, verilerinize teknik olarak erişemez.</b>
</p>
