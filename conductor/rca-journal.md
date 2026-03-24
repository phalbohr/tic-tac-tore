## 2026-03-24 Task: Create Player Profile Dashboard

- **Issue:** Potential invalid URL construction in `getLeaderboard` (trailing `?`) and JSDoc violation of "Zero Comments Policy".
- **Root Cause:** Incomplete query parameter handling in the initial implementation and habit of using JSDoc.
- **Resolution:** Deduplicated API fetch logic into a robust `apiFetch` helper that handles query strings correctly and removed all JSDoc/comments as per Rule 14.
- **Lesson:** Always use a centralized helper for API requests to ensure consistent behavior and strictly follow the project's comment policy from the start.

## 2026-03-24 Task: Implement Head-to-Head (H2H) Analytics Table

- **Issue:** Sorting of strings (opponent names) using `<` operator may yield inconsistent results; missing accessibility attributes (`aria-sort`) for screen readers.
- **Root Cause:** Use of basic comparison operators instead of locale-aware methods for string sorting and omission of standard accessibility attributes.
- **Resolution:** Implemented `localeCompare` for robust string sorting and added `aria-sort` attributes to the table headers to improve accessibility.
- **Lesson:** Use `localeCompare` for all user-facing string sorting and always consider accessibility (A11y) early in the component design.
