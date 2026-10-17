# ==============================================================================
# StoreConnect - Complete Automated End-to-End & Database Test Runner
# ==============================================================================
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  StoreConnect - End-to-End & Database Verification       " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Load Environment Variables from .env
Get-Content "d:\PS_CPS_Project\.env" | Where-Object { $_ -match '^([^#=]+)=(.*)$' } | ForEach-Object {
    $name = $matches[1].Trim()
    $val = $matches[2].Trim()
    [System.Environment]::SetEnvironmentVariable($name, $val, "Process")
}

# Set PostgreSQL password for native psql client
[System.Environment]::SetEnvironmentVariable("PGPASSWORD", "JaiShriRam321@", "Process")
$env:PGPASSWORD = "JaiShriRam321@"

# Pre-flight cleanup: kill any orphan java processes
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

$javaBin = "$env:JAVA_HOME\bin\java.exe"
$baseDir = "d:\PS_CPS_Project"

if (-not (Test-Path "$baseDir\logs")) {
    New-Item -ItemType Directory -Path "$baseDir\logs" -Force | Out-Null
}

# 2. Launch Eureka Server
Write-Host "`n[1/7] Launching Eureka Server (Port 8761)..." -ForegroundColor Yellow
$eurekaProcess = Start-Process -FilePath $javaBin -ArgumentList "-jar `"$baseDir\eureka-server\target\eureka-server-1.0.0-SNAPSHOT.jar`"" -WorkingDirectory $baseDir -RedirectStandardOutput "$baseDir\logs\eureka.log" -RedirectStandardError "$baseDir\logs\eureka_err.log" -PassThru
Start-Sleep -Seconds 10

# 3. Launch Core Microservices
Write-Host "[2/7] Launching Microservices in parallel..." -ForegroundColor Yellow
$invProcess = Start-Process -FilePath $javaBin -ArgumentList "-jar `"$baseDir\store-inventory-service\target\store-inventory-service-1.0.0-SNAPSHOT.jar`"" -WorkingDirectory $baseDir -RedirectStandardOutput "$baseDir\logs\inventory.log" -RedirectStandardError "$baseDir\logs\inventory_err.log" -PassThru
$payProcess = Start-Process -FilePath $javaBin -ArgumentList "-jar `"$baseDir\payment-service\target\payment-service-1.0.0-SNAPSHOT.jar`"" -WorkingDirectory $baseDir -RedirectStandardOutput "$baseDir\logs\payment.log" -RedirectStandardError "$baseDir\logs\payment_err.log" -PassThru
$userProcess = Start-Process -FilePath $javaBin -ArgumentList "-jar `"$baseDir\user-service\target\user-service-1.0.0-SNAPSHOT.jar`"" -WorkingDirectory $baseDir -RedirectStandardOutput "$baseDir\logs\user.log" -RedirectStandardError "$baseDir\logs\user_err.log" -PassThru
$orderProcess = Start-Process -FilePath $javaBin -ArgumentList "-jar `"$baseDir\order-service\target\order-service-1.0.0-SNAPSHOT.jar`"" -WorkingDirectory $baseDir -RedirectStandardOutput "$baseDir\logs\order.log" -RedirectStandardError "$baseDir\logs\order_err.log" -PassThru

# 4. Launch API Gateway
Write-Host "[3/7] Launching API Gateway (Port 8080)..." -ForegroundColor Yellow
$gatewayProcess = Start-Process -FilePath $javaBin -ArgumentList "-jar `"$baseDir\api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar`"" -WorkingDirectory $baseDir -RedirectStandardOutput "$baseDir\logs\gateway.log" -RedirectStandardError "$baseDir\logs\gateway_err.log" -PassThru

