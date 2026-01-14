# Testing Guide

This document contains the main rules and recommendations for writing tests in the project. Following these rules helps maintain the cleanliness, readability, and consistency of the test codebase.

## 1. Testing Approach

### Test Through the Public Contract

#### Core Principle:

When writing tests, always prefer testing through the component's public contract.
Your tests should interact with the code in the same way a real consumer would—through public methods, classes, and interfaces.

#### Use of Reflection:

1.  **Avoid by Default:**

    Do not use reflection (accessing private fields, methods, or constructors) as your primary testing strategy.
    Tests that rely on internal implementation details are brittle and often break during refactoring,
    even if the public behavior of the code has not changed.

2.  **Use as a Last Resort:**

    Resort to reflection only in exceptional cases where it is impossible to verify critical behavior through the public API.

3.  **Justify Its Use:**

    If you must use reflection, provide a clear justification in a comment within the test.
    Explain why other approaches were not feasible and what specific behavior this test verifies that could not be checked otherwise.

#### Example Workflow:

1.  **Attempt #1 (Preferred):** Write the test using only the public methods and properties of the class under test.
2.  **Attempt #2:** If Attempt #1 is not successful, consider whether the code's design can be modified (if possible)
    to make it more testable without reflection.
3.  **Attempt #3 (Last Resort):** If the previous steps are not possible, use reflection and add a comment providing justification.

## 2. Test Structure: Given-When-Then

Each test method should be clearly divided into three logical sections using comments:

-   `// Given`: This section is for preparing all necessary data, mocks, and initial conditions for the test.
-   `// When`: Here, the action under test is performed, or the method is called.
-   `// Then`: In this section, the results of the execution are checked, and assertions are made that the system's behavior meets expectations.

## 3. Naming Tests with @DisplayName

To improve the readability and understanding of tests, use the @DisplayName annotation from JUnit 5.

1. **For test classes:** The annotation should briefly describe the component being tested.

- Format: @DisplayName("[ClassName] Tests")
- Example: @DisplayName("PhysicalMassStockController Tests")

2. **For test methods:** The annotation should describe the condition and the expected behavior.

- Format: @DisplayName("[Condition] - should [expected behavior]")
- Example: @DisplayName("execute should delegate to service and log the transaction")

## 4. Self-Documenting Tests: Avoid Explanatory Comments

Tests should be self-documenting. The combination of a descriptive `@DisplayName`, 
well-named variables, and a clear `Given-When-Then` structure should make the test's intent obvious without needing extra comments.

- **Strive for Clarity:** Write code that is easy to read and understand on its own. 
If a piece of logic is complex, consider extracting it into a private helper method with a descriptive name.

- **Avoid "What" Comments:** Do not add comments that explain *what* a line of code is doing. 
If you feel the need to add such a comment, it's often a sign that the code should be refactored for clarity.

- **Structural Comments are the Exception:** The `// Given`, `// When`, `// Then` comments are an exception to this rule. 
They are structural markers that improve readability and are a required part of our testing style.

## 5: Check For Redundancy

### Objective
Identify and reduce test redundancy within a test file by merging or deleting redundant tests without decreasing coverage metrics for lines, branches, and expressions/statements.

### Requirement
If there are redundant checks that can be merged or removed without lowering coverage for lines, branches, and expressions (statements), evaluate and perform such optimization to reduce test duplication without losing coverage metrics.

### Redundancy Criteria

A test is considered redundant if it exhibits one or more of the following characteristics:

#### 1. Duplicate Scenarios
- Multiple tests check identical behavior with insignificant differences in input data
- Tests cover the same code paths as other existing tests

#### 2. Overlapping Assertions
- Sets of assertions in different tests are completely or almost completely identical
- Tests perform the same validations as other tests

#### 3. Repetitive Fixtures/Setup
- Identical environment preparation with identical expected results
- Same setup/teardown procedures for functionally equivalent scenarios

