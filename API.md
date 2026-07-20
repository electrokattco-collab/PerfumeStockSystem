# API Documentation

## Base URL

- **Development**: `http://localhost:8080/api`
- **Production**: `https://your-domain.com/api`

## Authentication

All endpoints (except `/api/auth/login`) require authentication via JWT cookie.
The JWT is set automatically on login and sent with every request.

## Pagination

All list endpoints support pagination via query parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number (0-indexed) |
| `size` | `50` | Number of items per page |
| `sort` | `id` | Field to sort by |
| `direction` | `asc` | Sort direction: `asc` or `desc` |

### Paginated Response Format

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 50,
  "totalElements": 150,
  "totalPages": 3,
  "first": true,
  "last": false,
  "empty": false
}
```

## Error Responses

All errors follow this format:
```json
{
  "status": 400,
  "error": "Error Type",
  "message": "Human-readable message",
  "path": "/api/endpoint",
  "timestamp": "2026-07-19T18:00:00",
  "fieldErrors": []
}
```

---

## Auth Endpoints

### POST `/api/auth/login`
**Public** - Authenticate user

**Request:**
```json
{ "username": "admin", "password": "admin123" }
```

**Response (200):**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@perfumestock.com",
  "role": "ADMIN"
}
```
Also sets `jwt` httpOnly cookie.

### POST `/api/auth/logout`
**Authenticated** - Clear JWT cookie

### GET `/api/auth/me`
**Authenticated** - Get current user info

### POST `/api/auth/register`
**ADMIN only** - Create new user

---

## Product Endpoints

### GET `/api/products?page=0&size=50&sort=name&direction=asc`
**Authenticated** - List all products (paginated)

### GET `/api/products/{id}`
**Authenticated** - Get product by ID

### POST `/api/products`
**ADMIN, MANAGER** - Create product

### PUT `/api/products/{id}`
**ADMIN, MANAGER** - Update product (with `@Version` optimistic locking)

### DELETE `/api/products/{id}`
**ADMIN** - Delete product

### DELETE `/api/products/clear`
**ADMIN** - Clear all products

### GET `/api/products/search?name=...&category=...&page=0&size=50`
**Authenticated** - Search products (paginated, combined name+category)

### GET `/api/products/lowstock`
**Authenticated** - Get low stock products

---

## Sale Endpoints

### GET `/api/sales?page=0&size=50&sort=createdAt&direction=desc`
**Authenticated** - List all sales (paginated, default newest first)

### POST `/api/sales`
**Authenticated** - Record a sale

### PUT `/api/sales/{id}`
**ADMIN, MANAGER** - Update sale

### PUT `/api/sales/{id}/pay`
**ADMIN, MANAGER** - Mark sale as paid

### DELETE `/api/sales/{id}`
**ADMIN** - Delete sale

---

## Customer Endpoints

### GET `/api/customers?page=0&size=50&sort=name&direction=asc`
**Authenticated** - List all customers (paginated)

### GET `/api/customers/{id}`
**Authenticated** - Get customer by ID

### POST `/api/customers`
**ADMIN, MANAGER** - Create customer

### PUT `/api/customers/{id}`
**ADMIN, MANAGER** - Update customer

### GET `/api/customers/search?name=...&page=0&size=50`
**Authenticated** - Search customers (paginated)

---

## Expense Endpoints

### GET `/api/expenses?page=0&size=50&sort=expenseDate&direction=desc`
**ADMIN, MANAGER** - List all expenses (paginated, default newest first)

### POST `/api/expenses`
**ADMIN, MANAGER** - Create expense

### PUT `/api/expenses/{id}`
**ADMIN, MANAGER** - Update expense

### DELETE `/api/expenses/{id}`
**ADMIN** - Delete expense

### GET `/api/expenses/category/{category}?page=0&size=50`
**ADMIN, MANAGER** - Get expenses by category (paginated)

---

## Report Endpoints

### GET `/api/reports/dashboard`
**ADMIN, MANAGER** - Dashboard summary

### GET `/api/reports/profit`
**ADMIN, MANAGER** - Profit report

### GET `/api/reports/daily`
**ADMIN, MANAGER** - Daily report

### GET `/api/reports/weekly`
**ADMIN, MANAGER** - Weekly report

### GET `/api/reports/monthly`
**ADMIN, MANAGER** - Monthly report

### GET `/api/reports/lowstock`
**ADMIN, MANAGER** - Low stock report

### GET `/api/reports/export?start=...&end=...`
**ADMIN, MANAGER** - Export sales data

---

## User Endpoints

### GET `/api/users?page=0&size=50&sort=id&direction=asc`
**ADMIN** - List all users (paginated)

### GET `/api/users/{id}`
**ADMIN** - Get user by ID

### POST `/api/users`
**ADMIN** - Create user

### PUT `/api/users/{id}`
**ADMIN** - Update user

### DELETE `/api/users/{id}`
**ADMIN** - Deactivate user

### POST `/api/users/{id}/activate`
**ADMIN** - Activate user

---


## Image Endpoints

### POST `/api/images/upload`
**Public** - Upload a product image

**Request:** `multipart/form-data` with `file` field
**Allowed types:** JPEG, PNG, WebP, GIF (max 5 MB)

**Response (200):**
```json
{
  "url": "/api/images/abc-123.jpg",
  "filename": "abc-123.jpg"
}
```

### GET `/api/images/{filename}`
**Public** - Retrieve an uploaded image

