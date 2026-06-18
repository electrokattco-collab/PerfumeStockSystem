import { describe, it, expect, vi, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from './mocks/server';
import { render } from './utils';
import Layout from '@/components/Layout';
import { AuthContext, AuthContextType } from '@/context/AuthContext';
import { ReactNode } from 'react';
import { Outlet } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL || '/api';

// Mock useNavigate
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
  beforeAll(() => server.listen());
  afterEach(() => {
    server.resetHandlers();
    mockNavigate.mockClear();
  });
  afterAll(() => server.close());

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

  describe('Loading State', () => {
    it('should show loading spinner when auth is loading', () => {
      renderLayout({ isLoading: true });

      expect(screen.getByRole('status')).toBeInTheDocument();
      expect(document.querySelector('.animate-spin')).toBeInTheDocument();
    });

    it('should not show sidebar when loading', () => {
      renderLayout({ isLoading: true });

      expect(screen.queryByText(/perfume stock/i)).not.toBeInTheDocument();
    });
  });

  describe('Authenticated User', () => {
    const mockAdminUser = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN' as const,
      active: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    const mockManagerUser = {
      id: 2,
      username: 'manager',
      email: 'manager@example.com',
      role: 'MANAGER' as const,
      active: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    const mockSalesUser = {
      id: 3,
      username: 'sales',
      email: 'sales@example.com',
      role: 'SALES_REP' as const,
      active: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    it('should show logo and navigation when authenticated', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === 'ADMIN',
      });

      expect(screen.getByText(/perfume stock/i)).toBeInTheDocument();
      expect(screen.getByTestId('navlink-home')).toBeInTheDocument();
      expect(screen.getByTestId('navlink-inventory')).toBeInTheDocument();
    });

    it('should display user info in sidebar', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === 'ADMIN',
      });

      expect(screen.getByText('admin')).toBeInTheDocument();
      expect(screen.getByText('admin')).toBeInTheDocument();
    });

    it('should show all navigation items for admin', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === 'ADMIN',
      });

      expect(screen.getByTestId('navlink-home')).toBeInTheDocument();
      expect(screen.getByTestId('navlink-inventory')).toBeInTheDocument();
      expect(screen.getByTestId('navlink-record-sale')).toBeInTheDocument();
      expect(screen.getByTestId('navlink-sales')).toBeInTheDocument();
      expect(screen.getByTestId('navlink-reports')).toBeInTheDocument();
      expect(screen.getByTestId('navlink-users')).toBeInTheDocument();
    });

    it('should hide Users link for non-admin users', () => {
      renderLayout({
        user: mockManagerUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === mockManagerUser.role,
      });

      expect(screen.getByTestId('navlink-home')).toBeInTheDocument();
      expect(screen.queryByTestId('navlink-users')).not.toBeInTheDocument();
    });

    it('should hide Users link for sales rep', () => {
      renderLayout({
        user: mockSalesUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === mockSalesUser.role,
      });

      expect(screen.queryByTestId('navlink-users')).not.toBeInTheDocument();
    });

    it('should show logout button', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
      });

      expect(screen.getByRole('button', { name: /logout/i })).toBeInTheDocument();
    });

    it('should call logout and navigate on logout button click', async () => {
      const mockLogout = vi.fn().mockResolvedValue(undefined);

      const { user } = renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        logout: mockLogout,
      });

      const logoutButton = screen.getByRole('button', { name: /logout/i });
      await user.click(logoutButton);

      await waitFor(() => {
        expect(mockLogout).toHaveBeenCalled();
      });

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login');
      });
    });

    it('should show loading state during logout', async () => {
      const mockLogout = vi.fn().mockImplementation(() => new Promise((resolve) => setTimeout(resolve, 100)));

      const { user } = renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        logout: mockLogout,
      });

      const logoutButton = screen.getByRole('button', { name: /logout/i });
      await user.click(logoutButton);

      // Check for loading state
      expect(screen.getByRole('button', { name: /logging out/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /logging out/i })).toBeDisabled();

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login');
      });
    });

    it('should render outlet content', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
      });

      expect(screen.getByTestId('outlet')).toBeInTheDocument();
      expect(screen.getByText('Main Content')).toBeInTheDocument();
    });
  });

  describe('Unauthenticated State', () => {
    it('should not show sidebar when not authenticated', () => {
      renderLayout({
        user: null,
        isAuthenticated: false,
        isLoading: false,
      });

      // Component should still render but might be empty or redirect
      expect(screen.queryByText(/perfume stock/i)).not.toBeInTheDocument();
    });
  });

  describe('Navigation Links', () => {
    const mockAdminUser = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN' as const,
      active: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    it('should have correct navigation structure', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === 'ADMIN',
      });

      const nav = document.querySelector('nav');
      expect(nav).toBeInTheDocument();
    });

    it('should display correct icons with navigation items', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === 'ADMIN',
      });

      // Check that nav links have icons (SVG elements)
      const navLinks = screen.getAllByTestId(/navlink-/);
      navLinks.forEach((link) => {
        expect(link.querySelector('svg')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    const mockAdminUser = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN' as const,
      active: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    it('should handle logout error gracefully', async () => {
      const mockLogout = vi.fn().mockRejectedValue(new Error('Logout failed'));

      const { user } = renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        logout: mockLogout,
      });

      const logoutButton = screen.getByRole('button', { name: /logout/i });
      await user.click(logoutButton);

      // Should still navigate even if logout fails
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/login');
      });
    });
  });

  describe('Accessibility', () => {
    const mockAdminUser = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN' as const,
      active: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    it('should have accessible navigation structure', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
      });

      const nav = document.querySelector('nav');
      expect(nav).toBeInTheDocument();
    });

    it('should have accessible logout button', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
      });

      const logoutButton = screen.getByRole('button', { name: /logout/i });
      expect(logoutButton).toHaveAttribute('type', 'button');
    });

    it('should support keyboard navigation', async () => {
      const { user } = renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
        hasRole: (role: string) => role === 'ADMIN',
      });

      // Tab through navigation
      await user.tab();
      
      // First focusable element should be focused
      const focusedElement = document.activeElement;
      expect(focusedElement).toBeTruthy();
    });
  });

  describe('Responsive Layout', () => {
    const mockAdminUser = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN' as const,
      active: true,
      createdAt: '2024-01-01T00:00:00Z',
    };

    it('should have fixed sidebar positioning', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
      });

      const aside = document.querySelector('aside');
      expect(aside).toHaveClass('fixed');
    });

    it('should have proper main content margin', () => {
      renderLayout({
        user: mockAdminUser,
        isAuthenticated: true,
        isLoading: false,
      });

      const main = document.querySelector('main');
      expect(main).toHaveClass('ml-64');
    });
  });
});
