# StoreConnect – Domain Model Classes, Methods & Enums Specification

**Project:** StoreConnect – Omnichannel Retail Platform  
**Jira Subtask:** `ST-OO-01: Classes and Its Methods Specification Documentation`  
**Package Base:** `com.storeconnect.domain`  
**Source Directory:** [`src/main/java/com/storeconnect/domain`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain)  

---

## 1. Domain Enums

Enums are pure value enumerations without business methods or code logic.

### 1.1 Enum: `OrderStatus`
Source: [`src/main/java/com/storeconnect/domain/OrderStatus.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/OrderStatus.java)

Represents the order lifecycle states. State transition validation is handled by the `Order` aggregate root class.

```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED
}
```

**Constants:**
- `PENDING`: Order placed, pending stock confirmation.
- `CONFIRMED`: Inventory confirmed and reserved.
- `PROCESSING`: Store staff picking and packing items.
- `READY_FOR_PICKUP`: Staged at store ready for customer collection.
- `COMPLETED`: Order successfully handed over to customer (Terminal State).
- `CANCELLED`: Order cancelled, reserved stock released (Terminal State).

---

### 1.2 Enum: `FulfillmentType`
Source: [`src/main/java/com/storeconnect/domain/FulfillmentType.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/FulfillmentType.java)

Specifies the delivery/pickup channel chosen by the customer.

```java
public enum FulfillmentType {
    STORE_PICKUP,
    HOME_DELIVERY
}
```

**Constants:**
- `STORE_PICKUP`: Buy-Online-Pickup-In-Store (BOPIS) at assigned store.
- `HOME_DELIVERY`: Dispatched from store to customer address.

---

### 1.3 Enum: `PaymentStatus`
Source: [`src/main/java/com/storeconnect/domain/PaymentStatus.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/PaymentStatus.java)

Tracks financial transaction states for orders.

```java
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
```

**Constants:**
- `PENDING`: Payment awaiting capture.
- `PAID`: Payment successfully charged.
- `FAILED`: Payment authorization failed.
- `REFUNDED`: Payment returned to customer upon order cancellation.

---

## 2. Domain Model Classes & Method Signatures

### 2.1 Class: `Category`
Source: [`src/main/java/com/storeconnect/domain/Category.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/Category.java)

Represents a product catalog department.

**Fields:**
- `private String id`
- `private String name`
- `private String description`

| Method Signature | Parameters | Returns | Description |
|:---|:---|:---:|:---|
| `getId()`, `setId(String id)` | `String id` | `String` / `void` | Category ID getter/setter. |
| `getName()`, `setName(String name)` | `String name` | `String` / `void` | Department name getter/setter. |
| `getDescription()`, `setDescription(String d)` | `String description` | `String` / `void` | Department description getter/setter. |

---

### 2.2 Class: `Product`
Source: [`src/main/java/com/storeconnect/domain/Product.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/Product.java)

Represents catalog products.

**Fields:**
- `private String id`
- `private String sku`
- `private String name`
- `private String description`
- `private String categoryId`
- `private BigDecimal price`
- `private boolean active`

| Method Signature | Parameters | Returns | Description |
|:---|:---|:---:|:---|
| `getId()`, `setId(String id)` | `String id` | `String` / `void` | Product ID getter/setter. |
| `getSku()`, `setSku(String sku)` | `String sku` | `String` / `void` | SKU getter/setter. |
| `getName()`, `setName(String name)` | `String name` | `String` / `void` | Product display name getter/setter. |
| `getDescription()`, `setDescription(String d)` | `String description` | `String` / `void` | Product description getter/setter. |
| `getCategoryId()`, `setCategoryId(String id)` | `String categoryId` | `String` / `void` | Category reference getter/setter. |
| `getPrice()`, `setPrice(BigDecimal price)` | `BigDecimal price` | `BigDecimal` / `void` | Selling price getter/setter. |
| `isActive()`, `setActive(boolean active)` | `boolean active` | `boolean` / `void` | Active visibility flag getter/setter. |

---

### 2.3 Class: `Store`
Source: [`src/main/java/com/storeconnect/domain/Store.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/Store.java)

Represents a physical retail store location acting as a fulfillment hub.

**Fields:**
- `private String id`
- `private String name`
- `private String code`
- `private String address`
- `private String city`
- `private String phone`
- `private boolean active`

