$ErrorActionPreference = "Stop"

$repoRoot = "D:\test\online-exam-system"
$mysqlBase = "D:\MySQL\server"
$mysqlBin = Join-Path $mysqlBase "bin"
$runtimeDir = Join-Path $repoRoot "backend\ops\mysql-replica\runtime"
$dataDir = Join-Path $runtimeDir "data"
$logDir = Join-Path $runtimeDir "logs"
$dumpFile = Join-Path $runtimeDir "online_exam_replica_dump.sql"
$configFile = Join-Path $runtimeDir "my3307.ini"
$stdoutLog = Join-Path $logDir "mysql3307.out.log"
$stderrLog = Join-Path $logDir "mysql3307.err.log"
$errorLog = Join-Path $logDir "mysql3307.error.log"
$pidFile = Join-Path $runtimeDir "mysql3307.pid"
$relayLog = Join-Path $logDir "mysql3307-relay"
$binlog = Join-Path $logDir "mysql3307-bin"
$mysql = Join-Path $mysqlBin "mysql.exe"
$mysqld = Join-Path $mysqlBin "mysqld.exe"
$mysqldump = Join-Path $mysqlBin "mysqldump.exe"
$mysqlAdmin = Join-Path $mysqlBin "mysqladmin.exe"
$primaryPort = 3306
$replicaPort = 3307
$primaryRootPassword = "root"
$replicationUser = "replica_user"
$replicationPassword = "Replica@123!"
$primaryMysqlArgs = @("--host=localhost", "--port=$primaryPort", "-uroot", "--password=$primaryRootPassword")

function To-MySqlPath([string]$path) {
    return $path.Replace("\", "/")
}

function Ensure-Dir([string]$path) {
    if (!(Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

function Wait-ForMySql([int]$port, [int]$timeoutSec = 60) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            & $mysqlAdmin --protocol=TCP --host=127.0.0.1 --port=$port -uroot ping 2>$null | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "mysql on port $port did not become ready within $timeoutSec seconds"
}

Ensure-Dir $runtimeDir
Ensure-Dir $logDir

& (Join-Path $repoRoot "backend\ops\scripts\stop-mysql-replica.ps1") | Out-Null

$config = @"
[mysqld]
port=$replicaPort
basedir=$(To-MySqlPath $mysqlBase)
datadir=$(To-MySqlPath $dataDir)
server-id=2
character-set-server=utf8mb4
collation-server=utf8mb4_0900_ai_ci
relay-log=$(To-MySqlPath $relayLog)
log-bin=$(To-MySqlPath $binlog)
log-error=$(To-MySqlPath $errorLog)
pid-file=$(To-MySqlPath $pidFile)
read_only=ON
super_read_only=ON
skip_replica_start=ON
mysqlx=0
report_host=127.0.0.1
report_port=$replicaPort

[client]
port=$replicaPort
"@
Set-Content -Path $configFile -Value $config -Encoding ASCII

if (Test-Path $dataDir) {
    Remove-Item -LiteralPath $dataDir -Recurse -Force
}
Ensure-Dir $dataDir

& $mysqld "--defaults-file=$configFile" "--initialize-insecure" "--console"

if (Test-Path $stdoutLog) { Remove-Item $stdoutLog -Force }
if (Test-Path $stderrLog) { Remove-Item $stderrLog -Force }

Start-Process `
    -FilePath $mysqld `
    -ArgumentList "--defaults-file=$configFile" `
    -WindowStyle Hidden

Wait-ForMySql -port $replicaPort -timeoutSec 90

$grantSql = @"
CREATE USER IF NOT EXISTS '$replicationUser'@'127.0.0.1' IDENTIFIED BY '$replicationPassword';
ALTER USER '$replicationUser'@'127.0.0.1' IDENTIFIED BY '$replicationPassword';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO '$replicationUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@
$grantSql | & $mysql @primaryMysqlArgs

& $mysqldump `
    @primaryMysqlArgs `
    --single-transaction `
    --set-gtid-purged=OFF `
    --source-data=2 `
    --databases online_exam `
    --result-file=$dumpFile

$sourceLine = Select-String -Path $dumpFile -Pattern "SOURCE_LOG_FILE='([^']+)', SOURCE_LOG_POS=([0-9]+)" | Select-Object -First 1
if (-not $sourceLine) {
    throw "failed to read binlog coordinates from dump file"
}
$matches = [regex]::Match($sourceLine.Line, "SOURCE_LOG_FILE='([^']+)', SOURCE_LOG_POS=([0-9]+)")
$sourceLogFile = $matches.Groups[1].Value
$sourceLogPos = $matches.Groups[2].Value

$resetSql = @"
STOP REPLICA;
RESET REPLICA ALL;
SET GLOBAL super_read_only = OFF;
SET GLOBAL read_only = OFF;
DROP DATABASE IF EXISTS online_exam;
"@
$resetSql | & $mysql --protocol=TCP --host=127.0.0.1 --port=$replicaPort -uroot

$dumpSourceSql = "SOURCE $(To-MySqlPath $dumpFile)"
& $mysql --protocol=TCP --host=127.0.0.1 --port=$replicaPort -uroot -e $dumpSourceSql
$replicaSql = @"
CHANGE REPLICATION SOURCE TO
  SOURCE_HOST='127.0.0.1',
  SOURCE_PORT=$primaryPort,
  SOURCE_USER='$replicationUser',
  SOURCE_PASSWORD='$replicationPassword',
  SOURCE_LOG_FILE='$sourceLogFile',
  SOURCE_LOG_POS=$sourceLogPos,
  GET_SOURCE_PUBLIC_KEY=1;
START REPLICA;
"@
$replicaSql | & $mysql --protocol=TCP --host=127.0.0.1 --port=$replicaPort -uroot

$readonlySql = @"
SET GLOBAL read_only = ON;
SET GLOBAL super_read_only = ON;
"@
$readonlySql | & $mysql --protocol=TCP --host=127.0.0.1 --port=$replicaPort -uroot

Start-Sleep -Seconds 3

$validationSql = @"
SHOW REPLICA STATUS\G
"@
$validationSql | & $mysql --protocol=TCP --host=127.0.0.1 --port=$replicaPort -uroot

Write-Output "mysql replica ready on 127.0.0.1:$replicaPort"
