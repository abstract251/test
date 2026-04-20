$ErrorActionPreference = "Stop"

$pathHelper = Join-Path $PSScriptRoot "Resolve-OesPaths.ps1"
. $pathHelper
$paths = Get-OesPathConfig -ScriptDir $PSScriptRoot

$runtimeDir = $paths.MysqlReplicaRuntimeDir
$mysqlAdmin = Join-Path $paths.MysqlHome "bin\mysqladmin.exe"
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
