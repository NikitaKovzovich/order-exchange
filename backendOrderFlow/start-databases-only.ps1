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

# ---------------------------------------------------------------------------
# Имена сервисов из docker-compose.yaml (только БД + инфраструктура)
# ---------------------------------------------------------------------------
$DbServices = @(
    "auth-mysql",
    "catalog-mysql",
    "order-mysql",
    "chat-mysql",
    "document-mysql"
)

$InfraServices = @(
    "rabbitmq",
    "minio"
)

$MonitoringServices = @(
    "prometheus",
    "grafana"
)

$AllInfraServices = $DbServices + $InfraServices + $MonitoringServices
$ServiceList = $AllInfraServices -join " "

# ---------------------------------------------------------------------------
# Вспомогательные функции вывода
# ---------------------------------------------------------------------------
function Write-ColorOutput($ForegroundColor, $Message) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    Write-Output $Message
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Success($Message) { Write-ColorOutput Green   "[SUCCESS] $Message" }
function Write-Info($Message)    { Write-ColorOutput Cyan    "[INFO]    $Message" }
function Write-Warn($Message)    { Write-ColorOutput Yellow  "[WARNING] $Message" }
function Write-Err($Message)     { Write-ColorOutput Red     "[ERROR]   $Message" }

# ---------------------------------------------------------------------------
# Описание контейнеров для статуса
# ---------------------------------------------------------------------------
$Containers = @(
    @{ Name = "auth-mysql";     Service = "auth-service";     Port = "3306" },
    @{ Name = "catalog-mysql";  Service = "catalog-service";  Port = "3307" },
    @{ Name = "order-mysql";    Service = "order-service";    Port = "3308" },
    @{ Name = "chat-mysql";     Service = "chat-service";     Port = "3309" },
    @{ Name = "document-mysql"; Service = "document-service"; Port = "3310" },
    @{ Name = "minio";          Service = "minio";            Port = "9000/9001" },
    @{ Name = "rabbitmq";       Service = "rabbitmq";         Port = "5672/15672" },
    @{ Name = "prometheus";     Service = "prometheus";       Port = "9090" },
    @{ Name = "grafana";        Service = "grafana";          Port = "3001" }
)

# ---------------------------------------------------------------------------
# Help
# ---------------------------------------------------------------------------
function Show-Help {
    Write-Host ""
    Write-Host "OrderFlow — Запуск ТОЛЬКО баз данных и инфраструктуры" -ForegroundColor Cyan
    Write-Host "======================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Использование: .\start-databases-only.ps1 [опция]" -ForegroundColor White
    Write-Host ""
    Write-Host "Опции:" -ForegroundColor Yellow
    Write-Host "  (без опций)      Запустить все БД + RabbitMQ + MinIO + мониторинг"
    Write-Host "  -Stop            Остановить все контейнеры инфраструктуры"
    Write-Host "  -Restart         Перезапустить все контейнеры инфраструктуры"
    Write-Host "  -Status          Показать статус контейнеров"
    Write-Host "  -Logs            Показать логи контейнеров"
    Write-Host "  -Clean           Остановить и удалить контейнеры + тома"
    Write-Host "  -Help            Показать эту справку"
    Write-Host ""
    Write-Host "Сервисы и порты:" -ForegroundColor Yellow
    Write-Host "  auth-mysql       -> MySQL порт 3306  (user_db)"
    Write-Host "  catalog-mysql    -> MySQL порт 3307  (catalog_db)"
    Write-Host "  order-mysql      -> MySQL порт 3308  (order_db)"
    Write-Host "  chat-mysql       -> MySQL порт 3309  (chat_db)"
    Write-Host "  document-mysql   -> MySQL порт 3310  (document_db)"
    Write-Host "  minio            -> S3 порт 9000, консоль 9001"
    Write-Host "  rabbitmq         -> AMQP порт 5672, консоль 15672"
    Write-Host "  prometheus       -> порт 9090"
    Write-Host "  grafana          -> порт 3001"
    Write-Host ""
}

