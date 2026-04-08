package com.laiketui.common.service.dubbo.plugin.integral;

import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.vo.plugin.integral.AddScoreVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 积分相关
 *
 * @author Trick
 * @date 2021/11/9 17:48
 */
@Service
public class PublicIntegralServiceImpl implements PublicIntegralService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Override
    public void addScore(AddScoreVo vo) throws LaiKeAPIException
    {
        try
        {
            String          event           = vo.getEvent();
            SignRecordModel signRecordModel = new SignRecordModel();
            signRecordModel.setStore_id(vo.getStoreId());
            signRecordModel.setUser_id(vo.getUserId());
            signRecordModel.setSign_score(vo.getScore());
            signRecordModel.setTotal_score(vo.getScoreOld());
            signRecordModel.setRecord(event);
            signRecordModel.setType(vo.getType());
            signRecordModel.setsNo(vo.getOrderNo());
            signRecordModel.setScore_invalid(vo.getScoreInvalidDate());
            signRecordModel.setSign_time(new Date());
            signRecordModel.setFrozen_time(vo.getFrozenTime());
            signRecordModelMapper.insertSelective(signRecordModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("记录增减积分流水 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addScore");
        }
    }

    @Override
    public void giveScore(int storeId, String userId, String orderNo, BigDecimal orderPrice, int userScore, int type,Integer self_lifting) throws LaiKeAPIException
    {
        try
        {
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(orderNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            int mchId = Integer.parseInt(StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH).split(SplitUtils.DH)[0]);
            //默认获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            //积分确认收货获取积分
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(storeId);
            integralConfigModel.setMch_id(storeMchId);
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            //禅道 53990
//            if (integralConfigModel != null && integralConfigModel.getStatus() == 1 && integralConfigModel.getGive_status() == type) {
            //4097 【JAVA开发环境】积分：移动端--积分设置的收货后1天，购买无需核销的虚拟订单，付款后冻结明细没有记录；
            if (integralConfigModel != null && integralConfigModel.getGive_status() == type ||
                    (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equalsIgnoreCase(orderModel.getOtype()) && orderModel.getSelf_lifting() == 4))
            {
                AddScoreVo scoreVo = new AddScoreVo();
                scoreVo.setStoreId(storeId);
                scoreVo.setUserId(userId);
                scoreVo.setScoreOld(userScore);
                scoreVo.setType(SignRecordModel.ScoreType.VIP_BUY);
                //是否冻结积分
                int addSec = 0;
                outFrozen:
                if (integralConfigModel.getGive_status().equals(IntegralConfigModel.GiveStatus.RECEIVING) && integralConfigModel.getAms_time() >= 0)
                {
                    //计算冻结结束时间=确认收货时间+当时设置的冻结天数(冻结天数>售后时间) or 当时设置的售后时间(售后时间<=冻结天数)
                    Date frozenTime = new Date();
                    //订单收货时间
                    Date orderArriveTime = orderModelMapper.getOrderArriveTime(storeId, orderNo);
                    if(orderArriveTime == null ){
                        orderArriveTime = frozenTime;
                    }
                    //订单当时设置的售后时间
                    Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchId, DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                    if (configMap != null)
                    {
                        //售后日期 单位【秒】
                        int orderAfterSec = addSec = MapUtils.getInteger(configMap, "orderAfter");
                        //冻结天数转换成【秒】
                        int frozenSec = DateUtil.dateConversion(integralConfigModel.getAms_time(), DateUtil.TimeType.DAY);
                        if (frozenSec > orderAfterSec)
                        {
                            addSec = frozenSec;
                        }
                        //禅道4128::【JAVA开发环境】积分：移动端--积分设置的收货后1天，购买需要核销的虚拟订单，核销完成冻结结束时间应该是收货后天数而不是售后天数
                        if (orderModel.getSelf_lifting() == 3)
                        {
                            addSec = frozenSec;
                        }
                        //冻结天数和售后天数都为0的时候直接发放
                        if (addSec <= 0)
                        {
                            break outFrozen;
                        }
                    }
                    if (Objects.nonNull(self_lifting) && self_lifting == 1)
                    {
                        addSec = integralConfigModel.getAms_time() * 86400;
                    }
                    frozenTime = DateUtil.getAddDateBySecond(orderArriveTime, addSec);
                    logger.debug("订单{} 获取的积分被冻结,解冻时间为:{}", orderNo, DateUtil.dateFormate(frozenTime, GloabConst.TimePattern.YMDHMS));
                    //记录冻结天数
                    scoreVo.setType(SignRecordModel.ScoreType.INTEGRAL_FROZEN);
                    scoreVo.setFrozenTime(frozenTime);
                }
                scoreVo.setOrderNo(orderNo);
                //计算所得积分 购物赠送积分=购物交易金额*赠送比例
                BigDecimal score = orderPrice.multiply(integralConfigModel.getProportion()).divide(new BigDecimal("100"), 0, BigDecimal.ROUND_UP);
                if (score.compareTo(BigDecimal.ZERO) > 0)
                {
                    //积分过期时间
                    Date scoreInvalidDate = null;
                    if (integralConfigModel.getOverdue_time() != null && integralConfigModel.getOverdue_time() > 0)
                    {
                        scoreInvalidDate = DateUtil.getAddDateBySecond(new Date(), integralConfigModel.getOverdue_time());
                        //如果是冻结积分则加上冻结天数,积分过期是冻结解冻后开始算
                        if (scoreVo.getFrozenTime() != null)
                        {
                            scoreInvalidDate = DateUtil.getAddDateBySecond(scoreInvalidDate, addSec);
                        }
                    }
                    String event = "";
                    scoreVo.setScore(score.intValue());
                    scoreVo.setEvent(event);
                    scoreVo.setScoreInvalidDate(scoreInvalidDate);
                    this.addScore(scoreVo);
                    //如果积分为不冻结，才去修改用户表的积分字段
                    if (scoreVo.getFrozenTime() == null)
                    {
                        int row = userMapper.updateUserScore(score.intValue(), storeId, userId);
                        if (row < 0)
                        {
                            logger.debug("{} 购物赠送积分失败", event);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "giveScore");
                        }
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("赠送积分 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "giveScore");
        }
    }
}

