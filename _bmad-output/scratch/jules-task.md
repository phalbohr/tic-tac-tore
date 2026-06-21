Fix the following code review findings on the current branch.
Context: branch=story/1-7-onboarding-tutorial

1. Memory Leak in Carousel [TutorialCarousel.vue]: scroll listener on carouselRef.value added in onMounted but not removed in onUnmounted.
2. Failing CI Checks and Committed Test Junk [test-results/.last-run.json]: Do not commit this file, and fix any failed tests.
3. Missing Rollback on Optimistic UI Update [authStore.ts]: authStore.ts profile state mutated before API request without rollback logic if it fails.
4. Silent failure without UI feedback [TutorialCarousel.vue:157-158]: TutorialCarousel.vue catches API error but gives no visual feedback, leaving user stuck.
5. Missing Accessibility Traps (inert/aria-hidden) [TutorialCarousel.vue]: Fullscreen overlay lacks inert/aria-hidden for background.
6. Missing Keyboard Dismissal (Escape) [TutorialCarousel.vue]: No Escape key listener to close tutorial.
7. Missing Body Scroll Lock [TutorialCarousel.vue]: Missing scroll lock on body tag while tutorial is open.
8. Dangerous Test Concurrency Side Effects [UserService.java]: UserService.findOrCreateTestUser unconditionally mutates existing test users.
9. Incomplete API Contract [UpdateProfileRequest.java]: UpdateProfileRequest misses @NotNull constraints or @Schema defaults for tutorialCompleted.
10. NaN on zero clientWidth [TutorialCarousel.vue:141]: currentSlide becomes NaN if clientWidth is 0.
11. Rapid clicks on Next break state [TutorialCarousel.vue:252]: repeatedly targets same slide.
12. Missing Integration Tests [UserServiceTest.java]: Add integration tests for the new profile field.
13. E2E Test File Path Deviation [e2e/scenarios/onboarding.spec.ts]: Playwright test is in e2e/scenarios/ instead of e2e/onboarding.spec.ts. Move it.
