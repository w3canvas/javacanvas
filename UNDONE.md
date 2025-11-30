# UNDONE - Incomplete Features & Technical Debt

This file tracks features that are incomplete, approximated, or need implementation work.

## Build & Platform Support Status

### ✅ Verified Working
1. **Native Image Compilation**: Fully supported and tested via GitHub Actions CI on:
   - Linux (ubuntu-latest)
   - macOS (macos-latest)
   - Windows (windows-latest)
   - Build methods: JBang (`jbang export native`), Maven (`-Pnative`), Gradle (`nativeCompile -Pgraalvm`)

2. **JBang Support**: Cross-platform support verified on all major platforms via CI
   - Linux, macOS, and Windows all tested in CI workflow
   - Installation via SDKMAN confirmed working

3. **Standard Builds**: Maven and Gradle builds fully configured and working
   - All 149 tests passing (100%)
   - Both AWT and JavaFX backends functional

### ⚠️ Not Fully Verified
1. **JDK 8 Legacy Build**: Gradle `-Plegacy` flag configured but not tested in CI
   - **Status**: Configuration exists but requires JDK 8 environment to verify
   - **Mitigation**: Language level 8 enforced via Gradle, but actual compilation untested
