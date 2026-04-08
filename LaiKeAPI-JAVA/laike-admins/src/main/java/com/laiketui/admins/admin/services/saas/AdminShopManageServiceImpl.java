package com.laiketui.admins.admin.services.saas;

import com.laiketui.admins.api.admin.saas.AdminCurrencyStoreService;
import com.laiketui.admins.api.admin.saas.AdminShopManageService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.LangModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.mch.RoleModel;
import com.laiketui.domain.payment.PaymentConfigModel;
import com.laiketui.domain.payment.PaymentModel;
import com.laiketui.domain.systems.SystemConfigurationModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.saas.AddShopVo;
import com.laiketui.root.license.Md5Util;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户管理
 *
 * @author Trick
 * @date 2021/1/28 9:20
 */
@Service
public class AdminShopManageServiceImpl implements AdminShopManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminShopManageServiceImpl.class);

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SystemConfigurationModelMapper systemConfigurationModelMapper;

    private static final String SPECIAL_CHARS_REGEX = "[!\"#$%&'()*+,-./:;<=>?@\\[\\\\^_`{|}~]";

    @Override
    public Map<String, Object> getShopInfo(MainVo vo, String storeName, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            // 获取商城ID前缀逻辑
            String storeIdPrefix = "";
            SystemConfigurationModel systemConfiguration = new SystemConfigurationModel();
            systemConfiguration.setStore_id(AdminModel.PLATFORM_STORE_ID);
            systemConfiguration = systemConfigurationModelMapper.selectOne(systemConfiguration);
            boolean hasStoreIdPrefix = !(StringUtils.isEmpty(systemConfiguration) || StringUtils.isEmpty(systemConfiguration.getStore_id_prefix()));
            if (hasStoreIdPrefix)
            {
                storeIdPrefix = systemConfiguration.getStore_id_prefix();
            }
            resultMap.put("storeIdPrefix", hasStoreIdPrefix);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            if (vo.getStoreId() != 0)
            {
                parmaMap.put("store_id", vo.getStoreId());
            }
            if (!StringUtils.isEmpty(storeName))
            {
                parmaMap.put("store_name", storeName);
            }
            if (!StringUtils.isEmpty(startDate))
            {
                parmaMap.put("startDate", startDate);
                if (!StringUtils.isEmpty(endDate))
                {
                    parmaMap.put("endDate", endDate);
                }
            }
            // >>> 修正：使用原始排序字段
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            // <<<
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int total = customerModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> dataList = customerModelMapper.selectDynamic(parmaMap);

            if (dataList != null && !dataList.isEmpty())
            {
                // ======== 批量预加载（优化核心）========
                Set<Integer> storeIds = new HashSet<>();
                Set<Integer> adminIds = new HashSet<>();
                for (Map<String, Object> map : dataList)
                {
                    Integer id      = MapUtils.getInteger(map, "id");
                    Integer adminId = MapUtils.getInteger(map, "admin_id");
                    if (id != null) storeIds.add(id);
                    if (adminId != null) adminIds.add(adminId);
                }

                // 批量查 Config
                Map<Integer, ConfigModel> configMap = new HashMap<>();
                if (!storeIds.isEmpty())
                {
                    List<ConfigModel> configs = configModelMapper.selectByStoreIds(new ArrayList<>(storeIds));
                    for (ConfigModel c : configs)
                    {
                        configMap.put(c.getStore_id(), c);
                    }
                }

                // 批量查 Admin
                Map<Integer, AdminModel> adminMap = new HashMap<>();
                if (!adminIds.isEmpty())
                {
                    List<AdminModel> adminList = adminModelMapper.selectByIds(new ArrayList<>(adminIds));
                    for (AdminModel a : adminList)
                    {
                        adminMap.put(a.getId(), a);
                    }
                }

                // 批量查 Role —— 直接从 adminMap 取，避免作用域问题
                Set<String> roleIds = adminMap.values().stream()
                        .map(AdminModel::getRole)
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.toSet());

                Map<String, RoleModel> roleMap = new HashMap<>();
                if (!roleIds.isEmpty())
                {
                    List<RoleModel> roles = roleModelMapper.selectByIds(new ArrayList<>(roleIds));
                    for (RoleModel r : roles)
                    {
                        roleMap.put(r.getId().toString(), r);
                    }
                }

                // 批量查 CurrencyStore（所有）
                Map<Integer, List<CurrencyStoreModel>> currencyStoreMap = new HashMap<>();
                if (!storeIds.isEmpty())
                {
                    List<CurrencyStoreModel> currencyStores = currencyStoreModelMapper.selectByStoreIds(new ArrayList<>(storeIds));
                    for (CurrencyStoreModel cs : currencyStores)
                    {
                        currencyStoreMap.computeIfAbsent(cs.getStore_id(), k -> new ArrayList<>()).add(cs);
                    }
                }

                // 批量查默认币种（模拟原始 getDefaultCurrency）
                Map<Integer, CurrencyStoreModel> defaultCurrencyInfoMap = new HashMap<>();
                if (!storeIds.isEmpty())
                {
                    List<CurrencyStoreModel> defaults = currencyStoreModelMapper.selectDefaultCurrencies(new ArrayList<>(storeIds));
                    for (CurrencyStoreModel d : defaults)
                    {
                        defaultCurrencyInfoMap.put(d.getStore_id(), d);
                    }
                }
                // ======== 批量加载结束 ========

                for (Map<String, Object> map : dataList)
                {
                    Integer id         = MapUtils.getInteger(map, "id");
                    Integer adminId    = MapUtils.getInteger(map, "admin_id");
                    Date    expireDate = DateUtil.dateFormateToDate(MapUtils.getString(map, "end_date"), GloabConst.TimePattern.YMDHMS);
                    if (expireDate != null)
                    {
                        storeExpireHandle(id, expireDate);
                    }

                    int    status     = MapUtils.getIntValue(map, "status");
                    String statusName = "生效中";
                    if (status == 1) statusName = "已过期";
                    else if (status == 2) statusName = "已锁定";
                    map.put("statusName", statusName);

                    // 币种处理
                    List<CurrencyStoreModel> currencyStores = currencyStoreMap.get(id);
                    if (currencyStores != null)
                    {
                        StringJoiner curs            = new StringJoiner(SplitUtils.DH);
                        Integer      defaultCurrency = null;
                        for (CurrencyStoreModel cs : currencyStores)
                        {
                            curs.add(cs.getCurrency_id().toString());
                            if (cs.getDefault_currency() == 1)
                            {
                                defaultCurrency = cs.getCurrency_id();
                            }
                        }
                        map.put("store_currencys", curs.toString());
                        if (defaultCurrency != null)
                        {
                            map.put("default_currency", defaultCurrency);
                        }
                    }

                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    map.put("end_date", DateUtil.dateFormate(MapUtils.getString(map, "end_date"), GloabConst.TimePattern.YMDHMS));

                    // Config 信息
                    ConfigModel config      = configMap.get(id);
                    String      domain      = config != null ? config.getDomain() : "";
                    String      defaultLang = GloabConst.Lang.CN;
                    if (config != null && StringUtils.isNotEmpty(config.getDefault_lang_code()))
                    {
                        defaultLang = config.getDefault_lang_code();
                    }
                    map.put("storeDomain", domain);
                    map.put("default_lang_code", defaultLang);

                    // Admin 信息
                    AdminModel admin = adminMap.get(adminId);
                    if (admin != null)
                    {
                        String roleName = "";
                        if (StringUtils.isNotEmpty(admin.getRole()))
                        {
                            RoleModel role = roleMap.get(admin.getRole());
                            if (role != null) roleName = role.getName();
                        }
                        if (StringUtils.isNotEmpty(admin.getLang()))
                        {
                            map.put("default_lang_code", admin.getLang()); // 覆盖
                        }
                        map.put("roleName", roleName);
                        map.put("adminName", admin.getName());
                        map.put("roleId", admin.getRole());
                        map.put("portrait", admin.getPortrait());
                    }

                    // >>> 关键：设置前缀和默认币种详情
                    map.put("storeIdPrefix", storeIdPrefix);
                    map.put("storeDefaultCurrencyInfo", defaultCurrencyInfoMap.get(id));
                    // <<<
                }
            }

            if (vo.getExportType() != null && vo.getExportType().equals(1))
            {
                exportStoreData(dataList, response);
                return null;
            }

            resultMap.put("total", total);
            resultMap.put("dataList", dataList != null ? dataList : new ArrayList<>());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商城列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getShopInfo");
        }
        return resultMap;
    }

    private void exportStoreData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            list.forEach(map ->
            {
                map.put("id", MapUtils.getString(map, "storeIdPrefix") + MapUtils.getInteger(map, "id"));
                Integer is_default = MapUtils.getInteger(map, "is_default");
                if (is_default == 1)
                {
                    map.put("is_default", "是");
                }
                else
                {
                    map.put("is_default", "否");
                }

            });
            //表头
            String[] headerList = new String[]{"商城ID", "商城名称", "角色", "手机", "价格", "公司名称", "购买时间", "到期时间", "状态", "是否默认"};
            //对应字段
            String[]     kayList = new String[]{"id", "name", "roleName", "mobile", "price", "company", "add_date", "end_date", "statusName", "is_default"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("商户列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(false);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    /**
     * 商城过期处理
     *
     * @param storeId -
     * @param endDate - 过期时间
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 10:09
     */
    private void storeExpireHandle(int storeId, Date endDate) throws LaiKeAPIException
    {
        try
        {
            boolean       isExpire      = false;
            CustomerModel customerModel = new CustomerModel();
            customerModel.setId(storeId);
            //过期延期7天,超过7天则回收商城
            Date delayedTime = DateUtil.getAddDate(endDate, 7);
            //判断是否过期
            if (DateUtil.dateCompare(new Date(), endDate))
            {
                //标记过期
                isExpire = true;
                customerModel.setStatus(CustomerModel.CUSTOMER_STATUS_EXPIRE);
                logger.debug("商城id={}已过期", storeId);
            }
            //判断是否需要回收商城
            if (DateUtil.dateCompare(new Date(), delayedTime))
            {
                //标记过期
                customerModel.setStatus(CustomerModel.CUSTOMER_STATUS_EXPIRE);
                logger.debug("商城id={}超时未缴费,回收商城", storeId);
            }
            if (isExpire)
            {
                int count = customerModelMapper.updateByPrimaryKeySelective(customerModel);
                logger.debug("商城已过期 处理结果{}", count > 0);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("商城过期处理 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeExpireHandle");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean setStoreOpenSwitch(MainVo vo, Integer storeId) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            //获取商城信息
            CustomerModel customerModel = new CustomerModel();
            customerModel.setId(storeId);
            customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            customerModel = customerModelMapper.selectOne(customerModel);
            if (customerModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCBCZ, "商城不存在");
            }
            CustomerModel customerModelUpdate = new CustomerModel();
            customerModelUpdate.setId(customerModel.getId());
            if (CustomerModel.CUSTOMER_STATUS_OPEN.equals(customerModel.getStatus()))
            {
                logger.debug("正在锁定商城id{}", storeId);
                //锁定商城
                customerModelUpdate.setStatus(CustomerModel.CUSTOMER_STATUS_LOCK);
                //把商城管理员都踢下线
                publicAdminService.outLoginStoreAdminAll(customerModel.getId());
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "将商城ID：" + storeId + "进行了锁定操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else if (CustomerModel.CUSTOMER_STATUS_LOCK.equals(customerModel.getStatus()) || CustomerModel.CUSTOMER_STATUS_EXPIRE.equals(customerModel.getStatus()))
            {
                if (DateUtil.dateCompare(new Date(), customerModel.getEnd_date()))
                {
                    logger.debug("商户已到期,请续费再启用");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_GSHSYSJYDQ, "该商户使用时间已到期，请先修改到期时间再启用!");
                }
                logger.debug("正在启用商城id{}", storeId);

                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "将商城ID：" + storeId + "进行了启用操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                //启用商城
                customerModelUpdate.setStatus(CustomerModel.CUSTOMER_STATUS_OPEN);
                //恢复管理员
                adminModelMapper.reductionAdmin(storeId);
            }
            count = customerModelMapper.updateByPrimaryKeySelective(customerModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("是否启用开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setStoreOpenSwitch");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setStoreDefaultSwitch(MainVo vo, Integer store_id) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminUser = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取商城信息
            CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(store_id);
            if (Objects.isNull(customerModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCBCZ, "商城不存在");
            }
            //删除之前默认商城
            customerModelMapper.removeDefault();

            int i = customerModelMapper.setDefaultById(customerModel.getId());
            if (i == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将商城ID：" + store_id + "设为默认", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置默认商城 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setStoreDefaultSwitch");
        }
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    @Override
    public boolean addStore(AddShopVo vo, String ip) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminUser = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            if (StringUtils.isEmpty(vo.getMobile()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机号不能为空");
            }
            /*else if (!MobileUtils.isMobile(vo.getMobile()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机号格式不正确");
            }*/
            if (vo.getPrice() == null || vo.getPrice().doubleValue() < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCJGBNWK, "商城价格不能为空");
            }
            if (StringUtils.isNotEmpty(vo.getEmail()))
            {
                //校验邮箱格式
                DataCheckTool.checkEmail(vo.getEmail());
            }
            if (StringUtils.isEmpty(vo.getEndDate()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DQSJBNWK, "到期时间不能为空");
            }
            else if (DateUtil.dateCompare(new Date(), DateUtil.dateFormateToDate(vo.getEndDate(), GloabConst.TimePattern.YMDHMS)))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DQSJBNXYDQSJ, "到期时间不能小于当前时间");
            }
            if (vo.getIsOpen() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SFQYBNWK, "是否启用不能为空");
            }
            if (StringUtils.isNotEmpty(vo.getAdminPwd()) && !DataCheckTool.checkLength(vo.getAdminPwd(), 6, 16))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMBFHGF, "密码不符合6-16位的长度规范");
            }

            CustomerModel customerModelOld = null;
            if (vo.getId() != null)
            {
                customerModelOld = new CustomerModel();
                customerModelOld.setId(vo.getId());
                customerModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                customerModelOld = customerModelMapper.selectOne(customerModelOld);
                if (customerModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCBCZ, "商城不存在");
                }
            }
            CustomerModel customerModelSave = new CustomerModel();
            if (StringUtils.isEmpty(vo.getStore_langs()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZYZ, "请选择语种");
            }


            String langCodes = vo.getStore_langs();
            customerModelSave.setStore_langs(langCodes);
            customerModelSave.setName(vo.getStoreName());
            customerModelSave.setCustomer_number(vo.getStoreNo());
            customerModelSave.setCompany(vo.getCompany());
            customerModelSave.setMobile(vo.getMobile());
            customerModelSave.setPrice(vo.getPrice());
            customerModelSave.setCpc(vo.getCpc());
            customerModelSave.setEmail(vo.getEmail());
            customerModelSave.setEnd_date(DateUtil.dateFormateToDate(vo.getEndDate(), GloabConst.TimePattern.YMDHMS));
            int status = vo.getIsOpen();
            if (!DateUtil.dateCompare(customerModelSave.getEnd_date(), new Date()))
            {
                status = CustomerModel.CUSTOMER_STATUS_EXPIRE;
            }
            else
            {
                //没有过期则启用商城
                if (!CustomerModel.CUSTOMER_STATUS_LOCK.equals(status))
                {
                    status = CustomerModel.CUSTOMER_STATUS_OPEN;
                }
            }
            customerModelSave.setStatus(status);
            //新增增段 2021-06-08 11:07:39
            customerModelSave.setContact_address(vo.getContactAddress());
            customerModelSave.setContact_number(vo.getContactNumber());
            customerModelSave.setCopyright_information(vo.getCopyrightInformation());
            customerModelSave.setRecord_information(vo.getRecordInformation());
            customerModelSave.setOfficial_website(vo.getWebsite());
            customerModelSave.setMerchant_logo(vo.getLogUrl());
            //end
            if (StringUtils.isEmpty(vo.getStoreNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KHBHBNWK, "商城编号不能为空");
            }
            if (!vo.getStoreNo().trim().matches("[0-9a-zA-Z]{1,}"))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "商城编号只能为数字和字母组成");
            }
            if (StringUtils.isEmpty(vo.getCompany()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSMCBNWK, "公司名称不能为空");
            }
            if (StringUtils.isEmpty(vo.getAdminAccount()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYZHBNWK, "管理员账号不能为空");
            }
            if (!StringUtils.isEmpty(vo.getAdminAccount()))
            {
                if (vo.getAdminAccount().matches(".*" + SPECIAL_CHARS_REGEX + ".*"))
                {
                    throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE,"商城账号格式错误，不支持输入特殊符号");
                }
                if (!DataCheckTool.checkNumAndLetter(vo.getAdminAccount(), 6, 12))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "商城账号长度为 6-12");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ILLEGAL_FAIL, "商城账号不能为空");
            }
            if (StringUtils.isEmpty(vo.getStoreName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXKHMC, "请填写商城名称");
            }

            CustomerModel customerCount = new CustomerModel();
            if (customerModelOld == null || !customerModelOld.getCustomer_number().equals(vo.getStoreNo()))
            {
                customerCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                customerCount.setCustomer_number(vo.getStoreNo());
                count = customerModelMapper.selectCount(customerCount);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KHBHYCZ, "商城编号已存在");
                }
            }

            if (customerModelOld == null)
            {

                if (StringUtils.isNotEmpty(vo.getEmail()))
                {
                    customerCount = new CustomerModel();
                    customerCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    customerCount.setEmail(vo.getEmail());
                    count = customerModelMapper.selectCount(customerCount);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXYCZ, "邮箱已存在");
                    }
                }
                customerCount = new CustomerModel();
                customerCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                customerCount.setName(vo.getStoreName());
                count = customerModelMapper.selectCount(customerCount);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCMCCZ, "商城名称已存在");
                }
                if (StringUtils.isEmpty(vo.getAdminPwd()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYMMBNWK, "商城密码不能为空");
                }
                if (vo.getRoleId() == null || vo.getRoleId() == 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZJSQX, "请选择角色权限");
                }
                String password = Md5Util.MD5endoce(vo.getAdminPwd());

                customerModelSave.setAdmin_id(0);
                customerModelSave.setFunction("2");
                customerModelSave.setAdd_date(new Date());
                //添加管理员
                AdminModel adminModel = new AdminModel();
                adminModel.setName(vo.getAdminAccount());
                adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                count = adminModelMapper.selectCount(adminModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYZHYCZ, "商城账号已存在");
                }
                CustomerModel customerModel = new CustomerModel();
                customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                int selectCount = customerModelMapper.selectCount(customerModel);
                //如果是第一个商城，则默认商城
                if (selectCount == 0)
                {
                    customerModelSave.setIs_default(1);
                }
                //添加商城
                count = customerModelMapper.insertSelective(customerModelSave);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCTJSB, "商城添加失败");
                }

                //添加操作日志
                publiceService.addAdminRecord(customerModelSave.getId(), "添加了商城ID：" + customerModelSave.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
                //清除商城缓存
                //redisUtil.del(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST);
                AdminModel adminModelSave = new AdminModel();
                adminModelSave.setPassword(vo.getAdminPwd());
                adminModelSave.setType(AdminModel.TYPE_CLIENT);
                adminModelSave.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                adminModelSave.setStore_id(customerModelSave.getId());
                adminModelSave.setCpc(vo.getCpc());
                adminModelSave.setTel(vo.getMobile());
                adminModelSave.setName(vo.getAdminAccount());
                adminModelSave.setRole(vo.getRoleId() + "");
                adminModelSave.setIp(ip);
                adminModelSave.setAdd_date(new Date());

                //默认选择第一个语种作为管理员的语言
                adminModelSave.setLang(vo.getDefault_lang());

                //校验数据
//                adminModelSave = DataCheckTool.checkAdminDataFormate(adminModelSave, false);
                adminModelSave.setPassword(password);
                //添加默认头像
                SystemConfigurationModel systemConfiguration = new SystemConfigurationModel();
                systemConfiguration.setStore_id(customerModelSave.getId());
                systemConfiguration.setAdminDefaultPortrait(vo.getAdminDefaultPortrait());
                systemConfiguration.setAdd_time(new Date());
                systemConfigurationModelMapper.insertSelective(systemConfiguration);
                adminModelSave.setPortrait(vo.getAdminDefaultPortrait());
                count = adminModelMapper.insertSelective(adminModelSave);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYTJSB, "管理员添加失败");
                }
                //修改商城管理员
                CustomerModel customerModelTemp = new CustomerModel();
                customerModelTemp.setId(customerModelSave.getId());
                customerModelTemp.setAdmin_id(adminModelSave.getId());
                count = customerModelMapper.updateByPrimaryKeySelective(customerModelTemp);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCGLYTJSB, "商城管理员添加失败");
                }

                //获取支付方式列表
                List<PaymentModel> paymentModelList = paymentModelMapper.selectAll();
                for (PaymentModel paymentModel : paymentModelList)
                {
                    //添加支付方式
                    PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
                    paymentConfigModel.setStore_id(customerModelSave.getId());
                    paymentConfigModel.setPid(paymentModel.getId());
                    paymentConfigModel.setStatus(paymentModel.getStatus());
                    count = paymentConfigModelMapper.insertSelective(paymentConfigModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSTJSB, "支付方式添加失败");
                    }
                }
                //
            }
            else
            {
                if (StringUtils.isNotEmpty(vo.getEmail()) && !customerModelOld.getEmail().equals(vo.getEmail()))
                {
                    customerCount = new CustomerModel();
                    customerCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    customerCount.setEmail(vo.getEmail());
                    count = customerModelMapper.selectCount(customerCount);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXYCZ, "邮箱已存在");
                    }
                }

                if (!customerModelOld.getName().equals(vo.getStoreName()))
                {
                    customerCount = new CustomerModel();
                    customerCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    customerCount.setName(vo.getStoreName());
                    count = customerModelMapper.selectCount(customerCount);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KHMCYCZ, "商城名称已存在");
                    }
                }
                //修改管理员账号/密码
                AdminModel adminModelOld = adminModelMapper.selectByPrimaryKey(customerModelOld.getAdmin_id());
                adminModelOld = adminModelMapper.selectOne(adminModelOld);
                if (adminModelOld != null)
                {
                    AdminModel adminModelUpdate = new AdminModel();
                    adminModelUpdate.setId(adminModelOld.getId());
                    if (!vo.getAdminAccount().equals(adminModelOld.getName()))
                    {
                        AdminModel adminModel = new AdminModel();
                        adminModel.setName(vo.getAdminAccount());
                        adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        count = adminModelMapper.selectCount(adminModel);
                        if (count > 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYZHYCZ, "管理员账号已存在");
                        }
                        adminModelUpdate.setName(vo.getAdminAccount());
                    }
                    if (StringUtils.isEmpty(adminModelOld.getTel()) || adminModelOld.getTel().equals(vo.getMobile()))
                    {
                        adminModelUpdate.setTel(vo.getMobile());
                    }
                    if (CustomerModel.CUSTOMER_STATUS_LOCK.equals(vo.getIsOpen()))
                    {
                        adminModelUpdate.setStatus(CustomerModel.CUSTOMER_STATUS_LOCK);
                    }
                    adminModelUpdate.setIp(ip);
                    adminModelUpdate = DataCheckTool.checkAdminDataFormate(adminModelUpdate, true);
                    if (StringUtils.isNotEmpty(vo.getAdminPwd()))
                    {
                        String pwd = Md5Util.MD5endoce(vo.getAdminPwd());
                        if (!adminModelOld.getPassword().equals(pwd))
                        {
                            adminModelUpdate.setPassword(pwd);
                        }
                    }
                    count = adminModelMapper.updateByPrimaryKeySelective(adminModelUpdate);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                    }
                    //修改编号、账号或密码都会使管理员重新登陆
                    if (!vo.getStoreNo().equals(customerModelOld.getCustomer_number()) || !adminModelOld.getName().equals(vo.getAdminAccount()) || !adminModelOld.getPassword().equals(Md5Util.MD5endoce(vo.getAdminPwd())))
                    {
                        //获取所有商城管理员id
                        List<Integer> adminIdList = adminModelMapper.getAdminIdList(vo.getId());
                        for (Integer id : adminIdList)
                        {
                            String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + id;
                            String tokenOld = redisUtil.get(logKey) + "";
                            if (StringUtils.isNotEmpty(tokenOld))
                            {
                                //踢人
                                logger.info("adminId:{}强制退出系统", id);
                                redisUtil.del(logKey);
                                redisUtil.del(tokenOld);
                            }
                        }
                    }

                }
                customerModelSave.setId(customerModelOld.getId());
                count = customerModelMapper.updateByPrimaryKeySelective(customerModelSave);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCXGSB, "商城修改失败");
                }
                //添加操作日志
                publiceService.addAdminRecord(customerModelSave.getId(), "修改了商城ID：" + customerModelSave.getId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }

            //商城币种绑定和默认币种设置【有默认币种则不设置；没有默认币种则设置。】[1,2]
            bindingStoreCurrencys(vo, customerModelSave);

            //添加系统设置
            ConfigModel configOld = new ConfigModel();
            configOld.setStore_id(customerModelSave.getId());
            configOld = configModelMapper.selectOne(configOld);
            ConfigModel configSave = new ConfigModel();
            configSave.setStore_id(customerModelSave.getId());
            configSave.setDomain(vo.getStoreDomain());
            configSave.setCompany(vo.getCompany());
            if (configOld != null)
            {
                configSave.setId(configOld.getId());
                configSave.setModify_date(new Date());
                count = configModelMapper.updateByPrimaryKeySelective(configSave);
            }
            else
            {
                configSave.setDefault_lang_code(vo.getDefault_lang());
                configSave.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_OSS);
                configSave.setUploadImg("");
                count = configModelMapper.insertSelective(configSave);
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XTSZTJSB, "系统设置添加失败");
            }
            //初始化商城必备的数据
            publicAdminService.initialStoreData(customerModelSave.getId(), adminUser.getId(), vo);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑商城 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStore");
        }
    }

    private void bindingStoreCurrencys(AddShopVo vo, CustomerModel customerModelSave) throws LaiKeAPIException
    {
        try
        {
            //编辑和保存时的界面选择的绑定货币
            String[] currencys = vo.getStore_currencys().split(SplitUtils.DH);
            List<Integer> viewsCurrencys = Arrays.stream(currencys).map(Integer::parseInt).collect(Collectors.toList());

            //数据库中目前商城绑定的货币
            CurrencyStoreModel queryCurrencyStore = new CurrencyStoreModel();
            queryCurrencyStore.setStore_id(customerModelSave.getId());
            List<CurrencyStoreModel> currencyStoreModels = currencyStoreModelMapper.select(queryCurrencyStore);
            List<Integer> databaseCurrencys = currencyStoreModels.stream().map(CurrencyStoreModel::getCurrency_id).collect(Collectors.toList());

            Set<Integer> setViews    = new HashSet<>(viewsCurrencys);
            Set<Integer> setDatabase = new HashSet<>(databaseCurrencys);

            // 计算 viewsCurrencys - databaseCurrencys 得到商城需要新增的货币ID信息
            List<Integer> addCurrencysId = viewsCurrencys.stream().filter(e -> !setDatabase.contains(e)).collect(Collectors.toList());
            // 计算 databaseCurrencys - viewsCurrencys 得到商城需要删除的货币ID信息
            List<Integer> delCurrencysId = databaseCurrencys.stream().filter(e -> !setViews.contains(e)).collect(Collectors.toList());

            //删除本次没有,上次有的币种
            List<CurrencyStoreModel> tmpDelCurrencys = new ArrayList<>();
            int pos = 0;
            for (Integer currencyId : delCurrencysId)
            {
                CurrencyStoreModel currencyStoreModel = currencyStoreModels.get(pos++);
                if (currencyId == currencyStoreModel.getCurrency_id())
                {
                    tmpDelCurrencys.add(currencyStoreModel);
                }
            }

            if (tmpDelCurrencys.size() > 0)
            {
                //批量删除
                currencyStoreModelMapper.deleteByBatchParams(tmpDelCurrencys);
            }

            //编辑和保存时的界面选择的绑定货币
            List<CurrencyStoreModel> tmpAddCurrencys    = new ArrayList<>();
            CurrencyStoreModel       currencyStoreModel = null;
            for (Integer currencyId : addCurrencysId)
            {
                currencyStoreModel = new CurrencyStoreModel();
                currencyStoreModel.setCurrency_id(currencyId);
                currencyStoreModel.setStore_id(customerModelSave.getId());
                //商城自己设置汇率具体值，默认设置为1
                currencyStoreModel.setExchange_rate(new BigDecimal(1));
                currencyStoreModel.setIs_show(CurrencyStoreModel.ShowOptions.YES);
                //默认设置为非默认币种
                currencyStoreModel.setDefault_currency(CurrencyStoreModel.DefaultCurrency.NO);
                currencyStoreModel.setRecycle(CurrencyStoreModel.RecycleOptions.NO);
                currencyStoreModel.setUpdate_time(new Date());
                tmpAddCurrencys.add(currencyStoreModel);
            }

            //保存上次没有出现的
            if (tmpAddCurrencys.size() > 0)
            {
                currencyStoreModelMapper.insertList(tmpAddCurrencys);
            }

            //设置默认货币
            if (vo.getDefault_currency() != null)
            {
                // 设置选中的币种为默认
                CurrencyStoreModel updateDefault = new CurrencyStoreModel();
                updateDefault.setStore_id(customerModelSave.getId());
                updateDefault.setCurrency_id(vo.getDefault_currency());
                updateDefault.setDefault_currency(CurrencyStoreModel.DefaultCurrency.YES);
                adminCurrencyStoreService.setDefaultCurrency(updateDefault,true);
            }

        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delStore(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int count;
            //获取上传信息
            CustomerModel customerModel = new CustomerModel();
//            customerModel.setId(vo.getStoreId());
            //禅道 2226
            customerModel.setId(id);
            customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            customerModel = customerModelMapper.selectOne(customerModel);
            if (customerModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCBCZ, "商城不存在");
            }
            //默认商城不能删除
            if (DictionaryConst.WhetherMaven.WHETHER_OK == customerModel.getIs_default())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "默认商城不能删除");
            }
            CustomerModel customerModelCount = new CustomerModel();
            customerModelCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            if (customerModelMapper.selectCount(customerModelCount) <= 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "默认商城不能失败");
            }
            //删除商城
            CustomerModel customerModelUpdate = new CustomerModel();
