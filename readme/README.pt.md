<p align="right">
  <a href="../README.md">English/Inglês</a> |
  <a href="./README.zh-CN.md">简体中文/Chinês (simplificado)</a> |
  <a href="./README.ja.md">日本語/Japonês</a> |
  <a href="./README.ko.md">한국어/Coreano</a> |
  <a href="./README.fr.md">Français/Francês</a> |
  <a href="./README.es.md">Español/Espanhol</a> |
  <a href="./README.de.md">Deutsch/Alemão</a> |
  <a href="./README.ru.md">Русский/Russo</a> |
  <a href="./README.it.md">Italiano/Italiano</a> |
  <a href="./README.tr.md">Türkçe/Turco</a> |
  <a href="./README.hi.md">हिन्दी/Hindi</a> |
  <a href="./README.th.md">ภาษาไทย/Tailandês</a> |
  <a href="./README.vi.md">Tiếng Việt/Vietnamita</a> |
  <a href="./README.id.md">Bahasa Indonesia/Indonésio</a> |
  <a href="./README.ms.md">Bahasa Melayu/Malaio</a> |
  <a href="./README.pl.md">Polski/Polonês</a> |
  <a href="./README.nl.md">Nederlands/Holandês</a> |
  <a href="./README.sv.md">Svenska/Sueco</a> |
  <a href="./README.uk.md">Українська/Ucraniano</a> |
  <a href="./README.ar.md">العربية/Árabe</a>
</p>

# TianshangHealth

> **Companheiro de saúde Android totalmente offline e focado em privacidade.**
> Combina monitoramento de ciclo, fitness, sono, nutrição e contagem de passos em um único banco de dados local criptografado — com análises multidimensionais baseadas em IA local.

---

## Destaques

- **Zero permissões de rede** — Todos os dados permanecem no seu dispositivo. Sem nuvem, sem análises, sem Firebase.
- **Criptografia de nível militar** — Banco de dados SQLCipher AES-256 protegido por chaves de hardware Android Keystore.
- **Arquitetura modular** — 12 módulos Gradle seguindo MVVM + Repository + Hilt + Compose.
- **Inferência de IA local** — Modelos TensorFlow Lite melhoram a previsão do ciclo e análise de humor sem sair do dispositivo.
- **21 idiomas** — Incluindo suporte RTL para árabe; localizado desde o primeiro dia.
- **Interface adaptativa por gênero** — Navegação inferior e funcionalidades se ajustam conforme o gênero selecionado.

---

## Funcionalidades

| Domínio | Capacidades |
|---------|-------------|
| **Monitoramento de ciclo** | Visualização calendário, motor de previsão (filtragem IQR + decaimento exponencial), registro de sintomas, lembretes |
| **Passos** | Serviço em primeiro plano de contador de passos + acelerômetro de fallback, sincronização WorkManager, análise de passos por fase do ciclo |
| **Fitness** | 17 tipos de exercício, calculadora calórica MET, recomendações de treino por fase do ciclo |
| **Sono** | Registro manual de sono, pontuação de qualidade, gráfico de tendências Canvas |
| **Nutrição** | Registro de refeições (café/almoço/jantar/lanche), monitoramento de macronutrientes, ingestão de água |
| **Análise** | Motor analítico multidimensional, sugestões de saúde, exportação de relatórios médicos, aprimoramento de previsão TFLite |
| **Segurança** | PIN (Argon2id) + bloqueio biométrico, backup/restauração ZIP criptografado, proteção de captura de tela em telas sensíveis |

---

## Stack tecnológico

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| Linguagem | Kotlin | 1.9.24 |
| Build | Gradle + Android Gradle Plugin | 8.9 / 8.6.0 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Arquitetura | MVVM + Repository + StateFlow | — |
| DI | Hilt (KSP) | 2.51.1 |
| Navegação | Navigation Compose | 2.7.6 |
| Banco de dados | Room + SQLCipher | 2.6.1 / 4.5.4 |
| Segundo plano | WorkManager | 2.9.0 |
| Criptografia | Android Keystore + Bouncy Castle (Argon2id) | — / 1.78 |
| Gráficos | MPAndroidChart + Vico | v3.1.0 / 1.13.1 |
| ML | TensorFlow Lite | 2.14.0 |
| Análise estática | Detekt | 1.23.6 |

---

## Arquitetura

