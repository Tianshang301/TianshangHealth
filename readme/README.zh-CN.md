<p align="right">
  <a href="../README.md">English/英语</a> |
  <a href="./README.ja.md">日本語/日语</a> |
  <a href="./README.ko.md">한국어/韩语</a> |
  <a href="./README.fr.md">Français/法语</a> |
  <a href="./README.es.md">Español/西班牙语</a> |
  <a href="./README.de.md">Deutsch/德语</a> |
  <a href="./README.ru.md">Русский/俄语</a> |
  <a href="./README.it.md">Italiano/意大利语</a> |
  <a href="./README.tr.md">Türkçe/土耳其语</a> |
  <a href="./README.hi.md">हिन्दी/印地语</a> |
  <a href="./README.th.md">ภาษาไทย/泰语</a> |
  <a href="./README.vi.md">Tiếng Việt/越南语</a> |
  <a href="./README.id.md">Bahasa Indonesia/印尼语</a> |
  <a href="./README.ms.md">Bahasa Melayu/马来语</a> |
  <a href="./README.pl.md">Polski/波兰语</a> |
  <a href="./README.pt.md">Português/葡萄牙语</a> |
  <a href="./README.nl.md">Nederlands/荷兰语</a> |
  <a href="./README.sv.md">Svenska/瑞典语</a> |
  <a href="./README.uk.md">Українська/乌克兰语</a> |
  <a href="./README.ar.md">العربية/阿拉伯语</a>
</p>

# 天殇·悦康（TianshangHealth）

> **隐私优先、完全离线的 Android 综合健康管理应用。**
> 将经期追踪、运动健身、睡眠监测、营养管理、步数统计等健康维度整合到统一的本地加密数据库中，并通过设备端 AI 提供跨维度健康洞察。

---

## 核心亮点

- **零网络权限** — 所有数据保存在本地。无云端、无分析、无 Firebase。
- **军工级加密** — SQLCipher AES-256 数据库由 Android Keystore 硬件级密钥保护。
- **模块化架构** — 12 个 Gradle 模块，遵循 MVVM + Repository + Hilt + Compose 架构。
- **本地 AI 推理** — TensorFlow Lite 模型增强经期预测与情绪分析，全程无需联网。
- **21 种语言** — 包括阿拉伯语 RTL 支持，所有语言从第一天起即完整本地化。
- **性别自适应 UI** — 底部导航与功能根据用户性别选择动态调整。

---

## 功能模块

| 健康维度     | 主要能力                                        |
| -------- | ------------------------------------------- |
| **经期追踪** | 日历视图、周期预测引擎（IQR 过滤 + 指数衰减）、症状记录、提醒          |
| **步数统计** | 硬件计步器前台服务 + 加速度计兜底、WorkManager 同步、经期-步数关联洞察 |
| **运动健身** | 17 种运动类型、MET 卡路里计算、经期阶段性运动建议                |
| **睡眠监测** | 手动睡眠记录、睡眠质量评分、Canvas 趋势图                    |
| **营养管理** | 饮食记录（早/午/晚/加餐）、宏量营养素追踪、饮水量统计                |
| **分析洞察** | 跨维度统计引擎、健康建议、医疗报告导出、TFLite 预测增强             |
| **安全隐私** | PIN（Argon2id）+ 生物识别锁定、加密 ZIP 备份/恢复、敏感页面截图保护 |

---

## 技术栈

| 层级    | 技术                                          | 版本              |
| ----- | ------------------------------------------- | --------------- |
| 语言    | Kotlin                                      | 1.9.24          |
| 构建    | Gradle + Android Gradle Plugin              | 8.9 / 8.6.0     |
| UI    | Jetpack Compose (Material 3)                | BOM 2024.09.00  |
| 架构    | MVVM + Repository + StateFlow               | —               |
| 依赖注入  | Hilt (KSP)                                  | 2.51.1          |
| 导航    | Navigation Compose                          | 2.7.6           |
| 数据库   | Room + SQLCipher                            | 2.6.1 / 4.5.4   |
| 后台任务  | WorkManager                                 | 2.9.0           |
| 加密    | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78        |
| 图表    | MPAndroidChart + Vico                       | v3.1.0 / 1.13.1 |
| 本地 AI | TensorFlow Lite                             | 2.14.0          |
| 静态分析  | Detekt                                      | 1.23.6          |

