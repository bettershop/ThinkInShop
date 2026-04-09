package com.laiketui.plugins.coupon.dubbo;

import com.laiketui.common.api.PublicCouponService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.consts.MchConst;
import com.laiketui.common.mapper.CouponConfigModelMapper;
import com.laiketui.common.mapper.CouponOrderModalMapper;
import com.laiketui.common.mapper.CustomerModelMapper;
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
import com.laiketui.domain.coupon.CouponConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.SeeCouponVo;
import com.laiketui.plugins.api.coupon.PluginCouponService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 优惠卷实现
 *
 * @author Trick
 * @date 2020/12/7 9:48
 */
@Service
public class PluginCouponServiceImpl implements PluginCouponService
{
    private final Logger logger = LoggerFactory.getLogger(PluginCouponServiceImpl.class);


    @Override
    public Map<String, Object> mycoupon(MainVo vo, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> dataMap = publicCouponService.mycoupon(vo.getStoreId(), user.getUser_id(), type);
            resultMap.put("list", dataMap.get("list"));
            resultMap.put("wsy_num", dataMap.get("wsy_num"));
            resultMap.put("ysy_num", dataMap.get("ysy_num"));
            resultMap.put("ygq_num", dataMap.get("ygq_num"));

            //个人中心入口是否显示 优惠券入口特殊处理 36190
            boolean isCouponSho = publiceService.frontPlugin(vo.getStoreId(), null, DictionaryConst.Plugin.COUPON, null,false);

            CouponConfigModel couponConfigModel = new CouponConfigModel();
            couponConfigModel.setStore_id(vo.getStoreId());
            couponConfigModel.setMch_id(0);
            couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);

            boolean isCouponShow = DictionaryConst.WhetherMaven.WHETHER_OK == couponConfigModel.getIs_show();
            resultMap.put("isCouponShow", isCouponShow);
            if (type == 1)
            {
                //已使用
                resultMap.put("useNum", MapUtils.getIntValue(dataMap, "total"));
            }
            else if (type == 2)
            {
                //已过期
                resultMap.put("overdueNum", MapUtils.getIntValue(dataMap, "total"));
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mycoupon");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> index(MainVo vo, int type, String mchName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User      user      = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
            PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize());
            String    userId    = null;
            if (user != null && user.getUser_id() != null)
            {
                userId = user.getUser_id();
            }
            resultMap.putAll(publicCouponService.mobileTerminalCouponCenter(vo.getStoreId(), userId, type, pageModel, mchName));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mycoupon");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> receive(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User   user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            String msg  = publicCouponService.receive(vo.getStoreId(), user.getUser_id(), id);
            resultMap.put("money", msg);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("领取优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "receive");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> proCoupon(MainVo vo, int goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User   user   = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            String userId = null;
            if (user != null)
            {
                userId = user.getUser_id();
            }
            List<Map<String, Object>> dataList = publicCouponService.proCoupon(vo.getStoreId(), userId, goodsId);
            resultMap.put("list", dataList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商品可用优惠券活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "proCoupon");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> mchCoupon(MainVo vo, int mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User   user   = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            String userId = null;
            if (user != null)
            {
                userId = user.getUser_id();
            }
            List<Map<String, Object>> dataList = publicCouponService.mchCoupon(vo.getStoreId(), userId, mchId, 1);
            resultMap.put("list", dataList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商品可用优惠券活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "proCoupon");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> mchIndex(CouponParmaVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                resultMap = publicCouponService.storeCoupons(vo);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取优惠卷列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchIndex");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> addPage(MainVo vo, int mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            resultMap = publicCouponService.addStoreCouponsPage(vo.getStoreId(), user.getUser_id(), mchId);
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
    public boolean add(AddCouponActivityVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            vo.setMchId(user.getMchId());
            vo.setIssueUnit(vo.getIssueUnit());
            return publicCouponService.addStoreCoupons(vo, user.getUser_id());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addPage");
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (!publicCouponService.addStoreCoupons(vo, user.getUser_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
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
    public Map<String, Object> modifyPage(MainVo vo, int mchId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), mchId);
            resultMap = publicCouponService.getCouponsInfoById(vo.getStoreId(), id);
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
    public Map<String, Object> seeCoupon(MainVo vo, int id, int mchId, Integer status, String sNo, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicMchService.verificationMchExits(vo.getStoreId(), user.getUser_id(), mchId);
            SeeCouponVo seeCouponVo = new SeeCouponVo();
            seeCouponVo.setStoreId(vo.getStoreId());
            seeCouponVo.setAccessId(vo.getAccessId());
            seeCouponVo.setId(id);
            PageModel pageModel = new PageModel(vo.getPageNo(),vo.getPageSize());
            seeCouponVo.setPageModel(pageModel);
            seeCouponVo.setStatus(status);
            seeCouponVo.setsNo(sNo);
            seeCouponVo.setName(name);
            seeCouponVo.setIsFree(null);
            seeCouponVo.setGroupByUserId(true);
            resultMap = publicCouponService.seeCoupon(seeCouponVo);
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //禅道 53355
//            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            publicCouponService.delBatchMyCoupon(vo.getStoreId(), Arrays.asList(ids.split(SplitUtils.DH)), user.getUser_id());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除我的优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchDel");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean del(MainVo vo, int id, int mchId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), mchId);

                return publicCouponService.delMchCoupon(vo.getStoreId(), id, mchId);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除优惠卷活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> fenlei(MainVo vo, int mchId) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), mchId);
                resultMapList = publicCouponService.mchFenlei(vo.getStoreId(), mchId);
            }
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
        return resultMapList;
    }

    @Override
    public List<Map<String, Object>> mchProduct(MainVo vo, int mchId, String name) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), mchId);
                resultMapList = publicCouponService.mchProduct(vo, mchId, name);
            }
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
        return resultMapList;
    }

    @Override
    public Map<String, Object> mchUseRecord(MainVo vo, Integer acId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicMchService.verificationMchExits(vo.getStoreId(), user.getUser_id(), user.getMchId());
            int                       total      = 0;
            List<Map<String, Object>> resultList = new ArrayList<>();

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("acId", acId);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            total = couponOrderModalMapper.countDynamic(parmaMap);
            if (total > 0)
            {
                List<Map<String, Object>> list = couponOrderModalMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : list)
                {
                    Map<String, Object> tempMap = new HashMap<>(16);
                    tempMap.put("userName", MapUtils.getString(map, "user_name"));
                    tempMap.put("receiveDate", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                    tempMap.put("orderNo", MapUtils.getString(map, "sNo"));

                    tempMap.putAll(couponOrderModalMapper.getUserCouponCount(MapUtils.getString(map, "user_id"), MapUtils.getIntValue(map, "hid")));
                    tempMap.putAll(couponOrderModalMapper.getUserCouponCount2(MapUtils.getString(map, "user_id"), MapUtils.getIntValue(map, "hid")));
                    resultList.add(tempMap);
                }
            }
            resultMap.put("total", total);
            resultMap.put("resultList", resultList);

            resultMap.put("manNum", couponOrderModalMapper.countUserNum(acId));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺优惠券使用记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchUseRecord");
        }
        return resultMap;
    }

    @Autowired
    private RedisUtil           redisUtil;
    @Autowired
    private PublicCouponService publicCouponService;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private CouponOrderModalMapper couponOrderModalMapper;

    @Autowired
    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PubliceService publiceService;
}

