from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict


class ProductCreate(BaseModel):
    sku: str
    name: str
    description: Optional[str] = None
    categoryId: Optional[str] = None
    price: float
    isActive: bool = True


class ProductUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    categoryId: Optional[str] = None
    price: Optional[float] = None
    isActive: Optional[bool] = None


class ProductResponse(BaseModel):
    id: str
    sku: str
    name: str
    description: Optional[str] = None
    categoryId: Optional[str] = None
    categoryName: Optional[str] = "General"
    price: float
    isActive: bool = True
    createdAt: Optional[datetime] = None
    model_config = ConfigDict(from_attributes=True)


class CategoryCreate(BaseModel):
    id: Optional[str] = None
    name: str
    description: Optional[str] = None


class CategoryResponse(BaseModel):
    id: str
    name: str
    description: Optional[str] = None
    model_config = ConfigDict(from_attributes=True)


class StoreCreate(BaseModel):
    id: Optional[str] = None
    name: str
    code: str
    address: str
    city: str
    phone: Optional[str] = None
    isActive: bool = True


class StoreResponse(BaseModel):
    id: str
    name: str
    code: str
    address: str
    city: str
    phone: Optional[str] = None
    isActive: bool = True
    createdAt: Optional[datetime] = None
    model_config = ConfigDict(from_attributes=True)


class InventoryResponse(BaseModel):
    id: Optional[str] = None
    storeId: str
    storeName: Optional[str] = None
    productId: str
    productName: Optional[str] = None
    productSku: Optional[str] = None
    quantityAvailable: int = 0
    quantityReserved: int = 0
    reorderThreshold: int = 5
    updatedAt: Optional[datetime] = None
    model_config = ConfigDict(from_attributes=True)


class RestockRequest(BaseModel):
    quantity: int = 10


class StockRequest(BaseModel):
    storeId: str
    productId: str
    quantity: int
