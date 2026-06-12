<p align="right">
  <a href="../README.md">English/अंग्रेज़ी</a> |
  <a href="./README.zh-CN.md">简体中文/चीनी (सरलीकृत)</a> |
  <a href="./README.ja.md">日本語/जापानी</a> |
  <a href="./README.ko.md">한국어/कोरियाई</a> |
  <a href="./README.fr.md">Français/फ़्रेंच</a> |
  <a href="./README.es.md">Español/स्पेनिश</a> |
  <a href="./README.de.md">Deutsch/जर्मन</a> |
  <a href="./README.ru.md">Русский/रूसी</a> |
  <a href="./README.it.md">Italiano/इतालवी</a> |
  <a href="./README.tr.md">Türkçe/तुर्की</a> |
  <a href="./README.th.md">ภาษาไทย/थाई</a> |
  <a href="./README.vi.md">Tiếng Việt/वियतनामी</a> |
  <a href="./README.id.md">Bahasa Indonesia/इंडोनेशियाई</a> |
  <a href="./README.ms.md">Bahasa Melayu/मलय</a> |
  <a href="./README.pl.md">Polski/पोलिश</a> |
  <a href="./README.pt.md">Português/पुर्तगाली</a> |
  <a href="./README.nl.md">Nederlands/डच</a> |
  <a href="./README.sv.md">Svenska/स्वीडिश</a> |
  <a href="./README.uk.md">Українська/यूक्रेनियाई</a> |
  <a href="./README.ar.md">العربية/अरबी</a>
</p>

# TianshangHealth

> **गोपनीयता-प्रथम, पूरी तरह से ऑफ़लाइन Android स्वास्थ्य साथी।**
> मासिक धर्म ट्रैकिंग, फिटनेस, नींद, पोषण और पैदल चलने की गणना को एक एन्क्रिप्टेड स्थानीय डेटाबेस में जोड़ता है — डिवाइस पर AI-संचालित बहु-आयामी अंतर्दृष्टि के साथ।

---

## मुख्य विशेषताएं

- **शून्य नेटवर्क अनुमतियाँ** — सभी डेटा आपके डिवाइस पर रहता है। कोई क्लाउड, कोई एनालिटिक्स, कोई Firebase नहीं।
- **सैन्य-ग्रेड एन्क्रिप्शन** — SQLCipher AES-256 डेटाबेस Android Keystore हार्डवेयर-समर्थित कुंजियों द्वारा सुरक्षित।
- **मॉड्यूलर आर्किटेक्चर** — MVVM + Repository + Hilt + Compose का पालन करते हुए 12 Gradle मॉड्यूल।
- **स्थानीय AI अनुमान** — TensorFlow Lite मॉडल डिवाइस छोड़े बिना मासिक धर्म पूर्वानुमान और मूड विश्लेषण को बढ़ाते हैं।
- **21 भाषाएं** — अरबी के लिए RTL समर्थन सहित; पहले दिन से स्थानीयकृत।
- **लिंग-अनुकूल UI** — उपयोगकर्ता द्वारा चुने गए लिंग के आधार पर निचला नेविगेशन और सुविधाएं समायोजित होती हैं।

---

## सुविधाएं

| डोमेन | क्षमताएं |
|--------|----------|
| **चक्र ट्रैकिंग** | कैलेंडर दृश्य, चक्र पूर्वानुमान इंजन (IQR फ़िल्टरिंग + घातीय क्षय), लक्षण लॉगिंग, अनुस्मारक |
| **कदम** | हार्डवेयर स्टेप काउंटर अग्रभूमि सेवा + एक्सेलेरोमीटर फ़ॉलबैक, WorkManager सिंक, चक्र चरण कदम अंतर्दृष्टि |
| **फिटनेस** | 17 व्यायाम प्रकार, MET-आधारित कैलोरी कैलकुलेटर, चक्र-जागरूक कसरत अनुशंसाएं |
| **नींद** | मैनुअल नींद लॉगिंग, गुणवत्ता स्कोरिंग, Canvas ट्रेंड चार्ट |
| **पोषण** | भोजन लॉगिंग (नाश्ता/दोपहर/रात/स्नैक), मैक्रो ट्रैकिंग, पानी का सेवन |
| **विश्लेषण** | बहु-आयामी विश्लेषण इंजन, स्वास्थ्य सुझाव, चिकित्सा रिपोर्ट निर्यात, TFLite पूर्वानुमान संवर्धन |
| **सुरक्षा** | PIN (Argon2id) + बायोमेट्रिक लॉक, एन्क्रिप्टेड ZIP बैकअप/रीस्टोर, संवेदनशील स्क्रीन पर स्क्रीनशॉट सुरक्षा |

---

## तकनीकी स्टैक

| परत | तकनीक | संस्करण |
|------|--------|---------|
| भाषा | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| आर्किटेक्चर | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| नेविगेशन | Navigation Compose | 2.7.6 |
| डेटाबेस | Room + SQLCipher | 2.6.1 / 4.5.4 |
| पृष्ठभूमि | WorkManager | 2.9.0 |
| क्रिप्टो | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| चार्ट | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| स्थैतिक विश्लेषण | Detekt | 1.23.6 |

---

## आर्किटेक्चर

