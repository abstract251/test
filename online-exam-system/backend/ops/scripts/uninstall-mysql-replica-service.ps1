$ErrorActionPreference = "Stop"

$serviceName = "MySQLReplica3307"
$mysqld = "D:\MySQL\server\bin\mysqld.exe"

& sc.exe stop $serviceName 2>$null | Out-Null
Start-Sleep -Seconds 2
& $mysqld --remove $serviceName 2>$null | Out-Null

Write-Output "uninstalled service $serviceName"
