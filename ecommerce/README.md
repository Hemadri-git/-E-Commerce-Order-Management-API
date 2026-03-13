# ⚡ E-Commerce Order Management API

> A production-grade Spring Boot backend with JWT authentication, full order lifecycle tracking, automatic inventory sync, and a polished frontend dashboard.

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot 3.2 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Database | MySQL 8+ with JPA/Hibernate |
| ORM | Spring Data JPA (OneToMany, ManyToMany) |
| Build | Maven |
| Frontend | Vanilla HTML/CSS/JS (zero dependencies) |

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based stateless authentication
- Two roles: **ADMIN** and **CUSTOMER**
- Admins: full product/category/order management
- Customers: browse, cart, and order placement

### 🛍️ Product Catalog
- Paginated product listing with Hibernate query optimization
- Advanced search & filter by name, category, min/max price
- Soft delete (products deactivated, not erased)
- Multi-category support via ManyToMany relationship

### 🛒 Shopping Cart
- Per-user persistent cart (OneToOne with User)
- Add, update quantity, remove items
- Real-time stock validation on every cart operation

### 📦 Order Lifecycle
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
    ↘         ↘
   CANCELLED  CANCELLED
```
- Atomic inventory deduction on order confirmation
- Stock restoration on cancellation
- State machine validation for invalid transitions

### 🔄 Inventory Sync
- Stock levels update atomically when orders are placed
- Prevents overselling with transaction-scoped checks
- Automatic restoration when orders are cancelled

---

## 📁 Project Structure

```
ecommerce/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ecommerce/
│       │   ├── EcommerceApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java       # JWT + CORS setup
│       │   │   └── DataInitializer.java      # Seeds admin, customer, products
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── ProductController.java
│       │   │   ├── CategoryController.java
│       │   │   ├── CartController.java
│       │   │   └── OrderController.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── ProductService.java
│       │   │   ├── CategoryService.java
│       │   │   ├── CartService.java
│       │   │   └── OrderService.java         # Atomic stock sync
│       │   ├── model/
│       │   │   ├── User.java, Role.java
│       │   │   ├── Product.java, Category.java
│       │   │   ├── Cart.java, CartItem.java
│       │   │   └── Order.java, OrderItem.java, OrderStatus.java
│       │   ├── repository/
│       │   │   └── *Repository.java          # JPA + custom JPQL queries
│       │   ├── dto/
│       │   │   ├── AuthDTOs.java, ApiResponse.java
│       │   │   ├── ProductDTOs.java, CartDTOs.java, OrderDTOs.java
│       │   ├── security/
│       │   │   ├── JwtUtils.java             # Token generation & validation
│       │   │   ├── JwtAuthFilter.java        # Per-request filter
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       ├── ResourceNotFoundException.java
│       │       └── BadRequestException.java
│       └── resources/
│           ├── application.properties
│           └── data.sql                      # Seed categories
└── frontend/
    └── index.html                            # Single-file SPA dashboard
```

---

## 🚀 Setup & Run

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

---

### Step 1 — Clone the repository

```bash
git clone <your-repo-url>
cd ecommerce
```

---

### Step 2 — Configure MySQL

Create a MySQL database (or let Spring auto-create it):

```sql
CREATE DATABASE ecommerce_db;
```

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE
```

---

### Step 3 — Build & Run the Backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

The API will start at: **http://localhost:8080**

On first run, it automatically seeds:
- ✅ Admin account: `admin / admin123`
- ✅ Customer account: `customer / customer123`
- ✅ 5 categories + 12 sample products

---

### Step 4 — Open the Frontend

Open `frontend/index.html` directly in your browser (no server needed):

```bash
# macOS
open frontend/index.html

# Linux
xdg-open frontend/index.html

# Windows
start frontend/index.html
```

> Or use VS Code Live Server extension for hot reload.

---

## 🔑 Default Credentials

| Role | Username | Password | Access |
|------|----------|----------|--------|
| Admin | `admin` | `admin123` | Full access — manage products, categories, all orders |
| Customer | `customer` | `customer123` | Browse, cart, own orders |