| Method Signature | Parameters | Returns | Description |
|:---|:---|:---:|:---|
| `getId()`, `setId(String id)` | `String id` | `String` / `void` | Store ID getter/setter. |
| `getName()`, `setName(String name)` | `String name` | `String` / `void` | Store name getter/setter. |
| `getCode()`, `setCode(String code)` | `String code` | `String` / `void` | Store short code getter/setter. |
| `getAddress()`, `setAddress(String addr)` | `String address` | `String` / `void` | Physical address getter/setter. |
| `getCity()`, `setCity(String city)` | `String city` | `String` / `void` | Operating city getter/setter. |
| `getPhone()`, `setPhone(String phone)` | `String phone` | `String` / `void` | Phone number getter/setter. |
| `isActive()`, `setActive(boolean active)` | `boolean active` | `boolean` / `void` | Store operational status getter/setter. |

---

### 2.4 Class: `Customer`
Source: [`src/main/java/com/storeconnect/domain/Customer.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/Customer.java)

Represents a registered platform customer.

**Fields:**
- `private String id`
- `private String firstName`
- `private String lastName`
- `private String email`
- `private String phone`
- `private String address`

| Method Signature | Parameters | Returns | Description |
|:---|:---|:---:|:---|
| `getFullName()` | None | `String` | Returns `firstName + " " + lastName`. |
| `getId()`, `setId(String id)` | `String id` | `String` / `void` | Customer ID getter/setter. |
| `getFirstName()`, `setFirstName(String fn)` | `String firstName` | `String` / `void` | First name getter/setter. |
| `getLastName()`, `setLastName(String ln)` | `String lastName` | `String` / `void` | Last name getter/setter. |
| `getEmail()`, `setEmail(String email)` | `String email` | `String` / `void` | Email address getter/setter. |
| `getPhone()`, `setPhone(String phone)` | `String phone` | `String` / `void` | Phone number getter/setter. |
| `getAddress()`, `setAddress(String addr)` | `String address` | `String` / `void` | Delivery address getter/setter. |

---

### 2.5 Class: `Inventory`
Source: [`src/main/java/com/storeconnect/domain/Inventory.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/Inventory.java)

Encapsulates store-level stock calculations and validation invariants.

**Fields:**
- `private String id`
- `private String storeId`
- `private String productId`
- `private int quantityAvailable`
- `private int quantityReserved`
- `private int reorderThreshold`
- `private LocalDateTime updatedAt`

| Method Signature | Parameters | Returns | Description & Invariants |
|:---|:---|:---:|:---|
| `hasSufficientStock(int requestedQty)` | `int requestedQty` | `boolean` | Returns `true` if `requestedQty > 0` and `quantityAvailable >= requestedQty`. |
| `reserve(int quantity)` | `int quantity` | `void` | Atomically subtracts quantity from available and adds to reserved. Throws `IllegalArgumentException` if insufficient stock. |
| `release(int quantity)` | `int quantity` | `void` | Decrements reserved and restores available (e.g., on order cancellation). |
| `fulfill(int quantity)` | `int quantity` | `void` | Decrements reserved stock upon customer pickup/dispatch completion. |
| `restock(int quantity)` | `int quantity` | `void` | Increments `quantityAvailable` when store receives replenishments. |
| `isLowStock()` | None | `boolean` | Returns `true` if `quantityAvailable <= reorderThreshold`. |
| `getQuantityAvailable()` | None | `int` | Available shelf stock count getter. |
| `getQuantityReserved()` | None | `int` | Reserved committed stock count getter. |

---

### 2.6 Class: `Cart` and `CartItem`
Sources: [`src/main/java/com/storeconnect/domain/Cart.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/Cart.java) & [`src/main/java/com/storeconnect/domain/CartItem.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/CartItem.java)

Models the pre-checkout customer shopping basket.

**`Cart` Fields:**
- `private String customerId`
- `private String selectedStoreId`
- `private final Map<String, CartItem> items`

| Method Signature | Parameters | Returns | Description & Logic |
|:---|:---|:---:|:---|
| `addItem(Product p, int qty)` | `Product product, int quantity` | `void` | Adds item or increments existing quantity in cart map. |
| `updateQuantity(String prodId, int newQty)` | `String productId, int newQuantity` | `void` | Updates quantity. If `newQty <= 0`, removes item from cart. |
| `removeItem(String prodId)` | `String productId` | `void` | Removes product entry from cart map. |
| `calculateTotal()` | None | `BigDecimal` | Uses Java Streams to map `CartItem::getSubtotal` and reduce to total sum. |
| `getTotalItemCount()` | None | `int` | Uses `items.values().stream().mapToInt(CartItem::getQuantity).sum()`. |
| `clear()` | None | `void` | Clears all items after successful checkout. |
| `isEmpty()` | None | `boolean` | Returns `true` if cart contains no items. |
| `getItems()` | None | `Map<String, CartItem>` | Returns unmodifiable map of items. |

