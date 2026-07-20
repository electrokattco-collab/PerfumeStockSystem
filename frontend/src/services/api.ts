import axios from 'axios';
import { LoginRequest, LoginResponse, Product, Sale, User, Customer, Expense, DashboardSummary, ProfitReport, PaginatedResponse } from '@/types';

const API_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

api.interceptors.response.use(
  (r) => r,
  (e) => Promise.reject(new Error(e.response?.data?.message || e.message || 'Error'))
);

export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

function paginationQuery(params?: PaginationParams): Record<string, string> {
  if (!params) return {};
  const q: Record<string, string> = {};
  if (params.page !== undefined) q.page = String(params.page);
  if (params.size !== undefined) q.size = String(params.size);
  if (params.sort) q.sort = params.sort;
  if (params.direction) q.direction = params.direction;
  return q;
}

export const authApi = {
  login: (d: LoginRequest) => api.post<LoginResponse>('/auth/login', d),
  logout: () => api.post('/auth/logout'),
  me: () => api.get<User>('/auth/me'),
};

export const productApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<Product>>('/products', { params: paginationQuery(params) }),
  create: (d: any) => api.post<Product>('/products', d),
  update: (id: number, d: any) => api.put<Product>(`/products/${id}`, d),
  delete: (id: number) => api.delete(`/products/${id}`),
  clearAll: () => api.delete('/products/clear'),
  getById: (id: number) => api.get<Product>(`/products/${id}`),
  getLowStock: () => api.get<Product[]>('/products/lowstock'),
  search: (name?: string, category?: string, params?: PaginationParams) =>
    api.get<PaginatedResponse<Product>>('/products/search', { params: { ...paginationQuery(params), ...(name ? { name } : {}), ...(category ? { category } : {}) } }),
};

export const saleApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<Sale>>('/sales', { params: paginationQuery(params) }),
  create: (d: any) => api.post<Sale>('/sales', d),
  update: (id: number, d: any) => api.put<Sale>(`/sales/${id}`, d),
  search: (name?: string, customer?: string, params?: PaginationParams) =>
    api.get<PaginatedResponse<Sale>>('/sales', { params: { ...paginationQuery(params), ...(name ? { name } : {}), ...(customer ? { customer } : {}) } }),
  markPaid: (id: number) => api.put<Sale>(`/sales/${id}/pay`),
  delete: (id: number) => api.delete(`/sales/${id}`),
};

export const customerApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<Customer>>('/customers', { params: paginationQuery(params) }),
  getById: (id: number) => api.get<Customer>(`/customers/${id}`),
  create: (d: any) => api.post<Customer>('/customers', d),
  update: (id: number, d: any) => api.put<Customer>(`/customers/${id}`, d),
  search: (name: string, params?: PaginationParams) =>
    api.get<PaginatedResponse<Customer>>('/customers/search', { params: { ...paginationQuery(params), name } }),
};

export const expenseApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<Expense>>('/expenses', { params: paginationQuery(params) }),
  create: (d: any) => api.post<Expense>('/expenses', d),
  update: (id: number, d: any) => api.put<Expense>(`/expenses/${id}`, d),
  delete: (id: number) => api.delete(`/expenses/${id}`),
  getByCategory: (c: string, params?: PaginationParams) =>
    api.get<PaginatedResponse<Expense>>(`/expenses/category/${c}`, { params: paginationQuery(params) }),
};

export const reportApi = {
  getDashboard: () => api.get<DashboardSummary>('/reports/dashboard'),
  getProfit: () => api.get<ProfitReport>('/reports/profit'),
  getDaily: () => api.get('/reports/daily'),
  getWeekly: () => api.get('/reports/weekly'),
  getMonthly: () => api.get('/reports/monthly'),
  getLowStock: () => api.get<Product[]>('/reports/lowstock'),
};

export const userApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<User>>('/users', { params: paginationQuery(params) }),
  create: (d: any) => api.post<User>('/users', d),
  update: (id: number, d: any) => api.put<User>(`/users/${id}`, d),
  delete: (id: number) => api.delete(`/users/${id}`),
  activate: (id: number) => api.post(`/users/${id}/activate`),
};

