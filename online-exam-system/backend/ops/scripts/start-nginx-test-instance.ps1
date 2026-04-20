$ErrorActionPreference = "Stop"

$pathHelper = Join-Path $PSScriptRoot "Resolve-OesPaths.ps1"
. $pathHelper
$paths = Get-OesPathConfig -ScriptDir $PSScriptRoot

$nginxDir = $paths.NginxHome
$nginxExe = Join-Path $nginxDir "nginx.exe"
$configFile = New-OesNginxGeneratedConfig -PathConfig $paths
$pidFile = Join-Path $nginxDir "logs\online-exam-nginx.pid"
$legacyPidFile = Join-Path $nginxDir "logs\nginx.pid"
$errorLog = Join-Path $nginxDir "logs\online-exam-error.log"
$frontendDist = $paths.FrontendDist
$cmdExe = Join-Path $env:WINDIR "System32\cmd.exe"

if (!(Test-Path $nginxExe)) {
    throw "nginx.exe not found: $nginxExe"
}

if (!(Test-Path $cmdExe)) {
    throw "cmd.exe not found: $cmdExe"
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

$cmdArgs = "/c start `"`" /min `"$nginxExe`" -p `"$nginxDir\`" -c `"$configFile`""

Start-Process `
    -FilePath $cmdExe `
    -ArgumentList $cmdArgs `
    -WorkingDirectory $nginxDir `
    -WindowStyle Hidden

$started = $false
for ($i = 0; $i -lt 20; $i++) {
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
    Write-Warning "nginx process has been launched, but 8080 is not reachable yet. Check $errorLog or try the documented foreground startup command."
} else {
    Write-Output "nginx test instance is reachable on http://localhost:8080"
}
