# Specification: User Authentication Flow

## Goal
Implement a secure and user-friendly authentication flow that allows users to sign in to the Foosball Statistics Platform using Google OAuth2.

## Requirements
- **Backend:**
    - Configure Spring Security with OAuth2 Client.
    - Implement a callback endpoint to handle Google's authorization code.
    - Create/Update user records in the PostgreSQL database upon successful login.
    - Issue a custom JWT for subsequent stateless requests.
- **Frontend:**
    - Create a "Login with Google" button.
    - Handle the redirection to Google and the callback to the application.
    - Store the JWT securely (e.g., in a secure cookie or local storage with appropriate precautions).
    - Implement a Pinia store to manage authentication state (`isAuthenticated`, `user`).
- **Security:**
    - Ensure all communication is over HTTPS (simulated in local dev).
    - Implement JWT validation on the backend for protected endpoints.

## User Flow
1. User clicks "Login with Google" on the landing page.
2. User is redirected to Google's consent screen.
3. Upon approval, Google redirects back to the application.
4. The application exchanges the code for user information.
5. User is logged in and redirected to their dashboard.
