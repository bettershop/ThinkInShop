# M3-A2 地址双实现收敛方案（批次1-3）

更新时间：2026-04-08 11:20
范围：`app/admin/controller/app/Address.php` 与 `app/admin/controller/mall/Address.php`

## 0. 状态更正（2026-04-08）

1. 根据最新迁移约束：同库对齐不允许新增 service 层。
2. 本批次中新增的 `AddressDomainService/AddressWriteService` 已整体回退。
3. 代码已恢复为控制器原实现：
   - `app/admin/controller/app/Address.php`（已回退）
   - `app/admin/controller/mall/Address.php`（已回退）
4. 后续 Address 域仅在“字段或返回结构存在差异”时做最小修改；无差异不改动。

## 1. 背景与问题

当前存在两套地址实现：
1. C端：`app/Address`（下划线风格方法）
2. 商城端：`mall/Address`（驼峰风格方法）

两套代码在以下能力上长期重复：
1. 省市区级联查询
2. `place` 解析与省市区归一化
3. `address_xq` 组装
4. 省市区名称与行政区ID互转

风险：
1. 规则不一致会导致同一用户在不同端地址数据表现不一致。
2. 修改一端容易漏改另一端，出现回归。

## 2. 收敛目标（M3-A2）

1. 在不改接口契约（路径/参数/返回字段）的前提下，抽离公共地址域能力。
2. 先收敛“纯公共逻辑”，再收敛“CRUD流程与返回结构”。
3. 保持 `lkt_user_address` 语义不变，仅做实现层统一。

## 3. 分批实施

### 批次 1（本次已完成）

收敛内容：
1. 新增公共服务：`app/common/AddressDomainService.php`
2. 统一“地区级联”能力：
   - `getCascaderRegionList()`
   - `getRegionListByPid()`
3. 统一“地址归一化”能力：
   - `normalizeAreaByPlaceIds()`
   - `normalizeAreaByCpc()`
   - `buildAddressDetail()`（统一 `address_xq`）
4. 统一“名称/ID路径转换”能力：
   - `getRegionIdPathByNames()`
5. 双控制器接入公共服务：
   - `app/Address`：地区查询与地址归一化切换至公共服务
   - `mall/Address`：地区级联、place解析、地址详情、名称转ID切换至公共服务

不变项（刻意保留）：
1. 现有接口方法名与返回结构不变。
2. 各端各自 CRUD 事务与返回体暂不合并（避免一次改动面过大）。

### 批次 2（本次已完成）

目标：收敛 CRUD 核心流程。

完成内容：
1. 新增公共写服务：`app/common/AddressWriteService.php`
2. 抽离 `save/update/delete/setDefault` 四类写操作：
   - `createAddress()`
   - `updateAddress()`
   - `setDefaultAddress()`
   - `deleteAddress()`
3. 双控制器改为“参数适配 + 返回封装”：
   - `app/Address`：`set_default/del_adds/up_adds/SaveAddress`
   - `mall/Address`：`saveAddress/upAdds/setDefault/delAdds`
4. 保留端侧差异策略（通过服务参数控制）：
   - 清默认是否强校验
   - 删除默认地址是否必须有替补默认地址
   - 新默认地址选择策略（`latest/first`）

### 批次 3（已完成，收口）

目标：完成回归基线收口，并严格遵循“非必要不改逻辑”原则。

完成内容：
1. 明确收口策略：
   - 不做读模型重构，不改既有业务分支逻辑。
   - 仅在“同名表字段差异影响兼容”时做最小改动。
2. 完成接口级回归脚本增强：
   - `scripts/address_gateway_smoke.sh` 升级为“统一账号登录态回测”。
   - 默认账号：`000000/000000`。
   - 回测覆盖：`mall/app` 登录、地址列表、省市区查询、地址编辑页读取。
3. 回测结论：
   - `mall.Login.login=200`
   - `app.login.login=200`
   - `mall.Address.addressManagement/index/upAddsindex=200`
   - `app.Address.AddressManagement/index/getCityArr/up_addsindex=200`
   - `app.Address.getCountyInfo=109`（历史兼容返回码，保持不变）

未执行项（按你要求刻意不做）：
1. 不推进地址读逻辑重构。
2. 不改变 `app/Address` 与 `mall/Address` 现有读接口行为。

## 4. 改造清单（代码）

1. 新增：`app/common/AddressDomainService.php`
2. 新增：`app/common/AddressWriteService.php`
3. 修改：`app/admin/controller/app/Address.php`
4. 修改：`app/admin/controller/mall/Address.php`

## 5. 验证记录（本批）

1. 语法检查通过：
   - `php -l app/common/AddressDomainService.php`
   - `php -l app/common/AddressWriteService.php`
   - `php -l app/admin/controller/app/Address.php`
   - `php -l app/admin/controller/mall/Address.php`
2. ThinkPHP 命令可用：`php think route:list` 正常返回。
3. 网关接口回测通过：`bash scripts/address_gateway_smoke.sh`。

## 6. 风险与注意事项

1. `app/Address::getCountyInfo()` 历史返回码为 `109`（非 `200`），本批保持不变，避免前端回归。
2. `mall/Address` 与 `app/Address` 返回体字段不同（如 `adds/adds0`、是否回传最新地址列表），本批不强行统一。
3. 已按“最小改动”原则收口，后续继续迁移时优先走“字段映射驱动”，避免实现层大改。
