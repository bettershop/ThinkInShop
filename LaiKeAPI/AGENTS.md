# Repository Guidelines

This repository is a ThinkPHP 6-based API service. Use the structure and naming patterns already present to keep modules discoverable and consistent.

## Project Structure & Module Organization
- `app/` core application code (controllers, models, common helpers).
- `app/admin/controller/` API controllers grouped by channel (admin, app, mall, mch, supplier, plugin).
- `app/common/` shared domain logic and utilities.
- `app/db_log/` database scripts and change logs (add SQL here when schema changes).
- `config/` framework and environment configuration.
- `route/` route definitions.
- `public/` web entry points (`public/index.php`, `public/admin.php`) and static assets.
- `extend/` legacy or third-party extensions (PSR-0 autoload).
- `view/` templates (if used by specific endpoints).
- `runtime/` generated cache/logs; do not commit.
- `vendor/` Composer dependencies.

## Build, Test, and Development Commands
Use PHP `>=7.2.5` as required by `composer.json`.

```bash
composer install        # install PHP dependencies
php think               # list ThinkPHP console commands
php -S 127.0.0.1:8000 -t public  # quick local server (optional)
```

## Coding Style & Naming Conventions
- Indentation: 4 spaces; braces on new lines (follow files under `app/admin/controller/`).
- Namespaces follow PSR-4 (`app\\` → `app/`); keep class names and filenames aligned.
- Non-plugin modules use concise PascalCase names (max two English words), e.g. `OrderSettlement`.
- Plugin controllers live under `app/admin/controller/plugin/<plugin>/` and use prefixes:
  `AdminXxx`, `AppXxx`, `MchXxx`.

## Testing Guidelines
No dedicated `tests/` directory or test runner is configured in this checkout. If you add tests, place them under `tests/`, name files `*Test.php`, and document the exact command (e.g., `vendor/bin/phpunit`) in `README.md`.

## Commit & Pull Request Guidelines
This checkout has no Git history available, so follow a clear, minimal convention:
- Commit messages: imperative summary line, scoped when helpful (e.g., `admin: fix order export`).
- PRs: include a short description, verification steps, and any config or SQL changes.
- Schema changes should ship with an updated script in `app/db_log/`.
- Include screenshots for admin/mall UI changes.

## Security & Configuration Tips
- Keep secrets in `.env`; avoid committing credentials.
- Prefer config updates in `config/` with sensible defaults.
- Never commit `runtime/` artifacts.
