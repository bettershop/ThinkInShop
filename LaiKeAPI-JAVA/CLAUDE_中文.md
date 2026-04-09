# CLAUDE_中文.md

此文件为 Claude Code (claude.ai/code) 提供中文项目指导文档。

## 项目概述

这是一个多模块 Maven 项目，用于构建来客（Laike）电商平台 - 一个多租户、多店铺的 SaaS 电商系统，支持商城、店铺、供应商以及各种业务扩展（优惠券、拼团、积分、竞拍等）。

## 技术栈

| 组件 | 版本 | 说明 |
|-----------|----------|-------|
| JDK | 1.8.211 | 必须严格使用此版本 |
| Spring Boot | 2.3.12.RELEASE | - |
| Spring Cloud Alibaba | 2.2.7.RELEASE | - |
| Spring Cloud | Hoxton.SR12 | - |
| Nacos | 2.1.1 | 配置中心与服务发现 |
| Dubbo | 3.0.8 | RPC 框架（部分已弃用，新代码使用 OpenFeign） |
| MyBatis/Tk-MyBatis | 2.1.5 | 持久层框架 |
| MySQL | 5.6-5.7 | 需配置 `sql_mode=NO_ZERO_DATE` |
| Redis | 7 | - |
| Maven | 3.6.1 | 3.8+ 版本存在兼容问题 |
| Tomcat | 9.0.55 | 用于 WAR 包部署 |

## 架构设计

### 模块结构

```
laike-root/          # 授权库（预安装的 JAR 包）
laike-core/          # 公共配置、缓存、工具类、异常处理
laike-domain/         # 领域实体和模型
laike-common/         # 共享业务逻辑、Mapper
laike-common-api/     # 公共 API 接口定义

laike-admins/        # PC 管理后台（平台级 + 店铺级 + 供应商级管理）
laike-admins-api/    # 管理后台 API 接口

laike-apps/          # 移动端买家应用后台
laike-apps-api/      # 买家应用 API 接口

laike-comps/          # 公共服务（网关、支付、订单、文件、IM、定时任务）
laike-comps-api/      # 公共服务 API 接口

laike-plugins/        # 插件服务（营销功能：优惠券、拼团、秒杀等）
laike-plugins-api/    # 插件服务 API 接口

laike-cloud-gateway/  # Spring Cloud Gateway（可选，用于 OpenFeign 路由[说明:现有不支持 新增功能需自己新增 OpenFeign 调用和分布式事务处理]）
laike-apis/           # 统一入口模块
```

### 部署架构

系统使用两层网关架构：

1. **laike-comps/gateway** - 自定义网关（集成在 comps 模块中，现有项目必须启动）
2. **laike-cloud-gateway** - Spring Cloud Gateway（可选，仅在使用 OpenFeign 调用新服务时需要）

除 IM 和支付回调外，所有 API 请求必须经过网关。

### 核心服务

- **laike-comps**: 包含核心共享服务（网关、支付、订单处理、文件管理、IM、定时任务）
- **laike-admins**: 多租户管理后台（平台级 + 店铺级 + 供应商级）
- **laike-apps**: 移动端买家接口
- **laike-plugins**: 可扩展的营销插件（拼团、优惠券、积分、竞拍等）

### 数据库设计

多租户隔离：通过 store_id（商城 ID）进行数据隔离。表名使用 `lkt_` 前缀（如 `lkt_order`、`lkt_user`）。

## 构建与运行命令

### Maven 构建

```bash
# 完整构建（默认跳过测试）
mvn clean install -DskipTests

# 打包（跳过测试）
mvn clean package

# 构建指定模块及其依赖
mvn -pl laike-admins -am package -DskipTests

# 强制执行测试
mvn test -DskipTests=false
```

### 环境配置

可用的 Maven 配置文件：`local`（默认）、`dev`、`prod`、`test`、`demo`、`inter`

配置文件控制：
- Nacos 命名空间和地址
- 数据库和 Redis 配置（通过 Nacos）
- MyBatis 配置文件

### 本地运行服务

使用 IDEA 的 Services 视图或直接运行主类：

- `com.laiketui.admins.LaikeAdminsApplication` - 管理后台
- `com.laiketui.apps.LaikeAppsApplication` - 买家应用后台
- `com.laiketui.comps.LaikeCompsApplication` - 公共服务（包含网关，必须启动）
- `com.laiketui.plugins.LaikePluginsApplication` - 插件服务

### Spring Cloud Gateway（可选）

```bash
java -jar laike-cloud-gateway/target/laike-cloud-gateway-0.0.1-SNAPSHOT.jar
```

## 配置管理

### Nacos 配置中心

所有运行时配置存储在 Nacos 配置中心。

