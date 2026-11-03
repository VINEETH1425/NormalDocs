from sqlalchemy.orm import Session
import models


def seed_initial_data(db: Session):
    if db.query(models.Category).first() is not None:
        return

    categories = [
        models.Category(id="CAT-PRODUCE", name="Fresh Produce", description="Organic fruits and vegetables"),
        models.Category(id="CAT-DAIRY", name="Dairy & Bakery", description="Farm fresh milk, cheese and bread"),
        models.Category(id="CAT-BEVERAGES", name="Beverages", description="Cold pressed juices and craft coffee"),
        models.Category(id="CAT-SNACKS", name="Packaged Snacks", description="Healthy snacks and trail mixes"),
    ]
    db.add_all(categories)

    stores = [
        models.Store(id="STORE-GGN-01", name="DLF CyberHub SuperStore", code="GGN-01", address="Ground Floor, CyberHub", city="Gurugram", phone="+91 124 4567890"),
        models.Store(id="STORE-DEL-01", name="Connaught Place Flagship", code="DEL-01", address="Block B, Inner Circle, CP", city="New Delhi", phone="+91 11 23415678"),
        models.Store(id="STORE-BLR-01", name="Indiranagar Experience Hub", code="BLR-01", address="100 Feet Road, HAL 2nd Stage", city="Bengaluru", phone="+91 80 41235678"),
    ]
    db.add_all(stores)

    products = [
        models.Product(id="PROD-001", sku="SKU-APP-001", name="Shimla Royal Apple (1kg)", description="Sweet mountain apples", category_id="CAT-PRODUCE", price=180.00),
        models.Product(id="PROD-002", sku="SKU-MILK-001", name="A2 Farm Fresh Whole Milk (1L)", description="Cold-chain cow milk", category_id="CAT-DAIRY", price=75.00),
        models.Product(id="PROD-003", sku="SKU-COF-001", name="Coorg Dark Roast Coffee Beans (250g)", description="Arabica roasted beans", category_id="CAT-BEVERAGES", price=350.00),
        models.Product(id="PROD-004", sku="SKU-ALM-001", name="California Jumbo Roasted Almonds (500g)", description="Slow roasted almonds", category_id="CAT-SNACKS", price=499.00),
        models.Product(id="PROD-005", sku="SKU-BAN-001", name="Robusta Farm Bananas (1 Dozen)", description="Nutrient-rich table bananas", category_id="CAT-PRODUCE", price=65.00),
        models.Product(id="PROD-006", sku="SKU-SPN-001", name="Hydroponic Baby Spinach (250g)", description="Hydroponic spinach leaves", category_id="CAT-PRODUCE", price=45.00),
        models.Product(id="PROD-007", sku="SKU-AVO-001", name="Hass Fresh Avocado (Pack of 2)", description="Creamy Hass avocados", category_id="CAT-PRODUCE", price=220.00),
        models.Product(id="PROD-008", sku="SKU-TOM-001", name="Organic Hybrid Tomatoes (1kg)", description="Vine-ripened organic tomatoes", category_id="CAT-PRODUCE", price=40.00),
    ]
    db.add_all(products)
    db.commit()

    for s in stores:
        for p in products:
            db.add(
                models.StoreInventory(
                    id=f"INV-{s.id[-6:]}-{p.id}",
                    store_id=s.id,
                    product_id=p.id,
                    quantity_available=35,
                    quantity_reserved=0,
                    reorder_threshold=5
                )
            )
    db.commit()

