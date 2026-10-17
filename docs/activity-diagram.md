# StoreConnect – Activity Diagrams

> **How to render:** Open this file in VS Code with the "Markdown Preview Mermaid Support" extension, or paste the mermaid block into [mermaid.live](https://mermaid.live).

---

## 1. Customer Order Placement Journey (Primary Flow)

This is the **core business journey** that the CPS must demonstrate end-to-end.

```mermaid
flowchart TD
    Start([🟢 Start]) --> Login{Customer Logged In?}
    Login -->|No| Register[Register / Login]
    Register --> Browse
    Login -->|Yes| Browse[Browse Product Catalog]

    Browse --> Search[Search or Filter by Name / Category]
    Search --> ViewProduct[View Product Details]

    ViewProduct --> SelectStore[Select a Store / Location]
    SelectStore --> CheckAvail{Stock Available\nat Selected Store?}

    CheckAvail -->|No| ShowUnavail[Show: Out of Stock at this Store]
    ShowUnavail --> SelectStore
    CheckAvail -->|Yes| AddToCart[Add Product to Cart]

    AddToCart --> ContinueShopping{Continue\nShopping?}
    ContinueShopping -->|Yes| Browse
    ContinueShopping -->|No| ViewCart[View Cart]

    ViewCart --> ModifyCart{Modify Cart?}
    ModifyCart -->|Update Quantity| UpdateQty[Update Item Quantity]
    UpdateQty --> ViewCart
    ModifyCart -->|Remove Item| RemoveItem[Remove Item from Cart]
    RemoveItem --> ViewCart
    ModifyCart -->|Proceed| PlaceOrder[Place Order]

    PlaceOrder --> ValidateInventory{System:\nValidate All Items\nInventory Available?}

    ValidateInventory -->|Insufficient Stock| RejectOrder[Reject: Insufficient Stock\nNotify Customer]
    RejectOrder --> ViewCart

    ValidateInventory -->|All Items Available| ReserveStock[System: Reserve Inventory\nfor Each Item]
    ReserveStock --> AssignStore[System: Associate Order\nwith Fulfillment Store]
    AssignStore --> CreateOrder[System: Create Order\nStatus = CONFIRMED]
    CreateOrder --> PublishEvent[System: Publish Kafka Event\n'OrderConfirmed']
    PublishEvent --> ShowConfirmation[Show Order Confirmation\nto Customer]
    ShowConfirmation --> End([🔴 End])

    %% Cancellation side-path
    ShowConfirmation -.-> CancelCheck{Customer\nCancels Order?}
    CancelCheck -->|Yes| ReleaseStock[System: Release\nReserved Stock]
    ReleaseStock --> CancelOrder[System: Update Status\nto CANCELLED]
    CancelOrder --> PublishCancel[System: Publish Kafka Event\n'OrderCancelled']
    PublishCancel --> End

    style Start fill:#22c55e,color:#fff
    style End fill:#ef4444,color:#fff
    style ValidateInventory fill:#f59e0b,color:#000
    style RejectOrder fill:#ef4444,color:#fff
    style CreateOrder fill:#3b82f6,color:#fff
    style PublishEvent fill:#8b5cf6,color:#fff
    style PublishCancel fill:#8b5cf6,color:#fff
```

---

## 2. Store Operator – Order Fulfillment Flow

```mermaid
flowchart TD
    Start([🟢 Start]) --> Login[Store Operator Login]
    Login --> Dashboard[View Store Dashboard]
    Dashboard --> ViewOrders[View Incoming Orders\nfor this Store]

    ViewOrders --> SelectOrder[Select an Order]
    SelectOrder --> ReviewItems[Review Order Items\n& Required Stock]

    ReviewItems --> StartProcessing[Update Status:\nCONFIRMED → PROCESSING]
    StartProcessing --> PrepareItems[Prepare / Pick Items\nfrom Store Inventory]
    PrepareItems --> MarkReady[Update Status:\nPROCESSING → READY]
    MarkReady --> CustomerPickup[Customer Picks Up /\nDelivery Dispatched]
    CustomerPickup --> MarkDelivered[Update Status:\nREADY → DELIVERED]
    MarkDelivered --> NotifyCustomer[Customer Sees:\nOrder Delivered ✅]
    NotifyCustomer --> End([🔴 End])

    style Start fill:#22c55e,color:#fff
    style End fill:#ef4444,color:#fff
    style StartProcessing fill:#f59e0b,color:#000
    style MarkReady fill:#3b82f6,color:#fff
    style MarkDelivered fill:#22c55e,color:#fff
```

---

## 3. Order Status State Machine

```mermaid
stateDiagram-v2
    [*] --> CREATED : Customer places order

    CREATED --> CONFIRMED : Inventory validated & reserved
    CREATED --> CANCELLED : Customer cancels / Stock unavailable

    CONFIRMED --> PROCESSING : Store operator starts fulfillment
    CONFIRMED --> CANCELLED : Customer cancels → stock released

    PROCESSING --> READY : Items prepared for pickup/delivery
    PROCESSING --> CANCELLED : Issue during fulfillment → stock released

    READY --> DELIVERED : Customer receives order

    DELIVERED --> [*]
    CANCELLED --> [*]
```

---

## 4. Inventory Management Flow

```mermaid
flowchart TD
    Start([🟢 Start]) --> Login[Store Operator Login]
    Login --> ViewInventory[View Store Inventory]

    ViewInventory --> Action{Action?}

    Action -->|Restock| SelectProduct[Select Product]
    SelectProduct --> EnterQty[Enter Restock Quantity]
    EnterQty --> UpdateStock[System: Increase\nInventory Quantity]
    UpdateStock --> ViewInventory

    Action -->|Check Low Stock| LowStockReport[System: List Products\nwith Qty Below Threshold]
    LowStockReport --> ViewInventory

    Action -->|View Reservations| ShowReserved[Show Reserved Qty\nvs Available Qty]
    ShowReserved --> ViewInventory

    style Start fill:#22c55e,color:#fff
```

---

## Flow Summary

| Flow | Actors Involved | Key System Actions |
|------|----------------|-------------------|
| **Order Placement** | Customer, System | Validate inventory → Reserve stock → Create order → Publish Kafka event |
| **Order Fulfillment** | Store Operator, System | CONFIRMED → PROCESSING → READY → DELIVERED |
| **Order Cancellation** | Customer/Operator, System | Release reserved stock → Update status to CANCELLED |
| **Inventory Management** | Store Operator | Restock, view reservations, low-stock alerts |
