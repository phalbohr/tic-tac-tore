## 2026-03-24 Task: Create Player Profile Dashboard

- **Issue:** Potential invalid URL construction in `getLeaderboard` (trailing `?`) and JSDoc violation of "Zero Comments Policy".
- **Root Cause:** Incomplete query parameter handling in the initial implementation and habit of using JSDoc.
- **Resolution:** Deduplicated API fetch logic into a robust `apiFetch` helper that handles query strings correctly and removed all JSDoc/comments as per Rule 14.
- **Lesson:** Always use a centralized helper for API requests to ensure consistent behavior and strictly follow the project's comment policy from the start.
