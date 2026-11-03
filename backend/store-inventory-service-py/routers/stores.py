from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from database import get_db
import schemas
import crud

router = APIRouter(prefix="/api/stores", tags=["Stores"])


@router.get("", response_model=List[schemas.StoreResponse])
@router.get("/", response_model=List[schemas.StoreResponse], include_in_schema=False)
async def read_stores(city: Optional[str] = None, limit: int = 100, db: Session = Depends(get_db)):
    return crud.get_stores(db, city=city, limit=limit)


@router.get("/{id}", response_model=schemas.StoreResponse)
async def read_store_by_id(id: str, db: Session = Depends(get_db)):
    store = crud.get_store_by_id(db, store_id=id)
    if not store:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Store not found with id: {id}")
    return store


@router.post("", response_model=schemas.StoreResponse, status_code=status.HTTP_201_CREATED)
@router.post("/", response_model=schemas.StoreResponse, status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def create_new_store(store: schemas.StoreCreate, db: Session = Depends(get_db)):
    return crud.create_store(db, store)

