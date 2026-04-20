# M3-A1 用户域基线产物（P0）

更新时间：2026-04-07 13:58
范围：`lkt_user`、`lkt_user_address`、`lkt_record`

## 1. Gate 与环境实测

1. 强制前置 `svn update` 已执行：
   - `LaikeAPI`：`r37652`
   - `thinkinshop`：`r2902`
   - `svn status` 无冲突（无 `C`）
2. 数据库连通通过：
   - 本地：`127.0.0.1:3306/lkt_db`（root/123456）
   - 远端：`47.107.123.240:3386/v3_db`
3. 用户域三表结构比对：
   - 本地导出：`runtime/m3_a1_user_domain/local_columns.tsv`（90 行）
   - 远端导出：`runtime/m3_a1_user_domain/v3_columns.tsv`（90 行）
   - `cmp` 结果：`NO_DIFF`
4. 数据量抽样（本地 vs 远端）：
   - `lkt_user = 158`
   - `lkt_user_address = 102`
   - `lkt_record = 12706`
5. 服务冒烟：
   - `php think`：通过
   - `php -S 127.0.0.1:8000 -t public` + `curl /`：`200`
   - `mvn -pl laike-apis -am spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests`：
     - `GET /apis/`：`200`
     - `GET /apis/actuator/health`：`200`

## 2. 字段映射表（旧字段 -> 新来源 -> 兼容规则）

结论：当前 M3-A1 范围内三张核心表在 `lkt_db` 与 `v3_db` 为 **1:1 同构**，本批不需要字段改名，仅固化兼容规则。

### 2.1 `lkt_user`

| 旧字段（PHP） | 新来源（Java/v3） | 兼容规则 | 备注 |
|---|---|---|---|
| `id` | `lkt_user.id` | 自增主键，保持整型 | 主键不外露给 C 端 |
| `store_id` | `lkt_user.store_id` | 所有读写必须带店铺隔离条件 | 多租户强约束 |
| `user_id` | `lkt_user.user_id` | 业务主键，跨系统统一键 | 与 `id` 并存 |
| `access_id` | `lkt_user.access_id` | 登录态索引键，不改语义 | token 关联 |
| `user_name` | `lkt_user.user_name` | 允许空字符串，返回保持原字段名 | 昵称 |
| `wx_name` | `lkt_user.wx_name` | 保持原字段；绑定微信流程可覆盖 | 微信昵称 |
| `wx_id` | `lkt_user.wx_id` | 解绑时允许置空字符串 | 微信 openid |
| `mobile` | `lkt_user.mobile` | 与 `cpc+country_num` 联合校验唯一性 | 手机号 |
| `cpc` | `lkt_user.cpc` | 默认 `86`；入参为空按历史规则补默认 | 区号 |
| `country_num` | `lkt_user.country_num` | 默认 `156`；与 `cpc/mobile`联合校验 | 国家码 |
| `e_mail` | `lkt_user.e_mail` | 允许空；设置时需查重 | 邮箱 |
| `zhanghao` | `lkt_user.zhanghao` | 账号字段保留，不做迁移改名 | 登录账号 |
| `mima` | `lkt_user.mima` | 登录密码（历史）沿用 | 不改 API |
| `password` | `lkt_user.password` | 支付密码（MD5）沿用 | 与 `mima` 区分 |
| `money` | `lkt_user.money` | `decimal(12,2)`，统一两位小数语义 | 钱包余额 |
| `score` | `lkt_user.score` | 整型积分，保留历史逻辑 | 积分 |
| `grade` | `lkt_user.grade` | 会员等级枚举不变 | 会员域依赖 |
| `grade_end` | `lkt_user.grade_end` | 到期判断逻辑不变 | 会员有效期 |
| `is_out` | `lkt_user.is_out` | 0/1 语义不变 | 是否到期 |
| `source` | `lkt_user.source` | 来源码按既有字典 | 小程序/H5/APP 等 |
| `Register_data` | `lkt_user.Register_data` | 原字段名大小写保留 | 注册时间 |
| `last_time` | `lkt_user.last_time` | 登录统计逻辑不变 | 最后登录时间 |
| `is_default_value` | `lkt_user.is_default_value` | 默认值标记 1/2 语义不变 | 个人中心弹窗 |
| `is_lock` | `lkt_user.is_lock` | 冻结语义不变 | 登录拦截依赖 |

