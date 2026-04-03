# LaiKeAPI - 来客电商系统 API 服务

基于 ThinkPHP 6.0 开发的电商系统后端 API 服务，支持多商户、多端访问、丰富的营销插件和国际化支持。

## 功能特性

- 多商户管理系统
- 多端适配（管理后台、移动端、PC商城、店铺端、供应商端）
- 丰富的营销插件（秒杀、拼团、预售、分销、积分、竞拍等）
- 多语言支持（8种语言）
- 多种支付方式（微信支付、支付宝、PayPal等）
- 完善的权限系统
- 定时任务支持
- 多种文件存储（本地、阿里云OSS、AWS S3）

## 环境要求

- **PHP**: 8.0.1*
- **MySQL**: 8.0.16
- **Redis**: >=6.0
- **PHP 扩展**:
  - PDO MySQL
  - Redis
  - cURL
  - mbstring
  - fileinfo
  - GD 或 Imagick
  - OpenSSL
  - XML

## 宝塔面板部署流程

### 1. 安装宝塔面板

```bash
# CentOS
yum install -y wget && wget -O install.sh http://download.bt.cn/install/install_6.0.sh && sh install.sh

# Ubuntu
wget -O install.sh http://download.bt.cn/install/install-ubuntu_6.0.sh && sudo bash install.sh
```

### 2. 安装运行环境

1. 登录宝塔面板，进入「软件商店」
2. 安装以下软件：
   - **Nginx** 1.18+
   - **PHP** 8.0.1*
   - **MySQL** 8.0.16
   - **Redis** 6.0+
   - **phpMyAdmin** 5.0+

### 3. PHP 扩展配置

1. 进入「软件商店」→「PHP8.0.1*」→「设置」
2. 点击「安装扩展」，安装以下扩展：
   - redis
   - fileinfo
   - opcache（可选，提升性能）

3. 点击「配置文件」，修改以下参数：
```ini
memory_limit = 256M
upload_max_filesize = 50M
post_max_size = 50M
max_execution_time = 300
```

### 4. 创建网站

1. 进入「网站」→「添加站点」
2. 填写信息：
   - **域名**: 你的域名（如 api.example.com）
   - **PHP 版本**: PHP-8.0.1*
   - **数据库**: MySQL
   - **数据库名**: laiketui
   - **用户名**: laiketui（或自定义）
   - **密码**: 自动生成或自定义
3. 点击「提交」

### 5. 上传项目代码

**方式一：通过 Git 拉取**
```bash
cd /www/wwwroot/api.example.com
git clone <仓库地址> .
```

**方式二：通过 FTP 上传**
1. 使用 FTP 工具将项目文件上传到网站根目录
2. 确保目录结构正确

### 6. 配置网站运行目录

1. 进入「网站」→ 点击网站名称 →「网站目录」
2. 修改「运行目录」为 `/public`
3. 保存设置

### 7. 配置伪静态（Nginx）

1. 进入「网站」→ 点击网站名称 →「伪静态」
2. 选择「ThinkPHP」规则，或手动添加以下配置：

```nginx
location / {
    if (!-e $request_filename){
        rewrite ^(.*)$ /index.php?s=$1 last; break;
    }
}

location ~ \.php$ {
    fastcgi_pass   unix:/tmp/php-cgi-74.sock;
    fastcgi_index  index.php;
    fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
    include        fastcgi_params;
}

# 禁止访问隐藏文件
location ~ /\. {
    deny all;
}
```

3. 点击「保存」

### 8. 配置数据库

1. 进入「数据库」，点击刚才创建的数据库
2. 点击「phpMyAdmin」，导入数据库文件
3. 数据库文件位于项目 `app/db_log/` 目录
4. 按顺序导入 SQL 文件

### 9. 配置项目环境

1. 复制环境配置文件：
```bash
cd /www/wwwroot/api.example.com
cp .env.example .env
```

2. 编辑 `.env` 文件：
```env
APP_DEBUG = false

[APP]
DEFAULT_TIMEZONE = Asia/Shanghai

[DATABASE]
TYPE = mysql
HOSTNAME = 127.0.0.1
DATABASE = laiketui
USERNAME = laiketui
PASSWORD = 你的数据库密码
HOSTPORT = 3306
CHARSET = utf8mb4
DEBUG = false

[REDIS]
HOST = 127.0.0.1
PORT = 6379
PASSWORD = 
SELECT = 1

[LANG]
default_lang = zh-cn
```

