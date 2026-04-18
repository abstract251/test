$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$instanceA = Join-Path $scriptDir "start-backend-test-instance-18080.ps1"
$instanceB = Join-Path $scriptDir "start-backend-test-instance-18081.ps1"

if (!(Test-Path $instanceA)) {
    throw "missing script: $instanceA"
}
if (!(Test-Path $instanceB)) {
    throw "missing script: $instanceB"
}

& $instanceA
Start-Sleep -Seconds 2
& $instanceB

Write-Output "backend dual instances starting on http://localhost:18080 and http://localhost:18081"
