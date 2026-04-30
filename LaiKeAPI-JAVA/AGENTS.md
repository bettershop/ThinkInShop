# 仓库指南

## 项目结构与模块组织

本项目是来客推电商平台的多模块 Maven 项目（根 `pom.xml`，打包类型 `pom`）。
核心模块按职责划分：

- `laike-core`、`laike-domain`、`laike-common`、`laike-common-api`：共享基础/领域/API 定义。
- `laike-admins`、`laike-apps`、`laike-comps`、`laike-plugins` 及对应的 `*-api`：业务服务与对外接口。
- `laike-apis`：Web/管理后台入口模块（含静态资源和模板）。
- `laike-root`：根应用模块。
- `docs/`：部署与中间件参考文档（Nacos、RocketMQ、XXL-JOB 等）。

每个模块使用标准 Maven 布局：`src/main/java`、`src/main/resources`、`src/test/java`。

## 构建、测试与开发命令

在仓库根目录执行：

- `mvn clean install -DskipTests`：完整多模块构建。
- `mvn clean package`：打包所有模块。
- `mvn -pl laike-apps -am package -DskipTests`：构建单个模块及其依赖。
- `mvn -pl laike-root spring-boot:run -Dspring-boot.run.profiles=dev`：本地启动目标应用。
- `mvn test -DskipTests=false`：强制执行测试。

注意：根 `pom.xml` 配置了 Surefire 默认 `skipTests=true`，验证修改时需显式覆盖。

## 代码风格与命名规范

- Java 8，4 空格缩进，UTF-8 源文件编码。
- 包命名：小写点分隔（`com.laiketui...`）。
- 类命名：`PascalCase`；方法/字段：`camelCase`；常量：`UPPER_SNAKE_CASE`。
- 遵循现有分层后缀：`*Controller`、`*Service`、`*ServiceImpl`、`*Mapper`。
- 环境相关配置放入 profiles/Nacos 配置中；禁止硬编码密钥/密码。

## 测试指南

- 框架：Spring Boot Test（`spring-boot-starter-test`），模块级测试放在 `src/test/java`。
- 测试类命名：集成/上下文测试使用 `*Tests.java`。
- 为修改模块中的服务逻辑和 API 行为添加测试。
- 提交前：运行 `mvn test -DskipTests=false`（或对应模块级别的测试）。

## 提交与 Pull Request 指南

- 提交信息格式：`<模块>: <摘要>`（例：`laike-apps: 修复订单状态流转校验`）。
- 保持提交聚焦、原子化；避免混淆重构与行为变更。
- PR 应包含：变更摘要、受影响模块、测试证据/命令、配置影响（Nacos/DB/RocketMQ），UI/模板变更需附截图。
