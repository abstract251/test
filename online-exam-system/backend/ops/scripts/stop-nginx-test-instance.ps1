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
    $pidValue = (Get-Content $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1).Trim()
    if ($pidValue) {
        $process = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
        if ($process -and $process.ProcessName -eq "nginx") {
            Stop-Process -Id $pidValue -Force -ErrorAction SilentlyContinue
            $stopped = $true
        }
    }
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}

if ($stopped) {
    Write-Output "nginx test instance stopped"
} else {
    Write-Output "no live nginx test instance found, stale pid cleaned if present"
}
