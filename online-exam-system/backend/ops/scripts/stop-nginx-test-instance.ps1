$ErrorActionPreference = "Stop"

$repoRoot = "D:\test\online-exam-system"
$nginxDir = Join-Path $repoRoot "tools\nginx\nginx-1.28.0"
$nginxExe = Join-Path $nginxDir "nginx.exe"
$pidFile = Join-Path $nginxDir "logs\online-exam-nginx.pid"

if (!(Test-Path $nginxExe)) {
    throw "nginx.exe not found: $nginxExe"
}

$stopped = $false
if (Test-Path $pidFile) {
    $pidRaw = Get-Content $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1
    $pidValue = if ($pidRaw) { $pidRaw.Trim() } else { "" }
    if ($pidValue) {
        $process = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
        if ($process -and $process.ProcessName -eq "nginx") {
            Stop-Process -Id $pidValue -Force -ErrorAction SilentlyContinue
            $stopped = $true
        }
    }
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}

if (-not $stopped) {
    $nginxProcesses = @(Get-Process nginx -ErrorAction SilentlyContinue)
    if ($nginxProcesses.Count -gt 0) {
        foreach ($process in $nginxProcesses) {
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
        $stopped = $true
    }
}

if ($stopped) {
    Write-Output "nginx test instance stopped"
} else {
    Write-Output "no live nginx test instance found, stale pid cleaned if present"
}