```
app/                    # ऐप शेल: एकल NavHost, थीम, MainActivity
├── navigation/MainNavigation.kt        # सशर्त निचला नेविगेशन + साइड रूट
core/
├── common/             # साझा UI घटक, उपयोगिताएं, सभी स्ट्रिंग संसाधन (21 भाषाएं)
├── database/           # Room संस्थाएं, DAO, रिपॉजिटरी, माइग्रेशन
└── security/           # Keystore, SQLCipher, Argon2id, बायोमेट्रिक प्रमाणीकरण, ऐप लॉक
feature/
├── onboarding/         # पहले उपयोग पर लिंग चयन
├── dashboard/          # आज का अवलोकन, स्वास्थ्य अंतर्दृष्टि
├── period/             # मासिक धर्म ट्रैकिंग, पूर्वानुमान, अनुस्मारक, बैकअप, सेटिंग्स
├── steps/              # स्टेप काउंटर सेवा + WorkManager सिंक
├── fitness/            # कसरत रिकॉर्ड + चक्र-जागरूक अनुशंसाएं
├── sleep/              # नींद लॉगिंग + ट्रेंड चार्ट
├── nutrition/          # भोजन + पानी ट्रैकिंग
└── analysis/           # बहु-आयामी विश्लेषण + TFLite ML अनुमान
```

---

## सुरक्षा और गोपनीयता

- **डेटा आराम पर**: SQLCipher AES-256 पूरे `tianshang_health.db` डेटाबेस को एन्क्रिप्ट करता है। डेटाबेस पासवर्ड Android Keystore द्वारा एन्क्रिप्टेड 32-वर्णों की यादृच्छिक स्ट्रिंग है और कभी भी सादे टेक्स्ट में संग्रहीत नहीं होता।
- **प्रमाणीकरण**: PIN Argon2id के साथ हैश किए जाते हैं। BiometricPrompt (`BIOMETRIC_STRONG`) वैकल्पिक रूप से उपलब्ध है।
- **नेटवर्क**: `AndroidManifest.xml` में **कोई `INTERNET` अनुमति नहीं**। कोई Firebase, कोई एनालिटिक्स, कोई क्रैश रिपोर्टिंग नहीं।
- **ऐप लॉक**: बैकग्राउंड में जाने पर कॉन्फ़िगर करने योग्य देरी (तुरंत / 30s / 1मिनट / 5मिनट) के बाद लॉक होता है। लॉक स्क्रीन पूरे ऐप को `zIndex(10f)` से कवर करती है।
- **बैकअप**: उपयोगकर्ता-प्रदत्त पासवर्ड के साथ एन्क्रिप्टेड ZIP निर्यात/पुनर्स्थापना। CSV निर्यात भी समर्थित है।
- **सुरक्षा**: संवेदनशील स्क्रीन पर `FLAG_SECURE`; ऐप स्टार्टअप पर `Debug.isDebuggerConnected()` जांच।

---

## आरंभ करना

### पूर्वापेक्षाएँ

- JDK 17
- Android Studio (Ladybug या नया अनुशंसित)
- API 35 स्थापित Android SDK

### Debug APK बनाएं

```bash
./gradlew :app:assembleDebug
```

APK इस स्थान पर होगा:
```
app/build/outputs/apk/debug/app-debug.apk
```

### डिवाइस पर इंस्टॉल करें

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release APK बनाएं

Release बिल्ड के लिए `local.properties` में हस्ताक्षर कॉन्फ़िगरेशन आवश्यक है:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

फिर चलाएं:

```bash
./gradlew :app:assembleRelease
```

हस्ताक्षरित APK का नाम होगा:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### रिलीज़-पूर्व सुरक्षा जांच

```bash
python check_release_security.py
```

---

## गुणवत्ता और परीक्षण

- **Detekt**: शून्य-सहनशीलता स्थैतिक विश्लेषण (`maxIssues: 0`)।
  ```bash
  ./gradlew detektAll
  ```
- **यूनिट परीक्षण**:
  ```bash
  ./gradlew test
  ```
- **इंस्ट्रुमेंटेशन परीक्षण**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **स्ट्रिंग सत्यापन**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## बहुभाषा समर्थन

सभी 21 भाषाओं के स्ट्रिंग संसाधन `core/common/src/main/res/values[-lang]/strings.xml` में केंद्रीकृत हैं:

English/अंग्रेज़ी, 简体中文/चीनी (सरलीकृत), 日本語/जापानी, 한국어/कोरियाई, Français/फ़्रेंच, Español/स्पेनिश, Deutsch/जर्मन, Русский/रूसी, Italiano/इतालवी, Türkçe/तुर्की, हिन्दी/हिन्दी, ภาษาไทย/थाई, Tiếng Việt/वियतनामी, Bahasa Indonesia/इंडोनेशियाई, Bahasa Melayu/मलय, Polski/पोलिश, Português/पुर्तगाली, Nederlands/डच, Svenska/स्वीडिश, Українська/यूक्रेनियाई, العربية/अरबी

---

## लाइसेंस

यह परियोजना MIT लाइसेंस के तहत लाइसेंस प्राप्त है। विवरण के लिए परियोजना फ़ाइलें देखें।

---

<p align="center">
  <b>डेटा संप्रभुता उपयोगकर्ता की है। डेवलपर तकनीकी रूप से आपके डेटा तक नहीं पहुंच सकता।</b>
</p>
