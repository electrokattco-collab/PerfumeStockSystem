import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import { ReactNode } from 'react';

import { render } from './utils';
import Layout from '@/components/Layout';
import { AuthContext, AuthContextType } from '@/context/AuthContext';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    NavLink: ({ children, to, ...props }: { children: ReactNode; to: string; [key: string]: unknown }) => (
      <a href={to} {...props} data-testid={`navlink-${to.replace('/', '') || 'home'}`}>
        {children}
      </a>
    ),
    Outlet: () => <div data-testid="outlet">Main Content</div>,
  };
});

describe('Navbar / Layout', () => {
  const createAuthProvider = (value: Partial<AuthContextType>) => {
    const defaultValue: AuthContextType = {
      user: null,
      isAuthenticated: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn().mockResolvedValue(undefined),
      hasRole: () => false,
    };

    return function AuthProviderWrapper({ children }: { children: ReactNode }) {
      return (
        <AuthContext.Provider value={{ ...defaultValue, ...value }}>
          {children}
        </AuthContext.Provider>
      );
    };
  };

  const renderLayout = (authValue: Partial<AuthContextType> = {}) => {
    const AuthProviderWrapper = createAuthProvider(authValue);
    return render(
      <AuthProviderWrapper>
        <Layout />
      </AuthProviderWrapper>,
      { withAuth: false }
    );
  };

  const adminUser = {
    id: 1, username: 'admin', email: 'admin@example.com',
    role: 'ADMIN' as const, active: true, createdAt: '2024-01-01T00:00:00Z',
  };

  it('should show loading spinner when auth is loading', () => {
    renderLayout({ isLoading: true });
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('should not show sidebar when loading', () => {
    renderLayout({ isLoading: true });
    expect(screen.queryByText(/perfume stock/i)).not.toBeInTheDocument();
  });

  it('should show app title for authenticated user', () => {
    renderLayout({
      user: adminUser,
      isAuthenticated: true,
      hasRole: (role: string) => role === 'ADMIN',
    });
    expect(screen.getAllByText(/perfume stock/i).length).toBeGreaterThan(0);
  });

  it('should show outlet for main content', () => {
    renderLayout({
      user: adminUser,
      isAuthenticated: true,
      hasRole: () => true,
    });
    expect(screen.getByTestId('outlet')).toBeInTheDocument();
  });

  it('should show navigation links for admin user', () => {
    renderLayout({
      user: adminUser,
      isAuthenticated: true,
      hasRole: () => true,
    });
    const expectedLinks = ['home', 'inventory', 'record-sale', 'sales', 'reports', 'customers', 'expenses'];
    expectedLinks.forEach((link) => {
      const elements = screen.getAllByTestId(`navlink-${link}`);
      expect(elements.length).toBeGreaterThan(0);
    });
    // Admin should see users link
    expect(screen.getAllByTestId('navlink-users').length).toBeGreaterThan(0);
  });

  it('should not show users link for non-admin', () => {
    renderLayout({
      user: { ...adminUser, role: 'MANAGER' as const },
      isAuthenticated: true,
      hasRole: (role: string) => role === 'MANAGER',
    });
    expect(screen.queryByTestId('navlink-users')).not.toBeInTheDocument();
  });

  it('should show logout button', () => {
    renderLayout({
      user: adminUser,
      isAuthenticated: true,
      hasRole: () => true,
    });
    expect(screen.getAllByText(/logout/i).length).toBeGreaterThan(0);
  });

  it('should call logout when clicked', async () => {
    const logoutMock = vi.fn().mockResolvedValue(undefined);
    const { user } = renderLayout({
      user: adminUser,
      isAuthenticated: true,
      logout: logoutMock,
      hasRole: () => true,
    });
    const logoutButton = screen.getAllByText(/logout/i)[0];
    await user.click(logoutButton);
    expect(logoutMock).toHaveBeenCalled();
  });
});
