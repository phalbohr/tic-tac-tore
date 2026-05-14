# CI/CD Pipeline Documentation

## Overview
This project uses **GitHub Actions** for CI/CD. The pipeline is designed to ensure high quality through multi-stage testing, parallel execution, and flaky test detection.

## Pipeline Structure
- **Backend (Java 21)**: Runs `mvn clean verify`, generates JaCoCo coverage reports.
- **Frontend Unit & Lint**: Runs `npm run type-check`, `npm run build` (to verify module resolution and asset bundling), and `npm run test:unit`.
- **Frontend E2E (Parallel)**: Runs Playwright tests sharded across 4 parallel jobs.
- **Burn-In Loop**: Automatically runs 10 iterations of E2E tests on Pull Requests to detect flakiness.
- **Report**: Aggregates all results into a GitHub Step Summary.

## Quality Gates
- **100% Pass Rate**: Required for all stages.
- **Production Build Verification**: The frontend must successfully build (`npm run build`) in CI to catch missing assets, CSS errors, or bundler-specific issues that isolated unit tests ignore.
- **Strict PR Policy**: All changes must pass the pipeline before merging into `develop` or `main`.
- **Review Requirement**: CI pass is a prerequisite for human review.

## Local Verification
You can simulate the CI environment locally using:
```bash
./scripts/ci-local.sh
```

## Flaky Test Detection
To manually run a burn-in loop on your branch:
```bash
BURN_IN_COUNT=5 ./scripts/burn-in.sh
```
