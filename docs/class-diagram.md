# StoreConnect – Class Diagram

> **How to render:** Open this file in VS Code with the "Markdown Preview Mermaid Support" extension, or paste the mermaid block into [mermaid.live](https://mermaid.live).

---

```mermaid
classDiagram
    direction LR

    class Category {
        -Long id
        -String name
        -String description
        +getId() Long
        +getName() String
        +setName(String)
    }

    class ProductStatus {
        &lt;&lt;enumeration&gt;&gt;
        ACTIVE
        INACTIVE
    }

    class Product {
        -Long id
        -String name
        -String description
        -double price
        -String imageUrl
        -ProductStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getId() Long
        +getName() String
        +getPrice() double
        +isActive() boolean
    }

    class StoreStatus {
        &lt;&lt;enumeration&gt;&gt;
        ACTIVE
        INACTIVE
    }

    class Store {
        -Long id
        -String name
        -String address
        -String city
        -String pincode
        -StoreStatus status
        +getId() Long
        +getName() String
        +isActive() boolean
    }

    class Customer {
        -Long id
        -String firstName
        -String lastName
        -String email
        -String passwordHash
        -String phone
        -LocalDateTime registeredAt
        +getId() Long
        +getFullName() String
        +getEmail() String
    }

    class Inventory {
        -Long id
        -int quantity
        -int reservedQuantity
        -LocalDateTime lastUpdated
        +getAvailableQuantity() int
        +reserve(int qty) boolean
        +release(int qty) void
        +restock(int qty) void
        +isInStock() boolean
    }

    class Cart {
        -Long id
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +addItem(CartItem) void
        +removeItem(Long productId) void
        +updateItemQuantity(Long productId, int qty) void
        +getTotalAmount() double
        +getItemCount() int
        +clear() void
    }

    class CartItem {
        -Long id
        -int quantity
        -double priceAtAddition
        +getSubtotal() double
    }

    class FulfillmentType {
        &lt;&lt;enumeration&gt;&gt;
        STORE_PICKUP
        HOME_DELIVERY
    }

    class OrderStatus {
        &lt;&lt;enumeration&gt;&gt;
        CREATED
        CONFIRMED
        PROCESSING
        READY
        DELIVERED
        CANCELLED
    }

    class Order {
        -String id
        -String orderNumber
        -String customerId
        -String storeId
        -OrderStatus status
        -FulfillmentType fulfillmentType
        -double totalAmount
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +calculateTotal() double
        +transitionTo(OrderStatus) boolean
        +canTransitionTo(OrderStatus) boolean
        +isCancellable() boolean
    }

    class OrderItem {
        -String id
        -String orderId
        -String productId
        -int quantity
        -double unitPrice
        -double subtotal
        +getSubtotal() double
    }

    %% ── Relationships ──

    Product "many" --> "1" Category : belongs to
    Product --> ProductStatus : has

    Store --> StoreStatus : has

    Inventory "many" --> "1" Product : tracks stock of
    Inventory "many" --> "1" Store : held at

    Cart "1" --> "1" Customer : owned by
    Cart "1" --> "0..*" CartItem : contains
    CartItem "many" --> "1" Product : references

    Order "many" --> "1" Customer : placed by
    Order "many" --> "1" Store : fulfilled at
    Order "1" --> "1..*" OrderItem : contains
    Order --> OrderStatus : has status
    Order --> FulfillmentType : uses
    OrderItem "many" --> "1" Product : references
```

---

## Entity Descriptions

| Entity | Purpose |
|--------|---------|
| **Product** | Core catalog item with name, price, description and category |
| **Category** | Groups products (e.g., Electronics, Clothing, Groceries) |
| **Store** | Physical retail location represented digitally |
| **Customer** | Registered user who shops on the platform |
| **Inventory** | Tracks quantity and reserved stock of a Product at a specific Store |
| **Cart** | Customer's shopping cart containing CartItems |
| **CartItem** | A line item in the cart (product + quantity + price snapshot) |
| **Order** | A placed order with lifecycle status and fulfillment store |
| **OrderItem** | A line item within an order (product + quantity + price at order time) |

## Key Design Decisions

1. **Price snapshot in CartItem & OrderItem** – Captures the price at the time of cart addition / order placement so that later price changes don't affect existing orders.
2. **Inventory has `reservedQuantity`** – `availableQuantity = quantity - reservedQuantity`. This allows checking availability vs. committed stock.
3. **OrderStatus is an enum with valid transitions** – The `canTransitionTo()` method enforces the state machine (`CREATED → CONFIRMED → PROCESSING → READY → DELIVERED | CANCELLED`).
4. **Cart is 1:1 with Customer** – Each customer has exactly one active cart.
5. **Relational Database Parity** – `Cart` and `CartItem` map directly to `carts` and `cart_items` in the relational schema for cross-device persistence.
