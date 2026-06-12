<p align="right">
  <a href="../README.md">English/英語</a> |
  <a href="./README.zh-CN.md">简体中文/簡体中国語</a> |
  <a href="./README.ko.md">한국어/韓国語</a> |
  <a href="./README.fr.md">Français/フランス語</a> |
  <a href="./README.es.md">Español/スペイン語</a> |
  <a href="./README.de.md">Deutsch/ドイツ語</a> |
  <a href="./README.ru.md">Русский/ロシア語</a> |
  <a href="./README.it.md">Italiano/イタリア語</a> |
  <a href="./README.tr.md">Türkçe/トルコ語</a> |
  <a href="./README.hi.md">हिन्दी/ヒンディー語</a> |
  <a href="./README.th.md">ภาษาไทย/タイ語</a> |
  <a href="./README.vi.md">Tiếng Việt/ベトナム語</a> |
  <a href="./README.id.md">Bahasa Indonesia/インドネシア語</a> |
  <a href="./README.ms.md">Bahasa Melayu/マレー語</a> |
  <a href="./README.pl.md">Polski/ポーランド語</a> |
  <a href="./README.pt.md">Português/ポルトガル語</a> |
  <a href="./README.nl.md">Nederlands/オランダ語</a> |
  <a href="./README.sv.md">Svenska/スウェーデン語</a> |
  <a href="./README.uk.md">Українська/ウクライナ語</a> |
  <a href="./README.ar.md">العربية/アラビア語</a>
</p>

# 天殇健康（TianshangHealth）

> **プライバシー重視、完全オフラインの Android 統合健康管理アプリ。**
> 生理周期追跡、運動、睡眠、栄養、歩数カウントなどの健康データを1つの暗号化されたローカルデータベースに統合し、オンデバイス AI による多次元健康インサイトを提供します。

---

## 主な特長

- **ゼロネットワーク権限** — すべてのデータは端末に保存されます。クラウドなし、アナリティクスなし、Firebase なし。
- **軍事レベルの暗号化** — SQLCipher AES-256 データベースを Android Keystore のハードウェア支援キーで保護。
- **モジュール化アーキテクチャ** — MVVM + Repository + Hilt + Compose に準拠した 12 の Gradle モジュール。
- **ローカル AI 推論** — TensorFlow Lite モデルが生理予測や気分分析を端末内で強化。
- **21 言語対応** — アラビア語 RTL 対応を含み、初日から完全にローカライズされています。
- **性別適応 UI** — ユーザーの性別選択に応じてボトムナビゲーションと機能を調整。

---

## 機能

| 領域 | 機能詳細 |
|--------|-------------|
| **生理周期管理** | カレンダー表示、周期予測エンジン（IQR フィルタリング + 指数減衰）、症状記録、リマインダー |
| **歩数カウント** | ハードウェア歩数計フォアグラウンドサービス + 加速度計兜底、WorkManager 同期、周期-歩数関連インサイト |
| **フィットネス** | 17 種類の運動、MET ベースのカロリー計算、周期に応じた運動提案 |
| **睡眠モニタリング** | 手動睡眠記録、品質スコア、Canvas トレンドグラフ |
| **栄養管理** | 食事記録（朝/昼/晩/間食）、マクロ栄養素追跡、水分摂取 |
| **分析** | 多次元統計エンジン、健康提案、医療レポート出力、TFLite 予測強化 |
| **セキュリティ** | PIN（Argon2id）+ 生体認証ロック、暗号化 ZIP バックアップ/復元、機密ページのスクリーンショット保護 |

---

## 技術スタック

| 層 | 技術 | バージョン |
|-------|-----------|---------|
| 言語 | Kotlin | 1.9.24 |
| ビルド | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| アーキテクチャ | MVVM + Repository + StateFlow | — |
| 依存性注入 | Hilt (KSP) | 2.51.1 |
| ナビゲーション | Navigation Compose | 2.7.6 |
| データベース | Room + SQLCipher | 2.6.1 / 4.5.4 |
| バックグラウンド | WorkManager | 2.9.0 |
| 暗号化 | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| チャート | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| 静的解析 | Detekt | 1.23.6 |

---