//            customerModelUpdate.setId(vo.getStoreId());
            //禅道 2226
            customerModelUpdate.setId(id);
            customerModelUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            count = customerModelMapper.updateByPrimaryKeySelective(customerModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSCSB, "商城删除失败");
            }
            //添加操作日志
//            publiceService.addAdminRecord(vo.getStoreId(), "删除了商城ID：" + vo.getStoreId(), AdminRecordModel.Type.DEL, vo.getAccessId());
            //禅道 2226
            publiceService.addAdminRecord(vo.getStoreId(), "删除了商城ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
            //获取所有商城管理员id
            List<Integer> adminIdList = adminModelMapper.getAdminIdList(vo.getStoreId());
            for (Integer adminId : adminIdList)
            {
                String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminId;
                String tokenOld = redisUtil.get(logKey) + "";
                if (StringUtils.isNotEmpty(tokenOld))
                {
                    //踢人
                    logger.info("【{}】商城已删除,adminId:{}强制退出系统", vo.getStoreId(), adminId);
                    redisUtil.del(logKey);
                    redisUtil.del(tokenOld);
                }
            }
            //删除管理员
//            count = adminModelMapper.delAdminByStoreId(vo.getStoreId());
            //禅道 2226
            count = adminModelMapper.delAdminByStoreId(id);
//            logger.debug("商城id{}一共删除{}个管理员", vo.getStoreId(), count);
            logger.debug("商城id{}一共删除{}个管理员", id, count);

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除商城 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delStore");
        }
    }

    @Override
    public boolean resetAdminPwd(MainVo vo, int adminId, String pwd) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            AdminModel adminModel = new AdminModel();
            adminModel.setId(adminId);
            adminModel = adminModelMapper.selectOne(adminModel);
            if (adminModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYBCZ, "管理员不存在");
            }
            if (!StringUtils.isEmpty(pwd))
            {
                if (!DataCheckTool.checkNumAndLetter(pwd, 6, 20))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYMMZDW, "管理员密码长度为 6-20");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYMMBNWK, "管理员密码不能为空");
            }
            AdminModel adminModelUpdate = new AdminModel();
            adminModelUpdate.setId(adminId);
            adminModelUpdate.setPassword(Md5Util.MD5endoce(pwd));
            adminModelUpdate.setToken("");
            //重置密码强制退出
            //获取所有商城管理员id
            List<Integer> adminIdList = adminModelMapper.getAdminIdList(adminModel.getStore_id());
            for (Integer id : adminIdList)
            {
                String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + id;
                String tokenOld = redisUtil.get(logKey) + "";
                if (StringUtils.isNotEmpty(tokenOld))
                {
                    //踢人
                    logger.info("adminId:{}强制退出系统", id);
                    redisUtil.del(logKey);
                    redisUtil.del(tokenOld);
                }
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将商城ID：" + adminModel.getStore_id() + "的密码进行了重置", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return adminModelMapper.updateByPrimaryKeySelective(adminModelUpdate) > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("重置管理员密码 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "resetAdminPwd");
        }
    }

    @Override
    public Boolean checkShopHavaSelfOwnedShop(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            //获取商城自营
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (Objects.isNull(mchId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTJZYD, "请添加自营店");
            }
        }catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "resetAdminPwd");
        }
        return true;
    }

    @Override
    public Map<String, Object> getLandingCurrency(MainVo vo) throws LaiKeAPIException
    {
       Map<String,Object> result = new HashMap<>();
       try
       {
           RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
           int storeId = vo.getStoreId();
           Integer default_currency = null;
           Integer default_lang_id = null;
           CurrencyStoreModel currencyStoreModel = new CurrencyStoreModel();
           currencyStoreModel.setStore_id(storeId);
           List<CurrencyStoreModel> currencyStoreModels = currencyStoreModelMapper.select(currencyStoreModel);
           //商城选择的语种
           for (CurrencyStoreModel currencyStore : currencyStoreModels)
           {
               if (currencyStore.getDefault_currency() == 1)
               {
                   default_currency = currencyStore.getCurrency_id();
                   break;
               }
           }
           //默认币种
           result.put("default_currency",default_currency);

           //默认语种
           ConfigModel configModel = new ConfigModel();
           configModel.setStore_id(storeId);
           configModel = configModelMapper.selectOne(configModel);
           if (Objects.nonNull(configModel) && StringUtils.isNotEmpty(configModel.getDefault_lang_code()))
           {
               LangModel langModel = new LangModel();
               langModel.setLang_code(configModel.getDefault_lang_code());
               langModel = langModelMapper.selectOne(langModel);
               if (Objects.nonNull(langModel))
               {
                   default_lang_id =  langModel.getId();
               }
           }
           result.put("default_lang_id",default_lang_id);
       }
       catch (LaiKeAPIException l)
       {
           throw l;
       }
       catch (Exception e)
       {
           e.printStackTrace();
           throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常",e.getMessage());
       }
       return result;
    }

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private LangModelMapper langModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private CurrencyModelMapper currencyModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private RoleModelMapper roleModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private AdminCurrencyStoreService adminCurrencyStoreService;
}