**Response:** Raw image bytes with appropriate `Content-Type`

---

## Barcode Endpoints

### GET `/api/barcodes/qr/{productId}?size=250`
**Public** - Generate QR code PNG for a product ID

**Query parameters:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `size` | `250` | Image size in pixels |

**Response:** PNG image with QR code and product ID text

### GET `/api/barcodes/code128/{productId}?width=300&height=80`
**Public** - Generate Code128 barcode PNG for a product ID

**Query parameters:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `width` | `300` | Image width in pixels |
| `height` | `80` | Image height in pixels |

**Response:** PNG image with Code128 barcode and product ID text

---

## Rate Limiting

All API endpoints (except health checks, images, barcodes, and Swagger) are rate-limited:

| Header | Description |
|--------|-------------|
| `X-RateLimit-Limit` | Maximum requests per window (default: 100) |
| `X-RateLimit-Remaining` | Remaining requests in current window |
| `Retry-After` | Seconds to wait before retrying (on 429) |

**Default:** 100 requests per 60-second sliding window per IP+endpoint.

---

## Swagger UI

### GET `/swagger-ui.html`
**Public** - Interactive API documentation (Swagger UI)

### GET `/v3/api-docs`
**Public** - OpenAPI 3.0 schema (JSON)
## Actuator Endpoints

### GET `/actuator/health`
**Public** - Application health status

### GET `/actuator/info`
**Public** - Application info

### GET `/actuator/metrics`
**Public** - Application metrics

---

## Supplier Endpoints

### GET `/api/suppliers`
**Admin, Manager** - List all suppliers (paginated)

### GET `/api/suppliers/active`
**Admin, Manager** - List active suppliers

### GET `/api/suppliers/{id}`
**Admin, Manager** - Get supplier by ID

### POST `/api/suppliers`
**Admin, Manager** - Create supplier

### PUT `/api/suppliers/{id}`
**Admin, Manager** - Update supplier

### DELETE `/api/suppliers/{id}`
**Admin, Manager** - Deactivate supplier

---

## Stock Movement Endpoints

### GET `/api/stock-movements/product/{productId}`
**Authenticated** - Get stock movement history for a product

---

## Business Finance Endpoints

### GET `/api/finance`
**Admin, Manager** - List all business transactions

### POST `/api/finance`
**Admin, Manager** - Record a business transaction

**Transaction Types:** `STIPEND`, `CASH_INJECTED`, `MONEY_COLLECTED`, `OTHER_INCOME`, `EXPENSE`, `TRANSPORT`, `MARKETING`, `RENT`, `UTILITIES`

### DELETE `/api/finance/{id}`
**Admin, Manager** - Delete a transaction

### GET `/api/finance/summary`
**Admin, Manager** - Financial summary (income vs expenses, net position)

### GET `/api/finance/range?start=...&end=...`
**Admin, Manager** - Transactions by date range

---

## Purchase Receipt Endpoints

### GET `/api/receipts`
**Admin, Manager** - List all receipts (paginated)

### GET `/api/receipts/{id}`
**Admin, Manager** - Get receipt by ID

### GET `/api/receipts/pending`
**Admin, Manager** - Get pending receipts

### POST `/api/receipts/scan`
**Admin, Manager** - Scan a receipt image with OCR

**Request:** `multipart/form-data` with `file` field (image)
**Response:** Extracted data (supplier, items, total, raw OCR text)

### POST `/api/receipts`
**Admin, Manager** - Create a receipt from extracted data

### PUT `/api/receipts/{id}/items`
**Admin, Manager** - Update receipt line items

### PUT `/api/receipts/{id}/process`
**Admin, Manager** - Mark receipt as processed (creates products, updates stock)

### PUT `/api/receipts/{id}/reject`
**Admin, Manager** - Reject a receipt

---

## Payment Endpoints

### GET `/api/payments/customer/{customerId}`
**Authenticated** - Get payment history for a customer

### POST `/api/payments`
**Authenticated** - Record a payment

**Request:**
```json
{
  "customerId": 1,
  "saleId": 1,
  "amount": 500.00,
  "paymentMethod": "CASH",
  "notes": "Partial payment"
}
```

---

## Stock Purchase Planning Endpoints

### POST `/api/planning/calculate`
**Admin, Manager** - Calculate purchase plan totals

**Request:**
```json
[
  { "supplier": "Supplier A", "product": "Perfume X", "quantity": 10, "costPerItem": 150, "sellingPrice": 300 }
]
```

### POST `/api/planning/simulate`
**Admin, Manager** - Simulate next month purchase plan

---

## Enhanced Reports Endpoints

### GET `/api/v2/reports/admin/dashboard`
**Admin** - Full admin dashboard with KPIs

### GET `/api/v2/reports/manager/dashboard`
**Admin, Manager** - Manager financial/inventory dashboard

### GET `/api/v2/reports/sales/dashboard`
**All** - Sales dashboard with today/week/month stats

### GET `/api/v2/reports/trend`
**All** - 7-day sales trend data

### GET `/api/v2/reports/expenses/breakdown`
**All** - Expense breakdown by category

### GET `/api/v2/reports/inventory`
**All** - Inventory report (by category, stock levels)

### GET `/api/v2/reports/debt`
**All** - Debt report with debtor details

### GET `/api/v2/reports/daily|weekly|monthly|yearly`
**All** - Period reports with profit calculation
