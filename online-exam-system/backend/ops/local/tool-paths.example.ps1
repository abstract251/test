# Copy this file to tool-paths.local.ps1 and adjust the paths for your machine.
# The local file is ignored by git.

$env:OES_MAVEN_CMD = "mvn.cmd"

# Optional: set this if you want all repo-managed tools to resolve from a custom base directory.
# $env:OES_TOOLS_ROOT = "E:\tools"

$env:OES_MYSQL_HOME = "D:\MySQL\server"
$env:OES_NGINX_HOME = "D:\test\online-exam-system\tools\nginx\nginx-1.28.0"
$env:OES_PROMETHEUS_HOME = "D:\test\online-exam-system\tools\prometheus\prometheus-3.5.0.windows-amd64"
$env:OES_K6_HOME = "D:\test\online-exam-system\tools\k6\k6\k6-v1.7.1-windows-amd64"
$env:OES_MEMURAI_HOME = "D:\Memurai"
$env:OES_RABBITMQ_HOME = "D:\RabbitMQ\rabbitmq_server-4.2.5"
$env:OES_ERLANG_HOME = "D:\Erlang OTP"
