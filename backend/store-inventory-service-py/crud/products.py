import uuid
from typing import List, Optional, Dict
from sqlalchemy.orm import Session
import models
import schemas


def _get_category_map(db: Session) -> Dict[str, str]:
    return {c.id: c.name for c in db.query(models.Category).all()}


def get_products(
    db: Session,
    keyword: Optional[str] = None,
    category_id: Optional[str] = None,
    limit: int = 100
) -> List[schemas.ProductResponse]:
    query = db.query(models.Product).filter(models.Product.is_active == True)
    if category_id and category_id.strip():
        query = query.filter(models.Product.category_id == category_id.strip())
    if keyword and keyword.strip():
        kw = f"%{keyword.strip().lower()}%"
        query = query.filter(models.Product.name.ilike(kw) | models.Product.sku.ilike(kw))

    products = query.limit(limit).all()
    cat_map = _get_category_map(db)

    return [
        schemas.ProductResponse(
            id=p.id,
            sku=p.sku,
            name=p.name,
            description=p.description,
            categoryId=p.category_id,
            categoryName=cat_map.get(p.category_id, "General"),
            price=p.price,
            isActive=p.is_active,
            createdAt=p.created_at
        )
        for p in products
    ]


def get_product_by_id(db: Session, product_id: str) -> Optional[schemas.ProductResponse]:
    prod = (
        db.query(models.Product)
        .filter((models.Product.id == product_id) | (models.Product.sku.ilike(product_id)))
        .first()
    )
    if not prod:
        return None

    cat_map = _get_category_map(db)
    return schemas.ProductResponse(
        id=prod.id,
        sku=prod.sku,
        name=prod.name,
        description=prod.description,
        categoryId=prod.category_id,
        categoryName=cat_map.get(prod.category_id, "General"),
        price=prod.price,
        isActive=prod.is_active,
        createdAt=prod.created_at
    )


def create_product(db: Session, product: schemas.ProductCreate) -> schemas.ProductResponse:
    data = product.model_dump()
    if not data.get("id"):
        data["id"] = f"PROD-{uuid.uuid4().hex[:8].upper()}"

    cat_id = data.get("category_id") or data.get("categoryId")
    db_product = models.Product(
        id=data["id"],
        sku=data["sku"],
        name=data["name"],
        description=data.get("description"),
        category_id=cat_id,
        price=data["price"],
        is_active=data.get("is_active", True)
    )
    db.add(db_product)
    db.commit()
    db.refresh(db_product)

    cat_map = _get_category_map(db)
    return schemas.ProductResponse(
        id=db_product.id,
        sku=db_product.sku,
        name=db_product.name,
        description=db_product.description,
        categoryId=db_product.category_id,
        categoryName=cat_map.get(db_product.category_id, "General"),
        price=db_product.price,
        isActive=db_product.is_active,
        createdAt=db_product.created_at
    )


def update_product(
    db: Session,
    product_id: str,
    product_update: schemas.ProductUpdate
) -> Optional[schemas.ProductResponse]:
    prod = (
        db.query(models.Product)
        .filter((models.Product.id == product_id) | (models.Product.sku.ilike(product_id)))
        .first()
    )
    if not prod:
        return None

    update_data = product_update.model_dump(exclude_unset=True)
    if "name" in update_data and update_data["name"]:
        prod.name = update_data["name"].strip()
    if "description" in update_data:
        prod.description = update_data["description"]
    if "price" in update_data and update_data["price"] is not None:
        prod.price = update_data["price"]
    if "category_id" in update_data and update_data["category_id"]:
        prod.category_id = update_data["category_id"].strip()
    if "is_active" in update_data and update_data["is_active"] is not None:
        prod.is_active = update_data["is_active"]

    db.commit()
    db.refresh(prod)

    cat_map = _get_category_map(db)
    return schemas.ProductResponse(
        id=prod.id,
        sku=prod.sku,
        name=prod.name,
        description=prod.description,
        categoryId=prod.category_id,
        categoryName=cat_map.get(prod.category_id, "General"),
        price=prod.price,
        isActive=prod.is_active,
        createdAt=prod.created_at
    )


def delete_product(db: Session, product_id: str) -> bool:
    prod = (
        db.query(models.Product)
        .filter((models.Product.id == product_id) | (models.Product.sku.ilike(product_id)))
        .first()
    )
    if not prod:
        return False
    db.delete(prod)
    db.commit()
    return True


def patch_product_status(db: Session, product_id: str, is_active: bool) -> Optional[schemas.ProductResponse]:
    prod = (
        db.query(models.Product)
        .filter((models.Product.id == product_id) | (models.Product.sku.ilike(product_id)))
        .first()
    )
    if not prod:
        return None
    prod.is_active = is_active
    db.commit()
    db.refresh(prod)

    cat_map = _get_category_map(db)
    return schemas.ProductResponse(
        id=prod.id,
        sku=prod.sku,
        name=prod.name,
        description=prod.description,
        categoryId=prod.category_id,
        categoryName=cat_map.get(prod.category_id, "General"),
        price=prod.price,
        isActive=prod.is_active,
        createdAt=prod.created_at
    )

