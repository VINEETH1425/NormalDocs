# Store Inventory Service (FastAPI)

FastAPI implementation following the `fastapi-withdb` pattern.

## Project Structure

```
backend/store-inventory-service-py/
├── database.py       # Database connection & session management
├── models.py         # SQLAlchemy table models
├── schemas.py        # Pydantic request/response schemas
├── crud.py           # Database CRUD operations
├── main.py           # FastAPI application & endpoints (default port 8082)
├── .env              # Environment configuration
├── .env.example      # Example environment template
├── requirements.txt  # Dependencies
└── README.md
```

## Running the Service

### 1. Install Dependencies
```powershell
pip install -r requirements.txt
```

### 2. Run with Uvicorn
```powershell
python main.py
```
Or via FastAPI CLI:
```powershell
fastapi dev main.py --port 8082
```

### 3. API Documentation
- Swagger UI: `http://localhost:8082/docs`
- ReDoc: `http://localhost:8082/redoc`
