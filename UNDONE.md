# UNDONE - Incomplete Features & Technical Debt

This file tracks features that are incomplete, approximated, or need implementation work.

## Build & Platform Support Status

### ✅ Configured (Ready for Testing)
1. **Native Image CI Workflow**: GitHub Actions workflow configured in `.github/workflows/native-build.yml`
   - **Platforms**: Linux, macOS, Windows
   - **Build methods**: JBang, Maven (-Pnative), Gradle (nativeCompile -Pgraalvm)
   - **Status**: Workflow file exists but no evidence of successful runs in commit history
   - **Action needed**: Enable GitHub Actions on repository and verify builds pass

2. **JBang Support**: Cross-platform runner scripts configured
   - `JBangRunner.java` and `JBangAwtRunner.java` present with proper dependencies
   - **Status**: JBang installed successfully, but network-dependent dependency resolution not tested
   - **Requirements**: Network access to Maven Central for JavaFX platform artifacts

3. **Standard Builds**: Maven and Gradle configurations present
   - All 149 tests reportedly passing (per documentation)
   - **Status**: Build configs exist but untested in isolated environment (require network access)
   - Both AWT and JavaFX backends configured

### ✅ Partially Verified

1. **JDK 8 Source Compatibility**: **CONFIRMED**
   - **Tested**: JDK 8 (Zulu 8.0.472) installed and compilation tested
   - **Results**: 25 classes compiled successfully (24 interfaces + 1 core class)
   - **Language Features**: No Java 9+ features detected (no var, streams, lambdas, default methods)
   - **Encoding**: UTF-8 required for Greek characters in comments
   - **Blocker**: Full compilation requires Rhino (org.mozilla:rhino:1.7.14) and network access
   - **See**: `JDK8_TEST_REPORT.md` for detailed findings

2. **JDK 8 Build Configuration**: **PROPERLY CONFIGURED**
   - Gradle `-Plegacy` flag targets Java 8
   - Excludes JavaFX and GraalJS backends (require Java 11+)
   - **Not tested**: Cannot verify full build without dependency resolution
   - **Recommendation**: Requires network-connected CI environment for full verification

### ❌ Not Available

1. **JDK 7 Support**:
   - **Status**: Not available via SDKMAN (EOL since April 2015)
   - **Recommendation**: Remove any JDK 7 claims from documentation

### 🚧 Network-Dependent (Cannot Test in Isolation)

**All builds require network access for:**
- Maven builds (Maven Central for dependencies)
  - Failed on: `kr.motd.maven:os-maven-plugin:1.7.0`
- Gradle builds (Gradle Plugin Portal + Maven Central)
  - Failed on: `org.gradle.toolchains.foojay-resolver-convention:0.8.0`
  - Failed on: `org.openjfx.javafxplugin:0.1.0`
- JBang dependency resolution (JavaFX platform artifacts, Rhino, GraalJS)
  - Failed on: `org.openjfx:javafx-controls:jar:linux:21.0.8`
- GraalVM native image compilation (requires full dependency tree)

**Critical External Dependencies:**
- Rhino JavaScript engine (org.mozilla:rhino:1.7.14) - required by core classes
- JavaFX libraries - platform-specific artifacts
- GraalJS polyglot libraries - for modern JavaScript support

## Dependency Architecture Issues

**Circular Dependency Chain:**
```
Interfaces → Core Classes → Rhino
     ↑            ↓
AWT Backend ← CSS Parser → Rhino Backend
```

**Impact:**
- Cannot compile core classes without Rhino dependency
- Cannot compile AWT backend without core classes
- Isolated class-by-class compilation only works for interfaces
- Full project requires complete dependency resolution

**Recommendation:** Consider extracting pure-Java interfaces into separate module without external dependencies.
