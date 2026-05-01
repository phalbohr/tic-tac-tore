#!/bin/bash
# Detect and run tests for changed files only (Git-based)

BASE_REF=${BASE_REF:-main}
echo "🔍 Detecting changes against $BASE_REF..."

CHANGED_FILES=$(git diff --name-only "$BASE_REF")

if [[ -z "$CHANGED_FILES" ]]; then
  echo "✅ No changes detected."
  exit 0
fi

echo "📝 Changed files:"
echo "$CHANGED_FILES"

# Logic to selectively trigger mvn test or npm test based on file paths
if echo "$CHANGED_FILES" | grep -q "^src/"; then
  echo "☕ Backend changes detected. Running Maven tests..."
  mvn test
fi

if echo "$CHANGED_FILES" | grep -q "^frontend/"; then
  echo "Vue Frontend changes detected. Running frontend tests..."
  cd frontend && npm run test:unit -- --run && npm run test:e2e
fi
