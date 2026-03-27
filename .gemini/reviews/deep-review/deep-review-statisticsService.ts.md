# Deep Review Report

**Date:** 2025-05-14
**Files reviewed:** frontend/src/services/statisticsService.ts

---

## 01-Architecture & Design Review

### Status: [🟡]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

- **`statisticsService.ts:60`** — `apiFetch` is a local helper. As the project grows, this will lead to code duplication and inconsistent error handling across services.

### 🔵 Recommendations for improvement

- **`statisticsService.ts:60`** — Extract `apiFetch` into a shared `apiClient.ts` to centralize header management (auth), base URL, and error normalization.

---

## 02-Functionality & Reliability Review

### Status: [🟡]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

- **`statisticsService.ts:114`** — `getPersonalStats` implementation ignores `myPosition` and `opponentPosition` from `PersonalStatsParams`, even though they are available in the interface. If the backend doesn't support them for this endpoint, the interface should be split.

### 🔵 Recommendations for improvement

- **`statisticsService.ts:88`** — Add a try/catch block around `fetch` in `apiFetch` to handle network errors (e.g., `TypeError: Failed to fetch`) and provide a more descriptive error message than the generic bubbling exception.

---

## 03-Secure Code Review

### Status: [🟡]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

- **`statisticsService.ts:114,122,132`** — Passing the `token` explicitly to every service function is prone to omission and leaks. A centralized auth interceptor (if using a library like Axios) or a wrapper around `fetch` that automatically pulls the token from the store/localStorage is safer.

### 🔵 Recommendations for improvement

- **`statisticsService.ts:58`** — `API_BASE_URL` fallback to `localhost` is hardcoded. Use a more robust config management if possible.

---

## 04-Performance Review

### Status: [🟢]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

_None found._

### 🔵 Recommendations for improvement

- **`statisticsService.ts:122`** — Statistics and leaderboards are ideal candidates for short-term client-side caching (e.g., using TanStack Query) to avoid redundant network calls on rapid navigation.

---

## 05-Test Review

### Status: [🔴]

### 🔴 Critical issues

- **`statisticsService.spec.ts:10`** — Missing test case for the polymorphic return branch in `getH2HStats` (when backend returns a `Page` object). This branch is critical logic but remains uncovered.

### 🟡 Potential risks

- **`statisticsService.spec.ts`** — `AbortSignal` propagation is not tested, which could lead to regressions where long-running requests are not actually cancelled.

### 🔵 Recommendations for improvement

- **`statisticsService.spec.ts:98`** — Add a test for malformed JSON responses to ensure the error handling logic in `apiFetch` (specifically the `.catch(() => ({}))`) behaves as expected under various failure modes.

---

## 06-Clean Code Review

### Status: [🟢]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

_None found._

### 🔵 Recommendations for improvement

- **`statisticsService.ts:133`** — Remove trailing space after `res = await apiFetch<Page<H2HStats> | H2HStats[]>('/statistics/h2h', { `.

---

## 07-Style & Automation Review

### Status: [🟢]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

_None found._

### 🔵 Recommendations for improvement

- **`statisticsService.ts`** — Ensure all service functions consistently pass the `AbortSignal` to `apiFetch` to ensure correct request cancellation.

---

## 08-Documentation Review

### Status: [🟡]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

- **`statisticsService.ts`** — Public API lacks documentation. While the types provide some context, the behavior of polymorphic returns (e.g., `getH2HStats`) is not explained.

### 🔵 Recommendations for improvement

- **`statisticsService.ts:114`** — Add TSDoc comments for all exported functions to clarify parameters and expected return structures.

---

## 09-Nitpick Review

### Status: [🟢]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

_None found._

### 🔵 Recommendations for improvement

- **`statisticsService.ts:133`** — Rename `res` to `response` or `data` in `getH2HStats` for better clarity.

---

## 10-Logging Security Review

### Status: [🟢]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

_None found._

### 🔵 Recommendations for improvement

_None found._

---

## 11-Logging Review

### Status: [🟡]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

- **`statisticsService.ts:88`** — Total lack of logging makes troubleshooting difficult when things fail in user environments.

### 🔵 Recommendations for improvement

- **`statisticsService.ts:100`** — Add a `console.error` in the `!response.ok` branch of `apiFetch` (wrapped in `if (import.meta.env.DEV)`) to help developers diagnose API issues quickly.

---

## 12-Logging & Error Handling Review

### Status: [🟡]

### 🔴 Critical issues

_None found._

### 🟡 Potential risks

- **`statisticsService.ts:101`** — Swallowing `response.json()` errors with `.catch(() => ({}))` could hide critical backend failures if the response is malformed.

### 🔵 Recommendations for improvement

- **`statisticsService.ts:101`** — Provide a more robust error object if the JSON fails to parse, perhaps including the raw status text.

---

## 13-Log Retention Review

### Status: [N/A]

_Not applicable for this frontend service file._

---

## Summary

| Severity           | Count   |
| ------------------ | ------- |
| 🔴 Critical        | 1       |
| 🟡 Risks           | 7       |
| 🔵 Recommendations | 11      |

**Total issues found:** 19