### 2.2 `lkt_user_address`

| 旧字段（PHP） | 新来源（Java/v3） | 兼容规则 | 备注 |
|---|---|---|---|
| `id` | `lkt_user_address.id` | 自增主键 | 地址主键 |
| `store_id` | `lkt_user_address.store_id` | 必须带店铺条件 | 多租户隔离 |
| `uid` | `lkt_user_address.uid` | 对应 `lkt_user.user_id` | 业务关联键 |
| `name` | `lkt_user_address.name` | 保持字段名 | 收货人 |
| `tel` | `lkt_user_address.tel` | 与 `cpc` 搭配展示 | 联系电话 |
| `cpc` | `lkt_user_address.cpc` | 默认 `86` | 区号 |
| `sheng` | `lkt_user_address.sheng` | 与历史中文省名/编码兼容 | 省 |
| `city` | `lkt_user_address.city` | 与历史值兼容 | 市 |
| `quyu` | `lkt_user_address.quyu` | 与历史值兼容 | 区/县 |
| `address` | `lkt_user_address.address` | 详细地址原样保留 | 明细地址 |
| `address_xq` | `lkt_user_address.address_xq` | 继续使用拼接地址 | 展示地址 |
| `code` | `lkt_user_address.code` | 为空时按 `0` 兼容 | 邮编 |
| `is_default` | `lkt_user_address.is_default` | 每用户仅 1 条默认地址 | 默认地址约束 |

### 2.3 `lkt_record`

| 旧字段（PHP） | 新来源（Java/v3） | 兼容规则 | 备注 |
|---|---|---|---|
| `id` | `lkt_record.id` | 自增主键 | 流水主键 |
| `store_id` | `lkt_record.store_id` | 必须按店铺过滤 | 多租户隔离 |
| `user_id` | `lkt_record.user_id` | 对应 `lkt_user.user_id` | 用户流水 |
| `money` | `lkt_record.money` | `decimal(12,2)`，两位小数 | 流水金额 |
| `oldmoney` | `lkt_record.oldmoney` | 记录变更前余额 | 对账依赖 |
| `add_date` | `lkt_record.add_date` | 时间字段语义不变 | 发生时间 |
| `event` | `lkt_record.event` | 文本保留 | 事件描述 |
| `type` | `lkt_record.type` | 枚举按 Java `RecordType` 对齐 | 关键字典字段 |
| `is_mch` | `lkt_record.is_mch` | 店铺提现标记语义不变 | 0/1 |
| `main_id` | `lkt_record.main_id` | 外键语义不变 | 插件场景使用 |
| `details_id` | `lkt_record.details_id` | 对应 `lkt_record_details.id` | 明细关联 |

补充：`type=42`（直播佣金转入）已在 Java `RecordModel.RecordType.LIVING_COMMISSION` 定义。

## 3. 用户域 SQL 读写点清单（核心）

说明：本节列“改造优先级最高”的核心入口。全量 grep 结果见：
- `runtime/m3_a1_user_domain/php_lkt_user_refs.txt`
- `runtime/m3_a1_user_domain/php_lkt_user_address_refs.txt`
- `runtime/m3_a1_user_domain/php_lkt_record_refs.txt`
- `runtime/m3_a1_user_domain/java_user_domain_refs.txt`