---

## 项目架构

```
app/                    # 应用壳层：单一 NavHost、主题、MainActivity
├── navigation/MainNavigation.kt        # 动态底部导航 + 侧边路由
core/                   # 核心基础设施
├── common/             # 共享组件、工具类、全部 21 语言字符串资源
├── database/           # Room 实体、DAO、Repository、数据库迁移
└── security/           # Keystore、SQLCipher、Argon2id、生物识别、应用锁
feature/                # 功能模块
├── onboarding/         # 首次启动：性别选择
├── dashboard/          # 首页仪表盘：今日概览与健康洞察
├── period/             # 经期追踪：预测引擎、提醒、备份、设置
├── steps/              # 硬件计步器前台服务 + WorkManager 兜底同步
├── fitness/            # 运动记录 + 经期阶段性建议
├── sleep/              # 睡眠记录 + Canvas 趋势图
├── nutrition/          # 饮食记录 + 饮水追踪
└── analysis/           # 跨维度分析 + TFLite 本地 AI 推理
```

---

## 安全与隐私

- **静态数据加密**：整个 `tianshang_health.db` 使用 SQLCipher AES-256 加密。数据库密码为 32 位随机字符串，由 Android Keystore 加密存储，永不出 Keystore。
- **身份认证**：PIN 码采用 Argon2id 哈希，支持 BiometricPrompt（强生物识别）作为回退。
- **网络隔离**：`AndroidManifest.xml` 中**无任何 `INTERNET` 权限**。无 Firebase、无分析、无崩溃上报。
- **应用锁**：应用进入后台后按设定延时（立即 / 30秒 / 1分钟 / 5分钟）自动锁定。`AppLockScreen` 以 `zIndex(10f)` 覆盖整个界面。
- **备份与导出**：支持用户自定义密码的加密 ZIP 备份/恢复，同时支持 CSV 导出。
- **加固措施**：敏感页面使用 `FLAG_SECURE` 防止截图；启动时检测 `Debug.isDebuggerConnected()` 反调试。

---

## 快速开始

### 环境要求

- JDK 17
- Android Studio（推荐 Ladybug 或更新版本）
- 已安装 API 35 的 Android SDK

### 编译 Debug APK

```bash
./gradlew :app:assembleDebug
```

APK 输出路径：

```
app/build/outputs/apk/debug/app-debug.apk
```

### 安装到设备

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 编译 Release APK

Release 构建需要在 `local.properties` 中配置签名信息：

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

然后执行：

```bash
./gradlew :app:assembleRelease
```

生成的签名 APK 文件名：

```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### 发布前安全检查

```bash
python check_release_security.py
```

---

## 质量与测试

- **Detekt**：零容忍静态分析（`maxIssues: 0`）。
  
  ```bash
  ./gradlew detektAll
  ```
- **单元测试**：
  
  ```bash
  ./gradlew test
  ```
- **Instrumentation 测试**：
  
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **字符串资源校验**：
  
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## 多语言支持

全部 21 种语言字符串集中在 `core/common/src/main/res/values[-lang]/strings.xml` 中统一维护：

English/英语, 简体中文/简体中文, 日本語/日语, 한국어/韩语, Français/法语, Español/西班牙语, Deutsch/德语, Русский/俄语, Italiano/意大利语, Türkçe/土耳其语, हिन्दी/印地语, ภาษาไทย/泰语, Tiếng Việt/越南语, Bahasa Indonesia/印尼语, Bahasa Melayu/马来语, Polski/波兰语, Português/葡萄牙语, Nederlands/荷兰语, Svenska/瑞典语, Українська/乌克兰语, العربية/阿拉伯语

---

## 开源许可

本项目采用 MIT 许可证。详见项目文件。

---

<p align="center">
  <b>数据主权永远属于用户。开发者从技术上无法获取您的数据。</b>
</p>
