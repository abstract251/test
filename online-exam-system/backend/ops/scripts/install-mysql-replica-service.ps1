$ErrorActionPreference = "Stop"

$serviceName = "MySQLReplica3307"
$repoRoot = "D:\test\online-exam-system"
$configFile = Join-Path $repoRoot "backend\ops\mysql-replica\runtime\my3307.ini"
$mysqld = "D:\MySQL\server\bin\mysqld.exe"

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