| 表 | 文件与行 | 影响接口(apiKey) | 读/写 | SQL片段/行为 |
|---|---|---|---|---|
| `lkt_user` | `app/admin/controller/app/User.php:157` | `app.user.index` | 读 | `select id from lkt_user ... mobile=...` 手机号冲突检查 |
| `lkt_user` | `app/admin/controller/app/User.php:166` | `app.user.index` | 写 | `UserModel->mobile` 同步写入 |
| `lkt_user` | `app/admin/controller/app/User.php:1732` | `app.user.set_password` | 写 | `update lkt_user set mima=...` |
| `lkt_user` | `app/admin/controller/app/User.php:1795` | `app.user.update_phone` | 读 | 账号/手机号查重 |
| `lkt_user` | `app/admin/controller/app/User.php:1807` | `app.user.update_phone` | 写 | 更新 `cpc/country_num/mobile/zhanghao` |
| `lkt_user` | `app/admin/controller/app/User.php:2080` | `app.user.set_user` | 读 | 邮箱查重 |
| `lkt_user` | `app/admin/controller/app/User.php:2091` | `app.user.set_user` | 写 | 更新邮箱 |
| `lkt_user` | `app/admin/controller/app/User.php:2651` | `app.user.synchronizeAccount` | 读 | 同手机号账号合并查询 |
| `lkt_user` | `app/admin/controller/app/User.php:2659` | `app.user.synchronizeAccount` | 写 | 账号合并更新 `wx_id/wx_name/headimgurl/access_id` |
| `lkt_user` | `app/admin/controller/app/User.php:2864` | `app.user.isDefaultValue` | 写 | 更新 `is_default_value=2` |
| `lkt_user` | `app/admin/controller/app/User.php:2964` | `app.user.bindWechat` | 写 | 绑定微信资料写入 |
| `lkt_user` | `app/admin/controller/app/User.php:2991` | `app.user.wxBind` | 写 | 小程序绑定微信 |
| `lkt_user` | `app/admin/controller/app/User.php:3013` | `app.user.wxUnbind` | 写 | 清空 `wx_id` |
| `lkt_user` | `app/admin/controller/admin/User.php:430` | `admin.user.saveUser` | 读 | 手机唯一性检查 |
| `lkt_user` | `app/admin/controller/admin/User.php:467` | `admin.user.saveUser` | 写 | 新增用户（`UserModel->save()`） |
| `lkt_user` | `app/admin/controller/admin/User.php:669` | `admin.user.updateUserById` | 读 | 更新前手机号查重 |
| `lkt_user` | `app/admin/controller/admin/User.php:712` | `admin.user.updateUserById` | 写 | 更新用户资料 |
| `lkt_user` | `app/admin/controller/admin/User.php:1963` | `admin.user.getUserMoneyInfo` | 读 | `count(*) from lkt_user` |
| `lkt_user` | `app/admin/controller/admin/User.php:1977` | `admin.user.getUserMoneyInfo` | 读 | `select * from lkt_user ...` |
| `lkt_user` | `app/admin/controller/admin/User.php:2703` | `admin.user.uploadAddUser` | 读 | 导入批次手机号查重 |
| `lkt_user` | `app/admin/controller/admin/User.php:2742` | `admin.user.uploadAddUser` | 写 | 批量 `UserModel` 插入 |
| `lkt_user` | `app/common/Order.php:618` | 钱包充值链路（`app.pay.*`） | 写 | `update user set money = money + ...` |
| `lkt_user` | `app/common/Plugin/Go_groupPublicMethod.php:2060` | 拼团结算链路 | 写 | `update lkt_user set money=...` |
| `lkt_user_address` | `app/admin/controller/app/Address.php:54` | `app.address.index` | 读 | 按 `uid` 查询地址列表 |
| `lkt_user_address` | `app/admin/controller/app/Address.php:112` | `app.address.set_default` | 写 | 先清零默认再置 1 |
| `lkt_user_address` | `app/admin/controller/app/Address.php:148` | `app.address.del_adds` | 写 | 删除地址并回补默认 |
| `lkt_user_address` | `app/admin/controller/app/Address.php:227` | `app.address.up_adds` | 写 | 修改地址（含默认逻辑） |
| `lkt_user_address` | `app/admin/controller/app/Address.php:305` | `app.address.up_addsindex` | 读 | 地址详情回显 |
| `lkt_user_address` | `app/admin/controller/app/Address.php:443` | `app.address.SaveAddress` | 写 | 新增地址（含默认逻辑） |
| `lkt_user_address` | `app/admin/controller/mall/Address.php:469` | `mall.address.delAdds` | 写 | `Db::table('lkt_user_address')->delete()` |
| `lkt_record` | `app/admin/controller/app/User.php:545` | `app.user.wallet_detailed` | 读 | `RecordModel` 按 type 分页查询 |
| `lkt_record` | `app/admin/controller/admin/User.php:1115` | `admin.user.getupInfo` | 读 | `lkt_record + lkt_record_details + lkt_user` 聚合 |
| `lkt_record` | `app/admin/controller/admin/User.php:2071` | `admin.user.getUserMoneyInfo_see` | 读 | 资金明细列表与统计 |
| `lkt_record` | `app/common/Order.php:628` | 钱包充值链路 | 写 | `insert into lkt_record ... type=1` |
| `lkt_record` | `app/common/Plugin/Go_groupPublicMethod.php:2072` | 拼团佣金结算链路 | 写 | `insert into lkt_record ... type=14` |
| `lkt_record` | `app/admin/controller/plugin/living/AppAnchor.php:1583` | 直播佣金转余额 | 写 | `Db::name('record')->insert(... type=42)` |
| `lkt_record` | `app/admin/controller/mch/Mch/Finance.php:768` | 店铺提现链路 | 写 | `Db::name('record')->insert(... type=2,is_mch=1)` |

