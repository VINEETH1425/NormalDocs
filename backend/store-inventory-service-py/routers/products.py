from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Header, status
from sqlalchemy.orm import Session
from database import get_db
import schemas
import crud

router = APIRouter(prefix="/api/products", tags=["Products"])


@router.get("", response_model=List[schemas.ProductResponse])
@router.get("/", response_model=List[schemas.ProductResponse], include_in_schema=False)
async def read_products(
    categoryId: Optional[str] = None,
    category_id: Optional[str] = None,
    keyword: Optional[str] = None,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    effective_category_id = categoryId or category_id
    return crud.get_products(db, keyword=keyword, category_id=effective_category_id, limit=limit)


@router.get("/{id}", response_model=schemas.ProductResponse)
async def read_product_by_id(id: str, db: Session = Depends(get_db)):
    product = crud.get_product_by_id(db, product_id=id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Product not found with id: {id}")
    return product


@router.post("", response_model=schemas.ProductResponse, status_code=status.HTTP_201_CREATED)
@router.post("/", response_model=schemas.ProductResponse, status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def create_new_product(
    product: schemas.ProductCreate,
    x_user_role: Optional[str] = Header(None, alias="X-User-Role"),
    db: Session = Depends(get_db)
):
    if x_user_role is not None and x_user_role.upper() != "ADMIN":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied: ADMIN role required")

    existing = crud.get_product_by_id(db, product_id=product.sku)
    if existing:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=f"Product with SKU already exists: {product.sku}")
    return crud.create_product(db, product)


@router.put("/{id}", response_model=schemas.ProductResponse)
async def update_existing_product(
    id: str,
    product: schemas.ProductUpdate,
    x_user_role: Optional[str] = Header(None, alias="X-User-Role"),
    db: Session = Depends(get_db)
):
    if x_user_role is not None and x_user_role.upper() != "ADMIN":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied: ADMIN role required")

    updated = crud.update_product(db, product_id=id, product_update=product)
    if not updated:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Product not found with id: {id}")
    return updated


@router.patch("/{id}/status", response_model=schemas.ProductResponse)
async def patch_product_active_status(id: str, is_active: bool, db: Session = Depends(get_db)):
    patched = crud.patch_product_status(db, product_id=id, is_active=is_active)
    if not patched:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Product with id '{id}' not found")
    return patched


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_existing_product(id: str, db: Session = Depends(get_db)):
    deleted = crud.delete_product(db, product_id=id)
    if not deleted:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Product with id '{id}' not found to delete")
    return None

