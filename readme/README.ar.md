<p align="right">
  <a href="../README.md">English/الإنجليزية</a> |
  <a href="./README.zh-CN.md">简体中文/الصينية (المبسطة)</a> |
  <a href="./README.ja.md">日本語/اليابانية</a> |
  <a href="./README.ko.md">한국어/الكورية</a> |
  <a href="./README.fr.md">Français/الفرنسية</a> |
  <a href="./README.es.md">Español/الإسبانية</a> |
  <a href="./README.de.md">Deutsch/الألمانية</a> |
  <a href="./README.ru.md">Русский/الروسية</a> |
  <a href="./README.it.md">Italiano/الإيطالية</a> |
  <a href="./README.tr.md">Türkçe/التركية</a> |
  <a href="./README.hi.md">हिन्दी/الهندية</a> |
  <a href="./README.th.md">ภาษาไทย/التايلاندية</a> |
  <a href="./README.vi.md">Tiếng Việt/الفيتنامية</a> |
  <a href="./README.id.md">Bahasa Indonesia/الإندونيسية</a> |
  <a href="./README.ms.md">Bahasa Melayu/الماليزية</a> |
  <a href="./README.pl.md">Polski/البولندية</a> |
  <a href="./README.pt.md">Português/البرتغالية</a> |
  <a href="./README.nl.md">Nederlands/الهولندية</a> |
  <a href="./README.sv.md">Svenska/السويدية</a> |
  <a href="./README.uk.md">Українська/الأوكرانية</a> |
  <a href="./README.ar.md">العربية/العربية</a>
</p>

# TianshangHealth

> **رفيق الصحة لنظام Android الذي يعمل دون اتصال بالإنترنت تمامًا ويحترم الخصوصية.**
> يجمع بين تتبع الدورة الشهرية واللياقة البدنية والنوم والتغذية وعداد الخطوات في قاعدة بيانات محلية مشفرة واحدة — مع تحليلات متعددة الأبعاد مدعومة بالذكاء الاصطناعي على الجهاز.

---

## أبرز الميزات

- **لا أذونات شبكة** — جميع البيانات تبقى على جهازك. لا سحابة، لا تحليلات، لا Firebase.
- **تشفير عسكري** — قاعدة بيانات SQLCipher AES-256 محمية بمفاتيح أجهزة Android Keystore.
- **هندسة معيارية** — 12 وحدة Gradle تتبع MVVM + Repository + Hilt + Compose.
- **استدلال ذكاء اصطناعي محلي** — نماذج TensorFlow Lite تعزز التنبؤ بالدورة وتحليل المزاج دون مغادرة الجهاز.
- **21 لغة** — بما في ذلك دعم RTL للعربية؛ مترجمة منذ اليوم الأول.
- **واجهة متكيفة مع الجنس** — شريط التنقل السفلي والوظائف يتكيفان حسب الجنس المختار.

---

## الميزات

| المجال | الإمكانيات |
|--------|-----------|
| **تتبع الدورة** | عرض التقويم، محرك التنبؤ بالدورة (تصفية IQR + الاضمحلال الأسي)، تسجيل الأعراض، التذكيرات |
| **الخطوات** | خدمة مقدمة لعداد الخطوات + مقياس التسارع احتياطيًا، مزامنة WorkManager، تحليلات الخطوات حسب مرحلة الدورة |
| **اللياقة** | 17 نوع تمرين، حاسبة السعرات الحرارية MET، توصيات تمارين حسب مرحلة الدورة |
| **النوم** | تسجيل النوم يدويًا، تقييم الجودة، رسم بياني للاتجاهات Canvas |
| **التغذية** | تسجيل الوجبات (فطور/غداء/عشاء/وجبة خفيفة)، تتبع المغذيات الكبيرة، تناول الماء |
| **التحليل** | محرك تحليلات متعدد الأبعاد، اقتراحات صحية، تصدير التقارير الطبية، تعزيز التنبؤ TFLite |
| **الأمان** | PIN (Argon2id) + قفل بيومتري، نسخ احتياطي/استعادة ZIP مشفر، حماية لقطة الشاشة على الشاشات الحساسة |

---

## الرصة التقنية

| الطبقة | التقنية | الإصدار |
|--------|---------|---------|
| اللغة | Kotlin | 1.9.24 |
| البناء | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| واجهة المستخدم | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| الهندسة | MVVM + Repository + StateFlow | — |
| حقن التبعيات | Hilt (KSP) | 2.51.1 |
| التنقل | Navigation Compose | 2.7.6 |
| قاعدة البيانات | Room + SQLCipher | 2.6.1 / 4.5.4 |
| الخلفية | WorkManager | 2.9.0 |
| التشفير | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| الرسوم البيانية | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| التعلم الآلي | TensorFlow Lite | 2.14.0 |
| التحليل الثابت | Detekt | 1.23.6 |

