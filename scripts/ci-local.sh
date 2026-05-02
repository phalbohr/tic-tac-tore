#!/bin/bash
# Security: This script is for local simulation of CI environment.
# It uses the same commands as .github/workflows/test.yml

echo "🚀 Simulating CI environment locally..."

echo "📦 Backend: Compiling and running tests..."
mvn clean verify || { echo "❌ Backend failed"; exit 1; }

echo "📦 Frontend: Installing dependencies..."
cd frontend
npm ci || { echo "❌ Frontend install failed"; exit 1; }

echo "🔍 Frontend: Type-checking..."
npm run type-check || { echo "❌ Frontend type-check failed"; exit 1; }

echo "🔨 Frontend: Building production bundle..."
npm run build || { echo "❌ Frontend build failed"; exit 1; }

echo "🧪 Frontend: Unit testing..."
npm run test:unit -- --run || { echo "❌ Frontend unit tests failed"; exit 1; }

echo "🎭 Frontend: E2E testing (Playwright)..."
npm run test:e2e || { echo "❌ Frontend E2E tests failed"; exit 1; }

echo "✅ All local CI checks passed!"
