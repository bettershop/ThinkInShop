package com.laiketui.apps.user.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.apps.api.user.AppsUserRechargeService;
import com.laiketui.common.api.PublicCouponService;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.PayMappingUtils;
import com.laiketui.common.utils.okhttp.HttpUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.config.Config;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DefaultImagesValues;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.*;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.ExtensionModel;
import com.laiketui.domain.coupon.CouponActivityModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.user.*;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.goods.ProductShareVo;
import com.laiketui.domain.vo.user.GradeOrderVo;
import com.laiketui.root.common.BuilderIDTool;
import com.laiketui.root.license.Md5Util;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

/**
 * 会员制实现
 *
 * @author Trick
 * @date 2020/12/21 17:49
 */
@Service
public class AppsUserRechargeServiceImpl implements AppsUserRechargeService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Config config;


    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取最低充值金额
            FinanceConfigModel financeConfigModel = new FinanceConfigModel();
            financeConfigModel.setStore_id(vo.getStoreId());
            financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
            if (financeConfigModel != null)
            {
                resultMap.put("min_cz", financeConfigModel.getMin_cz());
                List<Map<String, Object>> payment      = paymentConfigModelMapper.getPaymentTypeInfo(vo.getStoreId());
                Map<String, String>       payStatusMap = new HashMap<>(16);
                Map<String, String> defaultpayment = new HashMap<>(16);
                for (Map<String, Object> map : payment)
                {
                    String key = map.get("class_name").toString();
                    payStatusMap.put(key, map.get("status").toString());
                    int isdefaultpay = DataUtils.getIntegerVal(map, "isdefaultpay", 2);
                    if (isdefaultpay == 1)
                    {
                        defaultpayment.put("defaultpayName", PayMappingUtils.getPayName(key));
                    }
                }
                resultMap.put("payment", payStatusMap);
                resultMap.put("defaultpayment", defaultpayment);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取我的详细数据 异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> grade(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //开通方式
                List<String> openMethod = new ArrayList<>();
                //会员规则
                String htmlRule = "";
                //是否设置支付密码
                int isPayPwd = 0;
                //是否开通会员
                int isOpenVip = 0;
                //会员等级
                Integer vipLeve = null;

                //获取所有会员等级
                List<Map<String, Object>> userGradeModelList = userGradeModelMapper.getUserGradeAll(vo.getStoreId());

                //获取所有支付状态
                List<Map<String, Object>> payStatusList = paymentModelMapper.getPaymentStatusAll(vo.getStoreId());
                Map<String, String>       payStatusMap  = new HashMap<>(16);
                for (Map<String, Object> map : payStatusList)
                {
                    payStatusMap.put(map.get("class_name").toString(), map.get("status").toString());
                }

                //获取开通方式
                UserRuleModel userRuleModel = new UserRuleModel();
                userRuleModel.setStore_id(vo.getStoreId());
                userRuleModel = userRuleModelMapper.selectOne(userRuleModel);
                if (userRuleModel != null)
                {
                    openMethod = Arrays.asList(userRuleModel.getMethod().split(","));
                    htmlRule = userRuleModel.getRule();
                }
                //判断每个等级的开通方式是否在设置中开启
                for (Map<String, Object> map : userGradeModelList)
                {
                    BigDecimal tempAmt = new BigDecimal("0");
                    //包月
                    if (!openMethod.contains("1") || new BigDecimal(map.get("money").toString()).compareTo(tempAmt) == 0)
                    {
                        map.remove("money");
                    }
                    //包季
                    if (!openMethod.contains("2") || new BigDecimal(map.get("money_j").toString()).compareTo(tempAmt) == 0)
                    {
                        map.remove("money_j");
                    }
                    //包年
                    if (!openMethod.contains("3") || new BigDecimal(map.get("money_n").toString()).compareTo(tempAmt) == 0)
                    {
                        map.remove("money_n");
                    }

                    map.put("imgurl", publiceService.getImgPath(map.get("imgurl") + "", vo.getStoreId()));
                    map.put("imgurl_s", publiceService.getImgPath(map.get("imgurl_s") + "", vo.getStoreId()));
                }
                //是否设置支付密码
                if (!StringUtils.isEmpty(user.getPassword()))
                {
                    isPayPwd = 1;
                }
                //判断用户是否已开通会员 1-已开通  0-未开通
                if (user.getGrade() != null)
                {
                    isOpenVip = 1;
                    vipLeve = user.getGrade();
                }
                //是否启用钱包支付
                boolean isWalletOpen = publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.WALLET, null);

                resultMap.put("payment", payStatusMap);
                resultMap.put("data", userGradeModelList);
                resultMap.put("is_wallet", isWalletOpen ? 1 : 0);
                resultMap.put("is_password", isPayPwd);
                resultMap.put("rule", htmlRule);
                resultMap.put("is_open", isOpenVip);
                resultMap.put("grade_id", vipLeve);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员等级列表展示 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "grade");
        }

        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> gradeOrder(GradeOrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //获取会员等级信息
                UserGradeModel userGradeModel = new UserGradeModel();
                userGradeModel.setId(vo.getId());
                userGradeModel = userGradeModelMapper.selectOne(userGradeModel);
                if (userGradeModel == null)
                {
                    logger.info("id{}会员卡不存在", vo.getId());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "gradeOrder");
                }

                //获取vip金额
                BigDecimal vipAmt = publicUserService.orderTotal(vo.getStoreId(), vo.getId(), user.getUser_id(), vo.getFlag(), vo.getMethod());
                if (vipAmt.doubleValue() <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "gradeOrder");
                }

                if (DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY.equals(vo.getPayType()))
                {
                    //推送
                    String msgTitle   = "";
                    String msgContent = "";
                    //余额支付
                    if (StringUtils.isEmpty(user.getPassword()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WSZMM, "未设置密码", "gradeOrder");
                    }
                    //验证支付密码
                    if (!user.getPassword().equals(Md5Util.MD5endoce(vo.getPassword())))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFMMBZQ, "支付密码不正确", "gradeOrder");
                    }
                    //验证用户余额是否充足
                    if (user.getMoney().doubleValue() < vipAmt.doubleValue())
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEBZ, "余额不足", "gradeOrder");
                    }
                    //过期时间
                    Date gradeEnd = null;
                    //时长
                    Date gradeDate = null;
                    //判断是续费，充值，还是升级  根据选择充值时长，继续到期时间
                    if (vo.getFlag() == 1)
                    {
                        //充值
                        msgTitle = "充值会员成功";
                        gradeDate = new Date();
                    }
                    else if (vo.getFlag() == 2)
                    {
                        //续费
                        msgTitle = "续费会员成功";
                        gradeDate = user.getGrade_end();
                    }
                    else
                    {
                        //升级
                        msgTitle = "升级会员成功";
                    }
                    msgContent = "您已升级" + userGradeModel.getName() + "成功，快去享受特权吧";
                    if (vo.getFlag() != 3)
                    {
                        //包月、季、年
                        if (vo.getMethod() == 1)
                        {
                            gradeEnd = DateUtil.getAddMonth(gradeDate, 1);
                        }
                        else if (vo.getMethod() == 2)
                        {
                            gradeEnd = DateUtil.getAddMonth(gradeDate, 3);
                        }
                        else
                        {
                            gradeEnd = DateUtil.getAddMonth(gradeDate, 12);
                        }
                    }
                    //更新会员等级
                    User updateUser = new User();
                    updateUser.setId(user.getId());
                    updateUser.setGrade(vo.getId());
                    updateUser.setGrade_add(new Date());
                    updateUser.setGrade_m(String.valueOf(vo.getMethod()));
                    updateUser.setIs_box(String.valueOf(1));
                    updateUser.setIs_out(String.valueOf(0));
                    if (vo.getFlag() != 3)
                    {
                        updateUser.setGrade_end(gradeEnd);
                    }
                    //会员首次开通，或者升级 记录处理
                    UserRuleModel userRuleModel = new UserRuleModel();
                    userRuleModel.setStore_id(vo.getStoreId());
                    userRuleModel = userRuleModelMapper.selectOne(userRuleModel);
                    //获取是否开启会员赠送商品
                    int isproduct = 0;
                    if (userRuleModel != null)
                    {
                        isproduct = userRuleModel.getIs_product();
                    }
                    if (vo.getFlag() != 2)
                    {
                        //非续费流程 判断用户是否是第一次开通、升级会员等级
                        UserFirstModal userFirstModal = new UserFirstModal();
                        userFirstModal.setStore_id(vo.getStoreId());
                        userFirstModal.setLevel(vo.getId());
                        userFirstModal.setUser_id(user.getUser_id());
                        int count = userFirstModalMapper.selectCount(userFirstModal);

                        if (count < 1)
                        {
                            //第一次开通
                            logger.debug("用户{} 第一次开通/升级 grade-id:{}", user.getUser_id(), vo.getId());
                            //获取优惠卷活动信息
                            CouponActivityModel couponActivityModel = new CouponActivityModel();
                            couponActivityModel.setStore_id(vo.getStoreId());
                            couponActivityModel.setGrade_id(vo.getId());
                            //所有券都赠送
                            List<CouponActivityModel> couponActivityModels = couponActivityModelMapper.select(couponActivityModel);
                            if (couponActivityModels != null && couponActivityModels.size() > 0)
                            {
                                List<String> userIdList = new ArrayList<>();
                                userIdList.add(user.getUser_id());
                                for (CouponActivityModel couponActivity : couponActivityModels)
                                {
                                    try
                                    {
                                        if (!publicCouponService.giveCoupons(vo.getStoreId(), userIdList, couponActivity.getId()))
                                        {
                                            logger.info("会员第次一开通会员赠送优惠卷失败! 等级id={}", vo.getId());
                                        }
                                    }
                                    catch (LaiKeAPIException l)
                                    {
                                        logger.info("会员第一次开通,赠送优惠卷失败 失败原因:{}", l.getMessage());
                                    }
                                }
                            }
                            else
                            {
                                logger.debug("系统未找到等级赠送活动 等级id={}", vo.getId());
                            }
                            //是否赠送商品
                            if (isproduct == 1)
                            {
                                //商品兑换卷过期时间 默认7天
                                Date goodsCouponEndTime = DateUtil.getAddDate(7);
                                if (userRuleModel.getValid() != null && userRuleModel.getValid() > 0)
                                {
                                    goodsCouponEndTime = DateUtil.getAddDate(userRuleModel.getValid());
                                }
                                //准备记录赠送记录
                                userFirstModal = new UserFirstModal();
                                userFirstModal.setStore_id(vo.getStoreId());
                                userFirstModal.setUser_id(user.getUser_id());
                                userFirstModal.setGrade_id(vo.getId());
                                userFirstModal.setLevel(vo.getId());
                                userFirstModal.setEnd_time(goodsCouponEndTime);

                                if (userGradeModel.getPro_id() != null && userGradeModel.getPro_id() > 0)
                                {
                                    //获取商品库存信息
                                    Map<String, Object> goodsListInfoMap = confiGureModelMapper.getGoodsConfigureList(vo.getStoreId(), userGradeModel.getPro_id());
                                    if (goodsListInfoMap != null)
                                    {
                                        int attrId = Integer.parseInt(goodsListInfoMap.get("attr_id").toString());
                                        int num    = Integer.parseInt(goodsListInfoMap.get("num").toString());
                                        //库存是否充足
                                        if (num > 1)
                                        {
                                            count = confiGureModelMapper.reduceGoodsStockNum(-1, attrId);
                                            if (count < 1)
                                            {
                                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCKJSB, "库存扣减失败", "gradeOrder");
                                            }
                                            userFirstModal.setAttr_id(attrId);
                                        }
                                        else
                                        {
                                            logger.info("库存不足,赠送失败 userid:{}", user.getUser_id());
                                        }
                                    }
                                }
                                else
                                {
                                    logger.info("没有设置赠送的商品,赠送失败 userid:{}", user.getUser_id());
                                }
                            }
                            else
                            {
                                logger.info("当前商城未开启商品兑换卷赠送");
                            }
                            count = userFirstModalMapper.insertSelective(userFirstModal);
                            if (count < 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCKJSB, "库存扣减失败", "gradeOrder");
                            }
                        }
                        else
                        {
                            logger.info("userid:{} 已经开通过会员id{}", user.getUser_id(), vo.getId());
                        }
                    }
                    //更新会员信息
                    int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYKTSB, "会员开通失败", "gradeOrder");
                    }
                    //开始余额支付
                    if (!publicUserService.balancePay(vo.getAccessId(), vipAmt, "充值会员等级", RecordModel.BUYING_MEMBERS))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEZFSB, "余额支付失败", "gradeOrder");
                    }
                    // TODO: 2020/12/23 推送暂时不做

                    resultMap.put("msg", msgTitle);
                }
                else
                {
                    String              orderno = DictionaryConst.OrdersType.ORDERS_HEADER_DJ + BuilderIDTool.getNext(BuilderIDTool.Type.NUMBER, 19);
                    Map<String, Object> dataMap = new HashMap<>(16);
                    dataMap.put("order_id", orderno);
                    dataMap.put("user_id", user.getUser_id());
                    dataMap.put("trade_no", orderno);
                    dataMap.put("pay", vo.getPayType());
                    dataMap.put("total", vipAmt);
                    dataMap.put("store_id", vo.getStoreId());
                    dataMap.put("grade_id", vo.getId());
                    dataMap.put("method", vo.getMethod());
                    dataMap.put("flag", vo.getFlag());
                    dataMap.put("tui_id", vo.getTuiId());

                    //非余额支付 添加一条临时订单
                    OrderDataModel orderDataModel = new OrderDataModel();
                    orderDataModel.setTrade_no(orderno);
                    orderDataModel.setData(SerializePhpUtils.JavaSerializeByPhp(dataMap));
                    orderDataModel.setAddtime(new Date());
                    int count = orderDataModelMapper.insertSelective(orderDataModel);
                    if (count > 0)
                    {
                        resultMap.put("title", userGradeModel.getName() + "充值");
                        resultMap.put("sNo", orderno);
                        resultMap.put("total", vipAmt);
                        resultMap.put("pay_type", DictionaryConst.OrdersType.ORDERS_HEADER_DJ);
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败", "gradeOrder");
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
            logger.error("会员等级订单 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "gradeOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> upgrade(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取会员等级信息
            Map<String, Object> userGreadInfo = userBaseMapper.getUserGradeExpire(vo.getStoreId(), user.getUser_id());
            if (userGreadInfo != null && !userGreadInfo.isEmpty())
            {
                //是否开启支付
                int isWallet = 0;
                //会员规则
                String htmlRule = "";
                //当前会员的打折率
                BigDecimal userRate = new BigDecimal(userGreadInfo.get("rate").toString());

                //获取开通方式
                UserRuleModel userRuleModel = new UserRuleModel();
                userRuleModel.setStore_id(vo.getStoreId());
                userRuleModel = userRuleModelMapper.selectOne(userRuleModel);
                if (userRuleModel != null)
                {
                    isWallet = userRuleModel.getIs_wallet();
                    htmlRule = userRuleModel.getRule();
                }
                //查询出可升级的会员等级
                List<Map<String, Object>> userGradeModelMap = userGradeModelMapper.getUserGradeRateInfo(vo.getStoreId(), userRate);
                //计算差价
                for (Map<String, Object> map : userGradeModelMap)
                {
                    int gradeId = MapUtils.getIntValue(map, "id");
                    //获取当前升级vip所需金额
                    BigDecimal vipAmt = publicUserService.orderTotal(vo.getStoreId(), gradeId, user.getUser_id(), 3, 0);
                    if (vipAmt.doubleValue() < 0)
                    {
                        logger.debug("非法数据 当前升级vip所需金额为 {}", vipAmt);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "gradeOrder");
                    }
                    map.put("money", vipAmt);

                    map.put("money_up", 1);
                    map.remove("money_j");
                    map.remove("money_n");
                    String imageUrl  = publiceService.getImgPath(String.valueOf(map.get("imgurl")), vo.getStoreId());
                    String imageUrls = publiceService.getImgPath(String.valueOf(map.get("imgurl_s")), vo.getStoreId());
                    map.put("imgurl", imageUrl);
                    map.put("imgurl_s", imageUrls);
                    map.put("method", userGreadInfo.get("grade_m"));
                }

                //是否设置支付密码
                int isPayPwd = 0;
                if (!StringUtils.isEmpty(user.getPassword()))
                {
                    isPayPwd = 1;
                }
                List<Map<String, Object>> payment      = paymentConfigModelMapper.getPaymentTypeInfo(vo.getStoreId());
                Map<String, String>       payStatusMap = new HashMap<>(16);
                Map<String, String> defaultpayment = new HashMap<>(16);
                for (Map<String, Object> map : payment)
                {
                    String key = map.get("class_name").toString();
                    payStatusMap.put(key, map.get("status").toString());
                    int isdefaultpay = DataUtils.getIntegerVal(map, "isdefaultpay", 2);
                    if (isdefaultpay == 1)
                    {
                        defaultpayment.put("defaultpayName", PayMappingUtils.getPayName(key));
                    }
                }
                resultMap.put("data", userGradeModelMap);
                resultMap.put("payment", payStatusMap);
                resultMap.put("defaultpayment", defaultpayment);
                resultMap.put("is_wallet", isWallet);
                resultMap.put("is_password", isPayPwd);
                resultMap.put("rule", htmlRule);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员升价渲染等级接口 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upgrade");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> gradeCenter(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> userMap = new HashMap<>(16);
            userMap.put("user_id", user.getUser_id());
            userMap.put("user_name", user.getUser_name());
            userMap.put("wx_name", user.getWx_name());
            userMap.put("headimgurl", user.getHeadimgurl());
            userMap.put("grade", user.getGrade());
            userMap.put("grade_end", DateUtil.dateFormate(user.getGrade_end(), GloabConst.TimePattern.YMD));
            //获取用户当前会员等级
            int grade = user.getGrade();
            if (grade == 0)
            {
                userMap.put("grade", "普通会员");
                userMap.put("imgurl", "");
                userMap.put("level", 0);
            }
            else
            {
                UserGradeModel userGradeModel = new UserGradeModel();
                userGradeModel.setStore_id(vo.getStoreId());
                userGradeModel.setId(grade);
                userGradeModel = userGradeModelMapper.selectOne(userGradeModel);
                if (userGradeModel != null)
                {
                    userMap.put("grade", userGradeModel.getName());
                    userMap.put("rate", userGradeModel.getRate());
                    String imageUrl = userGradeModel.getImgurl();
                    imageUrl = publiceService.getImgPath(imageUrl, vo.getStoreId());
                    userMap.put("imgurl", imageUrl);
                    String imageUrls = userGradeModel.getImgurl_s();
                    imageUrls = publiceService.getImgPath(imageUrls, vo.getStoreId());
                    userMap.put("imgurl_s", imageUrls);

                    userMap.put("font_color", userGradeModel.getFont_color());
                    userMap.put("date_color", userGradeModel.getDate_color());
                }
                else
                {
                    logger.info("会员卡id={}不存在", grade);
                }
            }
            //会员规则 0-不可分享 1-可以分享
            int share = 1;
            //是否可以升级
            int isUp = 0;
            //规则详情
            String htmlRule = "";
            //海报
            String posterUrl = "";

            UserRuleModel userRuleModel = new UserRuleModel();
            userRuleModel.setStore_id(vo.getStoreId());
            userRuleModel = userRuleModelMapper.selectOne(userRuleModel);
            if (userRuleModel != null)
            {
                htmlRule = userRuleModel.getRule();
                posterUrl = publiceService.getImgPath(userRuleModel.getPoster(), vo.getStoreId());
                String imageUrl = userRuleModel.getPoster();
                userRuleModel.setPoster(publiceService.getImgPath(imageUrl, vo.getStoreId()));
                if (userRuleModel.getIs_limit() == 1)
                {
                    //判断当前会员是否达到会员推荐级别
                    UserGradeModel userGradeModel = new UserGradeModel();
                    userGradeModel.setStore_id(vo.getStoreId());
                    userGradeModel.setId(userRuleModel.getLevel());
                    userGradeModel = userGradeModelMapper.selectOne(userGradeModel);
                    if (userGradeModel != null && userGradeModel.getRate() != null)
                    {
                        //获取最低折扣会员卡
                        List<Map<String, Object>> lowRateMap = userGradeModelMapper.getUserGradeLowRateInfo(vo.getStoreId(), userGradeModel.getRate());
                        for (Map<String, Object> map : lowRateMap)
                        {
                            int id = Integer.parseInt(map.get("id").toString());
                            if (id == user.getGrade())
                            {
                                share = 1;
                                break;
                            }
                            else
                            {
                                share = 0;
                            }
                        }
                    }
                }
                //判断是否可以升级
                if (userRuleModel.getUpgrade() != null)
                {
                    if (userRuleModel.getUpgrade().contains("2"))
                    {
                        //获取会员等级信息
                        Map<String, Object> userGreadInfo = userBaseMapper.getUserGradeExpire(vo.getStoreId(), user.getUser_id());
                        BigDecimal          rate          = new BigDecimal("10");
                        if (userGreadInfo.containsKey("rate"))
                        {
                            rate = new BigDecimal(userGreadInfo.get("rate").toString());
                        }
                        int count = userGradeModelMapper.getUserGradeRateInfoCount(vo.getStoreId(), rate);
                        if (count > 0)
                        {
                            isUp = 1;
                        }
                    }
                }
                //判断是否可以升级 end
            }

            List<Object> list = new ArrayList<>();
            list.add(userMap);
            resultMap.put("data", list);
            resultMap.put("rule", htmlRule);
            resultMap.put("share", share);
            resultMap.put("share_img", posterUrl);
            resultMap.put("up", isUp);
            //获取特惠商品
            vo.setPageStart(0);
            resultMap.put("product", getMore(vo).get("data"));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员等级中心 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "gradeCenter");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getMore(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取特惠商品
            PageModel                 pageModel      = new PageModel(vo.getPageNo(), vo.getPageSize());
            List<Map<String, Object>> tehuiGoodsList = productListModelMapper.getGoodsTeHuiInfo(vo.getStoreId(), pageModel);
            for (Map<String, Object> map : tehuiGoodsList)
            {
                String image = map.get("imgurl").toString();
                image = publiceService.getImgPath(image, vo.getStoreId());
                map.put("imgurl", image);
                //商品规格最低价
                BigDecimal minGoodsPrice = new BigDecimal(MapUtils.getString(map, "price"));
                //获取会员折扣价
                BigDecimal grade           = new BigDecimal(publicMemberService.getMemberGradeRate(null, user.getUser_id(), vo.getStoreId()) + "");
                BigDecimal gradeGoodsPrice = minGoodsPrice.multiply(grade);

                map.put("yprice", minGoodsPrice);
                map.put("price", gradeGoodsPrice);
            }
            resultMap.put("data", tehuiGoodsList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取更多特惠商品信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMore");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> closeAutomaticRenewal(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String msg  = "关闭提示失败";
            User   user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                User updateUser = new User();
                updateUser.setId(user.getId());
                updateUser.setIs_box("0");
                int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
                if (count > 0)
                {
                    msg = "关闭提示成功";
                }
            }
            resultMap.put("msg", msg);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("关闭自动续费 异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "closeAutomaticRenewal");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> share(MainVo vo, Integer storeType, String httpUrl) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //封装跳转链接
            Map<String, Object> parmaData = new HashMap<>(2);
            parmaData.put("isfx", 1);
            parmaData.put("tui_id", user.getUser_id());
            String qrCodeUrl = "pagesA/vip/vip?data=%s";
            qrCodeUrl = String.format(qrCodeUrl, JSON.toJSONString(parmaData));

            //图片路径
            String imageUrl = "";

            //如果是微信小程序则获取小程序分享码
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(storeType)))
            {
//                imageUrl = publiceService.getWeiXinAppQrcode(vo.getStoreId(), storeType, qrCodeUrl, 50);
                logger.debug("生成小程序二维码路径:" + imageUrl);
            }
            else if (DictionaryConst.StoreSource.LKT_LY_011.equals(String.valueOf(storeType)))
            {
                //获取app域名
                ConfigModel configModel = new ConfigModel();
                configModel.setStore_id(vo.getStoreId());
                configModel = configModelMapper.selectOne(configModel);
                if (configModel != null)
                {
                    qrCodeUrl = configModel.getH5_domain() + qrCodeUrl;
                }
                //获取log
                imageUrl = publiceService.getStoreQrcode(vo.getStoreId(), storeType, qrCodeUrl);
            }
            if (StringUtils.isEmpty(imageUrl))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMHQSB, "二维码获取失败");
            }
            //主图
            BufferedImage mainImg = ImageTool.builderBaseMap(300, 487, Color.white);

            UserRuleModel userRuleModel = new UserRuleModel();
            userRuleModel.setStore_id(vo.getStoreId());
            userRuleModel = userRuleModelMapper.selectOne(userRuleModel);
            String backImageUrl = userRuleModel.getPoster();
            if (backImageUrl == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXHBBJTPWSZ, "分享海报,背景,图片,未设置", "share");
            }
            //获取logo url
            backImageUrl = publiceService.getImgPath(backImageUrl, vo.getStoreId()) + "?x-oss-process=image/resize,w_300,limit_0";
            //二维码 本地方式
            File rqCodeFile = new File(imageUrl);
            //合并图片 主图
            int           imgBackGroundX = 550;
            int           imgBackGroundY = 887;
            BufferedImage topImage       = Thumbnails.of(HttpUtils.getFile(backImageUrl, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgBackGroundX, imgBackGroundY).asBufferedImage();
            //合并图片 二维码
            int           imgQrcodeGroundX = 240;
            int           imgQrcodeGroundY = 240;
            BufferedImage qrcodeImage      = Thumbnails.of(rqCodeFile).size(imgQrcodeGroundX, imgQrcodeGroundY).asBufferedImage();

            String shareCodeName = BuilderIDTool.getGuid() + "_share.png";

            //存储路径
            String path = "static" + File.separator + shareCodeName;
            //外网地址
            httpUrl += File.separator + shareCodeName;
            //服务器路径
            String mchShareQrcodePath = ResourceUtils.getURL("classpath:").getPath() + path;
            Thumbnails
                    //打开一张图片作为底图
                    .of(mainImg)
                    //水印在底图上面的位置 第三个参数是图片的可视度百分比
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(0, 0), topImage, 1f)
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(95, 328), qrcodeImage, 1f)
                    //输出图片的压缩百分比
                    .outputQuality(0.8f)
                    //输出图片的大小
                    .size(1024, 682)
                    //输出文件的位置和名称
                    .toFile(mchShareQrcodePath);

            logger.debug("二维码路径:" + imageUrl);
            logger.debug("合成后的二维码本地路径:" + mchShareQrcodePath);

            //上传到oss
            List<MultipartFile> fileList = new ArrayList<>();
            MultipartFile       file;
            try (InputStream stream = new FileInputStream(mchShareQrcodePath))
            {
                file = new MockMultipartFile("logo", BuilderIDTool.getGuid() + "_share.png" + "." + GloabConst.UploadConfigConst.IMG_JPEG, MediaType.IMAGE_JPEG_VALUE, stream);
            }
            fileList.add(file);
            List<String> imageUrlList = publiceService.uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, vo.getStoreType(), vo.getStoreId());
            String       logoImageUrl = imageUrlList.get(0);
            if (logoImageUrl == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片生成失败");
            }
            logger.debug("合成后的二维码外网路径:" + logoImageUrl);

            resultMap.put("imgUrl", logoImageUrl);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员制分享 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "share");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> productShare(ProductShareVo vo, String httpUrl) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //跳转路径
            String qrCodeUrl = vo.getPath();
            //商品图片
            String goodsImageUrl = null;
            //商品价格
            BigDecimal goodsPrice = null;
            //商品标题
            String goodsTitle = "";

            if (vo.getType() == 4)
            {
                return getFenXiaoShare(vo, httpUrl);
            }
            if (DictionaryConst.OrdersType.ORDERS_HEADER_JP.equals(vo.getProductType()))
            {
                //获取竞拍商品信息 TODO 这块代码逻辑有问题
//                AuctionProductModel auctionProductModel = new AuctionProductModel();
//                auctionProductModel.setStore_id(vo.getStoreId());
//                auctionProductModel.setId(vo.getAuctionId());
//                AuctionProductModel goodsInfo = auctionProductModelMapper.selectOne(auctionProductModel);
//                if (goodsInfo == null) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JPSPBCZ, "竞拍商品不存在", "productShare");
//                }
//                goodsImageUrl = publiceService.getImgPath(goodsInfo., vo.getStoreId());
//                goodsPrice = auctionProductModel.getPrice();
//                goodsTitle = auctionProductModel.getTitle();
            }
            else
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("pid", vo.getPid());
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                List<Map<String, Object>> goodsInfo = productListModelMapper.getProductListJoinConfigureDynamic(parmaMap);
                if (goodsInfo == null || goodsInfo.isEmpty())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "productShare");
                }
                for (Map<String, Object> map : goodsInfo)
                {
                    goodsImageUrl = publiceService.getImgPath(String.valueOf(map.get("imgurl")), vo.getStoreId());
                    goodsPrice = new BigDecimal(map.get("price").toString());
                    goodsTitle = map.get("product_title").toString();
                }
            }
            if (goodsTitle.length() > 18)
            {
                goodsTitle = goodsTitle.substring(0, 18) + "...";
            }
            //根据商城ID、海报类型、来源、默认，查询推广图片表
            ExtensionModel expressModel = extensionModelMapper.getExtensionModel(vo.getStoreId(), vo.getType(), vo.getStoreType());
            if (expressModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDTGXX, "未找到推广信息", "productShare");
            }
            //如果是微信小程序则获取小程序分享码
            String weixinQrcode = "";
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                if (StringUtils.isEmpty(vo.getPath()))
                {
                    logger.debug("微信小程序码 scene参数是必填!!!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMCSCW, "二维码参数错误");
                }
                String pagePath  = vo.getPath().substring(0, vo.getPath().indexOf("?"));
                String parameter = vo.getPath().substring(vo.getPath().indexOf("?") + 1);
                if (StringUtils.isEmpty(pagePath))
                {
                    logger.debug("微信小程序码 page参数是必填!!!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMCSCW, "二维码参数错误");
                }
                if (StringUtils.isEmpty(parameter))
                {
                    logger.debug("微信小程序码 scene参数是必填!!!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMCSCW, "二维码参数错误");
                }
                weixinQrcode = publiceService.getWeiXinAppQrcode(vo.getStoreId(), vo.getStoreType(), parameter, pagePath, 50);
            }
            else if (DictionaryConst.StoreSource.LKT_LY_011.equals(String.valueOf(vo.getStoreType())))
            {
                //获取log
                weixinQrcode = publiceService.getStoreQrcode(vo.getStoreId(), vo.getStoreType(), qrCodeUrl);
            }
            if (StringUtils.isEmpty(weixinQrcode))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMHQSB, "二维码获取失败", "share");
            }
            //合并图片 生成底图
            int imgBackGroundX = 550;
            int imgBackGroundY = 978;
            //边界 x、y
            int[]         margin    = new int[]{50, 50};
            BufferedImage mainImage = ImageTool.builderBaseMap(imgBackGroundX, imgBackGroundY, Color.WHITE);

            //文字1
            String uname = user.getUser_name() + " 分享";
            //文字2
            String title = goodsTitle;
            //文字3
            String price = goodsPrice.toString();
            //文字4
            String text4 = "扫描二维码查看";
            //文字5
            String text5 = "来客推提供技术支持";
            //文字6
            String text6 = "www.laiketui.com";

            //合并图片 logo
            int           imgLogoGroundX = 100;
            int           imgLogoGroundY = 100;
            BufferedImage logoImage      = Thumbnails.of(HttpUtils.getFile(ImgUploadUtils.OOS_DEFAULT_IMAGE_NAME_LOGO, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgLogoGroundX, imgLogoGroundY).asBufferedImage();

            Font titleFont = new Font("微软雅黑", Font.PLAIN, 30);
            Font t5Font    = new Font("微软雅黑", Font.PLAIN, 15);
            Font t4Font    = new Font("微软雅黑", Font.PLAIN, 25);
            ImageTool.imageWatermark(mainImage, uname, 20, 625, Color.BLACK, new Font("微软雅黑", Font.PLAIN, 30));
            ImageTool.imageWatermark(mainImage, title, 100, 600, Color.BLACK, titleFont);
            ImageTool.imageWatermark(mainImage, text4, 360, 825, Color.GRAY, t4Font);
            ImageTool.imageWatermark(mainImage, price, 50, 825, Color.RED, new Font("微软雅黑", Font.BOLD, 35));
            ImageTool.imageWatermark(mainImage, text5, 200, 925, Color.GRAY, t5Font);
            ImageTool.imageWatermark(mainImage, text6, 200, 950, Color.GRAY, t5Font);

            //合并图片 用户头像
            int           imgUserGroundX = 80;
            int           imgUserGroundY = 80;
            BufferedImage userImage      = Thumbnails.of(HttpUtils.getFile(user.getHeadimgurl(), GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgUserGroundX, imgUserGroundY).asBufferedImage();
            //合并图片 商城app二维码
            int           imgQrcodeGroundX = 100;
            int           imgQrcodeGroundY = 100;
            BufferedImage qrcodeImage      = Thumbnails.of(HttpUtils.getFile(qrCodeUrl, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgQrcodeGroundX, imgQrcodeGroundY).asBufferedImage();
            //合并图片 商品图片
            int           imgGoodsGroundX = imgBackGroundX - 1;
            int           imgGoodsGroundY = imgBackGroundY / 2;
            BufferedImage goodsImage      = Thumbnails.of(HttpUtils.getFile(goodsImageUrl, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgGoodsGroundX, imgGoodsGroundY).asBufferedImage();


            String shareCodeName        = BuilderIDTool.getGuid() + "_share.png";
            String goodsShareQrcodePath = config.getUploadPath() + File.separator + shareCodeName;
            Thumbnails
                    //打开一张图片作为底图
                    .of(mainImage)
                    //水印在底图上面的位置 第三个参数是图片的可视度百分比
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(margin[0], margin[1]), goodsImage, 1f)
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(20, 25), logoImage, 1f)
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(20, 625), userImage, 1f)
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(360, 600), qrcodeImage, 1f)
                    //输出图片的压缩百分比
                    .outputQuality(0.8f)
                    //输出图片的大小
                    .size(imgBackGroundX, imgBackGroundY)
                    //输出文件的位置和名称
                    .toFile(goodsShareQrcodePath);
            System.out.println("商品分享二维码路径：" + goodsShareQrcodePath);
            ImgUploadUtils.givePower(GloabConst.LinuxPower.RW_XR_ALL, goodsShareQrcodePath);

            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                ConfigModel configModel = new ConfigModel();
                configModel.setStore_id(vo.getStoreId());
                configModel = configModelMapper.selectOne(configModel);
                if (configModel != null)
                {
                    shareCodeName = configModel.getH5_domain() + SplitUtils.FXG + shareCodeName;
                    logger.debug("(分销)我的推广二维码外网路径 {}", shareCodeName);
                }

            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("制作商品分享带参数 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "productShare");
        }
        return resultMap;
    }

    /**
     * 获取分销推广二维码
     *
     * @param vo      -
     * @param httpUrl -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/23 17:19
     */
    private Map<String, Object> getFenXiaoShare(ProductShareVo vo, String httpUrl) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取商城名称
            CustomerModel customerModel = new CustomerModel();
            customerModel.setId(vo.getStoreId());
            customerModel = customerModelMapper.selectOne(customerModel);

            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWPZ, "商城未配置");
            }
            //封装跳转链接
            String qrCodeUrl = vo.getPath();
            //图片路径
            String imageUrl = "";

            //如果是微信小程序则获取小程序分享码
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                if (StringUtils.isEmpty(vo.getPath()))
                {
                    logger.debug("微信小程序码 scene参数是必填!!!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMCSCW, "二维码参数错误");
                }
                String pagePath  = vo.getPath().substring(0, vo.getPath().indexOf("?"));
                String parameter = vo.getPath().substring(vo.getPath().indexOf("?") + 1);
                if (StringUtils.isEmpty(pagePath))
                {
                    logger.debug("微信小程序码 page参数是必填!!!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMCSCW, "二维码参数错误");
                }
                if (StringUtils.isEmpty(parameter))
                {
                    logger.debug("微信小程序码 scene参数是必填!!!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMCSCW, "二维码参数错误");
                }
                imageUrl = publiceService.getWeiXinAppQrcode(vo.getStoreId(), vo.getStoreType(), parameter, pagePath, 50);
                logger.debug("生成小程序二维码路径:" + imageUrl);
            }
            else
            {
                if (DictionaryConst.StoreSource.LKT_LY_011.equals(String.valueOf(vo.getStoreType()))
                        || DictionaryConst.StoreSource.LKT_LY_002.equals(String.valueOf(vo.getStoreType())))
                {
                    //获取app域名
                    qrCodeUrl = configModel.getH5_domain() + qrCodeUrl;
                }
                //生成带logo二维码
                imageUrl = publiceService.getStoreQrcode(vo.getStoreId(), vo.getStoreType(), qrCodeUrl);
            }
            if (StringUtils.isEmpty(imageUrl))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMHQSB, "二维码获取失败");
            }
            //主图
            int           imgMainGroundX = 1356;
            int           imgMainGroundY = 2256;
            BufferedImage mainImg        = ImageTool.builderBaseMap(imgMainGroundX, imgMainGroundY, Color.white);
            //背景图
            BufferedImage backImg = Thumbnails.of(HttpUtils.getFile(DefaultImagesValues.OOS_DEFAULT_SHAREBG, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgMainGroundX, imgMainGroundY).asBufferedImage();
            //用户头像
            int           imgUserGroundX = 250;
            int           imgUserGroundY = 250;
            String        userHeader     = user.getHeadimgurl() + "?x-oss-process=image/resize,m_fixed,h_250,w_250/circle,r_100/format,png";
            BufferedImage userImage      = Thumbnails.of(HttpUtils.getFile(userHeader, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgUserGroundX, imgUserGroundY).asBufferedImage();

            //二维码
            File rqCodeFile = new File(imageUrl);
            //合并图片 二维码
            int           imgQrcodeGroundX = 790;
            int           imgQrcodeGroundY = 790;
            BufferedImage qrcodeImage      = Thumbnails.of(rqCodeFile).size(imgQrcodeGroundX, imgQrcodeGroundY).asBufferedImage();

            //文字信息
            String text1  = String.format("我是 %s", user.getUser_name());
            Font   font1  = new Font("微软雅黑", Font.PLAIN, 70);
            int    font1X = ImageTool.getWordWidth(font1, text1);
            String text2  = String.format("我为 %s 代言", customerModel.getName());
            Font   font2  = new Font("微软雅黑", Font.PLAIN, 70);
            int    font2X = ImageTool.getWordWidth(font2, text2);

            ImageTool.imageWatermark(backImg, text1, (imgMainGroundX - (font1X)) / 2 + 15, 390, Color.BLACK, font1);
            ImageTool.imageWatermark(backImg, text2, (imgMainGroundX - (font2X)) / 2 + 15, 490, Color.BLACK, font2);

            //图片名称
            String shareCodeName = BuilderIDTool.getGuid() + "_share.png";
            //存储路径
            String path = "static" + File.separator + shareCodeName;
            //外网地址
            httpUrl += config.getUploadPath() + File.separator + shareCodeName;
            //服务器路径
            String mchShareQrcodePath = config.getUploadPath() + File.separator + shareCodeName;
            Thumbnails
                    //打开一张图片作为底图
                    .of(mainImg)
                    //水印在底图上面的位置 第三个参数是图片的可视度百分比
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(0, 0), backImg, 1f)
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(570, 10), userImage, 1f)
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(295, 528), qrcodeImage, 1f)
                    //输出图片的压缩百分比
                    .outputQuality(0.8f)
                    //输出图片的大小
                    .size(imgMainGroundX, imgMainGroundY)
                    //输出文件的位置和名称
                    .toFile(mchShareQrcodePath);


            ImgUploadUtils.givePower(GloabConst.LinuxPower.RW_XR_ALL, mchShareQrcodePath);
            if (System.getProperty("os.name").toLowerCase().startsWith("linux"))
            {
                // 使用ProcessBuilder避免命令注入
                ProcessBuilder processBuilder = new ProcessBuilder("chmod", "777", "-R", config.getUploadPath());
                logger.info("执行权限命令 {}", processBuilder.command());
                processBuilder.start();
            }
            logger.debug("二维码路径:" + imageUrl);
            logger.debug("合成后的二维码本地路径:" + mchShareQrcodePath);
            logger.debug("合成后的二维码路径:" + httpUrl);
            shareCodeName = GloabConst.UploadConfigConst.IMG_PATH_MAIN + File.separator + shareCodeName;

            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                shareCodeName = configModel.getH5_domain() + shareCodeName;
                logger.debug("(分销)我的推广二维码外网路径 {}", shareCodeName);
            }
            resultMap.put("url", shareCodeName);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("获取分销推广二维码 自定义异常 ", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分销推广二维码 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getFenXiaoShare");
        }
        return resultMap;
    }


    @Autowired
    private FinanceConfigModelMapper financeConfigModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private UserRuleModelMapper userRuleModelMapper;

    @Autowired
    private UserFirstModalMapper userFirstModalMapper;

    @Autowired
    private PublicCouponService publicCouponService;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private AuctionProductModelMapper auctionProductModelMapper;

    @Autowired
    private ExtensionModelMapper extensionModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;
}

