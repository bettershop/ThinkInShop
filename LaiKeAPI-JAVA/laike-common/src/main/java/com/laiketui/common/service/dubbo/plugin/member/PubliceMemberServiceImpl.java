package com.laiketui.common.service.dubbo.plugin.member;

import com.alibaba.fastjson2.JSONArray;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.api.plugin.member.PubliceMemberService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.plugin.member.BuyMemberVo;
import com.laiketui.domain.vo.plugin.member.MemberOrderVo;
import com.laiketui.domain.vo.plugin.member.OpenConfigVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 会员公共方法
 *
 * @author sunH_
 */
@Service
public class PubliceMemberServiceImpl implements PubliceMemberService
{

    private final Logger logger = LoggerFactory.getLogger(PubliceMemberServiceImpl.class);

    @Autowired
    private MemberConfigMapper memberConfigMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicIntegralService publicIntegralService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;


    @Override
    public void sendPoints(User user, Integer memberType, Integer storeId, OrderDataModel orderDataModel) throws LaiKeAPIException
    {
        try
        {
            //会员插件配置
            Map<String, Object> config = memberConfigMapper.getConfig(storeId);
            //开会员赠送积分
            String memberTypeDesc = "";
            switch (memberType)
            {
                case 1:
                    memberTypeDesc = "月卡";
                    break;
                case 2:
                    memberTypeDesc = "季卡";
                    break;
                case 3:
                    memberTypeDesc = "年卡";
                    break;
                default:
                    break;
            }
            Integer isOpen = MapUtils.getInteger(config, "is_open");
            if (isOpen == DictionaryConst.WhetherMaven.WHETHER_OK)
            {
                String             openConfig       = MapUtils.getString(config, "open_config");
                List<OpenConfigVo> openConfigVoList = JSONArray.parseArray(openConfig, OpenConfigVo.class);
                for (OpenConfigVo vo : openConfigVoList)
                {
                    if (vo.getOpenMethod().equals(memberTypeDesc))
                    {
                        //积分明细
                        SignRecordModel signRecordModel = new SignRecordModel();
                        signRecordModel.setStore_id(storeId);
                        signRecordModel.setUser_id(user.getUser_id());
                        signRecordModel.setSign_score(vo.getPoints());
                        signRecordModel.setTotal_score(user.getScore());
                        signRecordModel.setRecord(user.getUser_id() + "开通" + memberTypeDesc + "会员获得" + vo.getPoints() + "积分");
                        signRecordModel.setType(SignRecordModel.ScoreType.OPEN_MEMBER);
                        signRecordModel.setsNo(orderDataModel.getTrade_no());
                        signRecordModel.setScore_invalid(null);
                        signRecordModel.setSign_time(new Date());
                        signRecordModelMapper.insertSelective(signRecordModel);

                        user.setScore(user.getScore() + vo.getPoints());
                        userBaseMapper.updateByPrimaryKey(user);

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
            logger.error("开通会员赠送积分 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "sendPoints");
        }
    }

    @Override
    public void doublePoints(String userId, Integer storeId, BigDecimal price, String sNo) throws LaiKeAPIException
    {
        try
        {
            int integral = 0;
            //订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                return;
            }
            // 店铺ID
            String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            //积分插件是否打开
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(storeId);
            integralConfigModel.setMch_id(storeMchId);
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            //禅道38569
//            if (integralConfigModel == null || integralConfigModel.getStatus() == 0) {
//                logger.debug("积分插件未打开 不计算积分");
//                return;
//            }
            PluginsModel pluginsModel = new PluginsModel();
            pluginsModel.setStore_id(storeId);
            pluginsModel.setPlugin_code(DictionaryConst.Plugin.INTEGRAL);
            pluginsModel = pluginsModelMapper.selectOne(pluginsModel);
            if (pluginsModel == null || mchId == null
                    //未授权店铺  只有自营店可以获取积分 禅道53990
                    || (pluginsModel.getStatus().equals(DictionaryConst.WhetherMaven.WHETHER_NO) && !mchId.equals(storeMchId.toString())))
            {
                logger.debug("未授权店铺  只有自营店可以获取积分");
                return;
            }
            User select = new User();
            select.setStore_id(storeId);
            select.setUser_id(userId);
            User user = userBaseMapper.selectOne(select);
            if (Objects.isNull(user))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "doublePoints");
            }
            //会员插件配置
            Map<String, Object> config = memberConfigMapper.getConfig(storeId);
            Integer             id     = MapUtils.getInteger(config, "id");
            if (id != null)
            {
                Integer birthdayOpen   = MapUtils.getInteger(config, "birthday_open");
                String  pointsMultiple = MapUtils.getString(config, "points_multiple");
                if (birthdayOpen.equals(DictionaryConst.WhetherMaven.WHETHER_OK) && !pointsMultiple.equals("0") && user.getGrade().equals(User.MEMBER))
                {
                    if (user.getBirthday() != null)
                    {
                        String userBirthday = DateUtil.dateFormate(user.getBirthday(), GloabConst.TimePattern.MD);
                        String today        = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.MD);
                        if (today.equals(userBirthday))
                        {
                            BigDecimal multiply = price.multiply(new BigDecimal(pointsMultiple));
                            integral = multiply.intValue();
                            if (integral == 0)
                            {
                                integral = 1;
                            }
                            //积分明细
                            SignRecordModel signRecordModel = new SignRecordModel();
                            signRecordModel.setStore_id(storeId);
                            signRecordModel.setUser_id(user.getUser_id());
                            signRecordModel.setSign_score(multiply.intValue());
                            signRecordModel.setTotal_score(user.getScore());
                            signRecordModel.setRecord(user.getUser_id() + "会员生日特权奖励获得" + multiply.intValue() + "积分");
                            signRecordModel.setType(SignRecordModel.ScoreType.MEMBER_BIRTHDAY);
                            signRecordModel.setsNo(sNo);
                            signRecordModel.setScore_invalid(null);
                            signRecordModel.setSign_time(new Date());
                            signRecordModelMapper.insertSelective(signRecordModel);
                            user.setScore(user.getScore() + multiply.intValue());
//                            //禅道50113 先到冻结积分 等订单结算后转入积分
//                            user.setLock_score(user.getLock_score() + multiply.intValue());
                            userBaseMapper.updateByPrimaryKey(user);
                        }
                    }
                }
            }
//            if (integral <= 0 && integralConfigModel != null && integralConfigModel.getStatus() != 0){
            //禅道 53990
            if (integral <= 0 && integralConfigModel != null)
            {
                //如果没有享受上面vip特权 则享受积分购物规则
                publicIntegralService.giveScore(storeId, userId, sNo, price, user.getScore(), IntegralConfigModel.GiveStatus.PAYMENT,orderModel.getSelf_lifting());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员生日特权奖励 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "doublePoints");
        }
    }

