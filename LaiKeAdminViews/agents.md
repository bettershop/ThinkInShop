# Agent Skill 入口

AI 智能体通过本项目下的 skill 目录快速完成项目安装和开发环境配置。

## Skill 目录结构

```
LaiKeJavaViews/
├── skill/
│   ├── setup.sh              # 一键安装脚本
│   └── config.sh             # API 配置脚本（连接 Java/PHP 后端）
├── agents.md                 # ← 当前文件，skill 总入口说明
├── CLAUDE.md                 # Claude 自动读取的项目说明
└── .claude/
    └── settings.json         # Claude Code skill 定义 (/setup-laike)
```

## 第一步：安装项目

```bash
bash skill/setup.sh
```

脚本自动完成：nvm 切换 → Node v14.20 → 清理缓存 → npm install --legacy-peer-deps

## 第二步：配置后端 API（重要）

用户下载 Java/PHP 开源版后端并本地启动后，需配置前端 API 地址：

```bash
bash skill/config.sh
```

脚本交互式询问：
- 后端类型: Java (默认端口 8000) / PHP / 自定义
- 生成 `.env.local` 文件覆盖 `.env.development` 中的 `VUE_APP_BASE_API`

**原理**: Vue CLI 原生支持 `.env.local`，该文件的变量会覆盖 `.env.development` 中同名变量，且已加入 `.gitignore` 不会提交。

## 第三步：启动开发服务

```bash
npm run dev
# http://localhost:9528
```

## 构建命令

| 命令 | 用途 |
|------|------|
| `npm run java:dev` | Java 开发环境构建 |
| `npm run java:prod` | Java 生产环境构建 |
| `npm run java:test` | Java 测试环境构建 |
| `npm run php:dev` | PHP 开发环境构建 |
| `npm run php:prod` | PHP 生产环境构建 |
| `npm run php:test` | PHP 测试环境构建 |
