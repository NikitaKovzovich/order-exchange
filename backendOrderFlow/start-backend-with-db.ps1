param(
    [switch]$Stop,
    [switch]$Restart,
    [switch]$Status,
    [switch]$Logs,
    [switch]$Clean,
    [switch]$NoBuild,
    [switch]$LocalBackend,
    [switch]$Help
)

$ErrorActionPreference = "SilentlyContinue"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# ---------------------------------------------------------------------------
# Группы сервисов
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

$BackendServices = @(
    "eureka-server",
    "api-gateway",
    "auth-service",
    "catalog-service",
    "order-service",
    "chat-service",
    "document-service"
)

$AllDbInfra = $DbServices + $InfraServices + $MonitoringServices
$AllServices = $AllDbInfra + $BackendServices

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
# Описание контейнеров
# ---------------------------------------------------------------------------
$Containers = @(
    @{ Name = "auth-mysql";       Service = "auth-mysql";       Port = "3306"       },
    @{ Name = "catalog-mysql";    Service = "catalog-mysql";    Port = "3307"       },
    @{ Name = "order-mysql";      Service = "order-mysql";      Port = "3308"       },
    @{ Name = "chat-mysql";       Service = "chat-mysql";       Port = "3309"       },
    @{ Name = "document-mysql";   Service = "document-mysql";   Port = "3310"       },
    @{ Name = "minio";            Service = "minio";            Port = "9000/9001"  },
    @{ Name = "rabbitmq";         Service = "rabbitmq";         Port = "5672/15672" },
    @{ Name = "prometheus";       Service = "prometheus";       Port = "9090"       },
    @{ Name = "grafana";          Service = "grafana";          Port = "3001"       },
    @{ Name = "eureka-server";    Service = "eureka-server";    Port = "8761"       },
    @{ Name = "api-gateway";      Service = "api-gateway";      Port = "8765"       },
    @{ Name = "auth-service";     Service = "auth-service";     Port = "8081"       },
    @{ Name = "catalog-service";  Service = "catalog-service";  Port = "8082"       },
    @{ Name = "order-service";    Service = "order-service";    Port = "8083"       },
    @{ Name = "chat-service";     Service = "chat-service";     Port = "8084"       },
    @{ Name = "document-service"; Service = "document-service"; Port = "8085"       }
)

# ---------------------------------------------------------------------------
# Help
# ---------------------------------------------------------------------------
function Show-Help {
    Write-Host ""
    Write-Host "OrderFlow — Запуск бэкенда + БД (полный стек)" -ForegroundColor Cyan
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Использование: .\start-backend-with-db.ps1 [опция]" -ForegroundColor White
    Write-Host ""
    Write-Host "Опции:" -ForegroundColor Yellow
    Write-Host "  (без опций)       Запустить всё: БД -> собрать бэкенд -> запустить в Docker"
    Write-Host "  -NoBuild          Запустить без пересборки Docker-образов"
    Write-Host "  -LocalBackend     Запустить только БД в Docker, бэкенд запускать из IDE"
    Write-Host "  -Stop             Остановить всё"
    Write-Host "  -Restart          Перезапустить всё"
    Write-Host "  -Status           Показать статус всех контейнеров"
    Write-Host "  -Logs             Показать логи"
    Write-Host "  -Clean            Удалить все контейнеры и тома"
    Write-Host "  -Help             Показать эту справку"
    Write-Host ""
    Write-Host "Режимы работы:" -ForegroundColor Yellow
    Write-Host "  1. Полный стек в Docker (по умолчанию):"
    Write-Host "     .\start-backend-with-db.ps1"
    Write-Host ""
    Write-Host "  2. Только БД в Docker, бэкенд из IDE:"
    Write-Host "     .\start-backend-with-db.ps1 -LocalBackend"
    Write-Host ""
    Write-Host "  3. Запуск без пересборки (быстро):"
    Write-Host "     .\start-backend-with-db.ps1 -NoBuild"
    Write-Host ""
    Write-Host "Порты сервисов:" -ForegroundColor Yellow
    Write-Host "  eureka-server    -> 8761"
    Write-Host "  api-gateway      -> 8765"
    Write-Host "  auth-service     -> 8081"
    Write-Host "  catalog-service  -> 8082"
    Write-Host "  order-service    -> 8083"
    Write-Host "  chat-service     -> 8084"
    Write-Host "  document-service -> 8085"
    Write-Host ""
}

