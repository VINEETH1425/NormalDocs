from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

import models
import crud
from database import engine, SessionLocal
from routers import categories_router, products_router, stores_router, inventory_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    models.Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        crud.seed_initial_data(db)
    finally:
        db.close()
    yield


app = FastAPI(
    title="StoreConnect - Store Inventory Service",
    description="Python FastAPI implementation of the Store & Inventory microservice.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(categories_router)
app.include_router(products_router)
app.include_router(stores_router)
app.include_router(inventory_router)


@app.get("/")
async def root():
    return {
        "service": "store-inventory-service",
        "status": "UP",
        "docs_url": "/docs",
        "redoc_url": "/redoc"
    }


@app.get("/v3/api-docs")
async def get_openapi_spec():
    return app.openapi()
