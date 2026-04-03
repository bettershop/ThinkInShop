# MCP（Model Context Protocol）原理与使用文档（中文）

## 1. 文档说明

本文档面向 `laike-comps` 模块中的 MCP 实现，重点说明两件事：

1. MCP 在本项目里的实现原理（不是泛化理论）。
2. MCP 的落地使用方式（如何调用、如何排错、如何扩展）。

本文档基于当前代码实现整理：

- `laike-comps/src/main/java/com/laiketui/comps/mcp/McpController.java`
- `laike-comps/src/main/java/com/laiketui/comps/mcp/service/McpAuthService.java`
- `laike-comps/src/main/java/com/laiketui/comps/mcp/service/McpToolService.java`

## 2. MCP 在本项目中的定位

本项目将 MCP 作为“AI 客户端访问电商核心数据”的标准入口，使用 **JSON-RPC 2.0** 报文格式，通过 HTTP 提供统一接口。

- 统一入口：`POST /outapi/mcp`
- 协议风格：MCP + JSON-RPC 2.0
- 当前能力：用户、订单、商品的查询型工具

简化理解：

- AI 客户端只需要对接一个接口地址。
- 通过 `method` 指定操作（如 `tools/list`、`tools/call`）。
- 业务数据查询由 `McpToolService` 转到 Mapper 层执行。

## 3. 整体架构与调用链路

## 3.1 核心组件职责

- `McpController`
  - 解析 JSON-RPC 请求。
  - 按 `method` 路由到具体处理逻辑。
  - 统一封装成功/失败响应。
- `McpAuthService`
  - 负责 `initialize` 阶段的账号密码认证。
  - 认证信息来源当前为 Redis 键值。
- `McpToolService`
  - 承载 `tools/call` 的业务逻辑。
  - 通过 `UserMapper`、`OrderModelMapper`、`ProductListModelMapper` 查询数据库。

## 3.2 请求处理流程（时序）

1. 客户端向 `/outapi/mcp` 发送 JSON-RPC 请求。
2. `McpController` 解析 `jsonrpc/id/method/params`。
3. 根据 `method` 进入对应处理分支：
   - `initialize`
   - `tools/list`
   - `tools/call`
   - `resources/list`
   - `prompts/list`
4. 如果是 `tools/call`，进入 `McpToolService.executeTool()`。
5. `McpToolService` 调用 Mapper 查询并封装业务结果。
6. `McpController` 把结果包装为 JSON-RPC 响应并返回。

## 4. 协议与报文设计

## 4.1 请求格式

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "get_user",
    "arguments": {
      "user_id": "user123",
      "store_id": 1
    }
  }
}
```

字段说明：

- `jsonrpc`：建议固定传 `2.0`。
- `id`：请求标识，用于匹配响应。
- `method`：调用的方法名。
- `params`：方法参数。

## 4.2 响应格式

成功响应：

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "...": "..."
  }
}
```

失败响应：

```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": -32603,
    "message": "Internal error: ..."
  }
}
```

实现细节说明：

- 当前实现里 `id` 在控制器中按字符串处理，所以响应里的 `id` 可能是字符串。
- 控制器异常统一走 `-32603`（Internal error）。
- `tools/call` 的业务错误多数封装在 `result.content[0].text` 内部 JSON 中（`success=false`），不一定走 JSON-RPC `error`。

## 5. 认证原理与配置

## 5.1 认证触发点

认证发生在 `initialize` 的 `params.auth`：

```json
{
  "method": "initialize",
  "params": {
    "auth": {
      "account": "admin",
      "password": "123456"
    }
  }
}
```

## 5.2 认证实现逻辑

`McpAuthService.authenticate(account, password)` 当前流程：

1. 校验账号/密码非空。
2. 从 Redis 读取 `mcp:auth:{account}`。
3. 比较缓存密码是否一致。
4. 一致则认证成功，否则失败。

## 5.3 Redis 键约定

- Key：`mcp:auth:{account}`
- Value：`{password}`
- 典型 TTL：3600 秒（1 小时）

示例（请按你的 Redis 库选择 DB）：

```bash
redis-cli
SET mcp:auth:admin 123456 EX 3600
```

## 5.4 认证现状说明（重要）

- 认证只在 `initialize` 中执行。
- 当前实现没有会话态，不会强制后续 `tools/call` 必须先通过 `initialize`。

这意味着“协议上建议先初始化认证”，但“代码层当前并未对每次工具调用做强制鉴权”。

## 6. 对外方法清单

## 6.1 initialize

用途：握手、返回服务信息与能力声明。

请求示例：

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "auth": {
      "account": "admin",
      "password": "123456"
    }
  }
}
```

响应示例：

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "serverInfo": {
      "name": "LaikeShop MCP Server",
      "version": "1.0.0"
    },
    "capabilities": {
      "tools": {},
      "resources": {},
      "prompts": {}
    }
  }
}
```

## 6.2 tools/list

用途：获取可用工具列表。

## 6.3 tools/call

用途：执行指定工具。

必要参数：

- `params.name`：工具名
- `params.arguments`：工具参数对象（可空）

返回格式示例：

```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\"success\":true,\"data\":{...}}"
      }
    ]
  }
}
```

注意：`text` 是一个字符串化 JSON。

## 6.4 resources/list

用途：返回静态资源清单。

当前资源：

- `mcp://laike/user`
- `mcp://laike/order`
- `mcp://laike/product`