---

## الهندسة المعمارية

```
app/                    # غلاف التطبيق: NavHost واحد، سمة، MainActivity
├── navigation/MainNavigation.kt        # تنقل سفلي شرطي + مسارات جانبية
core/
├── common/             # مكونات واجهة مستخدم مشتركة، أدوات، جميع موارد النصوص (21 لغة)
├── database/           # كيانات Room، DAO، مستودعات، ترحيل
└── security/           # Keystore، SQLCipher، Argon2id، مصادقة بيومترية، قفل التطبيق
feature/
├── onboarding/         # اختيار الجنس عند التشغيل الأول
├── dashboard/          # نظرة عامة اليوم، رؤى صحية
├── period/             # تتبع الدورة الشهرية، التنبؤ، التذكيرات، النسخ الاحتياطي، الإعدادات
├── steps/              # خدمة عداد الخطوات + مزامنة WorkManager
├── fitness/            # سجلات التمارين + توصيات حسب مرحلة الدورة
├── sleep/              # تسجيل النوم + رسم بياني للاتجاهات
├── nutrition/          # تتبع الوجبات + الماء
└── analysis/           # تحليلات متعددة الأبعاد + استدلال ML TFLite
```

---

## الأمان والخصوصية

- **البيانات في حالة السكون**: SQLCipher AES-256 يشفر قاعدة بيانات `tianshang_health.db` بأكملها. كلمة مرور قاعدة البيانات هي سلسلة عشوائية من 32 حرفًا مشفرة بواسطة Android Keystore ولا تُخزن أبدًا في نص عادي.
- **المصادقة**: يتم تجزئة PIN باستخدام Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) متاح كبديل احتياطي.
- **الشبكة**: `AndroidManifest.xml` **لا يحتوي على إذن `INTERNET`**. لا Firebase، لا تحليلات، لا تقارير أعطال.
- **قفل التطبيق**: عند الانتقال إلى الخلفية، يتم تفعيل تأخير قابل للتكوين (فوري / 30ث / 1د / 5د). شاشة القفل تغطي التطبيق بأكمله بـ `zIndex(10f)`.
- **النسخ الاحتياطي**: تصدير/استعادة ZIP مشفر بكلمة مرور يقدمها المستخدم. تصدير CSV مدعوم أيضًا.
- **التحصين**: `FLAG_SECURE` على الشاشات الحساسة؛ التحقق من `Debug.isDebuggerConnected()` عند بدء التشغيل.

---

## البدء

### المتطلبات الأساسية

- JDK 17
- Android Studio (يوصى بـ Ladybug أو أحدث)
- Android SDK مع تثبيت API 35

### بناء APK Debug

```bash
./gradlew :app:assembleDebug
```

سيكون APK موجودًا في:
```
app/build/outputs/apk/debug/app-debug.apk
```

### التثبيت على جهاز

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### بناء APK Release

تتطلب إصدارات Release تكوين التوقيع في `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

ثم قم بتشغيل:

```bash
./gradlew :app:assembleRelease
```

سيتم تسمية APK الموقّع:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### فحص الأمان قبل الإصدار

```bash
python check_release_security.py
```

---

## الجودة والاختبار

- **Detekt**: تحليل ثابت بدون تسامح (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **اختبارات الوحدة**:
  ```bash
  ./gradlew test
  ```
- **اختبارات الأجهزة**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **التحقق من النصوص**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## دعم اللغات المتعددة

جميع موارد النصوص بـ 21 لغة مركزة في `core/common/src/main/res/values[-lang]/strings.xml`:

English/الإنجليزية, 简体中文/الصينية (المبسطة), 日本語/اليابانية, 한국어/الكورية, Français/الفرنسية, Español/الإسبانية, Deutsch/الألمانية, Русский/الروسية, Italiano/الإيطالية, Türkçe/التركية, हिन्दी/الهندية, ภาษาไทย/التايلاندية, Tiếng Việt/الفيتنامية, Bahasa Indonesia/الإندونيسية, Bahasa Melayu/الماليزية, Polski/البولندية, Português/البرتغالية, Nederlands/الهولندية, Svenska/السويدية, Українська/الأوكرانية, العربية/العربية

---

## الترخيص

هذا المشروع مرخص بموجب رخصة MIT. راجع ملفات المشروع للتفاصيل.

---

<p align="center">
  <b>سيادة البيانات تعود للمستخدم. لا يمكن للمطور تقنيًا الوصول إلى بياناتك.</b>
</p>
