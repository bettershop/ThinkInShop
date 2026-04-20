# 暂停记录（2026-04-07 15:06）

## 2026-04-08 约束修正（新增）
1. 已按“修改前先 `svn update`”执行：
   - 命令：`svn update --username xgg --password 112233 ...`
   - 结果：`LaikeAPI` 更新到 `r37653`。
2. 已按最新硬约束回退 Address service 层方案：
   - 回退：`app/admin/controller/app/Address.php`
   - 回退：`app/admin/controller/mall/Address.php`
   - 删除：`app/common/AddressDomainService.php`
   - 删除：`app/common/AddressWriteService.php`
3. 已确认代码中无 `AddressDomainService/AddressWriteService` 引用（`rg` 扫描为空）。
4. 后续统一执行口径：
   - 同库迁移不新增 service 层。
   - 表结构字段、接口返回结构、逻辑功能一致则不改。
   - 仅在存在差异时做最小修改，并持续记录到文档。

## 本轮已完成
1. 已执行数据库合并前 SVN 强制前置更新（账号按你指定）：
   - `LaikeAPI`：已更新到 `r37652`
   - `thinkinshop`：当前 `r2902`（已是最新）
   - 本轮未出现冲突（`svn status` 无 `C` 项）

2. 已按“非 Docker”方式完成线上库导入本地：
   - 远端：`47.107.123.240:3386` / `v3_db`
   - 本地：`127.0.0.1:3306` / `lkt_db`
   - 执行命令：`LOCAL_PASSWORD=123456 bash scripts/import_online_db_to_local.sh`

3. 导入后基础校验通过：
   - `table_count=237`
   - `lkt_admin=62`
   - `lkt_config=39`
   - `lkt_file_delivery=44`

4. 框架命令校验通过：
   - `php think` 正常输出命令列表，无 `MYSQL_* already defined` warning。

5. 已确认当前联调口径（你要求）：
   - 不使用 Docker 作为本地联调环境依赖。
   - 使用本机 MySQL/Redis（`3306` / `6339`）直接联调。

6. 已执行一次全表对齐检查脚本：
   - 报告目录：`runtime/db_align_reports/20260407_110959`
   - 结果显示存在差异（`diff_tables_v3_only=9` 等）。
   - 已确认差异由“本机 `v3_db` 历史快照滞后”导致，不代表远端 `v3_db` 现状。

## 本轮新增（13:58）
1. 已再次执行数据库合并前 SVN 强制前置更新：
   - `LaikeAPI`：`r37652`
   - `thinkinshop`：`r2902`
   - 无冲突（`svn status` 无 `C`）。
2. 已完成 `M3-A1 用户域基线批（P0）` 文档落库：
   - 新增：`M3_A1_USER_DOMAIN_BASELINE.md`
   - 已包含：字段映射表、SQL 读写点清单、接口清单、回归清单。
3. 已完成用户域三表跨库核对（本地 `lkt_db` vs 远端 `v3_db`）：
   - `lkt_user/lkt_user_address/lkt_record` 字段清单对比 `NO_DIFF`
   - 计数一致：`158 / 102 / 12706`。
4. 已复测服务可用性（非 Docker）：
   - PHP：`php think` 通过；`php -S` + `curl /` 返回 `200`
   - Java（统一入口）：`laike-apis` 启动后 `/apis/` 与 `/apis/actuator/health` 返回 `200`。
5. 已清理脚本内历史命名残留：
   - `scripts/db_full_alignment_check.sh` 去除 `TP_*` 回退变量，仅保留 `LOCAL_* / LKT_DB` 口径。

## M3-A2 第一批（已完成）
1. 已新增地址域公共服务：
   - `app/common/AddressDomainService.php`
   - 公共能力：地区级联、`place` 解析、`address_xq` 组装、名称/ID路径转换。
2. 已完成双控制器第一批接入：
   - `app/admin/controller/app/Address.php`
   - `app/admin/controller/mall/Address.php`
3. 已完成文档落库：
   - `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`
4. 验证通过：
   - `php -l`（3个改动PHP文件）通过
   - `php think route:list` 通过

## M3-A2 第二批（已完成）
1. 已新增地址写模型公共服务：
   - `app/common/AddressWriteService.php`
   - 公共能力：`createAddress/updateAddress/setDefaultAddress/deleteAddress`
2. 已完成双控制器写流程收敛：
   - `app/admin/controller/app/Address.php`
   - `app/admin/controller/mall/Address.php`
3. 已完成文档同步：
   - `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`
   - `DB_MIGRATION_PLAN.md`
4. 验证通过：
   - `php -l app/common/AddressWriteService.php`
   - `php -l app/admin/controller/app/Address.php`
   - `php -l app/admin/controller/mall/Address.php`
   - `php think route:list`

## 当前状态
1. 本地 `lkt_db` 已是最新远端 `v3_db` 导入结果，可用于继续 M2/M3 联调。
2. 本机 `v3_db` 可能不是最新口径，不建议直接作为 `lkt_db` 对比基准。
3. `scripts/db_full_alignment_check.sh` 已增强为支持“跨主机比对”（本地 `lkt_db` vs 远端 `v3_db`）。
4. `M3-A2` 已完成收口（批次1-3）。
5. 已开始订单域基线整理：
   - 文档：`M3_B1_ORDER_DOMAIN_BASELINE.md`
   - 下一步：按订单状态字典、接口清单、读写点清单继续细化
