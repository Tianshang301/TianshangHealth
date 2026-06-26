<p align="right">
  <a href="../README.md">English/Inglés</a> |
  <a href="./README.zh-CN.md">简体中文/Chino simplificado</a> |
  <a href="./README.ja.md">日本語/Japonés</a> |
  <a href="./README.ko.md">한국어/Coreano</a> |
  <a href="./README.fr.md">Français/Francés</a> |
  <a href="./README.de.md">Deutsch/Alemán</a> |
  <a href="./README.ru.md">Русский/Ruso</a> |
  <a href="./README.it.md">Italiano/Italiano</a> |
  <a href="./README.tr.md">Türkçe/Turco</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Tailandés</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamita</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonesio</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malayo</a> |
  <a href="./README.pl.md">Polski/Polaco</a> |
  <a href="./README.pt.md">Português/Portugués</a> |
  <a href="./README.nl.md">Nederlands/Neerlandés</a> |
  <a href="./README.sv.md">Svenska/Sueco</a> |
  <a href="./README.uk.md">Українська/Ucraniano</a> |
  <a href="./README.ar.md">العربية/Árabe</a>
</p>

# TianshangHealth(天殇·悦康)

> **Compañero de salud Android con enfoque en privacidad y totalmente sin conexión.**
> Combina seguimiento menstrual, fitness, sueño, nutrición y conteo de pasos en una base de datos local cifrada — con análisis multidimensionales impulsados por IA en el dispositivo.

---

## Puntos destacados

- **Cero permisos de red** — Todos los datos permanecen en tu dispositivo. Sin nube, sin analytics, sin Firebase.
- **Cifrado de nivel militar** — Base de datos SQLCipher AES-256 protegida por claves respaldadas por hardware de Android Keystore.
- **Arquitectura modular** — 12 módulos Gradle siguiendo MVVM + Repository + Hilt + Compose.
- **Inferencia de IA local** — Modelos TensorFlow Lite mejoran la predicción menstrual y el análisis de humor sin salir del dispositivo.
- **21 idiomas** — Incluyendo soporte RTL para árabe; localizado desde el primer día.
- **UI adaptativa por género** — La navegación inferior y los recursos se ajustan según el género seleccionado por el usuario.

---

## Funciones

| Dominio | Capacidades |
|--------|-------------|
| **Seguimiento menstrual** | Vista de calendario, motor de predicción de ciclos (filtrado IQR + decaimiento exponencial), registro de síntomas, recordatorios |
| **Pasos** | Servicio de contador de pasos en primer plano con hardware + respaldo de acelerómetro, sincronización WorkManager, análisis de pasos por fase del ciclo |
| **Fitness** | 17 tipos de ejercicio, calculadora de calorías basada en MET, recomendaciones de ejercicio por fase del ciclo |
| **Sueño** | Registro manual de sueño, puntuación de calidad, gráfico de tendencias Canvas |
| **Nutrición** | Registro de comidas (desayuno/almuerzo/cena/aperitivo), seguimiento de macronutrientes, ingesta de agua |
| **Análisis** | Motor analítico multidimensional, sugerencias de salud, exportación de informes médicos, mejora de predicción con TFLite |
| **Seguridad** | PIN (Argon2id) + bloqueo biométrico, copia de seguridad/restauración ZIP cifrada, protección contra capturas de pantalla en páginas sensibles |

---

## Stack tecnológico

| Capa | Tecnología | Versión |
|-------|-----------|---------|
| Lenguaje | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Arquitectura | MVVM + Repository + StateFlow | — |
| Inyección de dependencias | Hilt (KSP) | 2.51.1 |
| Navegación | Navigation Compose | 2.7.6 |
| Base de datos | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Tareas en segundo plano | WorkManager | 2.9.0 |
| Criptografía | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Gráficos | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Análisis estático | Detekt | 1.23.6 |

---

## Arquitectura

