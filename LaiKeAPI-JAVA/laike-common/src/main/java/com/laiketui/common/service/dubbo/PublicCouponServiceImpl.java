package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.laiketui.common.api.PublicCouponService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.plugin.CouponDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.*;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.coupon.*;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserGradeModel;
import com.laiketui.domain.user.UserRuleModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.coupon.AddCouponActivityVo;
import com.laiketui.domain.vo.coupon.CouponParmaVo;
import com.laiketui.domain.vo.coupon.SeeCouponVo;
import com.laiketui.root.gateway.util.I18nUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.phprpc.util.PHPSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 优惠卷公共流程
 *
 * @author Trick
 * @date 2020/10/30 15:00
 */
@Service
public class PublicCouponServiceImpl implements PublicCouponService
{

    private final Logger logger = LoggerFactory.getLogger(PublicCouponServiceImpl.class);

    @Autowired
    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private CouponModalMapper couponModelMapper;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private CouponOrderModalMapper couponOrderModalMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private UserFirstModalMapper userFirstModalMapper;

    @Autowired
    private UserRuleModelMapper userRuleModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private CouponPresentationRecordModelMapper couponPresentationRecordModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;


    @Override
    public Integer index(int storeId) throws LaiKeAPIException
    {
        int isStatus = 0;
        try
        {
            //当前时间
            String sysdateStr = FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS).format(new Date());
            Date   sysDate    = FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS).parse(sysdateStr);

