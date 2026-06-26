<p align="right">
  <a href="../README.md">English/อังกฤษ</a> |
  <a href="./README.zh-CN.md">简体中文/จีน (ตัวย่อ)</a> |
  <a href="./README.ja.md">日本語/ญี่ปุ่น</a> |
  <a href="./README.ko.md">한국어/เกาหลี</a> |
  <a href="./README.fr.md">Français/ฝรั่งเศส</a> |
  <a href="./README.es.md">Español/สเปน</a> |
  <a href="./README.de.md">Deutsch/เยอรมัน</a> |
  <a href="./README.ru.md">Русский/รัสเซีย</a> |
  <a href="./README.it.md">Italiano/อิตาลี</a> |
  <a href="./README.tr.md">Türkçe/ตุรกี</a> |
  <a href="./README.hi.md">हिन्दी/ฮินดี</a> |
  <a href="./README.vi.md">Tiếng Việt/เวียดนาม</a> |
  <a href="./README.id.md">Bahasa Indonesia/อินโดนีเซีย</a> |
  <a href="./README.ms.md">Bahasa Melayu/มาเลย์</a> |
  <a href="./README.pl.md">Polski/โปแลนด์</a> |
  <a href="./README.pt.md">Português/โปรตุเกส</a> |
  <a href="./README.nl.md">Nederlands/ดัตช์</a> |
  <a href="./README.sv.md">Svenska/สวีเดน</a> |
  <a href="./README.uk.md">Українська/ยูเครน</a> |
  <a href="./README.ar.md">العربية/อาหรับ</a>
</p>

# TianshangHealth(天殇·悦康)

> **เพื่อนคู่ใจสุขภาพ Android แบบออฟไลน์ที่ให้ความสำคัญกับความเป็นส่วนตัว**
> รวมการติดตามรอบเดือน ฟิตเนส การนอน โภชนาการ และการนับก้าวเข้าในฐานข้อมูลท้องถิ่นที่เข้ารหัสเดียว — พร้อมการวิเคราะห์หลายมิติด้วย AI บนอุปกรณ์

---

## จุดเด่น

- **ไม่มีสิทธิ์เครือข่าย** — ข้อมูลทั้งหมดอยู่บนอุปกรณ์ของคุณ ไม่มีคลาวด์ ไม่มีการวิเคราะห์ ไม่มี Firebase
- **การเข้ารหัสระดับทหาร** — ฐานข้อมูล SQLCipher AES-256 ป้องกันด้วยคีย์ฮาร์ดแวร์ Android Keystore
- **สถาปัตยกรรมแบบโมดูลาร์** — 12 โมดูล Gradle ตามรูปแบบ MVVM + Repository + Hilt + Compose
- **การอนุมาน AI ในเครื่อง** — โมเดล TensorFlow Lite ช่วยเพิ่มความแม่นยำในการพยากรณ์รอบเดือนและการวิเคราะห์อารมณ์โดยไม่ต้องออกจากอุปกรณ์
- **21 ภาษา** — รวมถึงการรองรับ RTL สำหรับภาษาอาหรับ; มีการแปลเป็นภาษาท้องถิ่นตั้งแต่วันแรก
- **UI ที่ปรับตามเพศ** — แถบนำทางด้านล่างและฟังก์ชันต่างๆ ปรับตามเพศที่ผู้ใช้เลือก

---

## คุณสมบัติ

| หมวดหมู่ | ความสามารถ |
|----------|-------------|
| **การติดตามรอบเดือน** | มุมมองปฏิทิน, เอ็นจินพยากรณ์รอบเดือน (การกรอง IQR + การลดแบบเอกซ์โพเนนเชียล), การบันทึกอาการ, การแจ้งเตือน |
| **การนับก้าว** | บริการ foreground นับก้าวฮาร์ดแวร์ + accelerometer สำรอง, การซิงค์ WorkManager, ข้อมูลเชิงลึกของก้าวตามช่วงรอบเดือน |
| **ฟิตเนส** | การออกกำลังกาย 17 ประเภท, เครื่องคำนวณแคลอรี่ MET, คำแนะนำการออกกำลังกายตามช่วงรอบเดือน |
| **การนอน** | การบันทึกการนอนด้วยตนเอง, การให้คะแนนคุณภาพ, กราฟแนวโน้ม Canvas |
| **โภชนาการ** | การบันทึกมื้ออาหาร (เช้า/กลางวัน/เย็น/ว่าง), การติดตามสารอาหาร, การดื่มน้ำ |
| **การวิเคราะห์** | เอ็นจินวิเคราะห์หลายมิติ, คำแนะนำด้านสุขภาพ, การส่งออกรายงานทางการแพทย์, การเพิ่มประสิทธิภาพการพยากรณ์ TFLite |
| **ความปลอดภัย** | PIN (Argon2id) + ล็อคไบโอเมตริก, การสำรอง/กู้คืน ZIP ที่เข้ารหัส, การป้องกันการจับภาพหน้าจอบนหน้าที่ละเอียดอ่อน |

---

## สแต็กเทคโนโลยี

| ชั้น | เทคโนโลยี | เวอร์ชัน |
|------|-----------|----------|
| ภาษา | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| สถาปัตยกรรม | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| การนำทาง | Navigation Compose | 2.7.6 |
| ฐานข้อมูล | Room + SQLCipher | 2.6.1 / 4.5.4 |
| พื้นหลัง | WorkManager | 2.9.0 |
| การเข้ารหัส | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| กราฟ | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| การวิเคราะห์สแตติก | Detekt | 1.23.6 |

---

## สถาปัตยกรรม

