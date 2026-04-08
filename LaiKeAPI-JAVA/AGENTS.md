# Repository Guidelines

## Project Structure & Module Organization

This repository is a multi-module Maven project (`pom.xml` at root, packaging `pom`) for the Laike e-commerce platform.
Core modules are organized by responsibility:

- `laike-core`, `laike-domain`, `laike-common`, `laike-common-api`: shared base/domain/api contracts.
- `laike-admins`, `laike-apps`, `laike-comps`, `laike-plugins` and `*-api` counterparts: business services and exposed
  interfaces.
- `laike-apis`: web/admin entry module with static assets and templates.
- `laike-root`: root application module.
- `docs/`: deployment and middleware setup references (Nacos, RocketMQ, XXL-JOB, etc.).

Use standard Maven layout in each module: `src/main/java`, `src/main/resources`, `src/test/java`.

## Build, Test, and Development Commands

Run from repository root:

- `mvn clean install -DskipTest``s`: full multi-module build.
- `mvn clean package`: package all modules.
- `mvn -pl laike-apps -am package -DskipTests`: build one module and its dependencies.
- `mvn -pl laike-root spring-boot:run -Dspring-boot.run.profiles=dev`: start a target app locally.
- `mvn test -DskipTests=false`: force tests to run.

Note: root `pom.xml` configures Surefire with `skipTests=true` by default, so explicitly override when validating
changes.

## Coding Style & Naming Conventions

- Java 8, 4-space indentation, UTF-8 source files.
- Package naming: lowercase dot-separated (`com.laiketui...`).
- Class naming: `PascalCase`; methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Follow existing layered suffixes: `*Controller`, `*Service`, `*ServiceImpl`, `*Mapper`.
- Keep environment-specific configuration in profiles/Nacos properties; avoid hardcoding secrets.

## Testing Guidelines

- Frameworks: Spring Boot Test (`spring-boot-starter-test`) and module-level tests under `src/test/java`.
- Test class naming: `*Tests.java` for integration/context tests.
- Add tests for service logic and API behavior in the module you modify.
- Before PR: run `mvn test -DskipTests=false` (or module-scoped equivalent).

## Commit & Pull Request Guidelines

Current workspace snapshot does not include Git metadata (`.git` missing), so project-local Git history is unavailable
here.
Recommended practice:

- Commit message format: `<module>: <imperative summary>` (example: `laike-apps: fix order status transition check`).
- Keep commits focused and atomic; avoid mixing refactor and behavior changes.
- PRs should include: change summary, affected modules, test evidence/commands, config impact (Nacos/DB/RocketMQ), and
  screenshots for UI/template changes.
