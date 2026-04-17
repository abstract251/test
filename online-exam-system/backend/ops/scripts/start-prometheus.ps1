$ErrorActionPreference = "Stop"

$repoRoot = "D:\test\online-exam-system"
$prometheusDir = Join-Path $repoRoot "tools\prometheus\prometheus-3.5.0.windows-amd64"
$prometheusExe = Join-Path $prometheusDir "prometheus.exe"
$configFile = Join-Path $repoRoot "backend\ops\prometheus\prometheus.yml"
$stdoutLog = Join-Path $repoRoot "tools\prometheus\prometheus.out.log"
$stderrLog = Join-Path $repoRoot "tools\prometheus\prometheus.err.log"

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
