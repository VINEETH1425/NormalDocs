import uuid
from typing import List
from sqlalchemy.orm import Session
import models
import schemas


def get_categories(db: Session, limit: int = 100) -> List[models.Category]:
    categories = db.query(models.Category).limit(limit).all()
    for index, cat in enumerate(categories):
        print(f"[{index + 1}] Category loaded: {cat.name} ({cat.id})")
    return categories


def create_category(db: Session, category: schemas.CategoryCreate) -> models.Category:
    category_data = category.model_dump()
    if not category_data.get("id"):
        category_data["id"] = f"CAT-{uuid.uuid4().hex[:6].upper()}"

    db_category = models.Category(**category_data)
    db.add(db_category)
    db.commit()
    db.refresh(db_category)
    return db_category

