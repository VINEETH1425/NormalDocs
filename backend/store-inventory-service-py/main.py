import os
from typing import Optional, List
from dotenv import load_dotenv
from fastapi import FastAPI, Depends, HTTPException, Header, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError

load_dotenv()

from database import get_db, engine, Base
import models
import crud
import schemas

Base.metadata.create_all(bind=engine)

SERVICE_PORT = int(os.getenv("SERVICE_PORT", os.getenv("PORT", os.getenv("INVENTORY_SERVICE_PORT", "8082"))))

app = FastAPI(
    title="Store Inventory API",
    description="Store Inventory Microservice (Python FastAPI)",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def welcome():
    return {
        "service": "store-inventory-service",
        "status": "UP",
        "port": SERVICE_PORT,
        "docs": "/docs"
    }


@app.get("/v3/api-docs")
def openapi_docs():
    return app.openapi()


@app.get("/api/categories", response_model=List[schemas.CategoryResponse])
def get_categories(db: Session = Depends(get_db)):
    return crud.get_all_categories(db)


@app.post("/api/categories", status_code=status.HTTP_201_CREATED)
def add_category(category: schemas.CategoryCreate, db: Session = Depends(get_db)):
    try:
        return crud.create_category(db, category)
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=409, detail="Category already exists")


@app.get("/api/products", response_model=List[schemas.ProductResponse])
def get_products(
    categoryId: Optional[str] = None,
    category_id: Optional[str] = None,
    keyword: Optional[str] = None,
    db: Session = Depends(get_db)
):
    cat_id = categoryId or category_id
    return crud.get_all_products(db, category_id=cat_id, keyword=keyword)


@app.get("/api/products/{product_id}", response_model=schemas.ProductResponse)
def get_product(product_id: str, db: Session = Depends(get_db)):
    product = crud.get_product_id(db, product_id)
    if product is None:
        raise HTTPException(status_code=404, detail="Product not found")
    return product


@app.post("/api/products", response_model=schemas.ProductResponse, status_code=status.HTTP_201_CREATED)
def add_product(
    product: schemas.ProductCreate,
    x_user_role: Optional[str] = Header(None, alias="X-User-Role"),
    db: Session = Depends(get_db)
):
    if x_user_role is not None and x_user_role.upper() != "ADMIN":
        raise HTTPException(status_code=403, detail="Access denied: ADMIN role required")
    try:
        return crud.create_product(db, product)
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=409, detail=f"Product with SKU '{product.sku}' already exists")


@app.put("/api/products/{product_id}", response_model=schemas.ProductResponse)
def update_product(
    product_id: str,
    product: schemas.ProductUpdate,
    x_user_role: Optional[str] = Header(None, alias="X-User-Role"),
    db: Session = Depends(get_db)
):
    if x_user_role is not None and x_user_role.upper() != "ADMIN":
        raise HTTPException(status_code=403, detail="Access denied: ADMIN role required")
    updated = crud.update_product_id(db, product_id, product)
    if updated is None:
        raise HTTPException(status_code=404, detail="Product not found")
    return updated


@app.delete("/api/products/{product_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_product(product_id: str, db: Session = Depends(get_db)):
    deleted = crud.delete_product_id(db, product_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Product not found")
    return None


@app.get("/api/stores", response_model=List[schemas.StoreResponse])
def get_stores(city: Optional[str] = None, db: Session = Depends(get_db)):
    return crud.get_all_stores(db, city=city)


@app.get("/api/stores/{store_id}", response_model=schemas.StoreResponse)
def get_store(store_id: str, db: Session = Depends(get_db)):
    store = crud.get_store_id(db, store_id)
    if store is None:
        raise HTTPException(status_code=404, detail="Store not found")
    return store


@app.post("/api/stores", status_code=status.HTTP_201_CREATED)
def add_store(store: schemas.StoreCreate, db: Session = Depends(get_db)):
    try:
        return crud.create_store(db, store)
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=409, detail="Store code already exists")


@app.get("/api/inventory/store/{store_id}/product/{product_id}", response_model=schemas.InventoryResponse)
def get_store_inventory(store_id: str, product_id: str, db: Session = Depends(get_db)):
    return crud.get_stock(db, store_id, product_id)


@app.post("/api/inventory/store/{store_id}/product/{product_id}/restock", response_model=schemas.InventoryResponse)
def restock_store_inventory(
    store_id: str,
    product_id: str,
    request: schemas.RestockRequest,
    x_user_role: Optional[str] = Header(None, alias="X-User-Role"),
    db: Session = Depends(get_db)
):
    if x_user_role is not None and x_user_role.upper() not in ["STORE_OPERATOR", "ADMIN"]:
        raise HTTPException(status_code=403, detail="Access denied: Operator or Admin role required")
    if request.quantity <= 0:
        raise HTTPException(status_code=400, detail="Restock quantity must be greater than zero")
    return crud.restock(db, store_id, product_id, request.quantity)


@app.post("/internal/inventory/reserve")
def reserve_stock(request: schemas.StockRequest, db: Session = Depends(get_db)):
    success = crud.reserve_stock(db, request.storeId, request.productId, request.quantity)
    if not success:
        raise HTTPException(status_code=400, detail="Insufficient stock")
    return {"status": "SUCCESS", "message": "Stock reserved successfully"}


@app.post("/internal/inventory/release")
def release_stock(request: schemas.StockRequest, db: Session = Depends(get_db)):
    success = crud.release_stock(db, request.storeId, request.productId, request.quantity)
    if not success:
        raise HTTPException(status_code=404, detail="Stock record not found")
    return {"status": "SUCCESS", "message": "Stock released successfully"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=SERVICE_PORT, reload=True)
