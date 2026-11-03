from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class CategoryBase(BaseModel):
    name: str
    description: Optional[str] = None


class CategoryCreate(CategoryBase):
    id: Optional[str] = None


class CategoryResponse(CategoryBase):
    id: str
    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class ProductBase(BaseModel):
    sku: str
    name: str
    description: Optional[str] = None
    category_id: Optional[str] = Field(default=None, alias="categoryId")
    price: float


class ProductCreate(ProductBase):
    id: Optional[str] = None
    is_active: bool = Field(default=True, alias="isActive")
    model_config = ConfigDict(populate_by_name=True)


class ProductUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    price: Optional[float] = None
    category_id: Optional[str] = Field(default=None, alias="categoryId")
    is_active: Optional[bool] = Field(default=None, alias="isActive")
    model_config = ConfigDict(populate_by_name=True)


class ProductResponse(BaseModel):
    id: str
    sku: str
    name: str
    description: Optional[str] = None
    category_id: Optional[str] = Field(default=None, alias="categoryId", serialization_alias="categoryId")
    category_name: Optional[str] = Field(default="General", alias="categoryName", serialization_alias="categoryName")
    price: float
    is_active: bool = Field(default=True, alias="isActive", serialization_alias="isActive")
    created_at: Optional[datetime] = Field(default=None, alias="createdAt", serialization_alias="createdAt")
    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class StoreBase(BaseModel):
    name: str
    code: str
    address: str
    city: str
    phone: Optional[str] = None


class StoreCreate(StoreBase):
    id: Optional[str] = None
    is_active: bool = Field(default=True, alias="isActive")
    model_config = ConfigDict(populate_by_name=True)


class StoreResponse(StoreBase):
    id: str
    is_active: bool = Field(default=True, alias="isActive", serialization_alias="isActive")
    created_at: Optional[datetime] = Field(default=None, alias="createdAt", serialization_alias="createdAt")
    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class InventoryResponse(BaseModel):
    id: Optional[str] = None
    store_id: str = Field(alias="storeId", serialization_alias="storeId")
    store_name: Optional[str] = Field(default=None, alias="storeName", serialization_alias="storeName")
    product_id: str = Field(alias="productId", serialization_alias="productId")
    product_name: Optional[str] = Field(default=None, alias="productName", serialization_alias="productName")
    product_sku: Optional[str] = Field(default=None, alias="productSku", serialization_alias="productSku")
    quantity_available: int = Field(default=0, alias="quantityAvailable", serialization_alias="quantityAvailable")
    quantity_reserved: int = Field(default=0, alias="quantityReserved", serialization_alias="quantityReserved")
    reorder_threshold: int = Field(default=5, alias="reorderThreshold", serialization_alias="reorderThreshold")
    updated_at: Optional[datetime] = Field(default=None, alias="updatedAt", serialization_alias="updatedAt")
    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class RestockRequest(BaseModel):
    quantity: int = 10
    model_config = ConfigDict(populate_by_name=True)


class StockReservationRequest(BaseModel):
    store_id: str = Field(alias="storeId")
    product_id: str = Field(alias="productId")
    quantity: int
    model_config = ConfigDict(populate_by_name=True)
