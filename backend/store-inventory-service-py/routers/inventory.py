from fastapi import APIRouter, Depends, HTTPException, Header, status
from sqlalchemy.orm import Session
from database import get_db
import schemas
import crud

router = APIRouter(tags=["Inventory"])


@router.get("/api/inventory/store/{store_id}/product/{product_id}", response_model=schemas.InventoryResponse)
async def read_store_product_stock(store_id: str, product_id: str, db: Session = Depends(get_db)):
    return crud.get_inventory_detail(db, store_id=store_id, product_id=product_id)


@router.post("/api/inventory/store/{store_id}/product/{product_id}/restock", response_model=schemas.InventoryResponse)
async def restock_product(
    store_id: str,
    product_id: str,
    request: schemas.RestockRequest,
    x_user_role: str = Header(None, alias="X-User-Role"),
    db: Session = Depends(get_db)
):
    if x_user_role is not None and x_user_role.upper() not in ["STORE_OPERATOR", "ADMIN"]:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied: Operator or Admin role required")

    if request.quantity <= 0:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Restock quantity must be greater than zero")

    return crud.restock_inventory(db, store_id=store_id, product_id=product_id, quantity=request.quantity)


@router.post("/internal/inventory/reserve")
async def reserve_stock_endpoint(request: schemas.StockReservationRequest, db: Session = Depends(get_db)):
    success = crud.reserve_stock(db, store_id=request.store_id, product_id=request.product_id, quantity=request.quantity)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Insufficient stock at store. Requested: {request.quantity}"
        )
    return {"status": "SUCCESS", "message": "Stock reserved successfully"}


@router.post("/internal/inventory/release")
async def release_stock_endpoint(request: schemas.StockReservationRequest, db: Session = Depends(get_db)):
    success = crud.release_stock(db, store_id=request.store_id, product_id=request.product_id, quantity=request.quantity)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Stock record not found for product {request.product_id} at store {request.store_id}"
        )
    return {"status": "SUCCESS", "message": "Stock released successfully"}

