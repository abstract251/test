$ErrorActionPreference = "Stop"

$repoRoot = "D:\test\online-exam-system"
$backendDir = Join-Path $repoRoot "backend"
$targetDir = Join-Path $backendDir "target"
$stdoutLog = Join-Path $targetDir "backend-run-18080.out.log"
$stderrLog = Join-Path $targetDir "backend-run-18080.err.log"
$maven = "C:\Users\GWK\tools\apache-maven-3.9.14\bin\mvn.cmd"
$port = 18080
$jvmXms = if ($env:APP_JVM_XMS) { $env:APP_JVM_XMS } else { "768m" }
$jvmXmx = if ($env:APP_JVM_XMX) { $env:APP_JVM_XMX } else { "768m" }
$jvmExtra = if ($env:APP_JVM_EXTRA) { $env:APP_JVM_EXTRA } else { "" }

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
$env:APP_HOT_START_PORT_AUTO_KILL_ENABLED = "true"
$env:APP_HOT_START_PORT_AUTO_KILL_GUARD_TOKEN = "LOCAL_SINGLE_INSTANCE_ONLY"
$jvmArgs = @(
    "-Xms$jvmXms",
    "-Xmx$jvmXmx",
    "-XX:+UseG1GC",
    "-XX:MaxGCPauseMillis=200",
    "-XX:+HeapDumpOnOutOfMemoryError",
    "-XX:HeapDumpPath=$targetDir",
    "-Dspring.devtools.restart.enabled=false",
    "-Dspring.devtools.livereload.enabled=false"
)
if ($jvmExtra) {
    $jvmArgs += $jvmExtra
}
$jvmArgsText = ($jvmArgs -join " ")
$argumentList = "spring-boot:run ""-Dspring-boot.run.jvmArguments=$jvmArgsText"""

Start-Process `
    -FilePath $maven `
    -ArgumentList $argumentList `
    -WorkingDirectory $backendDir `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -WindowStyle Hidden

Write-Output "backend test instance starting on http://localhost:18080"
