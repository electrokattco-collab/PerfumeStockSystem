import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthContext, AuthContextType } from '@/context/AuthContext';
import { ThemeProvider } from '@/context/ThemeContext';
import { vi } from 'vitest';

interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  withRouter?: boolean;
  withAuth?: boolean;
  authValue?: Partial<AuthContextType>;
}

const defaultAuthValue: AuthContextType = {
  user: {
    id: 1,
    username: 'admin',
    email: 'admin@example.com',
    role: 'ADMIN',
    active: true,
    createdAt: '2024-01-01T00:00:00Z',
  },
  isAuthenticated: true,
  isLoading: false,
  login: vi.fn(),
  logout: vi.fn().mockResolvedValue(undefined),
  hasRole: (role: string) => role === 'ADMIN',
};

function createAuthWrapper(authValue?: Partial<AuthContextType>) {
  return function AuthWrapper({ children }: { children: React.ReactNode }) {
    const value = { ...defaultAuthValue, ...authValue };
    return (
      <AuthContext.Provider value={value}>
        {children}
      </AuthContext.Provider>
    );
  };
}

export function customRender(
  ui: ReactElement,
  options: CustomRenderOptions = {}
): ReturnType<typeof render> & { user: ReturnType<typeof import('@testing-library/user-event').default.setup> } {
  const { withRouter = true, withAuth = true, authValue, ...renderOptions } = options;

  function AllTheProviders({ children }: { children: React.ReactNode }) {
    let content = children;
    if (withAuth) {
      const AuthWrapper = createAuthWrapper(authValue);
      content = <AuthWrapper>{content}</AuthWrapper>;
    }
    if (withRouter) {
      content = <BrowserRouter>{content}</BrowserRouter>;
    }
    return <ThemeProvider>{content}</ThemeProvider>;
  }

  const user = require('@testing-library/user-event').default.setup();

  return {
    user,
    ...render(ui, { wrapper: AllTheProviders, ...renderOptions }),
  };
}

export * from '@testing-library/react';
export { customRender as render };
