# 来客电商 (Laike E-commerce Platform)

一个基于 Spring Boot 和 Spring Cloud 的多租户、多店铺 SaaS 电商系统，支持商城、店铺、供应商以及各种业务扩展（优惠券、拼团、积分、竞拍等）。

## 项目简介

来客电商平台是一个功能完善的商业级电商解决方案，提供：

- **多租户架构**：支持平台级、店铺级、供应商级多层级管理
- **丰富的营销功能**：优惠券、拼团、秒杀、积分、竞拍等
- **多端支持**：PC 管理后台、移动端买家应用
- **灵活的支付集成**：微信支付、支付宝、PayPal、Stripe
- **微服务架构**：基于 Spring Cloud 和 Nacos 的分布式系统

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8.211 | Java 运行环境 |
| Spring Boot | 2.3.12.RELEASE | 应用框架 |
| Spring Cloud | Hoxton.SR12 | 微服务框架 |
| Spring Cloud Alibaba | 2.2.7.RELEASE | 阿里云微服务套件 |
| Nacos | 2.1.1 | 配置中心和服务发现 |
| MyBatis / Tk-MyBatis | 2.1.5 | 持久层框架 |
| MySQL | 8.0.29 | 关系型数据库 |
| Redis | 7 | 缓存和会话存储 |
| Maven | 3.6.1 | 项目构建工具 |
| Dubbo | 3.0.8 | RPC 框架（部分已弃用） |

## 项目结构

```
laike-root/              # 授权库（预安装 JAR）
laike-core/              # 公共配置、缓存、工具类、异常处理
laike-domain/            # 领域实体和模型
laike-common/            # 共享业务逻辑、Mapper
laike-common-api/        # 公共 API 接口定义

laike-admins/            # PC 管理后台（平台 + 店铺 + 供应商）
laike-admins-api/        # 管理后台 API 接

laike-apps/              # 移动端买家应用后端
laike-apps-api/          # 买家应用 API 接口

laike-comps/             # 公共服务（网关、支付、订单、文件、IM、任务）
laike-comps-api/         # 公共服务 API 接口

laike-plugins/           # 插件服务（营销功能）
laike-plugins-api/       # 插件服务 API 接口

laike-cloud-gateway/     # Spring Cloud Gateway（可选）
laike-apis/              # 统一入口模块
```

## 快速开始

### 前置要求

- JDK 1.8.211
- Maven 3.6.1
- MySQL 8.0.29
- Redis 7
- Nacos 2.1.1

### 安装授权库

将 `laike-root-0.0.1-SNAPSHOT.jar` 安装到本地 Maven 仓库：

```bash
mvn install:install-file \
  -Dfile=/path/to/laike-root-0.0.1-SNAPSHOT.jar \
  -DgroupId=com.laiketui \
  -DartifactId=laike-root \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackaging=jar
```

### 构建项目

```bash
# 完整构建（跳过测试）
mvn clean install -DskipTests

# 构建指定模块
mvn -pl laike-admins -am package -DskipTests

# 强制执行测试
mvn test -DskipTests=false
```

## 环境配置

项目支持多环境配置：`local`（默认）、`dev`、`prod`、`test`、`demo`、`inter`

切换环境：

```bash
# 开发环境

mvn clean install -P dev -DskipTests

# 生产环境
mvn clean install -P prod -DskipTests
```

### Nacos 配置

所有运行时配置存储在 Nacos 配置中心。需要配置：

- **公共配置文件**：`comm-{profile}.yml`
- **模块配置文件**：`laike-admin-store-{profile}.yml`、`laike-apps-{profile}.yml` 等
- **数据库连接**：MySQL URL、用户名、密码
- **Redis 连接**：主机、端口、密码、数据库
- **文件上传路径**：`uploadFile.path`
- **微信证书路径**：`node.wx-certp12-path`

## 运行服务

### 使用 IDEA 运行

在 IDEA 的 Services 视图中运行以下主类：

- `com.laiketui.admins.LaikeAdminsApplication` - 管理后台
- `com.laiketui.apps.LaikeAppsApplication` - 买家应用后台
- `com.laiketui.comps.LaikeCompsApplication` - 公共服务（必须启动，包含网关）
- `com.laiketui.plugins.LaikePluginsApplication` - 插件服务

### 使用命令行运行

```bash
# 管理后台
java -jar laike-admins/target/laike-admins-0.0.1-SNAPSHOT.war

# 买家应用
java -jar laike-apps/target/laike-apps-0.0.1-SNAPSHOT.war

# 公共服务（网关）
java -jar laike-comps/target/laike-comps-0.0.1-SNAPSHOT.war

# 插件服务
java -jar laike-plugins/target/laike-plugins-0.0.1-SNAPSHOT.war

# Spring Cloud Gateway（可选）
java -jar laike-cloud-gateway/target/laike-cloud-gateway-0.0.1-SNAPSHOT.jar
```

## 架构说明

### 网关架构

系统使用两层网关：

1. **laike-comps/gateway** - 自定义网关（集成在 comps 模块中，必须启动）
2. **laike-cloud-gateway** - Spring Cloud Gateway（可选，仅在使用 OpenFeign 调用新服务时需要）

### 多租户隔离

数据通过 `store_id`（商城 ID）进行多租户隔离。所有表名使用 `lkt_` 前缀。

### API 路由

控制器使用 `@HttpApiMethod(apiKey = "...")` 注解进行 API 路由：

```java
@ApiOperation("获取库存信息")
@PostMapping("/getStockInfo")
@HttpApiMethod(apiKey = "admin.goods.getStockInfo")
public Result getStockInfo(StockInfoVo vo, HttpServletResponse response) {
    // ...
}
```

## 支付集成

系统支持以下支付方式：

- **微信支付**：小程序支付、公众号支付
- **支付宝**：网页支付、移动支付
- **PayPal**：跨境支付
- **Stripe**：国际支付

## 故障排查

### 服务未找到 / 404

1. 验证 Tomcat `server.xml` 端口与 Nacos `server.port` 一致
2. 检查 Nacos 服务注册是否有陈旧条目（清空 Redis）
3. 确保 `laike-comps`（网关）正在运行

### Maven 构建问题

- 检查 Maven 版本是否为 3.6.1
- 验证 `laike-root` JAR 已安装到本地仓库
- 执行 `mvn clean` 并重新构建

### 配置未生效

- 切换 Maven profile 后执行 `mvn clean`
- 检查 Nacos 命名空间是否与选中的 profile 匹配

## 代码规范

- 包命名：`com.laiketui.{module}.{package}`
- 类命名：`PascalCase`
- 方法/字段：`camelCase`
- 常量：`UPPER_SNAKE_CASE`
- 文件编码：UTF-8
- 缩进：4 个空格

## 许可证

本项目为商业软件，版权归来客所有。未经授权不得用于商业用途。

## 联系方式

如有问题或建议，请联系项目维护团队。
