# Repository Guidelines

## AI Local Setup First
- Before local run/deploy, read `AI_LOCAL_SETUP.md` first.
- Follow that document for gateway config (`components/laiketuiCommon.js`) and sync flow (`LaiKePages -> laike-pages/src`).
- Use the startup command order defined there.

## Project Structure & Module Organization
- `App.vue` and `main.js` are the entry points for the Uni-app application.
- Page routing and app metadata live in `pages.json` and the `manifest*.json` files.
- Feature pages are organized under `pages/` and `pagesA/`–`pagesE/`.
- Reusable UI lives in `components/`, with shared logic in `common/`, `mixins/`, and `store/` (Vuex).
- Static assets are in `static/`; generated build output goes to `unpackage/` (do not edit manually).
- Scripts for packaging live in `scripts/`, and documentation is in `doc/`.

## Build, Test, and Development Commands
- `npm run build:packages`: builds bundled packages used by the app.
- `npm run predev`: runs `build:packages` before development.
- `npm run dev:h5`: starts the H5 dev server (uses `vue.config.js`).
- `npm run build`: production build via webpack.
- `npm test`: currently exits with “no test specified”. Testing is not wired yet.

## Coding Style & Naming Conventions
- Follow the Vue 2 style guide and local rules in `风格指南.md`.
- Vue SFCs should use 4-space indentation and end statements with semicolons.
- `<style>` blocks must include `scoped` and typically use `lang="less"`.
- Prefer `async/await`, avoid `let me = this`, and use shared navigation helpers from `doc/navigator.md`.
- Image references inside Vue files must use online URLs (local file imports break in App builds).

## Testing Guidelines
- No automated test framework is configured. Validate changes manually in H5 and target builds.
- If adding tests, document the framework and add a runnable script in `package.json`.

## Commit & Pull Request Guidelines
- Git history is not available in this checkout, so no commit convention can be inferred.
- Use clear, imperative commit messages (or your team’s standard, e.g., Conventional Commits).
- PRs should describe scope, include screenshots for UI changes, and link relevant issues.

## Configuration & Release Notes
- Environment-specific settings live in `manifest_php_*` and `manifest_java_*` files.
- Avoid editing `unpackage/` directly; regenerate via build commands.