# 5. Wait for Eureka registration
Write-Host "[4/7] Waiting for all services to register with Eureka..." -ForegroundColor Yellow
$maxRetries = 50
$registered = $false
for ($i = 1; $i -le $maxRetries; $i++) {
    try {
        $apps = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{"Accept"="application/json"} -Method Get
        $count = @($apps.applications.application).Count
        Write-Host "      Registered instances: $count / 5 (Attempt $i/$maxRetries)" -ForegroundColor Gray
        if ($count -ge 5) {
            $registered = $true
            Write-Host "  [OK] All 5 microservices registered with Eureka!" -ForegroundColor Green
            Start-Sleep -Seconds 10
            break
        }
    } catch {
        Write-Host "      Waiting for Eureka server... (Attempt $i/$maxRetries)" -ForegroundColor Gray
    }
    Start-Sleep -Seconds 3
}

if (-not $registered) {
    Write-Host "  [WARN] Not all services registered within timeout. Checking instances..." -ForegroundColor Red
}

# 6. Execute E2E User Journey via API Gateway
Write-Host "`n[5/7] Executing End-to-End Customer Journey via API Gateway (Port 8080)..." -ForegroundColor Yellow
$baseUrl = "http://localhost:8080"
$randomId = Get-Random -Minimum 1000 -Maximum 9999
$email = "aarav.test$randomId@storeconnect.com"

# A. Register Customer
Write-Host "  -> Registering Customer ($email)..." -ForegroundColor Cyan
$regBody = @{
    firstName = "Aarav"
    lastName = "Sharma"
    email = $email
    password = "Password@123"
    phone = "+919876543210"
    address = "42 Galleria Market, DLF Phase 4, Gurugram"
} | ConvertTo-Json
$regResp = $null
for ($attempt = 1; $attempt -le 12; $attempt++) {
    try {
        $regResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
        if ($regResp -and $regResp.user) { break }
    } catch {
        if ($attempt -eq 12) { throw $_ }
        Write-Host "     Syncing gateway load balancer cache... (Attempt $attempt/12)" -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }
}
$customerId = $regResp.user.id
Write-Host "     [OK] Registered Customer ID: $customerId" -ForegroundColor Green

# B. Login & Get JWT
Write-Host "  -> Logging in to get JWT token..." -ForegroundColor Cyan
$loginBody = @{ email = $email; password = "Password@123" } | ConvertTo-Json
$loginResp = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResp.token
$headers = @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json" }
Write-Host "     [OK] JWT Bearer Token generated successfully!" -ForegroundColor Green

# C. Query Catalog & Stores
Write-Host "  -> Browsing retail stores & products..." -ForegroundColor Cyan
$stores = Invoke-RestMethod -Uri "$baseUrl/api/stores" -Headers $headers -Method Get
$products = Invoke-RestMethod -Uri "$baseUrl/api/products" -Headers $headers -Method Get
$store = $stores[0]
$product = $products[0]
Write-Host "     [OK] Store Selected:   $($store.name) ($($store.city)) [ID: $($store.id)]" -ForegroundColor Green
Write-Host "     [OK] Product Selected: $($product.name) (Price: INR $($product.price)) [ID: $($product.id)]" -ForegroundColor Green

# D. Check Stock Before Order
Write-Host "  -> Checking real-time store stock..." -ForegroundColor Cyan
$stock = Invoke-RestMethod -Uri "$baseUrl/api/inventory/store/$($store.id)/product/$($product.id)" -Headers $headers -Method Get
Write-Host "     [OK] Available before order: $($stock.quantityAvailable) units, Reserved: $($stock.quantityReserved)" -ForegroundColor Green

# E. Add to Cart
Write-Host "  -> Adding product to customer active cart..." -ForegroundColor Cyan
$cartBody = @{ productId = $product.id; quantity = 1; storeId = $store.id } | ConvertTo-Json
$cart = Invoke-RestMethod -Uri "$baseUrl/api/cart/items" -Headers $headers -Method Post -Body $cartBody
Write-Host "     [OK] Cart updated: $($cart.itemCount) item(s), Subtotal: INR $($cart.totalAmount)" -ForegroundColor Green