    @Override
    public BigDecimal vipPrice(Integer storeId, BigDecimal price) throws LaiKeAPIException
    {
        BigDecimal vipPrice = BigDecimal.ZERO;
        try
        {
            Map<String, Object> config = memberConfigMapper.getConfig(storeId);
            Integer             id     = MapUtils.getInteger(config, "id");
            if (id != null)
            {
                String     memberDiscount = MapUtils.getString(config, "member_discount");
                BigDecimal divide         = price.multiply(new BigDecimal(memberDiscount)).divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_DOWN);
                if (divide.compareTo(vipPrice) <= 0)
                {
                    divide = new BigDecimal("0.01");
                }
                vipPrice = divide;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员生日特权奖励 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "doublePoints");
        }
        return vipPrice;
    }

    @Override
    public MemberOrderVo getOrderData(User user, BuyMemberVo vo) throws LaiKeAPIException
    {
        MemberOrderVo memberOrderVo = new MemberOrderVo();
        try
        {
            memberOrderVo.setStoreId(vo.getStoreId());
            memberOrderVo.setUserId(user.getUser_id());
            memberOrderVo.setAmount(vo.getAmount());
            memberOrderVo.setMemberType(vo.getMemberType());
            if (user.getGrade().equals(User.MEMBER) && String.valueOf(DictionaryConst.WhetherMaven.WHETHER_NO).equals(user.getIs_out()))
            {
                logger.debug("会员已存在到期时间为{}", DateUtil.dateFormate(user.getGrade_end(), GloabConst.TimePattern.YMDHMS));
                memberOrderVo.setStartTime(DateUtil.dateFormate(user.getGrade_end(), GloabConst.TimePattern.YMDHMS));
                //记录是续费会员
                memberOrderVo.setIs_renew(1);
                switch (vo.getMemberType())
                {
                    case 1:
                        logger.debug("会员已存在到期时间增加30天{}", DateUtil.dateFormate(DateUtil.getAddDate(user.getGrade_end(), 30), GloabConst.TimePattern.YMDHMS));
                        memberOrderVo.setEndTime(DateUtil.dateFormate(DateUtil.getAddDate(user.getGrade_end(), 30), GloabConst.TimePattern.YMDHMS));
                        break;
                    case 2:
                        logger.debug("会员已存在到期时间增加90天{}", DateUtil.dateFormate(DateUtil.getAddDate(user.getGrade_end(), 90), GloabConst.TimePattern.YMDHMS));
                        memberOrderVo.setEndTime(DateUtil.dateFormate(DateUtil.getAddDate(user.getGrade_end(), 90), GloabConst.TimePattern.YMDHMS));
                        break;
                    case 3:
                        logger.debug("会员已存在到期时间增加365天{}", DateUtil.dateFormate(DateUtil.getAddDate(user.getGrade_end(), 365), GloabConst.TimePattern.YMDHMS));
                        memberOrderVo.setEndTime(DateUtil.dateFormate(DateUtil.getAddDate(user.getGrade_end(), 365), GloabConst.TimePattern.YMDHMS));
                        break;
                    default:
                        break;
                }
            }
            else
            {
                memberOrderVo.setStartTime(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
                switch (vo.getMemberType())
                {
                    case 1:
                        memberOrderVo.setEndTime(DateUtil.dateFormate(DateUtil.getAddDate(new Date(), 30), GloabConst.TimePattern.YMDHMS));
                        break;
                    case 2:
                        memberOrderVo.setEndTime(DateUtil.dateFormate(DateUtil.getAddDate(new Date(), 90), GloabConst.TimePattern.YMDHMS));
                        break;
                    case 3:
                        memberOrderVo.setEndTime(DateUtil.dateFormate(DateUtil.getAddDate(new Date(), 365), GloabConst.TimePattern.YMDHMS));
                        break;
                    default:
                        break;
                }
            }
            memberOrderVo.setPayType(vo.getPayType());
            memberOrderVo.setCouponId(vo.getCouponId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员生日特权奖励 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "doublePoints");
        }
        return memberOrderVo;
    }
}