## 6.5 prompts/list

用途：返回静态 prompt 清单。

当前 prompts：

- `analyze_user_data`
- `analyze_order_data`
- `analyze_product_data`

## 7. 工具能力与参数说明

## 7.1 get_user

用途：按 `user_id` 查询单个用户。

参数：

- `user_id`：必填
- `store_id`：选填

错误示例：

- `user_id is required`
- `User not found`

## 7.2 list_users

用途：分页查询用户列表。

参数：

- `store_id`：选填
- `page`：选填，默认 `1`
- `page_size`：选填，默认 `10`

分页实现：

- 使用 MyBatis `RowBounds`。
- 偏移量计算：`(page - 1) * page_size`。
- `total` 字段为“本次返回列表大小”，不是全量总条数。

## 7.3 get_order

用途：按 `order_no` 查询单个订单。

参数：

- `order_no`：必填
- `store_id`：选填

错误示例：

- `order_no is required`
- `Order not found`

## 7.4 list_orders

用途：分页查询订单列表。

参数：

- `store_id`：选填
- `page`：选填，默认 `1`
- `page_size`：选填，默认 `10`
- `status`：选填（可解析为整数才会生效）
- `user_id`：选填

## 7.5 get_product

用途：按 `product_id` 查询单个商品。

参数：

- `product_id`：必填
- `store_id`：选填

错误示例：

- `product_id is required`
- `Product not found`

## 7.6 list_products

用途：分页查询商品列表。

参数：

- `store_id`：选填
- `page`：选填，默认 `1`
- `page_size`：选填，默认 `10`
- `status`：选填

## 8. 快速使用（可直接复制）

下面以本地服务 `http://127.0.0.1:8080/outapi/mcp` 为例。

## 8.1 初始化认证

```bash
curl -X POST 'http://127.0.0.1:8080/outapi/mcp' \
  -H 'Content-Type: application/json' \
  -d '{
    "jsonrpc":"2.0",
    "id":1,
    "method":"initialize",
    "params":{
      "auth":{
        "account":"admin",
        "password":"123456"
      }
    }
  }'
```

## 8.2 查看工具列表

```bash
curl -X POST 'http://127.0.0.1:8080/outapi/mcp' \
  -H 'Content-Type: application/json' \
  -d '{
    "jsonrpc":"2.0",
    "id":2,
    "method":"tools/list"
  }'
```

## 8.3 调用 get_user

```bash
curl -X POST 'http://127.0.0.1:8080/outapi/mcp' \
  -H 'Content-Type: application/json' \
  -d '{
    "jsonrpc":"2.0",
    "id":3,
    "method":"tools/call",
    "params":{
      "name":"get_user",
      "arguments":{
        "user_id":"user123",
        "store_id":1
      }
    }
  }'
```

## 8.4 调用 list_orders

```bash
curl -X POST 'http://127.0.0.1:8080/outapi/mcp' \
  -H 'Content-Type: application/json' \
  -d '{
    "jsonrpc":"2.0",
    "id":4,
    "method":"tools/call",
    "params":{
      "name":"list_orders",
      "arguments":{
        "store_id":1,
        "page":1,
        "page_size":10,
        "status":"1"
      }
    }
  }'
```

## 9. 错误处理与排查

## 9.1 两层错误模型

第一层：JSON-RPC 层错误（控制器异常）

- 统一返回 `error.code = -32603`

第二层：业务层错误（工具内部）

- 返回在 `result.content[0].text` 中
- 结构通常是：`{"success":false,"error":"..."}`

## 9.2 常见问题

1. 报 `Authentication failed`
- 检查 Redis 中是否存在 `mcp:auth:{account}`。
- 检查密码和 TTL 是否过期。

2. 报 `Tool name is required`
- `tools/call` 必须传 `params.name`。

3. 请求成功但数据为空
- 核对 `store_id`、`user_id`、`order_no`、`product_id` 是否匹配数据。

4. `total` 与预期总数不一致
- 当前 `total` 仅表示“当前页返回条数”，不是全表计数。

## 10. 扩展开发指南

## 10.1 新增一个工具

1. 在 `McpController` 的 `TOOLS` 常量中增加工具声明。
2. 在 `McpToolService.executeTool()` 的 `switch` 中增加分支。
3. 实现对应私有方法（参数校验、Mapper 查询、返回封装）。
4. 更新本文档中的工具章节与示例。

## 10.2 建议的返回规范

建议保持统一结构：

- 成功：`{"success":true,"data":...}`
- 失败：`{"success":false,"error":"..."}`

这样 AI 客户端解析更稳定。

## 11. 现状限制与优化建议

当前实现可用，但存在以下工程化优化空间：

1. `initialize` 鉴权未对后续请求形成强约束。
2. `tools/call` 返回 `text` 内嵌 JSON 字符串，客户端需二次解析。
3. 列表接口 `total` 非全量统计。
4. `resources/list`、`prompts/list` 目前为静态清单。
5. 控制器异常统一映射为 `-32603`，错误码粒度可继续细化。

## 12. 目录结构

```text
laike-comps/src/main/java/com/laiketui/comps/mcp/
├── McpController.java
├── dto/
│   ├── McpRequest.java
│   ├── McpResponse.java
│   └── PromptMessage.java
└── service/
    ├── McpAuthService.java
    └── McpToolService.java
```

---

作者：wangxian  
文档更新：2026-03-18
