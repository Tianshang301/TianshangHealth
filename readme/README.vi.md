<p align="right">
  <a href="../README.md">English/Tiếng Anh</a> |
  <a href="./README.zh-CN.md">简体中文/Tiếng Trung (Giản thể)</a> |
  <a href="./README.ja.md">日本語/Tiếng Nhật</a> |
  <a href="./README.ko.md">한국어/Tiếng Hàn</a> |
  <a href="./README.fr.md">Français/Tiếng Pháp</a> |
  <a href="./README.es.md">Español/Tiếng Tây Ban Nha</a> |
  <a href="./README.de.md">Deutsch/Tiếng Đức</a> |
  <a href="./README.ru.md">Русский/Tiếng Nga</a> |
  <a href="./README.it.md">Italiano/Tiếng Ý</a> |
  <a href="./README.tr.md">Türkçe/Tiếng Thổ Nhĩ Kỳ</a> |
  <a href="./README.hi.md">हिन्दी/Tiếng Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Tiếng Thái</a> |
  <a href="./README.id.md">Bahasa Indonesia/Tiếng Indonesia</a> |
  <a href="./README.ms.md">Bahasa Melayu/Tiếng Mã Lai</a> |
  <a href="./README.pl.md">Polski/Tiếng Ba Lan</a> |
  <a href="./README.pt.md">Português/Tiếng Bồ Đào Nha</a> |
  <a href="./README.nl.md">Nederlands/Tiếng Hà Lan</a> |
  <a href="./README.sv.md">Svenska/Tiếng Thụy Điển</a> |
  <a href="./README.uk.md">Українська/Tiếng Ukraina</a> |
  <a href="./README.ar.md">العربية/Tiếng Ả Rập</a>
</p>

# TianshangHealth(天殇·悦康)

> **Người bạn đồng hành sức khỏe Android hoàn toàn ngoại tuyến, ưu tiên quyền riêng tư.**
> Kết hợp theo dõi chu kỳ kinh nguyệt, thể dục, giấc ngủ, dinh dưỡng và đếm bước chân vào một cơ sở dữ liệu cục bộ được mã hóa duy nhất — với phân tích đa chiều bằng AI trên thiết bị.

---

## Điểm nổi bật

- **Không có quyền mạng** — Tất cả dữ liệu ở lại trên thiết bị của bạn. Không đám mây, không phân tích, không Firebase.
- **Mã hóa cấp quân sự** — Cơ sở dữ liệu SQLCipher AES-256 được bảo vệ bởi khóa phần cứng Android Keystore.
- **Kiến trúc mô-đun** — 12 mô-đun Gradle theo MVVM + Repository + Hilt + Compose.
- **Suy luận AI cục bộ** — Mô hình TensorFlow Lite cải thiện dự đoán chu kỳ và phân tích tâm trạng mà không cần rời khỏi thiết bị.
- **21 ngôn ngữ** — Bao gồm hỗ trợ RTL cho tiếng Ả Rập; được bản địa hóa ngay từ ngày đầu.
- **Giao diện thích ứng giới tính** — Thanh điều hướng dưới cùng và tính năng điều chỉnh theo giới tính người dùng chọn.

---

## Tính năng

| Lĩnh vực | Khả năng |
|----------|----------|
| **Theo dõi chu kỳ** | Xem lịch, công cụ dự đoán chu kỳ (lọc IQR + suy giảm hàm mũ), ghi nhận triệu chứng, nhắc nhở |
| **Bước chân** | Dịch vụ nền trước đếm bước phần cứng + gia tốc kế dự phòng, đồng bộ WorkManager, phân tích bước theo pha chu kỳ |
| **Thể dục** | 17 loại bài tập, máy tính calo MET, gợi ý tập luyện theo pha chu kỳ |
| **Giấc ngủ** | Ghi nhận giấc ngủ thủ công, đánh giá chất lượng, biểu đồ xu hướng Canvas |
| **Dinh dưỡng** | Ghi nhận bữa ăn (sáng/trưa/tối/ăn nhẹ), theo dõi dinh dưỡng đa lượng, lượng nước uống |
| **Phân tích** | Công cụ phân tích đa chiều, gợi ý sức khỏe, xuất báo cáo y tế, cải thiện dự đoán TFLite |
| **Bảo mật** | PIN (Argon2id) + khóa sinh trắc học, sao lưu/phục hồi ZIP mã hóa, bảo vệ ảnh chụp màn hình trên các màn hình nhạy cảm |

---

## Công nghệ sử dụng

| Tầng | Công nghệ | Phiên bản |
|------|-----------|-----------|
| Ngôn ngữ | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Kiến trúc | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Điều hướng | Navigation Compose | 2.7.6 |
| Cơ sở dữ liệu | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Nền | WorkManager | 2.9.0 |
| Mã hóa | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Biểu đồ | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Phân tích tĩnh | Detekt | 1.23.6 |

