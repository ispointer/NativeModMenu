# NativeModMenu
A **custom reimplementation of the LGL Android Mod Menu** that loads the Java UI **entirely from memory** using `InMemoryDexClassLoader`, without shipping a separate `.dex` file on disk.

This version embeds a prebuilt `FloatingModMenu.dex` as a **HEX string** and dynamically loads it at runtime via JNI.

---

## ✨ Key Differences from Original LGL Mod Menu

- **No external dex file**
    - The menu dex is embedded as HEX and loaded directly from memory.
- **In-memory class loading**
    - Uses `dalvik.system.InMemoryDexClassLoader`
- **JNI-driven bootstrap**
    - Menu initialization is triggered fully from native code
- **Cleaner injection surface**
    - No file writes, no asset extraction
- **Compatible with existing LGL menu Java code**

---

## 🧠 How It Works (High-Level)

1. **Dex embedding**
    - `FloatingModMenu.dex` is converted to a HEX string and stored in native code.
2. **HEX → ByteBuffer**
    - HEX string is converted back into bytes at runtime.
3. **InMemoryDexClassLoader**
    - Dex is loaded directly from memory.
4. **JNI native bindings**
    - Native functions are registered to the Java menu class.
5. **Menu startup**
    - Calls `FloatingModMenu.antik(Context)` to launch the floating menu.

---

## 📂 Important Components

### Embedded Dex
```cpp
static std::string DI = "HEX_CODE_OF_DEX";
```

- This must be the **HEX-encoded** version of `FloatingModMenu.dex`.

---

### JNI Native Interface

Registered native methods:

| Method | Description |
|------|------------|
| `Icon()` | Menu icon |
| `IconWebViewData()` | WebView icon data |
| `getFeatureList()` | Feature list |
| `settingsList()` | Settings list |
| `Changes(...)` | Feature toggle handler |
| `setTitleText(TextView)` | Title customization |
| `setHeadingText(TextView)` | Heading customization |

---

### Dex Loading Logic

Uses:
```java
InMemoryDexClassLoader(ByteBuffer[] dex, ClassLoader parent)
```

Loads:
```java
uk.lgl.modmenu.FloatingModMenu
```

---

## 🚀 Entry Point

```cpp
void binJava();
```

- Called after `JNI_OnLoad`
- Retrieves `Application` context via `ActivityThread`
- Starts menu initialization

---

## 🔧 Requirements

- Android 8.0+ (API 26+)
- ARMv7 / ARM64
- JNI-based injection environment
- Prebuilt compatible `FloatingModMenu.dex`

---

## ⚠️ Notes

- This project modifies runtime behavior of apps.
- Usage may violate application Terms of Service.
- Intended for educational and research purposes.

---

## 📜 Credits

- Original concept: LGL Android Mod Menu
- Project Creation: Aniket
- In-memory dex loader: NepMods

