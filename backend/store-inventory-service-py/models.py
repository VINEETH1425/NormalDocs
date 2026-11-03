from datetime import datetime
from sqlalchemy import Column, String, Float, Boolean, Integer, DateTime
from database import Base, DATABASE_URL

SCHEMA_NAME = "inventory_schema" if not DATABASE_URL.startswith("sqlite") else None


class Category(Base):
    __tablename__ = "categories"
    __table_args__ = {"schema": SCHEMA_NAME} if SCHEMA_NAME else ()

    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(String, nullable=True)


class Product(Base):
    __tablename__ = "products"
    __table_args__ = {"schema": SCHEMA_NAME} if SCHEMA_NAME else ()

    id = Column(String, primary_key=True, index=True)
    sku = Column(String, unique=True, index=True, nullable=False)
    name = Column(String, nullable=False)
    description = Column(String, nullable=True)
    category_id = Column(String, nullable=True)
    price = Column(Float, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)


class Store(Base):
    __tablename__ = "stores"
    __table_args__ = {"schema": SCHEMA_NAME} if SCHEMA_NAME else ()

    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    code = Column(String, unique=True, index=True, nullable=False)
    address = Column(String, nullable=False)
    city = Column(String, nullable=False, index=True)
    phone = Column(String, nullable=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)


class StoreInventory(Base):
    __tablename__ = "store_inventory"
    __table_args__ = {"schema": SCHEMA_NAME} if SCHEMA_NAME else ()

    id = Column(String, primary_key=True, index=True)
    store_id = Column(String, index=True, nullable=False)
    product_id = Column(String, index=True, nullable=False)
    quantity_available = Column(Integer, default=0, nullable=False)
    quantity_reserved = Column(Integer, default=0, nullable=False)
    reorder_threshold = Column(Integer, default=5, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