            //获取优惠卷状态
            CouponConfigModel couponModel = new CouponConfigModel();
            couponModel.setStore_id(storeId);
            couponModel.setMch_id(0);
            couponModel = couponConfigModelMapper.selectOne(couponModel);
            if (couponModel != null)
            {
                isStatus = couponModel.getIs_status();
            }
            //获取该商城所有优惠卷
            CouponActivityModel couponActivityModel = new CouponActivityModel();
            couponActivityModel.setStore_id(storeId);
            couponActivityModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<CouponActivityModel> couponActivityModelList = couponActivityModelMapper.getCouponActivityAll(couponActivityModel);
            for (CouponActivityModel couponActivity : couponActivityModelList)
            {
                couponActivityModel = new CouponActivityModel();
                couponActivityModel.setId(couponActivity.getId());
                if (couponActivity.getStatus().equals(CouponActivityModel.COUPON_ACTIVITY_STATUS_NOT_USE))
                {
                    //未启用插件流程
                    //如果优惠券活动开始时间 >= 当前时间 则修改优惠卷为开启状态
                    couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
                }
                else
                {
                    //所有启用或禁用的优惠卷 流程
                    //判断活动是否已结束，如果结束则标记为结束状态
                    if (!Objects.isNull(couponActivity.getEnd_time()))
                    {
                        if (couponActivity.getEnd_time().getTime() <= sysDate.getTime())
                        {
                            couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_END);
                        }
                        else
                        {
                            continue;
                        }
                    }
                }
                if (couponActivityModel.getStatus() != null)
                {
                    //修改优惠卷状态
                    int count = couponActivityModelMapper.updateSwitchCouponActivity(couponActivityModel);
                    if (count < 1)
                    {
                        logger.debug("优惠卷状态修改失败 id:" + couponActivity.getId());
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
            logger.error("优惠卷流程异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "coupon");
        }

        return isStatus;
    }


    @Override
    public Map<String, Object> settlementStoreCoupons(int storeId, String userId, List<Map<String, Object>> products, String couponIds, boolean canshu, int is_self_delivery) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            //优惠金额
            BigDecimal preferentialAmount = DataUtils.ZERO_BIGDECIMAL;
            //平台优惠券id
            String couponId0 = "0";
            //临时变量
            List<String> couponIdList0 = new ArrayList<>();
            //返回的优惠券变量
            List<String> couponIdList = new ArrayList<>();
            int          pos          = 0;
            for (Map<String, Object> mchProductInfo : products)
            {
                // 店铺ID
                int mchId = DataUtils.getIntegerVal(mchProductInfo, "shop_id");
//                mchProductInfo.put("shop_logo", publiceService.getImgPath(MapUtils.getString(mchProductInfo, "shop_logo"), storeId));
                // 光商品的总价
                BigDecimal mchProductTotal = DataUtils.getBigDecimalVal(mchProductInfo, "product_total");
                // 购买商品的分类信息
                Map<Integer, String> mchProductClassMap = new HashMap<>(16);
                //购买商品的商品ID
                List<String> mchProductId = new ArrayList<>();
                //店铺总运费,如果总运费是0则是免邮
                BigDecimal mchYunfei = new BigDecimal(MapUtils.getString(mchProductInfo, "freight_price"));
                //只是商品信息列表，排除了mchProductInfo中关于商店的信息
                List<Map<String, Object>> oneMchProducts = (List<Map<String, Object>>) mchProductInfo.get("list");
                for (Map<String, Object> product : oneMchProducts)
                {
                    //给商品列表的每个商品加上折扣和优惠券id
                    product.put("discount", 0);
                    product.put("coupon_id", 0);
                    //商品id
                    int goodsId = MapUtils.getIntValue(product, "pid");
                    // 购买商品的分类信息
                    mchProductClassMap.put(goodsId, DataUtils.getStringVal(product, "product_class"));
                    // 购买商品的商品ID
                    mchProductId.add(String.valueOf(goodsId));
                }

                String couponId = "0";
                if (StringUtils.isNotEmpty(couponIds))
                {
                    couponIdList0 = Splitter.on(SplitUtils.DH).omitEmptyStrings().trimResults().splitToList(couponIds);
                    //对应product里面的
                    //获取第一张优惠券，店铺优惠券
                    int couponPos = couponIdList0.size() - 1;
                    //获取第二张优惠券，平台优惠券
                    couponId0 = couponIdList0.get(couponPos);
                    if (pos < couponPos)
                    {
                        couponId = couponIdList0.get(pos);
                    }
                }

                couponIdList.add(couponId);
                // 获取用户满足条件的店铺优惠券
                Map<String, Object> userCoupons = getStoreCoupons(storeId, userId, mchId, oneMchProducts, mchProductTotal, mchProductClassMap,
                        mchProductId, mchYunfei, couponId, canshu, is_self_delivery);
                mchProductInfo.put("list", userCoupons.get("products_list"));
                List<Map<String, Object>> arr = (List<Map<String, Object>>) userCoupons.get("list");
                //未使用优惠劵-自动选择最优优惠劵
                if (couponId.equals("0") && !canshu)
                {
                    //有可用优惠劵--优惠金额最大的最靠前
                    if (arr.size() >= 1)
                    {
                        arr.get(0).put("coupon_status", true);
                        //修改默认不使用优惠劵
                        arr.get(arr.size() - 1).put("coupon_status", false);

                    }
                }
                mchProductInfo.put("coupon_list", arr);

                //不使用优惠券的情况 运费+商品金额
                BigDecimal shopMoney = mchYunfei.add(mchProductTotal);
                if (!CollectionUtils.isEmpty(arr))
                {
                    //使用优惠卷
                    for (Map<String, Object> couponInfo : arr)
                    {
                        //优惠券是否使用标识 1表示当前使用的优惠券
                        Boolean couponStatus = DataUtils.getBooleanVal(couponInfo, "coupon_status");
                        //当前循环的优惠券id
                        String id = DataUtils.getStringVal(couponInfo, "coupon_id");
                        couponInfo.put("coupon_id", StringUtils.trim(id, SplitUtils.DH));
                        if (couponStatus && !"0".equals(id))
                        {
                            //获取优惠金额
                            BigDecimal money = DataUtils.getBigDecimalVal(couponInfo, "money");
                            couponIdList.set(couponIdList.size() - 1, (String) couponInfo.get("coupon_id"));
                            //使用优惠卷 商品总金额-优惠金额=优惠后的金额
                            shopMoney = shopMoney.subtract(money);
                            //优惠后的价格须大于0 禅道 45837 (小于0 或者小于运费)
                            if (BigDecimal.ZERO.compareTo(shopMoney) >= 0 || mchYunfei.compareTo(shopMoney) > 0)
                            {
                                shopMoney = mchYunfei;
                            }
                            preferentialAmount = preferentialAmount.add(money);
                            //免邮处理 运费不计算在优惠金额里面
                            String activityType = MapUtils.getString(couponInfo, "activityType");
                            if (CouponModal.COUPON_TYPE_MY.equals(activityType))
                            {
                                mchProductInfo.put("isFreeFreight", true);
                                mchProductInfo.put("freight_price", BigDecimal.ZERO);
                                //使用免邮卷，店铺商品价格等于商品总价 禅道45838
//                                shopMoney = mchProductTotal;
                                //上面注释了导致问题-禅道54222
                                shopMoney = mchProductTotal;
                            }

                        }
                    }
                }
                mchProductInfo.put("shop_subtotal", shopMoney);
                pos++;
            }
            couponIdList.add(couponId0);
            resultMap.put("coupon_id", org.apache.commons.lang3.StringUtils.join(couponIdList, SplitUtils.DH));
            resultMap.put("products", products);
            resultMap.put("preferential_amount", preferentialAmount);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺优惠券筛选 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPYHQSXSB, "店铺优惠券筛选失败", "settlementStoreCoupons");
        }
        return resultMap;
    }


    /**
     * 获取店铺优惠券
     *
     * @param storeId
     * @param userId
     * @param mchId
     * @param productsList       - 当前店铺订单下所有的商品
     * @param mchProductTotal
     * @param mchProductClassMap - 商品分类信息 [key=商品id,val=分类id "-?-"]
     * @param mchProductId
     * @param mchYunfei          - 当前运费
     * @param couponId
     * @param canshu
     * @return
     * @throws LaiKeAPIException
     */
    private Map<String, Object> getStoreCoupons(int storeId, String userId, int mchId, List<Map<String, Object>> productsList, BigDecimal mchProductTotal,
                                                Map<Integer, String> mchProductClassMap, List<String> mchProductId, BigDecimal mchYunfei, String couponId,
                                                boolean canshu, int is_self_delivery) throws LaiKeAPIException
    {
        Map<String, Object> retMap = new HashMap<>();
        try
        {
            // 当前时间
            Date                      time = new Date();
            List<Map<String, Object>> arr  = new ArrayList<>();
            List<Map<String, Object>> arr1 = new ArrayList<>();

            // 查询该用户有多少使用中的优惠券属于该店铺
            Map<String, Object> map = new HashMap<>(16);
            map.put("store_id", storeId);
            map.put("user_id", userId);
            map.put("mch_id", mchId);
            //商品不能使用的优惠券，例如会员券
            List<Integer> notTypeList = new ArrayList<>();
            notTypeList.add(CouponActivityModel.USE_RANG_TYPE_VIP);
            map.put("notTypeList", notTypeList);
            //未使用的优惠券
            map.put("type", CouponModal.COUPON_TYPE_NOT_USED);
            //更新优惠券
            Map<String, Object> m = new HashMap<>(map);
            unUseCoupons(storeId, time, m);
            List<Map<String, Object>> usersCoupons = couponModelMapper.getUsersCoupons(map);
            if (!CollectionUtils.isEmpty(usersCoupons))
            {
                for (Map<String, Object> userCoupon : usersCoupons)
                {
                    // 循环判断 优惠券是否绑定
                    // 优惠券id
                    int tmpCouponId = DataUtils.getIntegerVal(userCoupon, "id");
                    // 优惠券到期时间
                    Date        expiryTime  = (Date) userCoupon.get("expiry_time");
                    CouponModal couponModel = new CouponModal();
                    if (expiryTime != null && expiryTime.before(time))
                    {
                        // 当前时间 >= 优惠券到期时间
                        couponModel.setType(CouponModal.COUPON_TYPE_EXPIRED);
                        couponModel.setId(tmpCouponId);
                    }
                    else
                    {
                        // 优惠券名称
                        String mchCouponId = DataUtils.getStringVal(userCoupon, "id");
                        // 优惠券名称
                        String name = DataUtils.getStringVal(userCoupon, "name");
                        // 优惠券类型
                        String activityType = DataUtils.getStringVal(userCoupon, "activity_type");
                        // ================== 新增逻辑开始 ==================
                        // 如果是免邮券且 is_self_delivery 不为0，则跳过此优惠券
                        if (CouponModal.COUPON_TYPE_MY.equals(activityType) && is_self_delivery != 1)
                        {
                            logger.debug("跳过免邮券 {}，因为 is_self_delivery = {}", name, is_self_delivery);
                            continue; // 直接跳过此优惠券
                        }
                        // ================== 新增逻辑结束 ==================
                        // 面值
                        BigDecimal money = DataUtils.getBigDecimalVal(userCoupon, "money");
                        // 折扣值
                        BigDecimal discount = DataUtils.getBigDecimalVal(userCoupon, "discount");
                        // 满多少
                        BigDecimal zMoney = DataUtils.getBigDecimalVal(userCoupon, "z_money");
                        // 优惠券使用范围
                        int type = DataUtils.getIntegerVal(userCoupon, "type");
                        // 指定分类id
                        String productClassId = DataUtils.getStringVal(userCoupon, "product_class_id");
                        // 指定商品id
                        String productId1 = DataUtils.getStringVal(userCoupon, "product_id");
                        // 用户可以使用优惠券的场景
                        Map<String, Object> tmpUserCanUseCouponSences = new HashMap<>();
                        //店铺优惠券筛选
                        tmpUserCanUseCouponSences = couponScreening(type, userCoupon, productId1, mchProductId, productClassId, mchProductClassMap);

                        // 商品总价
                        BigDecimal mchProductTotal0 = BigDecimal.ZERO;
                        if (tmpUserCanUseCouponSences != null)
                        {
                            //免邮券处理总运费-单店铺
                            BigDecimal mchFrreFreightPrice = BigDecimal.ZERO;
                            for (Map<String, Object> product : productsList)
                            {
                                //当前商品是否满足优惠券条件标识
                                boolean isCoupon = false;
                                String  pid      = DataUtils.getStringVal(product, "pid");
                                //商品分类id
                                String     classIdStr       = MapUtils.getString(product, "product_class");
                                BigDecimal membership_price = DataUtils.getBigDecimalVal(product, "membership_price", BigDecimal.ZERO);
                                BigDecimal num              = DataUtils.getBigDecimalVal(product, "num", BigDecimal.ZERO);
                                String     accord_with      = DataUtils.getStringVal(tmpUserCanUseCouponSences, "accord_with", "");
                                if (tmpUserCanUseCouponSences.containsKey("accord_with_classIdList"))
                                {
                                    //拆分当前分类id
                                    String[] currentClassIds = StringUtils.trim(classIdStr, SplitUtils.HG).split(SplitUtils.HG);
                                    //符合商品分类的优惠券
                                    List<Object> classIds = DataUtils.cast(tmpUserCanUseCouponSences.get("accord_with_classIdList"));
                                    if (classIds != null)
                                    {
                                        String accordWithClassIdStr = StringUtils.stringImplode(classIds, SplitUtils.HG, true);
                                        if (accordWithClassIdStr == null)
                                        {
                                            accordWithClassIdStr = "";
                                        }
                                        for (String id : currentClassIds)
                                        {
                                            if (accordWithClassIdStr.contains(SplitUtils.HG + id + SplitUtils.HG))
                                            {
                                                //满足优惠券条件
                                                isCoupon = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                else if (accord_with.contains(pid))
                                {
                                    isCoupon = true;
                                }
                                if (isCoupon)
                                {
                                    mchProductTotal0 = mchProductTotal0.add(membership_price.multiply(num));
                                    //没有运费的商品不显示免邮券 2023-09-01 gp
                                    if (CouponModal.COUPON_TYPE_MY.equals(activityType) && mchYunfei.compareTo(BigDecimal.ZERO) <= 0)
                                    {
                                        continue;
                                    }
                                    //是否是免邮券,如果是免邮券则运费清零(2023-08-08 gp 在使用了该优惠卷的时候才将详单的运费改为0)
                                    if (CouponModal.COUPON_TYPE_MY.equals(activityType) && couponId != null && !couponId.equals("0") && couponId.equals(mchCouponId))
                                    {
                                        product.put("freight_price", BigDecimal.ZERO);
                                    }
                                }
                            }
                            Map<String, Object> list;
                            if (DataUtils.ZERO_BIGDECIMAL.compareTo(zMoney) == 0)
                            {
                                //禅道bug45344，面值>店铺金额 也可以使用
                                // 当无限制时 面值>店铺金额 则不能使用
//                                if (money.compareTo(mchProductTotal0) >= 0) {
//                                    // 优惠券金额 > 商品总价
//                                    tmpUserCanUseCouponSences = null;
//                                } else {
//                                    tmpUserCanUseCouponSences.put("money", money);
//                                }
                                // 当无限制时 面值>店铺金额 则不能使用
                                if (money.compareTo(mchProductTotal0) >= 0)
                                {
                                    // 优惠券金额 > 商品总价
                                    tmpUserCanUseCouponSences = null;
                                }
                                else
                                {
                                    tmpUserCanUseCouponSences.put("money", money);
                                }
                            }
                            else
                            {
                                //满减限制
                                if (zMoney.compareTo(mchProductTotal0) > 0)
                                {
                                    logger.debug(name + " 满减失败,不满足条件;当前店铺商品合计价格:{},满减阈值金额:{}", mchProductTotal0, zMoney);
                                    continue;
                                }
                            }
                            list = tmpUserCanUseCouponSences;
                            if (list != null)
                            {
                                BigDecimal zongMoney  = mchProductTotal0.multiply(discount).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
                                BigDecimal zongMoney1 = DataUtils.ZERO_BIGDECIMAL;
                                if (zongMoney.compareTo(DataUtils.ZERO_BIGDECIMAL) > 0)
                                {
                                    zongMoney1 = mchProductTotal0.subtract(zongMoney);
                                }
                                if (CouponModal.COUPON_TYPE_ZK.equals(activityType))
                                {
                                    list.put("coupon_name", name + ":优惠" + zongMoney1 + "金额(" + discount + "折)");
                                    list.put("money", zongMoney1);
                                }
                                else
                                {
                                    final String value = DoubleFormatUtil.formattf(zongMoney1.doubleValue());
                                    //4139 【JAVA开发环境】卡券：移动端--优惠券--确认订单页面--按照ui后面不需要再显示（10）
                                    String text = name + ":优惠" + money + "金额";
                                    if (CouponModal.COUPON_TYPE_MJ.equals(activityType))
                                    {
                                        list.put("money", money);
                                        list.put("coupon_name", text);
                                    }
                                    else if (CouponModal.COUPON_TYPE_HYZS.equals(activityType))
                                    {
                                        // 当会员赠券为折扣时
                                        if (DataUtils.equalBigDecimalZero(money) && !DataUtils.equalBigDecimalZero(discount))
                                        {
                                            list.put("coupon_name", name + ":优惠" + zongMoney1 + "金额(" + discount + ")");
                                            list.put("money", value);
                                        }
                                        else
                                        {
                                            list.put("coupon_name", name + ":优惠" + list.get("money") + "金额");
                                        }
                                    }
                                    else
                                    {
                                        //免邮
                                        list.put("coupon_name", name + "(免邮)");
                                        //优惠金额
                                        list.put("money", mchFrreFreightPrice);
                                    }
                                }
                                // 优惠券金额
                                list.put("mch_product_total", mchProductTotal0);
                                //优惠券类型
                                list.put("activityType", activityType);
                                arr1.add(list);
                            }
                        }
                    }
                }
            }

            if (arr1.size() > 0)
            {
                //金额倒序排序 金额大的放第一位
                arr1.sort((o1, o2) ->
                {
                    BigDecimal sort1 = new BigDecimal(MapUtils.getString(o1, "money"));
                    BigDecimal sort2 = new BigDecimal(MapUtils.getString(o2, "money"));
                    return sort2.compareTo(sort1);
                });
                Map temp = new HashMap();
                temp.put("id", 0);
                temp.put("coupon_name", I18nUtils.getRawMessage("coupon.use.none"));
                temp.put("money", null);
                temp.put("accord_with", new ArrayList<>());
                temp.put("mch_product_total", mchProductTotal);
                arr1.add(temp);
                //修改默认优惠券
                int         couponid    = DataUtils.getIntegerVal(arr1.get(0), "id");
                CouponModal couponModal = new CouponModal();
                couponModal.setId(couponid);
                couponModal.setType(CouponModal.COUPON_TYPE_IN_USE);
                couponModelMapper.updateByPrimaryKeySelective(couponModal);

                int                 pos        = 0;
                Map<String, Object> retTempMap = null;
                for (Map<String, Object> list : arr1)
                {
                    retTempMap = new HashMap<>();
                    boolean couponStatus = false;
                    String  id           = DataUtils.getStringVal(list, "id");
                    if (StringUtils.isNotEmpty(couponId) && "0".equals(couponId))
                    {
                        if (couponId.equals(id))
                        {
                            couponStatus = true;
                        }
                    }
                    else
                    {
                        // 当还未选择优惠券
                        if (!canshu)
                        {
                            // 设置默认优惠券
                            if (pos == 0)
                            {
                                couponStatus = true;
                            }
                        }
                        else
                        {
                            // 不使用优惠券
                            if (couponId.equals(id))
                            {
                                couponStatus = true;
                            }
                        }
                    }
                    retTempMap.put("coupon_id", id);
                    BigDecimal money = DataUtils.getBigDecimalVal(list, "money", BigDecimal.ZERO);
                    retTempMap.put("coupon_name", list.get("coupon_name"));
                    retTempMap.put("coupon_status", couponStatus);
                    BigDecimal shopSubTotal = mchProductTotal.subtract(money);
                    retTempMap.put("shop_subtotal", shopSubTotal);
                    if ("0".equals(id))
                    {
                        retTempMap.put("money", 0);
                    }
                    else
                    {
                        retTempMap.put("money", money);
                    }
                    retTempMap.put("accord_with", list.get("accord_with"));
                    retTempMap.put("mch_product_total", list.get("mch_product_total"));
                    //当前优惠券限制金额
                    retTempMap.put("limitMoney", list.get("z_money"));
                    //当前优惠券折扣
                    retTempMap.put("discount", list.get("discount"));
                    //优惠券类型
                    retTempMap.put("activityType", list.get("activityType"));
                    pos = pos + 1;
                    arr.add(retTempMap);
                }
            }

            for (Map<String, Object> tmp : arr)
            {
                boolean couponStatus = (boolean) tmp.get("coupon_status");
                if (couponStatus)
                {
                    List<String> accordWiths = DataUtils.cast(tmp.get("accord_with"));
                    if (accordWiths.size() > 0)
                    {
                        // 当前店铺商品总价
                        BigDecimal mch_product_total = new BigDecimal(MapUtils.getString(tmp, "mch_product_total"));
                        for (String goodsId : accordWiths)
                        {
                            //优惠限制
                            BigDecimal limitMoney = new BigDecimal(MapUtils.getString(tmp, "limitMoney"));
                            //优惠金额
                            BigDecimal money = new BigDecimal(MapUtils.getString(tmp, "money"));
                            //计算优惠后的商品金额
                            this.calculationCoupon(productsList, Integer.parseInt(goodsId), mch_product_total, couponId, MapUtils.getInteger(map, "activityType"), money, limitMoney);
                        }
                    }
                }
            }


            retMap.put("products_list", productsList);
            retMap.put("list", arr);
            return retMap;
        }
        catch (LaiKeAPIException e)
        {
            logger.error("店铺优惠券获取 自定义异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPYHQHQSB, "店铺优惠券获取失败", "getStoreCoupons");
        }
        catch (Exception e)
        {
            logger.error("店铺优惠券获取 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPYHQHQSB, "店铺优惠券获取失败", "getStoreCoupons");
        }
    }

    /**
     * 计算商品优惠后的价格
     *
     * @param productList       - 商品集合
     * @param goodsId           - 满足店铺优惠券的商品id
     * @param mchPriceTotal     - 当前店铺商品总价
     * @param couponId          - 优惠券id
     * @param activityType      - 优惠券类型
     * @param couponMoney       - 优惠面额
     * @param couponLimitCoupon - 优惠券门槛金额
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/15 17:09
     */
    private void calculationCoupon(List<Map<String, Object>> productList, Integer goodsId, BigDecimal mchPriceTotal, String couponId, Integer activityType, BigDecimal couponMoney, BigDecimal couponLimitCoupon) throws LaiKeAPIException
    {
        try
        {
            //每个规格优惠均摊的金额
            Map<Integer, BigDecimal> attrAvgMap = new HashMap<>(16);
            //重新计算店铺总价(用来计算满足商品的优惠券平摊金额)
            BigDecimal mchPriceTotalTemp = BigDecimal.ZERO;
            for (Map<String, Object> productInfo : productList)
            {
                //当前商品价格  不知道为什么用商品单价来均分优惠金额
//                BigDecimal membership_price = new BigDecimal(MapUtils.getString(productInfo, "membership_price"));
                //改为使用商品总价计算均分优惠金额  甘鹏禅道： 54313
                //当前商品价格
                BigDecimal membership_price = new BigDecimal(MapUtils.getString(productInfo, "membership_price"));
                //商品数量
                int num = Integer.parseInt(MapUtils.getString(productInfo, "num"));
                membership_price = membership_price.multiply(new BigDecimal(num));
                if (goodsId.equals(MapUtils.getInteger(productInfo, "pid")))
                {
                    //当前商品是否满足优惠券条件,如果不满足则看有没有满足的,如果都不满足则一起使用优惠券。(该优惠券退款只能等到使用优惠券的商品都退款的情况才能退)
                    if (membership_price.compareTo(couponLimitCoupon) > 0)
                    {
                        //当前商品可以使用该优惠券
                        mchPriceTotalTemp = mchPriceTotalTemp.add(membership_price);
                    }
                }
                //规格价格特殊处理
                attrAvgMap.put(MapUtils.getInteger(productInfo, "cid"), membership_price);
            }
            Map<Integer, BigDecimal> youHuiMap = com.laiketui.common.utils.algorithm.DataAlgorithmTool.orderPriceAverage(mchPriceTotal, attrAvgMap, couponMoney);
            for (Map<String, Object> productInfo : productList)
            {
                if (goodsId.equals(MapUtils.getInteger(productInfo, "pid")))
                {
                    //当前商品价格
                    BigDecimal membership_price = new BigDecimal(MapUtils.getString(productInfo, "membership_price"));
                    //商品优惠后的金额
                    BigDecimal singleCommodityYouhuiMoney = BigDecimal.ZERO;
                    //商品数量
                    int num = Integer.parseInt(MapUtils.getString(productInfo, "num"));
                    //单商品总价
                    BigDecimal singleCommodity = membership_price.multiply(new BigDecimal(num));
                    //当前商品可以使用该优惠券
                    mchPriceTotalTemp = mchPriceTotalTemp.add(membership_price);
                    //单个商品的总优惠金额
                    singleCommodityYouhuiMoney = singleCommodity.subtract(youHuiMap.get(MapUtils.getInteger(productInfo, "cid")));

                    productInfo.put("discount", singleCommodityYouhuiMoney.setScale(2, BigDecimal.ROUND_HALF_UP));
                    productInfo.put("coupon_id", couponId);
                    //当前商品店铺券优惠金额
                    productInfo.put("shop_discount", youHuiMap.get(MapUtils.getInteger(productInfo, "cid")));
                    //优惠后金额
                    productInfo.put("amount_after_discount", singleCommodityYouhuiMoney);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("计算商品优惠后的价格 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPYHQHQSB, "计算商品优惠后的价格错误", "calculationCoupon");
        }
    }

    /**
     * couponScreening
     * 店铺优惠券筛选
     *
     * @param type
     * @param userCoupon
     * @param productId1
     * @param mchProductId
     * @param productClassId
     * @param mchProductClassMap - 店铺商品信息 [key=商品id,val=分类id “-?-” ]
     */
    private Map<String, Object> couponScreening(int type, Map<String, Object> userCoupon, String productId1,
                                                List<String> mchProductId, String productClassId, Map<Integer, String> mchProductClassMap) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            // 全部商品 符合
            if (CouponActivityModel.USE_RANG_TYPE_ALL == type)
            {
                userCoupon.put("accord_with", mchProductId);
                resultMap = userCoupon;
            }
            else if (CouponActivityModel.USE_RANG_TYPE_PRODUCT == type)
            {
                //指定商品
                resultMap = productAccord(userCoupon, productId1, mchProductId);
                int productStatus = (int) resultMap.get("product_status");
                if (productStatus != 0)
                {
                    resultMap = DataUtils.cast(resultMap.get("list"));
                }
            }
            else if (CouponActivityModel.USE_RANG_TYPE_CLASS == type)
            {
                //指定分类
                resultMap = this.classAccord(userCoupon, productClassId, mchProductClassMap);
                int classStatus = MapUtils.getIntValue(resultMap, "class_status");
                if (classStatus != 0)
                {
                    resultMap = DataUtils.cast(resultMap.get("list"));
                }
            }
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            e.printStackTrace();
            throw e;
        }
        catch (Exception e)
        {
            logger.error("优惠券筛选 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQSXSB, "优惠券筛选失败", "couponScreening");
        }
    }

    /**
     * 获取指定分类
     *
     * @param userCoupon           - 当前使用的优惠券信息
     * @param productClassId       - 当前商品所属分类id(序列化前的)
     * @param mchProductClassesMap - 店铺商品信息 [key=商品id,val=分类id “-?-” ]
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/12/28 14:31
     */
    private Map<String, Object> classAccord(Map<String, Object> userCoupon, String productClassId, Map<Integer, String> mchProductClassesMap) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            // 商品状态 0:代表购买商品 不符合 优惠券指定 分类
            int classStatus = 0;
            // 存储符合的商品ID
            List<String> accordWithGoodsId = new ArrayList<>();
            // 存储符合的商品类别ID
            List<String> accordWithClassIds = new ArrayList<>();
            productClassId = getUnserText(productClassId);
            for (Integer mchProductId : mchProductClassesMap.keySet())
            {
                String mchProductClass = StringUtils.trim(mchProductClassesMap.get(mchProductId), SplitUtils.HG).split(SplitUtils.HG)[0];
                if (productClassId.contains(mchProductClass + SplitUtils.DH))
                {
                    // 符合
                    accordWithClassIds.add(mchProductClass);
                    accordWithGoodsId.add(mchProductId + "");
                    // 商品状态 1:代表购买商品 符合 优惠券指定 商品
                    classStatus = 1;
                }
                else
                {
                    //有一个商品不符合，当前订单不可使用优惠卷
                    classStatus = 0;
                    break;
                }
            }
            userCoupon.put("accord_with_classIdList", accordWithClassIds);
            userCoupon.put("accord_with", accordWithGoodsId);
            resultMap.put("class_status", classStatus);
            resultMap.put("list", userCoupon);
            return resultMap;
        }
        catch (Exception e)
        {
            logger.error("优惠券筛选异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQSXSBZDFL, "优惠券筛选失败:指定分类", "couponScreening");
        }
    }

    /**
     * 获取反序列化的数据 12，12，212，
     *
     * @param content
     * @return
     */
    public String getUnserText(String content)
    {
        PHPSerializer p      = new PHPSerializer();
        Object        array1 = null;
        try
        {
            array1 = p.unserialize(content.getBytes());
            return new String((byte[]) array1) + SplitUtils.DH;
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 获取符合使用优惠券条件的指定商品信息
     *
     * @param userCoupon
     * @param productId1
     * @param mchProductIds
     * @return
     */
    private Map<String, Object> productAccord(Map<String, Object> userCoupon, String productId1, List<String> mchProductIds)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            // (优惠券指定商品ID,购买商品ID)
            // 商品状态 0:代表购买商品 不符合 优惠券指定 商品
            int productStatus = 0;
            // 存储符合的商品ID
            List<String> appointProductIds = new ArrayList<>();
            productId1 = getUnserText(productId1);
            for (String mchProductId : mchProductIds)
            {
                if (productId1.contains(mchProductId + SplitUtils.DH))
                {
                    // 符合
                    appointProductIds.add(mchProductId);
                    // 商品状态 1:代表购买商品 符合 优惠券指定 商品
                    productStatus = 1;
                }
                else
                {
                    //有一个商品不符合，当前订单不可使用优惠卷
                    productStatus = 0;
                    break;
                }
            }
            resultMap.put("product_status", productStatus);
            //符合的id集
            userCoupon.put("accord_with", appointProductIds);
            resultMap.put("list", userCoupon);
            return resultMap;
        }
        catch (Exception e)
        {
            logger.error("优惠券筛选失败:指定商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQSXSBZDSP, "优惠券筛选失败:指定商品", "productAccord");
        }
    }

    @Override
    public Map<String, Object> settlementPlaformCoupons(int storeId, String userId, List<Map<String, Object>> products, String couponIds, boolean canshu) throws LaiKeAPIException
    {
        Map<String, Object> retMap = new HashMap<>();
        try
        {
            // 当前时间
            Date                      time = new Date();
            List<Map<String, Object>> arr  = new ArrayList<>();
            List<Map<String, Object>> arr1 = new ArrayList<>();

            BigDecimal discount = BigDecimal.ONE;
            //商品总价(会员折扣后)
            BigDecimal shopSubtotal = BigDecimal.ZERO;
            //商品总价(折扣后、去除运费)
            BigDecimal shopGoodsTal = BigDecimal.ZERO;
            //总运费
            BigDecimal yunFeiTotal = BigDecimal.ZERO;
            //平台优惠券id
            String platfromCouponId = "0";
            // 购买商品的分类信息
            List<String> productClasses = new ArrayList<>();
            //每个规格优惠均摊的金额
            Map<Integer, BigDecimal> attrAvgMap = new HashMap<>(16);
            // 购买商品的商品ID
            List<String> productIdes = new ArrayList<>();
            if (StringUtils.isNotEmpty(couponIds))
            {
                String[] couponids = couponIds.split(SplitUtils.DH);
                platfromCouponId = couponids[couponids.length - 1];
            }

            //计算当前店铺商品价格信息
            for (Map<String, Object> mchProductInfo : products)
            {
                // 商品总价(使用店铺优惠券后的总价)
                BigDecimal mchProductTotal = DataUtils.getBigDecimalVal(mchProductInfo, "shop_subtotal", BigDecimal.ZERO);
                //运费
                BigDecimal yunFei = new BigDecimal(MapUtils.getString(mchProductInfo, "freight_price"));
                yunFeiTotal = yunFeiTotal.add(yunFei);
                shopSubtotal = shopSubtotal.add(mchProductTotal);
                //去除运费的纯价格
                shopGoodsTal = shopGoodsTal.add(mchProductTotal.subtract(yunFei));

                List<Map<String, Object>> oneMchProducts = (List<Map<String, Object>>) mchProductInfo.get("list");
                for (Map<String, Object> product : oneMchProducts)
                {
                    productClasses.add((String) product.get("product_class"));
                    productIdes.add(String.valueOf(product.get("pid")));
                    //规格价格特殊处理 请使用 amount_after_discount,amount_after_discount为店铺优惠后的金额(店铺券+平台券的场景)
                    attrAvgMap.put(MapUtils.getInteger(product, "cid"), new BigDecimal(MapUtils.getString(product, "amount_after_discount")));
                }
            }

            Map<String, Object> map = new HashMap<>(16);
            map.put("store_id", storeId);
            map.put("user_id", userId);
            //平台店铺id为0
            map.put("mch_id", 0);
            //商品不能使用的优惠券
            List<Integer> notTypeList = new ArrayList<>();
            notTypeList.add(CouponActivityModel.USE_RANG_TYPE_VIP);
            map.put("notTypeList", notTypeList);
            //  type 类型 0:未使用 1:使用中 2:已使用 3:已过期
            map.put("type", CouponModal.COUPON_TYPE_NOT_USED);
            //修改使用中的优惠券状态
            Map<String, Object> m = new HashMap<>(map);
            unUseCoupons(storeId, time, m);
            List<Map<String, Object>> usersCoupons = couponModelMapper.getUsersCoupons(map);
            if (!CollectionUtils.isEmpty(usersCoupons))
            {
                //获取可以使用的优惠券
                for (Map<String, Object> userCoupon : usersCoupons)
                {
                    // 优惠券id
                    int tmpCouponId = MapUtils.getIntValue(userCoupon, "id");
                    // 优惠券到期时间
                    Date        expiryTime  = (Date) userCoupon.get("expiry_time");
                    CouponModal couponModel = new CouponModal();
                    if (expiryTime != null && expiryTime.before(time))
                    {
                        // 当前时间 >= 优惠券到期时间
                        //  type 类型 0:未使用 1:使用中 2:已使用 3:已过期
                        couponModel.setType(CouponModal.COUPON_TYPE_EXPIRED);
                        couponModel.setId(tmpCouponId);
                    }
                    else
                    {
                        // 优惠券名称
                        String name = DataUtils.getStringVal(userCoupon, "name", "");
                        // 优惠券类型
                        String activityType = DataUtils.getStringVal(userCoupon, "activity_type", "");
                        // 面值
                        BigDecimal money = DataUtils.getBigDecimalVal(userCoupon, "money", BigDecimal.ZERO);
                        // 折扣值
                        discount = DataUtils.getBigDecimalVal(userCoupon, "discount");
                        // 满多少
                        BigDecimal zMoney = DataUtils.getBigDecimalVal(userCoupon, "z_money", BigDecimal.ZERO);
                        // 优惠券使用范围
                        int type = DataUtils.getIntegerVal(userCoupon, "type");
                        // 指定分类id
                        String productClassId = SerializePhpUtils.getUnserializeByBasic(MapUtils.getString(userCoupon, "product_class_id"), String.class);

                        //优惠翻译
                        String discounts_msg =  I18nUtils.getRawMessage("app.order.discounts");
                        //金额翻译
                        String price_msg = I18nUtils.getRawMessage("app.order.price");
                        if (StringUtils.isNotEmpty(productClassId))
                        {
                            productClassId = StringUtils.stringImplode(Arrays.asList(productClassId.split(SplitUtils.DH)), SplitUtils.DH, true);
                        }
                        // 指定商品id
                        String productId1 = (String) userCoupon.get("product_id");
                        // 用户可以使用优惠券的场景
                        Map<String, Object> tmpUserCanUseCouponSences = new HashMap<>();
                        //没有运费的商品不显示免邮券
                        if (CouponModal.COUPON_TYPE_MY.equals(activityType) && yunFeiTotal.compareTo(BigDecimal.ZERO) > 0)
                        {
                            userCoupon.put("money", money);
                            userCoupon.put("coupon_name", name);
                            tmpUserCanUseCouponSences = screenCouponList(CouponModal.COUPON_TYPE_MY, type, shopGoodsTal, zMoney, BigDecimal.ZERO, userCoupon, productId1, productIdes, productClasses, productClassId);
                        }

                        else if (CouponModal.COUPON_TYPE_MJ.equals(activityType))
                        {
                            //满减限制
                            userCoupon.put("coupon_name",  discounts_msg + money + price_msg);
                            userCoupon.put("money", money);
                            tmpUserCanUseCouponSences = screenCouponList(CouponModal.COUPON_TYPE_MJ, type, shopGoodsTal, zMoney, money, userCoupon, productId1, productIdes, productClasses, productClassId);

                        }
                        else if (CouponModal.COUPON_TYPE_ZK.equals(activityType))
                        {
                            BigDecimal zongMoney  = shopGoodsTal.multiply(discount).multiply(new BigDecimal("100")).divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal zongMoney1 = BigDecimal.ZERO;
                            if (zongMoney.compareTo(BigDecimal.ZERO) > 0)
                            {
                                zongMoney1 = shopGoodsTal.subtract(zongMoney);
                            }
                            userCoupon.put("coupon_name", discounts_msg + " " + zongMoney1 + I18nUtils.getRawMessage("app.order.priceDiscounts", new Object[]{discount}));
                            userCoupon.put("money", zongMoney1);
                            tmpUserCanUseCouponSences = screenCouponList(CouponModal.COUPON_TYPE_ZK, type, zongMoney1, zMoney, shopGoodsTal, userCoupon, productId1, productIdes, productClasses, productClassId);
                        }
                        else
                        {
                            //会员赠送券
                            if (CouponModal.COUPON_TYPE_HYZS.equals(activityType))
                            {
                                if (BigDecimal.ZERO.compareTo(money) == 0 && BigDecimal.ZERO.compareTo(discount) != 0)
                                {
                                    BigDecimal zongMoney  = shopSubtotal.multiply(discount).multiply(new BigDecimal("100")).divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
                                    BigDecimal zongMoney1 = BigDecimal.ZERO;
                                    if (zongMoney.compareTo(BigDecimal.ZERO) > 0)
                                    {
                                        zongMoney1 = shopSubtotal.subtract(zongMoney);
                                    }
                                    // 当会员赠券为折扣时
                                    userCoupon.put("money", zongMoney1);
                                    userCoupon.put("coupon_name", discounts_msg + " " + zongMoney1 + I18nUtils.getRawMessage("app.order.priceDiscounts", new Object[]{discount}));
                                    tmpUserCanUseCouponSences = screenCouponList(CouponModal.COUPON_TYPE_ZK, type, zongMoney1, zMoney, shopGoodsTal, userCoupon, productId1, productIdes, productClasses, productClassId);
                                }
                                else
                                {
                                    userCoupon.put("money", money);
                                    userCoupon.put("coupon_name",discounts_msg + " " + money + price_msg);
                                    tmpUserCanUseCouponSences = screenCouponList(CouponModal.COUPON_TYPE_MJ, type, shopGoodsTal, zMoney, money, userCoupon, productId1, productIdes, productClasses, productClassId);
                                }
                            }
                        }
                        if (tmpUserCanUseCouponSences == null || tmpUserCanUseCouponSences.isEmpty())
                        {
                            //该卷不满足条件
                            continue;
                        }
                        arr1.add(tmpUserCanUseCouponSences);
                    }
                }
            }
            //未使用优惠劵-自动选择最优优惠劵
            if (platfromCouponId.equals("0") && !canshu)
            {
                //有可用优惠劵--优惠金额最大的最靠前
                if (arr1.size() >= 1)
                {
                    platfromCouponId = arr1.get(0).get("id").toString();
                    //重新拼接优惠劵字符串
                    couponIds = platfromCouponId + SplitUtils.DH + "coupon";
                    retMap.put("couponIds", couponIds);
                }
            }
            BigDecimal couponDiscount = BigDecimal.ZERO;
            if (!CollectionUtils.isEmpty(arr1))
            {
                arr1.sort((o1, o2) ->
                {
                    BigDecimal sort1 = (BigDecimal) o1.get("money");
                    BigDecimal sort2 = (BigDecimal) o2.get("money");
                    return sort2.compareTo(sort1);
                });
                Map<String, Object> retTmp;
                //循环处理当前使用的优惠券
                for (Map<String, Object> couponInfos : arr1)
                {
                    retTmp = new HashMap<>();
                    //是否使用优惠券标识
                    boolean    couponStatus = false;
                    BigDecimal money        = DataUtils.getBigDecimalVal(couponInfos, "money", BigDecimal.ZERO);
                    if (StringUtils.isNotEmpty(platfromCouponId) && !"0".equals(platfromCouponId))
                    {
                        if (platfromCouponId.equals(String.valueOf(couponInfos.get("id"))))
                        {
                            //如果是使用,则标记该优惠券使用中
                            couponStatus = true;
                            CouponModal couponModal = new CouponModal();
                            couponModal.setId(Integer.parseInt(platfromCouponId));
                            couponModal.setType(CouponModal.COUPON_TYPE_IN_USE);
                            couponModelMapper.updateByPrimaryKeySelective(couponModal);
                        }
                    }

                    retTmp.put("coupon_id", couponInfos.get("id"));
                    retTmp.put("discount_type", "coupon");
                    retTmp.put("activity_type", couponInfos.get("activity_type"));
                    retTmp.put("money", couponInfos.get("money"));
                    retTmp.put("coupon_name", couponInfos.get("coupon_name"));
                    retTmp.put("coupon_status", couponStatus);
                    retTmp.put("shop_subtotal", DoubleFormatUtil.format(shopSubtotal.subtract(money).doubleValue()));
                    if (couponStatus)
                    {
                        //平台总共优惠金额
                        couponDiscount = couponDiscount.add(money);
                    }
                    arr.add(retTmp);
                }
            }

            //计算每个商品优惠后的价格 【场景:使用了店铺券+平台券 需要特殊处理】
            Map<Integer, BigDecimal> youHuiMap = com.laiketui.common.utils.algorithm.DataAlgorithmTool.orderPriceAverage(shopGoodsTal, attrAvgMap, couponDiscount);
            for (Map<String, Object> mchProducts : products)
            {
                List<Map<String, Object>> onlyProducts = DataUtils.cast(mchProducts.get("list"));
                //总运费
                BigDecimal zYunFei = new BigDecimal(MapUtils.getString(mchProducts, "freight_price", "0"));
                //店铺商品支付总价(*数量)
                BigDecimal orderShopPrice = new BigDecimal(MapUtils.getString(mchProducts, "shop_subtotal", "0")).subtract(zYunFei);
                //循环当前订单店铺下所有商品
                for (Map<String, Object> product : onlyProducts)
                {
                    if (!CollectionUtils.isEmpty(arr))
                    {
                        //当前商品运费
                        BigDecimal freightPrice = new BigDecimal(MapUtils.getString(product, "freight_price", "0"));
                        //当前商品实际支付价格
                        BigDecimal goodsPayPrice = new BigDecimal(MapUtils.getString(product, "amount_after_discount", "0"));
                        //优惠后的金额
                        BigDecimal youHuiPrice = BigDecimal.ZERO;
                        //计算平台优惠后的商品价格 如果有多个商品 则每个商品平摊优惠金额
                        youHuiPrice = goodsPayPrice.subtract(youHuiMap.get(MapUtils.getInteger(product, "cid"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                        if (youHuiPrice.compareTo(BigDecimal.ZERO) < 0)
                        {
                            youHuiPrice = BigDecimal.ZERO;
                        }
                        //当前商品优惠后金额(商品最终成交金额 不算运费)
                        product.put("amount_after_discount", youHuiPrice);
                        //当前商品平台券优惠金额
                        product.put("platform_discount", youHuiMap.get(MapUtils.getInteger(product, "cid")));
                    }
                    String couponId = MapUtils.getString(product, "coupon_id");
                    product.put("coupon_id", couponId + SplitUtils.DH + platfromCouponId);
                }
            }

            retMap.put("products", products);
            retMap.put("list", arr);
            return retMap;
        }
        catch (Exception e)
        {
            logger.error("平台优惠券筛选 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTYHQSXSB, "平台优惠券筛选失败", "settlementPlaformCoupons");
        }
    }

    @Override
    public Map<String, Object> mycoupon(int storeId, String userId, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //去使用
            List<Map<String, Object>> toUseList = new ArrayList<>();
            //已使用
            List<Map<String, Object>> useList = new ArrayList<>();
            //已过期
            List<Map<String, Object>> expiredUseList = new ArrayList<>();

            //获取用户优惠卷列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("user_id", userId);
            if (type != null)
            {
                List<Integer> typeList = new ArrayList<>();
                if (type == 1)
                {
                    //已使用
                    typeList.add(CouponModal.COUPON_TYPE_USED);
                }
                else if (type == 2)
                {
                    //已过期
                    typeList.add(CouponModal.COUPON_TYPE_EXPIRED);
                }
                else
                {
                    //未使用
                    typeList.add(CouponModal.COUPON_TYPE_NOT_USED);
                    typeList.add(CouponModal.COUPON_TYPE_IN_USE);
                }
                parmaMap.put("typeList", typeList);
            }
            parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            int                       total           = couponModelMapper.countCouponInfoListDynamic(parmaMap);
            List<Map<String, Object>> couponModalList = couponModelMapper.getCouponInfoListDynamic(parmaMap);
            for (Map<String, Object> map : couponModalList)
            {
                //优惠卷id
                int couponId = Integer.parseInt(map.get("id").toString());
                //活动id
                int hid = StringUtils.stringParseInt(map.get("hid") + "");
                //优惠卷到期时间
                Date expiryDate = (Date) map.get("expiry_time");
                //优惠卷名称
                String couponName = "";
                //优惠卷使用状态
                int couponType = Integer.parseInt(map.get("type").toString());

                if (expiryDate != null)
                {
                    map.put("add_time", DateUtil.dateFormate(map.get("add_time").toString(), GloabConst.TimePattern.YMD));
                    map.put("expiry_time", DateUtil.dateFormate(expiryDate, GloabConst.TimePattern.YMD));
                }
                //查询活动信息
                CouponActivityModel couponActivityModel = new CouponActivityModel();
                couponActivityModel.setStore_id(storeId);
                couponActivityModel.setId(hid);
                couponActivityModel = couponActivityModelMapper.selectOne(couponActivityModel);
                if (couponActivityModel != null)
                {
                    couponName = couponActivityModel.getName();
                    //优惠卷所属店铺id
                    int mchId = couponActivityModel.getMch_id();
                    //获取店铺信息
                    if (mchId != 0)
                    {
                        MchModel mchModel = new MchModel();
                        mchModel.setStore_id(storeId);
                        mchModel.setId(mchId);
                        mchModel = mchModelMapper.selectOne(mchModel);
                        if (mchModel != null)
                        {
                            couponName = String.format("[%s]%s", mchModel.getName(), couponName);
                        }
                    }
                    //消费限制处理
                    String limitStr = I18nUtils.getRawMessage("coupon.limit.unlimited");
                    ;
                    if (couponActivityModel.getZ_money().floatValue() > 0)
                    {
                        limitStr = I18nUtils.getRawMessage("coupon.limit.over_amount", new Object[]{couponActivityModel.getZ_money().setScale(2, BigDecimal.ROUND_HALF_UP)});
                    }

                    if (couponActivityModel.getDay() > 0)
                    {
                        map.put("day", couponActivityModel.getDay());
                        map.put("expiry_time", I18nUtils.getRawMessage("coupon.expire.in.days", new Object[]{couponActivityModel.getDay()}));
                    }
                    //优惠劵过期时间
                    String expiry_time = null;
                    if (map.get("expiry_time") != null)
                    {
                        expiry_time = map.get("expiry_time").toString();
                    }
                    //失效天数
                    Integer day            = MapUtils.getInteger(map, "day");
                    String  expirationDate = "领取后%s天失效";
                    //过期时间  1不过期  2设置指定过期时间  3设置领取后多久失效
                    if (day != null && day != 0)
                    {
                        expirationDate = I18nUtils.getRawMessage("coupon.expire.in.days", new Object[]{day});
                    }
                    else if (StringUtils.isNotEmpty(expiry_time))
                    {
                        expirationDate = expiry_time + " " + I18nUtils.getRawMessage("coupon.point.endtime");
                    }
                    else
                    {
                        expirationDate = I18nUtils.getRawMessage("coupon.expiration.permanent");
                    }
                    map.put("expirationDate", expirationDate);
                    map.put("name", couponName);
                    map.put("limit", limitStr);
                    map.put("money", couponActivityModel.getMoney());
                    map.put("discount", couponActivityModel.getDiscount());
                    map.put("activity_type", couponActivityModel.getActivity_type());
                    map.put("Instructions", couponActivityModel.getInstructions());
                }
                //过期处理
                if (expiryDate != null && couponType != CouponModal.COUPON_TYPE_USED && DateUtil.dateCompare(new Date(), expiryDate))
                {
                    //修改当前优惠卷状态
                    int count = couponModelMapper.updateCoupon(CouponModal.COUPON_TYPE_EXPIRED, storeId, userId, couponId);
                    if (count < 1)
                    {
                        logger.info(String.format("优惠卷修改过期失败 优惠卷id:%s", couponId));
                    }
                }
                //优惠卷状态处理
                String pointStr = null;
                if (CouponModal.COUPON_TYPE_NOT_USED == couponType)
                {
                    pointStr = I18nUtils.getRawMessage("coupon.point.use_now");
                    toUseList.add(map);
                }
                else if (CouponModal.COUPON_TYPE_IN_USE == couponType)
                {
                    //查询是否有订单在使用该优惠卷
                    parmaMap.clear();
                    parmaMap.put("store_id", storeId);
                    parmaMap.put("user_id", userId);
                    parmaMap.put("coupon_id", couponId);
                    //订单未关闭
                    parmaMap.put("statusType", 1);
                    int count = orderModelMapper.countOrdersNumDynamic(parmaMap);
                    if (count > 0)
                    {
                        pointStr = I18nUtils.getRawMessage("coupon.point.getanduse");
                        useList.add(map);
                    }
                    else
                    {
                        pointStr = I18nUtils.getRawMessage("coupon.point.use_now");
                        toUseList.add(map);
                    }
                }
                else if (CouponModal.COUPON_TYPE_USED == couponType)
                {
                    pointStr = I18nUtils.getRawMessage("coupon.point.getanduse");
                    useList.add(map);
                }
                else if (CouponModal.COUPON_TYPE_EXPIRED == couponType)
                {
                    pointStr = I18nUtils.getRawMessage("coupon.point.overtime");
                    expiredUseList.add(map);
                }
                map.put("point", pointStr);
            }

            //查询出用户未使用的赠送商品
            parmaMap.clear();
            parmaMap.put("store_id", storeId);
            parmaMap.put("user_id", userId);
            parmaMap.put("attrIdNotNull", "notNull");
            List<Map<String, Object>> userFreeGoodsList = userFirstModalMapper.getUserFirstInfoDynamic(parmaMap);
            for (Map<String, Object> map : userFreeGoodsList)
            {
                //商品规格id
                int attrId = Integer.parseInt(map.get("attr_id").toString());
                //是否使用了首次开通赠送商品券 0-未使用 1-已使用
                int isUse = Integer.parseInt(map.get("flag").toString());
                //兑换卷失效日期
                String endTime = DateUtil.dateFormate(map.get("end_time").toString(), GloabConst.TimePattern.YMD);

                //默认有效天数
                int validDay = 7;
                //获取配置有效天数
                UserRuleModel userRuleModel = new UserRuleModel();
                userRuleModel.setStore_id(storeId);
                userRuleModel = userRuleModelMapper.selectOne(userRuleModel);
                if (userRuleModel != null)
                {
                    validDay = userRuleModel.getValid();
                }
                map.put("valid", validDay);

                //获取商品库存信息
                parmaMap.clear();
                parmaMap.put("store_id", storeId);
                parmaMap.put("active", DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE);
                parmaMap.put("goodsStatus", DictionaryConst.GoodsStatus.NEW_GROUNDING);
                //该商品处于上架且库存足够才可使用
                parmaMap.put("stockNum_gt", 0);
                parmaMap.put("attr_id", attrId);
                List<Map<String, Object>> configureList = confiGureModelMapper.getProductListLeftJoinMchDynamic(parmaMap);
                for (Map<String, Object> configureMap : configureList)
                {
                    //库存id
                    int goodsId = Integer.parseInt(configureMap.get("goodsId").toString());
                    //规格id
                    int attrbuiteId = Integer.parseInt(configureMap.get("attr_id").toString());
                    //商品图片处理
                    String imgUrl = configureMap.get("imgurl").toString();
                    imgUrl = publiceService.getImgPath(imgUrl, storeId);
                    map.put("imgUrl", imgUrl);
                    map.put("product_title", configureMap.get("product_title").toString());
                    //json_encode(array(array('pid' => $res_1[0]->id), array('cid' => $res_1[0]->attr_id), array('num' => 1)));
                    List<Map<String, Object>> fatherList = new ArrayList<>();
                    Map<String, Object>       goodsMap   = new HashMap<>(16);
                    goodsMap.put("pid", goodsId);
                    Map<String, Object> attrMap = new HashMap<>(16);
                    attrMap.put("cid", attrbuiteId);
                    Map<String, Object> stockMap = new HashMap<>(16);
                    stockMap.put("num", 1);
                    fatherList.add(goodsMap);
                    fatherList.add(attrMap);
                    fatherList.add(stockMap);
                    map.put("order_list", JSON.toJSONString(fatherList));
                    map.put("activity_type", 5);
                    map.put("Instructions", I18nUtils.getRawMessage("coupon.use.rule", new Object[]{validDay}));
                    map.put("expiry_time", endTime);

                    if (isUse == 1)
                    {
                        map.put("point", I18nUtils.getRawMessage("coupon.point.getanduse"));
                        map.put("type", 2);
                        useList.add(map);
                    }
                    else
                    {
                        //未使用
                        if (!DateUtil.dateCompare(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS), endTime))
                        {
                            map.put("point", I18nUtils.getRawMessage("coupon.point.use_now"));
                            map.put("type", 0);
                            toUseList.add(map);
                        }
                        else
                        {
                            map.put("point", I18nUtils.getRawMessage("coupon.point.overtime"));
                            map.put("type", 3);
                            expiredUseList.add(map);
                        }
                    }
                }
            }

            List<Map<String, Object>> dataList = new ArrayList<>();
            if (type.equals(0))
            {
                dataList = toUseList;
            }
            else if (type.equals(1))
            {
                //已使用
                dataList = useList;
            }
            else if (type.equals(2))
            {
                //已过期
                dataList = expiredUseList;
            }

            resultMap.put("list", dataList);
            resultMap.put("total", total);

            List<Integer> typeList = new ArrayList<>();
            parmaMap.clear();
            parmaMap.put("store_id", storeId);
            parmaMap.put("user_id", userId);
            //未使用
            typeList.add(CouponModal.COUPON_TYPE_NOT_USED);
            typeList.add(CouponModal.COUPON_TYPE_IN_USE);
            parmaMap.put("typeList", typeList);
            resultMap.put("wsy_num", couponModelMapper.countUserCouponActivityDynamic(parmaMap));
            //已使用
            typeList.clear();
            typeList.add(CouponModal.COUPON_TYPE_USED);
            parmaMap.put("typeList", typeList);
            resultMap.put("ysy_num", couponModelMapper.countUserCouponActivityDynamic(parmaMap));
            //已过期
            typeList.clear();
            typeList.add(CouponModal.COUPON_TYPE_EXPIRED);
            parmaMap.put("typeList", typeList);
            resultMap.put("ygq_num", couponModelMapper.countUserCouponActivityDynamic(parmaMap));
        }
        catch (Exception e)
        {
            logger.error("获取用户优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mycoupon");
        }
        return resultMap;
    }


    /**
     * 【平台】优惠券 条件删选
     *
     * @param activeType
     * @param type
     * @param shopSubtotal
     * @param zMoney
     * @param money
     * @param userCoupon
     * @param productId1
     * @param productIdes
     * @param productClasses
     * @param productClassId
     * @return
     */
    private Map<String, Object> screenCouponList(String activeType, int type, BigDecimal shopSubtotal, BigDecimal zMoney, BigDecimal money, Map<String, Object> userCoupon, String productId1, List<String> productIdes, List<String> productClasses, String productClassId)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            //该优惠券是否可使用
            boolean isUse = true;
            // 全部商品
            if (CouponActivityModel.USE_RANG_TYPE_PRODUCT == type)
            {
                // (优惠券指定商品ID,购买商品ID)
                String productId1s = getUnserText(productId1);
                //  平台的优惠券必须要满足所有的条件才能使用
                for (String pid : productIdes)
                {
                    if (!productId1s.contains(pid + SplitUtils.DH))
                    {
                        isUse = false;
                        break;
                    }
                }
            }
            else if (CouponActivityModel.USE_RANG_TYPE_CLASS == type)
            {
                // 商品属于优惠券指定的分类
                for (String productClass : productClasses)
                {
                    String[] classList = StringUtils.trim(productClass, SplitUtils.HG).split(SplitUtils.HG);
                    for (String classId : classList)
                    {
                        if (!productClassId.contains(SplitUtils.DH + classId + SplitUtils.DH))
                        {
                            isUse = false;
                            break;
                        }
                    }
                }
            }
            if (isUse)
            {
                if (CouponModal.COUPON_TYPE_ZK.equals(activeType))
                {
                    if (money.compareTo(zMoney) > 0)
                    {
                        //折扣
                        userCoupon.put("money", shopSubtotal);
                    }
                    else
                    {
                        userCoupon.clear();
                    }
                    resultMap = userCoupon;
                }
                else
                {
                    //满减/免邮门槛
                    if (CouponModal.COUPON_TYPE_MJ.equals(activeType) || CouponModal.COUPON_TYPE_MY.equals(activeType))
                    {
                        userCoupon.put("money", money);
                        if (shopSubtotal.compareTo(zMoney) >= 0)
                        {
                            // 商品总价 >= 优惠券满多少
                            resultMap = userCoupon;
                        }
                    }
                }
            }
            else
            {
                userCoupon.clear();
            }

            return resultMap;
        }
        catch (Exception e)
        {
            logger.error("【平台】优惠券筛选失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTYHQSXSB, "【平台】优惠券筛选失败", "screenCouponList");
        }
    }

    /**
     * 优惠券是否绑定
     *
     * @param storeId
     * @param time
     * @param map
     */
    private void unUseCoupons(int storeId, Date time, Map map)
    {
        try
        {
            map.put("type", 1);
            //使用中的优惠券
            List<Map<String, Object>> usersCoupons = couponModelMapper.getUsersCoupons(map);
            if (!CollectionUtils.isEmpty(usersCoupons))
            {
                // 存在 使用中 的优惠券
                for (Map<String, Object> userCoupon : usersCoupons)
                {
                    // 循环判断 优惠券是否绑定
                    // 优惠券id
                    int tmpCouponId = DataUtils.getIntegerVal(userCoupon, "id");
                    // 优惠券到期时间
                    Date expiryTime = (Date) userCoupon.get("expiry_time");
                    // 根据优惠券id,查询订单表(查看优惠券是否绑定)
                    Example          example  = new Example(OrderModel.class);
                    Example.Criteria criteria = example.createCriteria();
                    criteria.andEqualTo("store_id", storeId);
                    criteria.andEqualTo("coupon_id", tmpCouponId);
                    criteria.andCondition(" status not in(6,7) ");
                    List<OrderModel> orderModelList = orderModelMapper.selectByExample(example);
                    if (orderModelList == null || orderModelList.size() == 0)
                    {
                        // 没有数据,表示优惠券没绑定
                        int type = 0;
                        if (expiryTime != null && expiryTime.before(time))
                        {
                            // 当前时间 >= 优惠券到期时间 0:未使用 1:使用中 2:已使用 3:已过期
                            type = 3;
                        }
                        //影响行数
                        couponModelMapper.updateCoupon(type, storeId, null, tmpCouponId);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("优惠券是否绑定 异常", e);
            throw e;
        }
    }


    /**
     * 更新优惠券状态 返回0失败1成功 coupon-update_coupon
     *
     * @param storeId
     * @param userId
     * @param couponIds
     * @param type
     * @return
     */
    @Override
    public int updateCoupons(int storeId, String userId, String couponIds, int type)
    {
        try
        {
            // 优惠券ID数组
            String[] couponIdList = couponIds.split(SplitUtils.DH);
            for (String couponIdStr : couponIdList)
            {
                int couponId = Integer.parseInt(couponIdStr);
                if (couponId != 0)
                {
                    int row = couponModelMapper.updateCouponAndRecycle(type, storeId, userId, couponId);
                    if (row < 0)
                    {
                        return 0;
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("更新优惠券状态 异常", e);
            return 0;
        }
        return 1;
    }


    /**
     * 优惠券关联订单 返回0 表示关联失败 1 表示成功 添加关联订单信息 ： coupon_sno
     *
     * @param storeId
     * @param userId
     * @param couponIds
     * @param sNo
     * @param type
     * @return
     */
    @Override
    public int couponWithOrder(int storeId, String userId, String couponIds, String sNo, String type)
    {
        try
        {
            // 优惠券ID数组
            String[] couponIdsArray = couponIds.split(SplitUtils.DH);
            for (String couponIdStr : couponIdsArray)
            {
                int couponId = Integer.parseInt(couponIdStr);
                if (couponId != 0)
                {
                    if ("add".equals(type))
                    {
                        CouponOrderModal couponOrderModal = new CouponOrderModal();
                        couponOrderModal.setStore_id(storeId);
                        couponOrderModal.setUser_id(userId);
                        couponOrderModal.setCoupon_id(couponId);
                        couponOrderModal.setsNo(sNo);
                        couponOrderModal.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        couponOrderModal.setAdd_date(new Date());
                        int arow = couponOrderModalMapper.insertSelective(couponOrderModal);
                        if (arow <= 0)
                        {
                            return 0;
                        }
                    }
                    else
                    {
                        int r0 = couponOrderModalMapper.updateCouponOrder(storeId, userId, couponId);
                        if (r0 <= 0)
                        {
                            return 0;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("优惠券关联订单 异常", e);
            return 0;
        }
        return 1;
    }

    private final static ReentrantLock lktlock = new ReentrantLock();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String receive(int storeId, String userId, int id) throws LaiKeAPIException
    {
        String resultStr;
        try
        {
            //获取操作的锁
            if (lktlock.tryLock())
            {
                //获取优惠卷活动
                CouponActivityModel couponActivityModel = new CouponActivityModel();
                couponActivityModel.setId(id);
                couponActivityModel.setStore_id(storeId);
                couponActivityModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                couponActivityModel = couponActivityModelMapper.selectOne(couponActivityModel);
                if (couponActivityModel != null)
                {
                    long incr;
                    if (couponActivityModel.getReceive() == 0)
                    {
                        incr = 0;
                    }
                    else
                    {
                        //分布式锁 key + userID + 优惠卷id
                        incr = redisUtil.incr(GloabConst.RedisHeaderKey.COUPON_USER_RECEIVE + userId + id, 1);
                        //过期时间5s
                        redisUtil.expire(GloabConst.RedisHeaderKey.COUPON_USER_RECEIVE + userId + id, 5);
                    }
                    if (incr <= 1)
                    {
                        //获取用户领取当前活动优惠卷数量
                        CouponModal couponModal = new CouponModal();
                        couponModal.setStore_id(storeId);
                        couponModal.setUser_id(userId);
                        couponModal.setFree_or_not(DictionaryConst.WhetherMaven.WHETHER_NO);
                        couponModal.setHid(id);
                        int num = couponModelMapper.selectCount(couponModal);
                        logger.info("领取优惠券");
                        //判断是否超过了限制
                        if (couponActivityModel.getReceive() > num)
                        {
                            if (StringUtils.isNotEmpty(couponActivityModel.getStart_time()))
                            {
                                //是否再领取时间内
                                if (DateUtil.dateCompare(couponActivityModel.getStart_time(), new Date()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDHWKSLQSB, "活动还未开始,领取失败");
                                }
                                if (DateUtil.dateCompare(new Date(), couponActivityModel.getEnd_time()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDYJSLQSB, "活动已结束,领取失败");
                                }
                            }
                            //是否启用
                            if (CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN == couponActivityModel.getStatus())
                            {
                                //获取优惠券配置
                                /*CouponConfigModel couponConfigModel = this.getCouponConfigInfo(storeId, 0);
                                if (couponConfigModel.getLimit_type().equals(0) && num > 0) {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NYJLQGL, "您已经领取过了", "receive");
                                }*/
                                //是否还有优惠卷
                                if (couponActivityModel.getNum() > 0)
                                {
                                    //扣取活动优惠卷数量
                                    int count = couponActivityModelMapper.updateCouponByNum(id, 1);
                                    if (count < 1)
                                    {
                                        logger.debug(String.format("会员【%s】领取优惠卷 id[%s] 时,修改优惠卷数量-1失败", userId, id));
                                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "receive");
                                    }
                                    //是否领取后n天失效
                                    if (couponActivityModel.getDay() > 0)
                                    {
                                        couponActivityModel.setEnd_time(DateUtil.getAddDate(couponActivityModel.getDay()));
                                    }
                                    //添加用户优惠卷
                                    CouponModal coupon = new CouponModal();
                                    coupon.setHid(id);
                                    coupon.setStore_id(storeId);
                                    coupon.setMch_id(couponActivityModel.getMch_id());
                                    coupon.setUser_id(userId);
                                    coupon.setExpiry_time(couponActivityModel.getEnd_time());
                                    coupon.setAdd_time(new Date());
                                    count = couponModelMapper.insertSelective(coupon);
                                    if (count < 1)
                                    {
                                        logger.debug(String.format("会员【%s】领取优惠卷 id[%s] 时,添加用户优惠卷失败", userId, id));
                                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "receive");
                                    }
                                    resultStr = String.format("您领取了%s优惠卷!", couponActivityModel.getName());
                                    logger.debug(String.format("会员【%s】领取了优惠卷 id[%s] ", userId, id));
                                    //释放锁
                                    redisUtil.del(GloabConst.RedisHeaderKey.COUPON_USER_RECEIVE + userId + id);
                                }
                                else
                                {
                                    resultStr = "您来晚了";
                                    logger.debug(String.format("会员【%s】领取优惠卷 id[%s] 时,您来晚了 已抢光", userId, id));
                                }
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYHJHWQD, "该优惠卷还未启动", "receive");
                            }
                        }
                        else
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NYJLQGL, "您已经领取过了", "receive");
                        }
                    }
                    else
                    {
                        //并发请求继续领取优惠卷
                        return receive(storeId, userId, id);
                    }
                }
                else
                {
                    //没有获取到锁不让操作。并发情况允许部分用户领取失败，不能超领。
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJBCZ, "优惠卷不存在", "receive");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZPFQSHZS, "请稍后再试", "receive");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            //释放锁
            redisUtil.del(GloabConst.RedisHeaderKey.COUPON_USER_RECEIVE + userId + id);
            logger.error("领取优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "receive");
        }
        finally
        {
            if (lktlock.isHeldByCurrentThread())
            {
                lktlock.unlock();
            }
        }
        return resultStr;
    }

    @Override
    public Map<String, Object> getUser(int storeId, int hid, Integer grade, String name, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取活动信息
            CouponActivityModel couponActivityModel = new CouponActivityModel();
            couponActivityModel.setId(hid);
            couponActivityModel = couponActivityModelMapper.selectByPrimaryKey(hid);
            if (couponActivityModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            //获取用户优惠卷信息
            CouponModal couponModal = new CouponModal();
            couponModal.setHid(hid);
            couponModal.setStore_id(storeId);
            couponModal.setFree_or_not(CouponModal.COUPON_GIVE);
            List<CouponModal> couponModalList = couponModelMapper.select(couponModal);
            List<String>      userIdList      = new ArrayList<>();
            for (CouponModal coupon : couponModalList)
            {
                userIdList.add(coupon.getUser_id());
            }
            //获取会员列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("notIdList", userIdList);
            parmaMap.put("grade", grade);
            parmaMap.put("user_name1", name);
            parmaMap.put("Register_data_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", pageModel.getPageNo());
            parmaMap.put("pageEnd", pageModel.getPageSize());
            int                       total       = userBaseMapper.countDynamic(parmaMap);
            List<Map<String, Object>> userListMap = userBaseMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : userListMap)
            {
                UserGradeModel userGradeModel = userGradeModelMapper.selectByPrimaryKey(map.get("grade"));
                String         gradeName      = "普通会员";
                if (userGradeModel != null)
                {
                    gradeName = userGradeModel.getName();
                }
                map.put("user_name", map.get("user_name"));
                map.put("grade_name", gradeName);
            }

            resultMap.put("total", total);
            resultMap.put("userList", userListMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取会员列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUser");
        }

        return resultMap;
    }

    @Override
    public List<Map<String, Object>> splitOrder(int storeId, String userId, String sNo)
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel.setUser_id(userId);
            orderModel = orderModelMapper.selectOne(orderModel);
            //每个商品价格,用于计算平摊
            Map<Integer, BigDecimal> currentPriceMap = new HashMap<>(16);

            if (orderModel != null)
            {
                // 店铺ID字符串
                String mchId = orderModel.getMch_id();
                // 优惠券ID
                String couponIdStr = orderModel.getCoupon_id();
                // 会员折扣(会员制暂时去掉 2022-02-16 16:28:13)
                BigDecimal gradeRate = BigDecimal.ONE;
                // 去掉字符串第一个字符和最后一个字符
                mchId = StringUtils.trim(mchId, SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
                // 跨店铺
                if (!CollectionUtils.isEmpty(shopIds) && shopIds.size() > 1)
                {
                    if (!StringUtils.equals(couponIdStr, "0") && StringUtils.isNotEmpty(couponIdStr))
                    {
                        // 优惠券ID数组
                        List<String> couponIdList = Splitter.on(SplitUtils.DH).splitToList(couponIdStr);
                        // 最后一个优惠券ID下标
                        int couponIdPos = couponIdList.size() - 1;
                        // 最后一个优惠券ID(平台发行优惠券)
                        int platformCouponId = Integer.parseInt(couponIdList.get(couponIdPos));
                        // 小计之和
                        BigDecimal sumOfSubtotal = BigDecimal.ZERO;
                        int        pos           = 0;
                        for (String couponIdVal : couponIdList)
                        {
                            int couponId = Integer.parseInt(couponIdVal);
                            // 优惠金额
                            BigDecimal preferentialAmount = BigDecimal.ZERO;
                            // 店铺商品总价
                            BigDecimal mchSpzPrice = BigDecimal.ZERO;
                            // 当前健名 != 最后一个优惠券ID健名 （店铺发行优惠券）[优惠券参数规则会多一个平台、所以循环需要过滤掉平台]
                            if (couponIdPos != pos)
                            {
                                //当前使用优惠券的店铺
                                mchId = shopIds.get(pos);
                                if (couponId != 0)
                                {
                                    // 根据商城ID、用户ID、店铺优惠券ID，查询优惠券信息
                                    List<Map<String, Object>> couponsInfoList = couponModelMapper.getCouponsInfoList(storeId, userId, couponId);
                                    if (!CollectionUtils.isEmpty(couponsInfoList))
                                    {
                                        Map<String, Object> couponsInfoMap = couponsInfoList.get(0);
                                        // 店铺ID
                                        int mchIdTmp = MapUtils.getIntValue(couponsInfoMap, "mch_id");
                                        // 优惠券类型
                                        int activity_type = DataUtils.getIntegerVal(couponsInfoMap, "activity_type");
                                        // 优惠券使用范围 1：全部商品 2:指定商品 3：指定分类
                                        Integer type = DataUtils.getIntegerVal(couponsInfoMap, "type");
                                        // 面值
                                        BigDecimal money = DataUtils.getBigDecimalVal(couponsInfoMap, "money");
                                        // 折扣值
                                        BigDecimal discount = DataUtils.getBigDecimalVal(couponsInfoMap, "discount");
                                        // 分类id
                                        String productClassId = DataUtils.getStringVal(couponsInfoMap, "product_class_id");
                                        // 商品id
                                        String productId1 = DataUtils.getStringVal(couponsInfoMap, "product_id");
                                        // 符合优惠券的商品总价
                                        BigDecimal accordWith = BigDecimal.ZERO;
                                        // 根据商城ID、订单号、店铺ID，查询订单详情
                                        List<Map<String, Object>> orderDetails4CouponList = orderModelMapper.getOrderDetails(storeId, sNo, mchIdTmp);
                                        if (!CollectionUtils.isEmpty(orderDetails4CouponList))
                                        {
                                            for (Map<String, Object> orderDetailsInfo : orderDetails4CouponList)
                                            {
                                                //规格价格
                                                BigDecimal pPrice = DataUtils.getBigDecimalVal(orderDetailsInfo, "p_price");
                                                //购买的数量
                                                BigDecimal num = DataUtils.getBigDecimalVal(orderDetailsInfo, "num");
                                                //商品id
                                                String pId = DataUtils.getStringVal(orderDetailsInfo, "p_id");
                                                //商品分类
                                                String productClass = DataUtils.getStringVal(orderDetailsInfo, "product_class");
                                                //单规格商品总价
                                                BigDecimal multiply = pPrice.multiply(gradeRate).multiply(num);
                                                //所有商品都可以用的优惠券
                                                if (type.equals(CouponActivityModel.USE_RANG_TYPE_ALL))
                                                {
                                                    // 符合优惠券的商品总价
                                                    accordWith = accordWith.add(multiply);
                                                    //指定商品可用
                                                }
                                                else if (type.equals(CouponActivityModel.USE_RANG_TYPE_PRODUCT))
                                                {
                                                    if (StringUtils.isNotEmpty(productId1))
                                                    {
                                                        // 去除字符串最后一个逗号
                                                        productId1 = StringUtils.rtrim(Objects.requireNonNull(SerializePhpUtils.getUnserializeString(productId1)), SplitUtils.DH);
                                                    }
                                                    // 字符串转数组
                                                    List<String> productIdList = Splitter.on(SplitUtils.DH).splitToList(productId1);
                                                    if (productIdList.contains(pId))
                                                    {
                                                        // 符合优惠券的商品总价
                                                        accordWith = accordWith.add(multiply);
                                                    }
                                                    //指定分类可用
                                                }
                                                else if (type.equals(CouponActivityModel.USE_RANG_TYPE_CLASS))
                                                {
                                                    productClassId = StringUtils.rtrim(Objects.requireNonNull(SerializePhpUtils.getUnserializeString(productClassId)), SplitUtils.DH);
                                                    // 字符串转数组
                                                    List<String> product_class_list = Splitter.on(SplitUtils.DH).splitToList(productClassId);
                                                    for (String product_class_tmp : product_class_list)
                                                    {
                                                        if (productClass.contains(product_class_tmp))
                                                        {
                                                            // 符合优惠券的商品总价
                                                            accordWith = accordWith.add(multiply);
                                                        }
                                                    }
                                                }
                                                // 符合优惠券的商品总价
                                                mchSpzPrice = mchSpzPrice.add(multiply);
                                            }
                                        }

                                        // 打完折扣后的金额
                                        BigDecimal zongMoney = accordWith.multiply(discount).divide(BigDecimal.TEN, 6, BigDecimal.ROUND_HALF_UP);
                                        // 不是打折优惠
                                        BigDecimal zongMoney1 = BigDecimal.ZERO;
                                        if (zongMoney.compareTo(BigDecimal.ZERO) > 0)
                                        {
                                            // 打折优惠
                                            zongMoney1 = accordWith.subtract(zongMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        }
                                        if (CouponModal.COUPON_TYPE_MJ.equals(activity_type + ""))
                                        {
                                            // 满减券 // 店铺优惠金额
                                            preferentialAmount = money;
                                        }
                                        else if (CouponModal.COUPON_TYPE_ZK.equals(activity_type + ""))
                                        {
                                            // 折扣券 // 店铺优惠金额
                                            preferentialAmount = zongMoney1;
                                        }
                                    }
                                }
                                else
                                {
                                    List<Map<String, Object>> orderDetails4CouponList = orderModelMapper.getOrderDetails(storeId, sNo, Integer.parseInt(mchId));
                                    if (!CollectionUtils.isEmpty(orderDetails4CouponList))
                                    {
                                        for (Map<String, Object> orderDetailsInfo : orderDetails4CouponList)
                                        {
                                            //订单明细价格
                                            BigDecimal pPrice = DataUtils.getBigDecimalVal(orderDetailsInfo, "p_price");
                                            //商品数量
                                            BigDecimal num = DataUtils.getBigDecimalVal(orderDetailsInfo, "num");
                                            //当前商品运费
                                            BigDecimal freightPrice = new BigDecimal(MapUtils.getString(orderDetailsInfo, "freight", "0"));
                                            // 当前商品实际支付价格(不算运费)
                                            mchSpzPrice = mchSpzPrice.add(pPrice.multiply(gradeRate).multiply(num));

                                        }
                                    }
                                }
                                Map<String, Object> retMap = new HashMap<>(16);
                                // 优惠ID
                                retMap.put("couponIdStr", couponIdVal);
                                retMap.put("couponId", couponId);
                                // 店铺ID
                                retMap.put("mchId", mchId);
                                // 店铺优惠金额
                                retMap.put("preferentialAmount", preferentialAmount);
                                // 该店铺下商品原价
                                retMap.put("origin_price", mchSpzPrice);
                                // 店铺小计
                                BigDecimal subtotal = mchSpzPrice.subtract(preferentialAmount);
                                retMap.put("subtotal", subtotal);
                                // 小计之和
                                sumOfSubtotal = sumOfSubtotal.add(subtotal);
                                resultList.add(retMap);
                                pos++;
                            }
                        }

                        //平台优惠
                        BigDecimal platformPreferentialAmount = BigDecimal.ZERO;
                        if (platformCouponId != 0)
                        {
                            // 根据商城ID、用户ID、平台优惠券ID，查询优惠券信息
                            List<Map<String, Object>> couponsInfoList = couponModelMapper.getCouponsInfoList(storeId, userId, platformCouponId);
                            Map<String, Object>       platformCouponInfoMap;
                            if (!CollectionUtils.isEmpty(couponsInfoList))
                            {
                                platformCouponInfoMap = couponsInfoList.get(0);
                                // 优惠券类型
                                int activityType = DataUtils.getIntegerVal(platformCouponInfoMap, "activity_type");
                                // 面值
                                BigDecimal money = DataUtils.getBigDecimalVal(platformCouponInfoMap, "money");
                                // 折扣值
                                BigDecimal discount = DataUtils.getBigDecimalVal(platformCouponInfoMap, "discount");
                                // 打完折扣后的金额
                                BigDecimal zongMoney = sumOfSubtotal.multiply(discount.divide(BigDecimal.TEN, 2, BigDecimal.ROUND_HALF_UP));
                                // 优惠金额
                                BigDecimal zongMoney1 = BigDecimal.ZERO;
                                // 不是打折优惠
                                if (zongMoney.compareTo(BigDecimal.ZERO) > 0)
                                {
                                    zongMoney1 = sumOfSubtotal.subtract(zongMoney);
                                }
                                if (CouponModal.COUPON_TYPE_MJ.equals(activityType + ""))
                                {
                                    // 满减券
                                    platformPreferentialAmount = money;
                                }
                                else if (CouponModal.COUPON_TYPE_ZK.equals(activityType + ""))
                                {
                                    // 折扣券
                                    platformPreferentialAmount = zongMoney1;
                                }
                            }
                        }

                        for (Map<String, Object> retMapTmp : resultList)
                        {
                            int currentMchId = MapUtils.getInteger(retMapTmp, "mchId");
                            //店铺小计（减去了店铺优惠金额）
                            BigDecimal subtotal = DataUtils.getBigDecimalVal(retMapTmp, "subtotal");
                            currentPriceMap.put(currentMchId, subtotal);
                        }
                        Map<Integer, BigDecimal> youHuiMap = com.laiketui.common.utils.algorithm.DataAlgorithmTool.orderPriceAverage(sumOfSubtotal, currentPriceMap, platformPreferentialAmount);
                        for (Map<String, Object> retMapTmp : resultList)
                        {
                            int    currentMchId   = MapUtils.getIntValue(retMapTmp, "mchId");
                            String couponIdStrTmp = DataUtils.getStringVal(retMapTmp, "subtotal");
                            retMapTmp.put("couponIdStr", couponIdStrTmp + SplitUtils.DH + platformCouponId);
                            retMapTmp.put("couponId", retMapTmp.get("couponId"));
                            retMapTmp.put("platformCouponId", platformCouponId);
                            retMapTmp.put("mchId", currentMchId);
                            //这里是店铺优惠金额
                            retMapTmp.put("mch_coupon_amount", retMapTmp.get("preferentialAmount"));
                            //这里是平台优惠金额
                            retMapTmp.put("platformPreferentialAmount", platformPreferentialAmount);

                            //当前铺优惠金额 店铺优惠金额+(店铺小计/小计之和*平台优化金额)
                            BigDecimal val = youHuiMap.get(currentMchId);
                            //这个是总优惠金额 店铺优惠金额总和+平台优惠金额
                            retMapTmp.put("preferential_amount", val);
                        }
                    }
                }
            }

            return resultList;
        }
        catch (Exception e)
        {
            logger.error("优惠券拆分订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQCFDDSB, "优惠券拆分订单失败", "splitOrder");
        }
    }


    @Override
    public List<Map<String, Object>> splitOrderForVI(int storeId, String userId, String sNo)
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel.setUser_id(userId);
            orderModel = orderModelMapper.selectOne(orderModel);
            //每个商品价格,用于计算平摊
            Map<Integer, BigDecimal> currentPriceMap = new HashMap<>(16);

            if (orderModel != null)
            {
                // 店铺ID字符串
                String mchId = orderModel.getMch_id();
                // 优惠券ID
                String couponIdStr = orderModel.getCoupon_id();
                // 会员折扣(会员制暂时去掉 2022-02-16 16:28:13)
                BigDecimal gradeRate = BigDecimal.ONE;
                // 去掉字符串第一个字符和最后一个字符
                mchId = StringUtils.trim(mchId, SplitUtils.DH);
                // 店铺id字符串
                List<String> shopIds = Splitter.on(SplitUtils.DH).splitToList(mchId);
                // 跨店铺
                if (!CollectionUtils.isEmpty(shopIds))
                {
                    if (!StringUtils.equals(couponIdStr, "0") && StringUtils.isNotEmpty(couponIdStr))
                    {
                        // 优惠券ID数组
                        List<String> couponIdList = Splitter.on(SplitUtils.DH).splitToList(couponIdStr);
                        // 最后一个优惠券ID下标
                        int couponIdPos = couponIdList.size() - 1;
                        // 最后一个优惠券ID(平台发行优惠券)
                        int platformCouponId = Integer.parseInt(couponIdList.get(couponIdPos));
                        // 小计之和
                        BigDecimal sumOfSubtotal = BigDecimal.ZERO;
                        int        pos           = 0;
                        for (String couponIdVal : couponIdList)
                        {
                            int couponId = Integer.parseInt(couponIdVal);
                            // 优惠金额
                            BigDecimal preferentialAmount = BigDecimal.ZERO;
                            // 店铺商品总价
                            BigDecimal mchSpzPrice = BigDecimal.ZERO;
                            // 当前健名 != 最后一个优惠券ID健名 （店铺发行优惠券）[优惠券参数规则会多一个平台、所以循环需要过滤掉平台]
                            if (couponIdPos != pos)
                            {
                                //当前使用优惠券的店铺
                                mchId = shopIds.get(pos);
                                if (couponId != 0)
                                {
                                    // 根据商城ID、用户ID、店铺优惠券ID，查询优惠券信息
                                    List<Map<String, Object>> couponsInfoList = couponModelMapper.getCouponsInfoList(storeId, userId, couponId);
                                    if (!CollectionUtils.isEmpty(couponsInfoList))
                                    {
                                        Map<String, Object> couponsInfoMap = couponsInfoList.get(0);
                                        // 店铺ID
                                        int mchIdTmp = MapUtils.getIntValue(couponsInfoMap, "mch_id");
                                        // 优惠券类型
                                        int activity_type = DataUtils.getIntegerVal(couponsInfoMap, "activity_type");
                                        // 优惠券使用范围 1：全部商品 2:指定商品 3：指定分类
                                        Integer type = DataUtils.getIntegerVal(couponsInfoMap, "type");
                                        // 面值
                                        BigDecimal money = DataUtils.getBigDecimalVal(couponsInfoMap, "money");
                                        // 折扣值
                                        BigDecimal discount = DataUtils.getBigDecimalVal(couponsInfoMap, "discount");
                                        // 分类id
                                        String productClassId = DataUtils.getStringVal(couponsInfoMap, "product_class_id");
                                        // 商品id
                                        String productId1 = DataUtils.getStringVal(couponsInfoMap, "product_id");
                                        // 符合优惠券的商品总价
                                        BigDecimal accordWith = BigDecimal.ZERO;
                                        // 根据商城ID、订单号、店铺ID，查询订单详情
                                        List<Map<String, Object>> orderDetails4CouponList = orderModelMapper.getOrderDetails(storeId, sNo, mchIdTmp);
                                        if (!CollectionUtils.isEmpty(orderDetails4CouponList))
                                        {
                                            for (Map<String, Object> orderDetailsInfo : orderDetails4CouponList)
                                            {
                                                //规格价格
                                                BigDecimal pPrice = DataUtils.getBigDecimalVal(orderDetailsInfo, "p_price");
                                                //购买的数量
                                                BigDecimal num = DataUtils.getBigDecimalVal(orderDetailsInfo, "num");
                                                //商品id
                                                String pId = DataUtils.getStringVal(orderDetailsInfo, "p_id");
                                                //商品分类
                                                String productClass = DataUtils.getStringVal(orderDetailsInfo, "product_class");
                                                //单规格商品总价
                                                BigDecimal multiply = pPrice.multiply(gradeRate).multiply(num);
                                                //所有商品都可以用的优惠券
                                                if (type.equals(CouponActivityModel.USE_RANG_TYPE_ALL))
                                                {
                                                    // 符合优惠券的商品总价
                                                    accordWith = accordWith.add(multiply);
                                                    //指定商品可用
                                                }
                                                else if (type.equals(CouponActivityModel.USE_RANG_TYPE_PRODUCT))
                                                {
                                                    if (StringUtils.isNotEmpty(productId1))
                                                    {
                                                        // 去除字符串最后一个逗号
                                                        productId1 = StringUtils.rtrim(Objects.requireNonNull(SerializePhpUtils.getUnserializeString(productId1)), SplitUtils.DH);
                                                    }
                                                    // 字符串转数组
                                                    List<String> productIdList = Splitter.on(SplitUtils.DH).splitToList(productId1);
                                                    if (productIdList.contains(pId))
                                                    {
                                                        // 符合优惠券的商品总价
                                                        accordWith = accordWith.add(multiply);
                                                    }
                                                    //指定分类可用
                                                }
                                                else if (type.equals(CouponActivityModel.USE_RANG_TYPE_CLASS))
                                                {
                                                    productClassId = StringUtils.rtrim(Objects.requireNonNull(SerializePhpUtils.getUnserializeString(productClassId)), SplitUtils.DH);
                                                    // 字符串转数组
                                                    List<String> product_class_list = Splitter.on(SplitUtils.DH).splitToList(productClassId);
                                                    for (String product_class_tmp : product_class_list)
                                                    {
                                                        if (productClass.contains(product_class_tmp))
                                                        {
                                                            // 符合优惠券的商品总价
                                                            accordWith = accordWith.add(multiply);
                                                        }
                                                    }
                                                }
                                                // 符合优惠券的商品总价
                                                mchSpzPrice = mchSpzPrice.add(multiply);
                                            }
                                        }

                                        // 打完折扣后的金额
                                        BigDecimal zongMoney = accordWith.multiply(discount).divide(BigDecimal.TEN, 6, BigDecimal.ROUND_HALF_UP);
                                        // 不是打折优惠
                                        BigDecimal zongMoney1 = BigDecimal.ZERO;
                                        if (zongMoney.compareTo(BigDecimal.ZERO) > 0)
                                        {
                                            // 打折优惠
                                            zongMoney1 = accordWith.subtract(zongMoney).setScale(2, BigDecimal.ROUND_HALF_UP);
                                        }
                                        if (CouponModal.COUPON_TYPE_MJ.equals(activity_type + ""))
                                        {
                                            // 满减券 // 店铺优惠金额
                                            preferentialAmount = money;
                                        }
                                        else if (CouponModal.COUPON_TYPE_ZK.equals(activity_type + ""))
                                        {
                                            // 折扣券 // 店铺优惠金额
                                            preferentialAmount = zongMoney1;
                                        }
                                    }
                                }
                                else
                                {
                                    List<Map<String, Object>> orderDetails4CouponList = orderModelMapper.getOrderDetails(storeId, sNo, Integer.parseInt(mchId));
                                    if (!CollectionUtils.isEmpty(orderDetails4CouponList))
                                    {
                                        for (Map<String, Object> orderDetailsInfo : orderDetails4CouponList)
                                        {
                                            //订单明细价格
                                            BigDecimal pPrice = DataUtils.getBigDecimalVal(orderDetailsInfo, "p_price");
                                            //商品数量
                                            BigDecimal num = DataUtils.getBigDecimalVal(orderDetailsInfo, "num");
                                            //当前商品运费
                                            BigDecimal freightPrice = new BigDecimal(MapUtils.getString(orderDetailsInfo, "freight", "0"));
                                            // 当前商品实际支付价格(不算运费)
                                            mchSpzPrice = mchSpzPrice.add(pPrice.multiply(gradeRate).multiply(num));

                                        }
                                    }
                                }
                                Map<String, Object> retMap = new HashMap<>(16);
                                // 优惠ID
                                retMap.put("couponIdStr", couponIdVal);
                                retMap.put("couponId", couponId);
                                // 店铺ID
                                retMap.put("mchId", mchId);
                                // 店铺优惠金额
                                retMap.put("preferentialAmount", preferentialAmount);
                                // 该店铺下商品原价
                                retMap.put("origin_price", mchSpzPrice);
                                // 店铺小计
                                BigDecimal subtotal = mchSpzPrice.subtract(preferentialAmount);
                                retMap.put("subtotal", subtotal);
                                // 小计之和
                                sumOfSubtotal = sumOfSubtotal.add(subtotal);
                                resultList.add(retMap);
                                pos++;
                            }
                        }

                        //平台优惠
                        BigDecimal platformPreferentialAmount = BigDecimal.ZERO;
                        if (platformCouponId != 0)
                        {
                            // 根据商城ID、用户ID、平台优惠券ID，查询优惠券信息
                            List<Map<String, Object>> couponsInfoList = couponModelMapper.getCouponsInfoList(storeId, userId, platformCouponId);
                            Map<String, Object>       platformCouponInfoMap;
                            if (!CollectionUtils.isEmpty(couponsInfoList))
                            {
                                platformCouponInfoMap = couponsInfoList.get(0);
                                // 优惠券类型
                                int activityType = DataUtils.getIntegerVal(platformCouponInfoMap, "activity_type");
                                // 面值
                                BigDecimal money = DataUtils.getBigDecimalVal(platformCouponInfoMap, "money");
                                // 折扣值
                                BigDecimal discount = DataUtils.getBigDecimalVal(platformCouponInfoMap, "discount");
                                // 打完折扣后的金额
                                //BigDecimal zongMoney = new BigDecimal(orderModel.getSpz_price().toString()).multiply(discount.divide(BigDecimal.TEN, 2, BigDecimal.ROUND_HALF_UP));
                                BigDecimal zongMoney = sumOfSubtotal.multiply(discount.divide(BigDecimal.TEN, 2, BigDecimal.ROUND_HALF_UP));
                                // 优惠金额
                                BigDecimal zongMoney1 = BigDecimal.ZERO;
                                // 不是打折优惠
                                if (zongMoney.compareTo(BigDecimal.ZERO) > 0)
                                {
                                    //zongMoney1 = new BigDecimal(orderModel.getSpz_price().toString()).subtract(zongMoney);
                                    zongMoney1 = sumOfSubtotal.subtract(zongMoney);
                                }
                                if (CouponModal.COUPON_TYPE_MJ.equals(activityType + ""))
                                {
                                    // 满减券
                                    platformPreferentialAmount = money;
                                }
                                else if (CouponModal.COUPON_TYPE_ZK.equals(activityType + ""))
                                {
                                    // 折扣券
                                    platformPreferentialAmount = zongMoney1;
                                }
                            }
                        }

                        for (Map<String, Object> retMapTmp : resultList)
                        {
                            int currentMchId = MapUtils.getInteger(retMapTmp, "mchId");
                            //店铺小计（减去了店铺优惠金额）
                            BigDecimal subtotal = DataUtils.getBigDecimalVal(retMapTmp, "subtotal");
                            currentPriceMap.put(currentMchId, subtotal);
                        }
                        Map<Integer, BigDecimal> youHuiMap = com.laiketui.common.utils.algorithm.DataAlgorithmTool.orderPriceAverage(sumOfSubtotal, currentPriceMap, platformPreferentialAmount);
                        for (Map<String, Object> retMapTmp : resultList)
                        {
                            int    currentMchId   = MapUtils.getIntValue(retMapTmp, "mchId");
                            String couponIdStrTmp = DataUtils.getStringVal(retMapTmp, "subtotal");
                            retMapTmp.put("couponIdStr", couponIdStrTmp + SplitUtils.DH + platformCouponId);
                            retMapTmp.put("couponId", retMapTmp.get("couponId"));
                            retMapTmp.put("platformCouponId", platformCouponId);
                            retMapTmp.put("mchId", currentMchId);
                            //这里是店铺优惠金额
                            retMapTmp.put("mch_coupon_amount", retMapTmp.get("preferentialAmount"));
                            //这里是平台优惠金额
                            retMapTmp.put("platformPreferentialAmount", platformPreferentialAmount);

                            //当前铺优惠金额 店铺优惠金额+(店铺小计/小计之和*平台优化金额)
                            BigDecimal val = youHuiMap.get(currentMchId);
                            //这个是总优惠金额 店铺优惠金额总和+平台优惠金额
                            retMapTmp.put("preferential_amount", val);
                        }
                    }
                }
            }

            return resultList;
        }
        catch (Exception e)
        {
            logger.error("优惠券拆分订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQCFDDSB, "优惠券拆分订单失败", "splitOrder");
        }
    }

    @Override
    public Map<String, Object> mobileTerminalCouponCenter(int storeId, String userId, Integer type, PageModel pageModel, String mchName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Integer             zyMchId      = customerModelMapper.getStoreMchId(storeId);
            PluginsModel        pluginsModel = pluginsModelMapper.getPluginInfo("coupon", storeId);
            Map<String, Object> parmaMap     = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("status", CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            //店铺开启了优惠劵  禅道49844
            parmaMap.put("isOpenCoupon", DictionaryConst.DefaultMaven.DEFAULT_OK);
            if (!publiceService.frontPlugin(storeId, DictionaryConst.Plugin.COUPON, new HashMap<>(16)))
            {
                //只获取自营店的优惠券
                parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            }
            //查询该商城所有开启的优惠券
            List<Map<String, Object>> couponActivityList = couponActivityModelMapper.getCouponActivityDynamic(parmaMap);

            parmaMap.clear();
            parmaMap.put("store_id", storeId);
            if (type != null)
            {
                if (type == 1)
                {
                    //平台
                    parmaMap.put("mch_id", GloabConst.LktConfig.LKT_CONFIG_TYPE_PT);
                }
                else
                {
                    //优惠劵插件未开启--只显示自营店优惠劵
                    if (pluginsModel == null || pluginsModel.getStatus().equals(DictionaryConst.WhetherMaven.WHETHER_NO))
                    {
                        parmaMap.put("mch_id", zyMchId);
                    }
                    //店铺
                    parmaMap.put("not_mch_id", GloabConst.LktConfig.LKT_CONFIG_TYPE_PT);
                    //店铺开启了优惠劵  禅道49844
                    parmaMap.put("isOpenCoupon", DictionaryConst.DefaultMaven.DEFAULT_OK);
                }
            }
            else
            {
                //优惠劵插件未开启--只显示自营店优惠劵/平台优惠劵
                if (pluginsModel == null || pluginsModel.getStatus().equals(DictionaryConst.WhetherMaven.WHETHER_NO))
                {
                    ArrayList<Integer> mchList = new ArrayList<>();
                    mchList.add(zyMchId);
                    mchList.add(GloabConst.LktConfig.LKT_CONFIG_TYPE_PT);
                    parmaMap.put("mchList", mchList);
                }
                //店铺开启了优惠劵  禅道49844
                parmaMap.put("isOpenCoupon", DictionaryConst.DefaultMaven.DEFAULT_OK);
            }

            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("receiveType", CouponActivityModel.ReceiveType.AUTO);
            parmaMap.put("status", CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
            if (StringUtils.isNotEmpty(mchName))
            {
                parmaMap.put("mchName", mchName);
            }
            parmaMap.put("activity_type_4", "activity_type_4");
            parmaMap.put("is_display", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            //parmaMap.put("page", pageModel.getPageNum());
            //parmaMap.put("pageSize", pageModel.getPageSize());
            int                       total    = couponActivityModelMapper.countCouponActivityDynamic(parmaMap);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (total > 0)
            {
                dataList = couponActivityModelMapper.getCouponActivityDynamic(parmaMap);
                couponCenter(storeId, userId, dataList);
                dataList = getPage(dataList, pageModel.getPageNo(), pageModel.getPageSize());
            }
            resultMap.put("total", total);
            resultMap.put("list", dataList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("移动端-领券中心 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mobileTerminalCouponCenter");
        }
        return resultMap;
    }

    // 分页方法，根据页码和页大小返回对应页的数据
    public static List<Map<String, Object>> getPage(List<Map<String, Object>> list, int pageNumber, int pageSize)
    {
        if (pageNumber == 0)
        {
            pageNumber = 1;
        }
        if (list == null || pageSize <= 0 || pageNumber <= 0)
        {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        int fromIndex = (pageNumber - 1) * pageSize;
        if (fromIndex >= list.size())
        {
            // 如果起始索引超出列表大小，返回空列表
            return new ArrayList<>();
        }

        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }

    @Override
    public List<Map<String, Object>> proCoupon(int storeId, String userId, int goodsId) throws LaiKeAPIException
    {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try
        {
            //获取商品信息
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setId(goodsId);
            productListModel = productListModelMapper.selectOne(productListModel);
            if (productListModel != null)
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", storeId);
                parmaMap.put("status", CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
                parmaMap.put("mch_id", productListModel.getMch_id());
                parmaMap.put("receiveType", CouponActivityModel.ReceiveType.AUTO.toString());
                parmaMap.put("start_time_sort", DataUtils.Sort.DESC.toString());
                //根据商品所属店铺获取活动信息
                List<Map<String, Object>> resultList = couponActivityModelMapper.getCouponActivityDynamic(parmaMap);
                for (Map<String, Object> map : resultList)
                {
                    //优惠卷活动id
                    int activityId = Integer.parseInt(map.get("id").toString());
                    //优惠卷类型
                    int activityType = Integer.parseInt(map.get("activity_type").toString());
                    //满减金额
                    BigDecimal fullReductionAmt = new BigDecimal(map.get("z_money").toString());
                    //领取限制
                    int receive = Integer.parseInt(map.get("receive").toString());
                    //优惠卷使用类型
                    int type = StringUtils.stringParseInt(map.get("type") + "");
                    //剩余数量
                    int surplusNum = Integer.parseInt(map.get("num").toString());
                    //指定商品id集
                    String goodsIdSerialize = map.get("product_id") + "";
                    //指定商品类型id集
                    String goodsTypeSerialize = map.get("product_class_id") + "";
                    //活动开始时间
                    Date startDate = (Date) map.get("start_time");
                    //活动结束时间
                    Date endDate = (Date) map.get("end_time");
                    //失效天数
                    Integer day = MapUtils.getInteger(map, "day");
                    if (endDate != null)
                    {
                        map.put("start_time", DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMD3));
                        map.put("end_time", DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMD3));
                    }
                    //优惠卷名称
                    String name = map.get("name").toString();
                    if (productListModel.getMch_id() != 0)
                    {
                        MchModel mchModel = new MchModel();
                        mchModel.setStore_id(storeId);
                        mchModel.setId(productListModel.getMch_id());
                        mchModel = mchModelMapper.selectOne(mchModel);
                        if (mchModel != null)
                        {
                            map.put("coupon_name", String.format("[%s]%s", mchModel.getName(), name));
                        }
                    }

                    //消费限制处理
                    String limitStr = "无限制";
                    if (fullReductionAmt.floatValue() > 0)
                    {
                        limitStr = String.format("满%s可用", fullReductionAmt);
                    }
                    map.put("limit", limitStr);
                    //判断活动是否过期
                    if (endDate != null && DateUtil.dateCompare(new Date(), endDate))
                    {
                        //设置活动已过期
                        CouponActivityModel couponActivityModel = new CouponActivityModel();
                        couponActivityModel.setId(activityId);
                        couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_END);
                        int count = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityModel);
                        if (count < 1)
                        {
                            logger.info("活动已结束,但是状态修改失败 参数" + JSON.toJSONString(couponActivityModel));
                        }
                    }
                    else
                    {
                        String point     = "已抢光";
                        int    pointType = 3;
                        if (surplusNum > 0)
                        {
                            //还又剩余券
                            if (StringUtils.isEmpty(userId))
                            {
                                if (type == 1)
                                {
                                    point = "立即领取";
                                }
                                else
                                {
                                    point = "领取";
                                }
                                pointType = 1;
                            }
                            else
                            {
                                //获取当前活动用户领取的优惠卷信息
                                CouponModal couponModal = new CouponModal();
                                couponModal.setUser_id(userId);
                                couponModal.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                                couponModal.setHid(activityId);
                                int receivedNum = couponModelMapper.selectCount(couponModal);
                                if (receivedNum > 0)
                                {
                                    //如果已经领取 判断是否超过领取数量限制
                                    if (receive > receivedNum)
                                    {
                                        //继续领取
                                        if (type == 1)
                                        {
                                            point = "立即领取";
                                        }
                                        else
                                        {
                                            point = "领取";
                                        }
                                        pointType = 1;
                                    }
                                    else
                                    {
                                        //超过领取限制
                                        if (type == 1)
                                        {
                                            point = "可用商品";
                                        }
                                        else
                                        {
                                            point = "去使用";
                                        }
                                        pointType = 2;
                                        continue;
                                    }
                                }
                                else
                                {
                                    //未领取
                                    if (type == 1)
                                    {
                                        point = "立即领取";
                                    }
                                    else
                                    {
                                        point = "领取";
                                    }
                                    pointType = 1;
                                }
                            }
                        }
                        String expirationDate = "领取后%s天失效";
                        //过期时间  1不过期  2设置指定过期时间  3设置领取后多久失效
                        if (day != null && day != 0)
                        {
                            expirationDate = String.format(expirationDate, day);
                        }
                        else if (StringUtils.isNotEmpty(endDate))
                        {
                            expirationDate = DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMD) + " 到期";
                        }
                        else
                        {
                            expirationDate = "该劵永久有效";
                        }
                        map.put("point", point);
                        map.put("point_type", pointType);
                        map.put("expirationDate", expirationDate);
                    }

                    //非会员赠送
                    int pointType = StringUtils.stringParseInt(map.get("point_type") + "");
                    if (!CouponModal.COUPON_TYPE_HYZS.equals(activityType + ""))
                    {
                        if (type == CouponActivityModel.USE_RANG_TYPE_ALL)
                        {
                            if (pointType != 3)
                            {
                                dataList.add(map);
                            }
                        }
                        else if (type == CouponActivityModel.USE_RANG_TYPE_PRODUCT)
                        {
                            //s:15:"757,906,907,909";
                            String goodsIds = SerializePhpUtils.getUnserializeByBasic(goodsIdSerialize, String.class);
                            if (goodsIds != null)
                            {
                                List<String> goodsIdList = Arrays.asList(goodsIds.split(","));
                                //当前商品是否包含在指定商品中
                                if (goodsIdList.contains(goodsId + ""))
                                {
                                    if (pointType != 3)
                                    {
                                        dataList.add(map);
                                    }
                                }
                            }
                        }
                        else if (type == CouponActivityModel.USE_RANG_TYPE_CLASS)
                        {
                            //s:8:"8,58,394";
                            String goodsTypes = SerializePhpUtils.getUnserializeByBasic(goodsTypeSerialize, String.class);
                            if (goodsTypes != null)
                            {
                                //优惠卷所属的商品类别集id
                                Set<String> goodsClassIdList = new HashSet<>(Arrays.asList(goodsTypes.split(",")));
                                //当前商品类别集
                                List<String> classIdList = Arrays.asList(StringUtils.trim(productListModel.getProduct_class(), "-").split("-"));
                                //当前商品类别是否包含在指定商品中
                                for (String goodsClassId : goodsClassIdList)
                                {
                                    if (classIdList.contains(goodsClassId))
                                    {
                                        if (pointType != 3)
                                        {
                                            dataList.add(map);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "proCoupon");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("移动端获取商品可用优惠券活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "proCoupon");
        }
        return dataList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> pcCoupon(int storeId, String userId, Integer type) throws LaiKeAPIException
    {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try
        {
            CouponActivityModel couponActivityModel = new CouponActivityModel();
            couponActivityModel.setStore_id(storeId);
            couponActivityModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_NOT_USE);
            couponActivityModel.setIs_display(DictionaryConst.WhetherMaven.WHETHER_OK);
            List<CouponActivityModel> couponActivityModelList = couponActivityModelMapper.select(couponActivityModel);
            for (CouponActivityModel couponActivity : couponActivityModelList)
            {
                //如果活动未启动,则看是否到了启动的时间
                if (couponActivity.getStart_time() != null && !DateUtil.dateCompare(new Date(), couponActivity.getStart_time()))
                {
                    //开启活动
                    CouponActivityModel couponActivityUpdate = new CouponActivityModel();
                    couponActivityUpdate.setId(couponActivity.getId());
                    couponActivityUpdate.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
                    couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityUpdate);
                }
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("activity_type_4", "activity_type_4");
            parmaMap.put("is_display", 1);
            parmaMap.put("status", CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("receiveType", CouponActivityModel.ReceiveType.AUTO.toString());
            parmaMap.put("page", 0);
            parmaMap.put("pageSize", 10);

            List<Map<String, Object>> couponList = couponActivityModelMapper.getCouponActivityDynamic(parmaMap);

            dataList = couponData(storeId, 0, userId, type, couponList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("PC商城-领券中心 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "pcCoupon");
        }

        return dataList;
    }

    @Override
    public List<Map<String, Object>> mchCoupon(int storeId, String userId, int mchId, int type) throws LaiKeAPIException
    {
        List<Map<String, Object>> dataList = new ArrayList<>();
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("status", CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
            parmaMap.put("mch_id", mchId);
            parmaMap.put("receiveType", CouponActivityModel.ReceiveType.AUTO.toString());
            parmaMap.put("start_time_sort", DataUtils.Sort.DESC.toString());
            //根据商品所属店铺获取活动信息
            List<Map<String, Object>> resultList = couponActivityModelMapper.getCouponActivityDynamic(parmaMap);
            dataList = couponData(storeId, mchId, userId, type, resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("PC端-店铺获取商品可用优惠券活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchCoupon");
        }
        return dataList;
    }

    /**
     * 优惠卷数据处理
     *
     * @param resultList -
     * @author Trick
     * @date 2021/6/17 17:07
     */
    private List<Map<String, Object>> couponData(int storeId, Integer mchId, String userId, int type, List<Map<String, Object>> resultList)
    {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Map<String, Object> map : resultList)
        {
            //优惠卷活动id
            int activityId = Integer.parseInt(map.get("id").toString());
            //优惠卷类型
            int activityType = Integer.parseInt(map.get("activity_type").toString());
            //满减金额
            BigDecimal fullReductionAmt = new BigDecimal(map.get("z_money").toString());
            //领取限制
            int receive = Integer.parseInt(map.get("receive").toString());
            //剩余数量
            int surplusNum = Integer.parseInt(map.get("num").toString());
            //活动结束时间
            Date endDate = (Date) map.get("end_time");
            //活动开始时间
            Date startDate = (Date) map.get("start_time");
            //失效天数
            Integer day = MapUtils.getInteger(map, "day");
            if (endDate != null)
            {
                map.put("start_time", DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMD3));
                map.put("end_time", DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMD3));
            }

            //优惠卷名称
            String name = map.get("name").toString();
            if (mchId != 0)
            {
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(storeId);
                mchModel.setId(mchId);
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModel != null)
                {
                    map.put("coupon_name", String.format("[%s]%s", mchModel.getName(), name));
                }
            }

            //消费限制处理
            String limitStr = I18nUtils.getRawMessage("coupon.limit.unlimited");
            if (fullReductionAmt.floatValue() > 0)
            {
                limitStr = I18nUtils.getRawMessage("coupon.limit.over_amount", new Object[]{fullReductionAmt});
            }
            map.put("limit", limitStr);
            //判断活动是否过期
            if (endDate != null && DateUtil.dateCompare(new Date(), endDate))
            {
                //设置活动已过期
                CouponActivityModel couponActivityModel = new CouponActivityModel();
                couponActivityModel.setId(activityId);
                couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_END);
                int count = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityModel);
                if (count < 1)
                {
                    logger.info("活动已结束,但是状态修改失败 参数" + JSON.toJSONString(couponActivityModel));
                }
            }
            else
            {
                String point = I18nUtils.getRawMessage("coupon.point.soldout");
                int    pointType = 3;
                if (surplusNum > 0)
                {
                    //还又剩余券
                    if (StringUtils.isEmpty(userId))
                    {
                        if (type == 1)
                        {
                            point = I18nUtils.getRawMessage("coupon.point.receive_now");
                        }
                        else
                        {
                            point = I18nUtils.getRawMessage("coupon.point.receive_now");
                        }
                        pointType = 1;
                    }
                    else
                    {
                        //获取当前活动用户领取的优惠卷信息
                        CouponModal couponModal = new CouponModal();
                        couponModal.setUser_id(userId);
                        couponModal.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        couponModal.setHid(activityId);
                        int receivedNum = couponModelMapper.selectCount(couponModal);
                        if (receivedNum > 0)
                        {
                            //如果已经领取 判断是否超过领取数量限制
                            if (receive > receivedNum)
                            {
                                //继续领取
                                if (type == 1)
                                {
                                    point = I18nUtils.getRawMessage("coupon.point.receive_now");
                                }
                                else
                                {
                                    point = I18nUtils.getRawMessage("coupon.point.receive_now");
                                }
                                pointType = 1;
                            }
                            else
                            {
                                //超过领取限制
                                if (type == 1)
                                {
                                    point = I18nUtils.getRawMessage("coupon.point.available");
                                }
                                else
                                {
                                    point = I18nUtils.getRawMessage("coupon.point.use_now");
                                }
                                pointType = 2;
                            }
                        }
                        else
                        {
                            //未领取
                            if (type == 1)
                            {
                                point = I18nUtils.getRawMessage("coupon.point.receive_now");
                            }
                            else
                            {
                                point = I18nUtils.getRawMessage("coupon.point.receive_now");
                            }
                            pointType = 1;
                        }
                    }
                }
                String expirationDate = "领取后%s天失效";
                //过期时间  1不过期  2设置指定过期时间  3设置领取后多久失效
                if (day != null && day != 0)
                {
                    expirationDate = I18nUtils.getRawMessage("coupon.expire.in.days", new Object[]{day});
                }
                else if (StringUtils.isNotEmpty(endDate))
                {
                    expirationDate = DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMD) + " " + I18nUtils.getRawMessage("coupon.point.endtime");
                }
                else
                {
                    expirationDate = I18nUtils.getRawMessage("coupon.expiration.permanent");
                }
                map.put("point", point);
                map.put("point_type", pointType);
                map.put("expirationDate", expirationDate);
            }
            //非会员赠送
            if (!CouponModal.COUPON_TYPE_HYZS.equals(activityType + ""))
            {
                dataList.add(map);
            }
        }
        return dataList;
    }

    @Override
    public Map<String, Object> storeCoupons(CouponParmaVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //优惠卷类型集
            List<String> couponTypeNameList = new ArrayList<>();
            //数据总数量
            int total;
            int count;

            //获取店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(vo.getMchId());
            if (vo.getMchId() != null && vo.getMchId() != 0)
            {
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "storeCoupons");
                }
            }
            //获取优惠卷配置信息
            CouponConfigModel couponConfigModel = new CouponConfigModel();
            couponConfigModel.setStore_id(vo.getStoreId());
            couponConfigModel.setMch_id(mchModel.getId());
            couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);
            if (couponConfigModel != null)
            {
                String couponTypes = couponConfigModel.getCoupon_type();
                if (!StringUtils.isEmpty(couponTypes))
                {
                    String[] couponTypeList = couponTypes.split(SplitUtils.DH);
                    for (String couponType : couponTypeList)
                    {
                        String typeName = CouponDataUtils.getCouponTypeName(Integer.parseInt(couponType));
                        couponTypeNameList.add(typeName);
                    }
                }
            }
            //获取优惠卷活动信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (vo.getMchId() == null || vo.getMchId() == 0)
            {
                parmaMap.put("mchList", new int[]{0, customerModelMapper.getStoreMchId(vo.getStoreId())});
            }
            else
            {
                parmaMap.put("mch_id", vo.getMchId());
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            if (vo.getHid() != null)
            {
                parmaMap.put("id", vo.getHid());
            }
            if (!StringUtils.isEmpty(vo.getName()))
            {
                parmaMap.put("likeName", vo.getName());
            }
            if (vo.getActivityType() != null && vo.getActivityType() != 0)
            {
                parmaMap.put("activity_type", vo.getActivityType());
            }
            parmaMap.put("issueUnit", vo.getIssueUnit());
            parmaMap.put("receiveType", vo.getReceiveType());
            //去除会员制相关
            parmaMap.put("activity_type_4", 4);

            if (vo.getStatus() != null && vo.getStatus() != 0)
            {
                parmaMap.put("status", vo.getStatus());
            }
            if (vo.getIsOverdue() != null)
            {
                if (vo.getIsOverdue() == 0)
                {
                    parmaMap.put("validate_not_overdue", new Date());
                }
                else
                {
                    parmaMap.put("validate_overdue", new Date());
                }
            }

            total = couponActivityModelMapper.countCouponActivityDynamic(parmaMap);
            List<Map<String, Object>> resultData = new ArrayList<>();
            if (total > 0)
            {
                resultData = couponActivityModelMapper.getCouponActivityDynamic(parmaMap);
                for (Map<String, Object> map : resultData)
                {
                    //活动id
                    int id = Integer.parseInt(map.get("id").toString());
                    //优惠卷类型
                    int acType = Integer.parseInt(map.get("activity_type").toString());
                    //发行单位
                    int issueUnit = MapUtils.getInteger(map, "issue_unit");
                    map.put("issueUnit", CouponActivityModel.IssueUnit.getNameByType(issueUnit));
                    int num = MapUtils.getIntValue(map, "num");
                    //优惠卷类型别名
                    String acTypeName = "";
                    //优惠卷使用范围
                    int type = Integer.parseInt(map.get("type").toString());
                    //活动开始时间
                    Date startDate = (Date) map.get("start_time");
                    //活动结束时间
                    Date endDate = (Date) map.get("end_time");
                    //失效天数
                    Integer day = MapUtils.getInteger(map, "day");
                    //范围类型
                    String typeName = "";

                    acTypeName = CouponDataUtils.getCouponTypeName(acType);
                    map.put("activity_type", acTypeName);
                    if (type == 1)
                    {
                        typeName = "全部商品";
                    }
                    else if (type == 2)
                    {
                        typeName = "指定商品";
                        if (vo.getHid() != null)
                        {
                            //分类数据处理
                            List<Map<String, Object>> goodsIdMaps = null;
                            String                    goodsIds    = SerializePhpUtils.getUnserializeByBasic(MapUtils.getString(map, "product_id"), String.class);
                            if (goodsIds != null)
                            {
                                List<String>        goodsIdList = DataUtils.convertToList(goodsIds.split(SplitUtils.DH));
                                Map<String, Object> parmaMap1   = new HashMap<>(16);
                                parmaMap1.put("store_id", vo.getStoreId());
                                parmaMap1.put("goodsIdList", goodsIdList);
                                goodsIdMaps = productListModelMapper.selectDynamic(parmaMap1);
                                goodsIdMaps.forEach(goods ->
                                {
                                    Map<String, Object> goodsInfoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(MapUtils.getString(goods, "initial"), Map.class));
                                    if (goodsInfoMap != null)
                                    {
                                        goods.put("price", new BigDecimal(MapUtils.getString(goodsInfoMap, "sj")));
                                        goods.put("imgurl", publiceService.getImgPath(MapUtils.getString(goods, "imgurl"), vo.getStoreId()));
                                    }
                                });
                            }
                            map.put("goodsIdList", goodsIdMaps);
                        }
                    }
                    else if (type == 3)
                    {
                        typeName = "指定分类";
                        if (vo.getHid() != null)
                        {
                            //分类数据处理
                            List<Map<String, Object>> classIdMaps = null;
                            String                    classIds    = SerializePhpUtils.getUnserializeByBasic(MapUtils.getString(map, "product_class_id"), String.class);
                            if (classIds != null)
                            {
                                List<String>  classIdList = DataUtils.convertToList(classIds.split(SplitUtils.DH));
                                List<Integer> classIdTemp = new ArrayList<>();
                                if (classIdList != null)
                                {
                                    for (String cid : classIdList)
                                    {
                                        classIdTemp.add(Integer.parseInt(cid));
                                    }
                                }
                                Map<String, Object> parmaMap1 = new HashMap<>(16);
                                parmaMap1.put("store_id", vo.getStoreId());
                                parmaMap1.put("classIdList", classIdList);
                                parmaMap1.put("level", 0);
                                parmaMap1.put("sid", 0);
                                parmaMap1.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                                classIdMaps = productClassModelMapper.selectDynamic(parmaMap1);
                                for (Map<String, Object> classMap : classIdMaps)
                                {
                                    int classId = MapUtils.getIntValue(classMap, "cid");
                                    classMap.put("cname", MapUtils.getString(classMap, "pname"));
                                    classMap.put("child", publicGoodsService.getClassLevelLowAll(vo.getStoreId(), classId, classIdTemp));
                                }
                            }
                            map.put("classIdList", classIdMaps);
                        }
                    }
                    else if (type == 4)
                    {
                        typeName = "充值会员";
                    }
                    map.put("type", typeName);
                    map.put("couponType", acType);
                    CouponActivityModel updateCouponActivityModel = new CouponActivityModel();
                    updateCouponActivityModel.setId(id);
                    //是否已经结束
                    boolean isEnd          = false;
                    String  expirationDate = "领取后%s天失效";
                    //过期时间  1不过期  2设置指定过期时间  3设置领取后多久失效
                    int dateType   = 1;
                    int statusTemp = CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN;
                    if (day != null && day != 0)
                    {
                        dateType = 3;
                        expirationDate = String.format(expirationDate, day);

                    }
                    else if (StringUtils.isNotEmpty(endDate))
                    {
                        boolean flag = true;
                        statusTemp = CouponActivityModel.COUPON_ACTIVITY_STATUS_NOT_USE;
                        if (DateUtil.dateCompare(new Date(), endDate))
                        {
                            statusTemp = CouponActivityModel.COUPON_ACTIVITY_STATUS_END;
                            isEnd = true;
                        }
                        else if (DateUtil.dateCompare(startDate, new Date()))
                        {
                            //互动是否已开启
                            statusTemp = CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN;
                        }
                        else
                        {
                            flag = false;
                        }
                        updateCouponActivityModel.setStatus(statusTemp);
                        if (flag)
                        {
                            count = couponActivityModelMapper.updateByPrimaryKeySelective(updateCouponActivityModel);
                            if (count < 1)
                            {
                                logger.info("修改活动状态,但是状态修改失败 参数" + JSON.toJSONString(updateCouponActivityModel));
                            }
                        }
                        dateType = 2;
                        expirationDate = DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMD);
                    }
                    else
                    {
                        expirationDate = I18nUtils.getRawMessage("coupon.limit.unlimited");
                    }
                    map.put("isEnd", isEnd);
                    map.put("status", statusTemp);
                    map.put("expirationDate", expirationDate);
                    //优惠卷是否被使用
                    int delStatus = 2;
                    parmaMap.clear();
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("hid", id);
                    List<Integer> typeList = new ArrayList<>();
                    typeList.add(CouponModal.COUPON_TYPE_NOT_USED);
                    typeList.add(CouponModal.COUPON_TYPE_IN_USE);
                    parmaMap.put("typeList", typeList);
                    count = couponActivityModelMapper.countCouponActivityDynamic(parmaMap);
                    if (count > 0)
                    {
                        //未被使用
                        delStatus = 1;
                    }
                    map.put("del_status", delStatus);

                    if (num >= 999999)
                    {
                        map.put("numStr", "无门槛");
                    }
                    map.put("isEndStr", isEnd ? "是" : "否");

                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                    map.put("start_time", DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMD));
                    map.put("end_time", DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMD));

                    String value = MapUtils.getString(map, "money");
                    if (CouponModal.COUPON_TYPE_ZK.equals(acType + ""))
                    {
                        value = MapUtils.getString(map, "discount");
                    }
                    map.put("value", value);
                    //发行单位 0商城 1自营店
                    map.put("issuer", MapUtils.getIntValue(map, "issue_unit"));
                    //发行数量 1不限制 2设置数量 输入框参数是以前的
                    Integer circulation = MapUtils.getInteger(map, "circulation");
                    map.put("issue_number_type", circulation == null || circulation > 999999 ? 1 : 2);
                    map.put("issue_number_type_str", circulation == null || circulation > 999999 ? "不限制" : circulation);
                    //消费门槛 1无门槛 2设置金额
                    BigDecimal moneySize = new BigDecimal(MapUtils.getString(map, "z_money", BigDecimal.ZERO.toString()));
                    map.put("consumption_threshold_type", BigDecimal.ZERO.compareTo(moneySize) >= 0 ? 1 : 2);
                    map.put("consumption_threshold_type_str", BigDecimal.ZERO.compareTo(moneySize) >= 0 ? "无门槛" : moneySize);
                    //领取方式 0=手动领取 1=系统赠送
                    String receive_type = MapUtils.getString(map, "receive_type");
                    map.put("pickup_type", receive_type);
                    if (receive_type.equals("0"))
                    {
                        map.put("pickupTypeName", "手动领取");
                    }
                    else if (receive_type.equals("1"))
                    {
                        map.put("pickupTypeName", "系统赠送");
                    }
                    else
                    {
                        map.put("pickupTypeName", "位置类型");
                    }
                    map.put("date_type", dateType);
                }
            }
            if (vo.getExportType().equals(1))
            {
                exportCouponData(resultData, vo.getResponse());
                return null;
            }
            resultMap.put("isOpenCoupon", mchModel.getIsOpenCoupon());
            resultMap.put("coupon_type", couponTypeNameList);
            resultMap.put("list", resultData);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺优惠券列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeCoupons");
        }
        return resultMap;
    }

    private void exportCouponData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"id", "优惠券名称", "发行单位", "优惠券类型", "可用范围", "发行数量", "面值/折扣", "消费门槛", "领取方式", "过期时间", "是否过期", "发行时间"};
            //对应字段
            String[] kayList = new String[]{"id", "name", "issueUnit", "activity_type", "type", "issue_number_type_str", "value", "consumption_threshold_type_str", "pickupTypeName", "expirationDate", "isEndStr", "add_time"};
            EasyPoiExcelUtil.excelExport("商品列表", headerList, kayList, goodsList, response);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    @Override
    public Map<String, Object> addStoreCouponsPage(int storeId, String userId, int mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //优惠卷类型集
            List<Map<String, Object>> couponTypeList = new ArrayList<>();

            //获取店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(storeId);
            mchModel.setId(mchId);
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                //获取全局优惠卷配置信息
                CouponConfigModel couponConfigModel = new CouponConfigModel();
                couponConfigModel.setStore_id(storeId);
                couponConfigModel.setMch_id(0);
                couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);
                if (couponConfigModel != null && !StringUtils.isEmpty(couponConfigModel.getCoupon_type()))
                {
                    String[] couponTypes = couponConfigModel.getCoupon_type().split(SplitUtils.DH);
                    for (String coupon : couponTypes)
                    {
                        Map<String, Object> couponTypeMap = new HashMap<>(16);
                        String              name;
                        if (CouponModal.COUPON_TYPE_MJ.equals(coupon))
                        {
                            name = "满减券";
                        }
                        else if (CouponModal.COUPON_TYPE_ZK.equals(coupon))
                        {
                            name = "折扣券";
                        }
                        else
                        {
                            continue;
                        }
                        couponTypeMap.put("value", coupon);
                        couponTypeMap.put("name", name);
                        couponTypeList.add(couponTypeMap);
                    }
                }
                //限领类型
                int limitType = CouponConfigModel.LIMIT_TYPE_SINGLE;
                if (couponConfigModel != null)
                {
                    limitType = couponConfigModel.getLimit_type();
                }

                resultMap.put("coupon_type_list", couponTypeList);
                resultMap.put("limit_type", limitType);
                List<Map<String, Object>> list = new ArrayList<>();
                CouponConfigModel.CouponTypeMap.forEach((key, value) -> list.add(ImmutableMap.of("key", key.toString(), "value", value)));
                resultMap.put("typeList", list);
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "addStoreCouponsPage");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加店铺优惠卷页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStoreCouponsPage");
        }
    }

    @Override
    public boolean addStoreCoupons(AddCouponActivityVo vo, String userId) throws LaiKeAPIException
    {
        try
        {
            CouponActivityModel oldCouponActivityModel = null;
            //剩余数量
            int num = vo.getCirculation();
            //是否是修改标识
            boolean isUpdate = false;
            if (vo.getId() != null)
            {
                isUpdate = true;
                oldCouponActivityModel = new CouponActivityModel();
                oldCouponActivityModel.setId(vo.getId());
                oldCouponActivityModel = couponActivityModelMapper.selectOne(oldCouponActivityModel);
                if (oldCouponActivityModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHHDBCZ, "优惠活动不存在", "addStoreCoupons");
                }
                //编辑时 数量必须大于已领取数量
                CouponModal couponModal = new CouponModal();
                couponModal.setStore_id(vo.getStoreId());
                couponModal.setHid(oldCouponActivityModel.getId());
                int receiveNum = couponModelMapper.selectCount(couponModal);
                if (receiveNum >= vo.getCirculation())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQFXSLBXDYYLQSL, "优惠券发行数量必须大于已领取数量");
                }
                //发行数量-领取数量
                num -= receiveNum;
                if (num < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQFXSLBXDYYLQSL, "优惠券发行数量必须大于已领取数量");
                }
            }

            if (vo.getActivityType() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZYHJ, "请选择优惠卷", "addStoreCoupons");
            }
            if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJBNWK, "优惠卷不能为空", "addStoreCoupons");
            }
            //4141 【JAVA开发环境】卡券：发布优惠券时这个图片在用户端没有看到显示的地方，如果没有显示的地方就去掉这个图片上传
//            if (StringUtils.isEmpty(vo.getCover_map()))
//            {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBNWK, "图片不能为空", "addStoreCoupons");
//            }
            BigDecimal discount = null;
            BigDecimal money    = null;
            int        count;
            //装载优惠卷数据
            CouponActivityModel couponActivityModel = new CouponActivityModel();
            couponActivityModel.setStore_id(vo.getStoreId());
            couponActivityModel.setName(vo.getName());
            couponActivityModel.setCover_map(vo.getCover_map());
            couponActivityModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            //自营店
            Integer mainMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (CouponActivityModel.IssueUnit.UNIT_STORE_MCH.getKey().equals(vo.getIssueUnit()))
            {
                couponActivityModel.setMch_id(mainMchId);
            }
            else if (CouponActivityModel.IssueUnit.UNIT_MCH.getKey().equals(vo.getIssueUnit()))
            {
                //获取店铺信息
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(vo.getStoreId());
                if (!StringUtils.isEmpty(userId))
                {
                    mchModel.setUser_id(userId);
                }
                mchModel.setId(vo.getMchId());
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModelMapper.selectCount(mchModel) < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "addStoreCoupons");
                }
                couponActivityModel.setMch_id(vo.getMchId());
                //如果当前人是自营店则发行单位是自营店
                if (mainMchId.equals(vo.getMchId()))
                {
                    vo.setIssueUnit(CouponActivityModel.IssueUnit.UNIT_STORE_MCH.getKey());
                }
            }
            else
            {
                couponActivityModel.setMch_id(0);
            }
            //优惠卷名称重复校验
            if (oldCouponActivityModel == null || !vo.getName().equals(oldCouponActivityModel.getName()))
            {
                count = couponActivityModelMapper.selectCount(couponActivityModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQMCYCZ, "优惠券名称已存在", "addStoreCoupons");
                }
            }
            couponActivityModel.setGrade_id(0);
            couponActivityModel.setNum(num);
            if (vo.getDay() == null)
            {
                //这是领取后多少天后过期
                couponActivityModel.setDay(vo.getDay());
            }
            else
            {
                //过期时间
                couponActivityModel.setStart_time(new Date());
                couponActivityModel.setEnd_time(DateUtil.dateFormateToDate(vo.getEndTime(), GloabConst.TimePattern.YMDHMS));
            }

            //优惠卷类型 校验流程
            switch (vo.getActivityType() + "")
            {
                case CouponModal.COUPON_TYPE_MJ:
                    if (vo.getMoney() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJMZBNWK, "优惠卷面值不能为空", "addStoreCoupons");
                    }
                    //验证使用门槛
                    if (vo.getZmoney() != null)
                    {
                        //门槛必须大于面值
                        if (vo.getMoney().compareTo(vo.getZmoney()) > 0 && vo.getZmoney().compareTo(BigDecimal.ZERO) > 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJBXDYMZ, "门槛必须大于等于面值");
                        }
                    }
                    //面值不能为负数
                    if (vo.getMoney().compareTo(BigDecimal.ZERO) <= 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZQDMZ, "请输入正确的面值", "addStoreCoupons");
                    }
                    couponActivityModel.setCirculation(vo.getCirculation());
                    couponActivityModel.setZ_money(vo.getZmoney());
                    discount = BigDecimal.ZERO;
                    money = vo.getMoney();
                    break;
                case CouponModal.COUPON_TYPE_ZK:
                    //验证折扣值
                    if (vo.getDiscount() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZKZBNWK, "折扣值不能为空", "addStoreCoupons");
                    }
                    //面值不能为负数
                    if (vo.getDiscount().compareTo(BigDecimal.ZERO) <= 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZQDZKZ, "请输入正确的折扣值", "addStoreCoupons");
                    }
                    couponActivityModel.setCirculation(vo.getCirculation());
                    couponActivityModel.setZ_money(vo.getZmoney());
                    discount = vo.getDiscount();
                    money = BigDecimal.ZERO;
                    break;
                case CouponModal.COUPON_TYPE_MY:
                    //免邮卷
                    couponActivityModel.setCirculation(vo.getCirculation());
                    //免邮券门槛
                    couponActivityModel.setZ_money(vo.getZmoney());
                    break;
            }
            //优惠卷使用范围 校验流程
            if (CouponActivityModel.USE_RANG_TYPE_PRODUCT == vo.getType())
            {
                if (StringUtils.isEmpty(vo.getMenuList()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSP, "请选择商品", "addStoreCoupons");
                }
                //验证商品
                StringBuilder goodsIdListStr = new StringBuilder();
                String[]      goodsNames     = vo.getMenuList().split(SplitUtils.DH);
                for (String goodsName : goodsNames)
                {
                    ProductListModel productListModel = new ProductListModel();
                    //以前的逻辑是根据商品名称，现在改成id
                    if (StringUtils.isInteger(goodsName))
                    {
                        productListModel.setId(Integer.valueOf(goodsName));
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCWQSYSP, "参数错误,请使用商品id");
                    }
                    productListModel = productListModelMapper.selectByPrimaryKey(productListModel);
                    if (productListModel != null)
                    {
                        goodsIdListStr.append(productListModel.getId()).append(SplitUtils.DH);
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBZYSPBCZ, "列表中有商品不存在", "addStoreCoupons");
                    }
                }
                String goodsIds = StringUtils.trim(goodsIdListStr.toString(), SplitUtils.DH);
                couponActivityModel.setProduct_id(goodsIds);
            }
            else if (CouponActivityModel.USE_RANG_TYPE_CLASS == vo.getType())
            {
                if (StringUtils.isEmpty(vo.getClassList()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZFL, "请选择分类", "addStoreCoupons");
                }
                couponActivityModel.setProduct_class_id(vo.getClassList());
            }
            couponActivityModel.setInstructions(vo.getInstructions());
            couponActivityModel.setActivity_type(vo.getActivityType());
            couponActivityModel.setType(vo.getType());
            //领取限制
            if (vo.getLimitCount() == null || vo.getLimitCount() < 1)
            {
                vo.setLimitCount(9999999);
            }
            couponActivityModel.setReceive(vo.getLimitCount());
            //默认没有过期时间
            couponActivityModel.setDay(0);
            couponActivityModel.setStart_time(new Date());
            couponActivityModel.setEnd_time(DateUtil.getAddMonth(new Date(), 12 * 10));
            couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
            if (vo.getDay() != null)
            {
                //设置了领取n天后过期
                couponActivityModel.setDay(vo.getDay());
                couponActivityModel.setStart_time(null);
                couponActivityModel.setEnd_time(null);
            }
            else if (StringUtils.isNotEmpty(vo.getEndTime()))
            {
                couponActivityModel.setStart_time(new Date());
                Date endDate = DateUtil.dateFormateToDate(vo.getEndTime(), GloabConst.TimePattern.YMD);
                couponActivityModel.setEnd_time(DateUtil.dateFormateToDate(DateUtil.getStartOfDay(endDate), GloabConst.TimePattern.YMDHMS));
                //过期处理
                //结束时间不会小于当前时间
//                if (!DateUtil.dateCompare(new Date(), couponActivityModel.getEnd_time())) {
//                    couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_END);
//                    logger.info("添加/编辑优惠卷活动时 【活动已结束】 优惠卷名称:{}", vo.getName());
//                } else
            }
            else
            {
                couponActivityModel.setStart_time(null);
                couponActivityModel.setEnd_time(null);
            }
            couponActivityModel.setIssue_unit(vo.getIssueUnit());
            couponActivityModel.setReceive_type(vo.getReceiveType());

            //校验数据
            couponActivityModel.setDiscount(discount);
            couponActivityModel.setMoney(money);
            couponActivityModel = DataCheckTool.checkCouponActivityData(couponActivityModel);
            //添加/修改数据
            if (isUpdate)
            {
                couponActivityModel.setId(vo.getId());
                count = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityModel);
                //增加日志
                if (vo.getStoreType() == 8 || vo.getStoreType() == 7)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "修改了优惠券ID：" + vo.getId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
            }
            else
            {
                couponActivityModel.setSkip_type(CouponActivityModel.SKIP_TYPE.HOME);
                couponActivityModel.setAdd_time(new Date());
                count = couponActivityModelMapper.insertSelective(couponActivityModel);
                if (vo.getStoreType() == 8 || vo.getStoreType() == 7)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "增加了优惠券ID：" + couponActivityModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
                }
            }
            if (count < 1)
            {
                logger.info("添加/编辑优惠卷 失败 参数:" + JSON.toJSONString(couponActivityModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障", "addStoreCoupons");
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStoreCoupons");
        }
    }


    @Override
    public Map<String, Object> getCouponsInfoById(int storeId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            //指定商品集
            String goodsNames = null;
            //指定商品类别集
            String goodsClassNames = null;

            //优惠卷类型集[{value:"2",name:"优惠卷类型"}...]
            List<Map<String, String>> couponTypeNameList = new ArrayList<>();
            //获取优惠卷信息
            CouponActivityModel couponActivityModel = new CouponActivityModel();
            couponActivityModel.setId(id);
            couponActivityModel = couponActivityModelMapper.selectOne(couponActivityModel);
            if (couponActivityModel != null)
            {
                //指定商品处理
                if (!StringUtils.isEmpty(couponActivityModel.getProduct_id()))
                {
                    String goodsIds = SerializePhpUtils.getUnserializeByBasic(couponActivityModel.getProduct_id(), String.class);
                    if (goodsIds != null)
                    {
                        String[]      goodsIdList   = goodsIds.split(SplitUtils.DH);
                        StringBuilder goodsNameStrs = new StringBuilder();
                        for (String goodsId : goodsIdList)
                        {
                            ProductListModel productListModel = new ProductListModel();
                            productListModel.setId(Integer.parseInt(goodsId));
                            productListModel = productListModelMapper.selectOne(productListModel);
                            if (productListModel != null)
                            {
                                goodsNameStrs.append(productListModel.getProduct_title()).append(SplitUtils.DH);
                            }
                        }
                        goodsNames = StringUtils.trim(goodsNameStrs.toString(), SplitUtils.DH);
                        couponActivityModel.setProduct_id(StringUtils.stringImplode(Arrays.asList(goodsIdList), SplitUtils.DH));
                    }
                }
                //指定商品类别处理
                if (!StringUtils.isEmpty(couponActivityModel.getProduct_class_id()))
                {
                    String goodsClassIds = SerializePhpUtils.getUnserializeByBasic(couponActivityModel.getProduct_class_id(), String.class);
                    assert goodsClassIds != null;
                    String[]      goodsClassIdList = goodsClassIds.split(SplitUtils.DH);
                    StringBuilder goodsNameStrs    = new StringBuilder();
                    for (String goodsClassId : goodsClassIdList)
                    {
                        ProductClassModel productClassModel = new ProductClassModel();
                        productClassModel.setCid(Integer.parseInt(goodsClassId));
                        productClassModel = productClassModelMapper.selectOne(productClassModel);
                        if (productClassModel != null)
                        {
                            goodsNameStrs.append(productClassModel.getPname()).append(SplitUtils.DH);
                        }
                    }
                    goodsClassNames = StringUtils.trim(goodsNameStrs.toString(), SplitUtils.DH);
                }
                //获取全局优惠卷信息
                CouponConfigModel couponConfigModel = new CouponConfigModel();
                couponConfigModel.setStore_id(storeId);
                couponConfigModel.setMch_id(0);
                couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);
                if (couponConfigModel != null)
                {
                    String couponTypes = couponConfigModel.getCoupon_type();
                    if (!StringUtils.isEmpty(couponTypes))
                    {
                        String[] couponTypeList = couponTypes.split(SplitUtils.DH);
                        for (String couponType : couponTypeList)
                        {
                            String typeName;
                            if ("2".equals(couponType))
                            {
                                typeName = "满减券";
                            }
                            else if ("3".equals(couponType))
                            {
                                typeName = "折扣券";
                            }
                            else
                            {
                                continue;
                            }
                            Map<String, String> couponTypeMap = new HashMap<>(16);
                            couponTypeMap.put("value", couponType);
                            couponTypeMap.put("name", typeName);
                            couponTypeNameList.add(couponTypeMap);
                        }
                    }
                }
                resultMap = JSON.parseObject(JSON.toJSONString(couponActivityModel), new TypeReference<Map<String, Object>>()
                {
                });
                resultMap.put("limitCount", couponActivityModel.getReceive());
                resultMap.put("start_time", DateUtil.dateFormate(couponActivityModel.getStart_time(), GloabConst.TimePattern.YMDHMS));
//                resultMap.put("end_time", DateUtil.dateFormate(couponActivityModel.getEnd_time(), GloabConst.TimePattern.YMDHMS));
                //4143 【JAVA开发环境】卡券：移动店铺端--优惠券过期后，点击查看按钮过期时间显示有误
                resultMap.put("end_time", DateUtil.dateFormate(couponActivityModel.getEnd_time(), GloabConst.TimePattern.YMD));
                resultMap.put("coupon_type", couponTypeNameList);
                resultMap.put("product_name", goodsNames);
                resultMap.put("product_class_name1", goodsClassNames);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJBCZ, "优惠卷不存在", "getCouponsInfoById");
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取优惠卷信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCouponsInfoById");
        }
    }

    @Override
    public Map<String, Object> seeCoupon(int storeId, int id, Integer mchId, Integer status, String sNo, String name, String pageTo, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            return this.seeCoupon(storeId, id, mchId, null, status, sNo, name, null, null, pageModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺查看优惠卷领取信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "seeCoupon");
        }
    }

    @Override
    public Map<String, Object> seeCoupon(int storeId, int id, Integer mchId, Integer status, Integer type, String sNo, String name, String keyWord, String pageTo, PageModel pageModel) throws LaiKeAPIException
    {
        try
        {
            SeeCouponVo seeCouponVo = new SeeCouponVo();
            seeCouponVo.setStoreId(storeId);
            seeCouponVo.setPageModel(pageModel);
            seeCouponVo.setId(id);
            seeCouponVo.setStatus(status);
            seeCouponVo.setType(type);
            seeCouponVo.setKeyWord(keyWord);
            seeCouponVo.setPageTo(pageTo);
            seeCouponVo.setsNo(sNo);
            seeCouponVo.setIsFree(null);
            return this.seeCoupon(seeCouponVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺查看优惠卷领取信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "seeCoupon");
        }
    }

    @Override
    public Map<String, Object> seeCoupon(SeeCouponVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap1 = new HashMap<>(16);
            parmaMap1.put("store_id", vo.getStoreId());
            parmaMap1.put("status", CouponModal.COUPON_STAUS_OPEN);
            parmaMap1.put("hid", vo.getId());
            parmaMap1.put("mch_id", vo.getMchId());
            parmaMap1.put("add_time_sort", DataUtils.Sort.DESC.toString());
            //参数列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.putAll(parmaMap1);
            if (!StringUtils.isEmpty(vo.getsNo()))
            {
                parmaMap.put("likeOrderno", vo.getsNo());
            }
            if (!StringUtils.isEmpty(vo.getName()))
            {
                parmaMap.put("user_name", vo.getName());
            }
            if (!StringUtils.isEmpty(vo.getKeyWord()))
            {
                parmaMap.put("keyWord", vo.getKeyWord());
            }
            if (vo.getStatus() != null)
            {
                parmaMap.put("couponType", vo.getStatus());
            }
            if (vo.isGroupByUserId())
            {
                parmaMap.put("group_user_id", "group_user_id");
            }
            //管理后台可以查看删除的记录
            if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_ADMIN)
            {
                parmaMap.put("recycle", "recycle");
            }
            parmaMap.put("activityType", vo.getType());
            parmaMap.put("free_or_not", vo.getIsFree());
            int NumberOfRecipients = couponModelMapper.countCouponActivityDynamicByUserId(parmaMap);
            parmaMap.put("page", vo.getPageModel().getPageNo());
            parmaMap.put("pageSize", vo.getPageModel().getPageSize());
            int                       total          = couponModelMapper.countUserCouponActivityDynamic(parmaMap);
            List<Map<String, Object>> couponInfoList = new ArrayList<>();
            if (total > 0)
            {
                couponInfoList = couponModelMapper.getUserCouponActivityDynamic(parmaMap);
                for (Map<String, Object> map : couponInfoList)
                {
                    //优惠卷id
                    int couponId = Integer.parseInt(map.get("id").toString());
                    //优惠卷活动id
                    int hid = Integer.parseInt(map.get("hid").toString());
                    //用户id
                    String userid = MapUtils.getString(map, "user_id");
                    //活动名称
                    String hName = MapUtils.getString(map, "name");

                    //获取关联订单
                    List<String>     orderList        = new ArrayList<>();
                    CouponOrderModal couponOrderModal = new CouponOrderModal();
                    couponOrderModal.setStore_id(vo.getStoreId());
                    couponOrderModal.setCoupon_id(couponId);
                    couponOrderModal.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    List<CouponOrderModal> couponOrderModalList = couponOrderModalMapper.select(couponOrderModal);
                    for (CouponOrderModal couponOrder : couponOrderModalList)
                    {
                        orderList.add(couponOrder.getsNo());
                    }
                    map.put("orderList", orderList);
                    map.put("typeName", CouponDataUtils.getCouponTypeName(MapUtils.getIntValue(map, "activity_type")));
                    //到期时间
                    Date expiryTime = (Date) map.get("expiry_time");
                    map.put("expiry_time", DateUtil.dateFormate(expiryTime, GloabConst.TimePattern.YMDHMS));
                    //优惠卷是否过期
                    if (expiryTime != null && DateUtil.dateCompare(new Date(), expiryTime))
                    {
                        CouponModal couponModal = new CouponModal();
                        couponModal.setId(couponId);
                        couponModal.setType(CouponModal.COUPON_TYPE_EXPIRED);
                        int count = couponModelMapper.updateByPrimaryKeySelective(couponModal);
                        if (count < 1)
                        {
                            logger.error("优惠卷已过期,但是修改状态失败 参数" + JSON.toJSONString(couponModal));
                        }
                    }
                    if (StringUtils.isEmpty(hName))
                    {
                        //没有活动名称则用公司名称
                        ConfigModel configModel = new ConfigModel();
                        configModel.setStore_id(vo.getStoreId());
                        configModel = configModelMapper.selectOne(configModel);
                        if (configModel != null)
                        {
                            map.put("name", configModel.getCompany());
                        }
                    }
                    //获取当前用户领取优惠卷得数量
                    CouponModal couponModal = new CouponModal();
                    couponModal.setStore_id(vo.getStoreId());
                    couponModal.setHid(hid);
                    couponModal.setUser_id(userid);
                    int receiveNum = couponModelMapper.selectCount(couponModal);
                    map.put("receive", receiveNum);
                    map.put("add_time", DateUtil.dateFormate(map.get("add_time") + "", GloabConst.TimePattern.YMDHMS));
                }
            }


            resultMap.put("list", couponInfoList);
            resultMap.put("total", total);
            //手动领取的人数
            resultMap.put("manNum", couponOrderModalMapper.adminCountUserNum(vo.getId()));
            //自动发放的人数
            int autoManNum;
            resultMap.put("autoManNum", autoManNum = couponModalMapper.countPersonNum(vo.getStoreId(), vo.getId(), DictionaryConst.WhetherMaven.WHETHER_OK));
            if (vo.getIsFree() != null)
            {
                if (DictionaryConst.WhetherMaven.WHETHER_OK != vo.getIsFree())
                {
                    autoManNum = couponModalMapper.countPersonNum(vo.getStoreId(), vo.getId(), DictionaryConst.WhetherMaven.WHETHER_NO);
                }
                resultMap.put("person", autoManNum);
            }
            //总领取人数
            resultMap.put("Number_of_recipients", couponModalMapper.adminCountAllPersonNum(vo.getStoreId(), vo.getId()));
            //resultMap.put("Number_of_recipients", NumberOfRecipients);
            //pc平台领取记录中的领取总数，随搜索变化
            resultMap.put("NumberOfRecipients", NumberOfRecipients);
            //总领取数量
            //resultMap.put("Number_of_receive", couponModalMapper.adminCountAllReceiveNum(vo.getStoreId(), vo.getId()));
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺查看优惠卷领取信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "seeCoupon");
        }
    }

    @Override
    public void delBatchMyCoupon(int storeId, List<String> ids, String userId) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("ids", ids);
            parmaMap.put("user_id", userId);
            parmaMap.put("store_id", storeId);
            int count = couponModelMapper.countUserCouponActivityDynamic(parmaMap);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "列表有活动不存在");
            }
            List<Map<String, Object>> couponList = couponModelMapper.getUserCouponActivityDynamic(parmaMap);

            for (Map<String, Object> map : couponList)
            {
                int     couponId = MapUtils.getIntValue(map, "id");
                int     type     = MapUtils.getInteger(map, "type");
                Integer status   = MapUtils.getInteger(map, "status");
                if (CouponModal.COUPON_TYPE_IN_USE.equals(type))
                {
                    logger.debug(String.format("用户优惠卷id:%s 删除失败,正在被订单使用中", couponId));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "优惠券使用中无法删除");
                }
                if (status != null && status.equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID))
                {
                    logger.debug(String.format("用户优惠卷id:%s 删除失败,对应订单未支付", couponId));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJSYZYDDWZFWFSC, "优惠劵使用中，有订单未支付无法删除记录");
                }
                CouponModal couponModalDel = new CouponModal();
                couponModalDel.setId(couponId);
                couponModalDel.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                int row = couponModelMapper.updateByPrimaryKeySelective(couponModalDel);
                if (row < 1)
                {
                    logger.debug("删除用户优惠卷失败 id:{}", couponId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障", "delMchCoupon");
                }
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除优惠券 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBatchMyCoupon");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMchCoupon(int storeId, List<String> ids, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("ids", ids);
            if (mchId != null)
            {
                parmaMap.put("mchId", mchId);
            }
            int count = couponActivityModelMapper.countCouponActivityDynamic(parmaMap);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "列表有活动不存在");
            }
            ids.forEach(id ->
            {
                int acId = Integer.parseInt(id);
                //删除优惠卷活动
                CouponActivityModel couponActivityDel = new CouponActivityModel();
                couponActivityDel.setId(acId);
                couponActivityDel.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                int row = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityDel);
                if (row < 1)
                {
                    logger.info("删除店铺活动失败 参数:{}", JSON.toJSONString(couponActivityDel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障", "delMchCoupon");
                }
                //删除未使用的优惠卷 [优化:店铺删除优惠券活动不删除已领取的券]
                /*row = couponModelMapper.delNotUsedCoupon(storeId, acId, mchId);
                logger.info("删除未使用的优惠卷 删除数量:{}", row);
                //获取使用中的优惠卷
                CouponModal couponModal = new CouponModal();
                couponModal.setHid(acId);
                couponModal.setStore_id(storeId);
                couponModal.setType(CouponModal.COUPON_TYPE_IN_USE);
                List<CouponModal> couponModalList = couponModelMapper.select(couponModal);
                for (CouponModal coupon : couponModalList) {
                    //删除未使用的优惠卷 订单使用了,但是未付款则不删除
                    OrderModel orderModel = new OrderModel();
                    orderModel.setStore_id(storeId);
                    orderModel.setCoupon_id(coupon.getId().toString());
                    orderModel.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
                    row = orderModelMapper.selectCount(orderModel);
                    if (row < 1) {
                        CouponModal couponModalDel = new CouponModal();
                        couponModalDel.setId(coupon.getId());
                        couponModalDel.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                        row = couponModelMapper.updateByPrimaryKeySelective(couponModalDel);
                        if (row < 1) {
                            logger.info("删除用户优惠卷失败 参数" + JSON.toJSONString(couponActivityDel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障", "delMchCoupon");
                        }
                    } else {
                        logger.debug(String.format("用户优惠卷id:%s 删除失败,正在被订单使用中", coupon.getId()));
                    }
                }*/
            });
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除店铺活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBatchMchCoupon");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delMchCoupon(int storeId, int id, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            this.delBatchMchCoupon(storeId, Collections.singletonList(String.valueOf(id)), mchId);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除店铺活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delMchCoupon");
        }
    }

    @Override
    public List<Map<String, Object>> mchFenlei(int storeId, Integer mchId) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            //店铺拥有的所有类别id
            Set<Integer> goodsClassIdList = new HashSet<>();

            //获取店铺所有商品信息
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setMch_id(mchId);
            productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            productListModel.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString());
            productListModel.setActive(DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE.toString());
            List<ProductListModel> productListModelList = productListModelMapper.select(productListModel);
            for (ProductListModel productList : productListModelList)
            {
                //商品类别 -3-31-191-
                String[] classIds = StringUtils.trim(productList.getProduct_class(), SplitUtils.HG).split(SplitUtils.HG);
                for (String classId : classIds)
                {
                    goodsClassIdList.add(Integer.parseInt(classId));
                }
            }
            for (int classId : goodsClassIdList)
            {
                //当前类别信息
                Map<String, Object> classMap = new HashMap<>(16);
                //获取平台商品类别 一级目录
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setStore_id(storeId);
                productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                productClassModel.setCid(classId);
                productClassModel.setLevel(0);
                ProductClassModel goodsClassLevel = productClassModelMapper.selectOne(productClassModel);
                if (goodsClassLevel == null)
                {
                    //继续找下级目录 二级目录
                    productClassModel.setLevel(1);
                    goodsClassLevel = productClassModelMapper.selectOne(productClassModel);
                    if (goodsClassLevel == null)
                    {
                        //三级目录
                        productClassModel.setLevel(2);
                        goodsClassLevel = productClassModelMapper.selectOne(productClassModel);
                    }
                }
                else
                {
                    classMap.put("open", true);
                }
                if (goodsClassLevel == null)
                {
                    logger.info("未找到该类别  参数:" + JSON.toJSONString(productClassModel));
                    continue;
                }
                classMap.put("id", classId);
                classMap.put("pId", goodsClassLevel.getSid());
                classMap.put("name", goodsClassLevel.getPname());
                classMap.put("level", goodsClassLevel.getLevel());

                resultList.add(classMap);
            }
            return resultList;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺所有商品分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchFenlei");
        }
    }

    @Override
    public List<Map<String, Object>> mchProduct(MainVo vo, int mchId, String name) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList;
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("shop_id", mchId);
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("keyword", name);
            }
            parmaMap.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            resultList = productListModelMapper.getProductListDynamic(parmaMap);
            for (Map<String, Object> map : resultList)
            {
                String imgUrl = map.get("imgurl").toString();
                imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                map.put("imgurl", imgUrl);
            }
            return resultList;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺商品信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchProduct");
        }
    }

    @Override
    public boolean giveCoupons(int storeId, List<String> userIdList, int hid) throws LaiKeAPIException
    {
        try
        {
            //获取优惠卷活动信息
            CouponActivityModel couponActivityModel = new CouponActivityModel();
            couponActivityModel.setId(hid);
            couponActivityModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_OPEN);
            couponActivityModel = couponActivityModelMapper.selectOne(couponActivityModel);
            if (couponActivityModel != null)
            {
                //优惠卷数量是否充足
                if (userIdList.size() > couponActivityModel.getNum())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHQSYSLBZ, "优惠券剩余数量不足");
                }
                for (String userId : userIdList)
                {
                    //每个优惠券活动赠送给用户，每个用户只能赠送1张，用户在移动端还能领取改优惠券，赠送给用户的优惠券，不占用用户可以领取的数量
                    int sysGiveNum = couponModalMapper.systemGiveUserCouponNum(storeId, couponActivityModel.getId(), userId);
                    if (sysGiveNum > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "赠送失败,列表中该优惠券已经赠送过一次");
                    }

                    //获取用户信息
                    User user = new User();
                    user.setUser_id(userId);
                    user = userBaseMapper.selectOne(user);
                    if (user == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, userId + "用户不存在");
                    }

                    //添加用户优惠卷信息
                    CouponModal couponModal = new CouponModal();
                    couponModal.setStore_id(storeId);
                    couponModal.setUser_id(userId);
                    couponModal.setFree_or_not(CouponModal.COUPON_GIVE);
                    couponModal.setMch_id(couponActivityModel.getMch_id());
                    if (couponActivityModel.getDay() != null && couponActivityModel.getDay() > 0)
                    {
                        //有期限
                        couponActivityModel.setEnd_time(DateUtil.getAddDate(couponActivityModel.getDay()));
                    }
                    couponModal.setExpiry_time(couponActivityModel.getEnd_time());
                    couponModal.setHid(couponActivityModel.getId());
                    couponModal.setAdd_time(new Date());
                    int count = couponModelMapper.insertSelective(couponModal);
                    if (count < 1)
                    {
                        logger.info("优惠卷领取失败 参数:{}", JSON.toJSONString(couponModal));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJLQSB, "优惠卷领取失败");
                    }
                    //再赠送优惠卷记录表里添加一条数据
                    CouponPresentationRecordModel couponPresentationRecordModel = new CouponPresentationRecordModel();
                    couponPresentationRecordModel.setStore_id(storeId);
                    couponPresentationRecordModel.setCoupon_activity_id(hid);
                    couponPresentationRecordModel.setCoupon_id(couponModal.getId());
                    couponPresentationRecordModel.setUser_id(userId);
                    couponPresentationRecordModel.setMobile(user.getMobile() == null ? "" : user.getMobile());
                    couponPresentationRecordModel.setActivity_type(couponActivityModel.getActivity_type());
                    couponPresentationRecordModel.setAdd_date(new Date());
                    count = couponPresentationRecordModelMapper.insertSelective(couponPresentationRecordModel);
                    if (count < 1)
                    {
                        logger.info("优惠卷赠送记录失败 参数:{}", JSON.toJSONString(couponPresentationRecordModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJZSJLSB, "优惠卷赠送记录失败");
                    }
                }
                //修改优惠卷剩余数量
                int count = couponActivityModelMapper.updateCouponByNum(hid, userIdList.size());
                if (count < 1)
                {
                    logger.info("修改优惠卷剩余数量修改失败 id:{}", hid);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJSLKJSB, "优惠卷数量扣减失败");
                }
                return true;
            }
            else
            {
                logger.info("优惠劵活动不存在或者未启动");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJHDYGQ, "优惠劵活动已过期");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("赠送优惠卷 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "giveCoupons");
        }
    }


    /**
     * 领券中心数据处理
     * 【php coupon.mycoupon】
     *
     * @param storeId -
     * @param userId  -
     * @param list    - 优惠卷活动信息
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/7 16:54
     */
    private void couponCenter(int storeId, String userId, List<Map<String, Object>> list) throws LaiKeAPIException
    {
        try
        {
            //优惠券配置缓存
            Map<Integer, Boolean> couponLimitMchMap = new HashMap<>(16);

            //排序规则 未领取、已领取、已抢光
            List<Map<String, Object>> listNotSort  = new ArrayList<>();
            List<Map<String, Object>> listUseSort  = new ArrayList<>();
            List<Map<String, Object>> listNullSort = new ArrayList<>();
            //条件参数列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            for (Map<String, Object> map : list)
            {
                //优惠卷活动id
                int activityId = Integer.parseInt(map.get("id").toString());
                //店铺id
                int mchId = Integer.parseInt(map.get("mch_id").toString());
                //活动名称
                String activityName = map.get("name").toString();
                //满减金额
                BigDecimal fullReductionAmt = new BigDecimal(map.get("z_money").toString());
                //领取限制
                int receive = Integer.parseInt(map.get("receive").toString());
                //剩余数量
                int surplusNum = Integer.parseInt(map.get("num").toString());
                //设置领取多少天后过期
                int day = MapUtils.getIntValue(map, "day");
                //活动结束时间
                Date endDate = (Date) map.get("end_time");
                //活动开始时间
                Date startDate = (Date) map.get("start_time");
                map.put("start_time", DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMD));
                map.put("end_time", DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMD));
                //发行数量 1不限制 2设置数量 输入框参数是以前的
                Integer circulation = MapUtils.getInteger(map, "circulation");
                map.put("issue_number_type", circulation == null || circulation > 999999 ? 1 : 2);
                map.put("issue_number_type_str", circulation == null || circulation > 999999 ? I18nUtils.getRawMessage("coupon.limit.unlimited") : circulation);
                if (mchId > 0)
                {
                    MchModel mchModel = new MchModel();
                    mchModel.setStore_id(storeId);
                    mchModel.setId(mchId);
                    mchModel = mchModelMapper.selectOne(mchModel);
                    if (mchModel != null)
                    {
                        map.put("name", String.format("[%s]%s", mchModel.getName(), activityName));
                    }
                }

                //消费限制处理
                String limitStr = I18nUtils.getRawMessage("coupon.limit.unlimited");
                if (fullReductionAmt.floatValue() > 0)
                {
                    limitStr = I18nUtils.getRawMessage("coupon.limit.over_amount", new Object[]{fullReductionAmt.setScale(2, BigDecimal.ROUND_HALF_UP)});
                }
                if (day > 0)
                {
                    map.put("day", day);
                    map.put("expiry_time", I18nUtils.getRawMessage("coupon.expire.in.days", new Object[]{day}));
                }

                map.put("limit", limitStr);
                //判断活动是否过期
                if (endDate != null && DateUtil.dateCompare(new Date(), endDate))
                {
                    //设置活动已过期
                    CouponActivityModel couponActivityModel = new CouponActivityModel();
                    couponActivityModel.setId(activityId);
                    couponActivityModel.setStatus(CouponActivityModel.COUPON_ACTIVITY_STATUS_END);
                    int count = couponActivityModelMapper.updateByPrimaryKeySelective(couponActivityModel);
                    if (count < 1)
                    {
                        logger.info("活动已结束,但是状态修改失败 参数" + JSON.toJSONString(couponActivityModel));
                    }
                }
                else
                {
                    //用户未领卷
                    String point;
                    int    pointType;
                    if (StringUtils.isEmpty(userId))
                    {
                        point = I18nUtils.getRawMessage("coupon.point.receive_now");
                        pointType = 1;
                    }
                    else
                    {
                        //获取当前活动用户领取的优惠卷信息
                        CouponModal couponModal = new CouponModal();
                        couponModal.setUser_id(userId);
                        couponModal.setFree_or_not(DictionaryConst.WhetherMaven.WHETHER_NO);
                        couponModal.setHid(activityId);
                        int count = couponModelMapper.selectCount(couponModal);
                        if (count > 0 && surplusNum > 0)
                        {
                            //判断是否超过领取数量限制
                            if (receive > count)
                            {
                                point = I18nUtils.getRawMessage("coupon.point.receive_now");
                                pointType = 1;
                            }
                            else
                            {
                                parmaMap.clear();
                                parmaMap.put("store_id", storeId);
                                parmaMap.put("user_id", userId);
                                parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                                parmaMap.put("hid", activityId);
                                List<Integer> typeList = new ArrayList<>();
                                typeList.add(CouponModal.COUPON_TYPE_NOT_USED);
                                typeList.add(CouponModal.COUPON_TYPE_IN_USE);
                                parmaMap.put("typeList", typeList);
                                count = couponModelMapper.countCouponDynamic(parmaMap);
                                if (count > 0)
                                {
                                    point = I18nUtils.getRawMessage("coupon.point.use_now");
                                    pointType = 2;
                                }
                                else
                                {
                                    //领取了且已使用的加上此标识 coupon.point.getanduse
                                    point = I18nUtils.getRawMessage("coupon.point.getanduse");
                                    pointType = 4;
                                }
                            }
                        }
                        else
                        {
                            if (surplusNum > 0)
                            {
                                point = I18nUtils.getRawMessage("coupon.point.receive_now");
                                pointType = 1;
                            }
                            else
                            {
                                point = I18nUtils.getRawMessage("coupon.point.soldout");
                                pointType = 3;
                            }
                        }
                    }
                    map.put("point", point);
                    map.put("point_type", pointType);
                    if (pointType == 1)
                    {
                        //可领取
                        listNotSort.add(map);
                    }
                    else if (pointType == 2)
                    {
                        //已领取
                        listUseSort.add(map);
                    }
                    else
                    {
                        //已抢完
                        listNullSort.add(map);
                    }
                }
            }
            //合并数据
            listNotSort.addAll(listUseSort);
            listNotSort.addAll(listNullSort);
            list.clear();
            list.addAll(listNotSort);
        }
        catch (Exception e)
        {
            logger.error("领券中心数据处理 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "couponCenter");
        }
    }

    /**
     * 返回用户可以使用的会员优惠券
     *
     * @param storeId
     * @param userId
     * @param amount
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public List<Map<String, Object>> getUserCoupon(int storeId, String userId, BigDecimal amount) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultMap = new ArrayList<>();
        try
        {
            List<Map<String, Object>> userCoupon = couponModalMapper.getUserCoupon(storeId, userId);
            userCoupon.stream().forEach(coupon ->
            {
                //优惠券类型 1=免邮 2=满减 3=折扣 4=会员赠送
                Integer activityType = MapUtils.getInteger(coupon, "activity_type");
                if (CouponModal.COUPON_TYPE_MJ.equals(activityType.toString()) || CouponModal.COUPON_TYPE_ZK.equals(activityType.toString()))
                {
                    //满减条件
                    String zMoney = MapUtils.getString(coupon, "z_money");
                    if (new BigDecimal(zMoney).compareTo(amount) <= 0)
                    {
                        resultMap.add(coupon);
                    }
                }
                else
                {
                    resultMap.add(coupon);
                }
            });
            // 添加一条不使用优惠劵
            Map<String, Object> map = new HashMap<>();
            map.put("activity_type", 2);
            map.put("couponType", 4);
            map.put("grade_id", 0);
            map.put("hid", 0);
            map.put("id", 0);
            map.put("money", BigDecimal.ZERO);
            map.put("z_money", BigDecimal.ZERO);
            map.put("name", I18nUtils.getRawMessage("coupon.use.none"));

            map.put("user_id", userId);
            map.put("choose", true);
            resultMap.add(map);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
        }
        return resultMap;
    }

    @Override
    public CouponConfigModel getCouponConfigInfo(int storeId, int mchId) throws LaiKeAPIException
    {
        CouponConfigModel configModel = new CouponConfigModel();
        try
        {
            configModel.setStore_id(storeId);
            configModel.setMch_id(mchId);
            configModel = couponConfigModelMapper.selectOne(configModel);
        }
        catch (Exception e)
        {
            logger.error("获取优惠券配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCouponConfigInfo");
        }
        return configModel;
    }

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private CouponModalMapper couponModalMapper;


    public static void main(String[] args)
    {
        List<String> couponIdList0 = new ArrayList<>();
        String       couponIds     = "889,882,871,0";
        couponIdList0 = Splitter.on(SplitUtils.DH).omitEmptyStrings().trimResults().splitToList(couponIds);

    }
}