#### 4. Dead Coverage Branches
- Tests that don't add new covered lines, branches, or expressions compared to existing tests
- Zero unique coverage contribution when removed

### Agent Actions

#### 1. Collect Coverage Metrics
- [ ] Generate comprehensive coverage report for the current test suite
- [ ] Compare each test's contribution to coverage by lines, branches, and expressions
- [ ] Map each test to its specific coverage areas

#### 2. Identify Duplicate Clusters
- [ ] Group tests with matching coverage and similar logic
- [ ] Group tests by similar:
  - Code paths covered
  - Input parameter patterns
  - Expected outcomes
  - Setup/teardown procedures
- [ ] Flag tests that provide zero unique coverage

#### 3. Evaluate Merging Opportunities
- [ ] Check if test cases can be combined into one parameterized test or more general scenario
- [ ] Verify that merged test maintains same assertion coverage
- [ ] Ensure merged test preserves failure diagnostics quality

#### 4. Propose Deletions
- [ ] Mark tests that don't add new coverage for removal
- [ ] Confirm removing test doesn't decrease coverage metrics
- [ ] Verify no unique edge cases are lost

#### 5. Preserve Intent
- [ ] If a test duplicates coverage but carries documentation/regression value, keep it and mark as "documenting" with comment
- [ ] Check if test documents critical business logic or bug regression
- [ ] Verify compliance/regulatory requirements

#### 6. Re-run Coverage Analysis
- [ ] Confirm that coverage metrics haven't decreased after optimizations
- [ ] Validate that all optimization goals are met

#### 7. Document Changes
- [ ] Describe what was merged/deleted and attach diffs
- [ ] Update test documentation/comments
- [ ] Provide comprehensive change summary

### Implementation Practices

#### Parameterization
- Combine similar tests through parameterization instead of copying
- Create parameterized tests where applicable

#### Factor Common Parts
- Extract common preparation into fixtures/helpers
- Reduce code duplication in test setup

#### Minimal Assertion Sets
- Keep only necessary assertions for unique branches/paths
- Avoid redundant validation statements

#### Regression Protection
- Preserve tests covering critical bugs, even with coverage duplication
- Maintain tests required by compliance/regulatory standards

### Preservation Rules

**DO NOT REMOVE** tests that:
- Document specific bug fixes (even if coverage overlaps)
- Test critical business logic edge cases
- Provide better failure diagnostics
- Are required by compliance/regulatory standards
- Carry significant documentation or regression value

### Success Criteria

#### Coverage Preservation
- [ ] Coverage by lines, branches, and expressions hasn't decreased
- [ ] Coverage metrics remain unchanged or improved

#### Performance Improvement
- [ ] Number of tests and execution time decreased or remained the same
- [ ] Test execution time reduced or maintained

#### Quality Maintenance
- [ ] Test readability and diagnostic value preserved
- [ ] Test suite maintainability improved
- [ ] No loss of diagnostic information quality

### Output Format

```markdown
## Redundancy Analysis Results

### Tests Merged:
- `test_user_validation_empty()` + `test_user_validation_null()` → `test_user_validation(invalid_input)`
- `test_calculation_positive()` + `test_calculation_negative()` → `test_calculation(test_value, expected)`
- Reason: Identical coverage patterns, different input parameters

### Tests Removed:
- `test_duplicate_login_flow()`
- `test_redundant_error_handling()`
- Reason: 100% coverage overlap with existing tests

### Tests Preserved (Documentation Value):
- `test_critical_security_bug_regression()` - marked as regression test
- `test_compliance_requirement_xyz()` - required by regulatory standards

### Coverage Impact:
- Lines: 95.2% → 95.2% (no change)
- Branches: 88.1% → 88.1% (no change) 
- Statements: 92.4% → 92.4% (no change)

### Performance Impact:
- Test count: 156 → 142 (-14 tests)
- Execution time: 45.2s → 41.8s (-7.5% improvement)
- Maintainability: Improved through parameterization and fixture consolidation
```

