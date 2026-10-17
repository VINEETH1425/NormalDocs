# StoreConnect – Problem Understanding & Provided Solution

## 1. Problem Statement Understanding

StoreConnect is an omnichannel retail system that connects customers, stores, products, inventory, carts, and orders into a single platform.

The system mainly supports:

- **Customer:** Browse products, check store-level availability, manage cart, place orders, and track order status.
- **Store:** Maintain store information and manage inventory available at each store location.
- **Product & Category:** Maintain a centralized product catalog with categories, pricing, and product details.
- **Cart:** Allow customers to add products, manage quantities, and proceed towards checkout.
- **Order:** Create and track customer orders with line items, quantities, prices, and fulfillment status.
- **Inventory:** Maintain store-level stock, including available/reserved quantities and reorder thresholds.
- **Fulfillment:** Support store operators in processing orders and moving them through valid fulfillment stages.
- **Consistency:** Prevent overselling through stock reservation and ensure valid order status transitions.
- **Auditability:** Maintain relevant timestamps and events so inventory and order changes can be tracked.

*Note: This represents the core understanding of the requirements. Any additional attributes or relationships required during subsequent iterations will be refined accordingly.*

---

## 2. Provided Solution

To fulfill these requirements cleanly without unnecessary complexity, StoreConnect implements the following solution:

1. **Centralized Catalog (Products & Categories):**
   - Products and categories are stored in a central catalog so that names, SKUs, descriptions, and base prices remain consistent across all physical stores.

2. **Store-Specific Inventory (`store_inventory`):**
   - Stock is not stored globally on the product. Instead, each physical store maintains its own inventory record (`store_id` + `product_id`).
   - Each inventory record tracks both `quantity_available` (shelf stock) and `quantity_reserved` (held for pending orders) to give customers accurate, real-time availability for their selected store.

3. **Persistent Shopping Cart:**
   - Each customer has an active cart linked to their account and selected store. Items and quantities can be added, updated, or removed, and the total amount is automatically recalculated.

4. **Preventing Overselling (Atomic Reservation):**
   - During checkout, the system validates stock for every cart item.
   - Upon confirmation, stock is atomically moved from `quantity_available` to `quantity_reserved`. If any item does not have enough stock, the order is rejected immediately, preventing double-selling.

5. **Clear Order Lifecycle & State Transitions:**
   - Orders follow a strict, linear flow:  
     `PENDING → CONFIRMED → PROCESSING → READY_FOR_PICKUP → COMPLETED` (or `CANCELLED`).
   - If an order is cancelled, reserved inventory is automatically released back to `quantity_available`.
   - When an order is completed, reserved inventory is marked as fulfilled.

6. **Store Operator Fulfillment:**
   - Store staff can view incoming orders assigned to their store, pick and pack items, and advance the order status until it is ready for customer pickup or dispatch.

7. **Audit Trail & Event Logging:**
   - Key business events (`ORDER_PLACED`, `ORDER_STATUS_CHANGED`, `INVENTORY_RESTORED`) and timestamps (`created_at`, `updated_at`) are captured so all stock adjustments and order changes can be audited and traced.
