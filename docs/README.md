# StoreConnect – Documentation Suite Index

**Project:** StoreConnect Omnichannel Retail Platform  
**Architecture Scope:** 9-Table Relational Schema (`PostgreSQL` / `ANSI SQL`) & MongoDB Audit Trail  
**Sprint 1 Delivery Checkpoint:** Foundation & Data Layer  

---

## 1. Relational Database & Entity Modeling (9-Table System)

All schema documents, SQL scripts, and diagrams are synchronized with the 9 normalized tables:
1. `stores` (Retail physical stores)
2. `categories` (Catalog departments)
3. `products` (Catalog items & base pricing)
4. `store_inventory` (Store-level physical shelf stock)
5. `customers` (Registered customer accounts)
6. `carts` (Active customer shopping basket, 1:1 with customer)
7. `cart_items` (Line items inside active cart, 1:N with carts)
8. `orders` (Confirmed customer orders, aggregate root)
9. `order_items` (Line items with snapshot purchase price, 1:N with orders)

---

## 2. Master Documentation Directory

| Document | Format | Description & Scope |
|:---|:---:|:---|
| [`StoreConnect_Traditional_ER_Diagram.png`](file:///d:/PS_CPS_Project/docs/StoreConnect_Traditional_ER_Diagram.png) | PNG (High DPI) | **Pure Peter Chen ER Diagram** (All 9 entities, 47 attributes, 11 relationships, zero headings/clutter). |
| [`traditional-er-diagram.html`](file:///d:/PS_CPS_Project/docs/traditional-er-diagram.html) | HTML | Standalone, borderless viewer displaying purely the Peter Chen ER diagram. |
| [`traditional-er-diagram.md`](file:///d:/PS_CPS_Project/docs/traditional-er-diagram.md) | Markdown | GeeksforGeeks / Peter Chen ER theory, complete attribute inventory, and cardinality mapping. |
| [`er-diagram.md`](file:///d:/PS_CPS_Project/docs/er-diagram.md) | Markdown | Visual Flowchart LR and standard Crow's Foot ERD in Mermaid covering all 9 tables. |
| [`database-fields-specification.md`](file:///d:/PS_CPS_Project/docs/database-fields-specification.md) | Markdown | Comprehensive field specification (data types, nullability, PK/FK, check constraints) for all 9 tables. |
| [`database-data-samples.md`](file:///d:/PS_CPS_Project/docs/database-data-samples.md) | Markdown | Confirmed seed data dictionary, sample rows, and integrity verification across all 9 tables. |
| [`storeconnect_ssms_setup.sql`](file:///d:/PS_CPS_Project/docs/storeconnect_ssms_setup.sql) | T-SQL | Ready-to-run SSMS DDL with explicit PK/FK constraints to generate visual diagrams in SQL Server. |
| [`storeconnect_pgadmin_setup.sql`](file:///d:/PS_CPS_Project/docs/storeconnect_pgadmin_setup.sql) | PL/pgSQL | PostgreSQL setup script with tables, constraints, and seed data for pgAdmin. |
| [`classes-methods-specification.md`](file:///d:/PS_CPS_Project/docs/classes-methods-specification.md) | Markdown | Object-oriented domain classes and business service methods (including `Cart` & `CartItem`). |
| [`class-diagram.md`](file:///d:/PS_CPS_Project/docs/class-diagram.md) | Mermaid | Class diagram defining entities, attributes, methods, and relationships. |
| [`activity-diagram.md`](file:///d:/PS_CPS_Project/docs/activity-diagram.md) | Mermaid | Customer order placement flow and store fulfillment flow. |
| [`problem-understanding.md`](file:///d:/PS_CPS_Project/docs/problem-understanding.md) | Markdown | Executive summary, business problem, scope boundaries, personas, and architectural patterns. |
| [`jira-subtasks-summary.md`](file:///d:/PS_CPS_Project/docs/jira-subtasks-summary.md) | Markdown | Ready-to-copy Bitbucket comments and Jira subtask deliverables for Sprint 1. |
| [`StoreConnect_UML_Diagrams.html`](file:///d:/PS_CPS_Project/docs/StoreConnect_UML_Diagrams.html) | HTML | Interactive multi-section visual suite presenting all UML and ER diagrams. |
