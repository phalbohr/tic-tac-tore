# Deep Review Report

**Date:** 2024-05-23
**File reviewed:** frontend/src/services/statisticsService.ts

## 01-arch-design-review.md

### Tight Coupling to Pinia Store
- **Severity:** MEDIUM
- **Description:** `getLeaderboard` directly calls `useAuthStore()` inside the function.
- **Risk:** Makes the service hard to test in isolation without mocking the entire Pinia store and its state. It also prevents the service from being used in contexts where the store might not be initialized.
- **Recommendation:** Pass the token as a parameter or use a functional approach to inject dependencies.
- **Code Example:**
```typescript
export async function getLeaderboard(params: LeaderboardParams, token: string): Promise<Page<LeaderboardEntry>> {
  // ...
  const response = await fetch(`${API_BASE_URL}/leaderboard?${queryParams.toString()}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    },
    // ...
```

---

## 02-functionality-reliability-review.md

### Basic Error Handling
- **Severity:** MEDIUM
- **Description:** Errors only include the status code: `throw new Error(\`Failed to fetch leaderboard: ${response.status}\`)`.
- **Risk:** Debugging becomes difficult as the actual error body from the backend (which might contain validation errors) is swallowed.
- **Recommendation:** Parse the response body on non-OK status to provide more context.
- **Code Example:**
```typescript
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to fetch leaderboard: ${response.status}`);
  }
```

---

## 03-secure-code-review.md

### Hardcoded API Base URL Fallback
- **Severity:** LOW
- **Description:** `const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'`.
- **Risk:** While standard for development, having a hardcoded fallback can lead to accidental leaks of production data to a local environment if the env var is missing in a staging build.
- **Recommendation:** Ensure `VITE_API_BASE_URL` is mandatory in the build process and remove the hardcoded fallback.

---

## 04-performance-review.md

### Good Use of AbortSignal
- **Severity:** FYI
- **Description:** The function accepts an `AbortSignal`.
- **Observation:** This is excellent for performance and resource management, allowing the UI to cancel pending requests when a user navigates away or changes filters rapidly.

---

## 05-test-review.md

### Missing Unit Tests
- **Severity:** HIGH
- **Description:** No tests were found for this service in the initial glob search (checked `__tests__/statisticsService.spec.ts` earlier).
- **Risk:** Changes to the API contract or logic (like param mapping) could break the frontend without notice.
- **Recommendation:** Add Vitest unit tests mocking `fetch` and `useAuthStore`.

---

## 06-clean-code-review.md

### Clean and Readable
- **Severity:** FYI
- **Description:** The code is very clean, uses descriptive names, and leverages TypeScript interfaces effectively.
- **Observation:** No issues found in naming or structure.

---

## 07-style-automation-review.md

### Naming Convention Compliance
- **Severity:** LOW
- **Description:** Follows standard TypeScript/Vue project conventions.
- **Observation:** Consistent with the rest of the project.

---

## 08-documentation-review.md

### Missing JSDoc for Public API
- **Severity:** LOW
- **Description:** `getLeaderboard` and the exported interfaces lack JSDoc comments.
- **Risk:** New developers might not immediately understand the purpose of specific `LeaderboardType` or `TimePeriod` values without checking the backend.
- **Recommendation:** Add basic JSDoc.

---

## 09-nitpick-review.md

### Literal String Concatenation for Query Params
- **Severity:** LOW
- **Description:** `fetch(\`${API_BASE_URL}/leaderboard?${queryParams.toString()}\`, ...)`
- **Nit:** While correct, ensure `API_BASE_URL` never ends with a slash to avoid double slashes.
- **Recommendation:** Use a utility to join URLs.

---

## 10-logging-security-review.md

### No Logging Issues
- **Severity:** FYI
- **Description:** The file does not perform any logging.
- **Observation:** No sensitive data is exposed.

---

## 11-logging-review.md

### Lack of Operational Logging
- **Severity:** LOW
- **Description:** No logs for failed requests.
- **Risk:** Transient issues in production might be invisible without client-side error reporting or logging.
- **Recommendation:** Add a `console.error` or integrate with an error tracking service (e.g., Sentry) in the error block.

---

## 12-logging-error-handling-review.md

### Exception Specificity
- **Severity:** MEDIUM
- **Description:** Throws a generic `Error`.
- **Risk:** Catching code cannot distinguish between a network error, a 401 Unauthorized, or a 400 Bad Request.
- **Recommendation:** Create a custom `ApiError` class.

---

## 13-log-retention-review.md

### N/A
- **Description:** No logging implemented, so retention is not applicable.