# ---------------------------------------------------------------------------
# Статус
# ---------------------------------------------------------------------------
function Get-ContainerStatus {
    Write-Host ""
    Write-Host "OrderFlow — Статус инфраструктуры" -ForegroundColor Cyan
    Write-Host "==================================" -ForegroundColor Cyan
    Write-Host ""

    foreach ($c in $Containers) {
        $status = docker ps --filter "name=$($c.Name)" --format "{{.Status}}" 2>$null

        Write-Host "  [$($c.Service)] " -NoNewline
        Write-Host $c.Name -ForegroundColor White -NoNewline
        Write-Host " -> " -NoNewline

        if ($status) {
            Write-Host "Running" -ForegroundColor Green -NoNewline
            Write-Host " (Port: $($c.Port))" -ForegroundColor Gray
        } else {
            Write-Host "Stopped" -ForegroundColor Red
        }
    }
    Write-Host ""
}

# ---------------------------------------------------------------------------
# Запуск только БД + инфраструктуры
# ---------------------------------------------------------------------------
function Start-Databases {
    Write-Info "Запуск баз данных и инфраструктуры..."

    Push-Location $ProjectRoot
    try {
        # Запускаем только указанные сервисы (без микросервисов)
        $cmd = "docker-compose up -d $ServiceList"
        Write-Info "Выполняю: $cmd"
        Invoke-Expression $cmd

        if ($LASTEXITCODE -eq 0) {
            Write-Success "Все базы данных и инфраструктура запущены!"
        } else {
            Write-Err "Не удалось запустить некоторые сервисы"
        }
    } finally {
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Остановка
# ---------------------------------------------------------------------------
function Stop-Databases {
    Write-Info "Остановка баз данных и инфраструктуры..."

    Push-Location $ProjectRoot
    try {
        $cmd = "docker-compose stop $ServiceList"
        Invoke-Expression $cmd
        Write-Success "Все контейнеры остановлены"
    } finally {
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Полная очистка
# ---------------------------------------------------------------------------
function Clean-Databases {
    Write-Warn "Остановка и удаление контейнеров + томов..."

    Push-Location $ProjectRoot
    try {
        $cmd = "docker-compose rm -s -v -f $ServiceList"
        Invoke-Expression $cmd
        Write-Success "Контейнеры и тома удалены"
    } finally {
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Логи
# ---------------------------------------------------------------------------
function Show-Logs {
    Push-Location $ProjectRoot
    try {
        $cmd = "docker-compose logs --tail 100 $ServiceList"
        Invoke-Expression $cmd
    } finally {
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Информация о подключении
# ---------------------------------------------------------------------------
function Show-ConnectionInfo {
    Write-Host ""
    Write-Host "Информация для подключения:" -ForegroundColor Yellow
    Write-Host "  MySQL:" -ForegroundColor Cyan
    Write-Host "    Username: app_user"
    Write-Host "    Password: app_password"
    Write-Host "    Root password: root"
    Write-Host ""
    Write-Host "    user_db:     localhost:3306"
    Write-Host "    catalog_db:  localhost:3307"
    Write-Host "    order_db:    localhost:3308"
    Write-Host "    chat_db:     localhost:3309"
    Write-Host "    document_db: localhost:3310"
    Write-Host ""
    Write-Host "  MinIO Console:" -ForegroundColor Cyan
    Write-Host "    URL:      http://localhost:9001"
    Write-Host "    Username: minioadmin"
    Write-Host "    Password: minioadmin123"
    Write-Host ""
    Write-Host "  RabbitMQ Console:" -ForegroundColor Cyan
    Write-Host "    URL:      http://localhost:15672"
    Write-Host "    Username: orderflow"
    Write-Host "    Password: orderflow123"
    Write-Host ""
    Write-Host "  Prometheus:  http://localhost:9090" -ForegroundColor Cyan
    Write-Host "  Grafana:     http://localhost:3001  (admin / admin123)" -ForegroundColor Cyan
    Write-Host ""
}

# =============================================================================
# MAIN
# =============================================================================

if ($Help) {
    Show-Help
    exit 0
}

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  OrderFlow — Databases & Infrastructure Only" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

if ($Status)  { Get-ContainerStatus;  exit 0 }
if ($Logs)    { Show-Logs;            exit 0 }
if ($Clean)   { Clean-Databases;      exit 0 }
if ($Stop)    { Stop-Databases;       exit 0 }

if ($Restart) {
    Stop-Databases
    Start-Sleep -Seconds 3
    Start-Databases
    Start-Sleep -Seconds 10
    Get-ContainerStatus
    Show-ConnectionInfo
    exit 0
}

# По умолчанию: запустить БД
Start-Databases
Write-Host ""
Write-Info "Ожидание готовности сервисов..."
Start-Sleep -Seconds 15
Get-ContainerStatus
Show-ConnectionInfo

