# UNDONE - Incomplete Features & Technical Debt

This file tracks features that are incomplete, approximated, or need implementation work.

## Legacy JDK Support (Medium Priority)

### Problem
The codebase targets JDK 17+ but has a `-Plegacy` build mode for JDK 8. Some architectural cleanups remain.

### Deferred / Limitations
1. **JDK 8 Compilation**: Actual compilation on JDK 8 is not verified in CI (requires JDK 8 installation).
   - **Mitigation**: Gradle `-Plegacy` flag enforces language level 8.
2. **Native AOT**: `native-image` build is configured but not tested.
   - **Reason**: `native-image` tool is not available in the current environment.
   - **Status**: `reflect-config.json` has been updated with Rhino and JavaCanvas configuration.
