param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("normal", "single-18080", "single-18081", "strict-protect", "relaxed")]
    [string]$Mode
)

$ErrorActionPreference = "Stop"

$pathHelper = Join-Path $PSScriptRoot "Resolve-OesPaths.ps1"
. $pathHelper
$paths = Get-OesPathConfig -ScriptDir $PSScriptRoot

$nginxDir = $paths.NginxHome
$nginxExe = Join-Path $nginxDir "nginx.exe"
$configFile = New-OesNginxGeneratedConfig -PathConfig $paths
$runtimeDir = $paths.NginxRuntimeDir

if (!(Test-Path $nginxExe)) {
    throw "nginx.exe not found: $nginxExe"
}

if (!(Test-Path $runtimeDir)) {
    throw "nginx runtime dir not found: $runtimeDir"
}

$upstreamFile = Join-Path $runtimeDir "upstream-mode.conf"
$loginFile = Join-Path $runtimeDir "login-limit.conf"
$refreshFile = Join-Path $runtimeDir "refresh-limit.conf"
$startFile = Join-Path $runtimeDir "start-limit.conf"
$submitFile = Join-Path $runtimeDir "submit-limit.conf"

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

switch ($Mode) {
    "normal" {
        $upstream = @(
            "server 127.0.0.1:18080 max_fails=3 fail_timeout=30s;",
            "server 127.0.0.1:18081 max_fails=3 fail_timeout=30s;"
        )
        $login = @("limit_req zone=oes_auth_login burst=10 nodelay;", "limit_conn oes_per_ip_conn 30;")
        $refresh = @("limit_req zone=oes_auth_refresh burst=10 nodelay;", "limit_conn oes_per_ip_conn 30;")
        $start = @("limit_req zone=oes_exam_start burst=10;", "limit_conn oes_per_ip_conn 20;")
        $submit = @("limit_req zone=oes_exam_submit burst=8;", "limit_conn oes_per_ip_conn 20;")
    }
    "single-18080" {
        $upstream = @("server 127.0.0.1:18080 max_fails=3 fail_timeout=30s;")
        $login = @("limit_req zone=oes_auth_login burst=10 nodelay;", "limit_conn oes_per_ip_conn 30;")
        $refresh = @("limit_req zone=oes_auth_refresh burst=10 nodelay;", "limit_conn oes_per_ip_conn 30;")
        $start = @("limit_req zone=oes_exam_start burst=10;", "limit_conn oes_per_ip_conn 20;")
        $submit = @("limit_req zone=oes_exam_submit burst=8;", "limit_conn oes_per_ip_conn 20;")
    }
    "single-18081" {
        $upstream = @("server 127.0.0.1:18081 max_fails=3 fail_timeout=30s;")
        $login = @("limit_req zone=oes_auth_login burst=10 nodelay;", "limit_conn oes_per_ip_conn 30;")
        $refresh = @("limit_req zone=oes_auth_refresh burst=10 nodelay;", "limit_conn oes_per_ip_conn 30;")
        $start = @("limit_req zone=oes_exam_start burst=10;", "limit_conn oes_per_ip_conn 20;")
        $submit = @("limit_req zone=oes_exam_submit burst=8;", "limit_conn oes_per_ip_conn 20;")
    }
    "strict-protect" {
        $upstream = @(
            "server 127.0.0.1:18080 max_fails=3 fail_timeout=30s;",
            "server 127.0.0.1:18081 max_fails=3 fail_timeout=30s;"
        )
        $login = @("limit_req zone=oes_auth_login burst=4 nodelay;", "limit_conn oes_per_ip_conn 15;")
        $refresh = @("limit_req zone=oes_auth_refresh burst=4 nodelay;", "limit_conn oes_per_ip_conn 15;")
        $start = @("limit_req zone=oes_exam_start burst=4;", "limit_conn oes_per_ip_conn 12;")
        $submit = @("limit_req zone=oes_exam_submit burst=4;", "limit_conn oes_per_ip_conn 12;")
    }
    "relaxed" {
        $upstream = @(
            "server 127.0.0.1:18080 max_fails=3 fail_timeout=30s;",
            "server 127.0.0.1:18081 max_fails=3 fail_timeout=30s;"
        )
        $login = @("limit_req zone=oes_auth_login burst=20 nodelay;", "limit_conn oes_per_ip_conn 60;")
        $refresh = @("limit_req zone=oes_auth_refresh burst=20 nodelay;", "limit_conn oes_per_ip_conn 60;")
        $start = @("limit_req zone=oes_exam_start burst=20;", "limit_conn oes_per_ip_conn 40;")
        $submit = @("limit_req zone=oes_exam_submit burst=16;", "limit_conn oes_per_ip_conn 40;")
    }
}

[System.IO.File]::WriteAllLines($upstreamFile, $upstream, $utf8NoBom)
[System.IO.File]::WriteAllLines($loginFile, $login, $utf8NoBom)
[System.IO.File]::WriteAllLines($refreshFile, $refresh, $utf8NoBom)
[System.IO.File]::WriteAllLines($startFile, $start, $utf8NoBom)
[System.IO.File]::WriteAllLines($submitFile, $submit, $utf8NoBom)

& $nginxExe -p "$nginxDir\" -c $configFile -t
& $nginxExe -p "$nginxDir\" -c $configFile -s reload

Write-Output "nginx runtime mode applied: $Mode"
