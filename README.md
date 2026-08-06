# Readium Test App - Technical Overview

This application is a modern Android implementation of the **Readium Toolkit (3.1.2)**, built with a
focus on **Jetpack Compose**, **Material 3 Expressive** design, and the latest **Navigation 3**
architectural standards.

## Architecture & Modularization

The project utilizes a "Plug-in Architecture" powered by **Hilt Multibindings** to achieve high
levels of decoupling between the central app orchestrator and individual features.

### The Navigation Contract (`navigation.api`)

To ensure features do not depend on each other's internal UI implementations, all navigation
contracts are centralized in the `navigation.api` package:

* **`Route`**: A sealed interface extending `NavKey` that defines the serializable destinations for
  the app (e.g., `Bookshelf`, `Catalogs`, `Reader`).
* **`NavEntryBuilder`**: A typealias for `EntryProviderScope<NavKey>.() -> Unit`. This represents a
  function that can register a route and its corresponding Composable content within the Navigation
  3 DSL.

### Feature Modularization

Each feature (Bookshelf, Reader, Catalogs, About) is organized into a consistent package structure:

* **`ui/`**: Contains the screen implementations and Compose UI logic.
* **`di/`**: Contains a Hilt module (e.g., `CatalogsNavModule`) that contributes the feature's
  entries to the global navigation set.

Example of a feature contributing its navigation:

```kotlin
@Module
@InstallIn(ActivityRetainedComponent::class)
object CatalogsNavModule {
    @Provides
    @IntoSet
    fun provideCatalogsEntry(): NavEntryBuilder = {
        entry<Catalogs> { CatalogFeedScreen() }
    }
}
```

### Orchestration (ReadiumApp)

The ReadiumApp composable acts as the "App Shell". It is completely ignorant of specific feature
screens. Instead:

1. It injects a Set<NavEntryBuilder> provided by Hilt.
2. It iterates through these builders inside the entryProvider to dynamically assemble the app's
   navigation graph.
3. It manages global UI elements like the NavigationSuiteScaffold (for adaptive Rail/Bottom Bar
   transitions) and the CenterAlignedTopAppBar.

## Tech Stack & Components

* **`Readium 3.1.2`**: Core toolkit for publication parsing and rendering.
* **`Navigation 3`**: Stable navigation component utilizing a state-hoisted Navigator and
  NavigationState.
* **`Material 3 Expressive`**: Utilization of the latest M3 components and adaptive layouts.
* **`Room Database`**: Local persistence for books, bookmarks, highlights, and OPDS catalogs.
* **`Hilt`**: Dependency injection for ViewModels and the modular navigation system.

## Project Structure

```
├── app/                  # MainActivity and global ReadiumApp orchestrator
├── core/                 # Shared data, domain models, and common UI components
├── features/             # Feature-specific logic (Reader, Bookshelf, Catalogs)
│   └── <feature>/
│       ├── ui/           # Screens and UI components
│       └── di/           # Navigation modules and Hilt providers
├── navigation/           # Navigation 3 engine and global API
│   └── api/              # Shared Routes and NavEntryBuilder contract
└── readium/              # Readium Toolkit integration logic (Importers, Repositories)
```

## Current Capabilities

* **`Adaptive Layouts`**: Automatic switching between Bottom Navigation for phones and Navigation
  Rails for tablets via NavigationSuiteScaffold.
* **`Book Management`**: Import publications from device storage or URLs into a local Room-backed
  library.
* **`OPDS Support`**: Initial framework for browsing and managing OPDS catalog feeds.
* **`Reader Framework`**: Decoupled reader implementation ready for EPUB/PDF integration.
