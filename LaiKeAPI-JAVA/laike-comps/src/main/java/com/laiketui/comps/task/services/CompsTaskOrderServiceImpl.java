package com.laiketui.comps.task.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.*;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.api.plugin.PubliceGroupService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.comps.api.task.CompsTaskOrderService;
import com.laiketui.comps.api.task.CompsTaskService;
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
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.coupon.CouponModal;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.mch.MchStoreWriteModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.report.OrderReportModel;
import com.laiketui.domain.vo.dic.DicVo;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单定时任务
 *
 * @author Trick
 * @date 2020/12/14 15:25
 */
@Service
public class CompsTaskOrderServiceImpl implements CompsTaskOrderService
{

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private CompsTaskService taskService;

    @Autowired
    private PubliceGroupService publiceGroupService;

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private PublicIntegralService publicIntegralService;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private PubliceDistributionService publiceDistributionService;

    @Autowired
    private PublicCouponService      publicCouponService;
    @Autowired
    private ReturnOrderModelMapper   returnOrderModelMapper;
    @Autowired
    private MchStoreWriteModelMapper mchStoreWriteModelMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void orderFailure() throws LaiKeAPIException
    {
        List<Integer>       storeIdList = null;
        Map<String, Object> showAdrMap  = null;
        //获取商城所有店铺
        List<Integer>       mchIdList = null;
        Map<String, Object> parmaMap  = null;
        try
        {
            XxlJobHelper.log("清除过期订单 开始执行!");
            if (!redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST))
            {
                XxlJobHelper.log("商城id未缓存,任务停止,等待下次执行....");
                return;
            }
            String json = redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString();
            storeIdList = JSON.parseObject(json, new TypeReference<List<Integer>>()
            {
            });
            DicVo dicVo = new DicVo();
            dicVo.setName("订单类型");
            dicVo.setShowChild(true);
            showAdrMap = publicDictionaryService.getDictionaryByName(dicVo);
            List<DictionaryListModel> showList = DataUtils.cast(showAdrMap.get("value"));

            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("================== 商城id={}-开始执行清空失效订单 ==================", storeId);
                //获取商城所有店铺
                mchIdList = mchModelMapper.getStoreMchIdList(storeId);

                for (Integer mchId : mchIdList)
                {
                    XxlJobHelper.log("============= 店铺id={}-开始执行 ============", mchId);
                    if (showList != null)
                    {
                        for (DictionaryListModel dic : showList)
                        {
                            XxlJobHelper.log("========= 正在处理【{}】插件-清空失效订单 =========", dic.getCtext());
                            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchId, dic.getValue().toUpperCase());
                            if (configMap == null)
                            {
                                XxlJobHelper.log("【{}】插件-未开启/未配置 ", dic.getCtext());
                                continue;
                            }
                            //竞拍订单不失效
                            if (DictionaryConst.OrdersType.ORDERS_HEADER_JP.equals(dic.getValue()))
                            {
                                XxlJobHelper.log("竞拍订单不失效 ", dic.getCtext());
                                continue;
                            }
                            //拼团无待付款订单
                            if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.equals(dic.getValue()))
                            {
                                XxlJobHelper.log("拼团无待付款订单! ", dic.getCtext());
                                continue;
                            }
                            //未付款订单保留时间
                            Integer orderFailureDay = MapUtils.getInteger(configMap, "orderFailureDay");
                            if (orderFailureDay != null && orderFailureDay > 0)
                            {
                                XxlJobHelper.log("商城id{},设置的订单保留时间【{}】秒", storeId, orderFailureDay);
                                parmaMap = new HashMap<>(16);
                                parmaMap.put("store_id", storeId);
                                parmaMap.put("mch_id", mchId);
                                parmaMap.put("orderStauts", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
                                parmaMap.put("orderType", dic.getValue());
                                parmaMap.put("recycle", DictionaryConst.ProductRecycle.RECOVERY);
                                //失效保留时间-秒
                                parmaMap.put("orderFailureTime", orderFailureDay);
                                parmaMap.put("sysDate", new Date());
                                parmaMap.put("supplierPro", "supplierPro");
                              /*  if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(dic.getValue()))
                                {
                                    //只有普通订单有供应商商品
                                    parmaMap.put("GMSupplierPro", "GMSupplierPro");
                                }*/
                                //预售订单只处理订货模式的订单 定金模式的订单尾款结束后就会取消
                                List<Map<String, Object>> resultMap = null;
                                if (DictionaryConst.OrdersType.ORDERS_HEADER_PS.equals(dic.getValue()))
                                {
                                    parmaMap.put("sell_type", PreSellGoodsModel.ORDER_PATTERN);
                                    resultMap = preSellRecordModelMapper.getTimerSellOrder(parmaMap);
                                    //查询所有尾款时间已过的订单塞入需要处理的订单集合中
                                    parmaMap.put("orderFailureTime", null);
                                    parmaMap.put("sysDate", null);
                                    parmaMap.put("sell_type", PreSellGoodsModel.DEPOSIT_PATTERN);
                                    List<Map<String, Object>> patternOrder = preSellRecordModelMapper.getTimerSellOrder(parmaMap);
                                    for (Map<String, Object> map : patternOrder)
                                    {
                                        String balancePayTime = MapUtils.getString(map, "balance_pay_time");
                                        if (StringUtils.isNotEmpty(balancePayTime))
                                        {
                                            Date    date    = DateUtil.dateFormateToDate(balancePayTime, GloabConst.TimePattern.YMD);
                                            Date    now     = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
                                            boolean compare = DateUtil.dateCompare(date, now);
                                            if (date.before(now))
                                            {
                                                resultMap.add(map);
                                            }
                                        }
                                        balancePayTime = null;
                                    }
                                }
                                else
                                {
                                    resultMap = orderModelMapper.getOrdersNumDynamic(parmaMap);
                                }
                                XxlJobHelper.log("商城未支付的过期订单数量:{}", resultMap.size());
                                for (Map<String, Object> map : resultMap)
                                {
                                    int    id      = Integer.parseInt(map.get("id").toString());
                                    String orderNo = map.get("sNo").toString();
                                    //父订单号
                                    String psNo = "";
                                    if (map.containsKey("p_sNo"))
                                    {
                                        psNo = map.get("p_sNo").toString();
                                    }
                                    //订单类型
                                    String oType = MapUtils.getString(map, "otype");
                                    //用户id
                                    String userId = MapUtils.getString(map, "user_id");
                                    //关闭订单
                                    int count = orderModelMapper.updateOrderStatusById(id, new Date());

                                    XxlJobHelper.log("订单{} 订单关闭 执行状态: {}", orderNo, count > 0);
                                    //获取明细信息
                                    parmaMap.clear();
                                    parmaMap.put("store_id", storeId);
                                    parmaMap.put("orderno", orderNo);
                                    parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                                    List<Map<String, Object>> orderDetailsModelList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(parmaMap);

                                    for (Map<String, Object> orderDetailsMap : orderDetailsModelList)
                                    {
                                        //插件商品id
                                        int commentId = MapUtils.getIntValue(orderDetailsMap, "commodityId");
                                        //商品id
                                        int goodsId = MapUtils.getIntValue(orderDetailsMap, "goodsId");
                                        //规格id
                                        int sid = MapUtils.getIntValue(orderDetailsMap, "sid");
                                        //数量
                                        int goodsNum = MapUtils.getIntValue(orderDetailsMap, "num");
                                        //订单商品使用的优惠卷id
                                        String couponId = MapUtils.getString(orderDetailsMap, "detailCouponId");
                                        //删除订单详情信息
                                        OrderDetailsModel updateOrderDetails = new OrderDetailsModel();
                                        updateOrderDetails.setId(MapUtils.getIntValue(orderDetailsMap, "id"));
                                        updateOrderDetails.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                                        updateOrderDetails.setSettlement_type(OrderDetailsModel.SETTLEMENT_TYPE_SETTLED);
                                        count = orderDetailsModelMapper.updateByPrimaryKeySelective(updateOrderDetails);
                                        XxlJobHelper.log("订单明细关闭 执行状态: {}", orderNo, count > 0);
                                        //如果订单商品使用了优惠卷
                                        if (!StringUtils.isEmpty(couponId))
                                        {
                                            if (!("0,0".equals(couponId) || "0".equals(couponId)))
                                            {
                                                // 当订单详情使用了优惠券
                                                // 订单详情使用的优惠券ID字符串 转数组
                                                String[] couponList = couponId.split(SplitUtils.DH);
                                                for (int i = 0; i < couponList.length; i++)
                                                {
                                                    String tmpCouponId = couponList[i];
                                                    if (!"0".equals(tmpCouponId) && null != tmpCouponId)
                                                    {
                                                        // 使用了优惠券
                                                        if (i == 0 || (i == 1 && "".equals(psNo)))
                                                        {
                                                            // 使用了店铺优惠券 或 (使用了平台优惠券 并且 不是跨店铺订单)
                                                            // 根据商城ID、订单号、店铺优惠券ID，查询不是这个订单详情的数据
                                                            List<Map<String, Object>> otherOrders = orderDetailsModelMapper.getOrderDetailsUseTheCoupon(storeId, orderNo, tmpCouponId, id);
                                                            boolean                   flag        = false;
                                                            //所有详单的优惠和金额为0
                                                            boolean allAfterDiscountIsZero = true;
                                                            if (otherOrders != null && otherOrders.size() > 0)
                                                            {
                                                                // 存在(该订单里，还有其它详情使用了这张店铺优惠券)
                                                                // 该订单里，有多少详情使用了这张店铺优惠券
                                                                int size = otherOrders.size();
                                                                // 该订单里，使用了这张店铺优惠券,并退款或退货退款成功的数量
                                                                int returnNum = 0;
                                                                for (Map<String, Object> otherOrderDetail : otherOrders)
                                                                {
                                                                    int orderStatus = MapUtils.getIntValue(otherOrderDetail, "r_status");
                                                                    if (orderStatus == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)
                                                                    {
                                                                        returnNum++;
                                                                    }
                                                                    //优惠后金额不为0
                                                                    if (BigDecimal.ZERO.compareTo(new BigDecimal(MapUtils.getString(otherOrderDetail, "after_discount"))) != 0)
                                                                    {
                                                                        allAfterDiscountIsZero = false;
                                                                    }
                                                                }
                                                                if (returnNum == size)
                                                                {
                                                                    flag = true;
                                                                }
                                                            }
                                                            else
                                                            {
                                                                flag = true;
                                                            }
                                                            if (flag)
                                                            {
                                                                // 该订单，使用了这张店铺优惠券的订单商品都退款或退款退款成功
                                                                int row = publicCouponService.couponWithOrder(storeId, userId, tmpCouponId, orderNo, "update");
                                                                if (row == 0)
                                                                {
                                                                    //回滚删除已经创建的订单
                                                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                                                                }
                                                                // =2
                                                                row = publicCouponService.updateCoupons(storeId, userId, tmpCouponId, CouponModal.COUPON_TYPE_NOT_USED);
                                                                if (row == 0)
                                                                {
                                                                    //回滚删除已经创建的订单
                                                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZTKSB, "网络故障,退款失败", "refund");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        //积分商品特殊处理
                                        if (!oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_IN))
                                        {
                                            //还原商品库存
                                            count = productListModelMapper.addGoodsStockNum(goodsId, goodsNum);
                                            XxlJobHelper.log("还原商品id:{} 库存总数量:{} 执行状态: {}", orderNo, goodsId, goodsNum, count > 0);
                                            //还原商品规格库存数量
                                            count = confiGureModelMapper.addGoodsAttrStockNumByPid(goodsNum, sid);
                                            XxlJobHelper.log("还原商品id:{} 规格库存id:{} 数量:{} 执行状态: {}", orderNo, goodsId, sid, goodsNum, count > 0);
                                            //回滚插件库存
                                            // TODO: 2021/11/10 Trick 以后有空要写出公共的
                                            switch (oType)
                                            {
                                                case DictionaryConst.OrdersType.ORDERS_HEADER_PS:
                                                    preSellGoodsMapper.addGoodNum(goodsNum, goodsId);
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                        else
                                        {
                                            integralGoodsModelMapper.releaseStockNum(orderNo, commentId);
                                        }
                                        //虚拟商品需要预约的商品退还预约次数
                                        if (oType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                                        {
                                            if (orderDetailsMap.containsKey("write_time") && orderDetailsMap.containsKey("write_time_id"))
                                            {
                                                //需要预约的商品
                                                //预约的时间   2024-07-10 05:00-06:00
                                                String   writeTime = DataUtils.getStringVal(orderDetailsMap, "write_time");
                                                String[] s         = writeTime.split(" ", 2);
                                                writeTime = s[0];
                                                Integer            writeTimeId        = DataUtils.getIntegerVal(orderDetailsMap, "write_time_id");
                                                MchStoreWriteModel mchStoreWriteModel = mchStoreWriteModelMapper.selectByPrimaryKey(writeTimeId);
                                                String             off_num            = mchStoreWriteModel.getOff_num();
                                                Integer            write_off_num      = mchStoreWriteModel.getWrite_off_num();
                                                // 格式化时间
                                                String start_time_ymd = DateUtil.dateFormate(mchStoreWriteModel.getStart_time(), GloabConst.TimePattern.YMD);
                                                String end_time_ymd   = DateUtil.dateFormate(mchStoreWriteModel.getEnd_time(), GloabConst.TimePattern.YMD);
                                                // 获取日期范围内的所有日期
                                                List<String> intervalDate = DateUtil.getIntervalDate(start_time_ymd, end_time_ymd);
                                                for (int i = 0; i < intervalDate.size(); i++)
                                                {
                                                    if (intervalDate.get(i).equals(writeTime))
                                                    {
                                                        String[] split = off_num.split(SplitUtils.DH);
                                                        //如果为无限预约次数，则无需回滚
                                                        if (write_off_num != null && write_off_num == 0)
                                                        {
                                                            break;
                                                        }
                                                        //将对应的已预约次数减掉1
                                                        split[i] = String.valueOf(Integer.parseInt(split[i]) - 1);
                                                        StringBuilder sb = new StringBuilder();
                                                        for (int j = 0; j < split.length; j++)
                                                        {
                                                            sb.append(split[j]);
                                                            if (j < split.length - 1)
                                                            {
                                                                sb.append(","); // 只在不是最后一个元素时添加逗号
                                                            }
                                                        }
                                                        String modified = sb.toString();
                                                        mchStoreWriteModel.setOff_num(modified);
                                                    }
                                                }
                                                mchStoreWriteModelMapper.updateByPrimaryKey(mchStoreWriteModel);
                                            }

                                        }

                                    }

                                    orderNo = null;
                                    oType = null;

                                }
                            }
                            else
                            {
                                XxlJobHelper.log("店铺未配置未付款订单保留天数 店铺id:{} ", mchId);
                            }
                        }
                    }
                }
            }
            XxlJobHelper.log("清除过期订单 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("清除过期订单 异常: ", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
            showAdrMap = null;
            //获取商城所有店铺
            mchIdList = null;
            parmaMap = null;
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void ptOrderTask() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("拼团任务 开始执行!");
            if (!redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST))
            {
                XxlJobHelper.log("商城id未缓存,任务停止,等待下次执行....");
                return;
            }
            String json = redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString();
            storeIdList = JSON.parseObject(json, new TypeReference<List<Integer>>()
            {
            });
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("店铺{} 正在执行!", storeId);
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("storeId", storeId);
                XxlJobHelper.log(httpApiUtils.executeHttpApi("plugin.group.task.execute", parmaMap).toString());
                XxlJobHelper.log("店铺{} 执行完毕!", storeId);
                parmaMap = null;
            }
            storeIdList = null;
            XxlJobHelper.log("拼团任务 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("拼团任务 异常: ", e.getMessage());
            XxlJobHelper.handleFail(e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receivingGoods() throws LaiKeAPIException
    {
        List<Integer>             storeIds  = null;
        List<DictionaryListModel> showList  = null;
        List<Integer>             mchIdList = null;
        List<Map<String, Object>> orderList = null;
        try
        {
            XxlJobHelper.log("自动收货 开始执行!");
            //执行成功的订单号集
            Set<String> taskOrderno = new HashSet<>();
            //获取所有商城id
            storeIds = taskService.getStoreIdAll();
            DicVo dicVo = new DicVo();
            dicVo.setName("订单类型");
            dicVo.setShowChild(true);
            Map<String, Object> showAdrMap = publicDictionaryService.getDictionaryByName(dicVo);
            showList = DataUtils.cast(showAdrMap.get("value"));
            for (Integer storeId : storeIds)
            {
                XxlJobHelper.log("【 商城id{} 正在执行中! 】", storeId);
                mchIdList = mchModelMapper.getStoreMchIdList(storeId);
                for (Integer mchId : mchIdList)
                {
                    if (showList != null)
                    {
                        XxlJobHelper.log(" 正在执行 店铺id{}", mchId);
                        for (DictionaryListModel dic : showList)
                        {
                            XxlJobHelper.log("========= 正在处理【{}】插件-自动收货 =========", dic.getCtext());
                            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchId, dic.getValue().toUpperCase());
                            if (configMap == null)
                            {
                                XxlJobHelper.log("【{}】插件-未开启/未配置 ", dic.getCtext());
                                continue;
                            }
                            //获取订单配置信息- 自动收货日期(单位秒)
                            Integer autoTeGood = MapUtils.getInteger(configMap, "autoTeGood");
                            //店铺新增余额
                            BigDecimal mchAddAccountMoney = BigDecimal.ZERO;
                            //获取满足自动收货的订单信息
                            orderList = orderDetailsModelMapper.getReceivingGoodsInfo(storeId, mchId, dic.getValue(), autoTeGood, new Date());
                            for (Map<String, Object> map : orderList)
                            {
                                Map<String, Object> parmaMap = new HashMap<>(16);
                                //明细id
                                Integer detailId = Integer.parseInt(map.get("id").toString());
                                //订单号
                                String orderNo = map.get("r_sNo").toString();
                                //商品id
                                int    goodsId = MapUtils.getIntValue(map, "goodsId");
                                String userId  = MapUtils.getString(map, "user_id");
                                String oType   = MapUtils.getString(map, "otype");
                                //订单总金额
                                BigDecimal orderPrice = new BigDecimal(MapUtils.getString(map, "z_price"));
                                //订单积分
                                BigDecimal allow = new BigDecimal(MapUtils.getString(map, "allow"));
                                XxlJobHelper.log(">>>> 订单{} 开始自动收货 <<<<", orderNo);
                                //获取商品信息
                                ProductListModel productListModel = new ProductListModel();
                                productListModel.setId(goodsId);
                                productListModel = productListModelMapper.selectOne(productListModel);
                                if (productListModel == null)
                                {
                                    XxlJobHelper.log("订单{} 有商品不存在 商品id{}", orderNo, goodsId);
                                    continue;
                                }
                                //标记订单完成 - 条件
                                parmaMap.put("detailId", detailId);
                                parmaMap.put("arrive_time", new Date());
                                parmaMap.put("rstatus", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
                                //修改值
                                parmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                                int count = orderDetailsModelMapper.updateByOrdernoDynamic(parmaMap);
                                if (count < 1)
                                {
                                    XxlJobHelper.log("订单{} (明细)自动收货失败 可能是订单状态已改变", orderNo);
                                    continue;
                                }
                                if (!taskOrderno.contains(orderNo))
                                {
                                    count = orderModelMapper.updateOrderStatusByOrderNo(storeId, DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE, orderNo);
                                    if (count < 1)
                                    {
                                        XxlJobHelper.log("订单{} 自动收货失败", orderNo);
                                        continue;
                                    }
                                    if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(oType))
                                    {
                                        XxlJobHelper.log("订单{} 进入收货赠送积分流程", orderNo);
                                        publicMemberService.memberSettlement(storeId, userId, orderNo, orderPrice, IntegralConfigModel.GiveStatus.RECEIVING);
                                        XxlJobHelper.log("收货赠送积分流程完成");
                                    }
                                }
                                //分销订单特殊处理
                                if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equals(MapUtils.getString(map, "otype")))
                                {
                                    if (pluginsModelMapper.getPluginInfo(DictionaryConst.Plugin.DISTRIBUTION, storeId) != null && publiceService.frontPlugin(MapUtils.getIntValue(map, "store_id"), DictionaryConst.Plugin.DISTRIBUTION, null))
                                    {
                                        //获取分销配置信息
                                        DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                                        distributionConfigModel.setStore_id(storeId);
                                        distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                                        if (distributionConfigModel != null)
                                        {
                                            Map<String, Object> cpayMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                                            int                 type    = MapUtils.getIntValue(cpayMap, DistributionConfigModel.SetsKey.C_PAY);
                                            //收货后结算
                                            if (type == DistributionConfigModel.SettementType.SETTEMENTTYPE_RECEIVING)
                                            {
                                                publiceDistributionService.commSettlement(storeId, userId, orderNo);
                                                XxlJobHelper.log("分销订单佣金结算,订单号:{}", orderNo);
                                            }
                                        }
                                        else
                                        {
                                            XxlJobHelper.log("分销订单结算失败{}商城分销设置未配置", storeId);
                                        }
                                    }
                                }
                                //虚拟订单特殊处理
                                if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                                {
                                    ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                                    returnOrderModel.setsNo(orderNo);
                                    returnOrderModel.setRe_type(9);
                                    List<ReturnOrderModel> select = returnOrderModelMapper.select(returnOrderModel);
                                    if (select.size() > 0)
                                    {
                                        orderPrice = orderPrice.subtract(select.get(0).getReal_money());
                                    }
                                }
                                taskOrderno.add(orderNo);
                                XxlJobHelper.log(">>>> 订单{} 开始自动收货完成 <<<<", orderNo);
//                                mchAddAccountMoney.add(orderPrice);
                                XxlJobHelper.log(">>>> 店铺id:{} 新增余额:{} 新增积分:{}<<<<", mchId, orderPrice, allow);
                                publicMchService.clientConfirmReceipt(storeId, mchId, orderNo, orderPrice, allow);
                                parmaMap = null;
                            }
//                            mchModelMapper.updateByMchIdAddAccountMoney(storeId,mchId,mchAddAccountMoney);
//                            XxlJobHelper.log(">>>> 店铺id:{} 新增余额:{} <<<<", mchId,mchAddAccountMoney);
                        }
                    }
                }

                XxlJobHelper.log("【商城id{} 执行完毕!】", storeId);
            }
            XxlJobHelper.log("自动收货 执行完毕! 总共处理了{}个订单", taskOrderno.size());
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("自动收货 异常: ", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIds = null;
            showList = null;
            mchIdList = null;
            orderList = null;
        }
    }

    /**
     * 订单报表定时任务
     *
     * @throws LaiKeAPIException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReportData() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("订单报表定时任务息 开始执行!");
            //获取所有商城id缓存
            String json = redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString();
            storeIdList = JSON.parseObject(json, new TypeReference<List<Integer>>()
            {
            });
            if (storeIdList == null || storeIdList.size() == 0)
            {
                XxlJobHelper.log("商城id缓存为空， 订单报表定时任务息以结束");
            }
            storeIdList.stream().forEach(storeid ->
            {
                OrderReportModel model = new OrderReportModel();
                int              l     = 0;
                //订单报表实时订单统计----订单金额
                orderReportModelMapper.deleteByType(storeid, 7);
                model.setType(7);
                model.setStoreId(storeid);
                model.setNum(0);
                model.setData(JSON.toJSONString(publicOrderService.getOrderData(storeid)));
                l = orderReportModelMapper.insert(model);
                if (l < 1)
                {
                    XxlJobHelper.log("订单报表定时任务,订单金额执行失败商城id： {}", storeid);
                }
                //订单报表实时订单统计---退款订单
                orderReportModelMapper.deleteByType(storeid, 6);
                model.setType(6);
                model.setStoreId(storeid);
                model.setNum(0);
                model.setData(JSON.toJSONString(publicOrderService.getRefundOrderData(storeid)));
                l = orderReportModelMapper.insert(model);
                if (l < 1)
                {
                    XxlJobHelper.log("订单报表定时任务,退款订单执行失败商城id： {}", storeid);
                }
                model = null;
            });
            XxlJobHelper.log("订单报表定时任务息 完成!");
        }
        catch (Exception e)
        {
            XxlJobHelper.log("订单报表定时任务息 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void distributionSettlement() throws LaiKeAPIException
    {
        String[] split = null;
        //查询订单信息
        OrderModel orderModel = null;
        try
        {
            // 获取参数
            String param = XxlJobHelper.getJobParam();
            XxlJobHelper.log("接收中心参数,:{}", param);

            if (param == null)
            {
                XxlJobHelper.log("参数为空，调度结束。");
                return;
            }
            split = param.split(SplitUtils.DH);
            for (String orderNo : split)
            {
                //查询订单信息
                orderModel = new OrderModel();
                orderModel.setsNo(orderNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                if (orderModel == null)
                {
                    XxlJobHelper.log("订单号:{},对应订单不存在", orderNo);
                    continue;
                }
                Integer storeId = orderModel.getStore_id();
                if (pluginsModelMapper.getPluginInfo(DictionaryConst.Plugin.DISTRIBUTION, storeId) != null && publiceService.frontPlugin(storeId, DictionaryConst.Plugin.DISTRIBUTION, null))
                {
                    //获取分销配置信息
                    DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                    distributionConfigModel.setStore_id(storeId);
                    distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                    if (distributionConfigModel != null)
                    {
                        Map<String, Object> cpayMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                        int                 type    = MapUtils.getIntValue(cpayMap, DistributionConfigModel.SetsKey.C_PAY);
                        //收货后结算
                        if (type == DistributionConfigModel.SettementType.SETTEMENTTYPE_RECEIVING)
                        {
                            publiceDistributionService.commSettlement(storeId, orderModel.getUser_id(), orderNo);
                            XxlJobHelper.log("分销订单佣金结算,订单号:{}", orderNo);
                        }
                    }
                    else
                    {
                        XxlJobHelper.log("分销订单结算失败{}商城分销设置未配置", storeId);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("分销订单确认收货佣金结算特殊处理 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            split = null;
            //查询订单信息
            orderModel = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void thirdPayOrderReturn() throws LaiKeAPIException
    {
        List<ReturnOrderModel> returnOrderModelList = null;
        try
        {
            //做测试用的,单个订单号
            String sNo = XxlJobHelper.getJobParam();
            if (StringUtils.isNotEmpty(sNo))
            {
                XxlJobHelper.log("订单号:{}执行退款业务回滚",sNo);
                returnOrderModelList = returnOrderModelMapper.getFinishReturnList(sNo);
            }
            else
            {
                returnOrderModelList = returnOrderModelMapper.getFinishReturnList(null);
            }
            if (CollectionUtils.isEmpty(returnOrderModelList))
            {
                XxlJobHelper.log("没有需要退款的订单，结束任务");
                return;
            }
            publicOrderService.thirdPayOrderReturn(returnOrderModelList);
        }
        catch (Exception e)
        {
            XxlJobHelper.log("第三方支付订单退款 异常信息:", e.getMessage());
        }
    }

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private OrderReportModelMapper orderReportModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PublicMchService publicMchService;
}

