# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-module Maven project for the Laike (来客) e-commerce platform - a multi-tenant, multi-store SaaS e-commerce system supporting malls, shops, suppliers, and various business extensions (coupons, groups, points, auctions, etc.).

## Technology Stack

| Component | Version | Notes |
|-----------|----------|-------|
| JDK | 1.8.211 | Strictly required |
| Spring Boot | 2.3.12.RELEASE | - |
| Spring Cloud Alibaba | 2.2.7.RELEASE | - |
| Spring Cloud | Hoxton.SR12 | - |
| Nacos | 2.1.1 | Config & Service Discovery |
| Dubbo | 3.0.8 | RPC (partially deprecated, newer code uses OpenFeign) |
| MyBatis/Tk-MyBatis | 2.1.5 | Persistence layer |
| MySQL | 5.6-5.7 | Config: `sql_mode=NO_ZERO_DATE` |
| Redis | 7 | - |
| Maven | 3.6.1 | 3.8+ has issues |
| Tomcat | 9.0.55 | For WAR deployment |

## Architecture

### Module Structure

```
laike-root/          # Authorization library (pre-installed JAR)
laike-core/          # Common configuration, cache, utils, exception handling
laike-domain/         # Domain entities and models
laike-common/         # Shared business logic, mappers
laike-common-api/     # Common API contracts

laike-admins/        # PC admin backend (platform + shop + supplier management)
laike-admins-api/    # Admin API interfaces

laike-apps/          # Mobile buyer app backend
laike-apps-api/      # Buyer app API interfaces

laike-comps/          # Common services (gateway, payment, order, file, IM, task)
laike-comps-api/      # Common services API interfaces

laike-plugins/        # Plugin services (marketing features: coupon, group, seckill, etc.)
laike-plugins-api/    # Plugins API interfaces

laike-cloud-gateway/  # Spring Cloud Gateway (optional, for OpenFeign routing)
laike-apis/           # Unified entry module
```

### Deployment Architecture

The system uses two gateway layers:

1. **laike-comps/gateway** - Custom gateway (legacy, bundled in comps module, always required)
2. **laike-cloud-gateway** - Spring Cloud Gateway (optional, required only if using OpenFeign for new services)改成

All API requests (except IM and payment callbacks) must go through a gateway.

### Key Services

- **laike-comps**: Contains core shared services (gateway, payment, order processing, file management, IM, scheduled tasks)
- **laike-admins**: Multi-tenant admin backend (platform-level + shop-level + supplier-level)
- **laike-apps**: Mobile app buyer endpoints
- **laike-plugins**: Extensible marketing plugins (groups, coupons, points, auctions, etc.)

### Database

Multi-tenant isolation: Data is isolated by store_id (mall ID). Tables use `lkt_` prefix (e.g., `lkt_order`, `lkt_user`).

## Build & Run Commands

### Maven Build

```bash
# Full build with tests skipped (default)
mvn clean install -DskipTests

# Package without tests
mvn clean package

# Build specific module with dependencies
mvn -pl laike-admins -am package -DskipTests

# Force test execution
mvn test -DskipTests=false
```

### Environment Profiles

Available Maven profiles: `local` (default), `dev`, `prod`, `test`, `demo`, `inter`

Profile controls:
- Nacos namespace and address
- Database and Redis configuration (via Nacos)
- MyBatis configuration files

### Running Services Locally

Use IDEA's Services view or run main classes:

- `com.laiketui.admins.LaikeAdminsApplication` - Admin backend
- `com.laiketui.apps.LaikeAppsApplication` - Buyer app backend
- `com.laiketui.comps.LaikeCompsApplication` - Common services (includes gateway, must start)
- `com.laiketui.plugins.LaikePluginsApplication` - Plugin services

### Spring Cloud Gateway (Optional)

```bash
java -jar laike-cloud-gateway/target/laike-cloud-gateway-0.0.1-SNAPSHOT.jar
```

## Configuration Management

### Nacos Configuration

All runtime configuration is stored in Nacos configuration center.

