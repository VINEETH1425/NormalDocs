# StoreConnect: Master Code Walkthrough, Syntax Dissection & Architectural Defense Manual

> **Project**: StoreConnect Omnichannel Retail Microservices Platform  
> **Technologies**: Java 25 (OpenJDK 25.0.3) | Spring Boot 4.1.1 | Spring Cloud 2025.1.3 | PostgreSQL 15 | Apache Kafka | Netflix Eureka | Spring Cloud Gateway (WebFlux) | Zero-Docker Native Host  
> **Target Audience**: Technical Evaluators, System Architecture Mentors, Viva / Interview Panels  

---

## Table of Contents
1. [Architecture Blueprint & Microservice Topology](#1-architecture-blueprint--microservice-topology)
2. [Part 1: Flow-First Customer Journey Walkthrough (Line-by-Line Code Syntax)](#2-part-1-flow-first-customer-journey-walkthrough)
   - [Hop 1: Customer Registration & BCrypt Hashing](#hop-1-customer-registration--bcrypt-hashing)
   - [Hop 2: Authentication & JJWT 0.12 Token Generation](#hop-2-authentication--jjwt-012-token-generation)
   - [Hop 3: Gateway Interception, HMAC Validation & Header Enrichment](#hop-3-gateway-interception-hmac-validation--header-enrichment)
   - [Hop 4: Downstream Internal Security Context Population](#hop-4-downstream-internal-security-context-population)
   - [Hop 5: Catalog Query & Physical Store Stock Lookup](#hop-5-catalog-query--physical-store-stock-lookup)
   - [Hop 6: Active Cart Addition & Synchronous Price Verification via Feign](#hop-6-active-cart-addition--synchronous-price-verification-via-feign)
   - [Hop 7: BOPIS Checkout & Atomic Inventory Reservation](#hop-7-bopis-checkout--atomic-inventory-reservation)
   - [Hop 8: Asynchronous Kafka Event Broadcast & Graceful Offline Handling](#hop-8-asynchronous-kafka-event-broadcast--graceful-offline-handling)
3. [Part 2: Component-Level Syntax Deep Dive (Annotations, Patterns & Error Handling)](#3-part-2-component-level-syntax-deep-dive)
   - [Deep Dive A: Centralized `@RestControllerAdvice` & Error Mapping](#deep-dive-a-centralized-restcontrolleradvice--error-mapping)
   - [Deep Dive B: Modern Java 25 & Spring Boot 4 Syntactic Evolutions](#deep-dive-b-modern-java-25--spring-boot-4-syntactic-evolutions)
   - [Deep Dive C: Programmatic Java DSL RouteLocator vs. YAML](#deep-dive-c-programmatic-java-dsl-routelocator-vs-yaml)
   - [Deep Dive D: JPA Entity Lifecycle & Cascade Persistence Management](#deep-dive-d-jpa-entity-lifecycle--cascade-persistence-management)
   - [Deep Dive E: Declarative OpenFeign Inter-Service Communication & Interceptors](#deep-dive-e-declarative-openfeign-inter-service-communication--interceptors)
   - [Deep Dive F: Domain Enums & Exception Polymorphism Hierarchy](#deep-dive-f-domain-enums--exception-polymorphism-hierarchy)
4. [Part 3: Decision Matrix — Design Patterns Implemented by YOU](#4-part-3-decision-matrix--design-patterns-implemented-by-you)
5. [Part 4: Decision Matrix — Key Architectural Choices Made by YOU](#5-part-4-decision-matrix--key-architectural-choices-made-by-you)
6. [Part 5: Syntax Traps & Evaluator "Gotchas" (The Real Test Questions)](#6-part-5-syntax-traps--evaluator-gotchas)
7. [Part 6: Oral Defense Q&A on Architectural Decisions Made by YOU (Q1 to Q15)](#7-part-6-oral-defense-qa-on-architectural-decisions-made-by-you)
8. [Part 7: Live Demo Execution & SQL Verification Commands](#8-part-7-live-demo-execution--sql-verification-commands)

---

## 1. Architecture Blueprint & Microservice Topology

StoreConnect bridges the gap between digital e-commerce and brick-and-mortar stores. It coordinates 6 autonomous microservices communicating via synchronous REST (OpenFeign) and asynchronous events (Kafka), bound logically to PostgreSQL schemas:

```
[ Client / Browser / Mobile App ]
              │
              ▼ HTTP :8080
 ┌──────────────────────────────────────────────────────────────┐
 │                API GATEWAY (Spring WebFlux)                 │
 │  - Programmatic RouteLocator (lb://...)                     │
 │  - JwtAuthGlobalFilter (HMAC-SHA256, Claims Enrichment)     │
 │  - CORS Policy & Edge Security                              │
 └──────┬──────────────────┬─────────────────┬──────────────────┘
        │ lb://...         │ lb://...        │ lb://...
        ▼                  ▼                 ▼
 ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
 │ USER-SERVICE │   │ STORE-INV    │   │ ORDER-SERVICE│
 │ Port: 8081   │   │ Port: 8082   │   │ Port: 8083   │
 │ Auth & Carts │   │ Catalog/Stock│   │ BOPIS Orders │
 └──────┬───────┘   └──────┬───────┘   └──────┬───────┘
        │                  │                  │
        │                  │ Feign (Sync)     │ Feign (Sync)
        │                  │ ◄────────────────┼───────────────
        │                  │                  │
        │                  │                  │ Kafka: order-events
        │                  │                  ▼
        │                  │           ┌──────────────┐
        │                  │           │ PAYMENT-SERV │
        │                  │           │ Port: 8084   │
        │                  │           │ Transactions │
        │                  │           └──────┬───────┘
        │                  │                  │
        ▼                  ▼                  ▼
 ┌──────────────────────────────────────────────────────────────┐
 │                PostgreSQL 15 (Port: 5432)                    │
 │  ┌──────────────┐┌──────────────┐┌──────────────┐┌──────────┐ │
 │  │ user_schema  ││inv_schema    ││ order_schema ││ pay_schema│
 │  └──────────────┘└──────────────┘└──────────────┘└──────────┘ │
 └──────────────────────────────────────────────────────────────┘
```

| Microservice | Port | Spring Boot / Java | Key Responsibility | Database Schema |
| :--- | :---: | :---: | :--- | :--- |
| **eureka-server** | `8761` | `4.1.1` / `Java 25` | Service Discovery & Heartbeat Registry | In-Memory Registry |
| **api-gateway** | `8080` | `4.1.1` / `Java 25` | Reactive Edge Gateway, JWT Verification, Load Balancing | Stateless |
| **user-service** | `8081` | `4.1.1` / `Java 25` | Customer Profiles, BCrypt Auth, Active Cart | `user_schema` |
| **store-inventory-service** | `8082` | `4.1.1` / `Java 25` | Physical Stores, Categories, Products, Store Stock | `inventory_schema` |
| **order-service** | `8083` | `4.1.1` / `Java 25` | BOPIS Checkout, Cascade Order Persistence, Feign Sync | `order_schema` |
| **payment-service** | `8084` | `4.1.1` / `Java 25` | Transaction Ledger, Kafka Event Consumer, Refunds | `payment_schema` |

---

## 2. Part 1: Flow-First Customer Journey Walkthrough

### Hop 1: Customer Registration & BCrypt Hashing
**Client Request**: `POST http://localhost:8080/api/auth/register`

#### Code in [AuthController.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/controller/AuthController.java#L29-L32):
```java
@Operation(summary = "Register a new customer account")
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
}
```
- **Line 29 `@Operation(...)`**: OpenAPI 3 (SpringDoc) metadata describing the endpoint in Swagger UI without polluting business logic.
- **Line 30 `@PostMapping("/register")`**: Maps HTTP POST requests to this method. Combined with class-level `@RequestMapping("/api/auth")`, the full URI is `/api/auth/register`.
- **Line 31 `@Valid @RequestBody RegisterRequest request`**:
  - `@RequestBody`: Instructs Spring's `HttpMessageConverter` to deserialize the incoming JSON payload into the `RegisterRequest` Java record.
  - `@Valid`: Activates Jakarta Bean Validation (Hibernate Validator). It inspects constraints on `RegisterRequest`:
    ```java
    public record RegisterRequest(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,
        String phone,
        String address
    ) {}
    ```
    *If any field fails validation, Spring aborts before entering the method body and throws `MethodArgumentNotValidException` (intercepted by `GlobalExceptionHandler`).*
- **Line 32 `ResponseEntity.status(HttpStatus.CREATED).body(...)`**: Adheres to REST best practices by returning **HTTP 201 Created** rather than a generic 200 OK.

#### Code in [AuthService.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/service/AuthService.java#L35-L65):
```java
@Transactional
public AuthResponse register(RegisterRequest request) {
    if (customerRepository.existsByEmail(request.email().trim().toLowerCase())) {
        throw new DuplicateEmailException(request.email());
    }

    String customerId = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String hashedPassword = passwordEncoder.encode(request.password());

    Customer customer = Customer.builder()
            .id(customerId)
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .email(request.email().trim().toLowerCase())
            .passwordHash(hashedPassword)
            .phone(request.phone())
            .address(request.address())
            .role(CustomerRole.CUSTOMER)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    Customer saved = customerRepository.save(customer);
    String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());

    return new AuthResponse(token, jwtUtil.getExpirationMs(), toDTO(saved));
}
```
- **Line 35 `@Transactional`**: Enforces database transaction boundaries. If any exception occurs after saving (e.g. token generation failure), the database row automatically rolls back.
- **Line 36 `request.email().trim().toLowerCase()`**: Defensive normalization prevents duplicate accounts with different capitalization (e.g., `User@Domain.com` vs `user@domain.com`).
- **Line 37 `throw new DuplicateEmailException(request.email())`**: Throws domain exception; inherits from `StoreConnectException` with `HttpStatus.CONFLICT` (HTTP 409).
- **Line 41 `passwordEncoder.encode(request.password())`**: Executes **BCrypt hashing with work factor 12**. BCrypt automatically generates a cryptographic salt and embeds it in the hash string (e.g., `$2a$12$...`), preventing rainbow table attacks.
- **Line 57 `customerRepository.save(customer)`**: Spring Data JPA persists the entity into `user_schema.customers`.

---

### Hop 2: Authentication & JJWT 0.12 Token Generation
**Client Request**: `POST http://localhost:8080/api/auth/login`

#### Code in [JwtUtil.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/config/JwtUtil.java#L35-L55):
```java
public String generateToken(String userId, String email, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMs);
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

    return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
}
```
- **Line 38 `Decoders.BASE64.decode(secret)`**: Decodes the base64-encoded secret string into raw cryptographic key bytes.
- **Line 38 `Keys.hmacShaKeyFor(...)`**: Generates a strongly typed `SecretKey` using JJWT's modern security API, enforcing key sizes >= 256 bits for HMAC-SHA256 algorithms.
- **Lines 40-47 `Jwts.builder()...compact()`**:
  - JJWT 0.12 modern syntax: Uses `.subject()`, `.issuedAt()`, `.expiration()`, and `.signWith(key)` directly on the fluent builder.
  - Generates the standard compact serialized JWT string: `<header>.<payload>.<signature>`.

---

### Hop 3: Gateway Interception, HMAC Validation & Header Enrichment
**Request**: Client adds `Authorization: Bearer <token>` to any request through the API Gateway (Port 8080).

#### Code in [JwtAuthGlobalFilter.java](file:///d:/PS_CPS_Project/api-gateway/src/main/java/com/storeconnect/gateway/config/JwtAuthGlobalFilter.java#L45-L88):
```java
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Allow public endpoints without authentication
        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Extract Bearer token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            String email = claims.get("email", String.class);

            // 3. Mutate request headers for downstream microservices
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-User-Role", role != null ? role : "CUSTOMER")
                    .header("X-User-Email", email != null ? email : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException ex) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "JWT token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid JWT token: " + ex.getMessage());
        }
    }
```
- **Line 45 `Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)`**:
  - Reactive method signature for non-blocking Netty processing.
  - `ServerWebExchange` encapsulates both HTTP request and response in reactive streams.
- **Lines 69-74 `Jwts.parser().verifyWith(key)...`**:
  - Validates the token's cryptographic signature against the secret key.
  - Automatically verifies `exp` (expiration date); if expired, throws `ExpiredJwtException`.
- **Lines 81-85 `request.mutate().header(...).build()`**:
  - `ServerHttpRequest` is immutable in Spring WebFlux. The `.mutate()` method returns a builder to safely attach custom HTTP headers without thread-safety conflicts.
  - Injects `X-User-Id`, `X-User-Role`, and `X-User-Email`.
- **Line 87 `exchange.mutate().request(mutatedRequest).build()`**: Forwards the mutated exchange containing the authenticated user headers to the downstream microservice.

---

### Hop 4: Downstream Internal Security Context Population
**Microservice Execution**: How downstream services (like `user-service` and `order-service`) authenticate the request without repeating the JWT verification.

#### Code in [InternalHeaderAuthFilter.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/config/InternalHeaderAuthFilter.java#L17-L36):
```java
@Component
public class InternalHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");

        if (userId != null && role != null && !role.isBlank()) {
            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
            var authorities = List.of(new SimpleGrantedAuthority(roleName));
            var authToken = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
```
- **Line 17 `OncePerRequestFilter`**: Spring Security base class guaranteeing this filter is executed exactly once per incoming request.
- **Line 24 `request.getHeader("X-User-Id")`**: Reads the trusted header injected by our API Gateway.
- **Line 29 `new SimpleGrantedAuthority(roleName)`**: Adapts the role string to Spring Security authority specification (e.g. `ROLE_CUSTOMER`, `ROLE_ADMIN`).
- **Line 30 `new UsernamePasswordAuthenticationToken(userId, null, authorities)`**: Creates an authenticated principal token with credentials omitted (`null`).
- **Line 31 `SecurityContextHolder.getContext().setAuthentication(authToken)`**: Populates the thread-local security context. Downstream controllers can now access the current user via `SecurityContextHolder` or `@AuthenticationPrincipal`!

---

### Hop 5: Catalog Query & Physical Store Stock Lookup
**Client Request**: `GET /api/inventory/store/STORE-GGN-01/product/PROD-AUD-001`

#### Code in [InventoryController.java](file:///d:/PS_CPS_Project/store-inventory-service/src/main/java/com/storeconnect/inventory/controller/InventoryController.java#L28-L35):
```java
@Operation(summary = "Get real-time stock availability for a specific store and product")
@GetMapping("/store/{storeId}/product/{productId}")
public ResponseEntity<StoreInventoryDTO> getStoreProductInventory(
        @PathVariable String storeId,
        @PathVariable String productId) {
    return ResponseEntity.ok(inventoryService.getStoreInventory(storeId, productId));
}
```
- **Lines 31-32 `@PathVariable String storeId, @PathVariable String productId`**: Extracts URI path parameters directly into service arguments.
- **Repository SQL executed**:
  ```sql
  SELECT id, store_id, product_id, quantity_available, quantity_reserved, updated_at 
  FROM inventory_schema.store_inventory 
  WHERE store_id = 'STORE-GGN-01' AND product_id = 'PROD-AUD-001';
  ```
- **Response**: Returns real-time quantities (`quantityAvailable: 11`, `quantityReserved: 4`).

---

### Hop 6: Active Cart Addition & Synchronous Price Verification via Feign
**Client Request**: `POST /api/cart/items` with `{ productId: "PROD-AUD-001", quantity: 1, storeId: "STORE-GGN-01" }`

#### Code in [CartService.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/service/CartService.java#L55-L95):
```java
@Transactional
public CartDTO addItemToCart(String customerId, AddToCartRequest request) {
    if (request.quantity() <= 0) {
        throw new InvalidQuantityException("Quantity must be greater than zero");
    }

    // 1. Synchronously fetch live product details from store-inventory-service
    ProductDTO product;
    try {
        product = inventoryFeignClient.getProductById(request.productId());
    } catch (Exception ex) {
        throw new StoreConnectException("Unable to verify product with catalog: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    if (product == null || !product.active()) {
        throw new StoreConnectException("Product is discontinued or inactive", HttpStatus.BAD_REQUEST);
    }

    // 2. Fetch or initialize active Cart for customer
    Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseGet(() -> createNewCart(customerId, request.storeId()));

    // 3. Update or append line item
    CartItem item = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(request.productId()))
            .findFirst()
            .orElse(null);

    if (item != null) {
        item.setQuantity(item.getQuantity() + request.quantity());
        item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    } else {
        item = CartItem.builder()
                .id("CITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cart(cart)
                .productId(product.id())
                .productName(product.name())
                .unitPrice(product.price())
                .quantity(request.quantity())
                .subtotal(product.price().multiply(BigDecimal.valueOf(request.quantity())))
                .build();
        cart.getItems().add(item);
    }

    cart.recalculateTotal();
    Cart savedCart = cartRepository.save(cart);
    return toDTO(savedCart);
}
```
- **Line 62 `inventoryFeignClient.getProductById(request.productId())`**:
  - Declarative REST call via OpenFeign. Feign contacts Eureka, discovers a healthy instance of `store-inventory-service`, makes an HTTP GET request to `/internal/products/{id}`, and parses the JSON response into `ProductDTO`.
- **Lines 82-90 `CartItem.builder()...build()`**:
  - Uses the **Builder Pattern** for clean object construction.
  - Computes `subtotal` using `BigDecimal.multiply()` to avoid IEEE-754 floating-point rounding errors on currency calculations.

---

### Hop 7: BOPIS Checkout & Atomic Inventory Reservation
**Client Request**: `POST /api/orders/checkout` with `{ storeId: "STORE-GGN-01", fulfillmentType: "STORE_PICKUP" }`

#### Code in [OrderService.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/service/OrderService.java#L56-L176):
```java
@Transactional
public OrderDTO checkout(String customerId, CheckoutRequest request) {
    // 1. Fetch shopping cart from user-service
    CartDTO cart = userFeignClient.getCart(customerId);
    if (cart == null || cart.items() == null || cart.items().isEmpty()) {
        throw new CheckoutFailedException("Checkout failed: Your shopping cart is empty");
    }

    String storeId = (request.storeId() != null && !request.storeId().isBlank())
            ? request.storeId().trim()
            : (cart.storeId() != null ? cart.storeId() : "STORE-GGN-01");

    // 2. Validate physical store stock availability via Feign
    List<StockValidateItem> stockItems = cart.items().stream()
            .map(item -> new StockValidateItem(item.productId(), item.quantity()))
            .toList();

    StockValidateResponse validation = inventoryFeignClient.validateStock(
            new StockValidateRequest(storeId, stockItems)
    );

    if (validation != null && !validation.isAvailable()) {
        throw new CheckoutFailedException("Stock unavailable for selected store pickup");
    }

    // 3. Create parent Order entity
    String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String orderNumber = "ORD-" + datePrefix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

    Order order = Order.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .customerId(customerId)
            .storeId(storeId)
            .status(OrderStatus.CREATED)
            .fulfillmentType(request.fulfillmentType() != null ? request.fulfillmentType() : FulfillmentType.STORE_PICKUP)
            .totalAmount(cart.totalAmount())
            .items(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    // 4. Construct OrderItems and associate with parent Order
    List<OrderItem> orderItems = new ArrayList<>();
    for (CartDTO.CartItemDTO item : cart.items()) {
        OrderItem orderItem = OrderItem.builder()
                .id("OITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .order(order)
                .productId(item.productId())
                .quantity(item.quantity())
                .unitPrice(item.unitPrice())
                .subtotal(item.subtotal())
                .build();
        orderItems.add(orderItem);
    }
    order.setItems(orderItems);

    // 5. Cascade save Order + Items in single JPA transaction
    Order savedOrder = orderRepository.save(order);

    // 6. Synchronous atomic stock reservation at the physical store
    inventoryFeignClient.reserveStock(new StockReservationRequest(
            savedOrder.getId(),
            savedOrder.getStoreId(),
            stockItems
    ));

    // 7. Clear the customer's cart
    userFeignClient.clearCart(customerId);

    // 8. Publish ORDER_PLACED event to Kafka
    orderEventProducer.publishOrderPlaced(toEvent(savedOrder));

    return toDTO(savedOrder);
}
```
- **Line 56 `@Transactional`**: Bundles the entire sequence into a database transaction.
- **Lines 135-136 `order.setItems(orderItems); orderRepository.save(order);`**:
  - **JPA Cascade Type ALL**: Persisting `order` automatically cascades and inserts all child `OrderItem` records in `order_schema.order_items`.
- **Lines 142-146 `inventoryFeignClient.reserveStock(...)`**:
  - Executes atomic SQL update on `inventory_schema.store_inventory`:
    ```sql
    UPDATE inventory_schema.store_inventory 
    SET quantity_available = quantity_available - :qty, 
        quantity_reserved = quantity_reserved + :qty 
    WHERE store_id = :storeId AND product_id = :productId;
    ```
- **Line 153 `userFeignClient.clearCart(customerId)`**: Synchronously clears active cart line items from `user_schema.cart_items`.

---

### Hop 8: Asynchronous Kafka Event Broadcast & Graceful Offline Handling
**Execution**: Publishing `ORDER_PLACED` event for analytics and payment processing.

#### Code in [OrderEventProducer.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/kafka/OrderEventProducer.java#L19-L26):
```java
public void publishOrderPlaced(OrderEvent event) {
    log.info("Publishing ORDER_PLACED event for order {} ({})", event.orderNumber(), event.orderId());
    try {
        kafkaTemplate.send("order-events", event.orderId(), event);
    } catch (Exception e) {
        log.warn("Could not publish ORDER_PLACED event to Kafka (broker may be offline): {}", e.getMessage());
    }
}
```
- **Line 22 `kafkaTemplate.send("order-events", event.orderId(), event)`**:
  - `topic`: `"order-events"`
  - `key`: `event.orderId()` — **Partitioning Key**: Ensures all events for the same order ID are hashed to the exact same Kafka partition, guaranteeing strict chronological in-order processing!
- **Lines 23-25 `try { ... } catch (Exception e)`**:
  - **Graceful Fault Tolerance Pattern**: In development or environments where Kafka is offline, transactional order placement and stock reservation are already committed. The try-catch prevents Kafka connection timeouts from failing an already persisted order!

---

## 3. Part 2: Component-Level Syntax Deep Dive

### Deep Dive A: Centralized `@RestControllerAdvice` & Error Mapping

Look at lines 19 to 42 in [GlobalExceptionHandler.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/exception/GlobalExceptionHandler.java#L19-L54):

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StoreConnectException.class)
    public ResponseEntity<ApiErrorResponse> handleStoreConnectException(StoreConnectException ex, HttpServletRequest request) {
        log.warn("Business exception [{}]: {}", ex.getStatus(), ex.getMessage());
        ApiErrorResponse error = new ApiErrorResponse(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                UUID.randomUUID().toString()
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation error on {}: {}", request.getRequestURI(), details);
        ApiErrorResponse error = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                details,
                request.getRequestURI(),
                UUID.randomUUID().toString()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

#### Detailed Breakdown of Tricky Syntax:
1. **`@RestControllerAdvice` (Line 19)**:
   - A specialized meta-annotation combining `@ControllerAdvice` and `@ResponseBody`.
   - It intercepts exceptions thrown from **any** `@RestController` across the application context and serializes the return value directly into HTTP response JSON.
2. **`@ExceptionHandler(StoreConnectException.class)` (Line 24)**:
   - Registers this handler method for `StoreConnectException` and all of its subclasses (`UserNotFoundException`, `CartEmptyException`, `DuplicateEmailException`, etc.).
   - Eliminates the need to write separate handlers for 20+ custom domain exceptions!
3. **`ex.getStatus().value()` vs `ex.getStatus().getReasonPhrase()` (Lines 29-30)**:
   - `.value()` returns the numeric integer (e.g. `404`, `400`, `409`).
   - `.getReasonPhrase()` returns the official HTTP description (e.g. `"Not Found"`, `"Bad Request"`, `"Conflict"`).
4. **`ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(...)` (Lines 40-42)**:
   - `ex.getBindingResult()`: Accesses the Spring Validation model inspection results.
   - `.getFieldErrors()`: Retrieves the `List<FieldError>` containing errors specifically caused by invalid bean fields.
   - `.stream()`: Opens a functional Java Stream.
   - `.map(FieldError::getDefaultMessage)`: Method reference extracting the human-readable validation error string (e.g. `"Email is required"`).
   - `.collect(Collectors.joining("; "))`: Concatenates multiple field errors into a single clean string separated by semicolons.
5. **Correlation / Trace ID (`UUID.randomUUID().toString()`)**:
   - Generates a unique tracking token in the error response. Clients can provide this token to customer support to find the exact stack trace in centralized logs.

---

### Deep Dive B: Modern Java 25 & Spring Boot 4 Syntactic Evolutions

1. **Java Records for Immutability**:
   ```java
   public record StockValidateItem(String productId, Integer quantity) {}
   ```
   - Auto-generates immutable private final fields, accessor methods `productId()`, `quantity()`, canonical constructor, `equals()`, `hashCode()`, and `toString()`.
2. **Stream `.toList()` vs `.collect(Collectors.toList())`**:
   - In modern Java, `.toList()` produces an unmodifiable, lightweight list directly from the stream pipeline with better memory locality.
3. **Hibernate 7 Auto-Dialect Discovery**:
   - In previous Spring Boot versions, developers wrote:
     `spring.jpa.properties.hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect`
   - In Spring Boot 4 / Hibernate 7, manual dialect specification is **deprecated (`HHH90000025`)**. Hibernate detects PostgreSQL 15 features dynamically from JDBC connection metadata.
4. **Lombok `@Data` vs Separate `@Getter` / `@Setter`**:
   - `@Data` is a composite annotation bundling `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, and `@RequiredArgsConstructor`.
   - Replaces 10+ lines of redundant annotations on every entity.

---

### Deep Dive C: Programmatic Java DSL RouteLocator vs. YAML

In [GatewayRoutesConfig.java](file:///d:/PS_CPS_Project/api-gateway/src/main/java/com/storeconnect/gateway/config/GatewayRoutesConfig.java#L16-L45):
```java
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-auth", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                .route("user-cart", r -> r
                        .path("/api/cart/**")
                        .uri("lb://user-service"))
                .route("store-inventory-stock", r -> r
                        .path("/api/inventory/**")
                        .uri("lb://store-inventory-service"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .uri("lb://order-service"))
                .build();
    }
}
```
- **Why did we choose this over YAML?**
  In Spring Cloud Gateway 2025.1.x, defining multiple path predicates in YAML (e.g., `- Path=/api/auth/**, /api/cart/**`) can suffer from whitespace trimming issues where ` /api/cart/**` is parsed with a leading space and fails route matching with a silent 404.
  The Java DSL `RouteLocatorBuilder` is compiled, type-checked, logged at startup, and directly registers routes in Netty's routing table.

---

### Deep Dive D: JPA Entity Lifecycle & Cascade Persistence Management

In [Order.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/entity/Order.java#L45-L50):
```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private List<OrderItem> items = new ArrayList<>();
```
- **`mappedBy = "order"`**: Indicates that the child entity `OrderItem` owns the foreign key relationship via its `private Order order;` field.
- **`cascade = CascadeType.ALL`**: Operations on the parent `Order` (e.g. `save`, `delete`, `refresh`) automatically cascade to all associated `OrderItem` entities.
- **`orphanRemoval = true`**: If an `OrderItem` is removed from `order.getItems()`, JPA automatically executes a `DELETE` SQL statement in the database.
- **`fetch = FetchType.LAZY`**: Prevents child items from being fetched until explicitly accessed, preventing N+1 query performance degradation.
- **The Gotcha We Fixed**: Never call `orderItemRepository.saveAll()` separately when the parent entity already has `CascadeType.ALL`. Doing so causes Hibernate 7 to throw `NonUniqueObjectException` because the same item is attached to the session twice. Calling `orderRepository.save(order)` handles the parent and all children in one clean transactional operation.

---

### Deep Dive E: Declarative OpenFeign Inter-Service Communication & Interceptors

StoreConnect relies on Spring Cloud OpenFeign for type-safe, declarative synchronous HTTP communication between microservices without hardcoding IP addresses or ports.

#### 1. The Declarative Client Interface: [InventoryFeignClient.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/feign/InventoryFeignClient.java#L10-L21)
```java
@FeignClient(name = "store-inventory-service")
public interface InventoryFeignClient {

    @PostMapping("/internal/inventory/validate-stock")
    StockValidateResponse validateStock(@RequestBody StockValidateRequest request);

    @PostMapping("/internal/inventory/reserve")
    void reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/internal/inventory/release")
    void releaseStock(@RequestBody StockReservationRequest request);
}
```
- **Line 10 `@FeignClient(name = "store-inventory-service")`**:
  - OpenFeign scans this annotation during application startup and dynamically generates a runtime JDK dynamic proxy bean implementing `InventoryFeignClient`.
  - The `name` parameter corresponds to the logical application ID registered in **Netflix Eureka**. Feign resolves `store-inventory-service` to active physical IP:Port instances via Ribbon/Spring Cloud LoadBalancer.
- **Line 13 `@PostMapping("/internal/inventory/validate-stock")`**:
  - OpenFeign translates this method call into an HTTP POST request targeting `http://store-inventory-service:8082/internal/inventory/validate-stock`.
  - `@RequestBody`: Jackson serializes `StockValidateRequest` into outgoing JSON; the response JSON is deserialized into `StockValidateResponse`.

#### 2. Service-to-Service Security Interceptor: [FeignRequestInterceptor.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/feign/FeignRequestInterceptor.java#L8-L15)
```java
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Service-Name", "order-service");
        template.header("X-User-Role", "SERVICE");
    }
}
```
- **Line 8 `implements RequestInterceptor`**:
  - Implements the **Decorator / Interceptor Pattern**.
  - Before any outgoing HTTP request is dispatched by Feign, Spring executes this interceptor.
  - Injects `X-Service-Name: order-service` and `X-User-Role: SERVICE`, enabling downstream internal security filters to authenticate internal microservice-to-microservice traffic.

---

### Deep Dive F: Domain Enums & Exception Polymorphism Hierarchy

Look at your open files in IntelliJ:
- [GlobalExceptionHandler.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/exception/GlobalExceptionHandler.java#L24)
- [CartItemNotFoundException.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/exception/CartItemNotFoundException.java)
- [FulfillmentType.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/entity/FulfillmentType.java)

#### 1. Why `@ExceptionHandler(StoreConnectException.class)` Handles 15+ Domain Exceptions Polymorphically:
In [GlobalExceptionHandler.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/exception/GlobalExceptionHandler.java#L24-L36):
```java
@ExceptionHandler(StoreConnectException.class)
public ResponseEntity<ApiErrorResponse> handleStoreConnectException(StoreConnectException ex, HttpServletRequest request) {
    log.warn("Business exception [{}]: {}", ex.getStatus(), ex.getMessage());
    ApiErrorResponse error = new ApiErrorResponse(
            LocalDateTime.now(),
            ex.getStatus().value(),
            ex.getStatus().getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            UUID.randomUUID().toString()
    );
    return ResponseEntity.status(ex.getStatus()).body(error);
}
```
And look at [CartItemNotFoundException.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/exception/CartItemNotFoundException.java#L5-L9):
```java
public class CartItemNotFoundException extends StoreConnectException {
    public CartItemNotFoundException(String itemId) {
        super("Cart item not found with ID: " + itemId, HttpStatus.NOT_FOUND);
    }
}
```
- **The Design Pattern**: This applies the **Liskov Substitution Principle (LSP)** and **Polymorphic Exception Handling**.
- **How it Works**:
  1. Base class `StoreConnectException` extends `RuntimeException` and holds an `HttpStatus` field (`HttpStatus.NOT_FOUND`, `HttpStatus.CONFLICT`, `HttpStatus.BAD_REQUEST`).
  2. Subclasses like `CartItemNotFoundException`, `UserNotFoundException`, `DuplicateEmailException`, and `ProductNotFoundException` inherit from `StoreConnectException` and pass their specific HTTP status up via `super(message, status)`.
  3. When `CartItemNotFoundException` is thrown anywhere in `user-service`, Spring's exception resolver matches the parent `@ExceptionHandler(StoreConnectException.class)`.
  4. The handler dynamically reads `ex.getStatus().value()` (`404`) and returns an RFC 7807 compliant JSON payload with HTTP 404!
  5. **Why this decision matters**: Without this pattern, you would have to write 20 identical `@ExceptionHandler` methods in every microservice. With this pattern, 1 handler dynamically handles every current and future business exception!

#### 2. Domain Enums and `@Enumerated(EnumType.STRING)`
In [FulfillmentType.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/entity/FulfillmentType.java#L3-L6):
```java
public enum FulfillmentType {
    STORE_PICKUP,
    HOME_DELIVERY
}
```
And in [Order.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/entity/Order.java#L38-L39):
```java
@Enumerated(EnumType.STRING)
@Column(name = "fulfillment_type", nullable = false)
private FulfillmentType fulfillmentType;
```
- **Critical Evaluator Question**: *"Why did you use `@Enumerated(EnumType.STRING)` instead of the default `@Enumerated(EnumType.ORDINAL)`?"*
- **Your Defense**:
  - `EnumType.ORDINAL` saves the enum's position integer into the database column (`0` for `STORE_PICKUP`, `1` for `HOME_DELIVERY`).
  - **The Catastrophic Bug with Ordinal**: If a future engineer adds `LOCKER_PICKUP` at the beginning of the enum, `STORE_PICKUP` shifts to `1` and `HOME_DELIVERY` shifts to `2`. Every existing order in your database is now permanently corrupted!
  - `EnumType.STRING` persists the exact string literal `'STORE_PICKUP'` into PostgreSQL `varchar`. It is immune to enum reordering and makes raw SQL queries human-readable in database inspection.

---

## 4. Part 3: Decision Matrix — Design Patterns Implemented by YOU

Evaluators love asking: *"Which design patterns did you use, where are they in your code, and why did you choose them?"* Use this table as your definitive reference:

| # | Design Pattern | Category | Where in StoreConnect Code | Why YOU Chose This Pattern (Problem Solved) | What If You Didn't Use It? (Anti-Pattern) |
|---|---|---|---|---|---|
| **1** | **Builder Pattern** | Creational | [Customer.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/entity/Customer.java), [Order.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/entity/Order.java), `Jwts.builder()` | Enables clean, readable construction of complex domain entities with 10+ optional/mandatory fields. | Telescoping constructors (`new Order(id, num, cust, store, status, ...)`), prone to argument ordering bugs. |
| **2** | **Factory Method Pattern** | Creational | `ResponseEntity.status(HttpStatus.CREATED).body(...)`, `Keys.hmacShaKeyFor(...)` | Encapsulates instantiation logic and provides fluent, expressive factory APIs for HTTP responses and crypto keys. | Manual object instantiation with `new ResponseEntity<>(body, headers, status)`. |
| **3** | **Decorator / Filter Pattern** | Structural | [JwtAuthGlobalFilter.java](file:///d:/PS_CPS_Project/api-gateway/src/main/java/com/storeconnect/gateway/config/JwtAuthGlobalFilter.java), [InternalHeaderAuthFilter.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/config/InternalHeaderAuthFilter.java) | Wraps incoming HTTP requests/exchanges, intercepts headers, verifies signatures, and mutates context without touching endpoint handlers. | Duplicating token verification logic inside every single controller endpoint. |
| **4** | **Declarative Proxy Pattern** | Structural | [InventoryFeignClient.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/feign/InventoryFeignClient.java), [UserFeignClient.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/feign/UserFeignClient.java) | Developers define plain Java interfaces; Spring Cloud generates dynamic runtime HTTP proxies with Eureka resolution. | Writing 50 lines of boilerplate `RestTemplate` / `WebClient` code with manual URL concatenation. |
| **5** | **Observer / Publish-Subscribe Pattern** | Behavioral | [OrderEventProducer.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/kafka/OrderEventProducer.java) & [OrderEventConsumer.java](file:///d:/PS_CPS_Project/payment-service/src/main/java/com/storeconnect/payment/kafka/OrderEventConsumer.java) | Decouples order placement from asynchronous post-checkout actions (payments, notifications, ledger recording). | Tight synchronous coupling; payment gateway failure would crash customer checkout. |
| **6** | **Data Transfer Object (DTO) Pattern** | Architectural | Java 25 `record` types in `dto/` across all services | Isolates database persistence entities (`@Entity`) from public REST contracts; prevents accidental data leaks and over-fetching. | Exposing JPA Entities directly to the frontend, risking lazy-loading exceptions and password hash leaks. |
| **7** | **Repository Pattern** | Architectural | [CustomerRepository.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/repository/CustomerRepository.java), [OrderRepository.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/repository/OrderRepository.java) | Completely decouples domain business logic in `@Service` from underlying SQL and Hibernate persistence mechanics. | Embedding raw JDBC `PreparedStatement` and SQL query strings directly in business services. |
| **8** | **Chain of Responsibility / Controller Advice** | Behavioral | [GlobalExceptionHandler.java](file:///d:/PS_CPS_Project/user-service/src/main/java/com/storeconnect/user/exception/GlobalExceptionHandler.java) | Provides a centralized pipeline that catches exceptions bubbling up from controllers and maps them to standard RFC 7807 responses. | Try-catch blocks in every controller method returning ad-hoc error structures. |
| **9** | **API Gateway / Reverse Proxy Pattern** | Architectural | [api-gateway/](file:///d:/PS_CPS_Project/api-gateway) on Port 8080 | Single entry point for all frontend traffic; centralizes CORS, SSL termination, JWT authentication, and Eureka dynamic routing. | Exposing all 5 microservice ports (8081, 8082, 8083, 8084) directly to the public internet. |
| **10** | **Service Discovery Pattern** | Architectural | [eureka-server/](file:///d:/PS_CPS_Project/eureka-server) on Port 8761 | Dynamic service registry; microservices self-register and discover downstream endpoints by logical name (`lb://user-service`). | Hardcoding static IP addresses and ports in configuration files, breaking scalability. |
| **11** | **Fault Tolerance & Graceful Degradation** | Resilience | `try { kafkaTemplate.send(...) } catch (Exception e)` in [OrderEventProducer.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/kafka/OrderEventProducer.java) | Ensures that a failure in a secondary system (Kafka message broker) does not abort an already committed, critical transactional order. | The entire checkout transaction failing and customer money being lost because a message queue was temporarily unreachable. |

---

## 5. Part 4: Decision Matrix — Key Architectural Choices Made by YOU

Evaluators will challenge you: *"Why did you make this architectural choice instead of the alternative?"* Use this table to defend every choice:

| Architectural Area | Decision Made by YOU | Rejected Alternative | Why YOU Chose It (The Technical Trade-off) |
|---|---|---|---|
| **System Architecture** | **Microservices Architecture** | Monolithic Application | Omnichannel catalog traffic is 100x higher than checkout traffic. Microservices allow independent horizontal scaling, autonomous failure domains, and technology flexibility. |
| **Database Isolation** | **PostgreSQL Multi-Schema (`user_schema`, `inventory_schema`, etc.)** | Separate physical DB instances OR single shared schema | Separate DB instances consume excessive RAM/CPU on host infrastructure. A single shared schema allows illegal cross-domain table joins. Multi-schema provides strict logical domain boundaries at zero infrastructure overhead. |
| **Framework Version** | **Spring Boot 4.1.1 + Spring Cloud 2025.1.3 + Java 25** | Spring Boot 2.7 / 3.x (Java 11 / 17) | Future-proofs the platform on modern Jakarta EE 11 and OpenJDK 25. Takes advantage of modern Java records, virtual threads readiness, and modern security specifications. |
| **Edge Gateway Engine** | **Reactive Netty (Spring Cloud Gateway WebFlux)** | Servlet Gateway (Spring Cloud Gateway MVC / Tomcat) | Gateways are pure I/O proxies. Tomcat's thread-per-request model exhausts memory under 10k connections. WebFlux Netty uses an asynchronous event loop handling high concurrency with tiny memory footprints. |
| **Gateway Route Definition** | **Programmatic Java DSL (`RouteLocatorBuilder`)** | YAML Route Declarations (`application.yaml`) | Spring Cloud Gateway 2025.1.x has a documented comma-path whitespace parsing issue in YAML. Java DSL provides compile-time type safety, IDE autocomplete, and immediate startup validation. |
| **Hibernate SQL Dialect** | **Hibernate 7 Dynamic Auto-Discovery** | Hardcoded `hibernate.dialect: PostgreSQLDialect` | Spring Boot 4 uses Hibernate 7.4.x, which introduced dynamic JDBC metadata interrogation. Manual dialect specification is deprecated (`HHH90000025`) and can conflict with Postgres 15 features. |
| **Entity Boilerplate** | **Lombok `@Data`** | Separate `@Getter` + `@Setter` + `@ToString` | `@Data` is the consolidated standard for Java beans and entities, generating getters, setters, `equals()`, `hashCode()`, and `toString()` in one clean annotation. |
| **DTO Data Contracts** | **Java 25 `record` types** | Mutable POJO Classes with Lombok | Java `record`s are shallowly immutable, thread-safe by default, auto-generate accessors and constructors, and prevent accidental state mutation across service boundaries. |
| **Inter-Service Communication** | **Hybrid: OpenFeign (Sync) + Kafka (Async)** | 100% REST OR 100% Messaging | Checkout stock validation requires millisecond-level immediate consistency (Feign). Post-order tasks (ledger recording, customer SMS) only require eventual consistency (Kafka). Hybrid gives the best of both worlds. |
| **Deployment Mode** | **Native Windows Host Process Architecture (Zero-Docker)** | Docker / Docker Compose Containers | Native execution eliminates Docker Desktop hypervisor memory overhead (~4GB RAM saved), ensures zero port-forwarding issues, and enables direct debugger attachment to JVM processes. |
| **Entity Identification** | **Prefix-UUID String IDs (`CUST-XXXXXXXX`, `ORD-XXXXXXXX`)** | Numerical Auto-Increment (`1, 2, 3...`) | Auto-increment IDs are vulnerable to enumeration attacks (`GET /api/orders/1`, `/2`) and suffer from primary key collisions in distributed databases. Prefix-UUIDs provide collision-free security and human-readable log identification. |

---

## 6. Part 5: Syntax Traps & Evaluator "Gotchas" (The Real Test Questions)

These are the exact "trap" questions an experienced evaluator will ask to test whether you wrote the code or understand it deeply:

#### Trap 1: "In `OrderService.java`, what happens if you call `orderItemRepository.saveAll(items)` when `Order` has `CascadeType.ALL`?"
> **The Gotcha**:
> *"If `Order` has `cascade = CascadeType.ALL` on `items`, calling `orderRepository.save(order)` ALREADY attaches and persists every item in the list. If you separately call `orderItemRepository.saveAll(items)`, Hibernate 7 detects that the exact same entity instance is being attached twice to the persistence context, throwing `org.hibernate.NonUniqueObjectException: A different object with the same identifier value was already associated with the session`. We solved this by calling `order.setItems(items)` and saving ONLY the parent order once!"*

#### Trap 2: "In `CartService.java`, why did you write `.orElseGet(() -> createNewCart(...))` instead of `.orElse(createNewCart(...))`?"
> **The Gotcha**:
> *"In Java `Optional`, `.orElse(...)` is evaluated **eagerly**, meaning `createNewCart()` would be executed on every single request, even if the customer's cart was already found in the database! `.orElseGet(() -> ...)` accepts a `Supplier` functional interface and evaluates **lazily**, executing `createNewCart()` ONLY when the cart is actually missing."*

#### Trap 3: "Why did you use `BigDecimal` for currency and prices instead of `double` or `float`?"
> **The Gotcha**:
> *"`double` and `float` use binary IEEE-754 floating-point arithmetic. Certain decimal values cannot be represented exactly in binary (e.g. `0.1 + 0.2 = 0.30000000000000004`). In a retail system processing thousands of transactions, floating-point rounding errors would lead to discrepancies between customer checkout totals and payment ledgers. `BigDecimal` provides arbitrary-precision fixed-point math with exact decimal arithmetic."*

#### Trap 4: "In `JwtAuthGlobalFilter.java`, why can't you just write `request.getHeaders().add("X-User-Id", userId)`?"
> **The Gotcha**:
> *"In Spring WebFlux and Project Reactor, `ServerHttpRequest` is **strictly immutable** by design to ensure thread safety across Netty event loops. You cannot mutate existing headers. To modify headers, you must call `request.mutate().header(...).build()`, which returns a new mutated request instance wrapped inside `exchange.mutate().request(mutatedRequest).build()`."*

#### Trap 5: "What is the exact syntactic difference between `@NotNull`, `@NotEmpty`, and `@NotBlank` in Jakarta Validation?"
> **The Gotcha**:
> - `@NotNull`: Rejects `null`. Allows empty string `""` and whitespace `"   "`.
> - `@NotEmpty`: Rejects `null` and empty string `""` (`length == 0`). Allows whitespace `"   "`.
> - `@NotBlank`: Rejects `null`, empty string `""`, AND strings containing only whitespace.
> *"We use `@NotBlank` on customer names, emails, and passwords because a user submitting spaces (`"   "`) is not a valid user name or password!"*

#### Trap 6: "In `GlobalExceptionHandler.java`, why is your cursor on line 24 `@ExceptionHandler(StoreConnectException.class)`? How does it handle `CartItemNotFoundException`?"
> **The Gotcha**:
> *"Spring's `@ExceptionHandler` mechanism uses Java reflection and exception class hierarchy. `CartItemNotFoundException` extends `StoreConnectException`. When `CartItemNotFoundException` is thrown, Spring inspects the inheritance tree and finds the matching `@ExceptionHandler(StoreConnectException.class)`. Because `CartItemNotFoundException` passed `HttpStatus.NOT_FOUND` to `super()`, the handler dynamically calls `ex.getStatus().value()` and automatically emits HTTP 404 with standard RFC 7807 problem details!"*

#### Trap 7: "If an exception is thrown inside a `@Transactional` method, does it ALWAYS roll back?"
> **The Gotcha**:
> *"No! By default in Spring Framework, `@Transactional` rolls back ONLY on **unchecked exceptions** (`RuntimeException` and `Error`). It does NOT roll back on **checked exceptions** (`Exception`, `IOException`, `SQLException`) unless explicitly declared via `@Transactional(rollbackFor = Exception.class)`. Because all of our custom domain exceptions extend `StoreConnectException` (which extends `RuntimeException`), rollback is guaranteed automatically."*

#### Trap 8: "In `Order.java`, why is `fetch = FetchType.LAZY` used on `items`? What is the N+1 problem?"
> **The Gotcha**:
> *"If `fetch = FetchType.EAGER`, querying 100 orders would execute 1 SQL query for the orders and then 100 individual SQL queries for each order's items—this is the classic **N+1 query performance bottleneck**. With `FetchType.LAZY`, Hibernate defers loading child items until `order.getItems()` is explicitly accessed within the transaction."*

#### Trap 9: "In `OrderEventConsumer.java`, what is `if (totalObj instanceof Number num)`?"
> **The Gotcha**:
> *"That is **Java Pattern Matching for instanceof**. In older Java versions, you had to write `if (totalObj instanceof Number) { Number num = (Number) totalObj; }`. Pattern matching combines the type check and casting into a single atomic expression, eliminating class-cast bugs and verbosity."*

#### Trap 10: "Why is `max.block.ms` important in Kafka and why did we wrap `kafkaTemplate.send()` in a try-catch?"
> **The Gotcha**:
> *"`KafkaTemplate.send()` will attempt to fetch cluster metadata. If the Kafka broker is offline or unreachable, Kafka blocks the executing thread for `max.block.ms` (default 60 seconds) before throwing a `TimeoutException`. Wrapping the send call in a try-catch and setting fail-safe error handling ensures that our primary PostgreSQL checkout transaction completes in milliseconds without freezing the customer's browser."*

---

## 7. Part 6: Oral Defense Q&A on Architectural Decisions Made by YOU

Here are the questions your evaluators will ask, along with the defensive answers to give:

#### Q1: "Why did you build microservices instead of a monolithic architecture?"
> **Your Defense**:
> *"In omnichannel retail, catalog browsing traffic is 100x higher than checkout traffic. In a monolithic architecture, a flash sale on headphones would flood the connection pool with read requests, starving checkout transactions. By isolating catalog and inventory into `store-inventory-service` and checkout into `order-service`, they scale independently. Furthermore, failure in the payment gateway does not prevent customers from browsing physical store inventory."*

#### Q2: "Why did you use PostgreSQL Multi-Schema Isolation instead of 4 separate physical DB instances?"
> **Your Defense**:
> *"Running 4 separate physical database engines on local infrastructure requires significant RAM and CPU overhead. We achieved the exact same architectural boundary through **PostgreSQL Schema Isolation** (`user_schema`, `inventory_schema`, `order_schema`, `payment_schema`). Each microservice's JDBC connection specifies `?currentSchema=<schema_name>`. Microservices have zero knowledge of other schemas and cannot perform cross-schema joins, preserving strict domain boundaries while keeping hardware footprint minimal."*

#### Q3: "Why did you choose Reactive Spring Cloud Gateway (WebFlux/Netty) over Servlet Gateway (MVC/Tomcat)?"
> **Your Defense**:
> *"An API Gateway is an I/O-intensive proxy. Traditional Servlet containers allocate one thread per concurrent connection. Under 10,000 concurrent requests, thread allocation and context switching degrade throughput. Spring Cloud Gateway WebFlux runs on **Netty Event Loops** using non-blocking asynchronous I/O. A small thread pool handles thousands of concurrent requests with minimal memory overhead."*

#### Q4: "Why do you have both OpenFeign and Apache Kafka in the same project?"
> **Your Defense**:
> *"They solve two fundamentally different communication requirements:
> 1. **Synchronous Request-Response (OpenFeign)**: Checkout requires immediate, real-time consistency. We cannot place an order without knowing in the same millisecond whether stock exists at that physical store. Feign handles this synchronous validation.
> 2. **Asynchronous Event Broadcast (Kafka)**: Once an order is created, downstream operations like payment ledger updates, customer SMS alerts, and warehouse dispatch do not need to block the user. We publish an `ORDER_PLACED` event to Kafka, achieving eventual consistency without slowing down the customer."*

#### Q5: "How does the system prevent race conditions / overselling during store pickup orders?"
> **Your Defense**:
> *"We implement an atomic two-phase reservation at the database level. In [InventoryService.java](file:///d:/PS_CPS_Project/store-inventory-service/src/main/java/com/storeconnect/inventory/service/InventoryService.java), the stock reservation executes an atomic SQL query:
> ```sql
> UPDATE inventory_schema.store_inventory 
> SET quantity_available = quantity_available - :qty, 
>     quantity_reserved = quantity_reserved + :qty 
> WHERE store_id = :storeId AND product_id = :productId AND quantity_available >= :qty;
> ```
> PostgreSQL's row-level locking guarantees that concurrent checkout requests cannot both claim the same last item."*

#### Q6: "Why did you remove `hibernate.dialect` from all configuration files?"
> **Your Defense**:
> *"Spring Boot 4 utilizes **Hibernate 7.4.5**. Hibernate 7 introduced dynamic JDBC driver metadata interrogation. Explicitly specifying `org.hibernate.dialect.PostgreSQLDialect` in `application.yaml` triggers deprecation warning `HHH90000025`. Omitting it allows Hibernate to automatically detect the exact database version (PostgreSQL 15.13) and configure optimized SQL dialect features dynamically."*

#### Q7: "Why did you use Lombok `@Data` instead of `@Getter` and `@Setter`?"
> **Your Defense**:
> *"Entity and DTO classes require not just getters and setters, but also consistent `equals()`, `hashCode()`, `toString()`, and required argument constructors. Using `@Data` encapsulates all of these into a single standard annotation, keeping entity code clean and focused on database mappings."*

#### Q8: "Why did you implement `InternalHeaderAuthFilter` in the services if the Gateway already checks the JWT?"
> **Your Defense**:
> *"This implements the **Defense-in-Depth and Gateway Offloading Pattern**. The Gateway performs the heavy cryptographic HMAC-SHA256 signature verification once at the perimeter. It then propagates verified user claims via HTTP headers (`X-User-Id`, `X-User-Role`). The internal microservices use `InternalHeaderAuthFilter` to reconstruct Spring Security's `SecurityContext` without repeating cryptographic overhead, while still enabling method-level security (`@PreAuthorize`)."*

#### Q9: "Why did you use Java 25 `record` types for all DTOs?"
> **Your Defense**:
> *"DTOs are pure data carriers between process boundaries. Java `record`s are shallowly immutable, prevent accidental state mutation after creation, and provide built-in serialization support. They eliminate the need for Lombok on DTOs and guarantee thread safety."*

#### Q10: "What happens if Kafka goes offline during checkout? Is the order lost?"
> **Your Defense**:
> *"No. The order creation and stock reservation are already committed inside a PostgreSQL transaction. In [OrderEventProducer.java](file:///d:/PS_CPS_Project/order-service/src/main/java/com/storeconnect/order/kafka/OrderEventProducer.java), Kafka event publication is wrapped in a fail-safe try-catch block. If the Kafka broker is offline or unreachable, the producer catches the timeout exception, logs a warning, and returns the successful order to the customer. A background reconciler or Outbox Pattern can re-publish pending events."*

#### Q11: "Why did you use String IDs (`CUST-XXXXXXXX`, `ORD-XXXXXXXX`) instead of auto-incrementing integers (`1, 2, 3...`)?"
> **Your Defense**:
> *"Sequential integer IDs create security vulnerabilities (enumeration attacks, where attackers guess `GET /orders/1`, `/orders/2`). Additionally, sequential IDs in distributed microservices cause primary key collision risks. Our prefix-UUID format (`CUST-933389BA`, `ORD-20261018-FE51A2`) provides collision-free uniqueness across distributed nodes while making IDs human-readable in log traces."*

#### Q12: "Why is Eureka client cache refresh set to 3 seconds in development?"
> **Your Defense**:
> *"Eureka's default registry cache refresh interval is 30 seconds. In local development or automated testing, waiting 30-60 seconds for the Gateway to discover newly booted microservices causes intermittent 404 or 503 errors. Configuring `eureka.client.registry-fetch-interval-seconds: 3` allows rapid discovery synchronization within seconds of startup."*

#### Q13: "How does your GlobalExceptionHandler adhere to RFC 7807 (Problem Details for HTTP APIs)?"
> **Your Defense**:
> *"Our `ApiErrorResponse` encapsulates the standard attributes mandated by RFC 7807: `timestamp`, `status` (HTTP code), `error` (standard reason phrase), `message` (developer detail), `path` (the requested URI), and a unique `correlationId` (UUID for distributed trace tracking)."*

#### Q14: "Why BCrypt with strength 12 for password hashing?"
> **Your Defense**:
> *"BCrypt work factor is logarithmic: $2^{12} = 4096$ hashing rounds. Strength 10 is fast but vulnerable to modern GPU brute-force clusters. Strength 12 strikes the ideal balance between computational cost (~200ms per authentication) and security against brute-force attacks."*

#### Q15: "How does your codebase adhere to SOLID Principles?"
> **Your Defense**:
> 1. **Single Responsibility (SRP)**: Controllers only handle HTTP binding; Services only execute business logic; Repositories only interface with PostgreSQL.
> 2. **Open/Closed (OCP)**: Gateway filters and global exception handlers can be extended without modifying existing business services.
> 3. **Liskov Substitution (LSP)**: All domain exceptions inherit cleanly from `StoreConnectException` and can be handled polymorphically by the global exception handler.
> 4. **Interface Segregation (ISP)**: Focused Feign client interfaces (`UserFeignClient`, `InventoryFeignClient`) only declare methods relevant to the calling service.
> 5. **Dependency Inversion (DIP)**: Services depend on repository and Feign interfaces, not concrete implementations, injected via Spring constructor injection."*

---

## 8. Part 7: Live Demo Execution & SQL Verification Commands

### Step 1: Run Master Verification Runner
From the root directory, run:
```powershell
powershell -ExecutionPolicy Bypass -File .\run-all-and-test.ps1
```

### Step 2: Live PostgreSQL Query Verification
Open a new PowerShell window and run:
```powershell
$env:PGPASSWORD = "JaiShriRam321@"
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -d storeconnect -h 127.0.0.1
```

Run these SQL queries to show the evaluator the persisted data:

```sql
-- 1. Verify newly registered customer
SELECT id, email, first_name, last_name, role, created_at 
FROM user_schema.customers 
ORDER BY created_at DESC LIMIT 1;

-- 2. Verify active shopping cart
SELECT id, customer_id, store_id, updated_at 
FROM user_schema.carts 
ORDER BY updated_at DESC LIMIT 1;

-- 3. Verify placed BOPIS order
SELECT id, order_number, customer_id, store_id, status, fulfillment_type, total_amount, created_at 
FROM order_schema.orders 
ORDER BY created_at DESC LIMIT 1;

-- 4. Verify order line items
SELECT id, order_id, product_id, quantity, unit_price, subtotal 
FROM order_schema.order_items 
ORDER BY id DESC LIMIT 1;

-- 5. Verify real-time store stock reservation
SELECT store_id, product_id, quantity_available, quantity_reserved, updated_at 
FROM inventory_schema.store_inventory 
WHERE store_id = 'STORE-GGN-01';
```

---
*Created for StoreConnect Architecture Defense, Code Walkthrough, and Evaluation Demo.*
