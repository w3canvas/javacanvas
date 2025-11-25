# JavaCanvas JDK Version Support Analysis

This document outlines the JVM versions this project can support, detailing the differences between using a modern JDK versus a legacy JDK.

### 1. Modern JDK Support (JDK 17+)

The project is currently configured to use modern Java technologies and, as such, requires a recent JDK.

- **Minimum Requirement:** **JDK 17** is the effective minimum version.
- **Primary Build File:** The `build.gradle` file explicitly sets a `JavaLanguageVersion` of 17 and uses the GraalVM toolchain.
- **Key Dependencies Requiring JDK 17+:**
    - **GraalJS (v24.1.0):** The modern JavaScript engine used for high-performance scripting requires JDK 17.
    - **JavaFX (v17+):** The JavaFX modules are built for JDK 17 and later.
- **Supported JVMs:** When using a modern JDK (17, 21, or newer), the project can run on any compliant JVM, including:
    - OpenJDK
    - Oracle JDK
    - GraalVM (recommended for best performance with GraalJS)
- **Features on Modern JDKs:**
    - **Full Functionality:** All features are available.
    - **Dual Rendering Backends:** Both AWT (standard) and JavaFX (modern) backends are supported.
    - **Dual JavaScript Engines:** Both Rhino (legacy) and GraalJS (modern) engines are supported.
    - **Native Image:** The project can be compiled into a native executable using GraalVM, providing significant performance benefits.

### 2. Legacy JDK Support (JDK 8)

Supporting older Java versions like JDK 8 is feasible but results in a **feature-limited version** of the library. It requires a separate, dedicated build configuration.

- **Minimum Requirement:** **JDK 8** can be supported with modifications. JDK 7 or older is not recommended due to the age of compatible dependencies.
- **Path to Legacy Support:** To run on JDK 8, the project must be compiled and run in a "legacy mode" that **removes modern dependencies**.
- **Changes Required for Legacy Build:**
    1.  **Create a `legacy` Build Profile:** A separate build profile (e.g., a Maven profile or a dedicated Gradle script) must be created to manage legacy dependencies and settings.
    2.  **Set JDK Target:** The compiler source and target must be set to `1.8`.
    3.  **Exclude Modern Dependencies:**
        - **JavaFX:** The `openjfx` dependencies must be completely removed.
        - **GraalJS:** The `org.graalvm.polyglot` dependencies must be completely removed.
    4.  **Rely on Legacy Components:**
        - **Rendering Backend:** The application must be configured to use the **AWT backend exclusively**.
        - **JavaScript Engine:** The application must use the **Rhino engine (v1.7.14)**, which is compatible with JDK 8.

- **Limitations of Legacy Mode (JDK 8):**
    - **AWT Only:** No access to the JavaFX backend, meaning any JavaFX-specific rendering improvements are unavailable.
    - **Rhino Only:** Slower JavaScript performance and limited support for modern ECMAScript standards compared to GraalJS.
    - **No Native Image:** GraalVM Native Image compilation is not available.
    - **Core Code Compatibility:** The core library (`com.w3canvas.javacanvas.core`) appears to be compatible with Java 8, as it avoids modern language features. However, thorough testing would be required to guarantee stability.

### Summary Table

| Feature                 | Modern JDK (17+)                               | Legacy JDK (8)                                      |
| ----------------------- | ---------------------------------------------- | --------------------------------------------------- |
| **Minimum Version**     | JDK 17                                         | JDK 8                                               |
| **Rendering Backends**  | AWT, JavaFX                                    | AWT only                                            |
| **JavaScript Engines**  | GraalJS, Rhino                                 | Rhino only                                          |
| **Native Image**        | Supported (with GraalVM)                       | Not supported                                       |
| **Build Configuration** | Standard `build.gradle`                        | Requires a separate `legacy` profile (e.g., in `pom.xml`) |
| **Performance**         | High (especially with GraalVM)                 | Lower (due to Rhino JS engine)                      |

In conclusion, while the project is designed for modern Java environments, a viable path for legacy support exists by carefully managing build configurations and dependencies to create a feature-limited but functional version for older systems.
