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

## [2026-03-14] Task: Implement Match Approval API

- **Issue:** ConstraintViolationException during integration tests setup.
- **Root Cause:** The 'Game' entity requires a 'gameNumber' field, which was missing in the manual object creation within the test's @BeforeEach method.
- **Resolution:** Added 'game.setGameNumber(1)' to the test setup.
- **Lesson:** Always verify that manual object creation in tests satisfies all JPA/Bean Validation constraints.

- **Issue:** UnsupportedOperationException during MatchRepository.save().
- **Root Cause:** Used 'List.of(game)' to initialize the games collection in the 'Match' entity within the test. Hibernate/JPA may attempt to modify this collection during the persistence lifecycle, but 'List.of()' returns an immutable list.
- **Resolution:** Wrapped the list in a mutable ArrayList: 'new ArrayList<>(List.of(game))'.
- **Lesson:** Use mutable collections (like ArrayList) when initializing entities in tests to allow JPA providers to manage them.
