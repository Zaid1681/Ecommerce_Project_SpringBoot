# 🛒 E-Commerce Backend System

A scalable **Spring Boot-based E-Commerce backend** designed with modular architecture and real-world production concepts like caching, messaging, and distributed systems in mind.

---

## 🚀 Project Overview

This project simulates a real-world e-commerce platform backend with core functionalities like:

- Product Management
- Cart Handling
- Order Processing
- Payment Integration (basic)
- Inventory Management (planned)

Built with a focus on **clean architecture, scalability, and extensibility**.

---

## 🧩 Modules Implemented
E-Commerce App
├── Product Module ✅
├── Cart Module ✅
├── Order Module ✅
├── Payment Module ✅ (basic)
└── Database (MySQL/PostgreSQL) ✅


---

## 📦 Features

### ✅ Product Module
- Add / Update / Delete Products
- Fetch all products
- Category mapping (self-referencing)

### ✅ Cart Module
- Add items to cart
- Remove items
- View cart

### ✅ Order Module
- Create order from cart
- Calculate total price
- Maintain order status

### ✅ Payment Module (Basic)
- Simulated payment processing
- Order status updates

---

## 🛠️ Tech Stack

- **Backend:** Spring Boot
- **Database:** MySQL / PostgreSQL
- **ORM:** Hibernate (JPA)
- **Build Tool:** Maven / Gradle
- **Caching:** In-Memory Cache (ConcurrentMapCacheManager)
- **Lombok:** For boilerplate reduction

---

## ⚙️ Architecture

Layered Architecture:
Controller → Service → Repository → Database


- DTO-based communication
- Entity relationships using JPA
- Clean separation of concerns

---

## 🧠 Concepts Used

- REST API Design
- DTO Mapping
- JPA Relationships (OneToMany, ManyToOne)
- Exception Handling
- Stream API
- Caching (In-Memory)

---

## 📌 Upcoming Enhancements

### 🔐 Authentication & Authorization
- JWT-based authentication
- User roles (Admin, Customer)

### ⚡ Redis Caching
- Replace in-memory cache with Redis
- Improve performance & scalability

### 📩 Kafka Integration
- Event-driven architecture
- Order & payment communication

### 📦 Inventory Service
- Stock management
- Concurrency handling

### 💳 Payment Resilience
- Retry mechanism
- Circuit breaker (Resilience4j)

### 🐳 Docker
- Containerization of services

### ☁️ AWS Deployment
- EC2 / RDS
- S3 for storage

---

## 🗂️ Project Structure
com.platform.ecommerce
├── product
├── cart
├── order
├── payment
├── category
└── common

---

## ▶️ How to Run

1. Clone the repository:
   ```bash
   git clone <repo-url>

Configure database in application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=your_password

Run the application:

mvn spring-boot:run

## 🧪 Sample API Endpoints
## 🧪 Sample API Endpoints

| Module  | Endpoint          | Method |
|---------|------------------|--------|
| Product | `/products`      | GET    |
| Product | `/products`      | POST   |
| Cart    | `/cart/add`      | POST   |
| Order   | `/orders/create` | POST   |


### Steps to run code 
- Clone it
- Run ngrok (I used ngrok for making api public, calling webhook of stripe payment gateway)
- run Redis (~ brew services redis ->  for mac) 
- run Backend code
- login and generate token (which will be used as authentication for hitting APIs)
- add item to cart 
- Frontend : click on checkout button (which acts as a checkout button of product cart and hit the /placeOrder APIs) and redirect to payment gateway
- Frontend (Payment Gateway) : fill the credit cart number , cvv, and reqiured details and hit proceed 
   - if -> payment succeeded hits the webhook and complete the order cycle and clear product from cart
   - if -> if the payment fails then save the status as order failed and cart remains as it is 



### Order State transition 
User clicks Place Order

        ↓

Order Created

Status = PLACED

        ↓

Inventory Check

        ↓

Status = CONFIRMED

        ↓

Payment Record Created

        ↓

Status = PAYMENT_PENDING

        ↓
     Customer Pays
        ↓

 ┌───────────────┐
 │ Payment Success│
 └───────────────┘

        ↓

Status = PAID

Payment = SUCCESS



OR



 ┌───────────────┐
 │ Payment Failed │
 └───────────────┘

        ↓

Status = PAYMENT_FAILED

        ↓

Restore Inventory

        ↓

Status = CANCELLED

Payment = FAILED




### Learning Goals
Understand real-world backend architecture
Learn how different modules interact (Product, Cart, Order, Payment)
Gain hands-on experience with Spring Boot and REST APIs
Implement layered architecture (Controller → Service → Repository)
Prepare for scaling with Redis, Kafka, and microservices