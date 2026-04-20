$ErrorActionPreference = "Stop"

$pathHelper = Join-Path $PSScriptRoot "Resolve-OesPaths.ps1"
. $pathHelper
$paths = Get-OesPathConfig -ScriptDir $PSScriptRoot

$prometheusDir = $paths.PrometheusHome
$prometheusExe = Join-Path $prometheusDir "prometheus.exe"
$configFile = $paths.PrometheusConfig
$stdoutLog = Join-Path $paths.TargetDir "prometheus.out.log"
$stderrLog = Join-Path $paths.TargetDir "prometheus.err.log"

if (!(Test-Path -LiteralPath $paths.TargetDir)) {
    New-Item -ItemType Directory -Path $paths.TargetDir -Force | Out-Null
}

if (!(Test-Path $prometheusExe)) {
    throw "prometheus.exe not found: $prometheusExe"
}

Start-Process `
    -FilePath $prometheusExe `
    -ArgumentList "--config.file=$configFile", "--storage.tsdb.path=$prometheusDir\data", "--web.listen-address=:9090" `
    -WorkingDirectory $prometheusDir `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -WindowStyle Hidden

Write-Output "Prometheus starting on http://localhost:9090"
