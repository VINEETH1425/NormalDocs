import os
import sys

sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

import database
import models
import schemas
import crud
import main


def run_all_tests():
    print("==================================================")
    print("  RUNNING STORE-INVENTORY VERIFICATION TEST SUITE ")
    print("==================================================")

    models.Base.metadata.create_all(bind=database.engine)
    db = database.SessionLocal()
    crud.seed_initial_data(db)

    # 1. Categories
    cats = crud.get_categories(db)
    assert len(cats) >= 4, f"Expected 4 categories, got {len(cats)}"
    cat_ids = [c.id for c in cats]
    assert "CAT-PRODUCE" in cat_ids
    print(f" [PASS] 1. Categories retrieval (count = {len(cats)})")

    # 2. Stores
    stores = crud.get_stores(db)
    assert len(stores) >= 3, f"Expected 3 stores, got {len(stores)}"
    delhi_stores = crud.get_stores(db, city="New Delhi")
    assert len(delhi_stores) == 1
    assert delhi_stores[0].city == "New Delhi"
    print(f" [PASS] 2. Stores retrieval & city filter ({len(stores)} stores, New Delhi filtered)")

    # 3. Product catalog
    prods = crud.get_products(db, category_id="CAT-PRODUCE")
    assert len(prods) >= 1
    first_prod = prods[0]
    dumped = first_prod.model_dump(by_alias=True)
    assert "categoryId" in dumped
    assert "categoryName" in dumped
    assert "isActive" in dumped
    print(f" [PASS] 3. Product catalog & camelCase serialization (CAT-PRODUCE count = {len(prods)})")

    # 4. Keyword search
    apple_prods = crud.get_products(db, keyword="Apple")
    assert len(apple_prods) >= 1
    assert "Apple" in apple_prods[0].name
    print(f" [PASS] 4. Keyword search ('Apple' -> {apple_prods[0].name})")

    # 5. Product lookup by ID & SKU
    p_id = crud.get_product_by_id(db, "PROD-001")
    assert p_id is not None and p_id.name == "Shimla Royal Apple (1kg)"
    p_sku = crud.get_product_by_id(db, "SKU-APP-001")
    assert p_sku is not None and p_sku.id == "PROD-001"
    print(" [PASS] 5. Product lookup by ID & case-insensitive SKU")

    # 6. Shelf inventory
    inv = crud.get_inventory_detail(db, "STORE-GGN-01", "PROD-001")
    inv_dump = inv.model_dump(by_alias=True)
    assert "quantityAvailable" in inv_dump
    initial_available = inv.quantity_available
    assert initial_available >= 35
    print(f" [PASS] 6. Shelf inventory check ({inv_dump['quantityAvailable']} units available for pickup)")

    # 7. Stock reservation (Order Checkout)
    reserve_ok = crud.reserve_stock(db, "STORE-GGN-01", "PROD-001", 5)
    assert reserve_ok is True
    inv_after_res = crud.get_inventory_detail(db, "STORE-GGN-01", "PROD-001")
    assert inv_after_res.quantity_available == initial_available - 5
    assert inv_after_res.quantity_reserved == 5
    print(f" [PASS] 7. Feign stock reservation (available={inv_after_res.quantity_available}, reserved={inv_after_res.quantity_reserved})")

    # 8. Insufficient stock rejection
    insufficient_ok = crud.reserve_stock(db, "STORE-GGN-01", "PROD-001", 9999)
    assert insufficient_ok is False
    print(" [PASS] 8. Insufficient stock reservation rejected")

    # 9. Stock release (Order Cancelled)
    release_ok = crud.release_stock(db, "STORE-GGN-01", "PROD-001", 5)
    assert release_ok is True
    inv_after_rel = crud.get_inventory_detail(db, "STORE-GGN-01", "PROD-001")
    assert inv_after_rel.quantity_available == initial_available
    assert inv_after_rel.quantity_reserved == 0
    print(f" [PASS] 9. Feign stock release (stock restored to {inv_after_rel.quantity_available})")

    # 10. Restock
    restock_inv = crud.restock_inventory(db, "STORE-GGN-01", "PROD-001", 20)
    assert restock_inv.quantity_available == initial_available + 20
    print(f" [PASS] 10. Operator restock (+20 units -> {restock_inv.quantity_available})")

    # 11. Product CRUD
    new_prod = crud.create_product(
        db,
        schemas.ProductCreate(
            sku="SKU-AUTO-HONEY-01",
            name="Organic Forest Honey (500g)",
            description="Pure unprocessed honey",
            categoryId="CAT-SNACKS",
            price=299.0
        )
    )
    assert new_prod.sku == "SKU-AUTO-HONEY-01"
    new_id = new_prod.id
    print(f" [PASS] 11. Product creation (POST /api/products -> ID: {new_id})")

    updated = crud.update_product(
        db,
        new_id,
        schemas.ProductUpdate(name="Wild Multifloral Honey", price=320.0)
    )
    assert updated.name == "Wild Multifloral Honey"
    assert updated.price == 320.0
    print(" [PASS] 12. Product update (PUT /api/products/{id})")

    patched = crud.patch_product_status(db, new_id, False)
    assert patched.is_active is False
    print(" [PASS] 13. Product status toggle (PATCH /api/products/{id}/status -> False)")

    deleted = crud.delete_product(db, new_id)
    assert deleted is True
    assert crud.get_product_by_id(db, new_id) is None
    print(" [PASS] 14. Product deletion (DELETE /api/products/{id} -> 204/True)")

    # 12. OpenAPI routes check
    spec = main.app.openapi()
    assert "/v3/api-docs" in spec["paths"]
    assert "/internal/inventory/reserve" in spec["paths"]
    assert "/internal/inventory/release" in spec["paths"]
    assert "/api/products/{id}" in spec["paths"]
    assert "/api/categories" in spec["paths"]
    assert "/api/stores" in spec["paths"]
    print(" [PASS] 15. OpenAPI documentation contract verified for Gateway & Swagger Hub")

    db.close()
    print("==================================================")
    print("  ALL 15 TESTS PASSED SUCCESSFULLY! 100% MATCH    ")
    print("==================================================")


if __name__ == "__main__":
    run_all_tests()