export const imageApi = {
  upload: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<{ url: string; filename: string }>('/images/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export const barcodeApi = {
  getQRCodeUrl: (productId: string, size = 250) =>
    `${API_URL}/barcodes/qr/${encodeURIComponent(productId)}?size=${size}`,
  getBarcodeUrl: (productId: string, width = 300, height = 80) =>
    `${API_URL}/barcodes/code128/${encodeURIComponent(productId)}?width=${width}&height=${height}`,
};


export const supplierApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<any>>('/suppliers', { params: paginationQuery(params) }),
  getActive: () => api.get<any[]>('/suppliers/active'),
  getById: (id: number) => api.get<any>(`/suppliers/${id}`),
  create: (d: any) => api.post<any>('/suppliers', d),
  update: (id: number, d: any) => api.put<any>(`/suppliers/${id}`, d),
  delete: (id: number) => api.delete(`/suppliers/${id}`),
};

export const financeApi = {
  getAll: () => api.get<any[]>('/finance'),
  create: (d: any) => api.post<any>('/finance', d),
  delete: (id: number) => api.delete(`/finance/${id}`),
  getSummary: () => api.get<any>('/finance/summary'),
  getByDateRange: (start: string, end: string) => api.get<any[]>('/finance/range', { params: { start, end } }),
};

export const receiptApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<any>>('/receipts', { params: paginationQuery(params) }),
  getById: (id: number) => api.get<any>(`/receipts/${id}`),
  getPending: () => api.get<any[]>('/receipts/pending'),
  scan: (file: File) => {
    const fd = new FormData();
    fd.append('file', file);
    return api.post('/receipts/scan', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
  create: (d: any) => api.post<any>('/receipts', d),
  updateItems: (id: number, items: any[]) => api.put<any>(`/receipts/${id}/items`, items),
  process: (id: number) => api.put<any>(`/receipts/${id}/process`),
  reject: (id: number) => api.put<any>(`/receipts/${id}/reject`),
};

export const paymentApi = {
  getByCustomer: (customerId: number) => api.get<any[]>(`/payments/customer/${customerId}`),
  record: (d: any) => api.post<any>('/payments', d),
};

export const planningApi = {
  calculate: (items: any[]) => api.post('/planning/calculate', items),
  simulate: (scenario: any) => api.post('/planning/simulate', scenario),
};

export const stockMovementApi = {
  getByProduct: (productId: number) => api.get<any[]>(`/stock-movements/product/${productId}`),
};

export const reportApiV2 = {
  adminDashboard: () => api.get<any>('/v2/reports/admin/dashboard'),
  managerDashboard: () => api.get<any>('/v2/reports/manager/dashboard'),
  salesDashboard: () => api.get<any>('/v2/reports/sales/dashboard'),
  salesTrend: () => api.get('/v2/reports/trend'),
  expenseBreakdown: () => api.get('/v2/reports/expenses/breakdown'),
  inventoryReport: () => api.get<any>('/v2/reports/inventory'),
  debtReport: () => api.get<any>('/v2/reports/debt'),
  daily: () => api.get('/v2/reports/daily'),
  weekly: () => api.get('/v2/reports/weekly'),
  monthly: () => api.get('/v2/reports/monthly'),
  yearly: () => api.get('/v2/reports/yearly'),
};

export default api;

export const procurementApi = {
  getAll: (params?: PaginationParams) => api.get<PaginatedResponse<any>>('/procurements', { params: paginationQuery(params) }),
  getById: (id: number) => api.get<any>(`/procurements/${id}`),
  create: (d: any) => api.post<any>('/procurements', d),
  update: (id: number, d: any) => api.put<any>(`/procurements/${id}`, d),
  delete: (id: number) => api.delete(`/procurements/${id}`),
  confirm: (id: number) => api.post<any>(`/procurements/${id}/confirm`),
  ocr: (id: number, d: any) => api.post<any>(`/procurements/${id}/ocr`, d),
  search: (params: { supplierName?: string; invoiceNumber?: string; status?: string } & PaginationParams) =>
    api.get<PaginatedResponse<any>>('/procurements/search', { params: paginationQuery(params) }),
  dashboard: () => api.get<any>('/procurements/dashboard'),
  uploadInvoice: (file: File) => {
    const fd = new FormData();
    fd.append('file', file);
    return api.post('/procurements/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
};
