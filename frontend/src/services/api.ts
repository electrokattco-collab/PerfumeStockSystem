import axios from 'axios';
import { LoginRequest, LoginResponse, Product, Sale, User, DashboardSummary, ProfitReport } from '@/types';

const API_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  login: (data: LoginRequest) => api.post<LoginResponse>('/auth/login', data),
  register: (data: Partial<User> & { password: string }) => api.post('/auth/register', data),
};

// Products API
export const productApi = {
  getAll: () => api.get<Product[]>('/products'),
  getById: (id: number) => api.get<Product>(`/products/${id}`),
  create: (data: Omit<Product, 'id' | 'createdAt' | 'updatedAt'>) => api.post<Product>('/products', data),
  update: (id: number, data: Partial<Product>) => api.put<Product>(`/products/${id}`, data),
  delete: (id: number) => api.delete(`/products/${id}`),
  search: (params: { name?: string; category?: string }) => api.get<Product[]>('/products/search', { params }),
  getLowStock: () => api.get<Product[]>('/products/lowstock'),
};

// Sales API
export const saleApi = {
  getAll: () => api.get<Sale[]>('/sales'),
  create: (data: { productId: string; quantity: number; customerTier: string }) => api.post<Sale>('/sales', data),
  search: (productName: string) => api.get<Sale[]>('/sales/search', { params: { productName } }),
  getToday: () => api.get<Sale[]>('/sales/today'),
};

// Reports API
export const reportApi = {
  getDashboard: () => api.get<DashboardSummary>('/reports/dashboard'),
  getProfit: () => api.get<ProfitReport>('/reports/profit'),
  getLowStock: () => api.get<Product[]>('/reports/lowstock'),
};

// Users API
export const userApi = {
  getAll: () => api.get<User[]>('/users'),
  getById: (id: number) => api.get<User>(`/users/${id}`),
  create: (data: Partial<User> & { password: string }) => api.post<User>('/users', data),
  update: (id: number, data: Partial<User>) => api.put<User>(`/users/${id}`, data),
  delete: (id: number) => api.delete(`/users/${id}`),
  activate: (id: number) => api.post(`/users/${id}/activate`),
};

export default api;
