<p align="right">
  <a href="../README.md">English/Anglais</a> |
  <a href="./README.zh-CN.md">简体中文/Chinois (simplifié)</a> |
  <a href="./README.ja.md">日本語/Japonais</a> |
  <a href="./README.ko.md">한국어/Coréen</a> |
  <a href="./README.es.md">Español/Espagnol</a> |
  <a href="./README.de.md">Deutsch/Allemand</a> |
  <a href="./README.ru.md">Русский/Russe</a> |
  <a href="./README.it.md">Italiano/Italien</a> |
  <a href="./README.tr.md">Türkçe/Turc</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Thaï</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamien</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonésien</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malais</a> |
  <a href="./README.pl.md">Polski/Polonais</a> |
  <a href="./README.pt.md">Português/Portugais</a> |
  <a href="./README.nl.md">Nederlands/Néerlandais</a> |
  <a href="./README.sv.md">Svenska/Suédois</a> |
  <a href="./README.uk.md">Українська/Ukrainien</a> |
  <a href="./README.ar.md">العربية/Arabe</a>
</p>

# TianshangHealth

> **Compagnon de santé Android entièrement hors ligne et axé sur la confidentialité.**
> Combine le suivi menstruel, la fitness, le sommeil, la nutrition et le comptage de pas dans une base de données locale chiffrée — avec des analyses multidimensionnelles alimentées par une IA embarquée.

---

## Points forts

- **Zéro autorisation réseau** — Toutes les données restent sur votre appareil. Pas de cloud, pas d'analytics, pas de Firebase.
- **Chiffrement de niveau militaire** — Base de données SQLCipher AES-256 sécurisée par des clés matérielles Android Keystore.
- **Architecture modulaire** — 12 modules Gradle suivant MVVM + Repository + Hilt + Compose.
- **Inférence IA locale** — Les modèles TensorFlow Lite améliorent la prédiction menstruelle et l'analyse de l'humeur sans quitter l'appareil.
- **21 langues** — Incluant le support RTL pour l'arabe ; localisé dès le premier jour.
- **UI adaptative selon le genre** — La navigation inférieure et les fonctionnalités s'ajustent selon le genre sélectionné.

---

## Fonctionnalités

| Domaine | Capacités |
|--------|-------------|
| **Suivi menstruel** | Vue calendrier, moteur de prédiction des cycles (filtrage IQR + décroissance exponentielle), journal des symptômes, rappels |
| **Pas** | Service de compteur de pas en premier plan matériel + secours accéléromètre, synchronisation WorkManager, analyses pas-phase du cycle |
| **Fitness** | 17 types d'exercices, calcul des calories basé sur les MET, recommandations selon la phase du cycle |
| **Sommeil** | Journal manuel du sommeil, score de qualité, graphique de tendance Canvas |
| **Nutrition** | Journal des repas (petit-déjeuner/déjeuner/dîner/collations), suivi des macronutriments, apport en eau |
| **Analyse** | Moteur analytique multidimensionnel, suggestions de santé, export de rapports médicaux, amélioration TFLite des prédictions |
| **Sécurité** | Code PIN (Argon2id) + verrou biométrique, sauvegarde/restauration ZIP chiffrée, protection contre les captures d'écran sur les pages sensibles |

---

## Stack technique

| Couche | Technologie | Version |
|-------|-----------|---------|
| Langage | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Architecture | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navigation | Navigation Compose | 2.7.6 |
| Base de données | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Tâches d'arrière-plan | WorkManager | 2.9.0 |
| Crypto | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Graphiques | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Analyse statique | Detekt | 1.23.6 |

---

## Architecture

```
app/                    # Coquille de l'application : NavHost unique, thème, MainActivity
├── navigation/MainNavigation.kt        # Navigation inférieure conditionnelle + routes latérales
core/
├── common/             # Composants partagés, utilitaires, toutes les ressources textuelles des 21 langues
├── database/           # Entités Room, DAOs, repositories, migrations
└── security/           # Keystore, SQLCipher, Argon2id, authentification biométrique, verrou d'application
feature/
├── onboarding/         # Premier lancement : sélection du genre
├── dashboard/          # Tableau de bord d'accueil : aperçu du jour et analyses
├── period/             # Suivi des règles : moteur de prédiction, rappels, sauvegarde, paramètres
├── steps/              # Service de compteur de pas matériel + synchronisation WorkManager
├── fitness/            # Entraînements + recommandations selon la phase du cycle
├── sleep/              # Journal de sommeil + graphique de tendance Canvas
├── nutrition/          # Journal alimentaire + apport en eau
└── analysis/           # Analyses multidimensionnelles + inférence IA TFLite locale
```

---

## Sécurité

- **Données au repos** : L'ensemble de la base `tianshang_health.db` est chiffrée par SQLCipher AES-256. Le mot de passe est une chaîne aléatoire de 32 caractères chiffrée par Android Keystore et jamais stockée en clair.
- **Authentification** : Les codes PIN sont hachés avec Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) est disponible en secours.
- **Réseau** : `AndroidManifest.xml` ne contient **aucune autorisation `INTERNET`**. Pas de Firebase, pas d'analytics, pas de rapport de crash.
- **Verrouillage de l'application** : Le passage en arrière-plan déclenche un verrouillage configurable (immédiat / 30s / 1min / 5min). L'écran de verrouillage recouvre toute l'application à `zIndex(10f)`.
- **Sauvegarde** : Export/restauration ZIP chiffré avec mot de passe utilisateur. L'export CSV est également supporté.
- **Renforcement** : `FLAG_SECURE` sur les pages sensibles ; vérification de `Debug.isDebuggerConnected()` au démarrage.

---

## Démarrage

### Prérequis

- JDK 17
- Android Studio (Ladybug ou plus récent recommandé)
- Android SDK avec API 35 installée

### Compiler l'APK Debug

```bash
./gradlew :app:assembleDebug
```

L'APK sera situé à :
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installer sur un appareil

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Compiler l'APK Release

Les builds Release nécessitent une configuration de signature dans `local.properties` :

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Puis exécutez :

```bash
./gradlew :app:assembleRelease
```

L'APK signé sera nommé :
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Vérification de sécurité pré-release

```bash
python check_release_security.py
```

---

## Qualité et tests

- **Detekt** : Analyse statique tolérance zéro (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Tests unitaires** :
  ```bash
  ./gradlew test
  ```
- **Tests d'instrumentation** :
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Validation des chaînes** :
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Support multilingue

Toutes les chaînes des 21 langues sont centralisées dans `core/common/src/main/res/values[-lang]/strings.xml` :

English/Anglais, 简体中文/Chinois (simplifié), 日本語/Japonais, 한국어/Coréen, Français/Français, Español/Espagnol, Deutsch/Allemand, Русский/Russe, Italiano/Italien, Türkçe/Turc, हिन्दी/Hindi, ภาษาไทย/Thaï, Tiếng Việt/Vietnamien, Bahasa Indonesia/Indonésien, Bahasa Melayu/Malais, Polski/Polonais, Português/Portugais, Nederlands/Néerlandais, Svenska/Suédois, Українська/Ukrainien, العربية/Arabe

---

## Licence

Ce projet est sous licence MIT. Voir les fichiers du projet pour plus de détails.

---

<p align="center">
  <b>La souveraineté des données appartient à l'utilisateur. Le développeur ne peut pas accéder à vos données par conception.</b>
</p>
