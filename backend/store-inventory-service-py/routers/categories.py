from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from database import get_db
import schemas
import crud

router = APIRouter(prefix="/api/categories", tags=["Categories"])


@router.get("", response_model=List[schemas.CategoryResponse])
@router.get("/", response_model=List[schemas.CategoryResponse], include_in_schema=False)
async def read_categories(limit: int = 100, db: Session = Depends(get_db)):
    return crud.get_categories(db, limit=limit)


@router.post("", response_model=schemas.CategoryResponse, status_code=status.HTTP_201_CREATED)
@router.post("/", response_model=schemas.CategoryResponse, status_code=status.HTTP_201_CREATED, include_in_schema=False)
async def create_new_category(category: schemas.CategoryCreate, db: Session = Depends(get_db)):
    return crud.create_category(db, category)