```
app/                    # Capa del aplicativo: NavHost único, tema, MainActivity
├── navigation/MainNavigation.kt        # Navegación inferior condicional + rutas laterales
core/
├── common/             # Componentes compartidos, utilidades, todos los recursos de texto (21 idiomas)
├── database/           # Entidades Room, DAOs, repositorios, migraciones
└── security/           # Keystore, SQLCipher, Argon2id, biometría, bloqueo del app
feature/
├── onboarding/         # Primera inicialización: selección de género
├── dashboard/          # Panel de inicio: visión general del día e insights de salud
├── period/             # Seguimiento menstrual: motor de predicción, recordatorios, copia de seguridad, ajustes
├── steps/              # Servicio de contador de pasos en primer plano + sincronización WorkManager
├── fitness/            # Registros de ejercicios + recomendaciones por fase del ciclo
├── sleep/              # Registro de sueño + gráfico de tendencias Canvas
├── nutrition/          # Registro de comidas + ingesta de agua
└── analysis/           # Análisis multidimensionales + inferencia de IA TFLite local
```

---

## Seguridad

- **Datos en reposo**: Toda la base de datos `tianshang_health.db` está cifrada con SQLCipher AES-256. La contraseña de la base de datos es una cadena aleatoria de 32 caracteres cifrada por Android Keystore y nunca almacenada en texto plano.
- **Autenticación**: Los PINs se hashean con Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) está disponible como fallback.
- **Red**: El `AndroidManifest.xml` no contiene **ninguna permiso `INTERNET`**. Sin Firebase, sin analytics, sin informes de fallos.
- **Bloqueo de la aplicación**: Al ir a segundo plano, la app se bloquea automáticamente después de un retraso configurable (inmediato / 30s / 1min / 5min). La `AppLockScreen` se sobrepone a toda la app con `zIndex(10f)`.
- **Copia de seguridad**: Exportación/restauración ZIP cifrada con contraseña proporcionada por el usuario. La exportación CSV también está soportada.
- **Endurecimiento**: `FLAG_SECURE` en páginas sensibles; verificación de `Debug.isDebuggerConnected()` al inicio.

---

## Primeros pasos

### Requisitos previos

- JDK 17
- Android Studio (Ladybug o más reciente recomendado)
- Android SDK con API 35 instalada

### Compilar APK Debug

```bash
./gradlew :app:assembleDebug
```

El APK se ubicará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Instalar en un dispositivo

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Compilar APK Release

Las compilaciones Release requieren configuración de firma en `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Luego ejecuta:

```bash
./gradlew :app:assembleRelease
```

El APK firmado se nombrará:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Verificación de seguridad previa al lanzamiento

```bash
python check_release_security.py
```

---

## Calidad y pruebas

- **Detekt**: Análisis estático con tolerancia cero (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Pruebas unitarias**:
  ```bash
  ./gradlew test
  ```
- **Pruebas de instrumentación**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Validación de cadenas**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Soporte multilingüe

Todas las cadenas de los 21 idiomas se centralizan en `core/common/src/main/res/values[-lang]/strings.xml`:

English/Inglés, 简体中文/Chino simplificado, 日本語/Japonés, 한국어/Coreano, Français/Francés, Español/Español, Deutsch/Alemán, Русский/Ruso, Italiano/Italiano, Türkçe/Turco, हिन्दी/Hindi, ภาษาไทย/Tailandés, Tiếng Việt/Vietnamita, Bahasa Indonesia/Indonesio, Bahasa Melayu/Malayo, Polski/Polaco, Português/Portugués, Nederlands/Neerlandés, Svenska/Sueco, Українська/Ucraniano, العربية/Árabe

---

## Licencia

Este proyecto está licenciado bajo la Licencia MIT. Consulta los archivos del proyecto para más detalles.

---

<p align="center">
  <b>La soberanía de los datos pertenece al usuario. El desarrollador no puede acceder a tus datos por diseño.</b>
</p>
