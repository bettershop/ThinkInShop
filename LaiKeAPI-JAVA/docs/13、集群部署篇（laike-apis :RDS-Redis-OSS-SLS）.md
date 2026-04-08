# 集群部署篇（RDS + Redis Cluster + OSS + SLS）

## 1. 目标与范围

本文档用于 `thinkinshop` 的生产集群部署，目标如下：

1. 应用层支持水平扩展（多实例）。
2. 所有实例共享云资源：`MySQL RDS`、`Redis Cluster`、`OSS`、`SLS`。
3. 支持 `ECS + SLB` 和 `Kubernetes` 两种部署模式。

单体入口仍为 `laike-apis`，默认 `jar` 运行。

## 2. 已完成的代码改造（非业务逻辑）

为满足多实例集群，已完成以下代码层调整：

1. `Redisson` 配置改为自动适配 `单机 / Sentinel / Cluster`。
2. IM WebSocket 增加 `Redis Pub/Sub` 集群分发能力，跨节点可投递消息。

涉及文件：

- `laike-common/src/main/java/com/laiketui/common/config/RedissionConfig.java`
- `laike-comps/src/main/java/com/laiketui/comps/im/websocket/CompsWebSocketServer.java`
- `laike-comps/src/main/java/com/laiketui/comps/im/websocket/CompsWebSocketClusterDispatcher.java`
- `laike-comps/src/main/java/com/laiketui/comps/im/websocket/CompsWebSocketRedisConfig.java`
- `laike-comps/src/main/java/com/laiketui/comps/im/api/CompsOnlineMessageServiceImpl.java`

## 3. 目标架构（生产推荐）

```text
[Client/App/H5/PC]
       |
       v
[SLB/Ingress]
       |
       v
[laike-apis Pod/ECS x N]
   |          |          |
   v          v          v
[Redis Cluster] [MySQL RDS] [OSS]
       |
       v
      [SLS]
```

关键点：

1. 应用节点无状态化，随时可扩缩容。
2. 用户会话、锁、限流、WebSocket 分发统一依赖 Redis 集群。
3. 图片文件统一上 OSS，不走本地盘。
4. 日志统一入 SLS，节点日志目录只做短期缓存。

## 4. 云资源准备清单

### 4.1 MySQL RDS

1. 准备主备或高可用版实例。
2. 提供集群连接地址（或代理地址）。
3. 开启慢 SQL 与审计日志。
4. 配置白名单：ECS/VPC/K8s 节点网段。

### 4.2 Redis Cluster

1. 至少 3 主 3 从（生产建议）。
2. 获取节点地址列表（host:port）。
3. 开启密码认证。
4. 确保应用与 Redis 在同 VPC，降低延迟。

### 4.3 OSS

1. 创建 Bucket（按环境隔离，如 `laike-prod`）。
2. 创建最小权限 RAM 子账号（仅该 Bucket 读写）。
3. 应用后台将上传方式设为 `OSS`。

### 4.4 SLS

1. 创建 Project 与 Logstore。
2. ECS 场景安装 Logtail；K8s 场景部署日志采集 DaemonSet。
3. 统一字段：`service=laike-apis`、`env=prod`。

## 5. 集群配置（外置 bootstrap.yml）

建议以外置配置方式运行，示例：

```yaml
server:
  port: 21898
  servlet:
    context-path: /apis

spring:
  application:
    name: laike-apis

  redis:
    # Redis Cluster 推荐写法
    cluster:
      nodes:
        - redis-1.xxx.rds.aliyuncs.com:6379
        - redis-2.xxx.rds.aliyuncs.com:6379
        - redis-3.xxx.rds.aliyuncs.com:6379
    password: "${REDIS_PASSWORD}"
    timeout: 30000
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 5

  shardingsphere:
    datasource:
      names: lkt-ds-master-0
      lkt-ds-master-0:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://rm-xxx.mysql.rds.aliyuncs.com:3306/v3_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&allowMultiQueries=true&rewriteBatchedStatements=true
        username: ${DB_USER}
        password: ${DB_PASSWORD}

uploadFile:
  # 如果已全量切 OSS，此目录只用于临时文件
  path: /opt/laike/apis/upload/
```

说明：

1. Redis 集群模式下，不再依赖 `spring.redis.host/port`。
2. 你们当前 `RedissionConfig` 已支持 cluster/sentinel/single 自动识别。
3. 生产凭据统一从环境变量或 Secret 注入，不写死配置文件。
4. 可直接参考模板：`docs/deploy/cluster/bootstrap.cluster.template.yml`。
5. 生产替换模板：`docs/deploy/cluster/bootstrap.cluster.prod.example.yml`。

## 6. ECS + SLB 部署

