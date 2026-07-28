import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401 && window.location.pathname !== '/login') {
      localStorage.removeItem('jwt');
      window.location.href = '/login';
    }
    const msg = err.response?.data?.message || err.message || 'Request failed';
    return Promise.reject(new Error(msg));
  }
);

export const authApi = {
  login: (data: { username: string; password: string }) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
};

export const productApi = {
  getAll: (params?: { page?: number; size?: number }) => api.get('/products', { params }),
  search: (q: string, params?: { page?: number; size?: number }) => api.get('/products/search', { params: { q, ...params } }),
  getById: (id: number) => api.get(`/products/${id}`),
  getLowStock: () => api.get('/products/lowstock'),
  getNonCombo: () => api.get('/products/non-combo'),
  create: (data: any) => api.post('/products', data),
  update: (id: number, data: any) => api.put(`/products/${id}`, data),
  delete: (id: number) => api.delete(`/products/${id}`),
};

export const purchaseApi = {
  getAll: (params?: { page?: number; size?: number }) => api.get('/purchases', { params }),
  getById: (id: number) => api.get(`/purchases/${id}`),
  record: (data: any) => api.post('/purchases', data),
  confirm: (id: number) => api.post(`/purchases/${id}/confirm`),
};

export const saleApi = {
  getAll: (params?: { page?: number; size?: number }) => api.get('/sales', { params }),
  getById: (id: number) => api.get(`/sales/${id}`),
  recent: () => api.get('/sales/recent'),
  record: (data: any) => api.post('/sales', data),
};

export const customerApi = {
  getAll: (params?: { page?: number; size?: number }) => api.get('/customers', { params }),
  search: (q: string, params?: { page?: number; size?: number }) => api.get('/customers/search', { params: { q, ...params } }),
  getById: (id: number) => api.get(`/customers/${id}`),
  getDebtors: () => api.get('/customers/debtors'),
  getLedger: (
    id: number,
    params?: { page?: number; size?: number; sort?: string; startDate?: string; endDate?: string; transactionType?: string }
  ) => api.get(`/customers/${id}/ledger`, { params }),
  getBalance: (id: number) => api.get(`/customers/${id}/balance`),
  getStatement: (
    id: number,
    params?: { startDate?: string; endDate?: string; transactionType?: string }
  ) => api.get(`/customers/${id}/statement`, { params }),
  create: (data: any) => api.post('/customers', data),
  update: (id: number, data: any) => api.put(`/customers/${id}`, data),
};

export const paymentApi = {
  getByCustomer: (customerId: number) => api.get(`/payments/customer/${customerId}`),
  record: (data: any) => api.post('/payments', data),
};

export const reportApi = {
  dashboard: () => api.get('/reports/dashboard'),
  period: (period: string) => api.get(`/reports/period/${period}`),
  inventory: () => api.get('/reports/inventory'),
  debt: () => api.get('/reports/debt'),
};
