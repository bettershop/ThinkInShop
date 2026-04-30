---
name: "thinkinshop-deploy"
description: "Quickly build and deploy thinkinshop (Java Spring Boot microservice e-commerce platform) to production. Invoke when user asks to deploy, build, package, or launch thinkinshop, or needs to set up Nacos/Nginx/K8s for thinkinshop."
---

# thinkinshop 快速部署 Skill

本 skill 用于 AI Agent 快速理解 thinkinshop 项目结构并完成部署操作。

---

## 1. 项目概览

- **类型**：Java 8 Spring Boot 2.3 + Spring Cloud Hoxton.SR12 + Dubbo 3.0.8 微服务电商平台
- **构建工具**：Maven 3.6+
- **注册/配置中心**：Nacos 2.1.1
- **数据库**：MySQL 8.0
- **缓存**：Redis 7

**关键模块（需要部署的 5 个服务）**：

| 模块 | 说明 | 默认端口 |
|------|------|----------|
| `laike-admins` | PC 管理后台 | 动态 |
| `laike-apps` | 买家端 | 动态 |
| `laike-comps` | 公共组件（内含网关，必须） | 动态 |
| `laike-plugins` | 营销插件接口 | 动态 |
| `laike-apis` | **统一入口模块（单体部署只需这个）** | 21898 |

## 2. 构建命令速查

```bash
# 全量构建（跳过测试）
mvn clean install -DskipTests

# 全量打包
mvn clean package -DskipTests

# 只构建某个模块 + 依赖
mvn clean package -pl laike-apps -am -DskipTests

# 单体部署打包（只需 laike-apis）
mvn clean package -pl laike-apis -am -DskipTests

# 本地开发启动
mvn -pl laike-root spring-boot:run -Dspring-boot.run.profiles=dev

# 运行测试
mvn test -DskipTests=false
```

## 3. 前置环境

| 依赖 | 版本 | 检查命令 |
|------|------|----------|
| JDK | 1.8.211 | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| MySQL | 5.6-8.0 | `mysql --version` |
| Redis | 7 | `redis-cli ping` |
| Nacos | 2.1.1 | `curl http://localhost:8848/nacos/` |

## 4. 环境检查脚本

项目内置环境检测脚本，部署前自动检查：

```bash
# Linux/Mac
bash docs/env-checker/env_linux_mac.sh

# Windows
powershell -File docs/env-checker/env_windows_powershell.ps1
```

## 5. 基础设施启动

### Nacos 启动

```bash
cd docs/soft/nacos2.1.1/bin
# Linux/Mac
sh startup.sh -m standalone
# Windows
startup.cmd -m standalone
```

Nacos 管理界面：`http://localhost:8848/nacos`

默认鉴权已关闭（`nacos.core.auth.enabled=false`）。

### MySQL 初始化

1. 创建数据库 `lkt`、`nacos_db`、`xxl_job`
2. 导入表结构：
   - `db_logs/all-sql/20231103/lkt.sql` → `lkt` 库
   - `db_logs/all-sql/20231103/xxl_job.sql` → `xxl_job` 库
   - `docs/soft/nacos2.1.1/conf/nacos-mysql-default.sql` → `nacos_db` 库
3. 按时间升序执行 `db_logs/incremental-sql/` 下的增量 SQL

```bash
# 示例
mysql -u root -p lkt < db_logs/all-sql/20231103/lkt.sql
```

### Redis 启动

```bash
redis-server  # 默认 6379 端口
```

## 6. 部署模式

### 模式A：单体部署（推荐，laike-apis 统一入口）

`laike-apis` 已聚合 `laike-admins`、`laike-apps`、`laike-comps`、`laike-plugins`，只需部署一个 JAR。

```bash
cd /path/to/thinkinshop

# 1. 构建
mvn clean package -pl laike-apis -am -DskipTests

# 2. 产物在 output/ 目录
ls output/*.jar

# 3. 部署到服务器
mkdir -p /opt/laike/apis/{app,config,logs,upload,xxl-logs}
cp output/laike-apis-0.0.1-SNAPSHOT.jar /opt/laike/apis/app/

# 4. 启动（dev profile）
java -jar /opt/laike/apis/app/laike-apis-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=dev \
  --server.port=21898 \
  > /opt/laike/apis/logs/stdout.log 2>&1 &

# 5. 验证
curl http://localhost:21898/apis/actuator/health
```

### 模式B：多服务部署（Tomcat + JAR）

5 个服务分别部署到 Tomcat：

```bash
# 1. 确认 Nacos 中每个模块端口与 Tomcat 端口一致
#    laike-admins, laike-apps, laike-comps, laike-plugins → 端口 18001

# 2. 分别打包各模块（war 包放入 Tomcat webapps）
mvn clean package -pl laike-admins -am -DskipTests
mvn clean package -pl laike-apps -am -DskipTests
# ... 依此类推

# 3. 网关独立 JAR 部署（非必须）
mvn clean package -pl laike-cloud-gateway -am -DskipTests
```

