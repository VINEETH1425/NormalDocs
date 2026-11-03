# Store Inventory Microservice (Python FastAPI)

Modular, production-ready Python FastAPI microservice for store inventory management.

---

## Architecture

```
backend/store-inventory-service-py/
│
├── database.py              # Database connection and session management
├── models.py                # SQLAlchemy database models
├── schemas.py               # Pydantic request/response validation schemas
│
├── crud/                    # Modular database CRUD operations
│   ├── categories.py        # Category DB operations
│   ├── products.py          # Product DB operations
│   ├── stores.py            # Store DB operations
│   ├── inventory.py         # Inventory & reservation operations
│   ├── seed.py              # Baseline catalog seeding
│   └── __init__.py          # Exports all CRUD methods
│
├── routers/                 # Modular API endpoints
│   ├── categories.py        # /api/categories
│   ├── products.py          # /api/products
│   ├── stores.py            # /api/stores
│   ├── inventory.py         # /api/inventory & /internal/inventory
│   └── __init__.py          # Exports all routers
│
├── main.py                  # FastAPI application entry point
├── test_service.py          # Automated verification test suite
└── requirements.txt         # Dependencies
```

---

## Setup & Running

### 1. Create & Activate Virtual Environment
```powershell
python -m venv venv
.\venv\Scripts\activate
```

### 2. Install Dependencies
```powershell
pip install -r requirements.txt
```

### 3. Run Development Server
```powershell
fastapi dev main.py --port 8082
```

### 4. Run Automated Test Suite
```powershell
python test_service.py
```

### 5. API Documentation
- Swagger UI: `http://127.0.0.1:8082/docs`
- ReDoc: `http://127.0.0.1:8082/redoc`
- OpenAPI JSON: `http://127.0.0.1:8082/v3/api-docs`
