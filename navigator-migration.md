# Migration Guide: Legacy Navigator to Compose Navigators

This guide covers the transition from the legacy Fragment-based **`readium/navigator`** to the modern Compose-based **`readium/navigators`**.

---

## 1. Dependency Transition
The monolithic `:readium:navigator` is now split into specialized modules. Update your `build.gradle.kts` and `libs.versions.toml`.

### Remove:
- `implementation(project(":readium:navigator"))`

### Add (Based on need):
- **Core API:** `implementation(project(":readium:navigators:common"))`
- **EPUB/Web:** `implementation(project(":readium:navigators:web:reflowable"))`
- **Fixed-Layout:** `implementation(project(":readium:navigators:web:fixedlayout"))`
- **Audio/TTS:** `implementation(project(":readium:navigators:media:audio"))`

---

## 2. Architecture: Fragment → Composable
The primary change is moving from a `Fragment` that manages its own view to a **Stateless Composable** driven by a **State Object**.

Additionally, the monolithic legacy navigator handled both fixed-layout and reflowable EPUBs. In the new architecture, these are separated into specialized modules (`web:fixedlayout` and `web:reflowable`). 

### Initialization Flow
Instead of passing configuration to a `FragmentFactory`, you will now use a `RenditionFactory` to create the `RenditionState`. You must inspect the `Publication` metadata to determine which layout engine to use:

```kotlin
val isFixedLayout = publication.metadata.layout == Layout.FIXED

val renditionState = if (isFixedLayout) {
    FixedWebRenditionFactory(application, publication, fixedConfig)
        ?.createRenditionState(initialSettings, initialLocation)
} else {
    ReflowableWebRenditionFactory(application, publication, reflowableConfig)
        ?.createRenditionState(initialSettings, initialLocation)
}
```

Then pass the state to the corresponding composable:

```kotlin
if (isFixedLayout) {
    FixedWebRendition(state = renditionState)
} else {
    ReflowableWebRendition(state = renditionState)
}
```

---

## 3. Configuration Setup
The legacy `EpubNavigatorFragment.Configuration` object has been replaced by specialized classes like `ReflowableWebConfiguration` and `FixedWebConfiguration`.

- **Assets:** `servedAssets` now typically requires a `persistentListOf` rather than a standard `List`.
- **Decorations:** The mutable `decorationTemplates` map is replaced by the `WebDecorationTemplates { set(...) }` builder.
- **Fonts:** Custom fonts are declared via `fontFamilyDeclarations = FontFamilyDeclarations { ... }`.

```kotlin
// Legacy
configuration = EpubNavigatorFragment.Configuration {
    servedAssets = listOf("fonts/.*")
    decorationTemplates[DecorationStyleAnnotationMark::class] = annotationMarkTemplate()
    addFontFamilyDeclaration(FontFamily.LITERATA) { ... }
}

// New
val reflowableConfig = ReflowableWebConfiguration(
    servedAssets = persistentListOf("fonts/.*"),
    decorationTemplates = WebDecorationTemplates {
        set(DecorationStyleAnnotationMark::class, annotationMarkTemplate())
    },
    fontFamilyDeclarations = FontFamilyDeclarations {
        addFontFamilyDeclaration(FontFamily.LITERATA) { ... }
    }
)
```

---

## 4. Controlling the Navigator
Interaction (navigation, settings, decorations) is no longer done via a single `Navigator` object. Instead, use **Controllers** provided by the `RenditionState`.

| Task            | Legacy Navigator                | New Controller Interface                 |
|:----------------|:--------------------------------|:-----------------------------------------|
| **Navigation**  | `navigator.go()`                | `NavigationController.goTo()`            |
| **Settings**    | `navigator.submitPreferences()` | `SettingsController.settings`            |
| **Decorations** | `navigator.applyDecorations()`  | `DecorationController.decorations`       |
| **Selection**   | `navigator.currentSelection()`  | `SelectionController.currentSelection()` |

### Example: Navigation
```kotlin
// Legacy
navigator.go(locator, animated = true)

// New
val controller = renditionState.controller
coroutineScope.launch {
    controller?.goTo(location)
}
```

---

## 5. Handling Events (Listeners)
The monolithic `Navigator.Listener` has been decomposed into specialized functional listeners.

