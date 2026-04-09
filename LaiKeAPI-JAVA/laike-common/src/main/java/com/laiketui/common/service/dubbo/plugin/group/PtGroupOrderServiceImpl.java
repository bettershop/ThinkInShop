package com.laiketui.common.service.dubbo.plugin.group;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.PublicAddressService;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.api.plugin.PubliceGroupService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.plugin.group.*;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.plugin.group.ProductInfoParamVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼团订单流程 - 平台
 *
 * @author Trick
 * @date 2021/4/26 16:16
 */
@HandlerOrderType(type = com.laiketui.core.lktconst.DictionaryConst.OrdersType.PTHD_ORDER_HEADER + com.laiketui.core.lktconst.DictionaryConst.OrdersType.ORDERS_HEADER_PT)
@Service
public class PtGroupOrderServiceImpl implements OrderDubboService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PtGroupProductModelMapper groupProductModelMapper;

    @Autowired
    private PtGoGroupOrderModelMapper goGroupOrderModelMapper;

    @Autowired
    private PtGoGroupOrderDetailsModelMapper goGroupOrderDetailsModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PtGoGroupOpenModelMapper groupOpenModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PublicAddressService publicAddressService;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PubliceGroupService publiceGroupService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> settlement(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //处理参数
            ProductInfoParamVo productInfoParamVo;
            try
            {
                productInfoParamVo = JSON.parseObject(StringUtils.trim(vo.getProductsInfo(), "\""), ProductInfoParamVo.class);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            //拼团人数
            Integer groupNum = productInfoParamVo.getGroupnum();
            //是否有收货地址标识
            int isAddress = 0;
            //用户是否设置支付密码标识
            int isSetPayPwd = 0;
            //是否能购买标识
            boolean canPay = true;
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            if (!StringUtils.isEmpty(user.getPassword()))
            {
                isSetPayPwd = 1;
            }
            //店铺信息
            Map<String, Object> resultMchMap = new HashMap<>(16);
            //商品信息
            Map<String, Object> goodsMap = new HashMap<>(16);

            if (!StringUtils.isEmpty(productInfoParamVo.getPtcode()))
            {
                //获取开团信息
                PtGoGroupOpenModel groupOpenModel = new PtGoGroupOpenModel();
                groupOpenModel.setStore_id(vo.getStoreId());
                groupOpenModel.setActivity_no(productInfoParamVo.getActivityNo());
                groupOpenModel.setPtcode(productInfoParamVo.getPtcode());
                groupOpenModel = groupOpenModelMapper.selectOne(groupOpenModel);
                if (groupOpenModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常");
                }
                groupNum = groupOpenModel.getGroupman();
            }
            //获取拼团商品活动信息
            PtGroupProductModel groupProductModel = new PtGroupProductModel();
            groupProductModel.setStore_id(vo.getStoreId());
            groupProductModel.setActivity_no(productInfoParamVo.getActivityNo());
            groupProductModel.setProduct_id(productInfoParamVo.getPid());
            groupProductModel.setAttr_id(productInfoParamVo.getCid());
            groupProductModel = groupProductModelMapper.selectOne(groupProductModel);
            if (groupProductModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常");
            }
            //获取商品信息
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", productInfoParamVo.getPid());
            parmaMap.put("attrId", productInfoParamVo.getCid());
            parmaMap.put("active", DictionaryConst.GoodsActive.GOODSACTIVE_SUPPORT_PT);
            List<Map<String, Object>> goodsInfo = productListModelMapper.getProductListDynamic(parmaMap);

            //商品运费模板id
            Integer goodsTemplateId = null;
            //商品价格
            BigDecimal goodsPrice = new BigDecimal("0");
            for (Map<String, Object> map : goodsInfo)
            {
                //填充店铺信息
                resultMchMap.put("id", map.get("mchId"));
                resultMchMap.put("store_id", vo.getStoreId());
                resultMchMap.put("name", map.get("mch_name"));
                resultMchMap.put("logo", map.get("logo"));
                //填充商品信息
                goodsMap.put("image", publiceService.getImgPath(map.get("img").toString(), vo.getStoreId()));
                goodsMap.put("product_id", map.get("cid"));
                goodsMap.put("num", map.get("num"));
                goodsMap.put("pro_name", map.get("product_title"));
                goodsMap.put("freight", map.get("freight"));
                goodsMap.put("size", GoodsDataUtils.getProductSkuValue(map.get("attribute").toString()));
                goodsMap.put("have", goGroupOrderModelMapper.countGroupPayOrderNum(vo.getStoreId(), user.getUser_id(), productInfoParamVo.getPtcode()));
                if (map.containsKey("freight") && !StringUtils.isEmpty(map.get("freight")))
                {
                    goodsTemplateId = Integer.parseInt(map.get("freight").toString());
                }
                goodsPrice = new BigDecimal(map.get("price").toString());
            }
            //拼团价格计算
            Map<String, BigDecimal> groupPriceMap = publiceGroupService.getGroupDiscountPrice(vo.getStoreId(), groupProductModel.getGroup_level(), goodsPrice, groupNum);
            goodsPrice = groupPriceMap.get("groupPrice");

            //获取收货地址信息
            UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            //获取用户购买数量
            Integer payNum = goGroupOrderModelMapper.countGroupPayOrderNum(vo.getStoreId(), user.getUser_id(), productInfoParamVo.getPtcode());
            if (payNum != null && payNum > 0)
            {
                //不能重复购买
                canPay = false;
            }
            BigDecimal freightPrice = new BigDecimal("0");
            //判断平台是否设置了免邮
            if (groupProductModel.getFree_freight() != 1)
            {
                if (userAddress != null)
                {
                    //未设置免邮,计算运费
                    freightPrice = publicOrderService.getFreight(goodsTemplateId, userAddress);
                    goodsPrice = goodsPrice.add(freightPrice);
                    isAddress = 1;
                }
            }

            //计算会员特惠
            BigDecimal gradeRate = new BigDecimal(publicMemberService.getMemberGradeRate(DictionaryConst.OrdersType.PTHD_ORDER_PP, user.getUser_id(), vo.getStoreId()) + "");

            resultMap.put("mch_data", resultMchMap);
            resultMap.put("can_pay", canPay);
            resultMap.put("is_add", isAddress);
            resultMap.put("buymsg", userAddress);
            resultMap.put("yunfei", freightPrice);
            resultMap.put("password_status", isSetPayPwd);
            resultMap.put("groupres", DataUtils.cast(SerializePhpUtils.getUnserializeObj(groupProductModel.getGroup_data(), Map.class)));
            //支付方式配置
            resultMap.put("payment", publicOrderService.getPaymentConfig(vo.getStoreId()));
            //用户余额
            resultMap.put("money", user.getMoney());
            resultMap.put("user_name", user.getUser_name());
            //商品信息
            resultMap.put("proattr", goodsMap);
            //订单价格
            resultMap.put("grade_rate_amount", goodsPrice);
            //会员折扣
            resultMap.put("grade_rate", gradeRate);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取拼团订单信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPtOrderInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> payment(OrderVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //处理参数
            ProductInfoParamVo productInfoParamVo;
            try
            {
                productInfoParamVo = JSON.parseObject(StringUtils.trim(vo.getProductsInfo(), "\""), ProductInfoParamVo.class);
            }
            catch (Exception e)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //参团订单
            PtGoGroupOrderModel goGroupOrderModel;
            if (GoGroupOrderModel.OrderPid.CANTUAN.equals(productInfoParamVo.getFromapge()))
            {
                //验证是否已参团
                goGroupOrderModel = new PtGoGroupOrderModel();
                goGroupOrderModel.setStore_id(vo.getStoreId());
                goGroupOrderModel.setPtcode(productInfoParamVo.getPtcode());
                goGroupOrderModel.setUser_id(user.getUser_id());
                goGroupOrderModel.setPid(GoGroupOrderModel.OrderPid.CANTUAN);
                goGroupOrderModel.setStatus(GoGroupOrderModel.OrderStatus.NOTPAY);
                goGroupOrderModel = goGroupOrderModelMapper.selectOne(goGroupOrderModel);
                if (goGroupOrderModel != null)
                {
                    logger.debug("请先取消之前参团的订单");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXQXZQCTDDD, "请先取消之前参团的订单");
                }
                //获取拼团信息
                PtGoGroupOpenModel groupOpenModel = new PtGoGroupOpenModel();
                groupOpenModel.setPtcode(productInfoParamVo.getPtcode());
                groupOpenModel.setStore_id(vo.getStoreId());
                groupOpenModel.setActivity_no(productInfoParamVo.getActivityNo());
                groupOpenModel = groupOpenModelMapper.selectOne(groupOpenModel);
                if (groupOpenModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                productInfoParamVo.setGroupnum(groupOpenModel.getGroupman());
            }

            //获取用户地址
            UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            if (userAddress == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSHDZ, "请选择收货地址");
            }
            //获取商品信息
            Map<String, Object> goodsMap;
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", productInfoParamVo.getPid());
            parmaMap.put("attrId", productInfoParamVo.getCid());
            parmaMap.put("active", DictionaryConst.GoodsActive.GOODSACTIVE_SUPPORT_PT);
            List<Map<String, Object>> goodsInfo = productListModelMapper.getProductListDynamic(parmaMap);
            if (goodsInfo == null || goodsInfo.size() != 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            goodsMap = goodsInfo.get(0);
            //商品id
            int goodsId = Integer.parseInt(goodsMap.get("id").toString());
            //商品价格
            BigDecimal goodsPrice = new BigDecimal(goodsInfo.get(0).get("price").toString());
            //商品运费模板id
            int goodsTemplateId = Integer.parseInt(goodsMap.get("freight").toString());
            //商品所属店铺
            int mchId = Integer.parseInt(goodsMap.get("mchId").toString());
            //商品名称
            String goodsName = goodsMap.get("product_title").toString();
            //商品规格
            String attrName = GoodsDataUtils.getProductSkuValue(goodsMap.get("attribute").toString());

            //获取拼团商品活动信息
            PtGroupProductModel groupProductModel = new PtGroupProductModel();
            groupProductModel.setStore_id(vo.getStoreId());
            groupProductModel.setAttr_id(productInfoParamVo.getCid());
            groupProductModel.setProduct_id(productInfoParamVo.getPid());
            groupProductModel.setActivity_no(productInfoParamVo.getActivityNo());
            groupProductModel = groupProductModelMapper.selectOne(groupProductModel);
            if (groupProductModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常");
            }
            //拼团价格计算
            Map<String, BigDecimal> groupPriceMap = publiceGroupService.getGroupDiscountPrice(vo.getStoreId(), groupProductModel.getGroup_level(), goodsPrice, productInfoParamVo.getGroupnum());
            //拼团价格
            BigDecimal groupPrice;
            if (GoGroupOrderModel.OrderPid.KAITUAN.equals(productInfoParamVo.getFromapge()))
            {
                groupPrice = groupPriceMap.get("groupOpenPrice");
            }
            else
            {
                groupPrice = groupPriceMap.get("groupPrice");
            }
            //拼团人数
            BigDecimal minMan = groupPriceMap.get("minMan");
            //计算会员特惠
            BigDecimal gradeRate = new BigDecimal(publicMemberService.getMemberGradeRate(DictionaryConst.OrdersType.PTHD_ORDER_PP, user.getUser_id(), vo.getStoreId()) + "");
            //计算订单金额
            BigDecimal orderPrice   = groupPrice.multiply(new BigDecimal(productInfoParamVo.getNum().toString())).multiply(gradeRate);
            BigDecimal freightPrice = new BigDecimal("0");
            //判断平台是否设置了免邮
            if (groupProductModel.getFree_freight() != 1)
            {
                //未设置免邮,计算运费
                freightPrice = publicOrderService.getFreight(goodsTemplateId, userAddress);
                orderPrice = orderPrice.add(freightPrice);
            }
            //拼接店铺
            String mchStr = String.format(",%s,", mchId);
            //添加订单信息
            PtGoGroupOrderModel goGroupOrderSave = new PtGoGroupOrderModel();
            goGroupOrderSave.setStore_id(vo.getStoreId());
            goGroupOrderSave.setActivity_no(productInfoParamVo.getActivityNo() + "");
            goGroupOrderSave.setUser_id(user.getUser_id());
            goGroupOrderSave.setName(userAddress.getName());
            goGroupOrderSave.setMobile(userAddress.getTel());
            goGroupOrderSave.setNum(productInfoParamVo.getNum());
            goGroupOrderSave.setSpz_price(groupPrice);
            goGroupOrderSave.setZ_price(orderPrice);
            goGroupOrderSave.setSheng(userAddress.getSheng());
            goGroupOrderSave.setShi(userAddress.getCity());
            goGroupOrderSave.setXian(userAddress.getQuyu());
            goGroupOrderSave.setAddress(userAddress.getAddress());
            goGroupOrderSave.setPay(vo.getPayType());
            goGroupOrderSave.setSource(vo.getStoreType());
            goGroupOrderSave.setOtype(DictionaryConst.OrdersType.PTHD_ORDER_PP.toLowerCase());
            goGroupOrderSave.setPtcode(productInfoParamVo.getPtcode());
            goGroupOrderSave.setStatus(GoGroupOrderModel.OrderStatus.NOTPAY);
            goGroupOrderSave.setPtstatus(GoGroupOrderModel.Ptstatus.NOTPAY);
            goGroupOrderSave.setPid(productInfoParamVo.getFromapge());
            goGroupOrderSave.setGroupman(minMan.intValue() + "");
            goGroupOrderSave.setMch_id(mchStr);
            goGroupOrderSave.setGrade_rate(gradeRate);
            goGroupOrderSave.setPlatform_activities_id(groupProductModel.getPlatform_activities_id());
            //生成订单号
            String orderno = publicOrderService.createOrderNo(DictionaryConst.OrdersType.PTHD_ORDER_PP);
            goGroupOrderSave.setsNo(orderno);
            goGroupOrderSave.setAdd_time(new Date());
            int count = goGroupOrderModelMapper.insertSelective(goGroupOrderSave);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }
            goGroupOrderModel = goGroupOrderSave;
            //详情数据
            PtGoGroupOrderDetailsModel goGroupOrderDetailsSave = new PtGoGroupOrderDetailsModel();
            goGroupOrderDetailsSave.setR_sNo(goGroupOrderSave.getsNo());
            goGroupOrderDetailsSave.setStore_id(vo.getStoreId());
            goGroupOrderDetailsSave.setAfter_discount(goGroupOrderSave.getZ_price());
            goGroupOrderDetailsSave.setUser_id(user.getUser_id());
            goGroupOrderDetailsSave.setP_id(productInfoParamVo.getPid());
            goGroupOrderDetailsSave.setP_name(goodsName);
            goGroupOrderDetailsSave.setP_price(goGroupOrderSave.getSpz_price());
            goGroupOrderDetailsSave.setNum(goGroupOrderSave.getNum());
            goGroupOrderDetailsSave.setSize(attrName);
            goGroupOrderDetailsSave.setSize(attrName);
            goGroupOrderDetailsSave.setSid(productInfoParamVo.getCid().toString());
            goGroupOrderDetailsSave.setFreight(freightPrice);
            goGroupOrderDetailsSave.setAdd_time(new Date());
            count = goGroupOrderDetailsModelMapper.insertSelective(goGroupOrderDetailsSave);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }
            //修改总库存
            count = productListModelMapper.reduceGoodsStockNum(goodsId, goGroupOrderSave.getNum());
            if (count < 1)
            {
                logger.error("商品id={} 商品总库存扣减失败 数量{}", productInfoParamVo.getPid(), goGroupOrderSave.getNum());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }
            //扣减规格库存
            count = confiGureModelMapper.reduceGoodsStockNum(-goGroupOrderSave.getNum(), productInfoParamVo.getCid());
            if (count < 1)
            {
                logger.error("商品id={} 规格存扣减失败 数量{}", productInfoParamVo.getPid(), goGroupOrderSave.getNum());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }

            //剩余库存
            int totalStockNum = confiGureModelMapper.sumConfigGureNum(productInfoParamVo.getCid());
            //生成记录
            String     msg       = "%s生成订单所需%s";
            StockModel stockSave = new StockModel();
            stockSave.setType(1);
            stockSave.setContent(String.format(msg, "拼团", goGroupOrderSave.getNum()));
            stockSave.setTotal_num(totalStockNum);
            stockSave.setFlowing_num(goGroupOrderSave.getNum());
            stockSave.setStore_id(vo.getStoreId());
            stockSave.setProduct_id(productInfoParamVo.getPid());
            stockSave.setAttribute_id(productInfoParamVo.getCid());
            stockSave.setUser_id(user.getUser_id());
            stockSave.setAdd_date(new Date());
            count = stockModelMapper.insertSelective(stockSave);
            if (count < 1)
            {
                logger.error("商品id={} 库存记录失败 数量{}", productInfoParamVo.getPid(), goGroupOrderSave.getNum());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }

            resultMap.put("status", 1);
            resultMap.put("order", goGroupOrderModel.getsNo());
            resultMap.put("total", goGroupOrderModel.getZ_price());
            resultMap.put("order_id", goGroupOrderModel.getId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("创建拼团订单 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "payment");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> updateOrderRemark(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> splitOrder(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnData(ApplyReturnDataVo vo, MultipartFile file) throws LaiKeAPIException
    {
        return null;
    }

    @Override
    public Map<String, Object> orderList(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> remindDelivery(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancleOrder(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> loadMore(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> delOrder(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> buyAgain(BuyAgainVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> orderSearch(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> delCart(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> getPaymentConf(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> orderDetails(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> showLogistics(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancleApply(int storeId, int id)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnOrderList(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> okOrder(OrderVo orderVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returnMethod(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> seeSend(int stroeId, int productId)
    {
        return null;
    }

    @Override
    public Map<String, Object> backSend(ReturnGoodsVo returnGoodsVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> seeExtractionCode(OrderVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> returndetails(RefundDetailsVo refundDetailsVo)
    {
        return null;
    }

    @Override
    public Map<String, Object> confirmReceipt(ReturnConfirmReceiptVo vo)
    {
        return null;
    }

    @Override
    public Map<String, Object> cancelApplication(CancleAfterSaleApplyVo params)
    {
        return null;
    }

    @Override
    public Map<String, Object> getPayment(OrderVo vo)
    {
        return null;
    }
}