## プロジェクトアーキテクチャ

```
app/                    # アプリケーションシェル：単一 NavHost、テーマ、MainActivity
├── navigation/MainNavigation.kt        # 動的ボトムナビゲーション + サイドルート
core/
├── common/             # 共有コンポーネント、ユーティリティ、全 21 言語文字列リソース
├── database/           # Room エンティティ、DAO、Repository、データベース移行
└── security/           # Keystore、SQLCipher、Argon2id、生体認証、アプリロック
feature/
├── onboarding/         # 初回起動：性別選択
├── dashboard/          # ホームダッシュボード：今日の概要と健康インサイト
├── period/             # 生理周期管理：予測エンジン、リマインダー、バックアップ、設定
├── steps/              # ハードウェア歩数計サービス + WorkManager 兜底同期
├── fitness/            # 運動記録 + 周期段階別提案
├── sleep/              # 睡眠記録 + Canvas トレンドグラフ
├── nutrition/          # 食事記録 + 水分摂取
└── analysis/           # 多次元分析 + TFLite ローカル AI 推論
```

---

## セキュリティとプライバシー

- **保存データの暗号化**：`tianshang_health.db` 全体を SQLCipher AES-256 で暗号化。データベースパスワードは 32 文字のランダム文字列で、Android Keystore で暗号化され、決して外部に出ません。
- **認証**：PIN は Argon2id でハッシュ化され、強生体認証の BiometricPrompt をフォールバックとして利用可能。
- **ネットワーク**：`AndroidManifest.xml` には **`INTERNET` 権限がありません**。Firebase、アナリティクス、クラッシュレポートは一切ありません。
- **アプリロック**：アプリがバックグラウンドに移行すると設定された遅延（即時 / 30秒 / 1分 / 5分）後に自動ロック。`AppLockScreen` は `zIndex(10f)` で全体を覆います。
- **バックアップ**：ユーザー指定のパスワードで暗号化された ZIP バックアップ/復元に対応。CSV エクスポートも可能。
- **保護対策**：機密画面で `FLAG_SECURE` を使用してスクリーンショットを防止。起動時に `Debug.isDebuggerConnected()` でデバッグ検出。

---

## クイックスタート

### 前提条件

- JDK 17
- Android Studio（Ladybug または更新版を推奨）
- Android SDK（API 35 インストール済み）

### Debug APK のビルド

```bash
./gradlew :app:assembleDebug
```

APK の出力先：
```
app/build/outputs/apk/debug/app-debug.apk
```

### 端末へのインストール

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release APK のビルド

Release ビルドには `local.properties` への署名設定が必要です：

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

次に実行：

```bash
./gradlew :app:assembleRelease
```

生成される署名済み APK のファイル名：
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### リリース前のセキュリティチェック

```bash
python check_release_security.py
```

---

## 品質とテスト

- **Detekt**：ゼロトレランスの静的解析（`maxIssues: 0`）。
  ```bash
  ./gradlew detektAll
  ```
- **ユニットテスト**：
  ```bash
  ./gradlew test
  ```
- **Instrumentation テスト**：
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **文字列リソースの検証**：
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## 多言語対応

21 言語のすべての文字列は `core/common/src/main/res/values[-lang]/strings.xml` に集中して管理されています：

English/英語, 简体中文/簡体中国語, 日本語/日本語, 한국어/韓国語, Français/フランス語, Español/スペイン語, Deutsch/ドイツ語, Русский/ロシア語, Italiano/イタリア語, Türkçe/トルコ語, हिन्दी/ヒンディー語, ภาษาไทย/タイ語, Tiếng Việt/ベトナム語, Bahasa Indonesia/インドネシア語, Bahasa Melayu/マレー語, Polski/ポーランド語, Português/ポルトガル語, Nederlands/オランダ語, Svenska/スウェーデン語, Українська/ウクライナ語, العربية/アラビア語

---

## ライセンス

このプロジェクトは MIT ライセンスの下で提供されています。詳細はプロジェクトファイルを参照してください。

---

<p align="center">
  <b>データ主権は永遠にユーザーに属します。開発者は技術的にあなたのデータにアクセスできません。</b>
</p>
