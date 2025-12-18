# Build and Test Script for Order Exchange Backend
# PowerShell script to clean, build and test all microservices

param(
    [switch]$SkipTests,
    [switch]$CleanOnly,
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$startTime = Get-Date

$services = @(
    "auth-service",
    "catalog-service",
    "order-service",
    "chat-service",
    "document-service"
)

$rootPath = $PSScriptRoot
if (-not $rootPath) {
    $rootPath = Get-Location
}

$results = @{}
$totalTests = 0
$totalFailed = 0

function Write-Header {
    param([string]$text)
    Write-Host ""
    Write-Host "=" * 70 -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host "=" * 70 -ForegroundColor Cyan
}

function Write-ServiceHeader {
    param([string]$service)
    Write-Host ""
    Write-Host "-" * 50 -ForegroundColor Yellow
    Write-Host "  Processing: $service" -ForegroundColor Yellow
    Write-Host "-" * 50 -ForegroundColor Yellow
}

function Get-TestResults {
    param([string]$servicePath)

    $jacocoPath = Join-Path $servicePath "build\reports\jacoco\test\html\index.html"
    $coverage = "N/A"

    if (Test-Path $jacocoPath) {
        $content = Get-Content $jacocoPath -Raw
        if ($content -match 'Total.*?ctr2[^>]*>(\d+)') {
            $coverage = "$($matches[1])%"
        }
    }

    return $coverage
}

function Run-ServiceBuild {
    param([string]$service)

    $servicePath = Join-Path $rootPath $service

    if (-not (Test-Path $servicePath)) {
        Write-Host "  [SKIP] Service directory not found: $servicePath" -ForegroundColor Gray
        return @{ Status = "SKIPPED"; Tests = 0; Failed = 0; Coverage = "N/A" }
    }

    Push-Location $servicePath

    try {
        # Clean
        Write-Host "  [1/3] Cleaning..." -ForegroundColor Gray
        $cleanOutput = & .\gradlew.bat clean --quiet 2>&1

        if ($CleanOnly) {
            Write-Host "  [OK] Clean completed" -ForegroundColor Green
            return @{ Status = "CLEANED"; Tests = 0; Failed = 0; Coverage = "N/A" }
        }

        # Build
        Write-Host "  [2/3] Building..." -ForegroundColor Gray
        $buildOutput = & .\gradlew.bat build -x test -x spotbugsMain -x spotbugsTest -x spotlessCheck --quiet 2>&1

        if ($LASTEXITCODE -ne 0) {
            Write-Host "  [FAIL] Build failed!" -ForegroundColor Red
            if ($Verbose) { Write-Host $buildOutput -ForegroundColor DarkGray }
            return @{ Status = "BUILD_FAILED"; Tests = 0; Failed = 0; Coverage = "N/A" }
        }

        Write-Host "  [OK] Build successful" -ForegroundColor Green

        if ($SkipTests) {
            return @{ Status = "BUILD_OK"; Tests = 0; Failed = 0; Coverage = "N/A" }
        }

        # Test
        Write-Host "  [3/3] Running tests..." -ForegroundColor Gray
        $testOutput = & .\gradlew.bat test jacocoTestReport 2>&1

        $testsCompleted = 0
        $testsFailed = 0
        $buildFailed = $false

        foreach ($line in $testOutput) {
            if ($line -match '(\d+) tests completed, (\d+) failed') {
                $testsCompleted = [int]$matches[1]
                $testsFailed = [int]$matches[2]
            }
            elseif ($line -match 'BUILD FAILED') {
                $buildFailed = $true
            }
        }

        # Parse XML test results if no count from console
        if ($testsCompleted -eq 0) {
            $testReportPath = Join-Path $servicePath "build\test-results\test"
            if (Test-Path $testReportPath) {
                $xmlFiles = Get-ChildItem -Path $testReportPath -Filter "*.xml" -ErrorAction SilentlyContinue
                foreach ($xml in $xmlFiles) {
                    try {
                        $content = Get-Content $xml.FullName -Raw -ErrorAction SilentlyContinue
                        if ($content -match 'tests="(\d+)".*?failures="(\d+)"') {
                            $testsCompleted += [int]$matches[1]
                            $testsFailed += [int]$matches[2]
                        }
                    } catch { }
                }
            }
        }

        $coverage = Get-TestResults -servicePath $servicePath

        if ($testsFailed -gt 0) {
            Write-Host "  [WARN] Tests: $testsCompleted completed, $testsFailed failed | Coverage: $coverage" -ForegroundColor Yellow
            return @{ Status = "TESTS_FAILED"; Tests = $testsCompleted; Failed = $testsFailed; Coverage = $coverage }
        }
        else {
            Write-Host "  [OK] Tests: $testsCompleted passed | Coverage: $coverage" -ForegroundColor Green
            return @{ Status = "SUCCESS"; Tests = $testsCompleted; Failed = 0; Coverage = $coverage }
        }
    }
    finally {
        Pop-Location
    }
}

# Main execution
Write-Header "Order Exchange Backend - Build & Test"
Write-Host "  Started at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host "  Root path: $rootPath" -ForegroundColor Gray

if ($CleanOnly) {
    Write-Host "  Mode: Clean Only" -ForegroundColor Magenta
}
elseif ($SkipTests) {
    Write-Host "  Mode: Build Only (skip tests)" -ForegroundColor Magenta
}
else {
    Write-Host "  Mode: Full Build & Test" -ForegroundColor Magenta
}

foreach ($service in $services) {
    Write-ServiceHeader $service
    $result = Run-ServiceBuild -service $service
    $results[$service] = $result
    $totalTests += $result.Tests
    $totalFailed += $result.Failed
}

# Summary
Write-Header "Summary"

$successCount = 0
$failCount = 0

Write-Host ""
Write-Host "  Service Results:" -ForegroundColor White
Write-Host "  " + ("-" * 66) -ForegroundColor DarkGray

foreach ($service in $services) {
    $r = $results[$service]
    $statusColor = switch ($r.Status) {
        "SUCCESS" { "Green"; $successCount++ }
        "BUILD_OK" { "Green"; $successCount++ }
        "CLEANED" { "Green"; $successCount++ }
        "TESTS_FAILED" { "Yellow"; $failCount++ }
        "BUILD_FAILED" { "Red"; $failCount++ }
        "SKIPPED" { "Gray" }
        default { "White" }
    }

    $statusText = $r.Status.PadRight(14)
    $testsText = if ($r.Tests -gt 0) { "Tests: $($r.Tests.ToString().PadLeft(3))" } else { "Tests:   -" }
    $failedText = if ($r.Failed -gt 0) { "Failed: $($r.Failed)" } else { "Failed: 0" }
    $coverageText = "Cov: $($r.Coverage)"

    Write-Host "  $($service.PadRight(20)) | $statusText | $testsText | $failedText | $coverageText" -ForegroundColor $statusColor
}

Write-Host "  " + ("-" * 66) -ForegroundColor DarkGray

$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host ""
Write-Host "  Total services: $($services.Count)" -ForegroundColor White
Write-Host "  Successful: $successCount" -ForegroundColor Green
if ($failCount -gt 0) {
    Write-Host "  Failed: $failCount" -ForegroundColor Red
}
Write-Host "  Total tests run: $totalTests" -ForegroundColor White
if ($totalFailed -gt 0) {
    Write-Host "  Total tests failed: $totalFailed" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "  Duration: $($duration.ToString('mm\:ss'))" -ForegroundColor Gray
Write-Host "  Finished at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host ""

if ($failCount -gt 0 -or $totalFailed -gt 0) {
    Write-Host "  BUILD COMPLETED WITH ISSUES" -ForegroundColor Yellow
    exit 1
}
else {
    Write-Host "  BUILD SUCCESSFUL" -ForegroundColor Green
    exit 0
}

