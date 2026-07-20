import { describe, it, expect } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { render } from './utils';
import Users from '@/pages/Users';
import { Toaster } from '@/components/ui/toaster';

describe('UserTable', () => {
  const renderUsers = () => {
    return render(<><Users /><Toaster /></>);
  };

  it('should render user list table', async () => {
    renderUsers();
    await waitFor(() => { expect(screen.getByRole('table')).toBeInTheDocument(); });
  });

  it('should display user data in table', async () => {
    renderUsers();
    await waitFor(() => { expect(screen.getByRole('table')).toBeInTheDocument(); });
    const rows = screen.getAllByRole('row');
    expect(rows.length).toBeGreaterThan(1);
  });

  it('should show deactivate buttons', async () => {
    renderUsers();
    await waitFor(() => { expect(screen.getByRole('table')).toBeInTheDocument(); });
    const deactivateButtons = screen.getAllByRole('button', { name: /deactivate/i });
    expect(deactivateButtons.length).toBeGreaterThan(0);
  });

  it('should render user management header', async () => {
    renderUsers();
    await waitFor(() => { expect(screen.getByText(/user management/i)).toBeInTheDocument(); });
  });

  it('should render all users section', async () => {
    renderUsers();
    await waitFor(() => { expect(screen.getByText(/all users/i)).toBeInTheDocument(); });
  });
});
