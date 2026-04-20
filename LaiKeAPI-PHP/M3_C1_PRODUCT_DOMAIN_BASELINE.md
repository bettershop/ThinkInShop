# M3_C1_PRODUCT_DOMAIN_BASELINE.md - 商品域基线对齐文档

## 1. 业务目标
实现 `LaikeAPI (PHP)` 与 `thinkinshop (Java)` 在商品数据口径上的 100% 兼容。确保 PHP 侧能够正确读取和处理 Java 侧产生的多语言商品、虚拟商品及多门店商品数据。

## 2. 核心表清单
- `lkt_product_list`: 商品主表 (对齐重点)
- `lkt_product_class`: 分类表
- `lkt_brand`: 品牌表
- `lkt_product_img`: 图片表

## 3. 字段映射与对齐方案

### 3.1 `lkt_product_list` 关键字段

| 字段名 | PHP 定义 (旧) | Java 定义 (基准) | 对齐策略 |
| :--- | :--- | :--- | :--- |
| `commodity_type` | 0-实物, 1-虚拟 | 0-实物, 1-虚拟, 2-虚拟不核销, 3-虚拟核销 | **需改造**：PHP 查询逻辑应兼容 1,2,3 为虚拟商品 |
| `status` | 1-待上架, 2-上架, 3-下架 | 1-待上架, 2-上架, 3-下架 | 已对齐 |
| `recycle` | 0-显示, 1-回收 | 0-显示, 1-回收 | 已对齐 |
| `lang_code` | 语种编码 (如 zh_cn) | 语种编码 | 已对齐 |
| `lang_pid` | - | 语种商品父ID | **需对齐**：PHP 列表需过滤 `lang_pid is null` 的主商品 |
| `is_hexiao` | 0-不支持, 1-支持 | (逐步弃用，改用 commodity_type) | 保持兼容 |

## 4. 逻辑对齐清单

### L1: 商品类型兼容性 (Commodity Type)
- **描述**：Java 侧为了支持虚拟商品核销，将 `commodity_type=1` 扩展为了 `1, 2, 3`。
- **现状**：PHP 侧列表查询仅匹配单一值。
- **对齐要求**：当查询 `commodity_type=1` (虚拟) 时，PHP 应匹配 `in (1, 2, 3)`。

### L2: 多语言商品过滤 (I18n Filtering)
- **描述**：Java 侧通过 `lang_pid` 关联多语言副本。主列表只显示 `lang_pid` 为空的记录。
- **现状**：PHP 侧可能直接查出了所有副本，导致列表重复。
- **对齐要求**：PHP 商品列表查询条件应默认包含 `and a.lang_pid is null`。

## 5. 改造执行记录 (M3-C1-A)
- [ ] 改造 `app/common/Product.php` 的 `get_product_list` 查询条件。
- [ ] 改造 `app/admin/controller/admin/Goods.php` (如有独立查询)。

---
*最后更新：2026-04-14*
