# 单体部署篇（laike-apis）

## 1. 文档目标

本文档用于指导 `thinkinshop` 项目按“单体模式”部署：

1. 只部署一个服务：`laike-apis`。
2. 打包形态支持 `jar` 和 `war`，默认 `jar`。
3. 覆盖从本地打包到线上部署全流程。
4. 提供 Docker 与 Kubernetes（K8s）可直接落地的示例。

## 2. 单体部署边界与说明

### 2.1 单体模式定义

在当前仓库中，`laike-apis` 作为统一入口模块，已经聚合依赖了以下业务模块：

- `laike-admins`
- `laike-apps`
- `laike-comps`
- `laike-plugins`

因此在“单体部署”场景，最终只需要启动 `laike-apis` 一个进程（或一个容器）。

### 2.2 当前默认配置（基于仓库代码）

- 打包类型：`laike-apis/pom.xml` 默认 `packaging=jar`
- 启动主类：`com.laiketui.apis.LaikeApisApplication`
- 默认端口：`21898`
- 默认上下文路径：`/apis`
- 默认健康检查地址：`/apis/actuator/health`

## 3. 环境准备

### 3.1 最小软件要求

- JDK：`1.8`（建议 `1.8.211+`）
- Maven：`3.6+`
- MySQL：`5.6 ~ 8.0.x`
- Redis：`6/7`
- Docker（可选）：`20+`
- Kubernetes（可选）：`1.22+`

### 3.2 基础连通性检查

上线前请确认：

1. 应用节点可连接 MySQL 与 Redis。
2. 对象存储（如 MinIO）与支付证书路径可访问。
3. 防火墙/安全组已放行业务端口（示例 `21898`）。

## 4. 本地打包（默认 jar）

### 4.1 推荐打包命令

在项目根目录执行：

```bash
mvn clean package -pl laike-apis -am -DskipTests
```

说明：

- `-pl laike-apis -am` 会构建 `laike-apis` 及其依赖模块。
- 根 `pom.xml` 默认 `skipTests=true`，此处显式传参便于统一脚本风格。

### 4.2 构建产物位置

`laike-apis` 的 Spring Boot 重打包输出目录已配置为根目录 `output/`。

常见产物：

```bash
ls -lh output/*.jar
```

默认文件名通常为：

- `output/laike-apis-0.0.1-SNAPSHOT.jar`

## 5. 服务器直接部署（jar）

> 适合裸机/云主机，不依赖 Docker。

### 5.1 目录规划

```bash
mkdir -p /opt/laike/apis/{app,config,logs,upload,xxl-logs}
```

目录建议：

- `app/`：放 jar 包
- `config/`：放外置配置
- `logs/`：应用日志
- `upload/`：上传文件目录
- `xxl-logs/`：任务执行日志

### 5.2 上传 jar

```bash
cp output/laike-apis-0.0.1-SNAPSHOT.jar /opt/laike/apis/app/laike-apis.jar
```

### 5.3 外置配置（推荐）

复制一份基础配置后再改生产参数：

```bash
cp laike-apis/src/main/resources/bootstrap.yml /opt/laike/apis/config/bootstrap.yml
```

必须修改项（至少）：

1. `spring.redis.host/port/password`
2. `spring.shardingsphere.datasource.lkt-ds-master-0.url/username/password`
3. `uploadFile.path`（改为线上目录，如 `/opt/laike/apis/upload/`）
4. `node.wx-certp12-path`（改为线上证书目录）
5. `xxl.job.admin.addresses`（如使用任务中心）

### 5.4 启动命令

```bash
nohup java -Xms1g -Xmx1g -XX:+UseG1GC \
  -Dfile.encoding=UTF-8 \
  -jar /opt/laike/apis/app/laike-apis.jar \
  --spring.config.additional-location=file:/opt/laike/apis/config/bootstrap.yml \
  --logging.file.path=/opt/laike/apis/logs \
  --uploadFile.path=/opt/laike/apis/upload/ \
  --xxl.job.executor.logpath=/opt/laike/apis/xxl-logs \
  > /opt/laike/apis/logs/console.log 2>&1 &
```

### 5.5 启动后验证

```bash
curl -f http://127.0.0.1:21898/apis/actuator/health
```

```bash
curl -I http://127.0.0.1:21898/apis
```

如果通了那么java对应网关地址就是 ：http://127.0.0.1:21898/apis/gw

### 5.6 systemd 守护（推荐）

新建 `/etc/systemd/system/laike-apis.service`：

```ini
[Unit]
Description=Laike APIs Monolith Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/laike/apis
Environment="JAVA_OPTS=-Xms1g -Xmx1g -XX:+UseG1GC -Dfile.encoding=UTF-8"
ExecStart=/bin/bash -lc 'java $JAVA_OPTS -jar /opt/laike/apis/app/laike-apis.jar --spring.config.additional-location=file:/opt/laike/apis/config/bootstrap.yml --logging.file.path=/opt/laike/apis/logs --uploadFile.path=/opt/laike/apis/upload/ --xxl.job.executor.logpath=/opt/laike/apis/xxl-logs'
SuccessExitStatus=143
Restart=always
RestartSec=5
LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
```

生效并启动：

```bash
systemctl daemon-reload
systemctl enable laike-apis
systemctl restart laike-apis
systemctl status laike-apis
```

## 6. war 打包与部署（可选）

> 默认推荐 `jar`。如果你必须接入已有 Tomcat 集群，可使用 `war`。

### 6.1 构建前调整

修改 `laike-apis/pom.xml`：

