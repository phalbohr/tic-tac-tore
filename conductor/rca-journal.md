## [2026-03-08] Task: Implement Match Creation API
- **Issue:** LazyInitializationException in MatchIntegrationTest
- **Root Cause:** Attempted to access lazy-loaded 'games' collection on Match entity after the JPA session had closed.
- **Resolution:** Annotated the test class with @Transactional and shifted to verifying the response via jsonPath on the DTO, which already has the data loaded.
- **Lesson:** Always use @Transactional for integration tests that touch lazy relationships, or prefer asserting on DTOs/JSON response to ensure session independence.
## [2026-03-08] Task: Security and Persistence Fixes
- **Issue:** JWT tokens ignored by backend; Users not saved after OAuth2 login.
- **Root Cause:** Missing JwtAuthenticationFilter in Security Filter Chain; CustomOAuth2SuccessHandler only generated token but didn't call UserRepository.save().
- **Resolution:** Implemented JwtAuthenticationFilter and updated SecurityConfig to use it with STATELESS policy. Added persistence logic to CustomOAuth2SuccessHandler.
- **Lesson:** Authentication is a two-way street: issuing tokens is useless without a filter to verify them. Always sync external OAuth2 identity with local database.
