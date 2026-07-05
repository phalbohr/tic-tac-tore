# E2E Testing with Playwright

This directory contains the End-to-End (E2E) tests for the Tic-Tac-Tore frontend.

## Architecture

We use a modular architecture based on the **TEA (Test Architect)** framework principles:

- **`scenarios/`**: Functional test scripts written in a declarative Gherkin-like style.
- **`support/fixtures/`**: Custom Playwright fixtures that extend the base `test` object with project-specific utilities.
- **`support/factories/`**: Data factories using `@faker-js/faker` to generate dynamic, unique test data.
- **`support/helpers/`**: Utility functions for network interception, authentication, and API interactions.
- **`support/page-objects/`**: (Optional) Page Object Models for encapsulating page-specific logic.

## Getting Started

### Prerequisites
- Node.js (version specified in `.nvmrc`)
- Dependencies installed: `npm install`

### Running Tests
- **All tests**: `npm run test:e2e`
- **With UI mode**: `npm run test:e2e:ui`
- **Debug mode**: `npm run test:e2e:debug`
- **Show report**: `npm run test:e2e:report`

## Best Practices

### Selector Strategy
Always prefer **`data-testid`** attributes for selecting elements. This ensures tests are resilient to changes in styling or DOM structure.
```html
<button data-testid="submit-match-button">Submit</button>
```
```typescript
await page.getByTestId('submit-match-button').click();
```

### Data Isolation
Use factories to create unique data for every test run. Avoid hardcoded IDs or names that could cause collisions in parallel execution.
```typescript
const player = playerFactory.create({ name: 'Pro Player' });
```

### Network Interception
Use the `interceptNetworkCall` helper to mock API responses. This allows testing frontend behavior in isolation from the backend.
```typescript
await interceptNetworkCall({
  page,
  url: '**/api/matches',
  fulfill: { json: { id: '123' } }
});
```
