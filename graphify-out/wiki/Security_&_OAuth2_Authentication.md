# Security & OAuth2 Authentication

> 38 nodes · cohesion 0.08

## Key Concepts

- **JwtService** (11 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.generateToken()** (7 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **CustomOAuth2SuccessHandler** (5 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/security/CustomOAuth2SuccessHandler.java`
- **.extractUsername()** (5 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.isTokenValid()** (5 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.onAuthenticationSuccess()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/security/CustomOAuth2SuccessHandler.java`
- **CustomOAuth2SuccessHandlerTest** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/security/CustomOAuth2SuccessHandlerTest.java`
- **.onAuthenticationSuccess_shouldRedirectWithToken()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/security/CustomOAuth2SuccessHandlerTest.java`
- **DevTestController** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevTestController.java`
- **.createUserData()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevTestController.java`
- **JwtAuthenticationFilter** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/security/JwtAuthenticationFilter.java`
- **.extractClaim()** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **JwtServiceTest** (4 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/JwtServiceTest.java`
- **.doFilterInternal()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/security/JwtAuthenticationFilter.java`
- **.buildToken()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.extractAllClaims()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.extractExpiration()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.getSignInKey()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.isTokenExpired()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- **.generateToken_shouldGenerateValidJwt()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/JwtServiceTest.java`
- **.isTokenValid_shouldReturnTrueForValidToken()** (3 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/JwtServiceTest.java`
- **OncePerRequestFilter** (3 connections)
- **SimpleUrlAuthenticationSuccessHandler** (3 connections)
- **.seed()** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevTestController.java`
- **CustomOAuth2SuccessHandler.java** (2 connections) — `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/security/CustomOAuth2SuccessHandler.java`
- *... and 13 more nodes in this community*

## Relationships

- [[Security Configuration]] (13 shared connections)
- [[Match Operations & Services]] (3 shared connections)

## Source Files

- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/controller/DevTestController.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/security/CustomOAuth2SuccessHandler.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/security/JwtAuthenticationFilter.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/main/java/com/tictactore/service/JwtService.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/security/CustomOAuth2SuccessHandlerTest.java`
- `/Users/ppolukhin/Projects/tic-tac-tore/src/test/java/com/tictactore/service/JwtServiceTest.java`

## Audit Trail

- EXTRACTED: 92 (80%)
- INFERRED: 23 (20%)
- AMBIGUOUS: 0 (0%)

---

*Part of the graphify knowledge wiki. See [[index]] to navigate.*