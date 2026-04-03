package com.laiketui.admins.admin.services.plugin;

import com.google.common.collect.ImmutableMap;
import com.laiketui.admins.api.admin.plugin.AdminCouponManageService;
import com.laiketui.common.api.PublicCouponService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.plugin.CouponDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.coupon.CouponActivityModel;
import com.laiketui.domain.coupon.CouponConfigModel;
import com.laiketui.domain.coupon.CouponOrderModal;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.coupon.*;
import com.laiketui.domain.vo.goods.GoodsConfigureVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 优惠卷管理
 *
 * @author Trick
 * @date 2021/1/22 14:37
 */
@Service
public class AdminCouponManageServiceImpl implements AdminCouponManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminCouponManageServiceImpl.class);

    @Autowired
    BrandClassModelMapper brandClassModelMapper;

    @Override
    public Map<String, Object> getCouponConfigInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            CouponConfigModel couponConfigModel = new CouponConfigModel();
            couponConfigModel.setStore_id(vo.getStoreId());
            couponConfigModel.setMch_id(0);
            couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);

            Map<Integer, String> typeMap = new HashMap<>(16);
            String[]             typeList;
            if (couponConfigModel != null && !StringUtils.isEmpty(couponConfigModel.getCoupon_type()))
            {
                typeList = couponConfigModel.getCoupon_type().split(SplitUtils.DH);
                for (String type : typeList)
                {
                    int couponType = Integer.parseInt(type);
                    typeMap.put(couponType, CouponConfigModel.CouponTypeMap.get(couponType));
                }
            }
            //55895 不影响平台端自营店按钮，暂时返1
            if (Objects.isNull(couponConfigModel))
            {
                couponConfigModel = new CouponConfigModel();
                couponConfigModel.setZy_coupon(1);
            }
            resultMap.put("typeList", typeMap);
            resultMap.put("data", couponConfigModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商城优惠卷类型 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCouponType");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getCouponTypeList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> list = new ArrayList<>();
            CouponConfigModel.CouponTypeMap.forEach((key, value) -> list.add(ImmutableMap.of("key", key.toString(), "value", value)));

            resultMap.put("typeList", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取所有优惠券类型 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCouponTypeList");
        }
        return resultMap;
    }

    @Override
    public boolean addCouponConfig(AddCouponConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            int               count;
            AdminModel        adminModel            = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            CouponConfigModel couponConfigModelSave = new CouponConfigModel();
            if (StringUtils.isEmpty(vo.getCouponType()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZYHQLX, "请选择优惠券类型");
            }
            CouponConfigModel couponConfigModelOld = new CouponConfigModel();
            couponConfigModelOld.setStore_id(vo.getStoreId());
            couponConfigModelOld.setMch_id(0);
            couponConfigModelOld = couponConfigModelMapper.selectOne(couponConfigModelOld);

            couponConfigModelSave.setMch_id(0);
            couponConfigModelSave.setIs_status(vo.getIsOpen());
            couponConfigModelSave.setCoupon_type(vo.getCouponType());
            couponConfigModelSave.setModify_date(new Date());
            couponConfigModelSave.setIs_show(vo.getIsOpen());

            if (couponConfigModelOld != null)
            {
                couponConfigModelSave.setId(couponConfigModelOld.getId());
                count = couponConfigModelMapper.updateByPrimaryKeySelective(couponConfigModelSave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了卡劵插件的配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                couponConfigModelSave.setStore_id(vo.getStoreId());
                count = couponConfigModelMapper.insertSelective(couponConfigModelSave);
            }
            if (DictionaryConst.WhetherMaven.WHETHER_OK == couponConfigModelSave.getIs_status())
            {
                //显示所有优惠券
                couponActivityModelMapper.setCouponDisplayByMchId(vo.getStoreId(), 0, DictionaryConst.WhetherMaven.WHETHER_OK);
            }
            else
            {
                couponActivityModelMapper.setCouponDisplayByMchId(vo.getStoreId(), 0, DictionaryConst.WhetherMaven.WHETHER_NO);
            }

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑优惠卷配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addCouponConfig");
        }
    }

    @Override
    public Map<String, Object> getCouponCardInfo(CouponParmaVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            vo.setMchId(0);
            resultMap = publicCouponService.storeCoupons(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取优惠卷信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCouponCardInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGiveUserInfo(CouponUserVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize());
            resultMap = publicCouponService.getUser(vo.getStoreId(), vo.getHid(), vo.getGrade(), vo.getName(), pageModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取优惠卷信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGiveUserInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean receiveUserCoupon(MainVo vo, String userIds, int hid) throws LaiKeAPIException
    {
        try
        {
            String[] userIdList = userIds.split(SplitUtils.DH);

            for (String userId : userIdList)
            {
                publiceService.addAdminRecord(vo.getStoreId(), "将优惠券ID：" + hid + " 赠送给了用户ID：" + userId, AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            return publicCouponService.giveCoupons(vo.getStoreId(), Arrays.asList(userIdList), hid);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取优惠卷信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "receiveUserCoupon");
        }
    }

    @Override
    public boolean activityisDisplay(MainVo vo, int hid) throws LaiKeAPIException
    {
        try
        {
            CouponActivityModel couponActivityModel = couponActivityModelMapper.selectByPrimaryKey(hid);
            if (couponActivityModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            CouponActivityModel couponActivityModelUpdate = new CouponActivityModel();
            couponActivityModelUpdate.setId(hid);
            int isshow = CouponActivityModel.ACTIVITY_IS_DISPLAY_SHOW;
            if (isshow == couponActivityModel.getIs_display())
            {
                isshow = CouponActivityModel.ACTIVITY_IS_DISPLAY_NOT_SHOW;
            }
            couponActivityModelUpdate.setIs_display(isshow);
            int count = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityModelUpdate);
            publiceService.addAdminRecord(vo.getStoreId(), "将优惠券ID：" + hid + " 进行了是否显示操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("活动显示开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityisDisplay");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDel(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "id集不能为空");
            }
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicCouponService.delBatchMchCoupon(vo.getStoreId(), Arrays.asList(ids.split(SplitUtils.DH)), adminModel.getShop_id());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchDel");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delCoupon(MainVo vo, int hid) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publiceService.addAdminRecord(vo.getStoreId(), "删除了优惠券ID：" + hid, AdminRecordModel.Type.DEL, vo.getAccessId());
            return publicCouponService.delMchCoupon(vo.getStoreId(), hid, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除优惠卷活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delCoupon");
        }
    }

    @Override
    public Map<String, Object> seeCouponLogger(MainVo vo, int hid, Integer status, Integer type, String keyWord, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            PageModel   pageModel   = new PageModel(vo.getPageNo(), vo.getPageSize());
            SeeCouponVo seeCouponVo = new SeeCouponVo();
            seeCouponVo.setStoreId(vo.getStoreId());
            seeCouponVo.setAccessId(vo.getAccessId());
            seeCouponVo.setPageModel(pageModel);
            seeCouponVo.setId(hid);
            seeCouponVo.setStatus(status);
            seeCouponVo.setType(type);
            seeCouponVo.setKeyWord(keyWord);
            seeCouponVo.setIsFree(0);
            seeCouponVo.setStoreType(vo.getStoreType());
            resultMap = publicCouponService.seeCoupon(seeCouponVo);
            if (vo.getExportType() != null && vo.getExportType() == 1)
            {
                exportSeeCouponData((List<Map<String, Object>>) resultMap.get("list"), response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看优惠卷领取记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "seeCouponLogger");
        }
        return resultMap;
    }

    //导出卡卷领取记录
    private void exportSeeCouponData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            list.stream().forEach(map ->
            {
                StringBuffer stringBuffer = new StringBuffer();
                List<String> orderList    = (List<String>) map.get("orderList");
                if (orderList != null && orderList.size() > 0)
                {
                    for (String s : orderList)
                    {
                        stringBuffer.append(SplitUtils.DH).append(s);
                    }
                }
                map.put("orderList", stringBuffer);
                int type = (int) map.get("type");
                switch (type)
                {
                    case 0:
                        map.put("type", "未使用");
                        break;
                    case 1:
                        map.put("type", "使用中");
                        break;
                    case 2:
                        map.put("type", "已使用");
                        break;
                    case 3:
                        map.put("type", "已过期");
                        break;
                }
            });
            //表头
            String[] headerList = new String[]{"优惠券ID", "用户ID", "用户名称", "优惠卷类型", "面值/折扣", "状态", "关联订单号", "领取时间", "过期时间"};
            //对应字段
            String[]     kayList = new String[]{"id", "user_id", "user_name", "typeName", "money", "type", "orderList", "add_time", "expiry_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("卡券领取记录表");
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

    @Override
    public Map<String, Object> seeGiveCouponLogger(MainVo vo, int hid, Integer status, Integer type, String keyWord, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("hid", hid);
            parmaMap.put("activityType", type);
            parmaMap.put("type", status);
            if (StringUtils.isNotEmpty(keyWord))
            {
                parmaMap.put("keyWord", keyWord);
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total          = couponPresentationRecordModelMapper.countCouponGiveInfo(parmaMap);
            List<Map<String, Object>> couponInfoList = couponPresentationRecordModelMapper.getCouponGiveInfo(parmaMap);
            for (Map<String, Object> map : couponInfoList)
            {
                //获取关联订单
                List<String>     orderList        = new ArrayList<>();
                CouponOrderModal couponOrderModal = new CouponOrderModal();
                couponOrderModal.setStore_id(vo.getStoreId());
                couponOrderModal.setCoupon_id(MapUtils.getIntValue(map, "coupon_id"));
                couponOrderModal.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                List<CouponOrderModal> couponOrderModalList = couponOrderModalMapper.select(couponOrderModal);
                for (CouponOrderModal couponOrder : couponOrderModalList)
                {
                    orderList.add(couponOrder.getsNo());
                }

                map.put("expiry_time", DateUtil.dateFormate(MapUtils.getString(map, "expiry_time"), GloabConst.TimePattern.YMDHMS));
                map.put("typeName", CouponDataUtils.getCouponTypeName(MapUtils.getIntValue(map, "activity_type")));
                map.put("orderList", orderList);
            }
            if (vo.getExportType() != null && vo.getExportType() == 1)
            {
                exportSeeGiveCouponData(couponInfoList, response);
                return null;
            }
            resultMap.put("total", total);
            resultMap.put("list", couponInfoList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看优惠卷赠送记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "seeGiveCouponLogger");
        }
        return resultMap;
    }

    //导出卡卷赠送记录
    private void exportSeeGiveCouponData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            list.stream().forEach(map ->
            {
                StringBuffer stringBuffer = new StringBuffer();
                List<String> orderList    = (List<String>) map.get("orderList");
                if (orderList != null && orderList.size() > 0)
                {
                    for (String s : orderList)
                    {
                        stringBuffer.append(SplitUtils.DH).append(s);
                    }
                }
                map.put("orderList", stringBuffer);
                int type = (int) map.get("type");
                switch (type)
                {
                    case 0:
                        map.put("type", "未使用");
                        break;
                    case 1:
                        map.put("type", "使用中");
                        break;
                    case 2:
                        map.put("type", "已使用");
                        break;
                    case 3:
                        map.put("type", "已过期");
                        break;
                }
            });
            //表头
            String[] headerList = new String[]{"优惠券ID", "用户ID", "用户名称", "手机号", "优惠卷类型", "面值/折扣", "状态", "关联订单号", "赠送时间", "过期时间"};
            //对应字段
            String[]     kayList = new String[]{"id", "user_id", "user_name", "mobile", "typeName", "money", "type", "orderList", "add_date", "expiry_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("卡券赠送记录表");
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

    @Override
    public Map<String, Object> getAssignGoods(MainVo vo, String goodsName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap   = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //获取当前商城自营店的商品
            parmaMap.put("mch_id", adminModel.getShop_id());
            parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
            parmaMap.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (!StringUtils.isEmpty(goodsName))
            {
                parmaMap.put("product_title", goodsName);
            }
            parmaMap.put("is_presell", DictionaryConst.WhetherMaven.WHETHER_NO);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> goodsInfoList = productListModelMapper.getGoodsInfoDynamic(parmaMap);
            resultMap.put("list", goodsInfoList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取指定商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAssignGoods");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getSpecifiedGoodsInfo(GoodsConfigureVo vo, String id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap  = new HashMap<>(16);
        Map<String, Object> resultMap1 = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取商品信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
            parmaMap.put("active", DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE);
            parmaMap.put("goodsRecycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("goodsStatus", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            parmaMap.put("stockNum_gt", 0);
            parmaMap.put("is_presell", 0);
            if (Objects.nonNull(vo.getIsZy()) && vo.getIsZy() == 1)
            {
                //获取自营店
                Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
                parmaMap.put("mch_id", mchId);
            }
            int[] arr = null;
            if (StringUtils.isNotEmpty(id))
            {
                String[] ids = id.split(",");
                arr = new int[ids.length];
                for (int i = 0; i < ids.length; i++)
                {
                    arr[i] = Integer.parseInt(ids[i]);
                }
                parmaMap.put("pid", arr);
            }
            if (vo.getCid() != null)
            {
                parmaMap.put("product_class", vo.getCid());
            }
            if (vo.getBrandId() != null)
            {
                parmaMap.put("brand_id", vo.getBrandId());
            }
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                parmaMap.put("product_title", vo.getProductTitle());
            }
            if (StringUtils.isNotEmpty(vo.getBlockId()))
            {
                parmaMap.put("blockId", vo.getBlockId());
            }
            List<Map<String, Object>> goodsSpecifiedInfoList = productListModelMapper.getSpecifiedGoodsInfoDynamic(parmaMap);
            int                       total                  = productListModelMapper.countSpecifiedGoodsInfoDynamic(parmaMap);
            ProductClassModel         productClassModel      = null;
            for (Map<String, Object> map : goodsSpecifiedInfoList)
            {
                //获取最后一级类别
                String[] productClass = MapUtils.getString(map, "product_class").split("-");
                for (int i = 0; i < productClass.length; i++)
                {
                    if (i == (productClass.length - 1))
                    {
                        //查询下级,没有下级则查询同级
                        productClassModel = new ProductClassModel();
                        productClassModel.setStore_id(vo.getStoreId());
                        productClassModel.setCid(Integer.parseInt(productClass[i]));
                        List<ProductClassModel> productLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                        //获取所有下级类别信息
                        for (ProductClassModel product : productLeves)
                        {
                            map.put("p_name", product.getPname());
                        }
                        if (productLeves.size() < 0)
                        {
                            //查询下级,没有下级则查询同级
                            productClassModel = new ProductClassModel();
                            productClassModel.setStore_id(vo.getStoreId());
                            productClassModel.setSid(Integer.parseInt(productClass[i]));
                            List<ProductClassModel> productClassLowLeves = productClassModelMapper.getProductClassLevel(productClassModel);
                            for (ProductClassModel product : productClassLowLeves)
                            {
                                map.put("p_name", product.getPname());
                            }
                        }
                    }
                }
                BrandClassModel brandClassModel = brandClassModelMapper.selectByPrimaryKey(MapUtils.getString(map, "brand_id"));
                map.put("brand_name", brandClassModel.getBrand_name());
                resultMap1.put("goodsId", MapUtils.getString(map, "id"));
                List<Map<String, Object>> confiGureModel = confiGureModelMapper.selectDynamic(resultMap1);
                for (Map<String, Object> map1 : confiGureModel)
                {
                    map.put("price", MapUtils.getString(map1, "price"));
                }
                //获取商品主图
                map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
            }
            resultMap.put("list", goodsSpecifiedInfoList);
            resultMap.put("total", total);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取指定商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAssignGoods");
        }
        return resultMap;
    }

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;
    @Autowired
    private PubliceService       publiceService;

    @Override
    public Map<String, Object> getAssignGoodsClass(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            resultMap.put("list", publicGoodsService.getClassLevelLowAll(vo.getStoreId(), 0));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取指定商品分类列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAssignGoodsClass");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addCoupon(AddCouponActivityVo vo) throws LaiKeAPIException
    {
        try
        {
            return publicCouponService.addStoreCoupons(vo, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addCoupon");
        }
    }

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicCouponService publicCouponService;

    @Autowired
    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private CouponPresentationRecordModelMapper couponPresentationRecordModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private CouponOrderModalMapper couponOrderModalMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;
}

