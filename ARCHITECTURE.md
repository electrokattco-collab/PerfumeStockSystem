# Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                          │
│  React 18 + TypeScript + Vite + Tailwind/shadcn/ui      │
│  Mobile-first responsive SPA + PWA                       │
│  ErrorBoundary + LoadingSkeletons + EmptyStates          │
├─────────────────────────────────────────────────────────┤
│                 NGINX (Reverse Proxy)                    │
│  Serves static files + proxies /api → backend:8080      │
├─────────────────────────────────────────────────────────┤
│               SPRING BOOT 3.2.5 (Java 21)               │
│                                                         │
│  ┌───────────────┐  ┌──────────────┐  ┌─────────────┐  │
│  │   Security    │  │ Controllers  │  │  Services   │  │
│  │ JWT/Cookie    │  │ 7 REST APIs  │  │ 6 Services  │  │
│  │ Role-based    │  │ @Valid       │  │ @Transactional│ │
│  │ @PreAuthorize │  │  → DTOs      │  │  Logging    │  │
│  └───────────────┘  └──────────────┘  └─────────────┘  │
│                                                         │
│  ┌───────────────┐  ┌──────────────┐  ┌─────────────┐  │
│  │   Entities    │  │ Repositories │  │  Exceptions │  │
│  │ 7 JPA entities│  │ 6 JPA repos  │  │ 4 custom    │  │
│  │ @PrePersist   │  │ @Query       │  │ + Global    │  │
│  └───────────────┘  └──────────────┘  │ Handler     │  │
│                                       └─────────────┘  │
│  ┌───────────────┐  ┌──────────────┐                    │
│  │ Response DTOs │  │ Request DTOs │                    │
│  │ 6 DTOs        │  │ 5 DTOs       │                    │
│  │ fromEntity()  │  │ @Valid       │                    │
│  └───────────────┘  └──────────────┘                    │
├─────────────────────────────────────────────────────────┤
│  PostgreSQL 16 (Production) / H2 (Dev) + Flyway Migrations│
└─────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.perfumestock.backend
├── config/
│   ├── CorsConfig.java          # CORS with configurable origins
│   └── WebSecurityConfig.java   # Security filter chain + role rules
├── controller/
│   ├── AuthController.java      # Login, logout, register, /me
│   ├── CustomerController.java  # Customer CRUD → CustomerResponse
│   ├── ExpenseController.java   # Expense CRUD → ExpenseResponse
│   ├── ProductController.java   # Product CRUD + search → ProductResponse
│   ├── ReportController.java    # Dashboard, profit, daily/weekly/monthly
│   ├── SaleController.java      # Sale CRUD + mark-as-paid → SaleResponse
│   └── UserController.java      # User CRUD (ADMIN only) → UserResponse
├── dto/
│   ├── ErrorResponse.java       # Structured error response
│   ├── JwtResponse.java         # Auth response (no token in body)
│   ├── LoginRequest.java        # Login credentials
│   ├── MessageResponse.java     # Simple message response
│   ├── ProductRequest.java      # Product create/update
│   ├── ProductResponse.java     # Product response DTO
│   ├── CustomerResponse.java    # Customer response DTO
│   ├── ExpenseResponse.java     # Expense response DTO
│   ├── SaleItemRequest.java     # Individual sale line item
│   ├── SaleItemResponse.java    # Sale item response DTO
│   ├── SaleRequest.java         # Sale create/update
│   ├── SaleResponse.java        # Sale response DTO
│   ├── UserRequest.java         # User create/update
│   └── UserResponse.java        # User response DTO (no password)
├── entity/
│   ├── Customer.java            # Customer with outstanding balance
│   ├── Expense.java             # Expense tracking
│   ├── Product.java             # Product with stock management
│   ├── Purchase.java            # Purchase/batch tracking
│   ├── Sale.java                # Sale with multi-item support
│   ├── SaleItem.java            # Individual sale line item
│   └── User.java                # User with role enum
├── exception/
│   ├── BusinessRuleException.java
│   ├── DuplicateResourceException.java
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── InsufficientStockException.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── CustomerRepository.java
│   ├── ExpenseRepository.java
│   ├── ProductRepository.java
│   ├── PurchaseRepository.java
│   ├── SaleRepository.java
│   └── UserRepository.java
├── security/
│   ├── AuthEntryPointJwt.java   # 401 handler
│   ├── AuthTokenFilter.java     # JWT cookie → SecurityContext
│   ├── JwtUtils.java            # Token generation/validation
│   ├── UserDetailsImpl.java     # Principal implementation
│   └── UserDetailsServiceImpl.java  # DB-backed UserDetailsService
└── service/
    ├── CustomerService.java     # Customer CRUD + balance management
    ├── ExpenseService.java      # Expense CRUD
    ├── ProductService.java      # Product CRUD + search
    ├── ReportService.java       # Dashboard/reports aggregation
    ├── SaleService.java         # Sale recording + stock management
    └── UserService.java         # User CRUD + activation
