param(
    [switch]$Stop,
    [switch]$Restart,
    [switch]$Status,
    [switch]$Logs,
    [switch]$Clean,
    [switch]$Help
)

$ErrorActionPreference = "SilentlyContinue"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

function Write-ColorOutput($ForegroundColor, $Message) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    Write-Output $Message
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Success($Message) { Write-ColorOutput Green "[SUCCESS] $Message" }
function Write-Info($Message) { Write-ColorOutput Cyan "[INFO] $Message" }
function Write-Warn($Message) { Write-ColorOutput Yellow "[WARNING] $Message" }
function Write-Err($Message) { Write-ColorOutput Red "[ERROR] $Message" }

$Containers = @(
    @{ Name = "user_db_mysql"; Service = "auth-service"; Port = "3306" },
    @{ Name = "catalog_db_mysql"; Service = "catalog-service"; Port = "3307" },
    @{ Name = "order_db_mysql"; Service = "order-service"; Port = "3308" },
    @{ Name = "chat_db_mysql"; Service = "chat-service"; Port = "3309" },
    @{ Name = "document_db_mysql"; Service = "document-service"; Port = "3310" },
    @{ Name = "document_minio"; Service = "minio"; Port = "9000/9001" },
    @{ Name = "orderflow_rabbitmq"; Service = "rabbitmq"; Port = "5672/15672" },
    @{ Name = "orderflow_prometheus"; Service = "prometheus"; Port = "9090" },
    @{ Name = "orderflow_grafana"; Service = "grafana"; Port = "3001" }
)

function Show-Help {
    Write-Host ""
    Write-Host "OrderFlow Platform - Database Management Script" -ForegroundColor Cyan
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\start-databases.ps1 [options]" -ForegroundColor White
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  (no options)     Start all databases and services"
    Write-Host "  -Stop            Stop all containers"
    Write-Host "  -Restart         Restart all containers"
    Write-Host "  -Status          Show container status"
    Write-Host "  -Logs            Show container logs"
    Write-Host "  -Clean           Stop and remove all containers and volumes"
    Write-Host "  -Help            Show this help"
    Write-Host ""
    Write-Host "Services and Ports:" -ForegroundColor Yellow
    Write-Host "  auth-service     -> MySQL on port 3306 (user_db)"
    Write-Host "  catalog-service  -> MySQL on port 3307 (catalog_db)"
    Write-Host "  order-service    -> MySQL on port 3308 (order_db)"
    Write-Host "  chat-service     -> MySQL on port 3309 (chat_db)"
    Write-Host "  document-service -> MySQL on port 3310 + MinIO on ports 9000/9001"
    Write-Host "  rabbitmq         -> RabbitMQ on port 5672, Console: 15672"
    Write-Host "  prometheus       -> Prometheus on port 9090"
    Write-Host "  grafana          -> Grafana on port 3001"
    Write-Host ""
}

function Get-ContainerStatus {
    Write-Host ""
    Write-Host "OrderFlow Database Status" -ForegroundColor Cyan
    Write-Host "=========================" -ForegroundColor Cyan
    Write-Host ""

    foreach ($container in $Containers) {
        $status = docker ps --filter "name=$($container.Name)" --format "{{.Status}}" 2>$null

        Write-Host "  [$($container.Service)] " -NoNewline
        Write-Host $container.Name -ForegroundColor White -NoNewline
        Write-Host " -> " -NoNewline

        if ($status) {
            Write-Host "Running" -ForegroundColor Green -NoNewline
            Write-Host " (Port: $($container.Port))" -ForegroundColor Gray
        }
        else {
            Write-Host "Stopped" -ForegroundColor Red
        }
    }
    Write-Host ""
}

function Start-AllServices {
    Write-Info "Starting all OrderFlow services..."

    # Сначала остановим конфликтующие контейнеры
    Write-Info "Cleaning up old containers..."
    foreach ($container in $Containers) {
        docker stop $container.Name 2>$null
        docker rm $container.Name 2>$null
    }

    Push-Location $ProjectRoot
    try {
        docker-compose up -d
        if ($LASTEXITCODE -eq 0) {
            Write-Success "All services started successfully!"
        }
        else {
            Write-Err "Failed to start some services"
        }
    }
    finally {
        Pop-Location
    }
}

function Stop-AllServices {
    Write-Info "Stopping all OrderFlow services..."

    Push-Location $ProjectRoot
    try {
        docker-compose down
        Write-Success "All services stopped"
    }
    finally {
        Pop-Location
    }
}

function Clean-AllServices {
    Write-Warn "Stopping and removing all containers and volumes..."

    Push-Location $ProjectRoot
    try {
        docker-compose down -v --remove-orphans
        Write-Success "All containers and volumes removed"
    }
    finally {
        Pop-Location
    }
}

function Show-Logs {
    Push-Location $ProjectRoot
    try {
        docker-compose logs --tail 100
    }
    finally {
        Pop-Location
    }
}

function Show-ConnectionInfo {
    Write-Host ""
    Write-Host "Connection Information:" -ForegroundColor Yellow
    Write-Host "  MySQL Username: app_user"
    Write-Host "  MySQL Password: app_password"
    Write-Host ""
    Write-Host "MinIO Console:" -ForegroundColor Yellow
    Write-Host "  URL: http://localhost:9001"
    Write-Host "  Username: minioadmin"
    Write-Host "  Password: minioadmin123"
    Write-Host ""
    Write-Host "RabbitMQ Console:" -ForegroundColor Yellow
    Write-Host "  URL: http://localhost:15672"
    Write-Host "  Username: orderflow"
    Write-Host "  Password: orderflow123"
    Write-Host ""
    Write-Host "Prometheus:" -ForegroundColor Yellow
    Write-Host "  URL: http://localhost:9090"
    Write-Host ""
    Write-Host "Grafana:" -ForegroundColor Yellow
    Write-Host "  URL: http://localhost:3001"
    Write-Host "  Username: admin"
    Write-Host "  Password: admin123"
    Write-Host ""
}

# =============================================================================
# Main Script Logic
# =============================================================================

if ($Help) {
    Show-Help
    exit 0
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  OrderFlow Platform - Database Manager" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($Status) {
    Get-ContainerStatus
    exit 0
}

if ($Logs) {
    Show-Logs
    exit 0
}

if ($Clean) {
    Clean-AllServices
    exit 0
}

if ($Stop) {
    Stop-AllServices
    exit 0
}

if ($Restart) {
    Stop-AllServices
    Start-Sleep -Seconds 3
    Start-AllServices
    Start-Sleep -Seconds 10
    Get-ContainerStatus
    Show-ConnectionInfo
    exit 0
}

# Default action: start all services
Start-AllServices
Write-Host ""
Write-Info "Waiting for services to be ready..."
Start-Sleep -Seconds 15
Get-ContainerStatus
Show-ConnectionInfo

