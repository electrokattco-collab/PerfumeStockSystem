# Changelog

All notable changes to the Perfume Stock System.

## [1.5.0] - 2026-07-19 — Business Operations, Finance & OCR Automation

### Business Operations (Milestone 4.1)

#### Role-Based Dashboards
- **Admin Dashboard** — Total sales, revenue, expenses, profit, inventory value, outstanding debts, users, activity
- **Manager Dashboard** — Cash flow, expenses, purchasing, financial planning, inventory value, profit forecast
- **Sales Dashboard** — Today/weekly/monthly sales, customers served, outstanding debts, quick actions

#### Financial Dashboard (Manager/Admin)
- **Redesigned Expenses page** into full Financial Dashboard with tabs
- Record business transactions: Monthly stipend, cash injected, money collected, other income
- Record expenses: Transport, marketing, rent, utilities, miscellaneous
- Automatic calculation: Current Cash, Total Sales, Money Collected, Money Owing, Operating Expenses, Available Cash, Net Business Position
- Income vs Expense breakdown charts

#### Stock Purchase Planner (Manager/Admin)
- Enter supplier, product, quantity, cost per item, selling price
- Auto-calculate: Total Purchase Cost, Expected Revenue, Expected Profit, Cash Remaining, Profit Margin %
- Next Month Simulation — simulate "if I buy these products..." with Available Cash, Money Needed, Shortfall, Remaining Balance

#### OCR Purchase Receipt Scanner (Manager/Admin)
- Upload receipt image (drag & drop, file picker, camera)
- Tesseract OCR extracts: Product Name, Quantity, Buying Price, Supplier, Date, Total, Tax
- Editable table for correcting extracted data before saving
- Receipt processing workflow: PENDING → PROCESSED/REJECTED
- Automatic product creation and stock updates

#### Customer Management Enhancements
- Customer debt list with days overdue
- Payment history per customer
- Record payments (full/partial) with method (Cash/Card/EFT)
- Automatic balance updates across sales and payments

#### Product History
- Stock movement history per product (PURCHASE, SALE, ADJUSTMENT)
- Price information (buying, selling, profit per item)
- Total purchased, total sold, stock value

#### Enhanced Reports
- Daily/Weekly/Monthly/Yearly periods with profit calculation
- Expense breakdown by category
- Inventory report (total, low stock, out of stock, by category)
- Debt report (total owing, debtor list with details)
- Sales trend chart (7-day rolling)

### Backend Changes

**New Entities:**
- `Supplier` — Supplier management with contact details
- `StockMovement` — Inventory movement audit trail
- `BusinessTransaction` — Income/expense tracking for financial dashboard
- `PurchaseReceipt` — OCR scanned purchase receipts
- `PurchaseReceiptItem` — Line items from receipts
- `PaymentHistory` — Customer payment tracking

**New Services:**
- `SupplierService` — Supplier CRUD
- `StockMovementService` — Record inventory movements
- `BusinessTransactionService` — Financial transactions + summary
- `ReceiptService` — Receipt CRUD + processing workflow
- `OcrService` — Tesseract OCR receipt processing with image enhancement
- `PaymentService` — Customer payment recording with balance updates
- `EnhancedReportService` — Role-based dashboards, trends, reports

**New Controllers:**
- `SupplierController` — `/api/suppliers`
- `StockMovementController` — `/api/stock-movements`
- `BusinessTransactionController` — `/api/finance`
- `ReceiptController` — `/api/receipts`
- `PaymentController` — `/api/payments`
- `StockPurchaseController` — `/api/planning`
- `EnhancedReportController` — `/api/v2/reports`

**Database Migration:**
- `V6__Business_operations_financial.sql` — 7 new tables, product/customer extensions, 10 indexes

### Frontend Changes

**New Pages:**
- `StockPlanner.tsx` — Purchase planning with calculation and simulation
- `ReceiptScanner.tsx` — OCR receipt scanning with editable results
- `ProductHistory.tsx` — Product stock movement history

**Updated Pages:**
- `Dashboard.tsx` — Role-based (Admin/Manager/Sales) with different KPIs
- `Expenses.tsx` → Financial Dashboard with tabs (Dashboard/Transactions/Expenses)
- `Customers.tsx` — Payment recording, payment history, full CRUD
- `Reports.tsx` — Period reports, inventory report, debt report with tabs
- `Layout.tsx` — Role-based navigation (Manager sees Planner + Receipts)

**New API Methods:**
- `supplierApi`, `financeApi`, `receiptApi`, `paymentApi`, `planningApi`
- `stockMovementApi`, `reportApiV2` (role-based dashboards)

### Testing
- **All 33 backend tests pass**
- **All 20 frontend tests pass**
- **Frontend build**: Clean production build
- **TypeScript**: Zero errors

---



## [1.2.0] - 2026-07-19 — Code Quality, DTOs & PWA

### Backend - Response DTOs
- **Added** 6 response DTOs (`ProductResponse`, `CustomerResponse`, `ExpenseResponse`, `SaleResponse`, `SaleItemResponse`, `UserResponse`)
- **Updated** all controllers to return DTOs instead of raw entities

### Backend - Exception Handling
- **Replaced** all remaining `RuntimeException` with domain-specific exceptions

### PWA
- **Added** `manifest.json`, `sw.js` service worker, app icons

### Frontend
- **Added** `ErrorBoundary`, `EmptyState`, `LoadingSkeleton` components

---

## [1.1.0] - 2026-07-19 — Critical Security Milestone

### Security
- **Fixed** `WebSecurityConfig`: Replaced `anyRequest().permitAll()` with role-based authorization
- **Enabled** `@EnableMethodSecurity`
- **Fixed** CORS, JWT, password validation

### Testing
- **All 33 backend tests pass**
- **All 20 frontend tests pass**