## 4. 用户域接口清单（路径 + apiKey + 调用端）

> PHP 老接口路径模式：`/index.php?module={module}&action={action}&app={method}`（后台部分接口使用 `m` 参数）。

### 4.1 C 端用户/地址（P0）

| 接口路径（Java） | apiKey | PHP 对应方法 | 调用端 |
|---|---|---|---|
| `/app/user/base/userIndex` | `app.user.index` | `app/User::index` | C端（小程序/H5/APP） |
| `/app/user/base/set` | `app.user.set` | `app/User::set` | C端 |
| `/app/user/base/updatepassword` | `app.user.updatepassword` | `app/User::updatepassword` | C端 |
| `/app/user/base/set_password` | `app.user.set_password` | `app/User::set_password` | C端 |
| `/app/user/base/update_phone` | `app.user.update_phone` | `app/User::update_phone` | C端 |
| `/app/user/base/setUser` | `app.user.set_user` | `app/User::set_user` | C端 |
| `/app/user/base/paymentPassword` | `app.user.payment_password` | `app/User::payment_password` | C端 |
| `/app/user/base/wallet_detailed` | `app.user.wallet_detailed` | `app/User::wallet_detailed` | C端 |
| `/app/user/base/getRecordDetails` | `app.user.getRecordDetails` | `app/User::getRecordDetails` | C端 |
| `/app/user/base/synchronizeAccount` | `app.user.synchronizeAccount` | `app/User::synchronizeAccount` | C端 |
| `/app/user/base/wxBind` | `app.user.wxBind` | `app/User::wxBind` | C端 |
| `/app/user/base/wxUnbind` | `app.user.wxUnbind` | `app/User::wxUnbind` | C端 |
| `/app/user/address/addressIndex` | `app.address.index` | `app/Address::index` | C端 |
| `/app/user/address/saveAddress` | `app.address.SaveAddress` | `app/Address::SaveAddress` | C端 |
| `/app/user/address/up_adds` | `app.address.up_adds` | `app/Address::up_adds` | C端 |
| `/app/user/address/del_adds` | `app.address.del_adds` | `app/Address::del_adds` | C端 |
| `/app/user/address/set_default` | `app.address.set_default` | `app/Address::set_default` | C端 |

### 4.2 管理端用户/资金

