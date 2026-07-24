1. Update `JwtService` to add claims for `avatar`, `language`, and `tutorialCompleted`.
2. Update `JwtAuthenticationFilter` to extract these claims and populate them in the `User` principal.
3. Update `ProfileApi` and `UserController` so that `getMyProfile` no longer hits the DB, but returns the profile directly from the `principal`.
4. Update `ProfileApi` and `UserController` so that `updateProfile` reissues the JWT token (and revokes the old one) so that the cookie is updated with the new claims.
5. Update `UserControllerTest`, `JwtServiceTest`, and `JwtAuthenticationFilterTest` to ensure they test these new claims and cookie logic.
6. Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.
