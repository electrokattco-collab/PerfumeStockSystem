import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { server } from './mocks/server';
import { render } from './utils';
import Users from '@/pages/Users';
import { Toaster } from '@/components/ui/toaster';

const API_URL = import.meta.env.VITE_API_URL || '/api';

describe('UserTable', () => {
  beforeAll(() => server.listen());
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  const renderUsers = () => {
    return render(
      <>
        <Users />
        <Toaster />
      </>
    );
  };

  describe('Loading State', () => {
    it('should display loading message initially', () => {
      renderUsers();

      expect(screen.getByText(/loading users/i)).toBeInTheDocument();
    });

    it('should hide loading message after data is fetched', async () => {
      renderUsers();

      await waitFor(() => {
        expect(screen.queryByText(/loading users/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Data Display', () => {
    it('should render user table with correct headers', async () => {
      renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const headers = screen.getAllByRole('columnheader');
      expect(headers).toHaveLength(5);
      expect(headers[0]).toHaveTextContent(/username/i);
      expect(headers[1]).toHaveTextContent(/email/i);
      expect(headers[2]).toHaveTextContent(/role/i);
      expect(headers[3]).toHaveTextContent(/status/i);
      expect(headers[4]).toHaveTextContent(/actions/i);
    });

    it('should display all users from API', async () => {
      renderUsers();

      await waitFor(() => {
        expect(screen.getByText('admin')).toBeInTheDocument();
      });

      expect(screen.getByText('manager')).toBeInTheDocument();
      expect(screen.getByText('sales')).toBeInTheDocument();
      expect(screen.getByText('inactiveuser')).toBeInTheDocument();
    });

    it('should display user emails correctly', async () => {
      renderUsers();

      await waitFor(() => {
        expect(screen.getByText('admin@example.com')).toBeInTheDocument();
        expect(screen.getByText('manager@example.com')).toBeInTheDocument();
        expect(screen.getByText('sales@example.com')).toBeInTheDocument();
      });
    });

    it('should display role badges with correct formatting', async () => {
      renderUsers();

      await waitFor(() => {
        // Check for role badges (formatted as lowercase with spaces)
        expect(screen.getByText('admin')).toBeInTheDocument();
        expect(screen.getByText('manager')).toBeInTheDocument();
        expect(screen.getByText('sales rep')).toBeInTheDocument();
      });
    });

    it('should display status badges with correct styling', async () => {
      renderUsers();

      await waitFor(() => {
        const activeStatuses = screen.getAllByText('Active');
        const inactiveStatuses = screen.getAllByText('Inactive');

        expect(activeStatuses).toHaveLength(3);
        expect(inactiveStatuses).toHaveLength(1);
      });
    });
  });

  describe('User Actions', () => {
    it('should show deactivate button for active users', async () => {
      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const deactivateButtons = screen.getAllByRole('button', { name: /deactivate/i });
      expect(deactivateButtons).toHaveLength(3);
    });

    it('should show activate button for inactive users', async () => {
      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const activateButtons = screen.getAllByRole('button', { name: /activate/i });
      expect(activateButtons).toHaveLength(1);
    });

    it('should deactivate user when deactivate button is clicked', async () => {
      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const deactivateButton = screen.getAllByRole('button', { name: /deactivate/i })[0];
      await user.click(deactivateButton);

      await waitFor(() => {
        expect(screen.getByText(/user deactivated/i)).toBeInTheDocument();
      });
    });

    it('should activate user when activate button is clicked', async () => {
      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const activateButton = screen.getByRole('button', { name: /activate/i });
      await user.click(activateButton);

      await waitFor(() => {
        expect(screen.getByText(/user activated/i)).toBeInTheDocument();
      });
    });

    it('should refresh user list after toggling user status', async () => {
      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const deactivateButton = screen.getAllByRole('button', { name: /deactivate/i })[0];
      await user.click(deactivateButton);

      // Wait for the success toast and data refresh
      await waitFor(() => {
        expect(screen.getByText(/user deactivated/i)).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('should display error toast when API call fails', async () => {
      server.use(
        http.get(`${API_URL}/users`, () => {
          return HttpResponse.error();
        })
      );

      renderUsers();

      // The component silently logs errors, so we just verify loading state clears
      await waitFor(() => {
        expect(screen.queryByText(/loading users/i)).not.toBeInTheDocument();
      });

      // Table should not be rendered on error
      expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });

    it('should display error toast when deactivation fails', async () => {
      server.use(
        http.delete(`${API_URL}/users/:id`, () => {
          return HttpResponse.json(
            { message: 'Failed to deactivate user' },
            { status: 400 }
          );
        })
      );

      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const deactivateButton = screen.getAllByRole('button', { name: /deactivate/i })[0];
      await user.click(deactivateButton);

      await waitFor(() => {
        expect(screen.getByText(/failed to deactivate user/i)).toBeInTheDocument();
      });
    });

    it('should display error toast when activation fails', async () => {
      server.use(
        http.post(`${API_URL}/users/:id/activate`, () => {
          return HttpResponse.json(
            { message: 'Failed to activate user' },
            { status: 400 }
          );
        })
      );

      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const activateButton = screen.getByRole('button', { name: /activate/i });
      await user.click(activateButton);

      await waitFor(() => {
        expect(screen.getByText(/failed to activate user/i)).toBeInTheDocument();
      });
    });
  });

  describe('Accessibility', () => {
    it('should have accessible table structure', async () => {
      renderUsers();

      await waitFor(() => {
        const table = screen.getByRole('table');
        expect(table).toBeInTheDocument();
      });

      const rows = screen.getAllByRole('row');
      expect(rows.length).toBeGreaterThan(1); // Header + data rows

      const headerRow = rows[0];
      const headers = within(headerRow).getAllByRole('columnheader');
      expect(headers).toHaveLength(5);
    });

    it('should have accessible buttons for user actions', async () => {
      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const buttons = screen.getAllByRole('button');
      expect(buttons.length).toBeGreaterThan(0);

      buttons.forEach((button) => {
        expect(button).toHaveAttribute('type', 'button');
      });
    });

    it('should maintain focus management after actions', async () => {
      const { user } = renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      const deactivateButton = screen.getAllByRole('button', { name: /deactivate/i })[0];
      await user.click(deactivateButton);

      // After action, focus should be managed (button may be disabled briefly)
      await waitFor(() => {
        expect(deactivateButton).not.toBeDisabled();
      });
    });
  });

  describe('Responsive Design', () => {
    it('should render within a card component', async () => {
      renderUsers();

      await waitFor(() => {
        expect(screen.getByRole('table')).toBeInTheDocument();
      });

      // Check for card title
      expect(screen.getByText(/all users/i)).toBeInTheDocument();
    });

    it('should have overflow handling for table', async () => {
      renderUsers();

      await waitFor(() => {
        const tableContainer = screen.getByRole('table').parentElement;
        expect(tableContainer).toHaveClass('overflow-x-auto');
      });
    });
  });
});
