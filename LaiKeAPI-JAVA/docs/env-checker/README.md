# Env Checker – 跨平台服务版本检测工具

自动检测以下组件的版本：

- MySQL 8.0.29
- Redis 6+
- PHP   8.0.1x
- Java  1.8
- Node  14.20
- Nacos 2.1.1

输出结果会同时展示：`当前机器版本`（当前机器）和 `要求版本`（模板要求）。

## 支持平台

- Linux
- macOS
- Windows (PowerShell)

## 使用方式

### Linux / macOS

```bash
chmod +x env_linux_mac.sh
./env_linux_mac.sh
```

linux 如下图：

```shell
[root@java resources]# ./env_linux_mac.sh
🔍 Detecting MySQL version...

✅ Platform: Linux
========================================
Service    | 当前机器版本     | 要求版本
-----------+-------------------------------+---------------------
MySQL      | (Docker) 8.0.29               | 8.0.29
Redis      | 8.2.2                         | 6+
PHP        | 8.0.26                        | 8.0.1x
Java       | 1.8.0                         | 1.8
Node       | 14.20.1                       | 14.20
Nacos      | Running (unknown)             | 2.1.1
```

macos 如下图：

```shell
wangxian@OO env-checker % ./env_linux_mac.sh
🔍 Detecting MySQL version...

✅ Platform: macOS
========================================
Service    | 当前机器版本     | 要求版本
-----------+-------------------------------+---------------------
MySQL      | 8.0.27                        | 8.0.29
Redis      | 6.2.6                         | 6+
PHP        | 5.6                           | 8.0.1x
Java       | 1.8.0                         | 1.8
Node       | 16.15.0                       | 14.20
Nacos      | Running (unknown)             | 2.1.1

```

### Windows

```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser  # 首次运行需设置
.\env_windows_powershell.ps1
```