### 6.1 打包

```bash
mvn clean package -pl laike-apis -am -DskipTests
```

### 6.2 多 ECS 节点启动

每台 ECS 启动同一个 `laike-apis.jar`：

```bash
nohup java -Xms2g -Xmx2g -XX:+UseG1GC \
  -jar /opt/laike/apis/app/laike-apis.jar \
  --spring.config.additional-location=file:/opt/laike/apis/config/bootstrap.yml \
  --logging.file.path=/opt/laike/apis/logs \
  > /opt/laike/apis/logs/console.log 2>&1 &
```

### 6.3 Nginx（WebSocket 必配）

```nginx
location /apis/onlineMessage/ {
    proxy_pass http://laike_apis_upstream;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_read_timeout 3600s;
}
```

### 6.4 SLB 要点

1. 后端挂所有 ECS 实例。
2. 健康检查：`/apis/actuator/health`。
3. 空闲超时建议 >= `3600s`（WebSocket 长连接）。
4. 是否会话保持：
   - 旧方案通常要求粘性。
   - 当前改造后，WebSocket 消息已支持跨节点分发，粘性非强依赖。

## 7. Kubernetes 集群部署

已提供模板目录：`docs/deploy/cluster/k8s/`

### 7.1 部署步骤

```bash
kubectl apply -f docs/deploy/cluster/k8s/namespace.yaml
kubectl apply -f docs/deploy/cluster/k8s/secret.example.yaml
kubectl apply -f docs/deploy/cluster/k8s/configmap.yaml
kubectl apply -f docs/deploy/cluster/k8s/pvc.yaml
kubectl apply -f docs/deploy/cluster/k8s/deployment.yaml
kubectl apply -f docs/deploy/cluster/k8s/service-loadbalancer.yaml
kubectl apply -f docs/deploy/cluster/k8s/ingress.yaml
kubectl apply -f docs/deploy/cluster/k8s/hpa.yaml
```

### 7.2 发布验证

```bash
kubectl -n laike-prod get pod,svc,hpa
kubectl -n laike-prod rollout status deploy/laike-apis
kubectl -n laike-prod logs -f deploy/laike-apis
```

## 8. OSS 与上传策略

1. 后台系统把上传方式切到 `OSS`。
2. 所有实例共用同一个 Bucket（按租户/店铺目录区分）。
3. 节点本地 `uploadFile.path` 仅保留临时缓存用途。

## 9. SLS 日志接入策略

### 9.1 ECS

1. 采集 `/opt/laike/apis/logs/*.log`。
2. 推荐采集 `console.log + apis.log + apis-error.log`。

### 9.2 K8s

1. 推荐采集容器 `stdout/stderr`（无需节点共享盘）。
2. 通过 Pod Label `app=laike-apis` 做过滤。
3. 采集策略模板见：`docs/deploy/cluster/sls-日志采集说明.md`。

## 10. WebSocket 在集群与 SLB 

### 10.1 改 WebSocket

原因：旧实现只在本机内存连接表推送，无法跨实例投递。

### 10.2 当前状态

改造为：

1. 本机命中连接：直接发送。
2. 本机未命中：发布到 Redis Pub/Sub 频道。
3. 其他节点消费后，向本机连接投递。

因此在 `ECS/SLB` 或 `K8s/Ingress` 多副本下，IM 可正常跨节点工作。

## 11. 水平扩展与回滚

### 11.1 扩容

1. ECS：增加节点并加入 SLB 后端。
2. K8s：提高 Deployment replicas 或调整 HPA。

### 11.2 回滚

1. 镜像版本回滚优先。
2. K8s 使用 `rollout undo`。
3. 数据库变更需先做向前兼容。

## 12. 常见故障排查

### 12.1 多副本下 IM 消息偶发丢失

1. 检查 Redis 集群连接与权限。
2. 检查 `laike:im:websocket:dispatch` 频道是否正常发布/消费。
3. 检查 SLB/Nginx 的 WebSocket Upgrade 与超时配置。

### 12.2 Redis 锁失效或报错

1. 确认使用的是 Redis Cluster 地址，不是单机地址。
2. 校验 `spring.redis.password` 与节点白名单。
3. 查看 `RedissionConfig` 实际加载模式（single/sentinel/cluster）。

### 12.3 日志未进入 SLS

1. ECS：Logtail 路径是否正确。
2. K8s：采集器是否选中 `app=laike-apis`。
3. 应用日志级别是否被过滤（建议 `INFO` 起步）。

## 13. 配套清单

1. 参数收集清单：`docs/15、集群生产参数清单（待填）.md`
2. 上线检查清单：`docs/16、生产上线检查清单（集群）.md`
