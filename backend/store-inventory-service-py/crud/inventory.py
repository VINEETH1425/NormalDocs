import uuid
from sqlalchemy.orm import Session
import models
import schemas


def get_inventory_detail(db: Session, store_id: str, product_id: str) -> schemas.InventoryResponse:
    inv = (
        db.query(models.StoreInventory)
        .filter(
            models.StoreInventory.store_id == store_id,
            models.StoreInventory.product_id == product_id
        )
        .first()
    )
    prod = (
        db.query(models.Product)
        .filter((models.Product.id == product_id) | (models.Product.sku.ilike(product_id)))
        .first()
    )
    store = db.query(models.Store).filter(models.Store.id == store_id).first()

    return schemas.InventoryResponse(
        id=inv.id if inv else None,
        storeId=store_id,
        storeName=store.name if store else store_id,
        productId=product_id,
        productName=prod.name if prod else f"Product {product_id}",
        productSku=prod.sku if prod else "",
        quantityAvailable=inv.quantity_available if inv else 0,
        quantityReserved=inv.quantity_reserved if inv else 0,
        reorderThreshold=inv.reorder_threshold if inv else 5,
        updatedAt=inv.updated_at if inv else None
    )


def restock_inventory(db: Session, store_id: str, product_id: str, quantity: int) -> schemas.InventoryResponse:
    inv = (
        db.query(models.StoreInventory)
        .filter(
            models.StoreInventory.store_id == store_id,
            models.StoreInventory.product_id == product_id
        )
        .first()
    )
    if not inv:
        inv = models.StoreInventory(
            id=f"INV-{uuid.uuid4().hex[:8].upper()}",
            store_id=store_id,
            product_id=product_id,
            quantity_available=0,
            quantity_reserved=0,
            reorder_threshold=5
        )
        db.add(inv)

    inv.quantity_available += quantity
    db.commit()
    db.refresh(inv)
    return get_inventory_detail(db, store_id, product_id)


def reserve_stock(db: Session, store_id: str, product_id: str, quantity: int) -> bool:
    inv = (
        db.query(models.StoreInventory)
        .filter(
            models.StoreInventory.store_id == store_id,
            models.StoreInventory.product_id == product_id
        )
        .first()
    )
    if not inv or inv.quantity_available < quantity:
        return False

    inv.quantity_available -= quantity
    inv.quantity_reserved += quantity
    db.commit()
    return True


def release_stock(db: Session, store_id: str, product_id: str, quantity: int) -> bool:
    inv = (
        db.query(models.StoreInventory)
        .filter(
            models.StoreInventory.store_id == store_id,
            models.StoreInventory.product_id == product_id
        )
        .first()
    )
    if not inv:
        return False

    inv.quantity_available += quantity
    inv.quantity_reserved = max(0, inv.quantity_reserved - quantity)
    db.commit()
    return True

