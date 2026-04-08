# IM 在线消息模块使用手册（本地联调）

## 1. 适用范围

本文档适用于 `laike-comps/src/main/java/com/laiketui/comps/im` 在线消息模块，包含：

- HTTP 接口调用（查询会话、发消息）
- WebSocket 实时收发
- 前端本地开发联调方式

---

## 2. 本地访问地址

当前工程 `laike-apis` 默认配置为：

- 服务地址：`http://127.0.0.1:21898`
- 上下文：`/apis`

所以 IM 模块本地基地址为：

- `http://127.0.0.1:21898/apis`

建议前端本地代理：

```ts
// vite.config.ts
export default {
  server: {
    proxy: {
      "/apis": {
        target: "http://127.0.0.1:21898",
        changeOrigin: true
      }
    }
  }
}
```

---

## 3. 鉴权与公共参数

在线消息接口在服务层会校验登录态，必须传 token：

- 参数名：`access_id`（推荐）
- 也兼容映射到 `accessId`

常用公共参数：

- `storeId`：商城 ID（必传，通常为 `1`）
- `store_type`：端类型（可选，默认 `1`）
- `access_id`：登录 token（必传）

请求方式建议：

- `POST`
- `Content-Type: application/x-www-form-urlencoded`

---

## 4. HTTP 接口清单

控制器前缀：`/comps/onlineMessage`

### 4.1 查询用户与店铺消息

- URL：`POST /apis/comps/onlineMessage/getMessageList`
- 业务 key（网关调用时）：`app.msg.getMessageList` / `mall.Msg.getMessageList` / `mch.Mch.Msg.getMessageList`
- 主要参数：
  - `access_id`
  - `storeId`
  - `userId`：用户ID
  - `mchId`：店铺ID
  - `type`：发送方类型，`0` 用户发送，`1` 店铺发送

示例：

```bash
curl -X POST 'http://127.0.0.1:21898/apis/comps/onlineMessage/getMessageList' \
  -d 'storeId=1' \
  -d 'access_id=你的token' \
  -d 'userId=user1001' \
  -d 'mchId=2001' \
  -d 'type=0'
```

### 4.2 发送消息

- URL：`POST /apis/comps/onlineMessage/addMessage`
- 业务 key：`app.msg.addMessage` / `mall.Msg.addMessage` / `mch.Mch.Msg.addMessage`
- 主要参数：
  - `access_id`
  - `storeId`
  - `send_id`：发送方 ID
  - `receive_id`：接收方 ID
  - `is_mch_send`：`0` 用户发送，`1` 店铺发送
  - `content`：文本消息（发送图片时可不填）
  - `img_list`：图片列表（可选，支持批量）
  - `orderId`：订单卡片消息（可选）
  - `pId`：商品卡片消息（可选）

示例（文本）：

```bash
curl -X POST 'http://127.0.0.1:21898/apis/comps/onlineMessage/addMessage' \
  -d 'storeId=1' \
  -d 'access_id=你的token' \
  -d 'send_id=user1001' \
  -d 'receive_id=2001' \
  -d 'is_mch_send=0' \
  -d 'content=你好，我想咨询订单'
```

### 4.3 查询店铺会话的用户列表

- URL：`POST /apis/comps/onlineMessage/mch_userList`
- 业务 key：`app.msg.mch_userList` / `mch.Mch.Msg.mch_userList`
- 参数：
  - `access_id`
  - `storeId`
  - `mchId`
  - `userName`（可选）

### 4.4 查询用户会话的店铺列表

- URL：`POST /apis/comps/onlineMessage/user_mchList`
- 业务 key：`app.msg.user_mchList` / `mall.Msg.user_mchList`
- 参数：
  - `access_id`
  - `storeId`
  - `userId`
  - `mchId`（可选，用于置顶当前店铺）
  - `mchName`（可选）

### 4.5 客服页订单列表

- URL：`POST /apis/comps/onlineMessage/customerOrderIndex`
- 业务 key：`app.customer.orderIndex`

---

## 5. WebSocket 实时通信

### 5.1 连接地址

- `ws://127.0.0.1:21898/apis/onlineMessage/{sendId}/{receiveId}/{storeType}`

其中 `storeType` 为来源编码：

- `1`：小程序
- `11`：APP
- `6`：PC商城
- `2`：H5
- `7`：PC店铺

### 5.2 连接 key 规则（重要）

服务端内部连接 key 规则：

- `{终端前缀}{sendId},{receiveId}`

消息推送时会发到以下两种 key：

- 商家通道：`{终端前缀}{mchId},{mchId}`
- 用户通道：`{终端前缀}{userId},{userId}`

因此前端监听消息时，建议按“自己ID,自己ID”建立连接。

### 5.3 前端示例

```js
const ws = new WebSocket("ws://127.0.0.1:21898/apis/onlineMessage/user1001/user1001/2");

ws.onopen = () => {
  // 心跳建议每 20~25 秒发一次
  setInterval(() => ws.send("ping"), 25000);
};

ws.onmessage = (e) => {
  if (e.data === "pong") return;
  const msg = JSON.parse(e.data);
  console.log("收到消息:", msg);
};
```

---

## 6. 网关模式调用（可选）

除直连 Controller 外，也可通过网关入口：

- `POST /apis/gw`

示例参数：

- `api=app.msg.getMessageList`
- 其余业务参数同上（`access_id/storeId/userId/mchId/type`）

本地调试建议优先直连 `/comps/onlineMessage/*`，问题定位更直接。

---

## 7. 常见问题

### 7.1 返回“未登录/登录过期”

- 检查 `access_id` 是否有效
- 确认 token 对应 Redis 登录态存在

### 7.2 WebSocket 连上但收不到消息

- 核对连接 URL 的 `storeType` 是否正确
- 核对是否使用了 `sendId=自己ID, receiveId=自己ID` 的监听方式
- 发送端 `send_id/receive_id/is_mch_send` 是否与实际身份一致

### 7.3 跨域报错

模块已放开 CORS（`*`），若仍报错，优先检查前端代理配置和请求地址是否包含 `/apis` 前缀。

### 7.4 集群与 SLB 下的 WebSocket 说明

- 已支持多实例集群：本机未命中连接时，会通过 Redis Pub/Sub 跨节点分发。
- SLB/Nginx 必须开启 `Upgrade/Connection` 透传并放大读超时（建议 `>=3600s`）。
- 生产建议使用 `wss://`，并保留前端心跳（20~25 秒一次）。
