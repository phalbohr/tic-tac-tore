Fix the following 16 issues found during code review in the current branch:

1. Flawed Optimistic UI Rollback on API Failure
In auth.ts, updateProfile relies on a shallow clone ({ ...profile.value }) for optimistic UI rollbacks. This will corrupt nested states on failure. Furthermore, if the API fails, the TutorialCarousel component is destroyed and remounts without showing an error.

2. Flaky E2E Test Relying on Magic Sleep
onboarding.spec.ts uses await page.waitForTimeout(600) to arbitrarily bypass a hardcoded scroll debounce. Tests must await deterministic DOM state changes, not arbitrary wall-clock delays.

3. Layout Thrashing in Scroll Event Handler
TutorialCarousel.vue binds handleScroll to the scroll event and reads carouselRef.value.clientWidth on every frame, forcing synchronous layout recalculations and causing frame drops.

4. Irresponsible Document State Mutation for Overflow
TutorialCarousel.vue sets document.body.style.overflow = 'hidden' on mount and blindly resets it to '' on unmount. This completely fails to store and restore the original overflow state, potentially overwriting custom overflow styles.

5. Reckless Global Event Listeners (Escape key)
Modal binds a keydown listener directly to document to capture the Escape key without checking the event target. Pressing Escape in an external input field will unintentionally trigger completeTutorial().

6. Missing Focus Trapping for Accessibility
Despite correctly using role="dialog" and aria-modal="true", TutorialCarousel.vue completely fails to trap keyboard focus, a severe WCAG compliance violation.

7. Race Conditions in Scroll Debouncing
TutorialCarousel.vue hardcodes a 500ms setTimeout to unlock the isScrolling guard. If a browser's native smooth scrolling takes longer, the guard releases prematurely, allowing overlapping scroll commands.

8. Incomplete Integration Testing
The unit tests added in UserServiceTest.java only verify the happy path where tutorialCompleted is set to true. There is zero coverage for explicit false payloads, omitted payloads, or concurrent modification attempts.

9. Masked Schema Drift in Migrations
The Flyway script V3__add_tutorial_completed_to_user.sql uses ADD COLUMN IF NOT EXISTS. This defeats the purpose of strict schema versioning and silently swallows anomalies.

10. macOS rubber-band scroll yields negative scrollLeft
In TutorialCarousel.vue:handleScroll, macOS rubber-band scrolling can yield negative scrollLeft, making currentSlide become -1 and rendering all slides invisible.

11. Missing Translation Key for Tutorial Error
TutorialCarousel.vue references t('tutorial.error') but neither en.json nor de.json includes the error key under the tutorial namespace. Because vue-i18n returns the key name when missing, the UI will display the literal string 'tutorial.error'.

12. Old error message remains visible during retry
In TutorialCarousel.vue:completeTutorial, if the user retries finishing after a previous API failure, the old error message remains visible during the retry.

13. Pagination dots aria-current issue
Screen reader user navigates pagination dots but :aria-current="currentSlide === index ? 'true' : 'false'" prevents user from identifying which slide is currently active correctly.

14. Unrelated Formatting Changes in Cabinet.vue
Cabinet.vue contains extensive formatting modifications (e.g., breaking HTML tags into multiple lines) without any functional changes related to the story constraints. Revert these formatting changes.

15. Missing Blank Line Between Arrange and Act Phases
In UserRepositoryTest.java (saveUser_PersistsTutorialCompletedFlag), the Act phase directly follows the Arrange phase without a blank line separating them.

16. Contradictory Comment in E2E Test Implementation
In frontend/e2e/onboarding.spec.ts, a comment explicitly states "// Explicitly set tutorial completed false for test robustness", but the subsequent API call to /api/auth/test-login only passes { email, nickname } as params, omitting the tutorialCompleted flag.
