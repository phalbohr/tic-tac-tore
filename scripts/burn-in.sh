#!/bin/bash
# Security: Inputs passed through env: to prevent script injection in CI usage

BURN_IN_COUNT=${BURN_IN_COUNT:-10}

echo "🔥 Starting burn-in loop ($BURN_IN_COUNT iterations) - detecting flaky tests"

cd frontend

for i in $(seq 1 "$BURN_IN_COUNT"); do
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "🔥 Burn-in iteration $i/$BURN_IN_COUNT"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  npm run test:e2e || { echo "❌ Flaky test detected at iteration $i"; exit 1; }
done

echo "✅ Burn-in complete - no flaky tests detected"