## 6. Test Class Structure

### Rule: Split Large Test Classes by Public Method

To improve organization and readability, any test class exceeding 500 lines of code should be split into separate classes for each public method.

#### Core Principle:
-   **When to Split:** If a test class (e.g., `SomeClassTest`) exceeds 500 lines, it should be split.
-   **How to Split:** Create a separate test class for each `public` method of the class under test.
-   **Naming:** The new test class should be named by combining the name of the class under test and the method name.
-   **Organization:** Place all split test classes into a directory named after the original class (in lowercase).
-   **Exception:** If a test for a single public method (e.g., `SomeClassDoSomethingTest`) itself exceeds 500 lines, no further splitting is required.

#### Naming Convention:
-   **Directory:** `[classname]/`
-   **Test Class:** `[ClassName][MethodName]Test`
-   **Example:** If `WarehouseServiceTest` becomes too large, tests for the `createWarehouse` method are moved to the `WarehouseServiceCreateWarehouseTest` class, which will be located in the `warehouseservice/` directory.

#### Rationale:
-   **Clarity:** It's immediately clear which part of the service is being tested.
-   **Maintainability:** Changes to a single public method only affect its corresponding test file, reducing the chance of merge conflicts and making refactoring easier.
-   **Focus:** Each test class has a single responsibility—testing one public entry point—making the tests easier to read, write, and understand.
-   **Organization:** Grouping tests in a dedicated folder makes it easier to run all tests related to a specific service from an IDE or build tool.
-   **Manageability:** The 500-line limit prevents test classes from growing to an unmanageable size.

#### Example Structure:

Given a `WarehouseService` class with several public methods. Initially, it has a single test class, `WarehouseServiceTest`.
If WarehouseServiceTest grows beyond 500 lines, it should be split. The new test structure would look like this:
```java
// WarehouseService.java
public class WarehouseService {
    public Warehouse createWarehouse(CreateWarehouseRequest request) { /* ... */ }
    public void deleteWarehouse(UUID warehouseId) { /* ... */ }
}
```

The corresponding test structure should be:
```
/src/test/java/.../service/
└── warehouseservice/
    ├── WarehouseServiceCreateWarehouseTest.java  // Contains tests for createWarehouse()
    └── WarehouseServiceDeleteWarehouseTest.java  // Contains tests for deleteWarehouse()
```

## 7. Suggesting Code Improvements for Testability

### Rule: Propose refactoring for hard-to-test code

If the class under test is difficult to test, you should propose changes to improve its testability before writing the tests. However, do not apply these changes automatically.

#### Core Principle:

Testing is a key indicator of code quality. If a class is hard to test, it often signals design problems such as:
-   **Violation of the Single Responsibility Principle (SRP):** A method or class does too many things.
-   **Complex conditional logic:** Deeply nested `if-else` statements or complex boolean expressions.
-   **Lack of clear entry points:** Behavior is hidden within private methods without a clear public interface to trigger it.

Instead of working around these issues with complex tests, it is better to suggest improvements to the production code.

#### Action Steps:

1.  **Identify Testability Issues:** When you encounter code that is difficult to cover with tests, analyze the root cause.
2.  **Propose Specific Changes:** Formulate clear suggestions for refactoring. For example:
    -   "Split the `processOrder` method into smaller methods, each with a single responsibility (e.g., `validateOrder`, `updateInventory`, `sendConfirmation`)."
    -   "Refactor the complex `if` condition for calculating discounts into a separate private method with a clear name."
    -   "Apply the Fail-Fast principle by adding initial validation checks at the beginning of the method."
3.  **Present Suggestions:** Provide your suggestions for code improvement before proceeding with test implementation. This allows for a discussion about improving the code's design.
4.  **Write Tests for Improved Code:** Once the suggestions are accepted and implemented, write clean and straightforward tests for the refactored code.