---

## 📡 API Reference

### Authentication — `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register new customer |
| POST | `/api/auth/login` | Public | Login, returns JWT token |

**Login Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGci...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

Use the token in subsequent requests:
```
Authorization: Bearer eyJhbGci...
```

---

### Products — `/api/products`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products` | Public | Paginated product list |
| GET | `/api/products/{id}` | Public | Single product |
| GET | `/api/products/search` | Public | Filter by name/category/price |
| POST | `/api/products` | Admin | Create product |
| PUT | `/api/products/{id}` | Admin | Update product |
| DELETE | `/api/products/{id}` | Admin | Soft-delete product |

**Search Query Params:**
```
GET /api/products/search?name=phone&categoryId=1&minPrice=100&maxPrice=999&page=0&size=10&sortBy=price&sortDir=asc
```

**Create Product:**
```json
{
  "name": "MacBook Pro",
  "description": "Apple Silicon M3 Pro",
  "price": 1999.99,
  "stockQuantity": 25,
  "imageUrl": "https://example.com/img.jpg",
  "categoryIds": [1]
}
```

---

### Categories — `/api/categories`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/categories` | Public | All categories |
| GET | `/api/categories/{id}` | Public | Single category |
| POST | `/api/categories` | Admin | Create category |
| PUT | `/api/categories/{id}` | Admin | Update category |
| DELETE | `/api/categories/{id}` | Admin | Delete category |

---

### Cart — `/api/cart`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/cart` | Customer | View cart |
| POST | `/api/cart/items` | Customer | Add item |
| PUT | `/api/cart/items/{itemId}` | Customer | Update quantity |
| DELETE | `/api/cart/items/{itemId}` | Customer | Remove item |
| DELETE | `/api/cart` | Customer | Clear cart |

**Add to Cart:**
```json
{
  "productId": 3,
  "quantity": 2
}
```

---

### Orders — `/api/orders`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/orders` | Customer | Place order from cart |
| GET | `/api/orders/my-orders` | Customer | My order history |
| GET | `/api/orders/my-orders/{id}` | Customer | Single order |
| GET | `/api/orders` | Admin | All orders (paginated) |
| GET | `/api/orders/{id}` | Admin | Any order by ID |
| PUT | `/api/orders/{id}/status` | Admin | Update order status |

**Place Order:**
```json
{
  "shippingAddress": "123 Main Street, Hyderabad, India"
}
```

**Update Order Status (Admin):**
```json
{
  "status": "CONFIRMED"
}
```

---

## 🗄️ Database Schema

```
users (id, username, email, password, role, active, created_at)
    │
    ├─── carts (id, user_id)
    │        └─── cart_items (id, cart_id, product_id, quantity)
    │
    └─── orders (id, user_id, status, total_amount, shipping_address, ...)
             └─── order_items (id, order_id, product_id, quantity, unit_price, subtotal)

products (id, name, description, price, stock_quantity, image_url, active, ...)
    └─── product_categories (product_id, category_id) ──── categories (id, name, description)
```

---

## 🧪 Testing with curl

```bash
# 1. Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. Get all products
curl -s http://localhost:8080/api/products | python3 -m json.tool

# 3. Add to cart (customer token)
curl -s -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# 4. Place order
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"123 Main Street"}'

# 5. Update order status (admin)
curl -s -X PUT http://localhost:8080/api/orders/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"CONFIRMED"}'
```

---

## 🛡️ Security Notes

- JWT secret is configurable in `application.properties` (use a strong, random key in production)
- Passwords are hashed with BCrypt
- All admin endpoints require `ROLE_ADMIN`
- CORS is open in development — restrict origins in production
- Token expiry: 24 hours (configurable via `jwt.expiration`)

---

## ⚙️ Configuration Reference

```properties
# Change server port
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# JWT
jwt.secret=your_256_bit_secret_key_here
jwt.expiration=86400000   # 24 hours in milliseconds

# Show SQL queries (disable in production)
spring.jpa.show-sql=true
```

---

## 📝 License

MIT — free to use, fork, and build upon.
