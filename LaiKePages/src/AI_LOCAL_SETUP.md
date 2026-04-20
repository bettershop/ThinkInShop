# AI 本地安装部署指南（LaiKePages -> laike-pages）

本文档用于指导 AI 或开发者在本地拉取代码后，快速完成移动端项目启动。

## 1. 启动前必改配置

在运行前，先修改：

- `LaiKePages/components/laiketuiCommon.js`

定位并修改 `LKT_ROOT_URL`，改为你本地后端网关地址（PHP 或 Java）：

```js
// 例：Java 网关 参考Java接口部署文档 参考文档： LaiKeAPI-JAVA/docs/11、单体部署篇（laike-apis）.md ，端口和路径可能不同，请根据实际情况修改
const LKT_ROOT_URL = 'http://127.0.0.1:21898/apis'

// 例：PHP 网关：网关地址可能是你本地环境的地址，端口和路径可能不同，请根据实际情况修改 具体php接口部署文档  参考文档： LaiKeAPI/README.md
// const LKT_ROOT_URL = 'http://127.0.0.1:18001' 
```

注意：
- 只改 `LKT_ROOT_URL` 的值，不要改变量名。
- 请确保后端网关可访问，否则前端会出现接口请求失败。

## 2. 同步 LaiKePages 到 laike-pages/src

配置改完后，把 `LaiKePages` 下文件同步到 `laike-pages/src`。

推荐命令（会覆盖旧文件）：

```bash
cd /你的代码根目录
rsync -av --delete \
  --exclude '.svn' \
  --exclude 'node_modules' \
  --exclude 'dist' \
  --exclude '.idea' \
  --exclude '.DS_Store' \
  LaiKePages/ laike-pages/src/
```

## 3. 按顺序执行启动命令

```bash
# 然后命令行切到 laike-pages/src 目录执行以下两个命令 
npm i
npm run build:packages

# 然后命令行切回 laike-pages 目录执行以下命令,打包
npm run build:h5

# 然后命令行切回 laike-pages 目录执行以下命令,本地运行
npm run serve
```

说明：
- 按上述顺序执行（团队约定流程）。
- 如首次环境执行 `build:packages` 提示依赖缺失，可先执行一次 `npm install` 后再重跑。

## 4. 启动成功判断

- 终端出现类似 `App running at:` 与本地访问地址（如 `http://localhost:8080`）。
- 打开页面后，首页能正常加载接口数据（不是纯静态空白）。

## 5. 常见问题排查

1. 报 `@my-miniprogram/...` 找不到：
- 通常是 `LaiKePages` 未完整同步到 `laike-pages/src`。
- 重新执行第 2 步同步命令。

2. 报 iconfont 字体文件找不到：
- 检查 `laike-pages/src/static/iconfont1/` 下是否存在 `iconfont.ttf / woff / woff2 / css`。
- 重新同步 `LaiKePages/static/` 到 `laike-pages/src/static/`。

3. 页面能开但接口 404/跨域：
- 核对 `laiketuiCommon.js` 中 `LKT_ROOT_URL` 是否指向本地实际网关。
- 核对本地网关端口、路径（`/apis` 或 `/gw`）是否正确。
