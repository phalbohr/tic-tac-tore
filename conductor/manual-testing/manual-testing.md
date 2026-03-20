# Manual Testing Guide: Match Recording Workflow

This guide provides step-by-step instructions for manually verifying the Backend Match Recording API.

## 1. Prerequisites

- **Backend:** Running on `http://localhost:8080` (`./mvnw spring-boot:run`)
- **Frontend:** Running on `http://localhost:3000` (`npm run dev`)
- **Database:** H2 In-Memory (or PostgreSQL if configured)

## 2. Obtain Authentication Token (Dev Token Panel)

1. Open your browser and navigate to the frontend: `http://localhost:3000`.
2. If not authenticated, click **"Go to Login"** and complete the **Google Login** flow.
3. Once back on the home page (`/`), you will see the **"Dev Token Panel"**.
4. Your JWT token is displayed in the text area.
5. Click **"Copy to clipboard ✅"** to copy the token.

**To relogin (if H2 was restarted):**
If you get a 401 error, click **"Logout"** on the home page, then login again to refresh the user record in the H2 database.

## 3. Prepare Test Data (H2 Console)

> **Note:** Since H2 is in-memory, the database is wiped on restart. You MUST log in via the frontend (Step 2) to create your user record before proceeding.

1. Navigate to: `http://localhost:8080/h2-console`.
2. Credentials: **JDBC URL:** `jdbc:h2:mem:testdb`, **User Name:** `sa`, **Password:** `password`.
3. Identity 4 users.
   - **Find your own ID:** Run `SELECT id, email FROM users;`. Copy your `id`.
   - **Add 3 dummy participants if needed:**
   ```sql
   INSERT INTO users (id, email, name) VALUES
   ('e6f2fba1-0b66-4c7a-9e33-d134c084a9b6', 'teammate@test.com', 'Teammate'),
   ('e7ca89f1-89fc-409a-a718-4a8417a65a97', 'opponent1@test.com', 'Opponent 1'),
   ('190fa034-60af-461e-aa3f-b52adf9fce52', 'opponent2@test.com', 'Opponent 2');
   ```

## 4. Test API via curl

### Set Environment Variables
```bash
TOKEN="PASTE_COPIED_JWT_HERE"
MY_ID="PASTE_YOUR_UUID"
TEAMMATE_ID="e6f2fba1-0b66-4c7a-9e33-d134c084a9b6"
OPP1_ID="e7ca89f1-89fc-409a-a718-4a8417a65a97"
OPP2_ID="190fa034-60af-461e-aa3f-b52adf9fce52"
```

### A. Create a Match (Initial status: PENDING_APPROVAL)
```bash
curl -X POST http://localhost:8080/api/v1/matches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"teamAAttackerId\": \"$MY_ID\",
    \"teamADefenderId\": \"$TEAMMATE_ID\",
    \"teamBAttackerId\": \"$OPP1_ID\",
    \"teamBDefenderId\": \"$OPP2_ID\",
    \"games\": [{\"teamAScore\": 10, \"teamBScore\": 8}]
  }"
```
*Take the `id` from the response for the next steps.*

### B. Approve the Match (As Opponent)
*Note: In production, you'd need the Opponent's token. For manual testing with your token, ensure YOU are the opponent (swap IDs in Create Match).*
```bash
MATCH_ID="PASTE_CREATED_MATCH_ID"
curl -X PUT http://localhost:8080/api/v1/matches/$MATCH_ID/approve \
  -H "Authorization: Bearer $TOKEN"
```
**Expected:** 204 No Content. Status in DB becomes `CONFIRMED`.

### C. Reject the Match (As Opponent)
```bash
# Create another match first, then:
curl -X PUT http://localhost:8080/api/v1/matches/$MATCH_ID/reject \
  -H "Authorization: Bearer $TOKEN"
```
**Expected:** 204 No Content. Status in DB becomes `DRAFT`.

## 5. Verify Persistence (H2 SQL)
```sql
SELECT id, status, team_a_attacker_id, team_b_attacker_id FROM matches;
SELECT * FROM games WHERE match_id = '...';
```
