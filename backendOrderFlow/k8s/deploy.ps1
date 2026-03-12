# OrderFlow - Kubernetes Deploy Script
# Usage: .\k8s\deploy.ps1

param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Continue"
$rootPath = Split-Path $PSScriptRoot -Parent
if (-not $rootPath) { $rootPath = Get-Location }

$services = @(
    "eureka-server",
    "api-gateway",
    "auth-service",
    "catalog-service",
    "order-service",
    "chat-service",
    "document-service"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " OrderFlow - Kubernetes Deployment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# --- Step 1: Build Docker images ---
if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "[1/6] Building Docker images..." -ForegroundColor Yellow

    foreach ($svc in $services) {
        Write-Host "  Building orderflow/${svc}:latest..." -ForegroundColor Gray
        $ctx = Join-Path $rootPath $svc
        docker build -t "orderflow/${svc}:latest" $ctx
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  FAILED to build ${svc}" -ForegroundColor Red
            exit 1
        }
    }
    Write-Host "  All images built!" -ForegroundColor Green
}
else {
    Write-Host ""
    Write-Host "[1/6] Skipping Docker build (--SkipBuild)" -ForegroundColor Gray
}

# --- Step 2: Namespace ---
Write-Host ""
Write-Host "[2/6] Creating namespace..." -ForegroundColor Yellow
kubectl apply -f (Join-Path $PSScriptRoot "namespace.yaml")

# --- Step 3: Secrets + ConfigMaps ---
Write-Host ""
Write-Host "[3/6] Applying secrets and configmaps..." -ForegroundColor Yellow
kubectl apply -f (Join-Path $PSScriptRoot "secrets.yaml")
kubectl apply -f (Join-Path $PSScriptRoot "configmaps.yaml")

# --- Step 4: Databases ---
Write-Host ""
Write-Host "[4/6] Deploying databases..." -ForegroundColor Yellow
kubectl apply -f (Join-Path $PSScriptRoot "databases.yaml")
Write-Host "  Waiting for databases to be ready..."

$dbLabels = @("auth-mysql", "catalog-mysql", "order-mysql", "chat-mysql", "document-mysql")
foreach ($db in $dbLabels) {
    kubectl wait --namespace=orderflow --for=condition=ready pod -l app=$db --timeout=120s
}

# --- Step 5: Infrastructure ---
Write-Host ""
Write-Host "[5/6] Deploying infrastructure..." -ForegroundColor Yellow
kubectl apply -f (Join-Path $PSScriptRoot "infrastructure.yaml")
Write-Host "  Waiting for infrastructure to be ready..."
kubectl wait --namespace=orderflow --for=condition=ready pod -l app=rabbitmq --timeout=120s
kubectl wait --namespace=orderflow --for=condition=ready pod -l app=minio --timeout=120s

# --- Step 6: Microservices ---
Write-Host ""
Write-Host "[6/6] Deploying microservices..." -ForegroundColor Yellow
kubectl apply -f (Join-Path $PSScriptRoot "services.yaml")
Write-Host "  Waiting for services to be ready..."

foreach ($svc in $services) {
    kubectl wait --namespace=orderflow --for=condition=ready pod -l app=$svc --timeout=180s
}

# --- Done ---
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " Deployment complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Endpoints:" -ForegroundColor Cyan
Write-Host "  API Gateway:   http://localhost:30765" -ForegroundColor White
Write-Host "  Eureka:        kubectl port-forward -n orderflow svc/eureka-server 8761:8761" -ForegroundColor White
Write-Host "  RabbitMQ:      kubectl port-forward -n orderflow svc/rabbitmq 15672:15672" -ForegroundColor White
Write-Host "  MinIO Console: kubectl port-forward -n orderflow svc/minio 9001:9001" -ForegroundColor White
Write-Host "  Prometheus:    kubectl port-forward -n orderflow svc/prometheus 9090:9090" -ForegroundColor White
Write-Host "  Grafana:       kubectl port-forward -n orderflow svc/grafana 3001:3000" -ForegroundColor White
Write-Host ""
Write-Host "Status: kubectl get pods -n orderflow" -ForegroundColor Gray
Write-Host "Logs:   kubectl logs -n orderflow -l app=<service-name>" -ForegroundColor Gray

