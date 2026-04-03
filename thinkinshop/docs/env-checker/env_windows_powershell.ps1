# check_versions.ps1 - Full non-blocking version checker with tabular output

Write-Host "🔍 Detecting OS..." -ForegroundColor Cyan
$Platform = "Windows"
Write-Host "✅ Platform: $Platform" -ForegroundColor Green
Write-Host "========================================"

$script:requiredVersions = @{
    MySQL = "8.0.29"
    Redis = "6+"
    PHP   = "8.0.1x"
    Java  = "1.8"
    Node  = "14.20"
    Nacos = "2.1.1"
}

$results = @()

function Add-Result {
    param(
        [string]$Component,
        [string]$Version,
        [string]$Source = "Local"
    )
    $required = if ($script:requiredVersions.ContainsKey($Component)) {
        $script:requiredVersions[$Component]
    } else {
        "-"
    }
    $script:results += [PSCustomObject]@{
        Component = $Component
        Current   = $Version
        Required  = $required
        Source    = $Source
    }
}

# 安全执行命令并提取简要版本信息
function Safe-Exec {
    param(
        [string]$Cmd,
        [string[]]$Args = @()
    )
    try {
        $output = & $Cmd @Args 2>&1 | Out-String
        if ($output -match '(\d+\.\d+[\.\d\w]*)') {
            return $matches[1]
        } else {
            $trimmed = ($output -replace "`r|`n|`t", " ").Trim()
            if ($trimmed.Length -gt 60) { $trimmed = $trimmed.Substring(0, 57) + "..." }
            return if ($trimmed) { $trimmed } else { "No output" }
        }
    } catch {
        return "⚠️ Error"
    }
}

# --- 1. Standard Tools (Local) ---
$tools = @(
    @{ Name = "MySQL"; Cmd = "mysql";        Args = @("--version") },
    @{ Name = "Redis"; Cmd = "redis-server"; Args = @("--version") },
    @{ Name = "PHP";   Cmd = "php";          Args = @("--version") },
    @{ Name = "Java";  Cmd = "java";         Args = @("-version") },
    @{ Name = "Node";  Cmd = "node";         Args = @("--version") }
)

foreach ($tool in $tools) {
    $cmdPath = Get-Command $tool.Cmd -ErrorAction SilentlyContinue
    if ($cmdPath) {
        $ver = Safe-Exec -Cmd $tool.Cmd -Args $tool.Args
        Add-Result -Component $tool.Name -Version $ver
    } else {
        Add-Result -Component $tool.Name -Version "⚠️ Not found"
    }
}

# --- 2. Nacos from Java processes ---
try {
    $javaProcs = Get-WmiObject Win32_Process -Filter "Name='java.exe'" -ErrorAction Stop
    $nacosFound = $false

    foreach ($proc in $javaProcs) {
        if (-not $nacosFound -and $proc.CommandLine -match 'nacos-server-([0-9.]+)\.jar') {
            Add-Result -Component "Nacos" -Version $matches[1]
            $nacosFound = $true
        }
        if ($nacosFound) { break }
    }

    if (-not $nacosFound) { Add-Result -Component "Nacos" -Version "⚠️ Not running" }
} catch {
    Add-Result -Component "Nacos" -Version "⚠️ WMI error"
}

# --- 3. Docker Containers ---
Write-Host "`n🐳 Checking Docker containers..." -ForegroundColor Cyan
if (Get-Command docker -ErrorAction SilentlyContinue) {
    try {
        $containers = docker ps --format "{{.Names}}" 2>$null | Where-Object { $_ }
        $checked = @{}

        foreach ($svc in @("mysql", "redis", "nacos")) {
            $matched = $containers | Where-Object { $_ -like "*$svc*" } | Select-Object -First 1
            if ($matched) {
                Write-Host "→ Detected container: '$matched' for $svc" -ForegroundColor DarkGray
                try {
                    switch ($svc) {
                        "mysql" {
                            $out = docker exec $matched mysql --version 2>$null | Out-String
                            $ver = if ($out -match 'Distrib ([\d\.]+)') { $matches[1] } else { "Unknown" }
                            Add-Result -Component "MySQL" -Version $ver -Source "Docker ($matched)"
                        }
                        "redis" {
                            $out = docker exec $matched redis-server --version 2>$null | Out-String
                            $ver = if ($out -match 'v=([\d\.]+)') { $matches[1] } else { "Unknown" }
                            Add-Result -Component "Redis" -Version $ver -Source "Docker ($matched)"
                        }
                        "nacos" {
                            $out = docker exec $matched ps aux 2>$null | Out-String
                            $ver = if ($out -match 'nacos-server-([0-9.]+)\.jar') { $matches[1] } else { "Unknown" }
                            Add-Result -Component "Nacos" -Version $ver -Source "Docker ($matched)"
                        }
                    }
                } catch {
                    Add-Result -Component ($svc.Substring(0,1).ToUpper() + $svc.Substring(1)) -Version "⚠️ Exec failed" -Source "Docker ($matched)"
                }
            }
        }
    } catch {
        Write-Host "→ Docker runtime error (skipped): $_" -ForegroundColor Red
    }
} else {
    Write-Host "→ Docker not available" -ForegroundColor Gray
}

# === 输出最终表格 ===
Write-Host "`n📊 Final Report (All Checks Completed)" -ForegroundColor Magenta
$results | Sort-Object Component, Source | Format-Table Component, Current, Required, Source -AutoSize

Write-Host "`n✅ All checks finished." -ForegroundColor Green
