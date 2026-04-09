package com.laiketui.admins.admin.services.supplier;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.supplier.AdminSupplierService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.MobileUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.supplier.SupplierBankCardModel;
import com.laiketui.domain.supplier.SupplierConfigModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.supplier.SupplierWithdrawModel;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.supplier.AddSupplierVo;
import com.laiketui.domain.vo.supplier.GetSupplierVo;
import com.laiketui.domain.vo.supplier.SupplierConfigVo;
import com.laiketui.domain.vo.supplier.SupplierWithdrawVo;
import com.laiketui.root.license.CryptoUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 供应商管理
 *
 * @Author: sunH_
 * @Date: Create in 14:38 2022/9/14
 */
@Service
public class AdminSupplierServiceImpl implements AdminSupplierService
{

    private final Logger logger = LoggerFactory.getLogger(AdminSupplierServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private SupplierProModelMapper supplierProModelMapper;

    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    @Autowired
    private SupplierWithdrawModelMapper supplierWithdrawModelMapper;

    @Autowired
    private SupplierBankCardModelMapper supplierBankCardModelMapper;

    @Autowired
    private SupplierConfigModelMapper supplierConfigModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(AddSupplierVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel    user          = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            SupplierModel supplierModel = new SupplierModel();
            if (StringUtils.isEmpty(vo.getLogo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请上传Logo图片", "addOrUpdate");
            }
            if (StringUtils.isEmpty(vo.getHeadUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请上传供应商头像", "addOrUpdate");
            }
            if (StringUtils.isEmpty(vo.getSupplierName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入供应商名称", "addOrUpdate");
            }
            if (Objects.isNull(vo.getPrice()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入价格", "addOrUpdate");
            }
            if (StringUtils.isEmpty(vo.getExpireDate()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置到期时间", "addOrUpdate");
            }
            if (Objects.isNull(vo.getDicId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择供应商类型", "addOrUpdate");
            }
            if (Objects.isNull(vo.getType()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择所属性质", "addOrUpdate");
            }
            if (vo.getType().equals(SupplierModel.ENTERPRISE))
            {
                if (StringUtils.isEmpty(vo.getBusinessLicense()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请上传营业执照图片", "addOrUpdate");
                }
                supplierModel.setBusiness_license(ImgUploadUtils.getUrlImgByName(vo.getBusinessLicense(), true));
            }
            else
            {
                if (StringUtils.isEmpty(vo.getBusinessLicense()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请上传身份证正反面图片", "addOrUpdate");
                }
                StringBuilder idImg = new StringBuilder();
                String[]      split = vo.getBusinessLicense().split(SplitUtils.DH);
                for (String img : split)
                {
                    String urlImgByName = ImgUploadUtils.getUrlImgByName(img, true);
                    idImg.append(urlImgByName).append(",");
                }
                supplierModel.setBusiness_license(StringUtils.trim(idImg.toString(), SplitUtils.DH));
            }
            if (StringUtils.isEmpty(vo.getContacts()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入联系人", "addOrUpdate");
            }
            if (StringUtils.isEmpty(vo.getContactPhone()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入联系电话", "addOrUpdate");
            }
            //手机号限制 11 位
           /* if (!MobileUtils.isMobile(vo.getContactPhone()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PHONE_NOT_CHECK, "手机号格式不正确", "addOrUpdate");
            }*/
            if (StringUtils.isEmpty(vo.getProvince()) || StringUtils.isEmpty(vo.getCity()) || StringUtils.isEmpty(vo.getArea()) || StringUtils.isEmpty(vo.getAddress()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请完善供应商省市区地址信息", "addOrUpdate");
            }
            if (StringUtils.isEmpty(vo.getAccountNumber()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置供应商管理员账号", "addOrUpdate");
            }
            if (StringUtils.isEmpty(vo.getPassword()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置供应商管理员密码", "addOrUpdate");
            }

            supplierModel.setStore_id(vo.getStoreId());
            supplierModel.setAccount_number(vo.getAccountNumber());
            supplierModel.setPassword(CryptoUtil.strEncode(vo.getPassword()));
            supplierModel.setLogo(ImgUploadUtils.getUrlImgByName(vo.getLogo(), true));
            supplierModel.setHead_url(ImgUploadUtils.getUrlImgByName(vo.getHeadUrl(), true));
            supplierModel.setSupplier_name(vo.getSupplierName());
            supplierModel.setPrice(vo.getPrice());
            supplierModel.setExpire_date(DateUtil.dateFormateToDate(vo.getExpireDate(), GloabConst.TimePattern.YMDHMS));
            supplierModel.setDic_id(vo.getDicId());
            supplierModel.setBusiness_scope(vo.getBusinessScope());
            supplierModel.setType(vo.getType());
            supplierModel.setCpc(vo.getCpc());
            supplierModel.setContacts(vo.getContacts());
            supplierModel.setContact_phone(vo.getContactPhone());
            supplierModel.setProvince(vo.getProvince());
            supplierModel.setCity(vo.getCity());
            supplierModel.setArea(vo.getArea());
            supplierModel.setAddress(vo.getAddress());
            if (Objects.isNull(vo.getId()))
            {
                int i = supplierModelMapper.countBySupplierName(vo.getStoreId(), vo.getSupplierName(), null);
                if (i > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSMCYCZ, "供应商名称已存在", "addOrUpdate");
                }
                int j = supplierModelMapper.countByAccountNumber(vo.getStoreId(), vo.getAccountNumber(), null);
                if (j > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSZHYCZ, "供应商账号已存在", "addOrUpdate");
                }
                supplierModel.setStatus(SupplierModel.NORMAL);
                supplierModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                supplierModel.setAdd_date(new Date());
                supplierModel.setCumulative_balance(BigDecimal.ZERO);
                supplierModel.setSurplus_balance(BigDecimal.ZERO);
                supplierModelMapper.insertSelective(supplierModel);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了供应商ID：" + supplierModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            else
            {
                int i = supplierModelMapper.countBySupplierName(vo.getStoreId(), vo.getSupplierName(), vo.getId());
                if (i > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSMCYCZ, "供应商名称已存在", "addOrUpdate");
                }
                int j = supplierModelMapper.countByAccountNumber(vo.getStoreId(), vo.getAccountNumber(), vo.getId());
                if (j > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSZHYCZ, "供应商账号已存在", "addOrUpdate");
                }
                supplierModel.setId(vo.getId());
                supplierModelMapper.updateByPrimaryKeySelective(supplierModel);

                publiceService.addAdminRecord(vo.getStoreId(), "修改了供应商ID：" + vo.getId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑供应商信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addOrUpdate");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            AdminModel    user          = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(supplierModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSXXBCZ, "供应商信息不存在", "del");
            }
            if (supplierModel.getSurplus_balance().compareTo(BigDecimal.ZERO) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_GGYSZHYEWTX, "该供应商账户余额未提现", "del");
            }
            int i = orderModelMapper.countForSupplier(vo.getStoreId(), String.valueOf(supplierModel.getId()));
            if (i > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_GGYSCZDFHDD, "该供应商存在代发货订单", "del");
            }
            supplierModel.setRecovery(DictionaryConst.ProductRecycle.RECOVERY);
            supplierModelMapper.updateByPrimaryKeySelective(supplierModel);

            publiceService.addAdminRecord(vo.getStoreId(), "删除了供应商ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
            //删除所有当前供应商的商品
            productListModelMapper.delAllSupplierPro(vo.getStoreId(), String.valueOf(supplierModel.getId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除供应商信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            AdminModel    adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(supplierModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSXXBCZ, "供应商信息不存在", "del");
            }

            String event = "";
            if (supplierModel.getStatus().equals(SupplierModel.NORMAL))
            {
                supplierModel.setStatus(SupplierModel.LOCKING);
                //强迫供应商下线
                String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_FLAG + id;
                String tokenOld = redisUtil.get(logKey) + "";
                if (StringUtils.isNotEmpty(tokenOld))
                {
                    //踢人
                    logger.info("adminId:{}锁定了" + id + "供应商,强制退出系统", adminModel.getId());
                    redisUtil.del(logKey);
                    redisUtil.del(tokenOld);
                }
                event = "将供应商ID：" + id + " 进行了锁定操作";
            }
            else if (supplierModel.getStatus().equals(SupplierModel.LOCKING))
            {
                supplierModel.setStatus(SupplierModel.NORMAL);
                event = "将供应商ID：" + id + " 进行了启用操作";
            }
            supplierModelMapper.updateByPrimaryKeySelective(supplierModel);

            publiceService.addAdminRecord(vo.getStoreId(), event, AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("锁定供应商信息异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "lock");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getList(GetSupplierVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("storeId", vo.getStoreId());
            if (StringUtils.isNotEmpty(vo.getSupplierId()))
            {
                paramMap.put("supplierId", vo.getSupplierId());
            }
            if (StringUtils.isNotEmpty(vo.getSupplierName()))
            {
                paramMap.put("supplierName", vo.getSupplierName());
            }
            if (!Objects.isNull(vo.getDicId()))
            {
                paramMap.put("dicId", vo.getDicId());
            }
            if (!Objects.isNull(vo.getType()))
            {
                paramMap.put("type", vo.getType());
            }
            if (!Objects.isNull(vo.getStatus()))
            {
                paramMap.put("status", vo.getStatus());
            }
            if (StringUtils.isNotEmpty(vo.getStartTime()))
            {
                paramMap.put("startTime", vo.getStartTime());
            }
            if (StringUtils.isNotEmpty(vo.getEndTime()))
            {
                paramMap.put("endTime", vo.getEndTime());
            }
            List<Map<String, Object>> list = new ArrayList<>();
            int                       i    = supplierModelMapper.countCondition(paramMap);
            if (i > 0)
            {
                list = supplierModelMapper.conditionQuery(paramMap);
                for (Map<String, Object> map : list)
                {
                    Integer id    = MapUtils.getInteger(map, "id");
                    Integer dicId = MapUtils.getInteger(map, "dic_id");
                    map.put("logo", publiceService.getImgPath(MapUtils.getString(map, "logo"), vo.getStoreId()));
                    map.put("head_url", publiceService.getImgPath(MapUtils.getString(map, "head_url"), vo.getStoreId()));
                    map.put("password", CryptoUtil.strDecode(MapUtils.getString(map, "password")));
                    String expireDate = DateUtil.dateFormate(MapUtils.getString(map, "expire_date"), GloabConst.TimePattern.YMDHMS);
                    map.put("expire_date", expireDate);
                    Date now = new Date();
                    Integer status = MapUtils.getInteger(map, "status");

                    DateTime expire_time = cn.hutool.core.date.DateUtil.parse(expireDate);
                    //已过期
                    if (now.compareTo(expire_time) >= 0 && status == SupplierModel.NORMAL) {
                        SupplierModel supplierModel = new SupplierModel();
                        supplierModel.setId(id);
                        supplierModel.setStatus(SupplierModel.EXPIRE);
                        supplierModelMapper.updateByPrimaryKeySelective(supplierModel);
                    }

                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    map.put("last_login_date", DateUtil.dateFormate(MapUtils.getString(map, "last_login_date"), GloabConst.TimePattern.YMDHMS));
                    String typeDesc = "";
                    if (MapUtils.getInteger(map, "type").equals(SupplierModel.PERSONAL))
                    {
                        typeDesc = "个人";
                        StringBuilder idImgs          = new StringBuilder();
                        String        businessLicense = MapUtils.getString(map, "business_license");
                        String[]      split           = businessLicense.split(SplitUtils.DH);
                        for (String imgs : split)
                        {
                            String imgPath = publiceService.getImgPath(imgs, vo.getStoreId());
                            idImgs.append(imgPath).append(",");
                        }
                        map.put("business_license", StringUtils.trim(idImgs.toString(), SplitUtils.DH));
                    }
                    else
                    {
                        typeDesc = "企业";
                        map.put("business_license", publiceService.getImgPath(MapUtils.getString(map, "business_license"), vo.getStoreId()));
                    }
                    map.put("typeDesc", typeDesc);
                    String statusDesc = "";
                    if (status.equals(SupplierModel.NORMAL))
                    {
                        statusDesc = "正常";
                    } else if (status.equals(SupplierModel.EXPIRE))
                    {
                        statusDesc = "到期";
                    } else if (status.equals(SupplierModel.LOCKING))
                    {
                        statusDesc = "锁定";
                    }
                    map.put("statusDesc", statusDesc);
                    DictionaryListModel dictionaryListModel = dictionaryListModelMapper.selectByPrimaryKey(dicId);
                    map.put("dicName", dictionaryListModel.getCtext());
                    map.put("proTotal", supplierModelMapper.proTotal(vo.getStoreId(), id));
                    map.put("salePro", supplierModelMapper.salePro(vo.getStoreId(), id,2));
                    map.put("violationPro", supplierModelMapper.violationPro(vo.getStoreId(), id));
                    map.put("examinePro", supplierModelMapper.examinePro(vo.getStoreId(), id));
                    map.put("mchNum", supplierModelMapper.mchNum(vo.getStoreId(), id));
                }
            }
            //导出
            if (vo.getExportType() != null && vo.getExportType() == 1)
            {
                exportSupplierData(list, response);
                return null;
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询供应商信息异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getList");
        }
        return resultMap;
    }

    @Autowired
    private PublicOrderService publicOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> orderIndex(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(adminOrderVo.getAccessId(), redisUtil);
            if (adminOrderVo.getSelfLifting() == null)
            {
                adminOrderVo.setSelfLifting(13);
            }
            resultMap = publicOrderService.pcMchOrderIndex(adminOrderVo);
            if (adminOrderVo.getExportType().equals(1))
            {
                exportOrderData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取订单列表 异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo)
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(orderVo.getAccessId(), redisUtil);
            String     sNo  = orderVo.getsNo();
            orderVo.setId(sNo);
            orderVo.setsNo(sNo);
            orderVo.setOrderType("see");
            return publicOrderService.orderPcDetails(orderVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单详情异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败", "orderDetailsInfo");
        }
    }

    @Override
    public Map<String, Object> settlementList(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
//            vo.setSupplierId(supplierCache.getId());
            vo.setSupplierOrder("supplierOrder");
            resultMap = publicOrderService.getSettlementOrderList(vo);
            if (vo.getExportType() == 1)
            {
                exportSupplierOrderData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单结算列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Override
    public Map<String, Object> withdraw(SupplierWithdrawVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            if (StringUtils.isNotEmpty(vo.getSupplierName()))
            {
                paramMap.put("supplierName", vo.getSupplierName());
            }
            if (StringUtils.isNotEmpty(vo.getPhone()))
            {
                paramMap.put("phone", vo.getPhone());
            }
            if (StringUtils.isNotEmpty(vo.getStatus()))
            {
                List<Integer> statusList = new ArrayList<>();
                String[]      split      = vo.getStatus().split(SplitUtils.DH);
                for (String status : split)
                {
                    statusList.add(Integer.valueOf(status));
                }
                paramMap.put("statusList", statusList);
            }
            if (StringUtils.isNotEmpty(vo.getStartTime()))
            {
                paramMap.put("startTime", vo.getStartTime());
            }
            if (StringUtils.isNotEmpty(vo.getEndTime()))
            {
                paramMap.put("endTime", vo.getEndTime());
            }
            List<Map<String, Object>> list = new ArrayList<>();
            int                       i    = supplierWithdrawModelMapper.countWithdraw(paramMap);
            if (i > 0)
            {
                list = supplierWithdrawModelMapper.getWithdraw(paramMap);
                list.stream().forEach(map ->
                {
                    String money = MapUtils.getString(map, "money");

                    int ws = 0;
                    if (MapUtils.getInteger(map, "withdraw_status") != null)
                    {
                        ws = MapUtils.getInteger(map, "withdraw_status");
                    }
                    if (ws == 1)
                    {
                        String                sCharge               = MapUtils.getString(map, "s_charge");
                        Integer               bankId                = MapUtils.getInteger(map, "bank_id");
                        SupplierBankCardModel supplierBankCardModel = supplierBankCardModelMapper.selectByPrimaryKey(bankId);
                        map.put("bankName", supplierBankCardModel.getBank_name());
                        map.put("bankCard", supplierBankCardModel.getBank_card_number());
                        map.put("cardholder", supplierBankCardModel.getCardholder());
                        map.put("branch", supplierBankCardModel.getBranch());
                        map.put("bankInfo", supplierBankCardModel.getBank_name() + "/" + supplierBankCardModel.getBranch());
                        map.put("received", new BigDecimal(money).subtract(new BigDecimal(sCharge)));
                    }


                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    List<String> imgList = new ArrayList<>();
                    String       voucher = MapUtils.getString(map, "voucher");
                    if (StringUtils.isNotEmpty(voucher))
                    {
                        String imgPath = publiceService.getImgPath(voucher, vo.getStoreId());
                        imgList.add(imgPath);
                        map.put("voucher", imgPath);
                        map.put("imgList", imgList);
                    }
                    switch (MapUtils.getInteger(map, "status"))
                    {
                        case 0:
                            map.put("statusDesc", "待审核");
                            break;
                        case 1:
                            map.put("statusDesc", "审核通过");
                            break;
                        case 2:
                            map.put("statusDesc", "审核拒绝");
                            break;
                        default:
                            map.put("statusDesc", "未知");
                            break;
                    }
                });
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
            if (vo.getExportType().equals(1))
            {
                if (vo.getExportData().equals(1))
                {
                    exportWithdrawData(DataUtils.cast(resultMap.get("list")), response);
                }
                else
                {
                    exportWithdrawRecordData(DataUtils.cast(resultMap.get("list")), response);
                }

                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取提现记录异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdraw");
        }
        return resultMap;
    }

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void examineWithdraw(MainVo vo, Integer id, Integer status, String voucher, String remark) throws LaiKeAPIException
    {
        try
        {
            String text = "";

            String                event                 = "";
            AdminModel            adminModel            = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            SupplierWithdrawModel supplierWithdrawModel = supplierWithdrawModelMapper.selectByPrimaryKey(id);
            if (supplierWithdrawModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJLBCZ, "供应商提现记录不存在");
            }
            supplierWithdrawModel.setStatus(status);
            SupplierModel supplierModel = supplierModelMapper.selectByPrimaryKey(supplierWithdrawModel.getSupplier_id());
            if (status.toString().equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS))
            {
                if (StringUtils.isEmpty(voucher))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请上传凭证");
                }
                supplierWithdrawModel.setVoucher(ImgUploadUtils.getUrlImgByName(voucher, true));
                //增加累计提现金额
                supplierModel.setCumulative_balance(supplierWithdrawModel.getMoney());
                text = "申请提现" + supplierWithdrawModel.getMoney() + "审核已通过";
                event = "通过了供应商ID：" + supplierWithdrawModel.getSupplier_id() + " 的提现审核";

                //贝宝提现
                if (Objects.nonNull(supplierWithdrawModel.getWithdrawStatus()))
                {
                    if (supplierWithdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.PAYPAL))
                    {
                        try
                        {
                            Map<String, Object> params = new HashMap<>(16);
                            params.put("storeId", vo.getStoreId());
                            params.put("withdrawId", supplierWithdrawModel.getId());
                            BigDecimal money    = supplierWithdrawModel.getMoney();
                            BigDecimal s_charge = supplierWithdrawModel.getS_charge();
                            money = money.subtract(s_charge);
                            params.put("money", money);
//                        params.put("money", supplierWithdrawModel.getMoney());
                            params.put("email", supplierWithdrawModel.getEmail());
                            Map<String, Object> paramMap1 = new HashMap<>(16);
                            paramMap1.put("paramJson", JSON.toJSONString(params));
                            logger.error("进入贝宝提现远程方法,paramMap1为：" + paramMap1.toString());
                            httpApiUtils.executeHttpApi("v1.payouts", paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                        }
                        catch (Exception e)
                        {
                            //提现失败修改 todo
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                        }
                    }
                }
            }
            else if (status.toString().equals(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS))
            {
                if (StringUtils.isEmpty(remark))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入拒绝理由");
                }
                supplierWithdrawModel.setRefuse(remark);
                //返回提现金额
                if (Objects.isNull(supplierModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYSXXBCZ, "供应商信息不存在");
                }
                supplierModel.setSurplus_balance(supplierModel.getSurplus_balance().add(supplierWithdrawModel.getMoney()));
                text = "申请提现" + supplierWithdrawModel.getMoney() + "审核已拒绝";

                event = "拒绝了供应商ID：" + supplierWithdrawModel.getSupplier_id() + " 的提现审核";
            }
            supplierModelMapper.updateByPrimaryKeySelective(supplierModel);
            supplierWithdrawModelMapper.updateByPrimaryKeySelective(supplierWithdrawModel);
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_WITHDRAW);
            messageLoggingSave.setParameter(supplierWithdrawModel.getId() + "");
            messageLoggingSave.setContent(text);
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingSave.setSupplier_id(supplierWithdrawModel.getSupplier_id());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            publiceService.addAdminRecord(vo.getStoreId(), event, AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("审核提现", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawalExamine");
        }
    }

    @Override
    public SupplierConfigModel getConfig(MainVo vo) throws LaiKeAPIException
    {
        SupplierConfigModel supplierConfigModel = new SupplierConfigModel();
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            supplierConfigModel.setStore_id(vo.getStoreId());
            supplierConfigModel = supplierConfigModelMapper.selectOne(supplierConfigModel);

            if (supplierConfigModel == null)
            {
                supplierConfigModel = new SupplierConfigModel();
                supplierConfigModel.setIs_examine(1);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取供应商配置信息异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getConfig");
        }
        return supplierConfigModel;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUpdateConfig(SupplierConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel          user              = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            SupplierConfigModel oldSupplierConfig = new SupplierConfigModel();
            oldSupplierConfig.setStore_id(vo.getStoreId());
            oldSupplierConfig = supplierConfigModelMapper.selectOne(oldSupplierConfig);
            if (Objects.isNull(vo.getMinRecharge()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置最小充值金额", "addUpdateConfig");
            }
            if (vo.getMinRecharge().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "最小充值金额需大于0", "addUpdateConfig");
            }
            if (Objects.isNull(vo.getMinWithdrawal()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置最小提现金额", "addUpdateConfig");
            }
            if (vo.getMinWithdrawal().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "最小提现金额需大于0", "addUpdateConfig");
            }
            if (Objects.isNull(vo.getMaxWithdrawal()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置最大提现金额", "addUpdateConfig");
            }
            if (vo.getMaxWithdrawal().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "最大提现金额需大于0", "addUpdateConfig");
            }
            if (vo.getMaxWithdrawal().compareTo(vo.getMinWithdrawal()) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "最大提现金额需大于等于最小提现金额", "addUpdateConfig");
            }
            if (Objects.isNull(vo.getCommission()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置手续费", "addUpdateConfig");
            }
            if (vo.getCommission().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "手续费需大于0", "addUpdateConfig");
            }
            //101 供应商后台配置不需要输入单位
            /*if (StringUtils.isEmpty(vo.getWalletUnit())) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请设置钱包单位", "addUpdateConfig");
            }*/
            SupplierConfigModel supplierConfigModel = new SupplierConfigModel();
            supplierConfigModel.setStore_id(vo.getStoreId());
            supplierConfigModel.setMin_recharge(vo.getMinRecharge());
            supplierConfigModel.setMin_withdrawal(vo.getMinWithdrawal());
            supplierConfigModel.setMax_withdrawal(vo.getMaxWithdrawal());
            supplierConfigModel.setCommission(vo.getCommission());
            //supplierConfigModel.setWallet_unit(vo.getWalletUnit());
            supplierConfigModel.setWithdrawal_ins(vo.getWithdrawalIns());
            if (oldSupplierConfig != null && oldSupplierConfig.getId() != null)
            {
                supplierConfigModel.setId(oldSupplierConfig.getId());
                supplierConfigModelMapper.updateByPrimaryKeySelective(supplierConfigModel);
            }
            else
            {
                supplierConfigModelMapper.insertSelective(supplierConfigModel);
            }

            publiceService.addAdminRecord(vo.getStoreId(), "修改了供应商的提现设置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("更新供应商配置信息异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addUpdateConfig");
        }
    }

    @Override
    public Map<String, Object> income(MainVo vo, String startTime, String endTime) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //时间段内结算金额数据
            List<Map<String, Object>> daySettlementList = new ArrayList<>();
            List<String>              dateList          = new ArrayList<>();
            if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime))
            {
                dateList = DateUtil.getIntervalDate(startTime, endTime);
            }
            else
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (int i = 0; i < 7; i++)
                {
                    Date   date       = DateUtils.addDays(new Date(), -i);
                    String formatDate = sdf.format(date);
                    dateList.add(formatDate);
                }
            }
            for (String date : dateList)
            {
                //结算金额
                Map<String, Object> daySettlement = new HashMap<>(16);
                BigDecimal          bigDecimal    = supplierModelMapper.settlementDay(vo.getStoreId(), date);
                daySettlement.put("date", date);
                daySettlement.put("settlementPrice", bigDecimal);
                daySettlementList.add(daySettlement);
            }
            //累计排行榜
            List<Map<String, Object>> cumulative = supplierModelMapper.cumulative(vo.getStoreId());
            if (cumulative.size() > 0)
            {
                cumulative.stream().forEach(supplier ->
                {
                    //供应商id
                    Integer id = MapUtils.getInteger(supplier, "id");
                    supplier.put("saleProNum", supplierModelMapper.salePro(vo.getStoreId(), id,2));
                });
            }
            //本周排行榜
            List<Map<String, Object>> thisWeek = supplierModelMapper.thisWeek(vo.getStoreId());
            if (thisWeek.size() > 0)
            {
                thisWeek.stream().forEach(supplier ->
                {
                    //供应商id
                    Integer id = MapUtils.getInteger(supplier, "id");
                    supplier.put("saleProNum", supplierModelMapper.salePro(vo.getStoreId(), id,2));
                });
            }
            resultMap.put("daySettlement", daySettlementList);
            resultMap.put("cumulative", cumulative);
            resultMap.put("thisWeek", thisWeek);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取经营收益异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "income");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void isExamine(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel          user              = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            SupplierConfigModel oldSupplierConfig = new SupplierConfigModel();
            oldSupplierConfig.setStore_id(vo.getStoreId());
            oldSupplierConfig = supplierConfigModelMapper.selectOne(oldSupplierConfig);
            if (oldSupplierConfig != null && oldSupplierConfig.getId() != null)
            {
                if (oldSupplierConfig.getIs_examine().equals(DictionaryConst.WhetherMaven.WHETHER_NO))
                {
                    oldSupplierConfig.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                }
                else
                {
                    oldSupplierConfig.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_NO);
                }
                supplierConfigModelMapper.updateByPrimaryKeySelective(oldSupplierConfig);
            }
            else
            {
                SupplierConfigModel supplierConfigModel = new SupplierConfigModel();
                supplierConfigModel.setStore_id(vo.getStoreId());
                supplierConfigModel.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_NO);
                supplierConfigModelMapper.insertSelective(supplierConfigModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置供应商商品是否需要审核异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isExamine");
        }
    }

    /**
     * 导出供应商列表
     *
     * @param goodsList -
     * @param response  -
     * @throws LaiKeAPIException-
     */
    private void exportSupplierData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"ID", "供应商名称", "联系人", "联系电话", "登录账号", "供应商类型", "所属性质", "状态", "添加时间", "到期时间", "最后登录时间"};
            //对应字段
            String[]     kayList = new String[]{"id", "supplier_name", "contacts", "contact_phone", "account_number", "dicName", "typeDesc", "statusDesc", "add_date", "expire_date", "last_login_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("供应商列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出供应商列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportSupplierData");
        }
    }

    //导出订单列表
    private void exportOrderData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"订单编号", "详情id", "创单时间", "产品名称", "规格", "件数", "价格", "订单总计", "数量", "下单类型", "订单状态", "订单类型", "用户ID", "联系人"
                    , "电话", "地址", "支付方式", "物流单号", "运费"};
            //对应字段
            String[] kayList = new String[]{"orderno", "detailId", "createDate", "goodsName", "attrStr", "needNum", "goodsPrice", "orderPrice", "goodsNum", "operationTypeName", "status", "otype", "userId", "userName"
                    , "mobile", "addressInfo", "payName", "courier_num", "detailFreight"};
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("订单列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    //导出订单结算列表
    private void exportSupplierOrderData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"结算单号", "订单编号", "店铺名称", "店铺结算金额", "供应商名称", "供应商结算金额", "交易金额", "运费", "结算状态", "结算时间", "订单生成时间"};
            //对应字段
            String[]     kayList = new String[]{"detailId", "sNo", "shopName", "mchSettlementPrice", "supplierName", "supplier_settlement", "z_price", "z_freight", "status_name", "arrive_time", "add_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("订单列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    //导出提现审核列表
    private void exportWithdrawData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"id", "用户名称", "联系电话", "申请时间", "提现金额", "提现手续费", "持卡人姓名", "银行名称", "支行名称", "卡号"};
            //对应字段
            String[]     kayList = new String[]{"id", "name", "mobile", "add_date", "money", "s_charge", "cardholder", "bankName", "branch", "bankCard"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("提现审核列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    //导出提现记录列表
    private void exportWithdrawRecordData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"id", "用户名称", "联系电话", "申请时间", "提现金额", "提现手续费", "持卡人姓名", "银行信息", "卡号", "凭证", "状态", "备注"};
            //对应字段
            String[]     kayList = new String[]{"id", "name", "mobile", "add_date", "money", "s_charge", "cardholder", "bankInfo", "bankCard", "voucher", "statusDesc", "refuse"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("提现记录列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }
}
