# StoreConnect – Jira Story & Subtasks Reference Guide

This document contains copy-paste ready text for your **Jira Story**, **Story-Level Comments**, and each of the **6 Subtasks** with direct links to the documentation in your Bitbucket repository.

---

## 📌 1. Jira Story Level Details

### Story Title:
`StoreConnect Sprint 1: Omnichannel Retail Foundation & Data Layer Architecture`

### Story Description:
```markdown
h2. Business Context & Objective
StoreConnect is a progressive omnichannel retail platform connecting customer digital shopping experiences with physical store-level inventory operations and order fulfillment.

During Sprint 1 (Days 1–5), the primary objective is establishing the architectural foundation and data layer:
1. Object-Oriented Domain Analysis & UML Modeling (Class Diagrams, Activity Diagrams, Order State Machine).
2. Relational Database Design (PostgreSQL schema, entity relationships, fields specification).
3. Omnichannel Inventory Architecture (Decoupled store-level stock isolation to prevent overselling).
4. Concrete Seed Dataset (5 Stores, 4 Categories, 12 Products, 60 Inventory Rows, 5 Customers).
5. Comprehensive Technical Documentation committed under the `docs/` repository directory.

All documentation artifacts have been verified and pushed to the repository to support the Sprint 1 checkpoint.
```

---

### Story-Level Comment ("Long Story from Last Week"):
*(Copy and paste this into the main Jira Story Comments section)*

```markdown
### 📢 Sprint 1 Summary & Architectural Milestone (Week 1 Review)

Over the past week, we completed the core foundation and data layer architecture for StoreConnect in accordance with the CPS requirements:

1. **Problem Decomposition & Domain Analysis:**
   - Addressed the core business challenge: Disconnected inventory where online customers see products available even when their local physical store is out of stock.
   - Formalized 4 key user roles: Customer, Store Operator, Retail Administrator, and System.
   - Mapped out the end-to-end omnichannel shopping journey, store fulfillment workflow, and order cancellation flow using UML Activity Diagrams.

2. **Decoupled Omnichannel Data Architecture:**
   - Rather than storing stock directly on the product record, we architected a composite `store_inventory` model (`store_id`, `product_id`). This enables real-time stock lookup per store location (e.g. Gurugram CyberHub vs. Ambience Mall vs. Delhi CP).
   - Designed atomic stock reservation rules (`quantity_available` decrements, `quantity_reserved` increments inside a transactional boundary) guarded by check constraints to eliminate race conditions.

3. **Confirmed Database Schema & Seed Scope:**
   - Confirmed the 7-table PostgreSQL schema (`categories`, `stores`, `products`, `store_inventory`, `customers`, `orders`, `order_items`).
   - Fixed the initial standard dataset for development and demonstration:
     * **5 Physical Stores** across Delhi-NCR (CyberHub, Ambience Mall, CP Flagship, Select Citywalk, Mall of India).
     * **4 Retail Categories** (Audio, Wearables, Computing, Lifestyle).
     * **12 Products** with realistic pricing, SKUs, and specifications.
     * **60 Store Inventory Records** reflecting distinct local stock levels, including intentional out-of-stock items to test availability logic.
     * **5 Test Customers** with realistic regional profiles.
     * **4 Historic Sample Orders** representing orders in COMPLETED, READY_FOR_PICKUP, PROCESSING, and CONFIRMED states.

4. **Order Lifecycle State Machine:**
   - Formalized strict state transitions: `PENDING` → `CONFIRMED` → `PROCESSING` → `READY_FOR_PICKUP` → `COMPLETED` / `CANCELLED`.
   - Prohibited invalid transitions with domain validation exceptions.
   - Wired automated stock restoration on order cancellation.

5. **NoSQL Secondary Audit Logging:**
   - Implemented a MongoDB audit document collection (`audit_events`) capturing non-blocking chronological event trails (`ORDER_PLACED`, `ORDER_STATUS_CHANGED`, `INVENTORY_RESTORED`).

6. **Documentation Suite:**
   - All design specifications, diagrams, data dictionaries, and method signatures have been published in the repository `docs/` folder, ready for review.

Sprint 1 foundation is complete and sets the baseline for Sprint 2 (Spring Boot REST APIs and layered service boundaries).
```

---

## 📋 2. The 6 Jira Subtasks (Copy-Paste Reference)

---

### Subtask 1: UML Activity Diagrams
- **Summary:** `ST-UML-01: Create UML Activity Diagrams for Omnichannel Ordering & Fulfillment`
- **Description:**
  ```markdown
  Create detailed UML Activity Diagrams documenting the end-to-end customer order placement journey, store operator fulfillment workflow (picking/packing/pickup), and order cancellation with automated inventory restoration.
  ```
- **Bitbucket Comment:**
  ```markdown
  ✅ Completed UML Activity Diagrams.
  Artifacts committed to repository:
  - Markdown Specification: docs/activity-diagram.md
  - Interactive Visual Suite: docs/StoreConnect_UML_Diagrams.html (Section 2 & 3)
  Bitbucket Link: https://bitbucket.org/your-workspace/storeconnect/src/main/docs/activity-diagram.md
  ```
