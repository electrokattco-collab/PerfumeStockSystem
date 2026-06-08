export interface User {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'MANAGER' | 'SALES_REP';
  active: boolean;
  createdAt: string;
}

export interface Product {
  id: number;
  productId: string;
  name: string;
  category: string;
  size: string;
  retailPrice: number;
  rewardsPrice: number;
  goldPrice: number;
  vipPrice: number;
  stockQuantity: number;
  lowStockThreshold: number;
  isLowStock: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Sale {
  id: number;
  saleId: string;
  product: Product;
  productName: string;
  category: string;
  quantity: number;
  unitPrice: number;
  costOfGoodsSold: number;
  customerTier: 'RETAIL' | 'REWARDS' | 'GOLD' | 'VIP';
  createdAt: string;
}

export interface DashboardSummary {
  totalProducts: number;
  lowStockCount: number;
  todaySalesCount: number;
  todayRevenue: number;
  totalStockValue: number;
}

export interface ProfitReport {
  totalRevenue: number;
  totalCost: number;
  totalProfit: number;
  totalSales: number;
  profitByTier: Record<string, number>;
}

export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * Login response no longer contains the JWT token.
 * The token is now set as an httpOnly cookie by the backend.
 */
export interface LoginResponse {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'MANAGER' | 'SALES_REP';
}
