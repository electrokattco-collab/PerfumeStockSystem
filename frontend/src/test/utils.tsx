import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';

// Create a custom render function that includes providers
interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  withRouter?: boolean;
  withAuth?: boolean;
  authValue?: {
    user: {
      id: number;
      username: string;
      email: string;
      role: 'ADMIN' | 'MANAGER' | 'SALES_REP';
      active: boolean;
      createdAt: string;
    } | null;
    isAuthenticated: boolean;
    isLoading: boolean;
  };
}

// Mock AuthProvider wrapper
function createAuthWrapper(authValue?: CustomRenderOptions['authValue']) {
  return function AuthWrapper({ children }: { children: React.ReactNode }) {
    const defaultValue = {
      user: {
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        role: 'ADMIN' as const,
        active: true,
        createdAt: '2024-01-01T00:00:00Z',
      },
      isAuthenticated: true,
      isLoading: false,
    };

    const value = authValue || defaultValue;

    return (
      <AuthContext.Provider value={value}>
        {children}
      </AuthContext.Provider>
    );
  };
}

// Create a mock AuthContext for testing
import { createContext, useContext } from 'react';
import { AuthContextType } from '@/context/AuthContext';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

// Custom render function
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

    return <>{content}</>;
  }

  const user = require('@testing-library/user-event').default.setup();

  return {
    user,
    ...render(ui, { wrapper: AllTheProviders, ...renderOptions }),
  };
}

// Re-export everything from testing-library
export * from '@testing-library/react';
export { customRender as render };
