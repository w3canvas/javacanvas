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

### ⚠️ Verified with Limitations
1. **JDK 8 Compatibility**:
   - **Verified**: JDK 8 (Zulu 8.0.472) installed and can compile basic Java code
   - **Not verified**: Full project compilation with `-Plegacy` flag (requires dependency resolution)
   - **Status**: Source compatibility with Java 8 language level configured via Gradle

2. **JDK 7 Support**:
   - **Status**: Not available via SDKMAN (EOL, no longer distributed)
   - **Recommendation**: Remove JDK 7 claims from documentation if present

### 🚧 Network-Dependent (Cannot Test in Isolation)
- Maven builds (require Maven Central access for dependencies)
- Gradle builds (require Gradle Plugin Portal and Maven Central)
- JBang dependency resolution (requires Maven Central for JavaFX artifacts)
- GraalVM native image compilation (requires network for dependency download)
