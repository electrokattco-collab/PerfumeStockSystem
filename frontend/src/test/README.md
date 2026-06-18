# Frontend Testing Guide

This directory contains Vitest tests with React Testing Library for the Perfume Stock System frontend.

## Tech Stack

- **Vitest** - Test runner
- **@testing-library/react** - React component testing utilities
- **@testing-library/jest-dom** - Custom DOM matchers
- **@testing-library/user-event** - User interaction simulation
- **MSW (Mock Service Worker)** - API mocking
- **jsdom** - Browser environment simulation

## Running Tests

```bash
# Run all tests
npm test

# Run tests in UI mode
npm run test:ui

# Run tests with coverage
npm run test:coverage

# Run tests in watch mode
npm test -- --watch
```

## Test Structure

```
src/test/
├── setup.ts              # Test setup and configuration
├── utils.tsx             # Custom render utilities with providers
├── mocks/
│   ├── handlers.ts       # MSW API mock handlers
│   └── server.ts         # MSW server setup
├── Login.test.tsx        # Login form tests
├── Users.test.tsx        # User table tests
└── Layout.test.tsx       # Layout/Navbar tests
```

## Writing Tests

### Basic Test Structure

```typescript
import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { render } from './utils';
import MyComponent from '@/components/MyComponent';

describe('MyComponent', () => {
  it('should render correctly', () => {
    render(<MyComponent />);
    expect(screen.getByText(/hello/i)).toBeInTheDocument();
  });
});
```

### Testing with User Interactions

```typescript
import userEvent from '@testing-library/user-event';

it('should handle user input', async () => {
  const { user } = render(<MyForm />);
  
  const input = screen.getByLabelText(/username/i);
  await user.type(input, 'john');
  
  expect(input).toHaveValue('john');
});
```

### Mocking API Calls

```typescript
import { http, HttpResponse } from 'msw';
import { server } from './mocks/server';

it('should handle API errors', async () => {
  server.use(
    http.get('/api/users', () => {
      return HttpResponse.error();
    })
  );
  
  // Test error handling...
});
```

## Custom Matchers

The following `@testing-library/jest-dom` matchers are available:

- `toBeInTheDocument()`
- `toHaveTextContent()`
- `toHaveClass()`
- `toHaveAttribute()`
- `toBeDisabled()`
- `toBeEnabled()`
- `toBeVisible()`
- `toBeChecked()`
- And more...

## Best Practices

1. **Use `screen` for queries** - Prefer `screen.getBy...` over destructuring from render
2. **Use `userEvent` over `fireEvent`** - More realistic user interactions
3. **Mock API calls with MSW** - Don't hit real APIs in tests
4. **Test behavior, not implementation** - Focus on what users see and do
5. **Use accessibility selectors** - `getByRole`, `getByLabelText` over `getByTestId`
