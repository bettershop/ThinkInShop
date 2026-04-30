# LaiKeJavaViews (来客电商管理后台)

基于 vue-admin-template (PanJiaChen) 二次开发的电商管理后台前端项目。Vue 2 + Element UI + iView。

## 快速开始

```bash
# 第一步：一键安装
bash skill/setup.sh

# 第二步：配置后端 API（连接 Java/PHP 本地后端）
bash skill/config.sh

# 第三步：启动
npm run dev
# 浏览器访问 http://localhost:9528
```

> `skill/config.sh` 会生成 `.env.local`，用于覆盖 `.env.development` 中的 `VUE_APP_BASE_API`。Vue CLI 原生支持 `.env.local`，无需改动现有文件。

## 可用脚本

| 命令 | 用途 |
|------|------|
| `npm run dev` | 本地开发 |
| `npm run java:dev` | Java 开发环境构建 |
| `npm run java:prod` | Java 生产环境构建 |
| `npm run java:test` | Java 测试环境构建 |
| `npm run php:dev` | PHP 开发环境构建 |
| `npm run php:prod` | PHP 生产环境构建 |
| `npm run php:test` | PHP 测试环境构建 |
| `npm run lint` | ESLint 检查 |

## 项目结构

- `src/api/` — API 接口层，分模块 (goods/order/finance/members/Platform/authority)
- `src/views/` — 页面视图
- `src/components/` — 全局 lkt 组件 (l-table, l-dialog, l-input, l-select 等)
- `src/packages/` — 组件库 (lUpload, LButton, LInput 等)
- `src/store/` — Vuex 状态管理
- `src/router/` — 路由定义
- `src/permission.js` — 路由守卫 + 动态路由加载（核心）

## 开发规范

1. **API 请求**: 使用 `src/api/https.js` 导出的 request 实例，参数通过 `params` 字段传递，`api` 字段指定后端接口名
2. **消息提示**: 使用 `this.Message('内容')`，不要直接用 `this.$message()`
3. **自定义组件**: 使用 `l-` 或 `lkt-` 前缀的组件（在 main.js 中全局注册）
4. **权限**: 动态路由从后端获取，路由守卫在 permission.js
5. **环境变量**: 编辑对应 `.env.xxx` 文件中的 `VUE_APP_PROXY_API`、`VUE_APP_STORE`、`PUBLIC_PATH`

## 新增页面流程

1. 在 `src/views/` 下创建对应目录和 `.vue` 文件
2. 在 `src/api/` 下添加 API 模块
3. 路由通常由后端动态返回，如需固定路由在 `src/router/index.js` 的 `constantRoutes` 中添加

## 默认账号

- admin / 123654 或 admin / 000000
