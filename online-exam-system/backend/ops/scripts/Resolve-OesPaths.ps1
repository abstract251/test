$ErrorActionPreference = "Stop"

function Get-OesRepoRoot {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ScriptDir
    )

    if ($env:OES_REPO_ROOT) {
        return (Resolve-Path -LiteralPath $env:OES_REPO_ROOT).Path
    }

    return (Resolve-Path -LiteralPath (Join-Path $ScriptDir "..\..\..")).Path
}

function Import-OesLocalToolOverrides {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RepoRoot
    )

    $localConfig = Join-Path $RepoRoot "backend\ops\local\tool-paths.local.ps1"
    if (Test-Path -LiteralPath $localConfig) {
        . $localConfig
    }
}

function Resolve-OesCommandPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    $looksLikePath = [System.IO.Path]::IsPathRooted($Command) -or $Command.Contains("\") -or $Command.Contains("/")
    if ($looksLikePath) {
        return $Command
    }

    try {
        return (Get-Command $Command -ErrorAction Stop | Select-Object -First 1).Source
    } catch {
        return $Command
    }
}

function Convert-ToOesUnixPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    return $Path.Replace("\", "/")
}

function Get-OesPathConfig {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ScriptDir
    )

    $repoRoot = Get-OesRepoRoot -ScriptDir $ScriptDir
    Import-OesLocalToolOverrides -RepoRoot $repoRoot

    $backendDir = Join-Path $repoRoot "backend"
    $opsDir = Join-Path $backendDir "ops"
    $targetDir = Join-Path $backendDir "target"
    $toolsRoot = if ($env:OES_TOOLS_ROOT) { $env:OES_TOOLS_ROOT } else { Join-Path $repoRoot "tools" }
    $nginxRuntimeDir = Join-Path $opsDir "nginx\runtime"
    $nginxGeneratedDir = Join-Path $nginxRuntimeDir "generated"

    return [pscustomobject]@{
        RepoRoot               = $repoRoot
        BackendDir             = $backendDir
        OpsDir                 = $opsDir
        TargetDir              = $targetDir
        ToolsRoot              = $toolsRoot
        FrontendDist           = Join-Path $repoRoot "frontend\dist"
        MavenCommand           = Resolve-OesCommandPath -Command ($(if ($env:OES_MAVEN_CMD) { $env:OES_MAVEN_CMD } else { "mvn.cmd" }))
        MysqlHome              = $(if ($env:OES_MYSQL_HOME) { $env:OES_MYSQL_HOME } else { "D:\MySQL\server" })
        NginxHome              = $(if ($env:OES_NGINX_HOME) { $env:OES_NGINX_HOME } else { Join-Path $toolsRoot "nginx\nginx-1.28.0" })
        PrometheusHome         = $(if ($env:OES_PROMETHEUS_HOME) { $env:OES_PROMETHEUS_HOME } else { Join-Path $toolsRoot "prometheus\prometheus-3.5.0.windows-amd64" })
        K6Home                 = $(if ($env:OES_K6_HOME) { $env:OES_K6_HOME } else { Join-Path $toolsRoot "k6\k6\k6-v1.7.1-windows-amd64" })
        MemuraiHome            = $(if ($env:OES_MEMURAI_HOME) { $env:OES_MEMURAI_HOME } else { "D:\Memurai" })
        RabbitMqHome           = $(if ($env:OES_RABBITMQ_HOME) { $env:OES_RABBITMQ_HOME } else { "D:\RabbitMQ\rabbitmq_server-4.2.5" })
        ErlangHome             = $(if ($env:OES_ERLANG_HOME) { $env:OES_ERLANG_HOME } else { "D:\Erlang OTP" })
        PrometheusConfig       = Join-Path $opsDir "prometheus\prometheus.yml"
        NginxTemplate          = Join-Path $opsDir "nginx\nginx.conf.template"
        NginxRuntimeDir        = $nginxRuntimeDir
        NginxGeneratedDir      = $nginxGeneratedDir
        NginxGeneratedConfig   = Join-Path $nginxGeneratedDir "online-exam-test.generated.conf"
        MysqlReplicaRuntimeDir = Join-Path $opsDir "mysql-replica\runtime"
        LocalToolConfig        = Join-Path $repoRoot "backend\ops\local\tool-paths.local.ps1"
    }
}

function New-OesNginxGeneratedConfig {
    param(
        [Parameter(Mandatory = $true)]
        [object]$PathConfig
    )

    if (!(Test-Path -LiteralPath $PathConfig.NginxTemplate)) {
        throw "nginx template not found: $($PathConfig.NginxTemplate)"
    }

    if (!(Test-Path -LiteralPath $PathConfig.NginxGeneratedDir)) {
        New-Item -ItemType Directory -Path $PathConfig.NginxGeneratedDir -Force | Out-Null
    }

    $template = Get-Content -LiteralPath $PathConfig.NginxTemplate -Raw -Encoding UTF8
    $rendered = $template.Replace("__OES_NGINX_DIR__", (Convert-ToOesUnixPath -Path $PathConfig.NginxHome))
    $rendered = $rendered.Replace("__OES_REPO_ROOT__", (Convert-ToOesUnixPath -Path $PathConfig.RepoRoot))
    $rendered = $rendered.Replace("__OES_FRONTEND_DIST__", (Convert-ToOesUnixPath -Path $PathConfig.FrontendDist))
    $rendered = $rendered.Replace("__OES_NGINX_RUNTIME_DIR__", (Convert-ToOesUnixPath -Path $PathConfig.NginxRuntimeDir))

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($PathConfig.NginxGeneratedConfig, $rendered, $utf8NoBom)

    return $PathConfig.NginxGeneratedConfig
}