### 模式C：K8s 部署

部署文件位于 `docs/deploy/`：

```bash
# 单体 K8s
kubectl apply -f docs/deploy/monolith/k8s/namespace.yaml
kubectl apply -f docs/deploy/monolith/k8s/

# 集群 K8s
kubectl apply -f docs/deploy/cluster/k8s/namespace.yaml
kubectl apply -f docs/deploy/cluster/k8s/
```

### 模式D：Docker Compose

```bash
# 单体 Docker Compose
docker-compose -f docs/deploy/monolith/docker-compose.single.yml up -d
```

也可以用根目录的 `docker-compose.yml`。

## 7. 配置要点

### pom.xml 中 Nacos 地址

根 `pom.xml` 的 `properties` 中包含：
```xml
<env.nacos.address>localhost:8848</env.nacos.address>
```

构建时可通过 Maven profile 或 `-D` 覆盖：
```bash
mvn clean package -pl laike-apis -am -DskipTests \
  -Denv.nacos.address=your-nacos-host:8848
```

### Nacos 配置命名空间

各环境使用不同的 Nacos namespace（tenant）：
- **dev**: `da8f7d37-960a-4c94-9691-9c77711d995c`
- **test**: `a8d5eeb0-856f-4cdb-afe3-0fbd8d5bb597`
- **prod**: `23467031-bda4-48a0-a7ab-6bc1a4be22fd`

对应的配置文件在 `docs/soft/nacos2.1.1/data/tenant-config-data/` 中已有模板。

## 8. 多语言 Nacos 配置处理

thinkinshop 支持多语言，Nacos 配置按 `laike-` 前缀管理：

| 配置文件 | 说明 |
|----------|------|
| `laike-common.yml` | 公共配置 |
| `laike-plugins-common.yml` | 插件公共配置 |
| `laike-mybatis.yml` | MyBatis 配置 |
| `laike-plugins-mybatis.yml` | 插件 MyBatis 配置 |
| `comm-prod.yml` | 生产环境通用配置 |

每个配置按语言分成多个 data-id（由 `*LangConfiguration` 类自动加载）。

## 9. 定时任务（XXL-JOB）

```bash
# 启动 XXL-JOB 管理端
nohup java -jar xxl-job-admin-2.3.0-SNAPSHOT.jar &

# 访问界面
# http://your-host:3366/task
```

数据库初始化：`db_logs/all-sql/20231103/xxl_job.sql`

## 10. 校验清单

部署完成后按顺序验证：

```bash
# 1. Nacos 在线
curl http://localhost:8848/nacos/

# 2. 应用健康检查
curl http://localhost:21898/apis/actuator/health

# 3. 检查 Dubbo 服务注册（Nacos 控制台 → 服务管理 → 服务列表）

# 4. 检查应用日志无异常
tail -f /opt/laike/apis/logs/*.log
```

## 11. 常见问题

| 问题 | 排查方向 |
|------|----------|
| `Cannot find module './views/xxx.vue'` | 前端 `loadView()` 缺 `/index.vue` 回退；确认 `getRoutes.js` / `permission.js` 已包含回退逻辑 |
| `Unknown column 'xxx'` (1054) | DB 缺字段；执行对应 `db_logs/incremental-sql/` 增量 SQL；或 PHP/Java 接口做列存在性兼容 |
| Dubbo 服务找不到 (404) | 检查 Nacos 中该模块端口配置是否与 Tomcat 端口一致 |
| Nacos 连不上 | 确认 `pom.xml` 中 `<env.nacos.address>` 正确；确认 Nacos standalone 模式已启动 |
| 端口占用 | `lsof -i :PORT` 查占用；修改对应 `bootstrap.yml` 或启动参数 |

## 12. 关键路径速查

| 用途 | 路径 |
|------|------|
| 根 pom | `pom.xml` |
| Nacos 安装包 | `docs/soft/nacos2.1.1/` |
| 数据库脚本 | `db_logs/all-sql/` + `db_logs/incremental-sql/` |
| 单体部署文档 | `docs/11、单体部署篇（laike-apis）.md` |
| 集群部署文档 | `docs/13、集群部署篇...` + `docs/14、集群架构说明篇...` |
| K8s 部署文件 | `docs/deploy/monolith/k8s/` 或 `docs/deploy/cluster/k8s/` |
| Docker 文件 | `docs/deploy/monolith/` + 根目录 `Dockerfile` / `docker-compose.yml` |
| 环境检测脚本 | `docs/env-checker/` |
| 生产检查清单 | `docs/16、生产上线检查清单（集群）.md` |
| AGENTS.md | 根目录 `AGENTS.md`（含构建/测试/代码风格指南） |
