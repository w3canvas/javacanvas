# State Management Bug Analysis - TestCanvas2D

## Problem Description

TestCanvas2D test suite is disabled because tests interfere with each other, causing assertion failures despite attempts at proper test isolation.

## Root Causes Identified

### 1. **Thread-Local Context Confusion** (Primary Issue)

**Location:** `src/test/java/com/w3canvas/javacanvas/test/TestCanvas2D.java:42-54`

The Rhino `Context` is **thread-local**, but the test code mixes contexts across threads:

```java
@BeforeEach
public void setUp() {
    javaCanvas = new JavaCanvas(".", true);
    javaCanvas.initializeBackend();
    Context.enter();  // ← Called on JUnit test thread
    scope = javaCanvas.getRhinoRuntime().getScope();
}

@Test
public void testFillText() {
    interact(() -> {  // ← Runs on JavaFX Application Thread
        Context.enter();  // ← Called on JavaFX thread
        try {
            // ... test logic ...
        } finally {
            Context.exit();  // ← Exits on JavaFX thread
        }
    });
}

@AfterEach
public void tearDown() {
    Context.exit();  // ← Called on JUnit test thread
}
```

**Problem:**
- `setUp()` enters Context on the **JUnit test thread**
- Test methods enter/exit Context on the **JavaFX Application thread** (via `interact()`)
- `tearDown()` exits Context on the **JUnit test thread**

Since Context is thread-local:
- The JUnit thread's Context depth accumulates: 1, 2, 3... with each test
- The JavaFX thread's Context is properly balanced within each interact() call
- Eventually the JUnit thread has deep Context nesting, causing unpredictable behavior

### 2. **Shared Document State**

**Location:** `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/Document.java:20`

```java
private Map<String, Node> documentsNode = new HashMap<String, Node>();
```

The Document instance created in `setUp()` maintains a map of all created elements. If elements are added to the Document during tests but not properly removed, they could persist and affect subsequent tests.

### 3. **RhinoRuntime Scope Persistence**

**Location:** `src/main/java/com/w3canvas/javacanvas/rt/RhinoRuntime.java:16`

```java
private Scriptable scope;
```

The RhinoRuntime scope is created once and reused. While each test creates a new JavaCanvas and RhinoRuntime instance, properties defined in the scope could theoretically persist if not properly isolated.

## Solutions

### Fix 1: Remove Context.enter/exit from test thread (RECOMMENDED)

Since all canvas operations happen on the JavaFX thread via `interact()`, the test thread doesn't need a Context at all:

```java
@BeforeEach
public void setUp() {
    javaCanvas = new JavaCanvas(".", true);
    javaCanvas.initializeBackend();
    // DO NOT call Context.enter() here - it's not needed on test thread
    scope = javaCanvas.getRhinoRuntime().getScope();
}

@AfterEach
public void tearDown() {
    // DO NOT call Context.exit() here
}
```

Each test method already properly manages Context.enter/exit within its `interact()` blocks on the JavaFX thread.

### Fix 2: Ensure unique canvas instances per test

Make sure each test gets a completely fresh canvas:

```java
private HTMLCanvasElement createCanvas() {
    try {
        HTMLCanvasElement canvas = (HTMLCanvasElement) javaCanvas.getDocument().jsFunction_createElement("canvas");
        // Don't set an ID to avoid conflicts in Document's element map
        return canvas;
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
```

### Fix 3: Clear Document state if needed

If elements must be tracked by ID, clear the Document's element map between tests:

```java
@AfterEach
public void tearDown() {
    // Clear any registered elements
    if (javaCanvas != null && javaCanvas.getDocument() != null) {
        // Would need to add a clearAllElements() method to Document
    }
}
```

## Implementation

The primary fix (Fix 1) is the most important and should resolve the majority of test interference issues. The Context threading issue explains why tests that should be isolated are affecting each other.

## Testing

After applying Fix 1:
1. Remove `@Disabled` or `@DisabledIfSystemProperty` from TestCanvas2D
2. Run the full test suite: `mvn test`
3. Verify all 35 tests pass without interference

## Impact

- **Low Risk**: The fix simplifies the code by removing unnecessary Context management
- **High Impact**: Should resolve all test interference issues
- **Side Effects**: None expected - test thread never needs Rhino Context since all canvas operations are on JavaFX thread
