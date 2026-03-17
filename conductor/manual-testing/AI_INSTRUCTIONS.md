# AI Instructions for Manual Testing

## ⚠️ Important Note for Future AI Assistants
When asked to test the MatchRecording service, follow these steps to save time:

1.  **Request Credentials**: Ask the user to provide the required number of users with valid JWT tokens.
2.  **Check Service Status**: Ask the user whether you should start the services (Backend/Frontend) yourself or if they are already running.
3.  **Enable Test Endpoint**:
    - Copy `conductor/manual-testing/DevTestController.java.template` to `src/main/java/com/tictactore/controller/DevTestController.java`.
    - In `SecurityConfig.java`, add `.requestMatchers("/api/v1/dev/**").permitAll()` to the `authorizeHttpRequests` chain.
    - Restart the backend server.
4.  **Seed Database**: Call `POST http://localhost:8080/api/v1/dev/seed` to ensure relevant users exist. Capture generated UUIDs from the response.
5.  **Run Verification**: Use the tokens and UUIDs to execute the `curl` commands documented in `conductor/manual-testing.md`.

## Cleanup
Always remember to revert `SecurityConfig.java` and remove `DevTestController.java` from the `src` directory once testing is complete.
