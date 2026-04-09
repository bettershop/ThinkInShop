package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserRuleModel;
import com.laiketui.domain.vo.plugin.integral.AddScoreVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 会员通用服务
 *
 * @author wangxian
 */
@Service
public class PublicMemberServiceImpl implements PublicMemberService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public double getMemberGradeRate(String orderType, String userId, int storeId)
    {
        try
        {
            int flag = 0;
            if (StringUtils.isNotEmpty(orderType))
            {
                switch (orderType)
                {
                    case DictionaryConst.OrdersType.ORDERS_HEADER_GM:
                        flag = 1;
                        break;
                    case DictionaryConst.OrdersType.ORDERS_HEADER_PT:
                    case DictionaryConst.OrdersType.ORDERS_HEADER_KJ:
                    case DictionaryConst.OrdersType.ORDERS_HEADER_JP:
                    case DictionaryConst.OrdersType.ORDERS_HEADER_FX:
                    case DictionaryConst.OrdersType.ORDERS_HEADER_MS:
                    case DictionaryConst.OrdersType.ORDERS_HEADER_TH:
                    case DictionaryConst.OrdersType.ORDERS_HEADER_PS:
                    case DictionaryConst.OrdersType.ORDERS_HEADER_DJ:
                        flag = -1;
                        break;
                    default:
                        break;
                }
            }
            // 非普通购买订单不参与会员折扣
            if (flag != 1)
            {
                return 1.0;
            }

            User user = new User();
            user.setStore_id(storeId);
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null || !User.MEMBER.equals(user.getGrade()))
            {
                return 1.0;
            }

            Map<String, Object> memberConfig = memberConfigMapper.getConfig(storeId);
            if (memberConfig == null || memberConfig.isEmpty())
            {
                return 1.0;
            }
            Integer id     = MapUtils.getInteger(memberConfig, "id");
            Integer isOpen = MapUtils.getInteger(memberConfig, "is_open", 0);
            if (id == null || isOpen <= 0)
            {
                return 1.0;
            }

            String discountStr = MapUtils.getString(memberConfig, "member_discount");
            if (StringUtils.isEmpty(discountStr))
            {
                return 1.0;
            }
            BigDecimal discount = new BigDecimal(discountStr);
            return discount.divide(new BigDecimal(10)).doubleValue();
        }
        catch (Exception e)
        {
            logger.error("会员优惠计算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYYHJSSB, "会员优惠计算失败", "getMemberGradeRate");
        }
    }

    @Override
    public void memberSettlement(int storeId, String userId, String sNo, BigDecimal zPrice, int type) throws LaiKeAPIException
    {
        try
        {
            int        row;
            String     event;
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                return;
            }
            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            //积分插件是否打开
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(storeId);
            integralConfigModel.setMch_id(storeMchId);
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            if (integralConfigModel == null || integralConfigModel.getStatus() == 0)
            {
                logger.debug("积分插件未打开 不计算积分");
                return;
            }

            UserRuleModel userRuleModel = new UserRuleModel();
            userRuleModel.setStore_id(storeId);
            userRuleModel = userRuleModelMapper.selectOne(userRuleModel);

            //获取是否开启生日特权 0-不开启 1-开启
            int is_birthday = 0;
            // 获取生日特权积分倍数
            int bir_multiple = 0;

            // 积分发送规则 0-付款后 1-收货后
            int jifen_m = 1;

            //是否打开vip等比例积分开关
            boolean vipProportion = false;
            if (userRuleModel != null)
            {
                //获取是否开启生日特权 0-不开启 1-开启
                is_birthday = userRuleModel.getIs_birthday();
                //生日特权倍数
                bir_multiple = userRuleModel.getBir_multiple();

                //Vip等比例
                vipProportion = userRuleModel.getIs_jifen() == 1;
                //会员等比例积分发送规则 1-付款后 0-收货后
                jifen_m = userRuleModel.getJifen_m() == 0 ? 1 : 0;
            }

            //查询是否是会员身份
            BigDecimal grade = BigDecimal.valueOf(this.getMemberGradeRate(DictionaryConst.OrdersType.ORDERS_HEADER_GM, userId, storeId));

            User user = new User();
            user.setStore_id(storeId);
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYBCZ, "会员不存在");
            }
            AddScoreVo addScoreVo = new AddScoreVo();
            addScoreVo.setType(SignRecordModel.ScoreType.VIP_BUY);
            addScoreVo.setOrderNo(sNo);
            addScoreVo.setUserId(userId);
            addScoreVo.setStoreId(storeId);
            addScoreVo.setScoreOld(user.getScore());

            int score = 0;
            if (BigDecimal.ONE.compareTo(grade) > 0)
            {
                // 生日特权流程 生日当天购买任意商品赠*n积分
                if (is_birthday == 1 && type == 1)
                {
                    if (user.getBirthday() != null)
                    {
                        String birthday = DateUtil.dateFormate(user.getBirthday(), GloabConst.TimePattern.MD);
                        String time     = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.MD);
                        if (time.equals(birthday))
                        {
                            score = zPrice.multiply(new BigDecimal(bir_multiple)).intValue();
                            //记录积分
                            event = String.format("%s 生日期间购物获得%s积分 [%s]", userId, score, "支付后");
                            addScoreVo.setScore(score);
                            addScoreVo.setEvent(event);
                        }
                    }
                }
                else if (vipProportion && jifen_m == type)
                {
                    //vip等比例积分
                    score = zPrice.setScale(0, BigDecimal.ROUND_UP).intValue();
                    //记录积分
                    event = String.format("%s vip等比例购物获得%s积分 [%s]", userId, score, type == 1 ? "支付后" : "收货后");
                    addScoreVo.setScore(score);
                    addScoreVo.setEvent(event);
                }

            }
            if (score > 0)
            {
                //是否冻结积分
                if (integralConfigModel.getGive_status().equals(IntegralConfigModel.GiveStatus.RECEIVING) && integralConfigModel.getAms_time() > 0)
                {
                    addScoreVo.setType(SignRecordModel.ScoreType.INTEGRAL_FROZEN);
                }
                //记录积分
                publicIntegralService.addScore(addScoreVo);
                row = userMapper.updateUserScore(score, storeId, userId);
                if (row < 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYJSSB, "会员结算失败", "memberSettlement");
                }
            }
            else
            {
                //如果没有享受上面vip特权 则享受积分购物规则
                publicIntegralService.giveScore(storeId, userId, sNo, zPrice, user.getScore(), type,orderModel.getSelf_lifting());
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("会员结算 异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYYHJSSB, "会员优惠计算失败", "memberSettlement");
        }
    }

    @Override
    public void returnUserMoney(int storeId, String userId, BigDecimal price, String sNo) throws LaiKeAPIException
    {
        try
        {
            //获取买家信息
            User clientUser = new User();
            clientUser.setUser_id(userId);
            clientUser.setStore_id(storeId);
            clientUser = userBaseMapper.selectOne(clientUser);
            if (clientUser != null)
            {
                String text = clientUser.getUser_id() + "退款" + price + "元余额";
                //买家当前余额
                BigDecimal currentPrice = clientUser.getMoney();
                //余额退还
                int count = userBaseMapper.rechargeUserPrice(clientUser.getId(), price);
                if (count < 1)
                {
                    logger.info("用户退款>余额退还 失败! " + text);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnUserMoney");
                }
                clientUser = userBaseMapper.selectByPrimaryKey(clientUser.getId());
                //查询订单商品名称，店铺名称
                String            pName             = "";
                String            mchName           = "";
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(storeId);
                orderDetailsModel.setR_sNo(sNo);
                List<OrderDetailsModel> list = orderDetailsModelMapper.select(orderDetailsModel);
                if (list != null && list.size() > 0)
                {
                    orderDetailsModel = list.get(0);
                    String sid = orderDetailsModel.getSid();
                    //插件订单使用规格id反查询
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(sid);
                    if (confiGureModel != null)
                    {
                        Integer          pid              = confiGureModel.getPid();
                        ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                        pName = productListModel.getProduct_title();
                        MchModel mchModel = mchModelMapper.selectByPrimaryKey(productListModel.getMch_id());
                        mchName = mchModel.getName();
                    }
                }
                else
                {
                    logger.error("添加钱包记录详情为获取到商品名称，店铺名称。订单号:" + sNo);
                }

                //添加记录详情
                RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                recordDetailsModel.setStore_id(storeId);
                recordDetailsModel.setMoney(price);
                recordDetailsModel.setUserMoney(clientUser.getMoney());
                recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
                recordDetailsModel.setType(RecordDetailsModel.type.ORDER_REFUND);
                recordDetailsModel.setsNo(sNo);
                recordDetailsModel.setMchName(mchName);
                recordDetailsModel.setTitleName(pName);
                recordDetailsModel.setRecordTime(new Date());
                recordDetailsModel.setAddTime(new Date());
                if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PS))
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_YS_ORDER);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_PT) || sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_KT))
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_PT_ORDER);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JP))
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_JP_ORDER);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_FX_ORDER);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_MS))
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_MS_ORDER);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_FS))
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_FS_ORDER);
                }
                else if (sNo.contains(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_IN_ORDER);
                }
                else
                {
                    recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_REFUND);
                }

                Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(storeId, clientUser.getPreferred_currency());
                Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(storeId);
                recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));

                recordDetailsModelMapper.insertSelective(recordDetailsModel);
                //记录一笔退还记录
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(storeId);
                recordModel.setUser_id(userId);
                recordModel.setMoney(price);
                recordModel.setOldmoney(currentPrice);
                recordModel.setEvent(text);
                recordModel.setType(RecordModel.RECORDTYPE_RETURNAMT);
                recordModel.setDetails_id(recordDetailsModel.getId());
                count = recordModelMapper.insertSelective(recordModel);

                if (count < 1)
                {
                    logger.info("用户退款>记录退款信息 失败! " + text);
                }
                else
                {
                    logger.info("用户退款>记录退款 成功! " + text);
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDYHXX, "未找到用户信息", "returnUserMoney");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退还钱包 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnUserMoney");
        }
    }

    @Override
    public void returnUserMoney(int storeId, String userId, BigDecimal price, Integer orderId, boolean isTempOrder) throws LaiKeAPIException
    {
        try
        {
            //获取买家信息
            User clientUser = new User();
            clientUser.setUser_id(userId);
            clientUser.setStore_id(storeId);
            Date date = new Date();
            clientUser = userBaseMapper.selectOne(clientUser);
            if (clientUser != null)
            {
                String text = clientUser.getUser_id() + "退款" + price + "元余额";
                //买家当前余额
                BigDecimal currentPrice = clientUser.getMoney();
                //余额退还
                int count = userBaseMapper.rechargeUserPrice(clientUser.getId(), price);
                if (count < 1)
                {
                    logger.info("用户退款>余额退还 失败! " + text);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnUserMoney");
                }
                clientUser = userBaseMapper.selectByPrimaryKey(clientUser.getId());
                //添加充值记录详情
                RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                recordDetailsModel.setStore_id(storeId);
                recordDetailsModel.setMoney(price);
                recordDetailsModel.setUserMoney(clientUser.getMoney());
                recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
                recordDetailsModel.setRecordTime(date);
                recordDetailsModel.setAddTime(date);

                if (orderId != null && isTempOrder)
                {
                    OrderDataModel orderDataOld = orderDataModelMapper.selectByPrimaryKey(orderId);
                    if (orderDataOld != null)
                    {
                        Map<String, Object> auctionInfo = JSON.parseObject(orderDataOld.getData(), new TypeReference<Map<String, Object>>()
                        {
                        });
                        String specialId = MapUtils.getString(auctionInfo, "specialId");
                        recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.AUCTION_ACTIVITIES);
                        recordDetailsModel.setType(RecordDetailsModel.type.EVENT_REFUND);
                        recordDetailsModel.setActivityCode(specialId);
                        recordDetailsModel.setTypeName("竞拍保证金退款");
                    }
                }
                else if (orderId != null)
                {
                    OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(orderId);
                    if (orderDetailsModel != null)
                    {
                        OrderModel orderModel = new OrderModel();
                        orderModel.setsNo(orderDetailsModel.getR_sNo());
                        orderModel.setStore_id(storeId);
                        orderModel = orderModelMapper.selectOne(orderModel);
                        //店铺id
                        int      mchId    = StringUtils.stringParseInt(StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH));
                        MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                        switch (orderModel.getOtype())
                        {
                            case DictionaryConst.OrdersType.ORDERS_HEADER_KT:
                            case DictionaryConst.OrdersType.ORDERS_HEADER_PT:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_PT_ORDER);
                                break;
                            case DictionaryConst.OrdersType.ORDERS_HEADER_PS:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_YS_ORDER);
                                break;
                            case DictionaryConst.OrdersType.ORDERS_HEADER_VI:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_VI_ORDER);
                                break;
                            case DictionaryConst.OrdersType.ORDERS_HEADER_ZB:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_ZB_ORDER);
                                break;
                            case DictionaryConst.OrdersType.ORDERS_HEADER_FX:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_FX_ORDER);
                                break;
                            case DictionaryConst.OrdersType.ORDERS_HEADER_FS:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_FS_ORDER);
                                break;
                            case DictionaryConst.OrdersType.ORDERS_HEADER_MS:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_MS_ORDER);
                                break;
                            case DictionaryConst.OrdersType.ORDERS_HEADER_IN:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_IN_ORDER);
                                break;
                            default:
                                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_REFUND);
                                break;
                        }
                        recordDetailsModel.setType(RecordDetailsModel.type.ORDER_REFUND);
                        recordDetailsModel.setTitleName(orderDetailsModel.getP_name());
                        recordDetailsModel.setsNo(orderModel.getsNo());
                        recordDetailsModel.setMchName(mchModel.getName());
                    }
                }

                Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(storeId, clientUser.getPreferred_currency());
                Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(storeId);
                recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));

                recordDetailsModelMapper.insert(recordDetailsModel);
                //记录一笔退还记录
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(storeId);
                recordModel.setUser_id(userId);
                recordModel.setMoney(price);
                recordModel.setOldmoney(currentPrice);
                recordModel.setEvent(text);
                recordModel.setType(RecordModel.RecordType.REFUND);
                recordModel.setDetails_id(recordDetailsModel.getId());
                recordModel.setAdd_date(date);
                count = recordModelMapper.insertSelective(recordModel);

                if (count < 1)
                {
                    logger.info("用户退款>记录退款信息 失败! " + text);
                }
                else
                {
                    logger.info("用户退款>记录退款 成功! " + text);
                }
            }
            else
            {
                logger.debug("余额退款给用户【{}】失败 原因:未找到用户信息", userId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDYHXX, "未找到用户信息", "returnUserMoney");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退还钱包 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnUserMoney");
        }
    }

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private UserRuleModelMapper userRuleModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private PublicIntegralService publicIntegralService;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MemberConfigMapper memberConfigMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

}
