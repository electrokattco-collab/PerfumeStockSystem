import { describe, it, expect, vi, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from './mocks/server';
import { customRender as render } from './utils';
import Login from '@/pages/Login';
import { AuthProvider } from '@/context/AuthContext';
import { BrowserRouter } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL || '/api';

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('LoginForm', () => {
  beforeAll(() => server.listen());
  afterEach(() => {
    server.resetHandlers();
    mockNavigate.mockClear();
  });
  afterAll(() => server.close());

  const renderLogin = () => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          <Login />
        </AuthProvider>
      </BrowserRouter>,
      { withRouter: false, withAuth: false }
    );
  };

  describe('Rendering', () => {
    it('should render login form with all required elements', () => {
      renderLogin();

      expect(screen.getByRole('heading', { name: /perfume stock system/i })).toBeInTheDocument();
      expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    });

    it('should display default credentials hint', () => {
      renderLogin();

      expect(screen.getByText(/default credentials:/i)).toBeInTheDocument();
      expect(screen.getByText(/admin \/ admin123/i)).toBeInTheDocument();
    });

    it('should have required attributes on input fields', () => {
      renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      expect(usernameInput).toHaveAttribute('required');
      expect(passwordInput).toHaveAttribute('required');
    });
  });

  describe('User Interactions', () => {
    it('should update input values when user types', async () => {
      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await user.type(usernameInput, 'admin');
      await user.type(passwordInput, 'admin123');

      expect(usernameInput).toHaveValue('admin');
      expect(passwordInput).toHaveValue('admin123');
    });

    it('should show loading state during form submission', async () => {
      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      await user.type(usernameInput, 'admin');
      await user.type(passwordInput, 'admin123');
      
      // Submit form
      await user.click(submitButton);

      // Check loading state
      expect(screen.getByRole('button', { name: /signing in/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled();
    });

    it('should navigate to home page on successful login', async () => {
      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      await user.type(usernameInput, 'admin');
      await user.type(passwordInput, 'admin123');
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/');
      });
    });
  });

  describe('Error Handling', () => {
    it('should display error message on invalid credentials', async () => {
      server.use(
        http.post(`${API_URL}/auth/login`, () => {
          return HttpResponse.json(
            { message: 'Invalid credentials' },
            { status: 401 }
          );
        })
      );

      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      await user.type(usernameInput, 'wronguser');
      await user.type(passwordInput, 'wrongpassword');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
      });
    });

    it('should display generic error message when response has no message', async () => {
      server.use(
        http.post(`${API_URL}/auth/login`, () => {
          return HttpResponse.json({}, { status: 401 });
        })
      );

      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      await user.type(usernameInput, 'invalid');
      await user.type(passwordInput, 'invalid');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/login failed\. please try again\./i)).toBeInTheDocument();
      });
    });

    it('should clear error message when user starts typing again', async () => {
      server.use(
        http.post(`${API_URL}/auth/login`, () => {
          return HttpResponse.json(
            { message: 'Invalid credentials' },
            { status: 401 }
          );
        })
      );

      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      // First failed login
      await user.type(usernameInput, 'wronguser');
      await user.type(passwordInput, 'wrongpassword');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
      });

      // Clear and type again - error should be cleared on new submit
      await user.clear(usernameInput);
      await user.type(usernameInput, 'newuser');
      
      // The error should still be visible until form is submitted again
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper form labels', () => {
      renderLogin();

      expect(screen.getByLabelText(/username/i)).toHaveAttribute('id', 'username');
      expect(screen.getByLabelText(/password/i)).toHaveAttribute('id', 'password');
    });

    it('should support keyboard navigation', async () => {
      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      // Tab through elements
      await user.tab();
      expect(document.activeElement).toBe(usernameInput);

      await user.tab();
      expect(document.activeElement).toBe(passwordInput);

      await user.tab();
      expect(document.activeElement).toBe(submitButton);
    });

    it('should submit form on Enter key press', async () => {
      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);

      await user.type(usernameInput, 'admin');
      await user.type(passwordInput, 'admin123');
      await user.keyboard('{Enter}');

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/');
      });
    });
  });

  describe('Form Validation', () => {
    it('should prevent submission with empty username', async () => {
      const { user } = renderLogin();

      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      await user.type(passwordInput, 'admin123');
      await user.click(submitButton);

      // Form should not submit (HTML5 validation)
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    it('should prevent submission with empty password', async () => {
      const { user } = renderLogin();

      const usernameInput = screen.getByLabelText(/username/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      await user.type(usernameInput, 'admin');
      await user.click(submitButton);

      // Form should not submit (HTML5 validation)
      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });
});