```
app/                    # เชลล์แอป: NavHost เดียว, ชุดรูปแบบ, MainActivity
├── navigation/MainNavigation.kt        # แถบนำทางด้านล่างแบบมีเงื่อนไข + เส้นทางด้านข้าง
core/
├── common/             # คอมโพเนนต์ UI ที่ใช้ร่วมกัน, ยูทิลิตี้, ทรัพยากรสตริงทั้งหมด (21 ภาษา)
├── database/           # เอนทิตี Room, DAO, รีโพสิทอรี, การโยกย้าย
└── security/           # Keystore, SQLCipher, Argon2id, การยืนยันตัวตนไบโอเมตริก, ล็อคแอป
feature/
├── onboarding/         # การเลือกเพศเมื่อใช้งานครั้งแรก
├── dashboard/          # ภาพรวมวันนี้, ข้อมูลเชิงลึกด้านสุขภาพ
├── period/             # การติดตามรอบเดือน, การพยากรณ์, การแจ้งเตือน, การสำรอง, การตั้งค่า
├── steps/              # บริการนับก้าว + การซิงค์ WorkManager
├── fitness/            # บันทึกการออกกำลังกาย + คำแนะนำตามช่วงรอบเดือน
├── sleep/              # การบันทึกการนอน + กราฟแนวโน้ม
├── nutrition/          # การติดตามอาหาร + น้ำ
└── analysis/           # การวิเคราะห์หลายมิติ + การอนุมาน ML TFLite
```

---

## ความปลอดภัยและความเป็นส่วนตัว

- **ข้อมูลขณะไม่ได้ใช้งาน**: SQLCipher AES-256 เข้ารหัสฐานข้อมูล `tianshang_health.db` ทั้งหมด รหัสผ่านฐานข้อมูลเป็นสตริงสุ่ม 32 ตัวอักษรที่เข้ารหัสโดย Android Keystore และไม่เคยจัดเก็บในรูปแบบข้อความธรรมดา
- **การยืนยันตัวตน**: PIN ถูกแฮชด้วย Argon2id BiometricPrompt (`BIOMETRIC_STRONG`) พร้อมใช้งานเป็นทางเลือกสำรอง
- **เครือข่าย**: `AndroidManifest.xml` **ไม่มีสิทธิ์ `INTERNET`** ไม่มี Firebase ไม่มีการวิเคราะห์ ไม่มีการรายงานข้อขัดข้อง
- **ล็อคแอป**: เมื่อแอปทำงานพื้นหลัง จะมีการหน่วงเวลาที่กำหนดค่าได้ (ทันที / 30วินาที / 1นาที / 5นาที) หน้าจอล็อคครอบคลุมทั้งแอปด้วย `zIndex(10f)`
- **การสำรอง**: การส่งออก/กู้คืน ZIP ที่เข้ารหัสด้วยรหัสผ่านที่ผู้ใช้กำหนด รองรับการส่งออก CSV ด้วย
- **การป้องกัน**: `FLAG_SECURE` บนหน้าที่ละเอียดอ่อน; การตรวจสอบ `Debug.isDebuggerConnected()` เมื่อเริ่มแอป

---

## เริ่มต้นใช้งาน

### ข้อกำหนดเบื้องต้น

- JDK 17
- Android Studio (แนะนำ Ladybug หรือใหม่กว่า)
- Android SDK ที่ติดตั้ง API 35

### สร้าง APK Debug

```bash
./gradlew :app:assembleDebug
```

APK จะอยู่ที่:
```
app/build/outputs/apk/debug/app-debug.apk
```

### ติดตั้งบนอุปกรณ์

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### สร้าง APK Release

การสร้าง Release ต้องมีการกำหนดค่าลายเซ็นใน `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

จากนั้นรัน:

```bash
./gradlew :app:assembleRelease
```

APK ที่เซ็นชื่อแล้วจะมีชื่อ:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### การตรวจสอบความปลอดภัยก่อนวางจำหน่าย

```bash
python check_release_security.py
```

---

## คุณภาพและการทดสอบ

- **Detekt**: การวิเคราะห์สแตติกแบบไม่ยอมรับข้อผิดพลาด (`maxIssues: 0`)
  ```bash
  ./gradlew detektAll
  ```
- **การทดสอบหน่วย**:
  ```bash
  ./gradlew test
  ```
- **การทดสอบแบบเครื่องมือวัด**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **การตรวจสอบสตริง**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## การรองรับหลายภาษา

ทรัพยากรสตริงทั้งหมดใน 21 ภาษาถูกรวมศูนย์ใน `core/common/src/main/res/values[-lang]/strings.xml`:

English/อังกฤษ, 简体中文/จีน (ตัวย่อ), 日本語/ญี่ปุ่น, 한국어/เกาหลี, Français/ฝรั่งเศส, Español/สเปน, Deutsch/เยอรมัน, Русский/รัสเซีย, Italiano/อิตาลี, Türkçe/ตุรกี, हिन्दी/ฮินดี, ภาษาไทย/ไทย, Tiếng Việt/เวียดนาม, Bahasa Indonesia/อินโดนีเซีย, Bahasa Melayu/มาเลย์, Polski/โปแลนด์, Português/โปรตุเกส, Nederlands/ดัตช์, Svenska/สวีเดน, Українська/ยูเครน, العربية/อาหรับ

---

## สัญญาอนุญาต

โครงการนี้อยู่ภายใต้สัญญาอนุญาต MIT ดูรายละเอียดได้ในไฟล์โครงการ

---

<p align="center">
  <b>อำนาจอธิปไตยของข้อมูลเป็นของผู้ใช้ นักพัฒนาไม่สามารถเข้าถึงข้อมูลของคุณได้ในทางเทคนิค</b>
</p>