**Common config files** (per environment):
- `comm-local.yml`, `comm-dev.yml`, `comm-prod.yml`, `comm-test.yml`, `comm-demo.yml`

**Module-specific configs**:
- `laike-admin-store-{profile}.yml`
- `laike-admin-mch-{profile}.yml`
- `laike-apps-{profile}.yml`
- `laike-plugins-{profile}.yml`

**Key settings in Nacos**:
- Database connection (url, username, password)
- Redis connection (host, port, password, database)
- Server ports (`server.port`)
- File upload paths (`uploadFile.path`)
- WeChat certificate path (`node.wx-certp12-path`)

### Local Configuration Override

Environment variables can override defaults:
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PWD=laiketui
export REDIS_DB=0
export UPLOAD_PATH=/usr/local/imgs
```

## Important Deployment Notes

### Port Configuration

Tomcat `server.xml` connector port MUST match the `server.port` in the corresponding Nacos configuration. Mismatch causes 404/service-not-found errors.

After changing ports: Clear Redis service registry cache before restarting Tomcat.

### Tomcat Configuration

Modify `conf/catalina.properties` line 108 to avoid slow startup:
```properties
tomcat.util.scan.StandardJarScanFilter.jarsToSkip=*.jar
```

### Authorization Library

The `laike-root-0.0.1-SNAPSHOT.jar` is a pre-installed JAR that must be in your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=/path/to/laike-root-0.0.1-SNAPSHOT.jar \
  -DgroupId=com.laiketui \
  -DartifactId=laike-root \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackaging=jar
```

### Database Migration

SQL change scripts go in `db_logs/incremental-sql/`.
Naming convention: `YYYYMMDD_nameInitials.sql` (e.g., `20240314_abc.sql`).

## Development Patterns

### Controller Layer

Controllers use `@HttpApiMethod(apiKey = "...")` annotation for API routing.
Example:
```java
@ApiOperation("Get stock info")
@PostMapping("/getStockInfo")
@HttpApiMethod(apiKey = "admin.goods.getStockInfo")
public Result getStockInfo(StockInfoVo vo, HttpServletResponse response) {
    // ...
}
```

### Service Layer

Services follow naming pattern: `*Service` (interface) and `*ServiceImpl` (implementation).
Package structure mirrors functional areas (e.g., `saas`, `plugin`, `users`, `order`).

### Excel Export

For Excel export, endpoints must:
1. Accept `exportType=1` request parameter
2. Return `Result.exportFile()` (null equivalent)

```java
Map<String, Object> ret = stockService.getStockInfo(vo, response);
return ret == null ? Result.exportFile() : Result.success(ret);
```

### Order Status Values

- `0`: Pending payment
- `1`: Paid
- `2`: Pending delivery
- `5`: Completed (unsettled, after-sales pending)
- `7`: Closed (settled, after-sales done)

## External Services

### Required Services

- **Nacos 2.1.1**: Configuration and service discovery ( standalone mode )
- **MySQL 5.6-5.7**: Database
- **Redis 7**: Caching and session
- **MinIO 8.5.4**: Object storage (optional, can use Aliyun OSS)
- **XXL-JOB**: Scheduled task management (for `laike-comps/task`)
- **RocketMQ**: Message queue (optional)

### Payment Integrations

- WeChat Pay (including小程序 and public account)
- Alipay
- PayPal
- Stripe

## Troubleshooting

### Service Not Found / 404

1. Verify Tomcat `server.xml` port matches Nacos `server.port`
2. Check if Nacos service registry has stale entries (clear Redis)
3. Ensure `laike-comps` (gateway) is running

### Maven Build Issues

- If JARs can't be downloaded, check Maven version is 3.6.1
- Verify `laike-root` JAR is installed in local repository
- Run multiple `mvn clean` and `Reload Project` operations

### Configuration Not Applied

- After switching Maven profiles, run `mvn clean` and Reload Project
- Check Nacos namespace matches the selected profile

## Code Conventions

- Package naming: `com.laiketui.{module}.{package}`
- Class naming: `PascalCase`
- Methods/fields: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- File encoding: UTF-8
- Indentation: 4 spaces
