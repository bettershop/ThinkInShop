# LaiKeAPI-TP8 AI 安装部署指南（低 Token 版）

目标：让用户拉取仓库后，AI 以最短步骤自动完成安装与启动。

## 为什么不是 Skill
- `Skill` 适合 Trae 本地能力，不随 GitHub 仓库天然分发。
- 你的场景是“代码拉下来就能被 AI 识别并执行”，应使用仓库内文档：
  - `AGENTS.md`（AI优先读取）
  - 本文档（人类 + AI 共读）

## 固定环境口径
- PHP：`>=8.4` 且 `<8.5`
- Composer：2.x
- MySQL：8.0+
- Redis：6.0+

## AI 最小执行步骤（可直接复制）
```bash
cd LaiKeAPI-TP8
composer install --no-dev --optimize-autoloader
php think service:discover
php think vendor:publish
php think run -H 0.0.0.0 -p 8001
```

## 一键脚本（推荐给 AI）
```bash
cd LaiKeAPI-TP8
bash scripts/ai_bootstrap.sh
```
脚本会先做 PHP 版本前置校验（必须 `>=8.4` 且 `<8.5`），不满足会立即失败并给出原因。

验证：
```bash
curl -s "http://127.0.0.1:8001/?api=admin.Index.index"
```

## Nginx 统一入口（/）
说明：本项目统一入口是 `/`，通过 `api` 参数分发；任意前缀由 Nginx 代理到 `index.php` 即可。

```nginx
location / {
    try_files $uri $uri/ /index.php?$query_string;
}

location ~ \.php$ {
    include fastcgi_params;
    fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
    fastcgi_pass 127.0.0.1:9000;
}
```

调用示例（等价）：
- `/?api=admin.Index.index`
- `/gw?api=admin.Index.index`
- `/any/prefix?api=admin.Index.index`

## 给 AI 的推荐提示词（短）
```text
Read AGENTS.md first. Install this project with minimum steps, keep root API style /?api=..., do not modify vendor.
```

## 常见失败与处理
- `composer install` 报 PHP 版本不符：切到 PHP 8.4.x。
- 404 或路由不生效：确认 Nginx `try_files` 是否指向 `/index.php?$query_string`。
- 接口返回 `api不能为空`：请求未携带 `api` 参数。
- 一键脚本失败：查看 `/tmp/laikeapi_tp8_bootstrap.log`。
