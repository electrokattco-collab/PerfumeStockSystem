export interface User {
  id: number;
  username: string;
  email: string;
  active: boolean;
  createdAt: string;
}

export interface LoginRequest { username: string; password: string; }
export interface LoginResponse { id: number; username: string; email: string; }

export type SalePaymentType = 'PAID' | 'PARTIAL' | 'CREDIT';
export type PaymentMethod = 'CASH' | 'EFT' | 'TRANSFER' | 'OTHER';
export type PurchaseSourceType = 'MANUAL' | 'PHOTO' | 'PDF' | 'OCR';
export type PurchaseStatus = 'PENDING_REVIEW' | 'CONFIRMED' | 'ARCHIVED';
export type CustomerLedgerEventType =
  | 'SALE_RECORDED'
  | 'SALE_REVERSED'
  | 'PAYMENT_RECEIVED'
  | 'PAYMENT_REVERSED'
  | 'PURCHASE_RECORDED'
  | 'PURCHASE_CONFIRMED'
  | 'PURCHASE_REVERSED'
  | 'INVENTORY_ADJUSTMENT'
  | 'CUSTOMER_REFUND';

export interface Product {
  id: number;
  productCode: string;
  name: string;
  category: string;
  combo: boolean;
  buyPrice: number;
  sellPrice: number;
  stockQuantity: number;
  lowStockThreshold: number;
  lowStock: boolean;
  active: boolean;
  bundleItems?: BundleItem[];
  createdAt: string;
}

export interface BundleItem {
  productId: number;
  productCode: string;
  productName: string;
  quantity: number;
}

export interface Purchase {
  id: number;
  purchaseDate: string;
  sourceType: PurchaseSourceType;
  status: PurchaseStatus;
  totalAmount: number;
  notes: string;
  receiptReference?: string | null;
  ocrText?: string | null;
  ocrConfidence?: number | null;
  confirmedAt?: string | null;
  confirmedBy?: string | null;
  items: PurchaseItem[];
  createdAt: string;
}

export interface PurchaseItem {
  id: number;
  productId: number;
  productCode: string;
  productName: string;
  quantity: number;
  unitCost: number;
  lineTotal: number;
  comboItem: boolean;
}

export interface Sale {
  id: number;
  saleDate: string;
  totalAmount: number;
  costOfGoodsSold: number;
  profit: number;
  paymentType: SalePaymentType;
  amountPaid: number;
  amountOwing: number;
  customerId: number | null;
  customerName: string | null;
  items: SaleItem[];
  createdAt: string;
}

export interface SaleItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  unitCost: number;
  lineTotal: number;
}

export interface Customer {
  id: number;
  name: string;
  phone: string;
  address: string;
  notes: string;
  outstandingBalance: number;
  createdAt: string;
}

export interface CustomerLedgerEntry {
  date: string;
  eventType: CustomerLedgerEventType;
  description: string;
  debit: number;
  credit: number;
  runningBalance: number;
  referenceId: number | null;
  businessEventId: number | null;
  saleId: number | null;
  paymentId: number | null;
}

export interface CustomerBalanceResponse {
  outstandingBalance: number;
  totalPurchases: number;
  totalPayments: number;
  lastPaymentDate: string | null;
}

export interface CustomerStatementResponse {
  businessName: string;
  customerName: string;
  statementPeriod: string;
  startDate: string | null;
  endDate: string | null;
  openingBalance: number;
  closingBalance: number;
  totalDebits: number;
  totalCredits: number;
  transactionCount: number;
  generatedDate: string;
  transactions: CustomerLedgerEntry[];
}

export interface Payment {
  id: number;
  customerId: number;
  customerName: string;
  saleId: number | null;
  amount: number;
  paymentMethod: PaymentMethod;
  notes: string;
  createdAt: string;
}

export interface DashboardData {
  todaySalesCount: number;
  todayRevenue: number;
  todayProfit: number;
  monthSalesCount: number;
  monthRevenue: number;
  monthCost: number;
  monthProfit: number;
  cashSalesMonth: number;
  creditSalesMonth: number;
  cashReceivedToday: number;
  cashReceivedMonth: number;
  totalOutstanding: number;
  overdueAccounts: number;
  monthPurchasesCount: number;
  monthPurchasesSpent: number;
  pendingPurchaseConfirmations: number;
  totalProducts: number;
  inventoryValue: number;
  lowStockCount: number;
  totalDebtors: number;
  totalCustomers: number;
  customersWithOutstandingBalances: number;
  largestDebtor: { id: number; name: string; balance: number } | null;
  customersPaidThisMonth: number;
  averageCustomerPurchaseValue: number;
  inventoryMovementsMonth: number;
  recentActivity: any[];
}

export interface PeriodReport {
  period: string;
  startDate: string;
  endDate: string;
  salesCount: number;
  totalRevenue: number;
  totalCost: number;
  totalProfit: number;
  topSellingProducts: { name: string; quantity: number }[];
}

export interface InventoryReport {
  totalProducts: number;
  lowStock: number;
  outOfStock: number;
  totalSellValue: number;
  totalCostValue: number;
  byCategory: Record<string, number>;
}

export interface DebtReport {
  totalOwing: number;
  debtorCount: number;
  debtors: { id: number; name: string; phone: string; balance: number }[];
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface SpringPage<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
