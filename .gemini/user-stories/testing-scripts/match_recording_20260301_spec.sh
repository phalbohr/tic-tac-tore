#!/bin/bash

# ==============================================================================
# E2E test script for Match Recording Workflow acceptance criteria
# Endpoints tested: POST /api/v1/matches, PUT /api/v1/matches/{id}/approve|reject
# ==============================================================================

# 1. Verification of Authorization Environment Variables
JWT="${JWT:-$1}"
OPPONENT_JWT="${OPPONENT_JWT:-$2}"

if [ -z "$JWT" ] || [ -z "$OPPONENT_JWT" ]; then
    echo -e "\e[31m[ERROR]\e[0m Both the Creator JWT and an Opponent JWT are required."
    echo "Usage: JWT=<creator_token> OPPONENT_JWT=<opponent_token> $0"
    exit 1
fi

API_URL="http://localhost:8080/api/v1/matches"

# Dummy UUIDs for players (in a live database, these need to correspond with actual UUIDs).
# Ensure CREATOR_ID matches the user for $JWT and OPPONENT1_ID matches $OPPONENT_JWT.
CREATOR_ID="11111111-1111-1111-1111-111111111111"
TEAMMATE_ID="22222222-2222-2222-2222-222222222222"
OPPONENT1_ID="33333333-3333-3333-3333-333333333333"
OPPONENT2_ID="44444444-4444-4444-4444-444444444444"

echo -e "\e[34m[TEST 1]\e[0m Create a 2v2 match - Validates creation and PENDING_APPROVAL behavior"
echo "Expected HTTP: 201"

RESPONSE=$(curl -g -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST "$API_URL" \
    -H "Authorization: Bearer $JWT" \
    -H "Content-Type: application/json" \
    -d '{
        "teamAAttackerId": "'$CREATOR_ID'",
        "teamADefenderId": "'$TEAMMATE_ID'",
        "teamBAttackerId": "'$OPPONENT1_ID'",
        "teamBDefenderId": "'$OPPONENT2_ID'",
        "games": [
            { "teamAScore": 10, "teamBScore": 8 },
            { "teamAScore": 10, "teamBScore": 9 }
        ]
    }')

HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | grep -v "HTTP_STATUS")

if [ "$HTTP_STATUS" -eq 201 ]; then
    echo -e "\e[32m[SUCCESS]\e[0m Match created."
    MATCH_ID=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    echo "Created Match ID: $MATCH_ID"
else
    echo -e "\e[31m[FAILED]\e[0m Unexpected HTTP Status: $HTTP_STATUS"
    echo "Response JSON: $BODY"
    exit 1
fi

echo ""
echo -e "\e[34m[TEST 2]\e[0m Opponent Rejects the match -> Match becomes DRAFT"
echo "Expected HTTP: 204"

REJECT_RESPONSE=$(curl -g -s -w "\nHTTP_STATUS:%{http_code}\n" -X PUT "$API_URL/$MATCH_ID/reject" \
    -H "Authorization: Bearer $OPPONENT_JWT")

REJECT_HTTP_STATUS=$(echo "$REJECT_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)

if [ "$REJECT_HTTP_STATUS" -eq 204 ]; then
    echo -e "\e[32m[SUCCESS]\e[0m Match rejected successfully."
else
    echo -e "\e[31m[FAILED]\e[0m Unexpected HTTP Status: $REJECT_HTTP_STATUS"
    echo "$REJECT_RESPONSE" | grep -v "HTTP_STATUS"
    exit 1
fi

echo ""
echo -e "\e[34m[TEST 3]\e[0m Creator cannot approve their own match"
echo "Expected HTTP: 400"

# We must create another match first to test this properly because the previous one is now in a DRAFT state.
NEW_MATCH_RES=$(curl -g -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST "$API_URL" \
    -H "Authorization: Bearer $JWT" \
    -H "Content-Type: application/json" \
    -d '{
        "teamAAttackerId": "'$CREATOR_ID'",
        "teamADefenderId": "'$TEAMMATE_ID'",
        "teamBAttackerId": "'$OPPONENT1_ID'",
        "teamBDefenderId": "'$OPPONENT2_ID'",
        "games": [
            { "teamAScore": 10, "teamBScore": 5 },
            { "teamAScore": 10, "teamBScore": 8 }
        ]
    }')

NEW_MATCH_ID=$(echo "$NEW_MATCH_RES" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

SELF_APPROVE_RESPONSE=$(curl -g -s -w "\nHTTP_STATUS:%{http_code}\n" -X PUT "$API_URL/$NEW_MATCH_ID/approve" \
    -H "Authorization: Bearer $JWT")

SELF_APP_HTTP=$(echo "$SELF_APPROVE_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)

if [ "$SELF_APP_HTTP" -eq 400 ]; then
    echo -e "\e[32m[SUCCESS]\e[0m Creator was rightly denied approval."
else
    echo -e "\e[31m[FAILED]\e[0m Unexpected HTTP Status: $SELF_APP_HTTP"
    echo "$SELF_APPROVE_RESPONSE" | grep -v "HTTP_STATUS"
    exit 1
fi

echo ""
echo -e "\e[34m[TEST 4]\e[0m Opponent Approves the match -> Match becomes CONFIRMED"
echo "Expected HTTP: 204"

APPROVE_RESPONSE=$(curl -g -s -w "\nHTTP_STATUS:%{http_code}\n" -X PUT "$API_URL/$NEW_MATCH_ID/approve" \
    -H "Authorization: Bearer $OPPONENT_JWT")

APPROVE_HTTP_STATUS=$(echo "$APPROVE_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)

if [ "$APPROVE_HTTP_STATUS" -eq 204 ]; then
    echo -e "\e[32m[SUCCESS]\e[0m Match approved successfully."
else
    echo -e "\e[31m[FAILED]\e[0m Unexpected HTTP Status: $APPROVE_HTTP_STATUS"
    echo "$APPROVE_RESPONSE" | grep -v "HTTP_STATUS"
    exit 1
fi

echo ""
echo -e "\e[32m[SUCCESS] All backend Match workflow API tests passed.\e[0m"
exit 0
