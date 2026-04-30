# AGENTS Instructions

This file is optimized for AI coding agents (minimum-token execution).

## 0) Repo-Local Skills
- For local install, deploy, start, health-check, or troubleshooting work, read `docs/laikeapi-local-deploy/SKILL.md` first.
- The local deployment skill is the canonical AI flow and defaults to port `8001`.

## 1) Runtime Baseline
- Framework: ThinkPHP 8 (`topthink/framework ^8.1`)
- PHP: `>=8.4` and `<8.5` (project deployment baseline)
- Entry: `public/index.php`
- Gateway style: unified root entry `/?api=a.b.c` or `/?api=a.b.c.d`

## 2) Fast Install (must-follow order)
```bash
cp .env .env.local 2>/dev/null || true
composer install --no-dev --optimize-autoloader
php think service:discover
php think vendor:publish
```

## 3) Local Run
```bash
php think run -H 0.0.0.0 -p 8001
```

Health check:
```bash
curl -s "http://127.0.0.1:8001/?api=admin.Index.index"
```

## 4) Routing Compatibility Contract
- Do not modify vendor framework files.
- Keep API dispatch compatibility:
  - `a.b.c` -> `app\admin\controller\a\b::c()`
  - `a.b.c.d` -> `app\admin\controller\a\b\c::d()`
- If route not matched but `api` exists, fallback dispatch is required.

## 5) Safe Edit Rules
- Do not commit secrets in `.env`.
- Do not commit `runtime/`.
- SQL schema changes must be added under `app/db_log/`.
- Keep PHP files ASCII unless existing file already uses Unicode content.