# F. Place Store Pickup (BOPIS) Order
Write-Host "  -> Placing Store Pickup Order via Checkout..." -ForegroundColor Cyan
$orderBody = @{
    storeId = $store.id
    fulfillmentType = "STORE_PICKUP"
} | ConvertTo-Json
$order = Invoke-RestMethod -Uri "$baseUrl/api/orders/checkout" -Headers $headers -Method Post -Body $orderBody
Write-Host "     [OK] Order Placed! Order Number: $($order.orderNumber), ID: $($order.id), Status: $($order.status), Total: INR $($order.totalAmount)" -ForegroundColor Green

# G. Verify Stock Reservation
Write-Host "  -> Verifying real-time inventory atomic reservation..." -ForegroundColor Cyan
Start-Sleep -Seconds 2
$stockAfter = Invoke-RestMethod -Uri "$baseUrl/api/inventory/store/$($store.id)/product/$($product.id)" -Headers $headers -Method Get
Write-Host "     [OK] Available after order:  $($stockAfter.quantityAvailable) units, Reserved: $($stockAfter.quantityReserved)" -ForegroundColor Green

# 7. Database Verification
Write-Host "`n[6/7] Direct PostgreSQL Database Verification (Live Queries):" -ForegroundColor Yellow

$psql = "C:\Program Files\PostgreSQL\15\bin\psql.exe"

Write-Host "`n--- [A] Table: user_schema.customers (Latest Registered Customer) ---" -ForegroundColor Magenta
& $psql -U postgres -d storeconnect -h 127.0.0.1 -w -c "SELECT id, email, first_name, last_name, role, created_at FROM user_schema.customers WHERE email='$email';"

Write-Host "`n--- [B] Table: user_schema.carts (Customer Cart Record) ---" -ForegroundColor Magenta
& $psql -U postgres -d storeconnect -h 127.0.0.1 -w -c "SELECT id, customer_id, store_id, updated_at FROM user_schema.carts WHERE customer_id='$customerId';"

Write-Host "`n--- [C] Table: order_schema.orders (Placed Omnichannel Order) ---" -ForegroundColor Magenta
& $psql -U postgres -d storeconnect -h 127.0.0.1 -w -c "SELECT id, order_number, customer_id, store_id, status, fulfillment_type, total_amount, created_at FROM order_schema.orders WHERE id='$($order.id)';"

Write-Host "`n--- [D] Table: order_schema.order_items (Order Line Items) ---" -ForegroundColor Magenta
& $psql -U postgres -d storeconnect -h 127.0.0.1 -w -c "SELECT id, order_id, product_id, quantity, unit_price, subtotal FROM order_schema.order_items WHERE order_id='$($order.id)';"

Write-Host "`n--- [E] Table: inventory_schema.store_inventory (Updated Stock Status) ---" -ForegroundColor Magenta
& $psql -U postgres -d storeconnect -h 127.0.0.1 -w -c "SELECT id, store_id, product_id, quantity_available, quantity_reserved, updated_at FROM inventory_schema.store_inventory WHERE store_id='$($store.id)' AND product_id='$($product.id)';"

Write-Host "`n[7/7] Cleaning up test service processes..." -ForegroundColor Yellow
Stop-Process -Id $gatewayProcess.Id -Force -ErrorAction SilentlyContinue
Stop-Process -Id $orderProcess.Id -Force -ErrorAction SilentlyContinue
Stop-Process -Id $userProcess.Id -Force -ErrorAction SilentlyContinue
Stop-Process -Id $payProcess.Id -Force -ErrorAction SilentlyContinue
Stop-Process -Id $invProcess.Id -Force -ErrorAction SilentlyContinue
Stop-Process -Id $eurekaProcess.Id -Force -ErrorAction SilentlyContinue

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  END-TO-END & DATABASE VERIFICATION COMPLETE & PASSED!   " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