**`CartItem` Fields:**
- `private Product product`
- `private int quantity`

| Method Signature | Parameters | Returns | Description & Logic |
|:---|:---|:---:|:---|
| `getSubtotal()` | None | `BigDecimal` | Calculates `product.getPrice() * quantity`. |
| `getProduct()`, `setProduct(Product p)` | `Product product` | `Product` / `void` | Product getter/setter. |
| `getQuantity()`, `setQuantity(int q)` | `int quantity` | `int` / `void` | Quantity getter/setter. |

---

### 2.7 Class: `Order` (Aggregate Root) and `OrderItem`
Sources: [`src/main/java/com/storeconnect/domain/Order.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/Order.java) & [`src/main/java/com/storeconnect/domain/OrderItem.java`](file:///d:/PS_CPS_Project/src/main/java/com/storeconnect/domain/OrderItem.java)

Represents confirmed customer commitments tied to a physical store location. Enforces the order lifecycle state machine.

**`Order` Fields:**
- `private String id`
- `private String orderNumber`
- `private String customerId`
- `private String storeId`
- `private OrderStatus status`
- `private FulfillmentType fulfillmentType`
- `private BigDecimal totalAmount`
- `private LocalDateTime createdAt, updatedAt`
- `private final List<OrderItem> items`

| Method Signature | Parameters | Returns | Description & Logic |
|:---|:---|:---:|:---|
| `addItem(OrderItem item)` | `OrderItem item` | `void` | Appends item, associates `orderId`, and recalculates total. |
| `calculateTotal()` | None | `void` | Streams line items and sets `totalAmount = sum(item.subtotal)`. |
| `canTransitionTo(OrderStatus next)` | `OrderStatus nextStatus` | `boolean` | Checks if transition from current `status` to `nextStatus` is legally allowed. |
| `transitionTo(OrderStatus next)` | `OrderStatus nextStatus` | `void` | Validates transition via `canTransitionTo(next)`. Throws `IllegalStateException` if invalid. Updates `status` and `updatedAt`. |
| `setItems(List<OrderItem> list)` | `List<OrderItem> list` | `void` | Replaces items collection and recalculates `totalAmount`. |
| `getItems()` | None | `List<OrderItem>` | Returns unmodifiable list of line items. |
| `getStatus()`, `setStatus(OrderStatus s)` | `OrderStatus status` | `OrderStatus` / `void` | Status getter/setter. |
| `getFulfillmentType()` | None | `FulfillmentType` | Fulfillment type getter. |
| `getTotalAmount()` | None | `BigDecimal` | Total order amount getter. |

**State Transition Matrix (enforced by `Order::transitionTo`):**
- `PENDING` $\rightarrow$ `CONFIRMED`, `CANCELLED`
- `CONFIRMED` $\rightarrow$ `PROCESSING`, `CANCELLED`
- `PROCESSING` $\rightarrow$ `READY_FOR_PICKUP`, `CANCELLED`
- `READY_FOR_PICKUP` $\rightarrow$ `COMPLETED`
- `COMPLETED` $\rightarrow$ `[]` (Terminal State)
- `CANCELLED` $\rightarrow$ `[]` (Terminal State)

**`OrderItem` Fields:**
- `private String id`
- `private String orderId`
- `private String productId`
- `private String productName`
- `private int quantity`
- `private BigDecimal unitPrice`
- `private BigDecimal subtotal`

| Method Signature | Parameters | Returns | Description & Logic |
|:---|:---|:---:|:---|
| `getSubtotal()` | None | `BigDecimal` | Returns purchase snapshot subtotal: `unitPrice * quantity`. |
| `getQuantity()`, `setQuantity(int qty)` | `int quantity` | `int` / `void` | Line quantity getter/setter. |
| `getUnitPrice()`, `setUnitPrice(BigDecimal p)` | `BigDecimal unitPrice` | `BigDecimal` / `void` | Unit price snapshot getter/setter. |
| `getProductId()`, `setProductId(String id)` | `String productId` | `String` / `void` | Product ID reference getter/setter. |
| `getProductName()` | None | `String` | Product name snapshot getter. |
