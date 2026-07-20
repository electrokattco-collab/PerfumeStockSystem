import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { render } from './utils';
import Login from '@/pages/Login';
import { server } from './mocks/server';
import { AuthContextType } from '@/context/AuthContext';

const API_URL = import.meta.env.VITE_API_URL || '/api';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

describe('LoginForm', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  const renderLogin = (authOverrides?: Partial<AuthContextType>) => {
    return render(<Login />, {
      withAuth: true,
      authValue: {
        user: null,
        isAuthenticated: false,
        isLoading: false,
        login: vi.fn(),
        logout: vi.fn(),
        hasRole: () => false,
        ...authOverrides,
      },
    });
  };

  it('should render login form', () => {
    renderLogin();
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('should display default credentials', () => {
    renderLogin();
    expect(screen.getByText(/admin \/ admin123/)).toBeInTheDocument();
  });

  it('should navigate to home on successful login', async () => {
    const { user } = renderLogin();
    await user.type(screen.getByLabelText(/username/i), 'admin');
    await user.type(screen.getByLabelText(/password/i), 'admin123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('should display error message on invalid credentials', async () => {
    server.use(
      http.post(`${API_URL}/auth/login`, () => {
        return HttpResponse.json({ message: 'Invalid credentials' }, { status: 401 });
      })
    );

    const { user } = renderLogin();
    await user.type(screen.getByLabelText(/username/i), 'wronguser');
    await user.type(screen.getByLabelText(/password/i), 'wrongpassword');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      // The API interceptor transforms the error, Login page shows the message or generic fallback
      expect(screen.getByText(/login failed|invalid/i)).toBeInTheDocument();
    });
  });

  it('should have proper form labels', () => {
    renderLogin();
    expect(screen.getByLabelText(/username/i)).toHaveAttribute('id', 'username');
    expect(screen.getByLabelText(/password/i)).toHaveAttribute('id', 'password');
  });

  it('should prevent submission with empty username', async () => {
    const { user } = renderLogin();
    await user.type(screen.getByLabelText(/password/i), 'admin123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('should prevent submission with empty password', async () => {
    const { user } = renderLogin();
    await user.type(screen.getByLabelText(/username/i), 'admin');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
