# Manual Testing Guide: Match Recording Workflow

This guide provides step-by-step instructions for manually verifying the Backend Match Recording API.

## 1. Prerequisites

- **Backend:** Running on `http://localhost:8080` (`./mvnw spring-boot:run`)
- **Frontend:** Running on `http://localhost:3000` (`npm run dev`)
- **Database:** H2 In-Memory (or PostgreSQL if configured)

## 2. Obtain Authentication Token

1. Open your browser and navigate to the frontend: `http://localhost:3000`.
2. Complete the **Google Login** flow.
3. Once logged in, open **Browser DevTools (F12)** -> **Console**.
4. Run the following command to get your JWT:
   ```javascript
   console.log(localStorage.getItem("token"));
   ```
5. Copy the printed token string.
6. Or F12 -> Application -> Local Storage -> http://localhost:3000 -> token

**To relogin (if H2 was restarted):**

```javascript
localStorage.clear();
location.href = "/login";
```

## 3. Prepare Test Data (H2 Console)

> **Note:** Since H2 is in-memory, the database is wiped on restart. You MUST log in via the frontend (Step 2) to create your user record before proceeding.

1. Navigate to: `http://localhost:8080/h2-console`.
2. Use the following credentials:
   - **JDBC URL:** `jdbc:h2:mem:testdb`
   - **User Name:** `sa`
   - **Password:** `password`
3. Identify 4 users.
   - **Find your own ID:** Run `SELECT id, email FROM users;`. Copy your `id`.
   - **Need more players?** Add 3 dummy participants:
   ```sql
   INSERT INTO users (id, email, name) VALUES
   (random_uuid(), 'teammate@test.com', 'Teammate'),
   (random_uuid(), 'opponent1@test.com', 'Opponent 1'),
   (random_uuid(), 'opponent2@test.com', 'Opponent 2');
   ```

   - **Get all IDs:** Run `SELECT id, name FROM users;` and copy the UUIDs.

## 4. Create a Match (via Postman)

1. **Method:** `POST`
2. **URL:** `http://localhost:8080/api/v1/matches`
3. **Headers:**
   - `Authorization`: `Bearer <YOUR_TOKEN_HERE>`
   - `Content-Type`: `application/json`
4. **Body (raw -> JSON):**

```json
{
  "teamAAttackerId": "YOUR_UUID",
  "teamADefenderId": "TEAMMATE_UUID",
  "teamBAttackerId": "OPPONENT1_UUID",
  "teamBDefenderId": "OPPONENT2_UUID",
  "games": [
    { "teamAScore": 10, "teamBScore": 8 },
    { "teamAScore": 10, "teamBScore": 7 }
  ]
}
```

## 5. Create a Match (via curl)

```bash
TOKEN="PASTE_YOUR_JWT_HERE"
MY_ID="PASTE_YOUR_UUID"
TEAMMATE_ID="PASTE_TEAMMATE_UUID"
OPP1_ID="PASTE_OPPONENT1_UUID"
OPP2_ID="PASTE_OPPONENT2_UUID"

curl -X POST http://localhost:8080/api/v1/matches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"teamAAttackerId\": \"$MY_ID\",
    \"teamADefenderId\": \"$TEAMMATE_ID\",
    \"teamBAttackerId\": \"$OPP1_ID\",
    \"teamBDefenderId\": \"$OPP2_ID\",
    \"games\": [
      {\"teamAScore\": 10, \"teamBScore\": 8},
      {\"teamAScore\": 10, \"teamBScore\": 7}
    ]
  }"
```

## 6. Verify Persistence

Check the database to ensure the match and games were saved correctly:

```sql
-- Verify match creation
SELECT * FROM matches;

-- Verify games associated with the match
SELECT * FROM games;
```
