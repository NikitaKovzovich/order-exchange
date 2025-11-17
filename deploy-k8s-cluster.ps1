param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("setup", "build", "deploy", "status", "clean", "all")]
    [string]$Action = "all",
    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild = $false
)
$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$K8sPath = Join-Path $ProjectRoot "k8s"
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
function Test-Prerequisites {
    Write-Header "Проверка предварительных требований"
    Write-Info "Проверка Docker..."
    try {
        $dockerVersion = docker --version
        Write-Success "Docker установлен: $dockerVersion"
    } catch {
        Write-Error "Docker не найден! Установите Docker Desktop."
        exit 1
    }
    Write-Info "Проверка kubectl..."
    try {
        $kubectlVersion = kubectl version
        Write-Success "kubectl установлен"
    } catch {
        Write-Error "kubectl не найден! Установите kubectl."
        exit 1
    }
    Write-Info "Проверка подключения к Kubernetes кластеру..."
    try {
        $clusterInfo = kubectl cluster-info 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Kubernetes кластер доступен"
        } else {
            Write-Error "Невозможно подключиться к Kubernetes кластеру!"
            Write-Warning "Убедитесь, что Kubernetes включен в Docker Desktop"
            exit 1
        }
    } catch {
        Write-Error "Ошибка при проверке кластера: $_"
        exit 1
    }
    Write-Success "Все предварительные требования выполнены"
}
function Setup-ClusterNodes {
    Write-Header "Настройка нод кластера"
    Write-Info "Получение списка нод..."
    $nodes = kubectl get nodes -o json | ConvertFrom-Json
    if ($nodes.items.Count -lt 1) {
        Write-Error "Не найдено ни одной ноды в кластере!"
        exit 1
    }
    Write-Info "Найдено нод: $($nodes.items.Count)"
    Write-Info "Настройка меток на нодах для распределения подов..."
    foreach ($node in $nodes.items) {
        $nodeName = $node.metadata.name
        Write-Info "Настройка ноды: $nodeName"
        kubectl label nodes $nodeName node-role=frontend --overwrite | Out-Null
        kubectl label nodes $nodeName node-role=backend --overwrite | Out-Null
        kubectl label nodes $nodeName node-role=database --overwrite | Out-Null
        kubectl label nodes $nodeName node-role=services --overwrite | Out-Null
        Write-Success "Метки установлены на ноде: $nodeName"
    }
    Write-Info "`nАрхитектура кластера (логическое разделение):"
    Write-Info "  - Нода 1 (Frontend): Angular приложение"
    Write-Info "  - Нода 2 (Backend): Микросервисы Spring Boot"
    Write-Info "  - Нода 3 (Database): MySQL базы данных"
    Write-Info "  - Нода 4 (Services): RabbitMQ, Prometheus, Grafana"
    Write-Success "Настройка нод завершена"
}
function Build-DockerImages {
    Write-Header "Сборка Docker образов"
    $services = @(
        @{Name="eureka-server"; Path="backendOrderFlow/eureka-server"},
        @{Name="api-gateway"; Path="backendOrderFlow/api-gateway"},
        @{Name="auth-service"; Path="backendOrderFlow/auth-service"},
        @{Name="catalog-service"; Path="backendOrderFlow/catalog-service"},
        @{Name="order-service"; Path="backendOrderFlow/order-service"},
        @{Name="chat-service"; Path="backendOrderFlow/chat-service"},
        @{Name="document-service"; Path="backendOrderFlow/document-service"},
        @{Name="frontend"; Path="clientOrderFlow"}
    )
    foreach ($service in $services) {
        Write-Info "Сборка образа: $($service.Name)"
        $contextPath = Join-Path $ProjectRoot $service.Path
        $imageName = "order-exchange/$($service.Name):latest"
        try {
            Push-Location $contextPath
            docker build -t $imageName . --quiet
            if ($LASTEXITCODE -eq 0) {
                Write-Success "Образ собран: $imageName"
            } else {
                Write-Error "Ошибка при сборке образа: $imageName"
                Pop-Location
                exit 1
            }
        } catch {
            Write-Error "Исключение при сборке $($service.Name): $_"
            Pop-Location
            exit 1
        } finally {
            Pop-Location
        }
    }
    Write-Success "Все Docker образы успешно собраны"
}
function Deploy-ToKubernetes {
    Write-Header "Развертывание в Kubernetes"
    $manifestFiles = @(
        "00-namespace.yaml",
        "01-configmaps-secrets.yaml",
        "02-database.yaml",
        "03-services-layer.yaml",
        "04-backend-services.yaml",
        "05-frontend.yaml"
    )
    Write-Info "Применение Kubernetes манифестов..."
    foreach ($manifest in $manifestFiles) {
        $manifestPath = Join-Path $K8sPath $manifest
        if (Test-Path $manifestPath) {
            Write-Info "Применение: $manifest"
            kubectl apply -f $manifestPath
            if ($LASTEXITCODE -eq 0) {
                Write-Success "Применен: $manifest"
                Start-Sleep -Seconds 2
            } else {
                Write-Error "Ошибка при применении: $manifest"
                exit 1
            }
        } else {
            Write-Warning "Файл не найден: $manifestPath"
        }
    }
    Write-Success "Все манифесты применены"
    Write-Info "`nОжидание готовности подов (это может занять несколько минут)..."
    Start-Sleep -Seconds 10
    Write-Info "Ожидание запуска MySQL..."
    kubectl wait --for=condition=ready pod -l app=mysql -n order-exchange --timeout=300s 2>$null
    Write-Info "Ожидание запуска сервисов..."
    Start-Sleep -Seconds 15
    Write-Info "Ожидание запуска Eureka Server..."
    kubectl wait --for=condition=ready pod -l app=eureka-server -n order-exchange --timeout=300s 2>$null
    Start-Sleep -Seconds 10
    Write-Success "Развертывание завершено"
}
function Show-Status {
    Write-Header "Статус развертывания"
    Write-Info "Pods в namespace order-exchange:"
    kubectl get pods -n order-exchange -o wide
    Write-Info "`nСервисы:"
    kubectl get services -n order-exchange
    Write-Info "`nDeployments:"
    kubectl get deployments -n order-exchange
    Write-Info "`nStatefulSets:"
    kubectl get statefulsets -n order-exchange 2>$null
    Write-Info "`nПроверка health подов:"
    $pods = kubectl get pods -n order-exchange -o json | ConvertFrom-Json
    $readyCount = 0
    $totalCount = $pods.items.Count
    foreach ($pod in $pods.items) {
        $podName = $pod.metadata.name
        $status = $pod.status.phase
        $ready = $false
        if ($pod.status.containerStatuses) {
            $ready = $pod.status.containerStatuses[0].ready
        }
        if ($ready -and $status -eq "Running") {
            $readyCount++
        }
    }
    Write-Info "`nГотовность: $readyCount/$totalCount подов"
    Write-Header "Доступ к приложению"
    Write-Info "Frontend:    http://localhost:30080"
    Write-Info "API Gateway: http://localhost:30800"
    Write-Info "Eureka:      http://localhost:30761"
    Write-Info "Prometheus:  http://localhost:30090"
    Write-Info "Grafana:     http://localhost:30300 (admin/admin)"
    Write-Info ""
    Write-Info "Для доступа к RabbitMQ Management выполните:"
    Write-Info "  kubectl port-forward -n order-exchange svc/rabbitmq-service 15672:15672"
}
function Clean-Resources {
    Write-Header "Очистка ресурсов"
    Write-Warning "Вы собираетесь удалить все ресурсы Order Exchange из Kubernetes"
    $confirmation = Read-Host "Продолжить? (yes/no)"
    if ($confirmation -ne "yes") {
        Write-Info "Отменено пользователем"
        return
    }
    Write-Info "Удаление namespace order-exchange..."
    kubectl delete namespace order-exchange --timeout=60s
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Namespace order-exchange удален"
    } else {
        Write-Warning "Ошибка при удалении namespace"
    }
    Write-Info "`nОчистка Docker образов? (yes/no)"
    $cleanImages = Read-Host
    if ($cleanImages -eq "yes") {
        Write-Info "Удаление Docker образов..."
        $images = docker images "order-exchange/*" -q
        if ($images) {
            docker rmi -f $images
            Write-Success "Docker образы удалены"
        } else {
            Write-Info "Образы order-exchange не найдены"
        }
    }
    Write-Success "Очистка завершена"
}
function Main {
    Write-ColorOutput Cyan "========================================================"
    Write-ColorOutput Cyan "    Order Exchange - Kubernetes Deployment (4 Nodes)"
    Write-ColorOutput Cyan "========================================================"
    Write-Output ""
    switch ($Action) {
        "setup" {
            Test-Prerequisites
            Setup-ClusterNodes
        }
        "build" {
            Test-Prerequisites
            Build-DockerImages
        }
        "deploy" {
            Test-Prerequisites
            if (-not $SkipBuild) {
                Build-DockerImages
            }
            Deploy-ToKubernetes
            Start-Sleep -Seconds 5
            Show-Status
        }
        "status" {
            Show-Status
        }
        "clean" {
            Clean-Resources
        }
        "all" {
            Test-Prerequisites
            Setup-ClusterNodes
            if (-not $SkipBuild) {
                Build-DockerImages
            }
            Deploy-ToKubernetes
            Start-Sleep -Seconds 5
            Show-Status
        }
    }
    Write-Header "Готово!"
}
try {
    Main
} catch {
    Write-Error "Критическая ошибка: $_"
    Write-Error $_.ScriptStackTrace
    exit 1
}
