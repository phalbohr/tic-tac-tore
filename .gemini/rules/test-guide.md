# AI Test Conventions Guide

Compressed rules for AI reasoning. Interpret strictly.

## 1. Testing Approach

Test through the PUBLIC contract ONLY. Do NOT use reflection to access private fields/methods unless absolutely impossible otherwise (last resort).

## 2. Naming Tests (@DisplayName)

- **Class:** `@DisplayName("[ClassName] Tests")`
- **Method:** `@DisplayName("[Condition] - should [expected behavior]")`

## 3. Zero Comments Policy

Tests MUST be self-documenting. Absolutely NO comments inside test methods (no "what/why", no TODOs). Extract complex setups into private helper methods instead of explaining them with text.

## 4. Test Redundancy & Coverage

Minimize duplicate tests without losing coverage metrics (lines, branches, statements).

- Merge duplicate scenarios using parameterized tests (`@ParameterizedTest`).
- Remove tests that provide ZERO unique coverage EXCEPT if they document a specific bug regression or compliance requirement.

## 5. Splitting Large Test Classes

If a test class exceeds 500 lines, SPLIT IT by public method being tested:

- Create directory `[classname]/` (lowercase).
- Create classes named `[ClassName][MethodName]Test.java`.

## 6. Refactoring for Testability

If production code is hard to test (e.g. SRP violations, deep nesting, lack of clear entry points), PROPOSE REFACTORING to the user before attempting to write complex, brittle tests to work around the bad design.

## 7. Integration Test Naming

Integration tests MUST end with `IT` or `IntegrationTest` (e.g., `MassStockControllerIT`). Do not use `Test` suffix for integration tests, so Gradle configuration properly separates them from unit tests.