# ---------------------------------------------------------------------------
# Статус
# ---------------------------------------------------------------------------
function Get-ContainerStatus {
    Write-Host ""
    Write-Host "OrderFlow — Статус всех сервисов" -ForegroundColor Cyan
    Write-Host "=================================" -ForegroundColor Cyan
    Write-Host ""

    Write-Host "  БАЗЫ ДАННЫХ:" -ForegroundColor Yellow
    foreach ($c in $Containers | Where-Object { $_.Service -match "mysql" }) {
        $status = docker ps --filter "name=$($c.Name)" --format "{{.Status}}" 2>$null
        Write-Host "    $($c.Name.PadRight(20))" -NoNewline -ForegroundColor White
        if ($status) {
            Write-Host "Running" -ForegroundColor Green -NoNewline
            Write-Host " (Port: $($c.Port))" -ForegroundColor Gray
        } else {
            Write-Host "Stopped" -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host "  ИНФРАСТРУКТУРА:" -ForegroundColor Yellow
    foreach ($c in $Containers | Where-Object { $_.Service -match "minio|rabbitmq|prometheus|grafana" }) {
        $status = docker ps --filter "name=$($c.Name)" --format "{{.Status}}" 2>$null
        Write-Host "    $($c.Name.PadRight(20))" -NoNewline -ForegroundColor White
        if ($status) {
            Write-Host "Running" -ForegroundColor Green -NoNewline
            Write-Host " (Port: $($c.Port))" -ForegroundColor Gray
        } else {
            Write-Host "Stopped" -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host "  МИКРОСЕРВИСЫ:" -ForegroundColor Yellow
    foreach ($c in $Containers | Where-Object { $_.Service -match "eureka|gateway|auth-service|catalog-service|order-service|chat-service|document-service" }) {
        $status = docker ps --filter "name=$($c.Name)" --format "{{.Status}}" 2>$null
        Write-Host "    $($c.Name.PadRight(20))" -NoNewline -ForegroundColor White
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
# Ожидание готовности БД
# ---------------------------------------------------------------------------
function Wait-ForDatabases {
    Write-Info "Ожидание готовности баз данных..."
    $maxAttempts = 30
    $attempt = 0

    while ($attempt -lt $maxAttempts) {
        $attempt++
        $allHealthy = $true

        foreach ($db in $DbServices) {
            $health = docker inspect --format='{{.State.Health.Status}}' $db 2>$null
            if ($health -ne "healthy") {
                $allHealthy = $false
                break
            }
        }

        if ($allHealthy) {
            Write-Success "Все базы данных готовы!"
            return $true
        }

        Write-Host "." -NoNewline -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }

    Write-Host ""
    Write-Warn "Некоторые БД могут быть ещё не готовы, но продолжаем..."
    return $false
}

# ---------------------------------------------------------------------------
# Ожидание готовности RabbitMQ
# ---------------------------------------------------------------------------
function Wait-ForRabbitMQ {
    Write-Info "Ожидание готовности RabbitMQ..."
    $maxAttempts = 20
    $attempt = 0

    while ($attempt -lt $maxAttempts) {
        $attempt++
        $health = docker inspect --format='{{.State.Health.Status}}' rabbitmq 2>$null
        if ($health -eq "healthy") {
            Write-Success "RabbitMQ готов!"
            return $true
        }
        Write-Host "." -NoNewline -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }

    Write-Host ""
    Write-Warn "RabbitMQ может быть ещё не готов..."
    return $false
}

# ---------------------------------------------------------------------------
# Запуск полного стека
# ---------------------------------------------------------------------------
function Start-FullStack {
    param([bool]$Build = $true)

    Write-Info "========== ЭТАП 1: Запуск баз данных и инфраструктуры =========="

    Push-Location $ProjectRoot
    try {
        $dbList = ($AllDbInfra -join " ")
        $cmd = "docker-compose up -d $dbList"
        Write-Info "Выполняю: $cmd"
        Invoke-Expression $cmd

        if ($LASTEXITCODE -ne 0) {
            Write-Err "Не удалось запустить инфраструктуру!"
            return
        }
    } finally {
        Pop-Location
    }

    # Ждём готовности БД и RabbitMQ
    Wait-ForDatabases
    Wait-ForRabbitMQ

    Write-Info "========== ЭТАП 2: Запуск микросервисов =========="

    Push-Location $ProjectRoot
    try {
        $backendList = ($BackendServices -join " ")

        if ($Build) {
            $cmd = "docker-compose up -d --build $backendList"
        } else {
            $cmd = "docker-compose up -d $backendList"
        }

        Write-Info "Выполняю: $cmd"
        Invoke-Expression $cmd

        if ($LASTEXITCODE -eq 0) {
            Write-Success "Все микросервисы запущены!"
        } else {
            Write-Err "Не удалось запустить некоторые микросервисы"
        }
    } finally {
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Запуск только БД (для локальной разработки из IDE)
# ---------------------------------------------------------------------------
function Start-DatabasesOnly {
    Write-Info "Запуск только БД и инфраструктуры (бэкенд запускайте из IDE)..."

    Push-Location $ProjectRoot
    try {
        $dbList = ($AllDbInfra -join " ")
        $cmd = "docker-compose up -d $dbList"
        Write-Info "Выполняю: $cmd"
        Invoke-Expression $cmd

        if ($LASTEXITCODE -eq 0) {
            Write-Success "Инфраструктура запущена!"
        } else {
            Write-Err "Не удалось запустить некоторые сервисы"
        }
    } finally {
        Pop-Location
    }

    Wait-ForDatabases
    Wait-ForRabbitMQ
}

# ---------------------------------------------------------------------------
# Остановка
# ---------------------------------------------------------------------------
function Stop-FullStack {
    Write-Info "Остановка всех сервисов..."

    Push-Location $ProjectRoot
    try {
        docker-compose down
        Write-Success "Все сервисы остановлены"
    } finally {
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Очистка
# ---------------------------------------------------------------------------
function Clean-FullStack {
    Write-Warn "Остановка и удаление всех контейнеров + томов..."

    Push-Location $ProjectRoot
    try {
        docker-compose down -v --remove-orphans
        Write-Success "Все контейнеры и тома удалены"
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
        docker-compose logs --tail 100 -f
    } finally {
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Информация
# ---------------------------------------------------------------------------
function Show-ConnectionInfo {
    Write-Host ""
    Write-Host "Информация для подключения:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  MySQL:" -ForegroundColor Cyan
    Write-Host "    Username: app_user"
    Write-Host "    Password: app_password"
    Write-Host "    user_db:     localhost:3306"
    Write-Host "    catalog_db:  localhost:3307"
    Write-Host "    order_db:    localhost:3308"
    Write-Host "    chat_db:     localhost:3309"
    Write-Host "    document_db: localhost:3310"
    Write-Host ""
    Write-Host "  MinIO:     http://localhost:9001  (minioadmin / minioadmin123)" -ForegroundColor Cyan
    Write-Host "  RabbitMQ:  http://localhost:15672  (orderflow / orderflow123)" -ForegroundColor Cyan
    Write-Host "  Prometheus: http://localhost:9090" -ForegroundColor Cyan
    Write-Host "  Grafana:   http://localhost:3001  (admin / admin123)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Eureka:    http://localhost:8761" -ForegroundColor Cyan
    Write-Host "  Gateway:   http://localhost:8765" -ForegroundColor Cyan
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
Write-Host "  OrderFlow — Backend + Databases Full Stack" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Проверяем Docker
$dockerVersion = docker --version 2>$null
if (-not $dockerVersion) {
    Write-Err "Docker не найден или не запущен! Установите и запустите Docker Desktop."
    exit 1
}
Write-Info "Docker: $dockerVersion"

if ($Status)  { Get-ContainerStatus;  exit 0 }
if ($Logs)    { Show-Logs;            exit 0 }
if ($Clean)   { Clean-FullStack;      exit 0 }
if ($Stop)    { Stop-FullStack;       exit 0 }

if ($Restart) {
    Stop-FullStack
    Start-Sleep -Seconds 3
    if ($LocalBackend) {
        Start-DatabasesOnly
    } else {
        Start-FullStack -Build (-not $NoBuild)
    }
    Start-Sleep -Seconds 5
    Get-ContainerStatus
    Show-ConnectionInfo
    exit 0
}

# По умолчанию
if ($LocalBackend) {
    Write-Info "Режим: только БД в Docker, бэкенд из IDE"
    Start-DatabasesOnly
} else {
    Write-Info "Режим: полный стек в Docker"
    Start-FullStack -Build (-not $NoBuild)
}

Write-Host ""
Write-Info "Ожидание стабилизации сервисов..."
Start-Sleep -Seconds 10
Get-ContainerStatus
Show-ConnectionInfo

if ($LocalBackend) {
    Write-Host ""
    Write-Host "  >>> Теперь запустите микросервисы из IDE (IntelliJ IDEA) <<<" -ForegroundColor Magenta
    Write-Host "  >>> Порядок запуска: eureka-server -> api-gateway -> остальные сервисы <<<" -ForegroundColor Magenta
    Write-Host ""
}