6. 已补订单域扫描产物：
   - `runtime/m3_b1_order_domain/php_order_refs.txt`
   - `runtime/m3_b1_order_domain/php_delivery_refs.txt`
   - `runtime/m3_b1_order_domain/php_pay_refs.txt`
   - `runtime/m3_b1_order_domain/php_admin_order_refs.txt`
   - `runtime/m3_b1_order_domain/php_order_status_dictionary.txt`
7. 已完成首批最小代码改造：
   - `app/admin/controller/admin/Order.php` 订单导出补齐 `8=待核销`
   - `app/admin/controller/plugin/sec/Order.php` 订单详情/列表补齐 `8=待核销`
8. 已按“最小改动”原则重新评估并固化执行口径：
   - 仅在“PHP/Java 同库字段差异”时改代码。
   - 禁止顺手重构与实现层大改。
   - 统一使用账号 `000000/000000` 做接口级回测。
8. 本地网关已跑通（现有服务，无小皮）：
   - 可用网关：`http://www.laike.com:82/gw`
   - 说明：`80` 端口当前被系统 Apache 占用，因此本轮以 `82` 作为稳定联调入口。
   - 连通回测：Address 相关 `app/mall` 接口已通过 `:82/gw` 命中后端（未登录态统一 `203 Illegal invasion`，符合预期）。
9. 已新增接口回测脚本：
   - `scripts/address_gateway_smoke.sh`
10. 本地 PC 登录验证码已可按开关跳过：
   - 代码：`app/admin/controller/mall/Login.php`
   - 配置：`.env` 中 `[LOCAL] SKIP_MALL_LOGIN_CAPTCHA = true`
   - 说明：仅影响 `mall.Login.login` 的图形验证码校验，默认可关闭。
11. 登录接口已做网关级回测（`:82/gw`）：
   - `mall.Login.login`（统一账号）返回：`200`
   - `app.login.login`（统一账号）返回：`200`
   - 默认基址：`http://www.laike.com:82/gw?store_id=1&store_type=6`
12. 已固化统一回测账号（后续数据库迁移接口级测试统一使用）：
   - 账号：`000000`
   - 密码：`000000`
   - 脚本：`scripts/address_gateway_smoke.sh` 已升级为“先登录拿 `access_id`，再做 Address 接口回测”。
   - 当前实测：`mall.Login.login=200`、`app.login.login=200`、Address 读接口均可达（`app.Address.getCountyInfo` 维持历史返回码 `109`）。

## 今天收口结论（到此停编码）
1. 已完成 `M3-A2 Batch 3` 收口，但采用“最小改动”方式：
   - 不改地址接口既有读逻辑
   - 不做额外重构
   - 仅补齐登录态接口级回测基线与记录
2. 明天继续从下一业务域开始，按“字段差异驱动改造”推进。

## 重启后建议继续命令（非 Docker）
1. 验证本机 MySQL 连通：
```bash
MYSQL_PWD=123456 mysql -h127.0.0.1 -P3306 -uroot -e "SELECT 1;"
```

2. 如需重新导入远端库：
```bash
cd /Users/wangxian/all-codes/LaikeAPI
LOCAL_PASSWORD=123456 bash scripts/import_online_db_to_local.sh
```

3. 运行跨主机全表对齐检查（推荐）：
```bash
LOCAL_MYSQL_HOST=127.0.0.1 LOCAL_MYSQL_PORT=3306 LOCAL_MYSQL_USER=root LOCAL_MYSQL_PWD=123456 \
V3_MYSQL_HOST=47.107.123.240 V3_MYSQL_PORT=3386 V3_MYSQL_USER=root V3_MYSQL_PWD=123456 \
LOCAL_DB=lkt_db V3_DB=v3_db \
bash scripts/db_full_alignment_check.sh
```

4. 框架命令冒烟：
```bash
php think
```

5. Java 统一入口启动（按项目约定）：
```bash
cd /Users/wangxian/all-codes/thinkinshop
mvn -pl laike-apis -am spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests
```
访问验证：
```bash
curl -i http://127.0.0.1:21898/apis/
curl -i http://127.0.0.1:21898/apis/actuator/health
```

6. Address 接口级回测（网关透传）：
```bash
cd /Users/wangxian/all-codes/LaikeAPI
php -S 127.0.0.1:8000 -t public
```
另开一个终端执行：
```bash
cd /Users/wangxian/all-codes/LaikeAPI
bash scripts/address_gateway_smoke.sh
```

## 关键变更文件
- `config/db_config.php`
- `scripts/import_online_db_to_local.sh`
- `scripts/db_full_alignment_check.sh`
- `DB_MIGRATION_PLAN.md`
- `M3_A1_USER_DOMAIN_BASELINE.md`
- `M3_A2_ADDRESS_CONVERGENCE_PLAN.md`
- `app/common/AddressDomainService.php`
- `app/common/AddressWriteService.php`
- `app/admin/controller/app/Address.php`
- `app/admin/controller/mall/Address.php`
- `scripts/address_gateway_smoke.sh`
- `PAUSE_RESUME_NOTE.md`