- **Local File:** [`docs/activity-diagram.md`](file:///d:/PS_CPS_Project/docs/activity-diagram.md)

---

### Subtask 2: UML Class Diagrams
- **Summary:** `ST-UML-02: Create UML Class Diagrams for Domain Entities & Relationships`
- **Description:**
  ```markdown
  Create object-oriented domain model class diagrams covering Core Entities (Product, Store, Inventory, Category, Customer) and the Cart & Order Subsystem with attribute types, method signatures, and OOP relationships.
  ```
- **Bitbucket Comment:**
  ```markdown
  ✅ Completed UML Class Diagrams.
  Artifacts committed to repository:
  - Markdown Specification: docs/class-diagram.md
  - Interactive Visual Suite: docs/StoreConnect_UML_Diagrams.html (Section 1a, 1b, 1c)
  Bitbucket Link: https://bitbucket.org/your-workspace/storeconnect/src/main/docs/class-diagram.md
  ```
- **Local File:** [`docs/class-diagram.md`](file:///d:/PS_CPS_Project/docs/class-diagram.md)

---

### Subtask 3: ERD Diagrams
- **Summary:** `ST-ERD-01: Create Entity Relationship Diagram (ERD) with Operational Flow`
- **Description:**
  ```markdown
  Create visual Entity Relationship Diagrams displaying the relational database schema, 1:N Crow's Foot cardinalities, Primary/Foreign Key mappings, and left-to-right operational flow (Master Data → Inventory Bridge → Transactions).
  ```
- **Bitbucket Comment:**
  ```markdown
  ✅ Completed Entity Relationship Diagrams (ERD).
  Artifacts committed to repository:
  - Markdown Specification: docs/er-diagram.md
  - Interactive Visual Suite: docs/StoreConnect_UML_Diagrams.html (Section 4a & 4b)
  Bitbucket Link: https://bitbucket.org/your-workspace/storeconnect/src/main/docs/er-diagram.md
  ```
- **Local File:** [`docs/er-diagram.md`](file:///d:/PS_CPS_Project/docs/er-diagram.md)

---

### Subtask 4: Database (Fields) (Doc)
- **Summary:** `ST-DB-01: Document Relational Database Schema & Fields Specification`
- **Description:**
  ```markdown
  Produce a comprehensive database specification document detailing all 9 relational tables (including Carts and Cart Items for persistent basket state), column data types, nullability, primary/foreign keys, check constraints, default values, indexing strategy, and secondary NoSQL MongoDB audit schema.
  ```
- **Bitbucket Comment:**
  ```markdown
  ✅ Completed Database Fields & Schema Specification.
  Artifacts committed to repository:
  - Markdown Specification: docs/database-fields-specification.md
  - SQL DDL Source: src/main/resources/schema.sql
  Bitbucket Link: https://bitbucket.org/your-workspace/storeconnect/src/main/docs/database-fields-specification.md
  ```
- **Local File:** [`docs/database-fields-specification.md`](file:///d:/PS_CPS_Project/docs/database-fields-specification.md)

---

### Subtask 5: Actual Database (Data-Rough)
- **Summary:** `ST-DB-02: Establish Confirmed Seed Data & Data Dictionary Samples`
- **Description:**
  ```markdown
  Finalize and document the confirmed dataset for StoreConnect: 5 physical stores, 4 retail categories, 12 products, 60 inventory rows (with store-level variance), 5 customers, 1 active shopping cart with items, and 4 sample orders demonstrating lifecycle states.
  ```
- **Bitbucket Comment:**
  ```markdown
  ✅ Completed Confirmed Database Seed Data & Sample Tables.
  Artifacts committed to repository:
  - Data Samples Document: docs/database-data-samples.md
  - Runnable SQL Seed Script: src/main/resources/seed_data.sql
  Bitbucket Link: https://bitbucket.org/your-workspace/storeconnect/src/main/docs/database-data-samples.md
  ```
- **Local File:** [`docs/database-data-samples.md`](file:///d:/PS_CPS_Project/docs/database-data-samples.md) & [`src/main/resources/seed_data.sql`](file:///d:/PS_CPS_Project/src/main/resources/seed_data.sql)

---

### Subtask 6: Classes and Its Methods (Relation) (Doc)
- **Summary:** `ST-OO-01: Document Classes, Methods, Signatures & OOP Relations`
- **Description:**
  ```markdown
  Document all domain entities, services, DAOs, enums, and audit classes with attribute types, constructor signatures, method parameters, return types, exception handling, and OOP relationships (Composition, Aggregation, Association).
  ```
- **Bitbucket Comment:**
  ```markdown
  ✅ Completed Classes, Methods & OOP Relations Specification.
  Artifacts committed to repository:
  - Markdown Specification: docs/classes-methods-specification.md
  Bitbucket Link: https://bitbucket.org/your-workspace/storeconnect/src/main/docs/classes-methods-specification.md
  ```
- **Local File:** [`docs/classes-methods-specification.md`](file:///d:/PS_CPS_Project/docs/classes-methods-specification.md)
