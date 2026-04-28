# RabbitMQ 本地 broker 说明

当前本机已完成原生安装：

- Erlang OTP: `D:\Erlang OTP`
- RabbitMQ: `D:\RabbitMQ\rabbitmq_server-4.2.5`
- Windows 服务名: `RabbitMQ`

## 当前可用口径

- AMQP: `127.0.0.1:5672`
- Management UI: `http://127.0.0.1:15672`
- Prometheus: `http://127.0.0.1:15692/metrics`
- 默认账号: `guest / guest`
- 当前仓库中的 `application.properties` 已默认对齐到这个本地 broker

## 当前目录布局

- 安装目录：`D:\RabbitMQ\rabbitmq_server-4.2.5`
- 运行根目录：`D:\RabbitMQ\appdata`
- 配置文件：`D:\RabbitMQ\appdata\rabbitmq.conf`
- 插件文件：`D:\RabbitMQ\appdata\enabled_plugins`
- 环境变量脚本：`D:\RabbitMQ\appdata\rabbitmq-env-conf.bat`
- 日志目录：`D:\RabbitMQ\appdata\log`
- 数据目录：`D:\RabbitMQ\appdata\db`

## 服务启动链路

Windows 服务实际走的是安装目录下的 `sbin` 脚本链：

- [rabbitmq-defaults.bat](/D:/RabbitMQ/rabbitmq_server-4.2.5/sbin/rabbitmq-defaults.bat)
- [rabbitmq-env.bat](/D:/RabbitMQ/rabbitmq_server-4.2.5/sbin/rabbitmq-env.bat)
- [rabbitmq-service.bat](/D:/RabbitMQ/rabbitmq_server-4.2.5/sbin/rabbitmq-service.bat)

当前已将 `rabbitmq-defaults.bat` 的默认 `RABBITMQ_BASE` 固定到：

```bat
D:\RabbitMQ\appdata
```

因此服务启动时会优先从 `D:\RabbitMQ\appdata` 读取运行态配置，而不是回退到 `%APPDATA%\RabbitMQ`。

## 管理控制台与 Prometheus 插件

`enabled_plugins` 内容：

```erlang
[rabbitmq_management,rabbitmq_prometheus].
```

`rabbitmq.conf` 推荐内容：

```conf
listeners.tcp.default = 5672
management.tcp.ip = 127.0.0.1
management.tcp.port = 15672
prometheus.tcp.ip = 127.0.0.1
prometheus.tcp.port = 15692
loopback_users.guest = true
```

## 环境变量

若要确保 RabbitMQ 的数据、日志、配置都固定落在 `D:`，建议设置以下系统环境变量。

管理员 PowerShell 执行：

```powershell
[Environment]::SetEnvironmentVariable('ERLANG_HOME', 'D:\Erlang OTP', 'Machine')
[Environment]::SetEnvironmentVariable('RABBITMQ_BASE', 'D:\RabbitMQ\appdata', 'Machine')
[Environment]::SetEnvironmentVariable('RABBITMQ_CONFIG_FILE', 'D:\RabbitMQ\appdata\rabbitmq', 'Machine')
[Environment]::SetEnvironmentVariable('RABBITMQ_ENABLED_PLUGINS_FILE', 'D:\RabbitMQ\appdata\enabled_plugins', 'Machine')
[Environment]::SetEnvironmentVariable('RABBITMQ_LOG_BASE', 'D:\RabbitMQ\appdata\log', 'Machine')
[Environment]::SetEnvironmentVariable('RABBITMQ_MNESIA_BASE', 'D:\RabbitMQ\appdata\db', 'Machine')
```

然后确保目录存在：

```powershell
New-Item -ItemType Directory -Force -Path 'D:\RabbitMQ\appdata' | Out-Null
New-Item -ItemType Directory -Force -Path 'D:\RabbitMQ\appdata\log' | Out-Null
New-Item -ItemType Directory -Force -Path 'D:\RabbitMQ\appdata\db' | Out-Null
```

## 刷新服务参数

写完环境变量后，建议重新安装一次 Windows 服务参数，然后重启服务。

管理员 PowerShell 执行：

```powershell
$env:ERLANG_HOME='D:\Erlang OTP'
$env:RABBITMQ_BASE='D:\RabbitMQ\appdata'
$env:RABBITMQ_CONFIG_FILE='D:\RabbitMQ\appdata\rabbitmq'
$env:RABBITMQ_ENABLED_PLUGINS_FILE='D:\RabbitMQ\appdata\enabled_plugins'
$env:RABBITMQ_LOG_BASE='D:\RabbitMQ\appdata\log'
$env:RABBITMQ_MNESIA_BASE='D:\RabbitMQ\appdata\db'

& 'D:\RabbitMQ\rabbitmq_server-4.2.5\sbin\rabbitmq-service.bat' install
net stop RabbitMQ
net start RabbitMQ
```

说明：

- `install` 在服务已存在时会更新服务参数
- `RABBITMQ_CONFIG_FILE` 不带 `.conf` 后缀，RabbitMQ 会自动读取 `rabbitmq.conf`

## 验证

端口验证：

```powershell
Test-NetConnection 127.0.0.1 -Port 5672
Test-NetConnection 127.0.0.1 -Port 15672
Test-NetConnection 127.0.0.1 -Port 15692
Invoke-WebRequest http://127.0.0.1:15692/metrics
```

预期结果：

- `5672` 连通
- `15672` 返回 `200`
- `15692` 返回 `200`
- `/metrics` 返回 Prometheus 文本指标

日志验证：

```powershell
Get-Content 'D:\RabbitMQ\appdata\log\rabbit@GWK.log' -Tail 80
```

应能在启动日志中看到类似信息：

- `config file(s) : d:/RabbitMQ/appdata/rabbitmq.conf`
- `log(s) : d:/RabbitMQ/appdata/log/...`
- `data dir : d:/RabbitMQ/appdata/db/...`

## 项目对接

当前仓库默认直连本机 RabbitMQ：

- `spring.rabbitmq.host=127.0.0.1`
- `spring.rabbitmq.port=5672`
- `spring.rabbitmq.username=guest`
- `spring.rabbitmq.password=guest`

对应文件：

- [application.properties](/D:/test/online-exam-system/backend/src/main/resources/application.properties)

若后续 phase 6 需要切换到独立业务用户，建议新增：

- 用户：`oes_app`
- vhost：`/oes`

然后同步修改 Spring 配置中的用户名、密码和 vhost。
