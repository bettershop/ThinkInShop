---
name: laikeapi-local-deploy
description: Use this repo-local skill when installing, deploying, starting, health-checking, or troubleshooting LaiKeAPI locally, especially when an AI agent needs to bootstrap the ThinkPHP API on port 8001.
---

# LaiKeAPI Local Deploy

## Ground rules

- Work from the `LaiKeAPI` project directory.
- Before editing code, run `svn status` and `svn update`; if tracked local changes or conflicts appear, pause and confirm with the user.
- Never commit `.env`, `vendor/`, `runtime/`, logs, or secrets.
- Baseline: PHP `>=8.4` and `<8.5`, Composer 2.x, MySQL 8+, Redis 6+.
- Default local test port: `8001`.

## Fast path for AI agents

```bash
cd LaiKeAPI
PORT=8001 HOST=127.0.0.1 bash scripts/ai_bootstrap.sh
```

The script installs Composer dependencies, runs ThinkPHP service discovery, publishes vendor assets, starts `php think run`, and checks:

```bash
curl -fsS "http://127.0.0.1:8001/?api=admin.Index.index"
```

If the check passes, start a persistent local server manually:

```bash
php think run -H 0.0.0.0 -p 8001
```

## Local services

If MySQL or Redis is not already running and Docker is acceptable, start the bundled local services:

```bash
docker compose -f docker-compose.local-test.yml up -d mysql redis
```

Defaults used by the bundled services:

- MySQL: `127.0.0.1:3306`, database `tp_db`, user `root`, password `000000`.
- Redis: `127.0.0.1:6339`, password `laiketui18`, database index `12`.

## Manual fallback

Use this when the bootstrap script is not appropriate:

```bash
composer install --no-dev --optimize-autoloader
php think service:discover
php think vendor:publish || true
php think run -H 0.0.0.0 -p 8001
```

Then verify:

```bash
curl -fsS "http://127.0.0.1:8001/?api=admin.Index.index"
```

## Configuration notes

- `.env` is local and ignored by version control; do not print or commit its contents.
- Database config is loaded through `config/db_config.php`.
- `config/cache.php` defaults to Redis on `127.0.0.1:6339` with password `laiketui18`.
- Keep the root API style: `/?api=admin.Index.index`.

## Troubleshooting

- PHP version failure: switch to PHP 8.4.x before running Composer.
- Port `8001` is busy: inspect with `lsof -iTCP:8001 -sTCP:LISTEN`.
- Database connection failure: verify MySQL is running and `.env` matches the intended local database.
- Redis connection failure: verify Redis is running on port `6339` with password `laiketui18`.
- Health check says `api不能为空`: the request is missing the `api` query parameter.
- Bootstrap failure details: inspect `/tmp/laikeapi_tp8_bootstrap.log`.

## Finish checklist

- Run the `8001` health check before claiming local deployment works.
- Run `svn status` before committing and confirm only intended files are staged/added.
- Add new docs with `svn add docs/...` before `svn commit`.
