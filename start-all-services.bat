@echo off
TITLE StoreConnect Microservices Launcher
COLOR 0A

echo ==============================================================================
echo               StoreConnect Omnichannel Retail Platform Launcher
echo ==============================================================================
echo.
echo NOTE: Ensure PostgreSQL is running on port 5432 and Apache Kafka is on port 9092.
echo.
pause

echo.
echo [1/6] Starting Eureka Service Discovery Server (Port 8761)...
start "StoreConnect :: Eureka Server [8761]" cmd /k "cd /d %~dp0 && mvn spring-boot:run -pl eureka-server"
echo Waiting 15 seconds for Eureka to initialize...
timeout /t 15 /nobreak >nul

echo.
echo [2/6] Starting API Gateway (Port 8080)...
start "StoreConnect :: API Gateway [8080]" cmd /k "cd /d %~dp0 && mvn spring-boot:run -pl api-gateway"
echo Waiting 10 seconds for API Gateway...
timeout /t 10 /nobreak >nul

echo.
echo [3/6] Starting Store Inventory Service (Port 8082)...
start "StoreConnect :: Inventory Service [8082]" cmd /k "cd /d %~dp0 && mvn spring-boot:run -pl store-inventory-service"

echo.
echo [4/6] Starting Payment Service (Port 8084)...
start "StoreConnect :: Payment Service [8084]" cmd /k "cd /d %~dp0 && mvn spring-boot:run -pl payment-service"

echo.
echo [5/6] Starting User Service (Port 8081)...
start "StoreConnect :: User Service [8081]" cmd /k "cd /d %~dp0 && mvn spring-boot:run -pl user-service"

echo.
echo [6/6] Starting Order Service (Port 8083)...
start "StoreConnect :: Order Service [8083]" cmd /k "cd /d %~dp0 && mvn spring-boot:run -pl order-service"

echo.
echo ==============================================================================
echo All 6 StoreConnect services launched in dedicated terminal windows!
echo - Eureka Dashboard: http://localhost:8761
echo - API Gateway:      http://localhost:8080
echo.
echo Run 'powershell -ExecutionPolicy Bypass -File .\test-api.ps1' to verify endpoints!
echo ==============================================================================
