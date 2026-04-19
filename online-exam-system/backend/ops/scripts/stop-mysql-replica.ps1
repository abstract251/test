$ErrorActionPreference = "Stop"

$repoRoot = "D:\test\online-exam-system"
$runtimeDir = Join-Path $repoRoot "backend\ops\mysql-replica\runtime"
$mysqlAdmin = "D:\MySQL\server\bin\mysqladmin.exe"
$pidFile = Join-Path $runtimeDir "mysql3307.pid"

try {
    & $mysqlAdmin --protocol=TCP --host=127.0.0.1 --port=3307 -uroot shutdown 2>$null | Out-Null
} catch {
}

if (Test-Path $pidFile) {
    $replicaPid = (Get-Content $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1).Trim()
    if ($replicaPid) {
        Stop-Process -Id ([int]$replicaPid) -Force -ErrorAction SilentlyContinue
    }
}

Write-Output "mysql replica on 3307 stopped"
