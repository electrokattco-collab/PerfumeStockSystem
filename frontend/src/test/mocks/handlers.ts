import { http, HttpResponse } from 'msw';
import { User } from '@/types';

const API_URL = import.meta.env.VITE_API_URL || '/api';

// Mock user data
export const mockUsers: User[] = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@example.com',
    role: 'ADMIN',
    active: true,
    createdAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 2,
    username: 'manager',
    email: 'manager@example.com',
    role: 'MANAGER',
    active: true,
    createdAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 3,
    username: 'sales',
    email: 'sales@example.com',
    role: 'SALES_REP',
    active: true,
    createdAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 4,
    username: 'inactiveuser',
    email: 'inactive@example.com',
    role: 'SALES_REP',
    active: false,
    createdAt: '2024-01-01T00:00:00Z',
  },
];

export const handlers = [
  // Auth handlers
  http.post(`${API_URL}/auth/login`, async ({ request }) => {
    const body = await request.json() as { username: string; password: string };
    
    if (body.username === 'admin' && body.password === 'admin123') {
      return HttpResponse.json({
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        role: 'ADMIN',
      });
    }
    
    if (body.username === 'invalid') {
      return HttpResponse.json(
        { message: 'Invalid credentials' },
        { status: 401 }
      );
    }
    
    return HttpResponse.json(
      { message: 'Login failed. Please try again.' },
      { status: 401 }
    );
  }),

  http.post(`${API_URL}/auth/logout`, () => {
    return HttpResponse.json({ message: 'Logged out successfully' });
  }),

  http.get(`${API_URL}/auth/me`, () => {
    return HttpResponse.json({
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      role: 'ADMIN',
    });
  }),

  // User handlers
  http.get(`${API_URL}/users`, () => {
    return HttpResponse.json(mockUsers);
  }),

  http.get(`${API_URL}/users/:id`, ({ params }) => {
    const user = mockUsers.find((u) => u.id === Number(params.id));
    if (user) {
      return HttpResponse.json(user);
    }
    return HttpResponse.json({ message: 'User not found' }, { status: 404 });
  }),

  http.post(`${API_URL}/users`, async ({ request }) => {
    const body = await request.json() as Partial<User>;
    
    if (mockUsers.some((u) => u.username === body.username)) {
      return HttpResponse.json(
        { message: `Username already taken: ${body.username}` },
        { status: 400 }
      );
    }
    
    const newUser: User = {
      id: mockUsers.length + 1,
      username: body.username || 'newuser',
      email: body.email || 'new@example.com',
      role: body.role || 'SALES_REP',
      active: true,
      createdAt: new Date().toISOString(),
    };
    
    return HttpResponse.json(newUser, { status: 201 });
  }),

  http.put(`${API_URL}/users/:id`, async ({ params, request }) => {
    const body = await request.json() as Partial<User>;
    const userIndex = mockUsers.findIndex((u) => u.id === Number(params.id));
    
    if (userIndex === -1) {
      return HttpResponse.json({ message: 'User not found' }, { status: 404 });
    }
    
    const updatedUser = { ...mockUsers[userIndex], ...body };
    return HttpResponse.json(updatedUser);
  }),

  http.delete(`${API_URL}/users/:id`, ({ params }) => {
    const userIndex = mockUsers.findIndex((u) => u.id === Number(params.id));
    
    if (userIndex === -1) {
      return HttpResponse.json({ message: 'User not found' }, { status: 404 });
    }
    
    return HttpResponse.json({ message: 'User deactivated successfully' });
  }),

  http.post(`${API_URL}/users/:id/activate`, ({ params }) => {
    const userIndex = mockUsers.findIndex((u) => u.id === Number(params.id));
    
    if (userIndex === -1) {
      return HttpResponse.json({ message: 'User not found' }, { status: 404 });
    }
    
    return HttpResponse.json({ message: 'User activated successfully' });
  }),
];

// Error handler for network errors
export const networkErrorHandler = http.get(`${API_URL}/users`, () => {
  return HttpResponse.error();
});
