
## linux、macos使用Docker安装Mysql8

### 1、首先在安装目标机上安装好Docker并启动

### 2、在目标机器上面建好目录：
/root/mysql8-data/conf.d
/root/mysql8-data/data
/root/mysql8-data/log

### 3、在conf.d目录中建文件夹 customer.cnf 内容如下
```cnf
[mysqld]
# 字符集配置
character-set-server = utf8mb4
collation-server = utf8mb4_general_ci
skip-name-resolve
sql_mode=NO_ZERO_DATE
default-time-zone = '+08:00'

# 允许远程访问
bind-address = 0.0.0.0

# 默认认证插件（兼容 Navicat / PHP / Java）
default_authentication_plugin = mysql_native_password

# 连接数设置
max_connections = 500
wait_timeout = 28800
interactive_timeout = 28800

# 日志
log-error = /var/lib/mysql/mysql-error.log
slow_query_log = 1
slow_query_log_file = /var/lib/mysql/mysql-slow.log
long_query_time = 2

# 事务 & InnoDB 配置
innodb_buffer_pool_size = 256M
innodb_log_file_size = 128M
innodb_flush_log_at_trx_commit = 1

[client]
default-character-set = utf8mb4

[mysql]
default-character-set = utf8mb4

```

### 4、然后执行以下脚本：

```shell
docker run --name mysql8-server 
-p 3307:3306 
-e MYSQL_ROOT_PASSWORD=123456 -d 
-v /root/mysql8-data/conf.d:/etc/mysql/conf.d 
-v /root/mysql8-data/log:/var/log/mysql 
-v /root/mysql8-data/data:/var/lib/mysql mysql:8.0.29 --skip-name-resolve

```

Mysql 版本可 mysql:8.0.29 修改这个冒号后面的版本号 此处以mysql:8.0.29版本安装为例子
命令行中解释
-v 宿主机目录：docker容器目录 注：确保宿主机目录写权限

### windows版本安装的不同点在于挂载的目录改成 windows支持的路径格式 