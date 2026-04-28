$ErrorActionPreference = "Stop"

$pathHelper = Join-Path $PSScriptRoot "Resolve-OesPaths.ps1"
. $pathHelper
$paths = Get-OesPathConfig -ScriptDir $PSScriptRoot

$nginxDir = $paths.NginxHome
$nginxExe = Join-Path $nginxDir "nginx.exe"
$pidFile = Join-Path $nginxDir "logs\online-exam-nginx.pid"
$taskKillExe = Join-Path $env:WINDIR "System32\taskkill.exe"

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
            Start-Sleep -Milliseconds 500
            $stopped = -not (Get-Process -Id $pidValue -ErrorAction SilentlyContinue)
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
        Start-Sleep -Milliseconds 500
        $stopped = @(Get-Process nginx -ErrorAction SilentlyContinue).Count -eq 0
    }
}

if (-not $stopped -and (Test-Path $taskKillExe)) {
    & $taskKillExe /F /T /IM nginx.exe | Out-Null
    Start-Sleep -Milliseconds 500
    $stopped = @(Get-Process nginx -ErrorAction SilentlyContinue).Count -eq 0
}

if (Test-Path $pidFile) {
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}

if (-not $stopped -and @(Get-Process nginx -ErrorAction SilentlyContinue).Count -gt 0) {
    Write-Warning "nginx processes are still running after stop attempts"
    return
}

if ($stopped) {
    Write-Output "nginx test instance stopped"
} else {
    Write-Output "no live nginx test instance found, stale pid cleaned if present"
}
