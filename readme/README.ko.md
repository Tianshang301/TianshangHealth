<p align="right">
  <a href="../README.md">English/영어</a> |
  <a href="./README.zh-CN.md">简体中文/중국어(간체)</a> |
  <a href="./README.ja.md">日本語/일본어</a> |
  <a href="./README.fr.md">Français/프랑스어</a> |
  <a href="./README.es.md">Español/스페인어</a> |
  <a href="./README.de.md">Deutsch/독일어</a> |
  <a href="./README.ru.md">Русский/러시아어</a> |
  <a href="./README.it.md">Italiano/이탈리아어</a> |
  <a href="./README.tr.md">Türkçe/터키어</a> |
  <a href="./README.hi.md">हिन्दी/힌디어</a> |
  <a href="./README.th.md">ภาษาไทย/태국어</a> |
  <a href="./README.vi.md">Tiếng Việt/베트남어</a> |
  <a href="./README.id.md">Bahasa Indonesia/인도네시아어</a> |
  <a href="./README.ms.md">Bahasa Melayu/말레이어</a> |
  <a href="./README.pl.md">Polski/폴란드어</a> |
  <a href="./README.pt.md">Português/포르투갈어</a> |
  <a href="./README.nl.md">Nederlands/네덜란드어</a> |
  <a href="./README.sv.md">Svenska/스웨덴어</a> |
  <a href="./README.uk.md">Українська/우크라이나어</a> |
  <a href="./README.ar.md">العربية/아랍어</a>
</p>

# 천상 건강（TianshangHealth）

> **프라이버시 중심, 완전 오프라인 Android 통합 건강 관리 앱입니다.**
> 생리 주기 추적, 운송, 수면, 영양, 걸음 수 통계 등 건강 차원을 하나의 암호화된 로컬 데이터베이스에 통합하고, 온디바이스 AI로 다차원 건강 인사이트를 제공합니다.

---

## 핵심 하이라이트

- **제로 네트워크 권한** — 모든 데이터는 기기에 로컬로 저장됩니다. 클라우드, 분석, Firebase 없음.
- **군사급 암호화** — SQLCipher AES-256 데이터베이스를 Android Keystore 하드웨어 지원 키로 보호합니다.
- **모듈형 아키텍처** — MVVM + Repository + Hilt + Compose를 따르는 12개 Gradle 모듈.
- **로컬 AI 추론** — TensorFlow Lite 모델이 생리 예측과 기분 분석을 기기 내에서 강화합니다.
- **21개 언어** — 아랍어 RTL 지원을 포함해 첫날부터 완전히 현지화되었습니다.
- **성별 적응형 UI** — 사용자의 성별 선택에 따라 하단 탐색 및 기능을 동적으로 조정합니다.

---

## 기능

| 영역 | 주요 기능 |
|--------|-------------|
| **생리 주기 추적** | 캘린더 보기, 주기 예측 엔진(IQR 필터링 + 지수 감쇠), 증상 기록, 알림 |
| **걸음 수 통계** | 하드웨어 보폭계 포그라운드 서비스 + 가속도계 폴택, WorkManager 동기화, 주기-걸음수 연관 인사이트 |
| **울동 피트니스** | 17가지 울동 유형, MET 기반 칼로리 계산, 주기 단계별 울동 권장 |
| **수면 모니터링** | 수동 수면 기록, 수면 질 점수, Canvas 트렌드 그래프 |
| **영양 관리** | 식사 기록(아침/점심/저녁/간식), 영양소 추적, 수분 섭취 |
| **분석 인사이트** | 다차원 통계 엔진, 건강 권장, 의료 보고서 낸볼, TFLite 예측 강화 |
| **보안 및 프라이버시** | PIN(Argon2id) + 생체 인식 잠금, 암호화 ZIP 백업/복원, 민감 페이지 스크린샷 방지 |

---

## 기술 스택

| 계층 | 기술 | 버전 |
|-------|-----------|---------|
| 언어 | Kotlin | 1.9.24 |
| 빌드 | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| 아키텍처 | MVVM + Repository + StateFlow | — |
| 의존성 주입 | Hilt (KSP) | 2.51.1 |
| 낼비게이션 | Navigation Compose | 2.7.6 |
| 데이터베이스 | Room + SQLCipher | 2.6.1 / 4.5.4 |
| 백그라운드 | WorkManager | 2.9.0 |
| 암호화 | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| 차트 | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| 정적 분석 | Detekt | 1.23.6 |