3. 编辑 `config/db_config.php`，修改数据库常量：
```php
define('MYSQL_TYPE','mysql');
define('MYSQL_SERVER','127.0.0.1');
define('MYSQL_USER','laiketui');
define('MYSQL_PASSWORD','你的数据库密码');
define('MYSQL_DATABASE','laiketui');
define('MYSQL_PORT',3306);
define('MYSQL_CHARSET','utf8mb4');
define('MYSQL_DEBUG',false);
```

### 10. 安装项目依赖

1. 进入宝塔终端，执行：
```bash
cd /www/wwwroot/api.example.com
composer install --no-dev
```

### 11. 设置目录权限

```bash
cd /www/wwwroot/api.example.com
chmod -R 755 runtime/
chmod -R 755 public/upload/
chmod -R 755 public/image/
chown -R www:www ./
```

### 12. 配置 SSL 证书（可选但推荐）

1. 进入「网站」→ 点击网站名称 →「SSL」
2. 选择「Let's Encrypt」免费证书，或上传自己的证书
3. 开启「强制 HTTPS」

### 13. 配置定时任务（可选）

1. 进入「计划任务」
2. 添加任务：
   - **任务类型**: Shell脚本
   - **任务名称**: 数据备份
   - **执行周期**: 每天
   - **脚本内容**:
   ```bash
   cd /www/wwwroot/api.example.com && php think backup
   ```

### 14. 配置防火墙

1. 进入「安全」
2. 放行端口：
   - 80（HTTP）
   - 443（HTTPS）
   - 3306（MySQL，如需远程连接）
   - 6379（Redis，如需远程连接）

## 项目目录结构

```
LaiKeAPI/
├── app/                    # 应用目录
│   ├── admin/             # 后台应用
│   │   ├── controller/    # 控制器（按功能分组）
│   │   ├── model/         # 数据模型
│   │   └── view/          # 视图模板
│   ├── common/            # 公共类库
│   ├── db_log/            # 数据库脚本
│   ├── pluginsdb/         # 插件数据库脚本
│   └── task/              # 定时任务
├── config/                # 配置目录
├── public/                # Web 入口目录
├── route/                 # 路由目录
├── runtime/               # 运行时目录（缓存、日志）
├── vendor/                # Composer 依赖
└── extend/                # 扩展目录
```

## API 接口目录

```
app/admin/controller/
├── admin/          # 管理后台接口
├── app/            # 移动端接口
├── mall/           # PC商城接口
├── mch/            # 店铺接口
├── MchSon/         # 门店管理接口
├── plugin/         # 插件接口
│   ├── sec/       # 秒杀
│   ├── presell/   # 预售
│   ├── distribution/ # 分销
│   ├── group/     # 拼团
│   ├── integral/  # 积分
│   ├── auction/   # 竞拍
│   ├── template/  # 模版/DIY
│   ├── coupon/    # 优惠券
│   └── member/    # 会员制
├── resources/      # 资源管理接口
├── saas/           # 平台接口
└── supplier/       # 供应商接口
```

## 开发命令

```bash
# 安装依赖
composer install

# 本地开发服务器
php -S 127.0.0.1:8000 -t public

# 查看 ThinkPHP 命令
php think

# 生成应用映射缓存（生产环境推荐）
php think optimize:autoload

# 清除缓存
php think clear
```

## 代码规范

- 缩进：4 个空格
- 大括号换行
- 类名：PascalCase
- 方法名：camelCase
- 变量名：camelCase
- 常量：UPPER_SNAKE_CASE

## 插件命名规则

插件控制器命名规范：
- `AdminXxx` - 商城管理后台接口
- `AppXxx` - 移动端接口
- `MchXxx` - PC卖家端接口

例如：`AdminPreSell`、`AppPreSell`、`MchPreSell`

## 常见问题

### 1. 500 错误
- 检查 `runtime/` 目录权限
- 检查 PHP 错误日志
- 确认 `.env` 配置正确

### 2. 数据库连接失败
- 检查数据库服务是否启动
- 确认数据库用户名和密码正确
- 检查数据库是否允许远程连接

### 3. Redis 连接失败
- 检查 Redis 服务是否启动
- 确认 Redis 配置正确
- 检查防火墙设置

### 4. 伪静态不生效
- 检查 Nginx 配置
- 重启 Nginx 服务
- 检查运行目录设置

### 5. 文件上传失败
- 检查 PHP 上传配置
- 确认目录权限正确
- 检查磁盘空间

## 技术支持

- 项目框架：ThinkPHP 6.0
- PHP 版本：8.0.1*
- 数据库：MySQL 8.0.16
- 缓存：Redis 6.0+

## License
Apache License 2.0