**公共配置文件**（按环境）：
- `comm-local.yml`、`comm-dev.yml`、`comm-prod.yml`、`comm-test.yml`、`comm-demo.yml`

**模块特定配置**：
- `laike-admin-store-{profile}.yml`
- `laike-admin-mch-{profile{.yml`
- `laike-apps-{profile}.yml`
- `laike-plugins-{profile}.yml`

**Nacos 中的关键配置**：
- 数据库连接（url、username、password）
- Redis 连接（host、port、password、database）
- 服务器端口（`server.port`）
- 文件上传路径（`uploadFile.path`）
- 微信证书路径（`node.wx-certp12-path`）

### 本地配置覆盖

可通过环境变量覆盖默认配置：
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PWD=laiketui
export REDIS_DB=0
export UPLOAD_PATH=/usr/local/imgs
```

## 重要部署说明

### 端口配置

Tomcat `server.xml` 中的连接器端口必须与对应 Nacos 配置中的 `server.port` 一致。端口不匹配会导致 404 或服务未找到错误。

修改端口后：在重启 Tomcat 前需要清空 Redis 中的服务注册缓存。

### Tomcat 配置

修改 `conf/catalina.properties` 第 108 行以避免启动缓慢：
```properties
tomcat.util.scan.StandardJarScanFilter.jarsToSkip=*.jar
```

### 授权库

`laike-root-0.0.1-SNAPSHOT.jar` 是预安装的 JAR 包，必须存在于本地 Maven 仓库中：

```bash
mvn install:install-file \
  -Dfile=/path/to/laike-root-0.0.1-SNAPSHOT.jar \
  -DgroupId=com.laiketui \
  -DartifactId=laike-root \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackaging=jar
```

### 数据库迁移

SQL 变更脚本放在 `db_logs/incremental-sql/` 目录下。
命名规范：`YYYYMMDD_姓名首字母.sql`（例如：`20240314_wx.sql`）。

## 开发规范

### 控制器层

控制器使用 `@HttpApiMethod(apiKey = "...")` 注解进行 API 路由。
示例：
```java
@ApiOperation("获取库存信息")
@PostMapping("/getStockInfo")
@HttpApiMethod(apiKey = "admin.goods.getStockInfo")
public Result getStockInfo(StockInfoVo vo, HttpServletResponse response) {
    // ...
}
```

### 服务层

服务遵循命名规范：`*Service`（接口）和 `*ServiceImpl`（实现类）。
包结构映射功能区域（如 `saas`、`plugin`、`users`、`order`）。

### Excel 导出

Excel 导出接口需要：
1. 接受 `exportType=1` 请求参数
2. 返回 `Result.exportFile()`（等价于返回 null）

```java
Map<String, Object> ret = stockService.getStockInfo(vo, response);
return ret == null ? Result.exportFile() : Result.success(ret);
```

### 订单状态值

- `0`：待付款
- `1`：已付款
- `2`：待收货
- `5`：订单完成（未结算、售后未完成）
- `7`：订单关闭（已结算、售后已完成）

## 外部服务

### 必需服务

- **Nacos 2.1.1**：配置中心和服务发现（高并发场景多、且用户量大场景请用阿里云Nacos服务）
- **MySQL 8.0.29**：数据库 （高并发场景多、且用户量大场景请用阿里云 RDS）
- **Redis 7**：缓存和会话 （高并发场景多、且用户量大场景请用阿里云REDIS服务）
- **MinIO 8.5.4**：对象存储（可选，业务大用户多的情况请用阿里云 OSS）
- **XXL-JOB**：定时任务管理（用于 `laike-comps/task`）
- **RocketMQ**：消息队列（可选）

### 支付集成

- 微信支付（包括小程序和公众号）
- 支付宝
- PayPal
- Stripe

## 故障排查

### 服务未找到 / 404

1. 验证 Tomcat `server.xml` 端口与 Nacos `server.port` 一致
2. 检查 Nacos 服务注册是否有陈旧条目（清空 Redis）
3. 确保 `laike-comps`（网关）正在运行

### Maven 构建问题

- 如果 JAR 包无法下载，检查 Maven 版本是否为 3.6.1
- 验证 `laike-root` JAR 已安装到本地仓库
- 执行多次 `mvn clean` 和 `Reload Project` 操作

### 配置未生效

- 切换 Maven 配置文件后，执行 `mvn` clean 并 Reload Project
- 检查 Nacos 命名空间是否与选中的配置文件匹配

## 代码规范

- 包命名：`com.laiketui.{模块}.{包名}`
- 类命名：`PascalCase`
- 方法/字段：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- 文件编码：UTF-8
- 缩进：4 个空格