```

## API Response Format

All API endpoints return consistent response formats:

### Success Response (single entity)
```json
{
  "id": 1,
  "name": "50mL Superior Perfume",
  "category": "Perfume",
  "sellPrice": 199.00,
  "stockQuantity": 10,
  "isLowStock": false,
  "createdAt": "2026-07-19T18:00:00"
}
```

### Success Response (list)
```json
[
  { "id": 1, "name": "Product A", ... },
  { "id": 2, "name": "Product B", ... }
]
```

### Error Response
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "username: size must be between 3 and 50",
  "path": "/api/auth/register",
  "timestamp": "2026-07-19T18:00:00",
  "fieldErrors": [
    { "field": "username", "message": "size must be between 3 and 50", "rejectedValue": "ab" }
  ]
}
```

## Security

### Authentication Flow
1. Client sends POST `/api/auth/login` with credentials
2. Server validates credentials via `AuthenticationManager`
3. JWT generated with claims: `id`, `email`, `role`
4. JWT set as httpOnly cookie (`SameSite=Lax`, `Secure=true`)
5. Client receives user info in response body (no token in body)

### Authorization Flow
1. Request arrives → `AuthTokenFilter` extracts JWT from cookie
2. JWT validated → `UserDetails` loaded → `SecurityContextHolder` populated
3. `WebSecurityConfig` role rules evaluated
4. `@PreAuthorize` on methods checked if `@EnableMethodSecurity` is active

## Data Model

```
User (id, username, email, password, role, active)
  └── records sales

Product (id, productId, name, category, size, buyPrice, sellPrice, stockQuantity, lowStockThreshold)
  ├── has many Sales
  └── has many Purchases

Sale (id, saleId, productName, quantity, unitPrice, costOfGoodsSold, paid, amountOwing)
  ├── belongs to Product
  ├── belongs to User (recordedBy)
  ├── belongs to Customer
  └── has many SaleItems

SaleItem (id, productName, quantity, unitPrice, lineTotal)
  └── belongs to Sale

Customer (id, name, phone, outstandingBalance)
  └── has many Sales

Expense (id, category, description, amount, expenseDate)

Purchase (id, purchaseId, productName, category, quantity, unitCost, remainingQuantity)
  └── belongs to Product
```

## PWA Features

- **Manifest**: App metadata, icons, shortcuts for quick access
- **Service Worker**: Cache-first for static assets, network-first for API
- **Offline Support**: Static assets cached for offline viewing
- **Install Prompt**: Users can install as native-like app
- **Theme Color**: Pink gradient (#ec4899)

## Error Response Format

| HTTP Status | Exception | Description |
|-------------|-----------|-------------|
| 400 | `BusinessRuleException` | Business logic violation |
| 400 | `InsufficientStockException` | Not enough stock |
| 400 | `MethodArgumentNotValidException` | Validation error |
| 401 | `BadCredentialsException` | Invalid credentials |
| 403 | `AccessDeniedException` | Insufficient permissions |
| 404 | `ResourceNotFoundException` | Entity not found |
| 409 | `DuplicateResourceException` | Duplicate value |
| 500 | `RuntimeException` | Unexpected server error |
