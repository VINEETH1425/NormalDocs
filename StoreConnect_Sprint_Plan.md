# StoreConnect CPS – Sprint Plan (5-Day Check-in Cycles)

**Project:** StoreConnect – Omnichannel Retail Platform  
**Batch:** PSI-2026 Sep ASDE GGN  
**Start Date:** Saturday, Oct 4, 2026  
**Check-in Cadence:** Every 5 days on **Tuesday**  
**Final Demo:** Day 21  

---

## Timeline Overview

| Sprint | Days | Check-in Date | Theme |
|--------|------|--------------|-------|
| **Sprint 1** | Days 1–5 | **Tue, Oct 7** | Foundation + Data Layer |
| **Sprint 2** | Days 6–10 | **Tue, Oct 14** | Spring Boot + REST APIs + Microservices |
| **Sprint 3** | Days 11–15 | **Tue, Oct 21** | Quality + Kafka + Frontend Foundation |
| **Sprint 4** | Days 16–20 | **Tue, Oct 28** | Full-Stack Integration + E2E + Polish |
| **Demo** | Day 21 | **Wed, Oct 29** | Final CPS Demo |

---

## Sprint 1: Days 1–5 → Check-in Tuesday, Oct 7

**Theme: Foundation + Data Layer**

### Day-by-Day Plan

| Day | Date | Topic | Deliverable |
|-----|------|-------|------------|
| **Day 1** | Sat, Oct 4 | OOAD/UML, Maven/Git | ✅ UML diagrams (class, activity, state), Git repo setup, problem understanding doc |
| **Day 2** | Sun, Oct 5 | Java 25 Domain Model | ✅ **SUBMIT: Class Diagram + Activity Diagram**, implement Product, Store, Customer, Inventory domain objects |
| **Day 3** | Mon, Oct 6 | Collections, Streams & Concurrency | Core business operations (catalog search, inventory checks, cart logic) using streams |
| **Day 4** | Tue, Oct 7 (AM) | PostgreSQL, SQL & JDBC | ERD, SQL schema scripts, JDBC persistence for all domain objects |
| **Day 5** | Tue, Oct 7 | MongoDB & NoSQL | One small MongoDB use case (audit/activity log) |

### Sprint 1 Check-in Deliverables:
- [x] **Class Diagram** (domain entities, relationships, enums) → `docs/class-diagram.md`
- [x] **Activity Diagram** (order placement, fulfillment, cancellation flows) → `docs/activity-diagram.md`
- [x] **Order State Machine** (strict valid status transitions) → `docs/activity-diagram.md`
- [x] **Entity Relationship Diagram (ERD)** (7 tables, PK/FK, 1:N cardinalities) → `docs/er-diagram.md`
- [x] **Problem understanding, assumptions, scope document** → `docs/problem-understanding.md`
- [x] **Working Java domain model** (Product, Store, Customer, Inventory, Order, Cart)
- [x] **Business logic using Java collections & streams** (Catalog, Inventory reservation, Order checkout)
- [x] **PostgreSQL ERD + schema scripts + working JDBC persistence** (`schema.sql`, `seed_data.sql`, DAOs)
- [x] **MongoDB audit/activity log** (`MongoAuditService` with resilient memory fallback)
- [x] **Automated Unit Tests & Live CLI Demo** (6/6 passing, runnable via `mvn compile exec:java`)

---

## Sprint 2: Days 6–10 → Check-in Tuesday, Oct 14

**Theme: Spring Boot Backend + REST APIs + Microservices**

| Day | Topic | Deliverable |
|-----|-------|------------|
| **Day 6** | CI/CD + CPS Work 1 | First working baseline, build workflow, domain/data review |
| **Day 7** | Java APIs & Enterprise Design | Refactor into layers, add DTOs, validation, design patterns |
| **Day 8** | Spring Core / IoC / MVC | Convert to Spring app, dependency injection, proper separation |
| **Day 9** | Spring Boot / REST / OpenAPI | REST APIs for catalog/inventory/orders, Swagger docs, error handling |
| **Day 10** | Microservices / Spring Cloud | Catalog, Inventory, Order service boundaries, inter-service communication |

