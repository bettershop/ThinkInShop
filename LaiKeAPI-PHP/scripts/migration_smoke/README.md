# migration_smoke 脚本说明

## user_domain_smoke.sh

用途：用户域只读回归雏形（登录 + 用户信息 + 钱包明细 + 地址读取 + 管理端用户列表）。

默认调用：

```bash
bash scripts/migration_smoke/user_domain_smoke.sh
```

可选参数：

```bash
bash scripts/migration_smoke/user_domain_smoke.sh http://www.laike.com:82
```

常用环境变量：

```bash
STORE_ID=1
MALL_STORE_TYPE=6
APP_STORE_TYPE=2
ACCOUNT=000000
PASSWORD=000000
COUNTRY_NUM=156
CPC=86
OUT_DIR=runtime/ai_migration/T10
```

输出：

1. 终端打印每个接口的 `code/message`
2. 报告文件保存到 `OUT_DIR`，文件名示例：
   - `runtime/ai_migration/T10/user_domain_smoke_20260413_120000.log`

## product_sku_recon.sh

用途：执行 T09 商品/SKU 对账 SQL，并自动汇总关键指标（Q2~Q8）。

默认调用：

```bash
bash scripts/migration_smoke/product_sku_recon.sh
```

常用环境变量：

```bash
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=lkt_db
DB_USER=root
DB_PASS=123456
SQL_FILE=runtime/ai_migration/T09/product_sku_recon.sql
OUT_DIR=runtime/ai_migration/T10
```

输出：

1. 原始执行日志：`product_sku_recon_<ts>.log`
2. 错误日志：`product_sku_recon_<ts>.err`
3. 汇总报告：`product_sku_recon_<ts>_summary.md`
