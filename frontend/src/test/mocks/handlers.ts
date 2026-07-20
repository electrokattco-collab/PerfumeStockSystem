import { http, HttpResponse } from 'msw';
import { User, PaginatedResponse } from '@/types';

const API_URL = import.meta.env.VITE_API_URL || '/api';

function paginate<T>(items: T[], page = 0, size = 50): PaginatedResponse<T> {
  const start = page * size;
  const content = items.slice(start, start + size);
  return {
    content,
    page,
    size,
    totalElements: items.length,
    totalPages: Math.ceil(items.length / size),
    first: page === 0,
    last: start + size >= items.length,
    empty: content.length === 0,
  };
}

export const mockUsers: User[] = [
  { id: 1, username: 'admin', email: 'admin@example.com', role: 'ADMIN', active: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 2, username: 'manager', email: 'manager@example.com', role: 'MANAGER', active: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 3, username: 'sales', email: 'sales@example.com', role: 'SALES_REP', active: true, createdAt: '2024-01-01T00:00:00Z' },
  { id: 4, username: 'inactiveuser', email: 'inactive@example.com', role: 'SALES_REP', active: false, createdAt: '2024-01-01T00:00:00Z' },
];

const emptyPage = <T>(): PaginatedResponse<T> => ({
  content: [], page: 0, size: 50, totalElements: 0, totalPages: 0, first: true, last: true, empty: true,
});

export const handlers = [
  http.post(`${API_URL}/auth/login`, async ({ request }) => {
    const body = await request.json() as { username: string; password: string };
    if (body.username === 'admin' && body.password === 'admin123') {
      return HttpResponse.json({ id: 1, username: 'admin', email: 'admin@example.com', role: 'ADMIN' });
    }
    return HttpResponse.json({ message: 'Invalid credentials' }, { status: 401 });
  }),

  http.post(`${API_URL}/auth/logout`, () => {
    return HttpResponse.json({ message: 'Logged out successfully' });
  }),

  http.get(`${API_URL}/auth/me`, () => {
    return HttpResponse.json({ id: 1, username: 'admin', email: 'admin@example.com', role: 'ADMIN' });
  }),

  http.get(`${API_URL}/users`, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '50');
    return HttpResponse.json(paginate(mockUsers, page, size));
  }),

  http.get(`${API_URL}/users/:id`, ({ params }) => {
    const user = mockUsers.find((u) => u.id === Number(params.id));
    if (user) return HttpResponse.json(user);
    return HttpResponse.json({ message: 'User not found' }, { status: 404 });
  }),

  http.post(`${API_URL}/users`, async ({ request }) => {
    const body = await request.json() as Partial<User>;
    if (mockUsers.some((u) => u.username === body.username)) {
      return HttpResponse.json({ message: `Username already taken: ${body.username}` }, { status: 400 });
    }
    const newUser: User = { id: mockUsers.length + 1, username: body.username || 'newuser', email: body.email || 'new@example.com', role: body.role || 'SALES_REP', active: true, createdAt: new Date().toISOString() };
    return HttpResponse.json(newUser);
  }),

  http.put(`${API_URL}/users/:id`, async ({ params, request }) => {
    const body = await request.json() as Partial<User>;
    const userIndex = mockUsers.findIndex((u) => u.id === Number(params.id));
    if (userIndex === -1) return HttpResponse.json({ message: 'User not found' }, { status: 404 });
    return HttpResponse.json({ ...mockUsers[userIndex], ...body });
  }),

  http.delete(`${API_URL}/users/:id`, () => {
    return HttpResponse.json({ message: 'User deactivated successfully' });
  }),

  http.post(`${API_URL}/users/:id/activate`, () => {
    return HttpResponse.json({ message: 'User activated successfully' });
  }),

  // Paginated endpoints returning empty pages
  http.get(`${API_URL}/products`, () => HttpResponse.json(emptyPage())),
  http.get(`${API_URL}/products/search`, () => HttpResponse.json(emptyPage())),
  http.get(`${API_URL}/products/lowstock`, () => HttpResponse.json([])),
  http.get(`${API_URL}/sales`, () => HttpResponse.json(emptyPage())),
  http.get(`${API_URL}/customers`, () => HttpResponse.json(emptyPage())),
  http.get(`${API_URL}/customers/search`, () => HttpResponse.json(emptyPage())),
  http.get(`${API_URL}/expenses`, () => HttpResponse.json(emptyPage())),
  http.get(`${API_URL}/reports/dashboard`, () => HttpResponse.json({
    totalProducts: 0, lowStockCount: 0, todaySalesCount: 0, todayRevenue: 0,
    weekRevenue: 0, weekSalesCount: 0, monthRevenue: 0, monthSalesCount: 0,
    totalStockValue: 0, bestSellingProduct: 'N/A',
  })),
];

export const networkErrorHandler = http.get(`${API_URL}/users`, () => HttpResponse.error());
