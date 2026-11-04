import uuid
from typing import Optional, List, Dict
from sqlalchemy.orm import Session
from models import Product, Category, Store, StoreInventory
from schemas import ProductCreate, ProductUpdate, ProductResponse, CategoryCreate, StoreCreate, StoreResponse, InventoryResponse


def _get_category_map(db: Session) -> Dict[str, str]:
    return {c.id: c.name for c in db.query(Category).all()}


def get_all_products(db: Session, category_id: Optional[str] = None, keyword: Optional[str] = None) -> List[ProductResponse]:
    query = db.query(Product).filter(Product.is_active == True)
    if category_id and category_id.strip():
        query = query.filter(Product.category_id == category_id.strip())
    if keyword and keyword.strip():
        kw = f"%{keyword.strip().lower()}%"
        query = query.filter(Product.name.ilike(kw) | Product.sku.ilike(kw))

    products = query.all()
    cat_map = _get_category_map(db)

    return [
        ProductResponse(
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


def get_product_id(db: Session, product_id: str) -> Optional[ProductResponse]:
    p = db.query(Product).filter((Product.id == product_id) | (Product.sku.ilike(product_id))).first()
    if not p:
        return None

    cat_map = _get_category_map(db)
    return ProductResponse(
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


def create_product(db: Session, product: ProductCreate) -> ProductResponse:
    prod_id = f"PROD-{uuid.uuid4().hex[:8].upper()}"
    new_product = Product(
        id=prod_id,
        sku=product.sku,
        name=product.name,
        description=product.description,
        category_id=product.categoryId,
        price=product.price,
        is_active=product.isActive
    )
    db.add(new_product)
    db.commit()
    db.refresh(new_product)

    cat_map = _get_category_map(db)
    return ProductResponse(
        id=new_product.id,
        sku=new_product.sku,
        name=new_product.name,
        description=new_product.description,
        categoryId=new_product.category_id,
        categoryName=cat_map.get(new_product.category_id, "General"),
        price=new_product.price,
        isActive=new_product.is_active,
        createdAt=new_product.created_at
    )


def update_product_id(db: Session, product_id: str, product_data: ProductUpdate) -> Optional[ProductResponse]:
    product = db.query(Product).filter((Product.id == product_id) | (Product.sku.ilike(product_id))).first()
    if product is None:
        return None

    if product_data.name is not None:
        product.name = product_data.name
    if product_data.description is not None:
        product.description = product_data.description
    if product_data.categoryId is not None:
        product.category_id = product_data.categoryId
    if product_data.price is not None:
        product.price = product_data.price
    if product_data.isActive is not None:
        product.is_active = product_data.isActive

    db.commit()
    db.refresh(product)

    cat_map = _get_category_map(db)
    return ProductResponse(
        id=product.id,
        sku=product.sku,
        name=product.name,
        description=product.description,
        categoryId=product.category_id,
        categoryName=cat_map.get(product.category_id, "General"),
        price=product.price,
        isActive=product.is_active,
        createdAt=product.created_at
    )


def delete_product_id(db: Session, product_id: str) -> bool:
    product = db.query(Product).filter((Product.id == product_id) | (Product.sku.ilike(product_id))).first()
    if product is None:
        return False
    db.delete(product)
    db.commit()
    return True


def get_all_categories(db: Session) -> List[Category]:
    return db.query(Category).all()


def create_category(db: Session, category: CategoryCreate) -> Category:
    cat_id = category.id or f"CAT-{uuid.uuid4().hex[:6].upper()}"
    new_category = Category(id=cat_id, name=category.name, description=category.description)
    db.add(new_category)
    db.commit()
    db.refresh(new_category)
    return new_category


def get_all_stores(db: Session, city: Optional[str] = None) -> List[StoreResponse]:
    query = db.query(Store).filter(Store.is_active == True)
    if city and city.strip():
        query = query.filter(Store.city.ilike(f"%{city.strip()}%"))
    stores = query.all()
    return [
        StoreResponse(
            id=s.id,
            name=s.name,
            code=s.code,
            address=s.address,
            city=s.city,
            phone=s.phone,
            isActive=s.is_active,
            createdAt=s.created_at
        )
        for s in stores
    ]


def get_store_id(db: Session, store_id: str) -> Optional[StoreResponse]:
    s = db.query(Store).filter(Store.id == store_id).first()
    if s is None:
        return None
    return StoreResponse(
        id=s.id,
        name=s.name,
        code=s.code,
        address=s.address,
        city=s.city,
        phone=s.phone,
        isActive=s.is_active,
        createdAt=s.created_at
    )


def create_store(db: Session, store: StoreCreate) -> StoreResponse:
    store_id = store.id or f"STORE-{uuid.uuid4().hex[:6].upper()}"
    new_store = Store(
        id=store_id,
        name=store.name,
        code=store.code,
        address=store.address,
        city=store.city,
        phone=store.phone,
        is_active=store.isActive
    )
    db.add(new_store)
    db.commit()
    db.refresh(new_store)
    return StoreResponse(
        id=new_store.id,
        name=new_store.name,
        code=new_store.code,
        address=new_store.address,
        city=new_store.city,
        phone=new_store.phone,
        isActive=new_store.is_active,
        createdAt=new_store.created_at
    )


def get_stock(db: Session, store_id: str, product_id: str) -> InventoryResponse:
    inv = db.query(StoreInventory).filter(
        StoreInventory.store_id == store_id,
        StoreInventory.product_id == product_id
    ).first()

    prod = db.query(Product).filter((Product.id == product_id) | (Product.sku.ilike(product_id))).first()
    store = db.query(Store).filter(Store.id == store_id).first()

    return InventoryResponse(
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


def restock(db: Session, store_id: str, product_id: str, quantity: int) -> InventoryResponse:
    inv = db.query(StoreInventory).filter(
        StoreInventory.store_id == store_id,
        StoreInventory.product_id == product_id
    ).first()

    if not inv:
        inv = StoreInventory(
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
    return get_stock(db, store_id, product_id)


def reserve_stock(db: Session, store_id: str, product_id: str, quantity: int) -> bool:
    inv = db.query(StoreInventory).filter(
        StoreInventory.store_id == store_id,
        StoreInventory.product_id == product_id
    ).first()

    if not inv or inv.quantity_available < quantity:
        return False

    inv.quantity_available -= quantity
    inv.quantity_reserved += quantity
    db.commit()
    return True


def release_stock(db: Session, store_id: str, product_id: str, quantity: int) -> bool:
    inv = db.query(StoreInventory).filter(
        StoreInventory.store_id == store_id,
        StoreInventory.product_id == product_id
    ).first()

    if not inv:
        return False

    inv.quantity_available += quantity
    inv.quantity_reserved = max(0, inv.quantity_reserved - quantity)
    db.commit()
    return True
