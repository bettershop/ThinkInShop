package com.laiketui.comps.task.services;

import com.laiketui.common.mapper.CouponActivityModelMapper;
import com.laiketui.common.mapper.CouponModalMapper;
import com.laiketui.common.mapper.CouponPresentationRecordModelMapper;
import com.laiketui.common.mapper.UserBaseMapper;
import com.laiketui.comps.api.task.CompsTaskCouponService;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.coupon.CouponActivityModel;
import com.laiketui.domain.coupon.CouponModal;
import com.laiketui.domain.coupon.CouponPresentationRecordModel;
import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 优惠卷定时任务实现
 *
 * @author Trick
 * @date 2020/12/11 13:45
 */
@Service
public class CompsTaskCouponServiceImpl implements CompsTaskCouponService
{
    private final Logger logger = LoggerFactory.getLogger(CompsTaskCouponServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void couponTimingTask() throws LaiKeAPIException
    {
        try
        {
            XxlJobHelper.log("优惠卷异步任务 开始执行!");
            clearDayCoupon();
            XxlJobHelper.log("优惠卷异步任务 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("定时刷新商品状态 异常: ", e.getMessage());
            XxlJobHelper.handleFail(e);
        }
    }

    /**
     * 清理需要删除的优惠卷
     *
     * @author Trick
     * @date 2020/12/11 14:24
     */
    private void clearDayCoupon() throws LaiKeAPIException
    {
        try
        {
            //region 获取所有优惠卷设置 del update by trick 2023-02-08 10:20:39
            /*List<CouponConfigModel> couponConfigModelList = couponConfigModelMapper.selectAll();
            for (CouponConfigModel couponConfig : couponConfigModelList) {
                //region 是否需要清理优惠卷 该功能已去掉del update by trick 2023-02-08 10:19:04
                *//*if (couponConfig.getCoupon_del() != null && couponConfig.getCoupon_del() == 1) {
                    //优惠卷删除时间
                    XxlJobHelper.log(String.format("优惠卷配置id=%s 正在执行清理[优惠卷] 优惠券过期后第:[{}]天后删除", couponConfig.getId()), couponConfig.getCoupon_day());
                    int count = couponModalMapper.delCoupon(couponConfig.getStore_id(), new Date(), couponConfig.getCoupon_day());
                    if (count < 1) {
                        XxlJobHelper.log(String.format("优惠卷配置id=%s 优惠卷无需清理", couponConfig.getId()));
                    }
                }*//*
                //endregion
                //region 是否需要清理活动 该功能已去掉del add update trick 2023-02-08 10:19:04
                *//*if (couponConfig.getActivity_del() != null && couponConfig.getActivity_del() == 1) {
                    //优惠卷删除时间
                    XxlJobHelper.log(String.format("优惠卷配置id=%s 正在执行清理[优惠卷活动] 优惠券过期后第:[{}]天后删除", couponConfig.getId()), couponConfig.getCoupon_day());
                    int count = couponActivityModelMapper.delActivity(couponConfig.getStore_id(), new Date(), couponConfig.getActivity_day());
                    if (count < 1) {
                        XxlJobHelper.log(String.format("优惠卷配置id=%s 优惠卷活动无需清理", couponConfig.getId()));
                    }
                }*//*
                //endregion
            }*/
            //endregion
            //清理已经结束的活动
            clearEndActivity();
        }
        catch (Exception e)
        {
            logger.error("清理需要删除的优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QLXYSCDYHJYC, "清理需要删除的优惠卷 异常", "clearDayCoupon");
        }
    }

    /**
     * 清理已经结束的活动
     *
     * @author Trick
     * @date 2020/12/11 14:29
     * @updateBy trick 2023-02-08 10:30:58
     */
    private void clearEndActivity() throws LaiKeAPIException
    {
        List<CouponActivityModel> couponActivityModelList = null;
        CouponActivityModel       couponActivityUpdate    = null;
        try
        {
            //获取所有未结束的活动
            couponActivityModelList = couponActivityModelMapper.getCouponActivityByNotEnd();
            for (CouponActivityModel couponActivity : couponActivityModelList)
            {
                if (couponActivity.getStatus() == null || couponActivity.getStart_time() == null || couponActivity.getEnd_time() == null)
                {
                    XxlJobHelper.log(String.format("活动id=%s 永久有效", couponActivity.getId()));
                    continue;
                }
                couponActivityUpdate = new CouponActivityModel();
                if (CouponActivityModel.COUPON_ACTIVITY_STATUS_NOT_USE == couponActivity.getStatus())
                {
                    //如果活动未启动,则看是否到了启动的时间
                    if (!DateUtil.dateCompare(new Date(), couponActivity.getStart_time()))
                    {
                        XxlJobHelper.log(String.format("活动id=%s 正在开启", couponActivity.getId()));
                        //开启活动
                        couponActivityUpdate.setId(couponActivity.getId());
                        couponActivityUpdate.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
                        int count = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityUpdate);
                        XxlJobHelper.log(String.format("活动id=%s 执行结果:%s", couponActivity.getId(), count > 0));
                    }
                }
                else if (couponActivity.getStatus() == CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN
                        || couponActivity.getStatus() == CouponActivityModel.COUPON_ACTIVITY_STATUS_DISABLE)
                {
                    if (!DateUtil.dateCompare(couponActivity.getEnd_time(), new Date()))
                    {
                        //标记活动结束
                        XxlJobHelper.log(String.format("活动id=%s 结束时间%s 标记结束", couponActivity.getId(), DateUtil.dateFormate(couponActivity.getEnd_time(), GloabConst.TimePattern.YMDHMS)));
                        //开启活动
                        couponActivityUpdate.setId(couponActivity.getId());
                        couponActivityUpdate.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_END);
                        int count = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityUpdate);
                        XxlJobHelper.log(String.format("活动id=%s 执行结果:%s", couponActivity.getId(), count > 0));
                        //标记用户优惠卷结束
                        count = couponModalMapper.updateCouponByHid(CouponModal.COUPON_TYPE_EXPIRED, couponActivity.getId());
                        XxlJobHelper.log(String.format("优惠卷id=%s 正在标记结束 执行结果:%s个", couponActivity.getId(), count > 0));
                    }
                }
            }
            clearEndCoupon();
            XxlJobHelper.log("清理已经结束的活动 执行完毕!");
        }
        catch (Exception e)
        {
            logger.error("清理已经结束的活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QLYJJSDHDYC, "清理已经结束的活动 异常", "clearEndActivity");
        }
        finally
        {
            couponActivityModelList = null;
        }
    }

    /**
     * 清空所有过期优惠卷
     *
     * @author Trick
     * @date 2020/12/11 14:56
     */
    private void clearEndCoupon() throws LaiKeAPIException
    {
        List<CouponModal> couponModalList = null;
        CouponModal       coupon          = null;
        try
        {
            XxlJobHelper.log("=========== 清空所有过期优惠卷 执行开始 ===========");
            //获取所有未使用和使用中的优惠卷
            couponModalList = couponModalMapper.getEffectiveCouponInfoList();
            for (CouponModal couponModal : couponModalList)
            {
                //是否已过期
                if (couponModal.getExpiry_time() != null && !DateUtil.dateCompare(couponModal.getExpiry_time(), new Date()))
                {
                    coupon = new CouponModal();
                    coupon.setId(couponModal.getId());
                    coupon.setType(CouponModal.COUPON_TYPE_EXPIRED);
                    int count = couponModalMapper.updateByPrimaryKeySelective(coupon);
                    XxlJobHelper.log(String.format("优惠卷id=%s 正在标记结束 执行结果:%s", coupon.getId(), count > 0));
                }
                else if (CouponModal.COUPON_TYPE_IN_USE.equals(couponModal.getType()))
                {
                    coupon = new CouponModal();
                    coupon.setId(couponModal.getId());
                    coupon.setType(CouponModal.COUPON_TYPE_NOT_USED);
                    int count = couponModalMapper.updateByPrimaryKeySelective(coupon);
                    XxlJobHelper.log(String.format("优惠卷id=%s 正在将使用中的优惠券改为未使用 执行结果:%s", coupon.getId(), count > 0));
                }
            }
            XxlJobHelper.log("=========== 清空所有过期优惠卷 执行完毕! ===========");
        }
        catch (Exception e)
        {
            logger.error("清理已经结束的优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QLYJJSDYHJYC, "清理已经结束的优惠卷 异常", "clearEndCoupon");
        }
        finally
        {
            coupon = null;
            couponModalList = null;
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoPushCoupon() throws LaiKeAPIException
    {
        int                       row;
        List<Integer>             storeIdList             = null;
        List<String>              userIdList              = null;
        List<CouponActivityModel> couponActivityModelList = null;
        //需要保存的的数据
        List<CouponModal> couponListSave = null;
        //保存记录
        List<CouponPresentationRecordModel> couponRecordListSave         = null;
        CouponModal                         couponSave                   = null;
        CouponActivityModel                 couponActivityUpdate         = null;
        CouponPresentationRecordModel       couponPresentationRecordSave = null;
        try
        {
            XxlJobHelper.log("自动发放卡券定时任务 开始执行!");
            storeIdList = compsTaskService.getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("商城【{}】系统自动发放优惠券 开始执行!", storeId);
                //获取当前商城所有用户
                int isAuth = 0;
                userIdList = userBaseMapper.getUserAllByUserId(storeId,isAuth);
                //获取所有可以发放的优惠券
                couponActivityModelList = couponActivityModelMapper.getAutoPushActiveList(storeId, new Date());
                XxlJobHelper.log("当前商城用户量:{},需要发放的系统活动个数:{}", userIdList.size(), couponActivityModelList.size());
                for (CouponActivityModel couponActivity : couponActivityModelList)
                {
                    //需要保存的的数据
                    couponListSave = new ArrayList<>();
                    //保存记录
                    couponRecordListSave = new ArrayList<>();
                    XxlJobHelper.log("====== 优惠券:{},id:{} 开始开始发放 ======", couponActivity.getName(), couponActivity.getId());
                    for (String userId : userIdList)
                    {
                        couponSave = new CouponModal();
                        couponSave.setStore_id(storeId);
                        couponSave.setMch_id(couponSave.getMch_id());
                        couponSave.setUser_id(userId);
                        couponSave.setMoney(couponActivity.getMoney());
                        couponSave.setType(CouponModal.COUPON_TYPE_NOT_USED);
                        couponSave.setStatus(CouponModal.COUPON_STAUS_OPEN);
                        couponSave.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        couponSave.setAdd_time(new Date());
                        couponSave.setMch_id(0);
                        //是否领取后n天失效
                        if (couponActivity.getDay() > 0)
                        {
                            couponSave.setExpiry_time(DateUtil.getAddDate(couponActivity.getDay()));
                        }
                        else if (couponActivity.getStart_time() != null && couponActivity.getEnd_time() != null)
                        {
                            couponSave.setExpiry_time(couponActivity.getEnd_time());
                        }
                        couponSave.setHid(couponActivity.getId());
                        couponSave.setFree_or_not(CouponModal.COUPON_GIVE);
                        couponListSave.add(couponSave);
                    }
                    couponActivityUpdate = new CouponActivityModel();
                    couponActivityUpdate.setId(couponActivity.getId());
                    couponActivityUpdate.setIs_auto_push(DictionaryConst.WhetherMaven.WHETHER_OK);
                    row = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityUpdate);
                    if (row < 1)
                    {
                        XxlJobHelper.log("发放失败!");
                        continue;
                    }
                    row = couponModalMapper.insertList(couponListSave);
                    for (CouponModal couponSave1 : couponListSave)
                    {
                        couponPresentationRecordSave = new CouponPresentationRecordModel();
                        couponPresentationRecordSave.setStore_id(couponSave1.getStore_id());
                        couponPresentationRecordSave.setCoupon_id(couponSave1.getId());
                        couponPresentationRecordSave.setMobile("");
                        couponPresentationRecordSave.setCoupon_activity_id(couponActivity.getId());
                        couponPresentationRecordSave.setUser_id(couponSave1.getUser_id());
                        couponPresentationRecordSave.setActivity_type(couponActivity.getType());
                        couponPresentationRecordSave.setAdd_date(new Date());
                        couponRecordListSave.add(couponPresentationRecordSave);
                    }
                    couponPresentationRecordModelMapper.insertList(couponRecordListSave);

                    XxlJobHelper.log("====== 发放完毕!一共发放:{}张 ======", row);
                }
                XxlJobHelper.log("商城【{}】系统自动发放优惠券 执行完毕!", storeId);
            }
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("系统自动发放优惠券 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QLYJJSDHDYC, "系统自动发放优惠券 异常", "autoPushCoupon");
        }
        finally
        {
            storeIdList = null;
            userIdList = null;
            couponActivityModelList = null;
            //需要保存的的数据
            couponListSave = null;
            //保存记录
            couponRecordListSave = null;
            couponSave = null;
            couponActivityUpdate = null;
            couponPresentationRecordSave = null;
        }
    }

//    @Autowired
//    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private CouponModalMapper couponModalMapper;

    @Autowired
    private CouponPresentationRecordModelMapper couponPresentationRecordModelMapper;

    @Autowired
    private CompsTaskService compsTaskService;

    @Autowired
    private UserBaseMapper userBaseMapper;
}