### Legacy (Combined)
```kotlin
class MyListener : EpubNavigatorFragment.Listener {
    override fun onTap(point: PointF): Boolean { }
    override fun onExternalLinkActivated(url: AbsoluteUrl) { }
}
```

### New (Specialized)
```kotlin
// Define specific listeners
val inputListener = object : InputListener {
    override fun onTap(event: TapEvent, context: TapContext) { }
}

val hyperlinkListener = defaultHyperlinkListener(
    controller = renditionState.controller,
    onExternalLinkActivated = { url, _ -> /* handle */ }
)

// Pass to the Composable
ReflowableWebRendition(
    state = renditionState,
    inputListener = inputListener,
    hyperlinkListener = hyperlinkListener
)
```

---

## 6. Preferences & Serialization
Both EPUB and TTS preferences are now `kotlinx.serialization` compatible.

### EPUB Preferences
The legacy `EpubPreferences` is replaced by `ReflowableWebPreferences` (for reflowable) and `FixedWebPreferences` (for fixed-layout).

- **Legacy:** `org.readium.r2.navigator.epub.EpubPreferences`
- **New:** `org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences`

#### Serialization
Unlike the legacy navigator, the new Compose-based navigators do not provide built-in `PreferencesSerializer` implementations for Web preferences. However, since the preference classes are annotated with `@Serializable`, you can use `kotlinx.serialization` directly:

```kotlin
val json = Json { ignoreUnknownKeys = true }

// Serialize
val serialized = json.encodeToString(ReflowableWebPreferences.serializer(), preferences)

// Deserialize
val deserialized = json.decodeFromString(ReflowableWebPreferences.serializer(), serialized)
```

#### Mapping Changes
- **Themes:** `ReflowableWebPreferences` removes the explicit `theme` property in favor of direct color control (`backgroundColor`, `textColor`, etc.). Use the companion constants like `ReflowableWebPreferences.DarkTheme` as a base.
- **Publisher Styles:** `publisherStyles` is renamed to `overridePublisherColors` (with inverted logic in some contexts—check documentation).
- **Margins:** `pageMargins` is now `minMargins`.

### TTS Preferences
TTS preferences have also moved. While an `AndroidTtsPreferencesSerializer` is provided for convenience, you can also use `kotlinx.serialization` directly.

- **New:** `org.readium.navigator.media.tts.android.AndroidTtsPreferences`

#### Serialization
```kotlin
// Using the provided serializer
val serialized = AndroidTtsPreferencesSerializer().serialize(ttsPreferences)

// Or using kotlinx.serialization directly
val serialized = Json.encodeToString(AndroidTtsPreferences.serializer(), ttsPreferences)
```

---

## 7. Decorations & Highlights
Decorations are now managed reactively through a Map in the `DecorationController`.

1.  **Define Location:** Convert `Locator` to a specialized location (e.g., `ReflowableWebDecorationLocation`).
2.  **Submit State:** Update the `decorations` Map.

```kotlin
// New Decoration approach
val newDecorations = persistentListOf(
    Decoration(
        id = Decoration.Id("highlight-1"),
        location = ReflowableWebDecorationLocation(locator),
        style = Decoration.Style.Highlight(tint = Color.Yellow)
    )
)

// Update the controller
renditionState.controller?.let { controller ->
    controller.decorations = controller.decorations.put("highlights", newDecorations)
}
```

---

## 8. Checklist for AI Agents
When migrating a file, ensure the following:
- [ ] Inspect `publication.metadata.layout` to use the correct `FixedWeb` or `ReflowableWeb` factory and composable.
- [ ] Migrate `EpubNavigatorFragment.Configuration` to `ReflowableWebConfiguration` or `FixedWebConfiguration`.
- [ ] Change package imports from `org.readium.r2.navigator.*` to `org.readium.navigator.*`.
- [ ] Replace `Locator` with `GoLocation` or specialized Location classes for navigation calls.
- [ ] Move `FragmentFactory` logic into a `RenditionFactory` to create the `RenditionState`.
- [ ] Wrap navigation calls in a `CoroutineScope` (new APIs are mostly `suspend`).
- [ ] Map legacy `EpubPreferences` to the new `Settings` objects specific to the navigator type.
- [ ] Replace `onTap` coordinate logic with the new `TapEvent` which uses `DpOffset`.
- [ ] Ensure `Application` context is passed to the factories and states.
