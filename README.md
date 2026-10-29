# VaultX — Smart Collectibles & Portfolio Tracker

[![Android CI](https://img.shields.io/badge/CI-GitHub_Actions-2088FF?logo=github-actions&logoColor=white)](.github/workflows/android_ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture_%2B_UDF-16A34A)](#architecture--modularization)
[![Hilt](https://img.shields.io/badge/DI-Dagger_Hilt_(KSP)-EC4899)](https://dagger.dev/hilt/)

**VaultX** is an enterprise-grade Android architecture showcase designed to track graded trading cards (`PSA 10`, `BGS 9.5`), factory-sealed booster boxes, and slabbed comic books (`CGC 9.8`) with real-time portfolio valuation, cost-basis calculation, and Offline-First synchronization.

---

## Key Architectural Highlights

1. **Strict Multi-Module Clean Architecture**:
   - **`:core:model`** is a **100% pure Kotlin/JVM module** with zero Android SDK dependencies (`android.*`), guaranteeing fast JVM unit tests and strict domain isolation.
   - Feature modules (`:feature:inventory`, `:feature:scanner`) depend strictly downward on `:core:*` modules and never on sibling features.
2. **Offline-First Single Source of Truth (SSOT)**:
   - UI and Domain layers exclusively observe reactive `Flow<List<CollectibleItem>>` streams emitted by **Room (`CollectibleDao`)**.
   - Remote market price synchronization (`MarketFeedApi`) writes updates directly into Room, which automatically triggers downstream recomposition in `GetPortfolioSummaryUseCase` and `InventoryViewModel`.
3. **Unidirectional Data Flow (UDF) & Reactive State Management**:
   - `InventoryViewModel` combines multiple domain streams (`GetPortfolioSummaryUseCase`, `FilterCollectiblesUseCase`, search query, and refresh state) into a single lifecycle-aware `StateFlow<InventoryUiState>` using `SharingStarted.WhileSubscribed(5_000)`.
4. **Live Simulated Market Pricing Engine**:
   - Includes `SimulatedMarketFeedInterceptor` in `:core:network` so reviewers can pull-to-refresh or tap the sync button out-of-the-box without external API keys. Each sync executes the full **Retrofit2 -> OkHttp3 -> Kotlinx Serialization -> Room -> StateFlow** pipeline and simulates realistic market price movements.
5. **100% KSP & Modern Toolchain**:
   - Fully migrated from KAPT to **KSP2** for both **Room** and **Dagger-Hilt**.
   - Uses **Gradle Version Catalog (`libs.versions.toml`)** and `TYPESAFE_PROJECT_ACCESSORS`.

---

## Architecture & Modularization

```mermaid
flowchart TD
    App[":app (Application Orchestrator)"]
    FeatInv[":feature:inventory (UI + ViewModel + Domain)"]
    FeatScan[":feature:scanner (Vision Scanner UI + Domain)"]
    CoreDS[":core:designsystem (Material 3 Theme & Atoms)"]
    CoreDB[":core:database (Room + DAOs + Entity Mappers)"]
    CoreNet[":core:network (Retrofit2 + OkHttp3 + DTOs)"]
    CoreModel[":core:model (Pure Kotlin Domain Models)"]

    App --> FeatInv
    App --> FeatScan
    App --> CoreDS
    App --> CoreDB
    App --> CoreNet

    FeatInv --> CoreDS
    FeatInv --> CoreDB
    FeatInv --> CoreNet
    FeatInv --> CoreModel

    FeatScan --> CoreDS
    FeatScan --> CoreNet
    FeatScan --> CoreModel

    CoreDB --> CoreModel
    CoreNet --> CoreModel
```

### Unidirectional Data Flow (Offline-First Pipeline)

```mermaid
sequenceDiagram
    participant UI as InventoryScreen (Compose)
    participant VM as InventoryViewModel (StateFlow)
    participant UC as GetPortfolioSummaryUseCase
    participant Repo as CollectibleRepositoryImpl
    participant DAO as CollectibleDao (Room SSOT)
    participant API as MarketFeedApi (Retrofit/OkHttp)

    UI->>VM: collectAsStateWithLifecycle()
    VM->>UC: invoke()
    UC->>Repo: getCollectiblesStream()
    Repo->>DAO: getAll() : Flow<List<CollectibleEntity>>
    DAO-->>UI: Emits InventoryUiState.Success(summary, items)

    Note over UI,API: User Triggers Pull-to-Refresh / Sync
    UI->>VM: onRefresh()
    VM->>Repo: syncMarketPrices()
    Repo->>API: GET /v1/market/prices
    API-->>Repo: List<MarketPriceUpdateDto>
    Repo->>DAO: updatePrice(id, latestMarketPrice)
    DAO-->>UI: Room Flow auto-emits updated Valuation & P&L
```

---

## Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language & Concurrency** | Kotlin `2.1.20`, Coroutines & `StateFlow` |
| **UI Framework** | Jetpack Compose (BOM `2024.12.01`), Material 3, Pull-to-Refresh, Lifecycle-Aware Collection |
| **Dependency Injection** | Dagger-Hilt (`2.56.2`) via KSP |
| **Local Persistence** | Room (`2.7.1`) with exported JSON schemas & reactive `Flow` DAOs |
| **Networking & Serialization** | Retrofit `2.11.0`, OkHttp `4.12.0`, Kotlinx Serialization JSON |
| **Unit Testing** | JUnit 5 (Jupiter), MockK, CashApp Turbine, `kotlinx-coroutines-test` |
| **CI/CD & Code Quality** | GitHub Actions, Spotless + Ktlint, Android Lint, R8 Full Mode Minification |

---

## Getting Started & Running Tests

### 1. Run in Android Studio
1. Clone the repository and open the root folder in **Android Studio**.
2. Run the **`app`** configuration on any Android Emulator or physical device (`minSdk 26`).
3. Pull down on the portfolio list or tap the top-right **Sync** button to trigger live simulated market price updates.

### 2. Run JUnit 5 Unit Tests via CLI
```bash
./gradlew :core:model:test testDebugUnitTest
```

### 3. Run Code Style & Lint Checks
```bash
./gradlew spotlessCheck lintDebug
```

### 4. Assemble R8-Minified Release Bundle
```bash
./gradlew bundleRelease
```
