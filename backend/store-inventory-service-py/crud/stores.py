import uuid
from typing import List, Optional
from sqlalchemy.orm import Session
import models
import schemas


def get_stores(db: Session, city: Optional[str] = None, limit: int = 100) -> List[schemas.StoreResponse]:
    query = db.query(models.Store).filter(models.Store.is_active == True)
    if city and city.strip():
        query = query.filter(models.Store.city.ilike(f"%{city.strip()}%"))
    return [
        schemas.StoreResponse(
            id=s.id,
            name=s.name,
            code=s.code,
            address=s.address,
            city=s.city,
            phone=s.phone,
            isActive=s.is_active,
            createdAt=s.created_at
        )
        for s in query.limit(limit).all()
    ]


def get_store_by_id(db: Session, store_id: str) -> Optional[schemas.StoreResponse]:
    s = db.query(models.Store).filter(models.Store.id == store_id).first()
    if not s:
        return None
    return schemas.StoreResponse(
        id=s.id,
        name=s.name,
        code=s.code,
        address=s.address,
        city=s.city,
        phone=s.phone,
        isActive=s.is_active,
        createdAt=s.created_at
    )


def create_store(db: Session, store: schemas.StoreCreate) -> schemas.StoreResponse:
    data = store.model_dump()
    if not data.get("id"):
        data["id"] = f"STORE-{uuid.uuid4().hex[:6].upper()}"

    db_store = models.Store(
        id=data["id"],
        name=data["name"],
        code=data["code"],
        address=data["address"],
        city=data["city"],
        phone=data.get("phone"),
        is_active=data.get("is_active", True)
    )
    db.add(db_store)
    db.commit()
    db.refresh(db_store)
    return schemas.StoreResponse(
        id=db_store.id,
        name=db_store.name,
        code=db_store.code,
        address=db_store.address,
        city=db_store.city,
        phone=db_store.phone,
        isActive=db_store.is_active,
        createdAt=db_store.created_at
    )

