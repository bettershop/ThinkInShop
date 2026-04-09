package com.laiketui.admins.mch.services.plugin;

import com.laiketui.admins.api.mch.plugin.MchCouponManageService;
import com.laiketui.common.api.PublicCouponService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.coupon.CouponActivityModel;
import com.laiketui.domain.coupon.CouponConfigModel;
import com.laiketui.domain.coupon.CouponOrderModal;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.AddCouponConfigVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.CouponUserVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 优惠卷管理
 *
 * @author Trick
 * @date 2021/1/22 14:37
 */
@Service
public class MchCouponManageServiceImpl implements MchCouponManageService
{
    private final Logger logger = LoggerFactory.getLogger(MchCouponManageServiceImpl.class);

    @Override
    public Map<String, Object> getCouponConfigInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User              user              = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            CouponConfigModel couponConfigModel = new CouponConfigModel();
            couponConfigModel.setStore_id(vo.getStoreId());
            couponConfigModel.setMch_id(user.getMchId());
            couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);

            Map<Integer, String> typeMap = new HashMap<>(16);
            String[]             typeList;
            if (couponConfigModel != null && !StringUtils.isEmpty(couponConfigModel.getCoupon_type()))
            {
                typeList = couponConfigModel.getCoupon_type().split(",");
                for (String type : typeList)
                {
                    int couooType = Integer.parseInt(type);
                    if (couooType == 1)
                    {
                        typeMap.put(couooType, "免邮券");
                    }
                    else if (couooType == 2)
                    {
                        typeMap.put(couooType, "满减券");
                    }
                    else if (couooType == 3)
                    {
                        typeMap.put(couooType, "折扣券");
                    }
                    else if (couooType == 4)
                    {
                        typeMap.put(couooType, "会员赠券");
                    }
                }
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
    public boolean addCouponConfig(AddCouponConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            User              user                  = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            int               count;
            CouponConfigModel couponConfigModelSave = new CouponConfigModel();
            if (vo.getIsAutoClearCoupon() != null && vo.getIsAutoClearCoupon() == 1)
            {
                if (vo.getAutoClearCouponDay() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJGQSCTSBNWK, "优惠卷过期删除天数不能为空");
                }
                couponConfigModelSave.setCoupon_del(vo.getIsAutoClearCoupon());
                couponConfigModelSave.setCoupon_day(vo.getAutoClearCouponDay());
            }
            else
            {
                couponConfigModelSave.setCoupon_del(0);
            }
            if (vo.getIsAutoClearaAtivity() != null && vo.getIsAutoClearaAtivity() == 1)
            {
                if (vo.getAutoClearaAtivityDay() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJHDGQSCTSBNWK, "优惠卷活动过期删除天数不能为空");
                }
                couponConfigModelSave.setActivity_del(vo.getIsAutoClearaAtivity());
                couponConfigModelSave.setActivity_day(vo.getAutoClearaAtivityDay());
            }
            else
            {
                couponConfigModelSave.setActivity_del(0);
            }
            if (StringUtils.isEmpty(vo.getCouponType()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZYHQLX, "请选择优惠券类型");
            }
            if (vo.getLimitType() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LQXZBNWK, "领取限制不能为空");
            }

            CouponConfigModel couponConfigModelOld = new CouponConfigModel();
            couponConfigModelOld.setStore_id(vo.getStoreId());
            couponConfigModelOld.setMch_id(user.getMchId());
            couponConfigModelOld = couponConfigModelMapper.selectOne(couponConfigModelOld);

            couponConfigModelSave.setMch_id(user.getMchId());
            couponConfigModelSave.setIs_status(vo.getIsOpen());
            couponConfigModelSave.setCoupon_type(vo.getCouponType());
            couponConfigModelSave.setLimit_type(vo.getLimitType());
            couponConfigModelSave.setModify_date(new Date());

            if (couponConfigModelOld != null)
            {
                couponConfigModelSave.setId(couponConfigModelOld.getId());
                count = couponConfigModelMapper.updateByPrimaryKeySelective(couponConfigModelSave);
            }
            else
            {
                couponConfigModelSave.setStore_id(vo.getStoreId());
                count = couponConfigModelMapper.insertSelective(couponConfigModelSave);
            }

            if (DictionaryConst.WhetherMaven.WHETHER_OK == couponConfigModelSave.getIs_status())
            {
                //显示所有优惠券
                couponActivityModelMapper.setCouponDisplayByMchId(vo.getStoreId(), user.getMchId(), DictionaryConst.WhetherMaven.WHETHER_OK);
            }
            else
            {
                couponActivityModelMapper.setCouponDisplayByMchId(vo.getStoreId(), user.getMchId(), DictionaryConst.WhetherMaven.WHETHER_NO);
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
            e.printStackTrace();
            logger.error("获取优惠卷信息 异常" + e.getMessage());
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
            String[] userIdList = userIds.split(",");
            return publicCouponService.giveCoupons(vo.getStoreId(), Arrays.asList(userIdList), hid);
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
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("活动显示开关 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityisDisplay");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delCoupon(MainVo vo, int hid) throws LaiKeAPIException
    {
        try
        {
            return publicCouponService.delMchCoupon(vo.getStoreId(), hid, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除优惠卷活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delCoupon");
        }
    }

    @Override
    public Map<String, Object> seeCouponLogger(MainVo vo, int hid, Integer status, String sNo, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize());
            resultMap = publicCouponService.seeCoupon(vo.getStoreId(), hid, null, status, sNo, name, null, pageModel);
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

    @Override
    public Map<String, Object> seeGiveCouponLogger(MainVo vo, int hid, Integer type, String sNo, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("hid", hid);
            parmaMap.put("type", type);
            if (!StringUtils.isEmpty(sNo))
            {
                parmaMap.put("likeOrderno", sNo);
            }
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("user_name1", name);
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
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
                map.put("orderList", orderList);
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
}