---

## 프로젝트 아키텍처

```
app/                    # 앱 셸: 단일 NavHost, 테마, MainActivity
├── navigation/MainNavigation.kt        # 동적 하단 탐색 + 사이드 라우트
core/                   # 핵심 인프라
├── common/             # 공유 컴포넌트, 유틸리티, 모든 21개 언어 문자열 리소스
├── database/           # Room 엔터티, DAO, Repository, 데이터베이스 마이그레이션
└── security/           # Keystore, SQLCipher, Argon2id, 생체 인식, 앱 잠금
feature/                # 기능 모듈
├── onboarding/         # 첫 실행: 성별 선택
├── dashboard/          # 홈 대시보드: 오늘의 요약과 건강 인사이트
├── period/             # 생리 주기 추적: 예측 엔진, 알림, 백업, 설정
├── steps/              # 하드웨어 보폭계 포그라운드 서비스 + WorkManager 폴택 동기화
├── fitness/            # 울동 기록 + 주기 단계별 권장
├── sleep/              # 수면 기록 + Canvas 트렌드 그래프
├── nutrition/          # 식사 기록 + 수분 섭취
└── analysis/           # 다차원 분석 + TFLite 로컬 AI 추론
```

---

## 보안 및 프라이버시

- **저장 데이터 암호화**: 전체 `tianshang_health.db`를 SQLCipher AES-256으로 암호화합니다. 데이터베이스 비밀번호는 32자 무작위 문자열이며 Android Keystore로 암호화되어 저장되며 Keystore를 벗어나지 않습니다.
- **인증**: PIN은 Argon2id로 해시되며, BiometricPrompt(강력 생체 인식)를 폴택으로 사용할 수 있습니다.
- **네트워크 격리**: `AndroidManifest.xml`에 **`INTERNET` 권한이 없습니다**. Firebase, 분석, 충돌 보고가 없습니다.
- **앱 잠금**: 앱이 백그라운드로 들어가면 설정된 지연(즉시 / 30초 / 1분 / 5분) 후 자동 잠금됩니다. `AppLockScreen`이 `zIndex(10f)`로 전체 화면을 덮습니다.
- **백업 및 낸볼**: 사용자 지정 비밀번호로 암호화된 ZIP 백업/복원을 지원하며 CSV 낸볼도 지원합니다.
- **강화 조치**: 민감한 페이지에서 `FLAG_SECURE`를 사용하여 스크린샷을 방지하고, 시작 시 `Debug.isDebuggerConnected()`로 디버깅을 감지합니다.

---

## 빠른 시작

### 필수 조건

- JDK 17
- Android Studio(Ladybug 또는 최신 버전 권장)
- API 35가 설치된 Android SDK

### Debug APK 빌드

```bash
./gradlew :app:assembleDebug
```

APK 출력 경로:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 기기에 설치

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release APK 빌드

Release 빌드에는 `local.properties`의 서명 구성이 필요합니다:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

그런 다음 실행:

```bash
./gradlew :app:assembleRelease
```

생성되는 서명된 APK 파일 이름:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### 릴리스 전 보안 검사

```bash
python check_release_security.py
```

---

## 품질 및 테스트

- **Detekt**: 제로 톨러런스 정적 분석(`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **단위 테스트**:
  ```bash
  ./gradlew test
  ```
- **Instrumentation 테스트**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **문자열 리소스 검증**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## 다국어 지원

21개 언어의 모든 문자열은 `core/common/src/main/res/values[-lang]/strings.xml`에 집중하여 관리됩니다:

English/영어, 简体中文/중국어(간체), 日本語/일본어, 한국어/한국어, Français/프랑스어, Español/스페인어, Deutsch/독일어, Русский/러시아어, Italiano/이탈리아어, Türkçe/터키어, हिन्दी/힌디어, ภาษาไทย/태국어, Tiếng Việt/베트남어, Bahasa Indonesia/인도네시아어, Bahasa Melayu/말레이어, Polski/폴란드어, Português/포르투갈어, Nederlands/네덜란드어, Svenska/스웨덴어, Українська/우크라이나어, العربية/아랍어

---

## 오픈소스 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 자세한 내용은 프로젝트 파일을 참조하세요.

---

<p align="center">
  <b>데이터 주권은 영원히 사용자에게 속합니다. 개발자는 기술적으로 사용자의 데이터에 접근할 수 없습니다.</b>
</p>
