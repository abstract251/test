$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$delegate = Join-Path $scriptDir "start-backend-test-instance-18080.ps1"

if (!(Test-Path $delegate)) {
    throw "missing script: $delegate"
}

& $delegate