| 接口路径（Java） | apiKey | PHP 对应方法 | 调用端 |
|---|---|---|---|
| `/admin/user/getUserInfo` | `admin.user.getUserInfo` | `admin/User::getUserInfo` | 平台后台 |
| `/admin/user/saveUser` | `admin.user.saveUser` | `admin/User::saveUser` | 平台后台 |
| `/admin/user/updateUserById` | `admin.user.updateUserById` | `admin/User::updateUserById` | 平台后台 |
| `/admin/user/delUserById` | `admin.user.delUserById` | `admin/User::delUserById` | 平台后台 |
| `/admin/user/getUserMoneyInfo` | `admin.user.getUserMoneyInfo` | `admin/User::getUserMoneyInfo` | 平台后台 |
| `/admin/user/getUserMoneyInfo_see` | `admin.user.getUserMoneyInfo_see` | `admin/User::getUserMoneyInfo_see` | 平台后台 |
| `/admin/user/getupInfo` | `admin.user.getupInfo` | `admin/User::getupInfo` | 平台后台 |
| `/admin/user/uploadAddUser` | `admin.user.uploadAddUser` | `admin/User::uploadAddUser` | 平台后台 |
| `/admin/user/uploadRecordList` | `admin.user.uploadRecordList` | `admin/User::uploadRecordList` | 平台后台 |
| `/admin/user/delUploadRecord` | `admin.user.delUploadRecord` | `admin/User::delUploadRecord` | 平台后台 |

## 5. 用户域回归清单（登录/资料/地址）

| 用例 | 接口 | 校验点 | 状态 |
|---|---|---|---|
| 登录态读取 | `app.user.index` | `user_id/mobile/cpc/country_num/source` 返回完整且语义不变 | 待执行 |
| 修改手机号 | `app.user.update_phone` | 账号查重与 `cpc/country_num/mobile` 更新正确 | 待执行 |
| 修改邮箱 | `app.user.set_user` | 邮箱查重、写入成功、不影响登录态 | 待执行 |
| 设置/验证支付密码 | `app.user.set_payment_password` + `app.user.payment_password` | 写入与验证一致 | 待执行 |
| 地址新增 | `app.address.SaveAddress` | 首条地址自动默认；`address_xq` 正确 | 待执行 |
| 地址修改 | `app.address.up_adds` | 默认地址切换规则正确 | 待执行 |
| 地址删除 | `app.address.del_adds` | 删除默认地址后自动回补默认 | 待执行 |
| 地址列表 | `app.address.index` | `adds/adds0` 与不配送逻辑一致 | 待执行 |
| 管理端会员列表 | `admin.user.getUserInfo` | 关键字段与过滤条件正确 | 待执行 |
| 管理端资金明细 | `admin.user.getUserMoneyInfo_see` | 入/出账分类与金额汇总正确 | 待执行 |

## 6. 当前风险与后续改造建议

1. 风险：`lkt_user` 在插件/订单等跨域 JOIN 点非常多，M3-A2 改造必须先冻结“用户域改造边界”，避免一次触发订单域回归。
2. 风险：`app/User::set_password` 同时存在原生 SQL 与 `UserModel->save()` 双写，建议在 M3-A2 收敛为单写路径。
3. 风险：地址能力存在 `app/Address` 与 `mall/Address` 双实现，建议统一保留一套服务层口径。
4. 建议：M3-A2 从“用户资料与地址”先行，不直接动资金流水写入链路（`lkt_record`）。

## 7. 本批产物文件

1. 本文档：`M3_A1_USER_DOMAIN_BASELINE.md`
2. 结构导出：
   - `runtime/m3_a1_user_domain/local_columns.tsv`
   - `runtime/m3_a1_user_domain/v3_columns.tsv`
3. 扫描清单：
   - `runtime/m3_a1_user_domain/php_lkt_user_refs.txt`
   - `runtime/m3_a1_user_domain/php_lkt_user_address_refs.txt`
   - `runtime/m3_a1_user_domain/php_lkt_record_refs.txt`
   - `runtime/m3_a1_user_domain/java_user_domain_refs.txt`
