export interface User { id: number; username: string; email: string; role: 'ADMIN' | 'MANAGER' | 'SALES_REP'; active: boolean; createdAt: string; }
export interface Product { id: number; productId: string; name: string; category: string; size: string; buyPrice: number; sellPrice: number; stockQuantity: number; lowStockThreshold: number; isLowStock: boolean; imageUrl?: string; barcode?: string; createdAt: string; updatedAt: string; }
export interface SaleItem { id: number; productName: string; quantity: number; unitPrice: number; lineTotal: number; }
export interface Sale { id: number; saleId: string; product: Product | null; productName: string; category: string; quantity: number; unitPrice: number; totalAmount: number; costOfGoodsSold: number; customerName: string; amountOwing: number; paid: boolean; items: SaleItem[]; recordedByUsername?: string; createdAt: string; }
export interface Customer { id: number; name: string; phone: string; outstandingBalance: number; createdAt: string; }
export interface Expense { id: number; category: string; description: string; amount: number; expenseDate: string; createdAt: string; }
export interface DashboardSummary { totalProducts: number; lowStockCount: number; todaySalesCount: number; todayRevenue: number; weekRevenue: number; weekSalesCount: number; monthRevenue: number; monthSalesCount: number; totalStockValue: number; bestSellingProduct: string; }
export interface ProfitReport { totalRevenue: number; totalCost: number; totalProfit: number; totalSales: number; }
export interface LoginRequest { username: string; password: string; }
export interface LoginResponse { id: number; username: string; email: string; role: 'ADMIN' | 'MANAGER' | 'SALES_REP'; }

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

export interface Supplier { id: number; name: string; phone: string; email: string; address: string; notes: string; active: boolean; createdAt: string; }
export interface StockMovement { id: number; product: Product; movementType: string; quantity: number; unitCost: number; referenceId: number; referenceType: string; notes: string; createdBy: string; createdAt: string; }
export interface BusinessTransaction { id: number; transactionType: string; category: string; description: string; amount: number; transactionDate: string; createdBy: string; createdAt: string; }
export interface PurchaseReceipt { id: number; receiptNumber: string; supplierName: string; totalAmount: number; taxAmount: number; subtotal: number; receiptDate: string; imageUrl: string; ocrRawText: string; status: string; processedBy: string; items: PurchaseReceiptItem[]; createdAt: string; }
export interface PurchaseReceiptItem { id: number; productName: string; product: Product | null; quantity: number; unitCost: number; totalCost: number; }
export interface PaymentHistory { id: number; amount: number; paymentType: string; paymentMethod: string; notes: string; createdBy: string; createdAt: string; }
export interface FinancialSummary { monthStipend: number; monthCashInjected: number; monthMoneyCollected: number; monthOtherIncome: number; monthExpenses: number; monthTransport: number; monthMarketing: number; monthRent: number; monthUtilities: number; totalIncome: number; totalExpenses: number; netPosition: number; }
export interface AdminDashboard { monthRevenue: number; yearRevenue: number; monthSalesCount: number; monthExpenses: number; monthProfit: number; totalProducts: number; inventoryValue: number; lowStockCount: number; totalCustomers: number; totalUsers: number; recentSales: Sale[]; }
export interface ManagerDashboard { monthRevenue: number; monthSalesCount: number; monthExpenses: number; stockValue: number; costValue: number; totalProducts: number; lowStockCount: number; totalPurchasesCost: number; totalOwing: number; owingCustomerCount: number; }
export interface SalesDashboard { todaySalesCount: number; todayRevenue: number; weekSalesCount: number; weekRevenue: number; monthSalesCount: number; monthRevenue: number; customersServedToday: number; outstandingCount: number; outstandingAmount: number; lowStockCount: number; recentSales: Sale[]; }
export interface DebtReport { totalOwing: number; debtorCount: number; debtors: { id: number; name: string; phone: string; balance: number; daysSinceCreated: number }[]; }
export interface InventoryReport { totalProducts: number; lowStock: number; outOfStock: number; totalSellValue: number; totalCostValue: number; byCategory: Record<string, number>; }

export interface ProcurementItem { id: number; productName: string; brand?: string; category?: string; quantityPurchased: number; buyPrice: number; suggestedSellingPrice?: number; expectedProfit?: number; lineTotal: number; barcode?: string; expiryDate?: string; batchNumber?: string; }
export interface Procurement { id: number; supplierName: string; supplierContact?: string; invoiceNumber?: string; purchaseDate: string; invoiceFilePath?: string; invoiceType?: string; subtotal: number; vatAmount: number; totalAmount: number; notes?: string; uploadedBy?: string; status: string; items: ProcurementItem[]; createdAt: string; updatedAt?: string; }
