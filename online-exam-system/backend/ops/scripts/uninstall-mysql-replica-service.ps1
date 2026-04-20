$ErrorActionPreference = "Stop"

$serviceName = "MySQLReplica3307"
$pathHelper = Join-Path $PSScriptRoot "Resolve-OesPaths.ps1"
. $pathHelper
$paths = Get-OesPathConfig -ScriptDir $PSScriptRoot

$mysqld = Join-Path $paths.MysqlHome "bin\mysqld.exe"

& sc.exe stop $serviceName 2>$null | Out-Null
Start-Sleep -Seconds 2
& $mysqld --remove $serviceName 2>$null | Out-Null

Write-Output "uninstalled service $serviceName"
