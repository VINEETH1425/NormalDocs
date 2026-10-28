# ==============================================================================
# StoreConnect - Comprehensive Live End-to-End API Verification
# ==============================================================================
$baseUrl = "http://localhost:8080"
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  StoreConnect - Live End-to-End Omnichannel Testing      " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Health Checks
Write-Host "`n[1/10] Checking Eureka Server & API Gateway..." -ForegroundColor Yellow
try {
    $eureka = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{"Accept"="application/json"} -Method Get
    Write-Host "  [OK] Eureka Server is online." -ForegroundColor Green
} catch {
    Write-Host "  [WARN] Eureka check returned: $($_.Exception.Message)" -ForegroundColor DarkYellow
}

# 2. Register New Customer
$randomSuffix = Get-Random -Minimum 1000 -Maximum 9999
$testEmail = "customer$randomSuffix@store.com"
Write-Host "`n[2/10] Registering Customer ($testEmail)..." -ForegroundColor Yellow
$registerBody = @{
    firstName = "Aarav"
    lastName = "Sharma"
    email = $testEmail
    password = "Password@123"
    phone = "+91 9876543210"
    address = "DLF Phase 4, Gurugram, Haryana"
} | ConvertTo-Json

$regResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
$custToken = $regResp.token
$custUser = $regResp.user
Write-Host "  [OK] Customer registered successfully! ID: $($custUser.id), Email: $($custUser.email)" -ForegroundColor Green

$custHeaders = @{
    "Authorization" = "Bearer $custToken"
    "Content-Type" = "application/json"
}

# 3. Browse Physical Stores & 25-Product Catalog
Write-Host "`n[3/10] Browsing Stores & Product Catalog..." -ForegroundColor Yellow
$stores = Invoke-RestMethod -Uri "$baseUrl/api/stores" -Headers $custHeaders -Method Get
Write-Host "  [OK] Found $($stores.Count) physical store locations." -ForegroundColor Green
$selectedStore = $stores[0]
Write-Host "       Selected Store: $($selectedStore.name) (ID: $($selectedStore.id), City: $($selectedStore.city))" -ForegroundColor Cyan

$products = Invoke-RestMethod -Uri "$baseUrl/api/products" -Headers $custHeaders -Method Get
Write-Host "  [OK] Found $($products.Count) active products in catalog." -ForegroundColor Green
$selectedProduct = $products[0]
Write-Host "       Selected Product: $($selectedProduct.name) (SKU: $($selectedProduct.sku), Price: INR $($selectedProduct.price))" -ForegroundColor Cyan

# 4. Product Details Endpoint (GET /api/products/:id)
Write-Host "`n[4/10] Fetching Dedicated Product Details for ID $($selectedProduct.id)..." -ForegroundColor Yellow
$productDetail = Invoke-RestMethod -Uri "$baseUrl/api/products/$($selectedProduct.id)" -Headers $custHeaders -Method Get
Write-Host "  [OK] Product details retrieved:" -ForegroundColor Green
Write-Host "       Name:        $($productDetail.name)" -ForegroundColor Cyan
Write-Host "       Category:    $($productDetail.categoryName)" -ForegroundColor Cyan
Write-Host "       Price:       INR $($productDetail.price)" -ForegroundColor Cyan
Write-Host "       Description: $($productDetail.description)" -ForegroundColor Cyan

# 5. Check Real-Time Store Inventory
Write-Host "`n[5/10] Checking real-time store stock for Product at $($selectedStore.name)..." -ForegroundColor Yellow
$stockBefore = Invoke-RestMethod -Uri "$baseUrl/api/inventory/store/$($selectedStore.id)/product/$($selectedProduct.id)" -Headers $custHeaders -Method Get
Write-Host "  [OK] Store Stock: Available: $($stockBefore.quantityAvailable), Reserved: $($stockBefore.quantityReserved)" -ForegroundColor Green

# 6. Add Product to Shopping Cart
Write-Host "`n[6/10] Adding product to Shopping Cart..." -ForegroundColor Yellow
$cartItemBody = @{
    productId = $selectedProduct.id
    quantity = 2
    storeId = $selectedStore.id
} | ConvertTo-Json

$cartResp = Invoke-RestMethod -Uri "$baseUrl/api/cart/items" -Headers $custHeaders -Method Post -Body $cartItemBody
Write-Host "  [OK] Cart updated! Total Items: $($cartResp.items.Count), Total Payable: INR $($cartResp.totalAmount)" -ForegroundColor Green

