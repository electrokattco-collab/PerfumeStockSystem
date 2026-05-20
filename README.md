# 🌸 Perfume Stock Management System

A full-stack, production-ready web application for managing perfume inventory and sales. Transformed from a Java console application into a modern web-based system with Spring Boot backend and React frontend.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start (Docker)](#quick-start-docker)
- [Local Development Setup](#local-development-setup)
- [Default Credentials](#default-credentials)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)

## ✨ Features

### Core Features
- **Product Management**: Add, edit, delete products with 4 pricing tiers (Retail, Rewards, Gold, VIP)
- **Inventory Tracking**: Real-time stock tracking with low-stock alerts
- **Sales Recording**: Record sales with automatic price calculation by customer tier
- **Reporting**: Dashboard with profit summaries, sales history, and stock valuation
- **User Management**: Role-based access control (Admin, Manager, Sales Rep)

### User Roles
| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access - Manage users, products, view all reports |
| **MANAGER** | Manage products, view reports, manage inventory |
| **SALES_REP** | Record sales, view inventory (read-only) |

## 🛠️ Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.x**
  - Spring Security with JWT authentication
  - Spring Data JPA
  - Spring Validation
- **PostgreSQL** (database)
- **Flyway** (database migrations)
- **Maven** (build tool)

### Frontend
- **React 18**
- **TypeScript**
- **Vite** (build tool)
- **Tailwind CSS**
- **shadcn/ui** (component library)
- **Axios** (HTTP client)

### Infrastructure
- **Docker** & **Docker Compose**
- **Nginx** (frontend server)

## 📦 Prerequisites

- [Docker](https://docs.docker.com/get-docker/) (for production deployment)
- [Java 21](https://adoptium.net/) (for local backend development)
- [Node.js 20](https://nodejs.org/) (for local frontend development)
- [Maven](https://maven.apache.org/) (for building backend)

## 🚀 Quick Start (Docker)

The easiest way to run the application is using Docker Compose:

1. **Clone the repository** (if not already done)
2. **Copy environment variables:**
   ```bash
   cp .env.example .env
   ```
3. **Edit `.env` file** with your preferred settings (especially database password and JWT secret)
4. **Start the application:**
   ```bash
   docker compose up --build
   ```
5. **Access the application:**
   - Frontend: http://localhost
   - Backend API: http://localhost:8080/api

6. **To stop the application:**
   ```bash
   docker compose down
   # To remove volumes (deletes database data):
   docker compose down -v
   ```

## 💻 Local Development Setup

### Backend Development

1. **Navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Create `application-local.properties` for local development:**
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/perfumestock
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   spring.jpa.hibernate.ddl-auto=validate
   spring.flyway.enabled=true
   jwt.secret=localdevsecret
   ```

3. **Start PostgreSQL** (you can use Docker):
   ```bash
   docker run -d --name postgres-dev \
     -e POSTGRES_DB=perfumestock \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 postgres:16-alpine
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   # or on Windows:
   mvnw.cmd spring-boot:run
   ```

   The backend will be available at http://localhost:8080

### Frontend Development

1. **Navigate to the frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Create `.env.local` file:**
   ```env
   VITE_API_URL=http://localhost:8080/api
   ```

4. **Start the development server:**
   ```bash
   npm run dev
   ```

   The frontend will be available at http://localhost:5173

## 🔐 Default Credentials

After the application starts, you can log in with these default accounts:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `manager` | `manager123` | MANAGER |
| `sales` | `sales123` | SALES_REP |

**⚠️ Important**: Change these passwords in production!

## 📚 API Documentation

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login with username/password |
| POST | `/api/auth/register` | Create new user (Admin only) |

### Products

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/products` | List all products | All authenticated |
| GET | `/api/products/{id}` | Get product by ID | All authenticated |
| GET | `/api/products/search?name=...&category=...` | Search products | All authenticated |
| GET | `/api/products/lowstock` | Get low stock products | All authenticated |
| POST | `/api/products` | Create product | ADMIN, MANAGER |
| PUT | `/api/products/{id}` | Update product | ADMIN, MANAGER |
| DELETE | `/api/products/{id}` | Delete product | ADMIN |

### Sales

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/sales` | List all sales | ADMIN, MANAGER |
| POST | `/api/sales` | Record a sale | All authenticated |
| GET | `/api/sales/today` | Get today's sales | All authenticated |

### Reports

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/reports/dashboard` | Dashboard summary | All authenticated |
| GET | `/api/reports/profit` | Profit report | ADMIN, MANAGER |
| GET | `/api/reports/lowstock` | Low stock report | ADMIN, MANAGER |

### Users

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/users` | List all users | ADMIN |
| POST | `/api/users` | Create user | ADMIN |
| PUT | `/api/users/{id}` | Update user | ADMIN |
| DELETE | `/api/users/{id}` | Deactivate user | ADMIN |
| POST | `/api/users/{id}/activate` | Activate user | ADMIN |

## 📁 Project Structure

```
perfume-stock-system/
├── backend/                  # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/perfumestock/backend/
│   │   │   │   ├── BackendApplication.java
│   │   │   │   ├── config/         # Configuration classes
│   │   │   │   ├── controller/     # REST controllers
│   │   │   │   ├── dto/            # Data transfer objects
│   │   │   │   ├── entity/         # JPA entities
│   │   │   │   ├── repository/     # Spring Data repositories
│   │   │   │   ├── security/       # JWT & security
│   │   │   │   └── service/        # Business logic
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/   # Flyway migrations
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                 # React frontend
│   ├── src/
│   │   ├── components/
│   │   │   ├── ui/           # UI components (shadcn)
│   │   │   └── Layout.tsx
│   │   ├── context/
│   │   │   └── AuthContext.tsx
│   │   ├── hooks/
│   │   │   └── use-toast.ts
│   │   ├── pages/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Inventory.tsx
│   │   │   ├── Login.tsx
│   │   │   ├── RecordSale.tsx
│   │   │   ├── Reports.tsx
│   │   │   ├── SalesHistory.tsx
│   │   │   └── Users.tsx
│   │   ├── services/
│   │   │   └── api.ts
│   │   ├── types/
│   │   │   └── index.ts
│   │   ├── App.tsx
│   │   ├── index.css
│   │   └── main.tsx
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.ts
├── docker-compose.yml
├── .env.example
└── README.md
```

## 🔧 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_NAME` | PostgreSQL database name | `perfumestock` |
| `DB_USER` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `JWT_SECRET` | Secret key for JWT signing | (required) |
| `JWT_EXPIRATION` | JWT token expiration (ms) | `86400000` (24h) |
| `CORS_ORIGINS` | Allowed CORS origins | `http://localhost:80` |

## 🧪 Running Tests

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📝 License

This project is open-source and available under the MIT License.

## 👤 Author

**Katlego Plessie**  
Systems Development Student | Aspiring Software Developer

---

**Note**: This project was transformed from a Java console application to a full-stack web application for educational and portfolio purposes.
