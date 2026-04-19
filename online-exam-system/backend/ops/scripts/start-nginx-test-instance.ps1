$ErrorActionPreference = "Stop"

$repoRoot = "D:\test\online-exam-system"
$nginxDir = Join-Path $repoRoot "tools\nginx\nginx-1.28.0"
$nginxExe = Join-Path $nginxDir "nginx.exe"
$configFile = Join-Path $repoRoot "backend\ops\nginx\online-exam-test.conf"
$pidFile = Join-Path $nginxDir "logs\online-exam-nginx.pid"
$legacyPidFile = Join-Path $nginxDir "logs\nginx.pid"
$errorLog = Join-Path $nginxDir "logs\online-exam-error.log"
$frontendDist = Join-Path $repoRoot "frontend\dist"

if (!(Test-Path $nginxExe)) {
    throw "nginx.exe not found: $nginxExe"
}

if (!(Test-Path $configFile)) {
    throw "nginx config not found: $configFile"
}

if (!(Test-Path $frontendDist)) {
    throw "frontend dist not found: $frontendDist. Run 'npm run build' in frontend first."
}

if (Test-Path $pidFile) {
    $existingPidRaw = Get-Content $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1
    $existingPid = if ($existingPidRaw) { $existingPidRaw.Trim() } else { "" }
    if ($existingPid) {
        $existingProcess = Get-Process -Id $existingPid -ErrorAction SilentlyContinue
        if ($existingProcess -and $existingProcess.ProcessName -eq "nginx") {
            Write-Output "nginx already running with pid $existingPid"
            exit 0
        }
    }
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}

if (Test-Path $legacyPidFile) {
    Remove-Item -LiteralPath $legacyPidFile -Force -ErrorAction SilentlyContinue
}

& $nginxExe -p "$nginxDir\" -c $configFile -t

$cmdLine = "start `"`" /min `"$nginxExe`" -p `"$nginxDir\`" -c `"$configFile`""
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $cmdLine -WorkingDirectory $nginxDir

$started = $false
for ($i = 0; $i -lt 10; $i++) {
    Start-Sleep -Seconds 1
    try {
        $response = Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:8080/backend-health/18080" -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            $started = $true
            break
        }
    } catch {
    }
}

if (-not $started) {
    Write-Warning "nginx started but 8080 is not reachable yet. Check $errorLog"
} else {
    Write-Output "nginx test instance is reachable on http://localhost:8080"
}