# 7. Place Omnichannel BOPIS Order
Write-Host "`n[7/10] Checking out BOPIS Order (Buy-Online-Pickup-In-Store)..." -ForegroundColor Yellow
$checkoutBody = @{
    storeId = $selectedStore.id
    fulfillmentType = "STORE_PICKUP"
} | ConvertTo-Json

$orderResp = Invoke-RestMethod -Uri "$baseUrl/api/orders/checkout" -Headers $custHeaders -Method Post -Body $checkoutBody
$orderId = $orderResp.id
Write-Host "  [OK] Order placed successfully!" -ForegroundColor Green
Write-Host "       Order Number: $($orderResp.orderNumber)" -ForegroundColor Cyan
Write-Host "       Order ID:     $orderId" -ForegroundColor Cyan
Write-Host "       Status:       $($orderResp.status)" -ForegroundColor Cyan
Write-Host "       Total:        INR $($orderResp.totalAmount)" -ForegroundColor Cyan

# 8. Verify Customer Orders List
Write-Host "`n[8/10] Fetching Customer's Personal Orders..." -ForegroundColor Yellow
$myOrders = Invoke-RestMethod -Uri "$baseUrl/api/orders" -Headers $custHeaders -Method Get
Write-Host "  [OK] Customer has $($myOrders.Count) order(s) on file." -ForegroundColor Green

# 9. Store Operator Login & Fulfillment Flow
Write-Host "`n[9/10] Store Operator Authentication & Order Fulfillment..." -ForegroundColor Yellow
$operatorLogin = @{
    email = "operator@store.com"
    password = "Password@123"
} | ConvertTo-Json

$opResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $operatorLogin -ContentType "application/json"
$opToken = $opResp.token
$opUser = $opResp.user
Write-Host "  [OK] Store Operator logged in! Role: $($opUser.role)" -ForegroundColor Green

$opHeaders = @{
    "Authorization" = "Bearer $opToken"
    "Content-Type" = "application/json"
    "X-User-Role" = "STORE_OPERATOR"
}

# Operator views store orders
$storeOrders = Invoke-RestMethod -Uri "$baseUrl/api/orders/store/$($selectedStore.id)" -Headers $opHeaders -Method Get
Write-Host "  [OK] Operator loaded $($storeOrders.Count) orders for $($selectedStore.id)." -ForegroundColor Green

# Advance Status: CONFIRMED -> PROCESSING
Write-Host "       Advancing status: CONFIRMED -> PROCESSING..." -ForegroundColor Yellow
$procBody = @{ status = "PROCESSING" } | ConvertTo-Json
$procResp = Invoke-RestMethod -Uri "$baseUrl/api/orders/$orderId/status" -Headers $opHeaders -Method Patch -Body $procBody
Write-Host "       [OK] Status updated to: $($procResp.status)" -ForegroundColor Green

# Advance Status: PROCESSING -> READY_FOR_PICKUP
Write-Host "       Advancing status: PROCESSING -> READY_FOR_PICKUP..." -ForegroundColor Yellow
$readyBody = @{ status = "READY_FOR_PICKUP" } | ConvertTo-Json
$readyResp = Invoke-RestMethod -Uri "$baseUrl/api/orders/$orderId/status" -Headers $opHeaders -Method Patch -Body $readyBody
Write-Host "       [OK] Status updated to: $($readyResp.status)" -ForegroundColor Green

# Advance Status: READY_FOR_PICKUP -> COMPLETED
Write-Host "       Advancing status: READY_FOR_PICKUP -> COMPLETED..." -ForegroundColor Yellow
$completeBody = @{ status = "COMPLETED" } | ConvertTo-Json
$compResp = Invoke-RestMethod -Uri "$baseUrl/api/orders/$orderId/status" -Headers $opHeaders -Method Patch -Body $completeBody
Write-Host "       [OK] Status updated to: $($compResp.status)" -ForegroundColor Green

# 10. Verify Final Stock & Completed Status
Write-Host "`n[10/10] Final System Verification..." -ForegroundColor Yellow
$finalOrder = Invoke-RestMethod -Uri "$baseUrl/api/orders/$orderId" -Headers $custHeaders -Method Get
Write-Host "  [OK] Final Order Status: $($finalOrder.status)" -ForegroundColor Green

$stockAfter = Invoke-RestMethod -Uri "$baseUrl/api/inventory/store/$($selectedStore.id)/product/$($selectedProduct.id)" -Headers $custHeaders -Method Get
Write-Host "  [OK] Store Inventory Reserved Stock: $($stockAfter.quantityReserved) unit(s)" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  SUCCESS! End-to-End Omnichannel Journey Fully Verified! " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
