# NativeModMenu

### High-Performance In-Memory Dex Loader for Android

`NativeModMenu` is a sophisticated reimplementation of the LGL Android Mod Menu. It leverages `InMemoryDexClassLoader` to load the Java UI layer directly from system memory, eliminating the need for physical `.dex` files on the device storage.

---

## Technical Overview

This implementation focuses on stealth and efficiency by embedding the `FloatingModMenu.dex` as a HEX-encoded string within the native binary. At runtime, the JNI layer performs a dynamic bootstrap to initialize the menu without leaving any traces in the application's assets or data folders.

### Key Architectural Differences

* **Zero Disk Footprint:** No external dex file is shipped; the menu exists only in memory.
* **Volatile Loading:** Utilizes `dalvik.system.InMemoryDexClassLoader` for runtime execution.
* **Native-Led Initialization:** The entire menu lifecycle is managed via JNI native bindings.
* **Reduced Attack Surface:** Eliminates file-based detection vectors by avoiding asset extraction.
* **Native Compatibility:** Fully compatible with existing LGL Java menu logic.

---

## Core Logic Flow

1. **Data Embedding:** The `FloatingModMenu.dex` is converted to a HEX string and compiled into the C++ source.
2. **Buffer Reconstruction:** At runtime, the HEX string is decoded into a `ByteBuffer`.
3. **Class Loading:** The `InMemoryDexClassLoader` interprets the buffer and loads the `uk.lgl.modmenu.FloatingModMenu` class.
4. **JNI Registration:** Native functions are dynamically bound to the Java class methods.
5. **Execution:** The `binJava()` entry point triggers `FloatingModMenu.antik(Context)` to render the UI.

---

## JNI Native Interface

The following methods are registered dynamically to bridge the Native and Java layers:

| Method | Functionality |
| --- | --- |
| `Icon()` | Returns the primary menu icon |
| `IconWebViewData()` | Manages WebView icon resources |
| `getFeatureList()` | Retrieves the feature configuration array |
| `settingsList()` | Retrieves the menu settings configuration |
| `Changes(...)` | Main event handler for feature toggles |
| `setTitleText(...)` | Customizes the UI title appearance |
| `setHeadingText(...)` | Customizes the UI heading appearance |

---

## Implementation Details

### Requirements

* **Android Version:** 8.0 (API 26) or higher
* **Architecture:** ARMv7, ARM64-v8a
* **Environment:** Native JNI-based injection

### Entry Point

```cpp
// Executed post JNI_OnLoad
void binJava(); 

```

The `binJava` function is responsible for retrieving the `Application` context via `ActivityThread` and initiating the memory-loading sequence.

---

## Legal Notice

This project is developed strictly for **educational and research purposes**. Modifying the runtime behavior of third-party applications may violate their Terms of Service. The developers assume no liability for misuse.

## Acknowledgments

* **Original Architecture:** LGL Android Mod Menu
* **Lead Developer:** AntikMods
* **Memory Loading Logic:** NepMods