```
app/                    # Shell do app: NavHost único, tema, MainActivity
├── navigation/MainNavigation.kt        # Navegação inferior condicional + rotas laterais
core/
├── common/             # Componentes UI compartilhados, utilitários, todos os recursos de string (21 idiomas)
├── database/           # Entidades Room, DAOs, repositórios, migrações
└── security/           # Keystore, SQLCipher, Argon2id, autenticação biométrica, bloqueio de app
feature/
├── onboarding/         # Seleção de gênero no primeiro uso
├── dashboard/          # Visão geral do dia, insights de saúde
├── period/             # Monitoramento de ciclo, previsão, lembretes, backup, configurações
├── steps/              # Serviço contador de passos + sincronização WorkManager
├── fitness/            # Registros de treino + recomendações por fase do ciclo
├── sleep/              # Registro de sono + gráfico de tendências
├── nutrition/          # Monitoramento de refeições + água
└── analysis/           # Análises multidimensionais + inferência ML TFLite
```

---

## Segurança e privacidade

- **Dados em repouso**: SQLCipher AES-256 criptografa todo o `tianshang_health.db`. A senha do banco de dados é uma string aleatória de 32 caracteres criptografada pelo Android Keystore e nunca armazenada em texto puro.
- **Autenticação**: PINs são hash com Argon2id. BiometricPrompt (`BIOMETRIC_STRONG`) está disponível como alternativa.
- **Rede**: `AndroidManifest.xml` **não contém permissão `INTERNET`**. Sem Firebase, sem análises, sem relatórios de falha.
- **Bloqueio de app**: Ao minimizar, um atraso configurável é ativado (imediato / 30s / 1min / 5min). A tela de bloqueio sobrepõe todo o app com `zIndex(10f)`.
- **Backup**: Exportação/restauração ZIP criptografado com senha fornecida pelo usuário. Exportação CSV também suportada.
- **Proteção**: `FLAG_SECURE` em telas sensíveis; verificação `Debug.isDebuggerConnected()` na inicialização.

---

## Primeiros passos

### Pré-requisitos

- JDK 17
- Android Studio (Ladybug ou superior recomendado)
- Android SDK com API 35 instalada

### Compilar APK Debug

```bash
./gradlew :app:assembleDebug
```

O APK estará localizado em:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Instalar em um dispositivo

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Compilar APK Release

Builds Release exigem configuração de assinatura em `local.properties`:

```properties
RELEASE_STORE_FILE=path/to/keystore.jks
RELEASE_STORE_PASSWORD=your-store-password
RELEASE_KEY_ALIAS=your-alias
RELEASE_KEY_PASSWORD=your-key-password
```

Então execute:

```bash
./gradlew :app:assembleRelease
```

O APK assinado será nomeado:
```
app/build/outputs/apk/release/TianshangHealth-v1.0.0.apk
```

### Verificação de segurança pré-release

```bash
python check_release_security.py
```

---

## Qualidade e testes

- **Detekt**: Análise estática com tolerância zero (`maxIssues: 0`).
  ```bash
  ./gradlew detektAll
  ```
- **Testes unitários**:
  ```bash
  ./gradlew test
  ```
- **Testes instrumentados**:
  ```bash
  ./gradlew :feature:period:connectedAndroidTest
  ./gradlew :feature:analysis:connectedAndroidTest
  ```
- **Validação de strings**:
  ```bash
  python check_strings.py
  python check_string_resources.py
  ```

---

## Suporte a múltiplos idiomas

Todos os recursos de string em 21 idiomas são centralizados em `core/common/src/main/res/values[-lang]/strings.xml`:

English/Inglês, 简体中文/Chinês (simplificado), 日本語/Japonês, 한국어/Coreano, Français/Francês, Español/Espanhol, Deutsch/Alemão, Русский/Russo, Italiano/Italiano, Türkçe/Turco, हिन्दी/Hindi, ภาษาไทย/Tailandês, Tiếng Việt/Vietnamita, Bahasa Indonesia/Indonésio, Bahasa Melayu/Malaio, Polski/Polonês, Português/Português, Nederlands/Holandês, Svenska/Sueco, Українська/Ucraniano, العربية/Árabe

---

## Licença

Este projeto é licenciado sob a MIT License. Consulte os arquivos do projeto para detalhes.

---

<p align="center">
  <b>A soberania dos dados pertence ao usuário. O desenvolvedor não pode tecnicamente acessar seus dados.</b>
</p>