1. 将 `<packaging>jar</packaging>` 改为 `<packaging>war</packaging>`。
2. 增加 `spring-boot-starter-tomcat`，并设置 `scope=provided`。

示例依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

### 6.2 打包命令

```bash
mvn clean package -pl laike-apis -am -DskipTests
```

产物示例：

- `output/laike-apis-0.0.1-SNAPSHOT.war`

### 6.3 部署到外部 Tomcat

```bash
cp output/laike-apis-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/apis.war
```

Tomcat 启动参数建议补充：

```bash
-Dspring.config.additional-location=file:/opt/laike/apis/config/bootstrap.yml
-Dlogging.file.path=/opt/laike/apis/logs
```

## 7. Docker 单体部署

已提供模板文件：

- `docs/deploy/monolith/Dockerfile.jar`
- `docs/deploy/monolith/Dockerfile.war`
- `docs/deploy/monolith/docker-compose.single.yml`

### 7.1 构建 jar 镜像

```bash
docker build \
  -f docs/deploy/monolith/Dockerfile.jar \
  -t registry.example.com/laike/laike-apis:0.0.1 \
  --build-arg JAR_FILE=output/laike-apis-0.0.1-SNAPSHOT.jar \
  .
```

### 7.2 运行容器

```bash
docker run -d --name laike-apis \
  -p 21898:21898 \
  -e JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseG1GC -Dfile.encoding=UTF-8" \
  -v /opt/laike/apis/config:/opt/laike/apis/config:ro \
  -v /opt/laike/apis/logs:/opt/laike/apis/logs \
  -v /opt/laike/apis/upload:/opt/laike/apis/upload \
  -v /opt/laike/apis/xxl-logs:/opt/laike/apis/xxl-logs \
  --restart always \
  registry.example.com/laike/laike-apis:0.0.1
```

### 7.3 Docker Compose 部署

```bash
cd docs/deploy/monolith
docker compose -f docker-compose.single.yml up -d
```

## 8. Kubernetes 单体部署

已提供模板文件目录：`docs/deploy/monolith/k8s/`

- `namespace.yaml`
- `configmap.yaml`
- `secret.example.yaml`
- `pvc.yaml`
- `deployment.yaml`
- `service.yaml`
- `ingress.yaml`
- `hpa.yaml`

### 8.1 发布前替换项

必须替换：

1. 镜像地址：`registry.example.com/laike/laike-apis:0.0.1`
2. 域名：`laike-api.example.com`
3. Secret 中数据库/Redis 密码
4. 存储类名称（如果使用 PVC）

### 8.2 部署顺序

```bash
kubectl apply -f docs/deploy/monolith/k8s/namespace.yaml
kubectl apply -f docs/deploy/monolith/k8s/secret.example.yaml
kubectl apply -f docs/deploy/monolith/k8s/configmap.yaml
kubectl apply -f docs/deploy/monolith/k8s/pvc.yaml
kubectl apply -f docs/deploy/monolith/k8s/deployment.yaml
kubectl apply -f docs/deploy/monolith/k8s/service.yaml
kubectl apply -f docs/deploy/monolith/k8s/ingress.yaml
kubectl apply -f docs/deploy/monolith/k8s/hpa.yaml
```

### 8.3 发布验证

```bash
kubectl -n laike-prod get pod,svc,ingress
kubectl -n laike-prod rollout status deploy/laike-apis
kubectl -n laike-prod logs -f deploy/laike-apis
```

### 8.4 回滚

```bash
kubectl -n laike-prod rollout history deploy/laike-apis
kubectl -n laike-prod rollout undo deploy/laike-apis --to-revision=2
```

## 9. 标准上线流程（建议）

1. 开发机执行 `mvn clean package -pl laike-apis -am -DskipTests`。
2. 构建镜像并打不可变标签（例如 `v20260323-01`）。
3. 推送镜像到镜像仓库。
4. 预发环境验证健康检查、核心接口、支付与上传链路。
5. 生产执行滚动发布。
6. 发布后 30 分钟重点观察：错误率、RT、JVM 内存、慢 SQL。

## 10. 运维基线建议

### 10.1 资源建议

- 单实例起步：`2C4G`
- 高峰建议：`4C8G`
- JVM：`-Xms` 与 `-Xmx` 尽量相同，减少堆抖动

### 10.2 日志与监控

- 应用日志挂载到持久化目录
- 暴露 `prometheus` 指标（已在配置中启用）
- 健康探针使用 `/apis/actuator/health`

### 10.3 安全与配置

1. 密码、密钥、证书不入镜像，统一走 Secret/外置挂载。
2. 生产禁止使用仓库里的默认数据库口令与邮箱口令。
3. 开启访问层 HTTPS（Ingress/Nginx/LB）。

## 11. 常见问题排查

### 11.1 启动失败：数据库连接异常

检查项：

1. `bootstrap.yml` 的 JDBC URL/账号/密码。
2. 数据库白名单和防火墙。
3. MySQL `sql_mode` 兼容配置。

### 11.2 启动成功但接口 404

检查项：

1. 请求路径是否包含上下文 `/apis`。
2. 服务端口是否为 `21898`。
3. Ingress 转发路径是否透传到 `/apis`。

### 11.3 上传失败

检查项：

1. `uploadFile.path` 对应目录是否存在且有写权限。
2. 容器是否正确挂载 `upload` 目录。
3. 上传文件大小是否超过配置上限。

---

如果你计划直接上生产，建议下一步在当前仓库里再补一份“生产参数清单模板”（数据库连接池、JVM、限流阈值、日志保留周期），用于每次发布前核对。
