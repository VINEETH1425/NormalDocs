# StoreConnect Backend Microservices

##  Problem Statement

Imagine buying a smartphone online and choosing to pick it up two hours later at your nearest mall store (BOPIS: Buy Online, Pick Up in Store). Traditional retail systems often suffer from stock mismatches, where a product appears in stock online but is already sold out on physical shelves. StoreConnect solves this by seamlessly linking online orders with real-time physical store inventory so customers can reserve, pay, and collect items without stock conflicts.

---

## Microservices Overview

| Name of Microservice | Purpose |
| :--- | :--- |
| **API Gateway** | Acts as the single entry point that validates JWT authentication and routes incoming requests to downstream services. |
| **User Service** | Handles customer registration, secure login, JWT issuance, and shopping cart management. |
| **Store Inventory Service** | Manages physical store locations, product catalogs, and real-time store stock levels. |
| **Order Service** | Processes checkout, reserves store inventory via OpenFeign, and tracks order history. |

---

## Roles :

- **Customer**
- **Admin**
- **Store Operator**
