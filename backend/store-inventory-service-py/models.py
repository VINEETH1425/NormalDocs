from datetime import datetime
from database import Base
from sqlalchemy import Column, String, Float, Integer, Boolean, DateTime

TABLE_ARGS = {"schema": "inventory_schema"}


class Category(Base):
    __tablename__ = "categories"
    __table_args__ = TABLE_ARGS

    id = Column(String(64), primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    description = Column(String, nullable=True)


class Product(Base):
    __tablename__ = "products"
    __table_args__ = TABLE_ARGS

    id = Column(String(64), primary_key=True, index=True)
    sku = Column(String(64), unique=True, index=True, nullable=False)
    name = Column(String(200), nullable=False)
    description = Column(String, nullable=True)
    category_id = Column(String(64), nullable=True)
    price = Column(Float, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)


class Store(Base):
    __tablename__ = "stores"
    __table_args__ = TABLE_ARGS

    id = Column(String(64), primary_key=True, index=True)
    name = Column(String(150), nullable=False)
    code = Column(String(50), unique=True, index=True, nullable=False)
    address = Column(String(255), nullable=False)
    city = Column(String(100), nullable=False, index=True)
    phone = Column(String(30), nullable=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)


class StoreInventory(Base):
    __tablename__ = "store_inventory"
    __table_args__ = TABLE_ARGS

    id = Column(String(64), primary_key=True, index=True)
    store_id = Column(String(64), index=True, nullable=False)
    product_id = Column(String(64), index=True, nullable=False)
    quantity_available = Column(Integer, default=0, nullable=False)
    quantity_reserved = Column(Integer, default=0, nullable=False)
    reorder_threshold = Column(Integer, default=5, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow)
