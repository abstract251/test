$ErrorActionPreference = "Stop"

$serviceName = "MySQLReplica3307"
$pathHelper = Join-Path $PSScriptRoot "Resolve-OesPaths.ps1"
. $pathHelper
$paths = Get-OesPathConfig -ScriptDir $PSScriptRoot

$configFile = Join-Path $paths.MysqlReplicaRuntimeDir "my3307.ini"
$mysqld = Join-Path $paths.MysqlHome "bin\mysqld.exe"

if (!(Test-Path $configFile)) {
    throw "missing replica config: $configFile"
}

& sc.exe stop $serviceName 2>$null | Out-Null
Start-Sleep -Seconds 2
& $mysqld --remove $serviceName 2>$null | Out-Null
Start-Sleep -Seconds 1
& $mysqld --install $serviceName "--defaults-file=$configFile"
& sc.exe start $serviceName

Write-Output "installed and started service $serviceName"