---

## Kiến trúc

```
app/                    # Vỏ ứng dụng: NavHost đơn, chủ đề, MainActivity
├── navigation/MainNavigation.kt        # Điều hướng dưới có điều kiện + tuyến phụ
core/
├── common/             # Thành phần UI dùng chung, tiện ích, tất cả tài nguyên chuỗi (21 ngôn ngữ)
├── database/           # Thực thể Room, DAO, kho lưu trữ, di chuyển
└── security/           # Keystore, SQLCipher, Argon2id, xác thực sinh trắc học, khóa ứng dụng
feature/
├── onboarding/         # Chọn giới tính khi khởi chạy lần đầu
├── dashboard/          # Tổng quan hôm nay, thông tin chi tiết sức khỏe
├── period/             # Theo dõi chu kỳ, dự đoán, nhắc nhở, sao lưu, cài đặt
├── steps/              # Dịch vụ đếm bước + đồng bộ WorkManager
├── fitness/            # Ghi nhận tập luyện + gợi ý theo pha chu kỳ
├── sleep/              # Ghi nhận giấc ngủ + biểu đồ xu hướng
├── nutrition/          # Theo dõi bữa ăn + nước uống
└── analysis/           # Phân tích đa chiều + suy luận ML TFLite
```

---

## Bảo mật và quyền riêng tư

- **Dữ liệu lưu trữ**: SQLCipher AES-256 mã hóa toàn bộ cơ sở dữ liệu `tianshang_health.db`. Mật khẩu cơ sở dữ liệu là chuỗi ngẫu nhiên 32 ký tự được mã hóa bởi Android Keystore và không bao giờ lưu ở dạng văn bản thuần.
- **Xác thực**: PIN được băm bằng Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) có sẵn như phương án dự phòng.
- **Mạng**: `AndroidManifest.xml` **không chứa quyền `INTERNET`**. Không Firebase, không phân tích, không báo cáo lỗi.
- **Khóa ứng dụng**: Khi ứng dụng chuyển sang nền, khóa sẽ kích hoạt sau độ trễ có thể cấu hình (ngay lập tức / 30s / 1phút / 5phút). Màn hình khóa phủ toàn bộ ứng dụng ở `zIndex(10f)`.
- **Sao lưu**: Xuất/phục hồi ZIP mã hóa với mật khẩu do người dùng cung cấp. Xuất CSV cũng được hỗ trợ.
- **Tăng cường**: `FLAG_SECURE` trên các màn hình nhạy cảm; kiểm tra `Debug.isDebuggerConnected()` khi khởi động.

---

## Bắt đầu

### Yêu cầu

- JDK 17
- Android Studio (khuyến nghị Ladybug hoặc mới hơn)
- Android SDK đã cài API 35

### Build APK Debug

```bash
./gradlew :app:assembleDebug
```

APK sẽ nằm tại:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Cài đặt lên thiết bị

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Build APK Release

Bản Release yêu cầu cấu hình chữ ký trong `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Sau đó chạy:

```bash
./gradlew :app:assembleRelease
```

APK đã ký sẽ có tên:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Kiểm tra bảo mật trước phát hành

```bash
python check_release_security.py
```

---

## Chất lượng và kiểm thử

- **Detekt**: Phân tích tĩnh không khoan nhượng (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Kiểm thử đơn vị**:
  ```bash
  ./gradlew test
  ```
- **Kiểm thử tích hợp**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Xác thực chuỗi**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Hỗ trợ đa ngôn ngữ

Tất cả tài nguyên chuỗi 21 ngôn ngữ được tập trung tại `core/common/src/main/res/values[-lang]/strings.xml`:

English/Tiếng Anh, 简体中文/Tiếng Trung (Giản thể), 日本語/Tiếng Nhật, 한국어/Tiếng Hàn, Français/Tiếng Pháp, Español/Tiếng Tây Ban Nha, Deutsch/Tiếng Đức, Русский/Tiếng Nga, Italiano/Tiếng Ý, Türkçe/Tiếng Thổ Nhĩ Kỳ, हिन्दी/Tiếng Hindi, ภาษาไทย/Tiếng Thái, Tiếng Việt/Tiếng Việt, Bahasa Indonesia/Tiếng Indonesia, Bahasa Melayu/Tiếng Mã Lai, Polski/Tiếng Ba Lan, Português/Tiếng Bồ Đào Nha, Nederlands/Tiếng Hà Lan, Svenska/Tiếng Thụy Điển, Українська/Tiếng Ukraina, العربية/Tiếng Ả Rập

---

## Giấy phép

Dự án này được cấp phép theo Giấy phép MIT. Xem tệp dự án để biết chi tiết.

---

<p align="center">
  <b>Quyền sở hữu dữ liệu thuộc về người dùng. Nhà phát triển về mặt kỹ thuật không thể truy cập dữ liệu của bạn.</b>
</p>
