param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("up", "down", "restart", "status", "logs", "build", "clean")]
    [string]$Action = "up"
)
$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}
function Write-Header {
    param([string]$Message)
    Write-ColorOutput Green "`n========================================="
    Write-ColorOutput Green $Message
    Write-ColorOutput Green "=========================================`n"
}
function Write-Info {
    param([string]$Message)
    Write-ColorOutput Cyan "INFO: $Message"
}
function Write-Success {
    param([string]$Message)
    Write-ColorOutput Green "SUCCESS: $Message"
}
function Write-Warning {
    param([string]$Message)
    Write-ColorOutput Yellow "WARNING: $Message"
}
function Write-Error {
    param([string]$Message)
    Write-ColorOutput Red "ERROR: $Message"
}
function Test-Docker {
    Write-Info "Проверка Docker..."
    try {
        docker --version | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Docker не работает"
        }
        Write-Success "Docker доступен"
    } catch {
        Write-Error "Docker не найден или не запущен!"
        Write-Error "Установите и запустите Docker Desktop"
        exit 1
    }
}
function Start-Services {
    Write-Header "Запуск Order Exchange"
    Test-Docker
    Write-Info "Создание файла конфигурации Prometheus..."
    Create-PrometheusConfig
    Write-Info "Запуск всех сервисов..."
    Write-Warning "Это может занять несколько минут при первом запуске..."
    docker-compose -f "$ProjectRoot\docker-compose.yaml" up -d --build
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Все сервисы запущены!"
        Start-Sleep -Seconds 10
        Show-ServiceStatus
        Show-AccessInfo
    } else {
        Write-Error "Ошибка при запуске сервисов"
        exit 1
    }
}
function Stop-Services {
    Write-Header "Остановка Order Exchange"
    Write-Info "Остановка всех сервисов..."
    docker-compose -f "$ProjectRoot\docker-compose.yaml" down
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Все сервисы остановлены"
    } else {
        Write-Error "Ошибка при остановке сервисов"
        exit 1
    }
}
function Restart-Services {
    Write-Header "Перезапуск Order Exchange"
    Stop-Services
    Start-Sleep -Seconds 5
    Start-Services
}
function Show-ServiceStatus {
    Write-Header "Статус сервисов"
    docker-compose -f "$ProjectRoot\docker-compose.yaml" ps
}
function Show-ServiceLogs {
    Write-Header "Логи сервисов"
    Write-Info "Показ последних логов (Ctrl+C для выхода)..."
    docker-compose -f "$ProjectRoot\docker-compose.yaml" logs -f --tail=100
}
function Build-Services {
    Write-Header "Сборка Docker образов"
    Test-Docker
    Write-Info "Сборка всех сервисов..."
    docker-compose -f "$ProjectRoot\docker-compose.yaml" build
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Все образы собраны успешно"
    } else {
        Write-Error "Ошибка при сборке образов"
        exit 1
    }
}
function Clean-All {
    Write-Header "Полная очистка"
    Write-Warning "Это удалит все контейнеры, volumes и образы Order Exchange!"
    $confirmation = Read-Host "Продолжить? (yes/no)"
    if ($confirmation -ne "yes") {
        Write-Info "Отменено пользователем"
        return
    }
    Write-Info "Остановка и удаление контейнеров..."
    docker-compose -f "$ProjectRoot\docker-compose.yaml" down -v
    Write-Info "Удаление Docker образов..."
    $images = docker images | Select-String "order-exchange"
    if ($images) {
        docker images | Select-String "order-exchange" | ForEach-Object {
            $imageId = ($_ -split "\s+")[2]
            docker rmi -f $imageId 2>$null
        }
    }
    Write-Success "Очистка завершена"
}
function Show-AccessInfo {
    Write-Header "Информация о доступе"
    Write-Info "Веб-сервисы:"
    Write-Output "  Frontend:        http://localhost"
    Write-Output "  API Gateway:     http://localhost:8080"
    Write-Output "  Eureka Server:   http://localhost:8761"
    Write-Output ""
    Write-Info "Микросервисы:"
    Write-Output "  Auth Service:     http://localhost:8081"
    Write-Output "  Catalog Service:  http://localhost:8082"
    Write-Output "  Order Service:    http://localhost:8083"
    Write-Output "  Chat Service:     http://localhost:8084"
    Write-Output "  Document Service: http://localhost:8085"
    Write-Output ""
    Write-Info "Базы данных (MySQL):"
    Write-Output "  User DB:    localhost:3306 (root/root)"
    Write-Output "  Catalog DB: localhost:3307 (root/root)"
    Write-Output "  Order DB:   localhost:3308 (root/root)"
    Write-Output "  Chat DB:    localhost:3309 (root/root)"
    Write-Output ""
    Write-Info "Мониторинг и инфраструктура:"
    Write-Output "  RabbitMQ:   http://localhost:15672 (admin/admin123)"
    Write-Output "  Prometheus: http://localhost:9090"
    Write-Output "  Grafana:    http://localhost:3000 (admin/admin)"
    Write-Output ""
    Write-Info "Полезные команды:"
    Write-Output "  Просмотр логов:    .\deploy-docker.ps1 -Action logs"
    Write-Output "  Статус сервисов:   .\deploy-docker.ps1 -Action status"
    Write-Output "  Остановка:         .\deploy-docker.ps1 -Action down"
}
function Create-PrometheusConfig {
    $monitoringDir = Join-Path $ProjectRoot "monitoring"
    if (-not (Test-Path $monitoringDir)) {
        New-Item -ItemType Directory -Path $monitoringDir | Out-Null
    }
    $prometheusConfig = @"
global:
  scrape_interval: 15s
  evaluation_interval: 15s
scrape_configs:
  - job_name: 'eureka-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8080']
  - job_name: 'auth-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['auth-service:8081']
  - job_name: 'catalog-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['catalog-service:8082']
  - job_name: 'order-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['order-service:8083']
  - job_name: 'chat-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['chat-service:8084']
  - job_name: 'document-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['document-service:8085']
  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq:15692']
"@
    $configPath = Join-Path $monitoringDir "prometheus.yml"
    $prometheusConfig | Out-File -FilePath $configPath -Encoding UTF8
}
function Main {
    Write-ColorOutput Cyan "========================================================"
    Write-ColorOutput Cyan "       Order Exchange - Docker Compose"
    Write-ColorOutput Cyan "========================================================"
    Write-Output ""
    switch ($Action) {
        "up" {
            Start-Services
        }
        "down" {
            Stop-Services
        }
        "restart" {
            Restart-Services
        }
        "status" {
            Show-ServiceStatus
        }
        "logs" {
            Show-ServiceLogs
        }
        "build" {
            Build-Services
        }
        "clean" {
            Clean-All
        }
    }
}
try {
    Main
} catch {
    Write-Error "Ошибка: $_"
    Write-Error $_.ScriptStackTrace
    exit 1
}
