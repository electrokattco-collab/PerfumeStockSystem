# ArthurFord Business Manager

A complete digital business management system for an ArthurFord Gold Agent operating in South Africa.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.2.5, Spring Security (JWT), JPA/Hibernate, PostgreSQL, Flyway
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS, shadcn/ui, Recharts
- **OCR**: Tesseract via Tess4j
- **Deployment**: Docker, Render.com

## Features

- **Product Management** — Gold Agent pricing, product codes, category-based catalog, combo/bundle support
- **Combo Auto-Expansion** — Purchase "Combo Black #005 × 4" and inventory auto-expands into Perfume + Lotion + Roll-on
- **Purchase Recording** — Record purchases with automatic stock updates and combo expansion
- **Sales** — Fast recording with auto-pricing, payment types (Paid/Partial/Credit), stock deduction
- **Customer Management** — Customer profiles with automatic outstanding balance tracking
- **Payments** — Record payments against customer debt, automatic balance sync
- **Dashboard** — Today/month revenue, profit, cash received, debt, inventory value, low stock alerts
- **Reports** — Daily/Weekly/Monthly/Yearly period reports, inventory report, debt report
- **Purchase Slip OCR** — Upload receipt photos for automatic data extraction (planned)

## Products Supported

| Category | Examples |
|----------|---------|
| 30mL Perfume | Black #005, Emerald #004, Coral #003 |
| 50mL Perfume | Wild For Her #004, Blue #001 |
| EDT | Various |
| Lotion | Black #005, etc. |
| Roll-on | Black #005, etc. |
| Combo | Combo Black #005 (Perfume + Lotion + Roll-on) |
| 3-in-1 Face Wash | Various |
| Face Toner | Various |
| Purelite Face Moisturizer | Various |

## Getting Started

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Docker
```bash
docker-compose up
```

## Default Login

- **Username**: admin
- **Password**: admin123

## API Documentation

Backend runs on `http://localhost:8080/api`

### Core Endpoints
- `POST /api/auth/login` — Authenticate
- `GET /api/products` — List products
- `POST /api/purchases` — Record purchase (combo auto-expansion)
- `POST /api/sales` — Record sale
- `GET /api/customers` — List customers
- `POST /api/payments` — Record payment
- `GET /api/reports/dashboard` — Dashboard data
- `GET /api/reports/period/{period}` — Period reports
