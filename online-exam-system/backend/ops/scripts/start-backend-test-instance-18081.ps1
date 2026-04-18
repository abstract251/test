$ErrorActionPreference = "Stop"

$repoRoot = "D:\test\online-exam-system"
$backendDir = Join-Path $repoRoot "backend"
$targetDir = Join-Path $backendDir "target"
$stdoutLog = Join-Path $targetDir "backend-run-18081.out.log"
$stderrLog = Join-Path $targetDir "backend-run-18081.err.log"
$maven = "C:\Users\GWK\tools\apache-maven-3.9.14\bin\mvn.cmd"
$port = 18081

if (!(Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir | Out-Null
}

$occupiedPids = @()
try {
    $occupiedPids = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction Stop |
        Select-Object -ExpandProperty OwningProcess -Unique
} catch {
    $occupiedPids = @()
}

foreach ($occupiedPid in $occupiedPids) {
    if ($occupiedPid -and $occupiedPid -ne $PID) {
        Stop-Process -Id $occupiedPid -Force -ErrorAction SilentlyContinue
    }
}

$env:SERVER_PORT = "$port"
$env:APP_HOT_START_PORT_AUTO_KILL_ENABLED = "false"
$env:APP_HOT_START_PORT_AUTO_KILL_GUARD_TOKEN = ""

Start-Process `
    -FilePath $maven `
    -ArgumentList "spring-boot:run" `
    -WorkingDirectory $backendDir `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -WindowStyle Hidden

Write-Output "backend test instance starting on http://localhost:18081"
