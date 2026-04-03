# SLS 日志采集说明（laike-apis）

## 1. 推荐采集策略

- ECS：采集应用日志文件（`/opt/laike/apis/logs/*.log`）。
- K8s：采集容器标准输出（stdout/stderr），按 `app=laike-apis` 过滤。

## 2. ECS 采集建议

1. 安装 Logtail Agent。
2. 采集路径：
   - `/opt/laike/apis/logs/apis.log`
   - `/opt/laike/apis/logs/apis-error.log`
   - `/opt/laike/apis/logs/console.log`
3. 标签建议：`service=laike-apis, env=prod`。

## 3. K8s 采集建议

1. 在 ACK 集群部署日志采集 DaemonSet。
2. 采集规则仅匹配 namespace=`laike-prod` 且 label `app=laike-apis`。
3. 日志平台字段建议：
   - `service`
   - `pod`
   - `namespace`
   - `traceId`（如后续接入链路追踪）

## 4. 告警建议

1. ERROR 日志 5 分钟增量异常告警。
2. 关键关键字告警：`WebSocket消息发送失败`、`Redis`、`SQL`、`OOM`。
3. 接口错误率告警与日志告警联动。
