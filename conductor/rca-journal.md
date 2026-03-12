# RCA Journal

## [2026-03-12] Task: Implement Match Approval API
- **Issue:** MatchApprovalTest failed with `IllegalStateException: Authentication context is missing`.
- **Root Cause:** The `getCurrentUser()` method in `MatchService` is called before the match lookup. In the test, the SecurityContext was not initialized for the "not found" scenario.
- **Resolution:** Added SecurityContext initialization to the `approveMatch_NotFound` test case.
- **Lesson:** Always ensure the entire execution path of the tested method is covered by appropriate mocks/context, even for failure scenarios.

- **Issue:** BUILD FAILURE due to compilation error in `CustomOAuth2SuccessHandlerTest`.
- **Root Cause:** The constructor of `CustomOAuth2SuccessHandler` was changed in a previous task, but the test was not updated to provide the new `UserRepository` dependency.
- **Resolution:** Updated the test constructor and added a mock for `UserRepository`.
- **Lesson:** Perform a full project compile (`mvn clean compile`) after making changes to shared components or their dependencies.

- **Issue:** Test failure in `CustomOAuth2SuccessHandlerTest` related to `sendRedirect`.
- **Root Cause:** `DefaultRedirectStrategy` calls `response.encodeRedirectURL()` before `sendRedirect`. Since it wasn't mocked, it returned `null`, causing the redirect to fail.
- **Resolution:** Added a mock for `encodeRedirectURL` to return the input URL.
- **Lesson:** Be aware of internal framework behaviors when mocking infrastructure components like HttpServletResponse.
