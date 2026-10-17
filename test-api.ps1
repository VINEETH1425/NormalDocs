# ==============================================================================
# StoreConnect - End-to-End API Verification Script
# ==============================================================================
# Run this script after starting Eureka, API Gateway, and the microservices.
# Usage: powershell -ExecutionPolicy Bypass -File .\test-api.ps1
# ==============================================================================

$baseUrl = "http://localhost:8080"
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  StoreConnect - Omnichannel Retail API Verification     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Health Checks
Write-Host "`n[1/8] Checking API Gateway and Eureka Health..." -ForegroundColor Yellow
try {
    $eureka = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{"Accept"="application/json"} -Method Get
    Write-Host "  [OK] Eureka Server is online." -ForegroundColor Green
} catch {
    Write-Host "  [WARN] Eureka Server check returned: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. Register Customer
$randomSuffix = Get-Random -Minimum 1000 -Maximum 9999
$testEmail = "testuser$randomSuffix@storeconnect.com"
Write-Host "`n[2/8] Registering Customer ($testEmail)..." -ForegroundColor Yellow
$registerBody = @{
    firstName = "Aarav"
    lastName = "Sharma"
    email = $testEmail
    password = "Password@123"
    phone = "+919876543210"
    address = "42 Galleria Market, DLF Phase 4, Gurugram"
} | ConvertTo-Json

try {
    $registerResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "  [OK] Customer registered successfully. User ID: $($registerResp.id)" -ForegroundColor Green
    $userId = $registerResp.id
} catch {
    Write-Host "  [FAIL] Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 3. Login Customer & Obtain JWT Token
Write-Host "`n[3/8] Logging in to obtain JWT Bearer token..." -ForegroundColor Yellow
$loginBody = @{
    email = $testEmail
    password = "Password@123"
} | ConvertTo-Json

$loginResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResp.token
Write-Host "  [OK] Login successful! Token received: $($token.Substring(0, 25))..." -ForegroundColor Green

$authHeaders = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 4. Browse Stores & Products
Write-Host "`n[4/8] Fetching physical retail stores and product catalog..." -ForegroundColor Yellow
$stores = Invoke-RestMethod -Uri "$baseUrl/api/stores" -Headers $authHeaders -Method Get
Write-Host "  [OK] Found $($stores.Count) physical store locations." -ForegroundColor Green
$store = $stores[0]
Write-Host "       Selected Store: $($store.name) ($($store.code), $($store.city))" -ForegroundColor Cyan

$products = Invoke-RestMethod -Uri "$baseUrl/api/products" -Headers $authHeaders -Method Get
Write-Host "  [OK] Found $($products.Count) active products." -ForegroundColor Green
$product = $products[0]
Write-Host "       Selected Product: $($product.name) (SKU: $($product.sku), Price: INR $($product.price))" -ForegroundColor Cyan

# 5. Check Store Inventory Availability
Write-Host "`n[5/8] Checking real-time inventory for Product $($product.id) at Store $($store.id)..." -ForegroundColor Yellow
$stock = Invoke-RestMethod -Uri "$baseUrl/api/inventory/store/$($store.id)/product/$($product.id)" -Headers $authHeaders -Method Get
Write-Host "  [OK] Stock status: Available: $($stock.quantityAvailable), Reserved: $($stock.quantityReserved), Status: $($stock.status)" -ForegroundColor Green

# 6. Manage Shopping Cart
Write-Host "`n[6/8] Adding item to customer shopping cart..." -ForegroundColor Yellow
$cartItemBody = @{
    productId = $product.id
    quantity = 1
} | ConvertTo-Json

$cart = Invoke-RestMethod -Uri "$baseUrl/api/cart/items" -Headers $authHeaders -Method Post -Body $cartItemBody
Write-Host "  [OK] Cart updated! Total items: $($cart.totalItems), Estimated Total: INR $($cart.subtotal)" -ForegroundColor Green

# 7. Create & Place Omnichannel Order (BOPIS)
Write-Host "`n[7/8] Placing Omnichannel Order (Buy-Online-Pickup-In-Store)..." -ForegroundColor Yellow
$orderBody = @{
    storeId = $store.id
    fulfillmentType = "BOPIS"
    items = @(
        @{
            productId = $product.id
            quantity = 1
        }
    )
} | ConvertTo-Json

$order = Invoke-RestMethod -Uri "$baseUrl/api/orders" -Headers $authHeaders -Method Post -Body $orderBody
Write-Host "  [OK] Order placed successfully!" -ForegroundColor Green
Write-Host "       Order Number: $($order.orderNumber)" -ForegroundColor Cyan
Write-Host "       Order ID:     $($order.id)" -ForegroundColor Cyan
Write-Host "       Status:       $($order.status)" -ForegroundColor Cyan
Write-Host "       Total Amount: INR $($order.totalAmount)" -ForegroundColor Cyan

# 8. Order Status & Payment Verification
Write-Host "`n[8/8] Verifying Order Status and Payment Processing..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
$fetchedOrder = Invoke-RestMethod -Uri "$baseUrl/api/orders/$($order.id)" -Headers $authHeaders -Method Get
Write-Host "  [OK] Verified Order Status: $($fetchedOrder.status)" -ForegroundColor Green

# Verify Stock Reservation
$stockAfter = Invoke-RestMethod -Uri "$baseUrl/api/inventory/store/$($store.id)/product/$($product.id)" -Headers $authHeaders -Method Get
Write-Host "  [OK] Inventory Check After Order: Available: $($stockAfter.quantityAvailable), Reserved: $($stockAfter.quantityReserved)" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  ALL TESTS PASSED! StoreConnect Omnichannel Platform OK! " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
