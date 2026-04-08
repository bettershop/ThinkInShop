package com.laiketui.admins.mch.services;

import com.laiketui.admins.api.mch.MchCouponService;
import com.laiketui.common.api.PublicCouponService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicMchService;
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
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.coupon.CouponActivityModel;
import com.laiketui.domain.coupon.CouponOrderModal;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.CouponUserVo;
import com.laiketui.domain.vo.coupon.SeeCouponVo;
import com.laiketui.domain.vo.goods.GoodsConfigureVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 优惠卷管理
 *
 * @author Trick
 * @date 2021/6/9 15:41
 */
@Service
public class MchCouponServiceImpl implements MchCouponService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicCouponService publicCouponService;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private CouponPresentationRecordModelMapper couponPresentationRecordModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(CouponParmaVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setMchId(user.getMchId());
            resultMap = publicCouponService.storeCoupons(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取优惠卷列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> addPage(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            resultMap = publicCouponService.addStoreCouponsPage(vo.getStoreId(), user.getUser_id(), user.getMchId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加优惠卷页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addPage");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(AddCouponActivityVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setMchId(user.getMchId());
            if (!publicCouponService.addStoreCoupons(vo, user.getUser_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "add");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void modify(AddCouponActivityVo vo) throws LaiKeAPIException
    {
        try
        {
            if (vo.getId() != null && vo.getId() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "modify");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setMchId(user.getMchId());
            if (!publicCouponService.addStoreCoupons(vo, user.getUser_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modify");
        }
    }

    @Override
    public Map<String, Object> modifyPage(CouponParmaVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            vo.setMchId(user.getMchId());
            resultMap = publicCouponService.storeCoupons(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑优惠卷页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modifyPage");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> seeCoupon(CouponParmaVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            if (vo.getMchId() == null)
            {
                vo.setMchId(user.getMchId());
            }
            resultMap = publicCouponService.storeCoupons(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "seeCoupon");
        }
        return resultMap;
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            publicCouponService.delBatchMchCoupon(vo.getStoreId(), Arrays.asList(ids.split(SplitUtils.DH)), user.getMchId());
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
    public void del(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            if (!publicCouponService.delMchCoupon(vo.getStoreId(), id, user.getMchId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了优惠券ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除优惠卷活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    public Map<String, Object> fenlei(MainVo vo) throws LaiKeAPIException
    {
        HashMap<String, Object> map = new HashMap<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            map.put("list", publicGoodsService.mchAllFenlei(vo.getStoreId(), user.getMchId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取店铺分类 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "fenlei");
        }
        return map;
    }

    @Override
    public Map<String, Object> mchProduct(MainVo vo, String name) throws LaiKeAPIException
    {
        HashMap<String, Object> resultMap = new HashMap<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //获取当前商城自营店的商品
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
            parmaMap.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("product_title", name);
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
            logger.error("获取店铺商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchProduct");
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
    public void receiveUserCoupon(MainVo vo, String userIds, int hid) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZZSYWHY, "请选择至少一位会员");
            }
            String[] userIdList = userIds.split(",");
            if (!publicCouponService.giveCoupons(vo.getStoreId(), Arrays.asList(userIdList), hid))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJZSSB, "优惠卷赠送失败");
            }

            for (String id : userIdList)
            {
                publiceService.addAdminRecord(vo.getStoreId(), "将优惠券ID：" + hid + " 赠送给了用户ID：" + id, AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取优惠卷信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "receiveUserCoupon");
        }
    }

    @Override
    public Map<String, Object> seeCouponLogger(MainVo vo, int hid, Integer status, Integer type, String keyWord, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User        user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            PageModel   pageModel   = new PageModel(vo.getPageNo(), vo.getPageSize());
            SeeCouponVo seeCouponVo = new SeeCouponVo();
            seeCouponVo.setStoreId(vo.getStoreId());
            seeCouponVo.setAccessId(vo.getAccessId());
            seeCouponVo.setMchId(user.getMchId());
            seeCouponVo.setPageModel(pageModel);
            seeCouponVo.setId(hid);
            seeCouponVo.setStatus(status);
            seeCouponVo.setType(type);
            seeCouponVo.setKeyWord(keyWord);
            seeCouponVo.setIsFree(0);
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
    public Map<String, Object> seeGiveCouponLogger(MainVo vo, int hid, Integer state, Integer type, String keyWord, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("hid", hid);
            parmaMap.put("activityType", type);
            parmaMap.put("type", state);
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
                if (orderList.size() == 0)
                {
                    map.put("orderList", "");
                }
                else
                {
                    map.put("orderList", orderList);
                }
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
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
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
    @Transactional(rollbackFor = Exception.class)
    public boolean activityisDisplay(MainVo vo, int hid) throws LaiKeAPIException
    {
        try
        {
            User                user                = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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

    /**
     * 开启店铺主页领卷入口-开关
     *
     * @param vo
     * @param mchId
     * @return
     */
    @Override
    public Boolean isOpenCoupon(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (mchId == null)
            {
                mchId = user.getMchId();
            }
            MchModel mchModel = new MchModel();
            mchModel.setId(mchId);
            mchModel = mchModelMapper.selectOne(mchModel);
            mchModel.setIsOpenCoupon(mchModel.getIsOpenCoupon() == 0 ? 1 : 0);
            int i = mchModelMapper.updateByPrimaryKey(mchModel);
            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("开启店铺主页领卷入口-开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityisDisplay");
        }
        return true;
    }

    @Override
    public Map<String, Object> getSpecifiedGoodsInfo(GoodsConfigureVo vo, String id)
    {
        Map<String, Object> resultMap  = new HashMap<>(16);
        Map<String, Object> resultMap1 = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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
            parmaMap.put("mch_id", user.getMchId());
            //parmaMap.put("commodity_type", "0");
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
                resultMap1.put("store_id", vo.getStoreId());
                List<Map<String, Object>> goodsIdMaps = productListModelMapper.selectDynamic(resultMap1);
                goodsIdMaps.forEach(goods ->
                {
                    Map<String, Object> goodsInfoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(MapUtils.getString(goods, "initial"), Map.class));
                    if (goodsInfoMap != null)
                    {
                        map.put("price", new BigDecimal(MapUtils.getString(goodsInfoMap, "sj")));
                        map.put("imgurl", publiceService.getImgPath(MapUtils.getString(goods, "imgurl"), vo.getStoreId()));
                    }
                });
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
    private CouponOrderModalMapper couponOrderModalMapper;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;
}