### Sprint 2 Check-in Deliverables:
- [ ] Spring Boot application running
- [ ] REST APIs: products, inventory, stores, orders, cart
- [ ] Swagger/OpenAPI documentation
- [ ] Layered architecture (Controller → Service → Repository)
- [ ] DTOs, validation, exception handling
- [ ] Service boundaries (Catalog, Inventory, Order)

---

## Sprint 3: Days 11–15 → Check-in Tuesday, Oct 21

**Theme: Quality + Kafka + Frontend Foundation**

| Day | Topic | Deliverable |
|-----|-------|------------|
| **Day 11** | Quality Engineering | JUnit/Mockito tests for business rules & services |
| **Day 12** | Kafka + CPS Work 2 | Kafka event for order/inventory change, backend review |
| **Day 13** | JavaScript & Node.js | Frontend ecosystem setup, npm tooling |
| **Day 14** | TypeScript | Typed models, API contracts, frontend services |
| **Day 15** | React Fundamentals | Product listing, product details, cart components |

### Sprint 3 Check-in Deliverables:
- [ ] Unit tests for critical business rules
- [ ] Kafka event (e.g., OrderConfirmed / InventoryReserved)
- [ ] React + TypeScript project initialized
- [ ] Typed models matching backend DTOs
- [ ] Product listing page, product details page
- [ ] Basic cart component

---

## Sprint 4: Days 16–20 → Check-in Tuesday, Oct 28

**Theme: Full-Stack Integration + E2E Testing + Polish**

| Day | Topic | Deliverable |
|-----|-------|------------|
| **Day 16** | Modern React / API Integration | Connect React to Spring APIs, routing, forms, loading/error states |
| **Day 17** | Redux Toolkit | Shared state for cart/product/order flows |
| **Day 18** | BDD/Selenium/Cucumber + CPS Work 3 | E2E test automation for 1-2 critical journeys |
| **Day 19** | Integration & Stabilization | Fix integration bugs, strengthen tests, logging |
| **Day 20** | Integration & Stabilization | Complete MVP, documentation, business-flow consistency |

### Sprint 4 Check-in Deliverables:
- [ ] React UI connected to Spring APIs end-to-end
- [ ] Redux Toolkit for cart/product/order state
- [ ] Complete customer journey working
- [ ] Admin & Store Operator views
- [ ] Selenium/Cucumber tests for critical flows
- [ ] README with setup, architecture, APIs, demo flow
- [ ] All business rules enforced

---

## Day 21: Final Demo (Oct 29)

### Demo Script:
1. **Business Problem** – Explain the retail problem being solved
2. **Architecture Walkthrough** – Show system design, service boundaries, tech stack
3. **Customer Journey Demo** – Browse → Search → Check Availability → Cart → Order → Track
4. **Store/Inventory Flow** – Operator views, stock updates, fulfillment status changes
5. **API Tour** – Swagger/OpenAPI documentation walkthrough
6. **Kafka Event** – Show the event-driven flow
7. **Test Evidence** – Unit tests, E2E tests
8. **Known Limitations** – What was scoped out and why

---

## Scope Exclusions (What NOT to Build)
- ❌ Docker / Kubernetes / AWS
- ❌ Python (comes after CPS)
- ❌ Real payment gateways
- ❌ Real logistics / shipping
- ❌ Real POS hardware
- ❌ Production-grade infra
- ❌ Advanced analytics

---

## Definition of Done

> A group should demonstrate **one complete business journey:**  
> Customer discovers product → checks store availability → adds to cart → places order  
> → inventory validated/reserved → order associated with store → fulfillment updated  
> → customer sees updated order status  
>  
> Supported by: Spring Boot APIs, React/TypeScript UI, PostgreSQL, service boundaries,  
> automated tests, and one Kafka event.
