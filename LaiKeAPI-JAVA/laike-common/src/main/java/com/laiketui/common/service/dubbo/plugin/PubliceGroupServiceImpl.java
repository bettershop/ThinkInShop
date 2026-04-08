package com.laiketui.common.service.dubbo.plugin;

import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.api.plugin.PubliceGroupService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.group.GroupConfigModel;
import com.laiketui.domain.order.OrderConfigModal;
import com.laiketui.domain.plugin.group.GoGroupOrderDetailsModel;
import com.laiketui.domain.plugin.group.GoGroupOrderModel;
import com.laiketui.domain.product.StockModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 拼团公共方法
 *
 * @author Trick
 * @date 2021/2/22 15:30
 */
@Service
public class PubliceGroupServiceImpl implements PubliceGroupService
{
    private final Logger logger = LoggerFactory.getLogger(PubliceGroupServiceImpl.class);

    @Autowired
    private GroupProductModelMapper groupProductModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private GroupOpenModelMapper groupOpenModelMapper;

    @Autowired
    private PtGoGroupOpenModelMapper ptGoGroupOpenModelMapper;

    @Autowired
    private GoGroupOrderModelMapper goGroupOrderModelMapper;

    @Autowired
    private PtGoGroupOrderModelMapper ptGoGroupOrderModelMapper;

    @Autowired
    private GoGroupOrderDetailsModelMapper goGroupOrderDetailsModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private GroupConfigModelMapper groupConfigModelMapper;

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    @Qualifier("publicAlipayServiceImpl")
    private PublicPaymentService paymentService;

    @Autowired
    private PubliceGroupService publiceGroupService;

    @Override
    public List<Map<String, Object>> getGroupGoodsInfo(int storeId, int pageStart, int pageEnd) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            resultList = groupProductModelMapper.getGroupActivityGoodsInfo(storeId, 0, pageEnd);
            for (Map<String, Object> map : resultList)
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                int                 id       = StringUtils.stringParseInt(map.get("id"));
                //平台活动id
                int platFormId = StringUtils.stringParseInt(map.get("platform_activities_id"));
                //查询活动库存
                parmaMap.put("store_id", storeId);
                parmaMap.put("product_id", id);
                Integer groupNum = groupProductModelMapper.getGroupProductNum(parmaMap);
                if (groupNum == null)
                {
                    groupNum = 0;
                }
                map.put("num", groupNum);
                //活动状态: 1.未开始 2.活动中 3.已结束
                int groupStatus = StringUtils.stringParseInt(map.get("g_status"));
                map.put("g_status", groupStatus);
                //获取商品图片
                String goodsImg = publiceService.getImgPath(map.get("imageurl").toString(), storeId);
                map.put("imgurl", goodsImg);
                //拼团参数处理
                Map<Integer, String> groupLevelMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(map.get("group_level").toString(), Map.class));
                map.put("level", groupLevelMap);
                //获取折扣
                BigDecimal zhekou = new BigDecimal("0");
                // 获取第一层级参数
                Integer levelTopKey = null;
                if (groupLevelMap != null)
                {
                    for (Integer key : groupLevelMap.keySet())
                    {
                        levelTopKey = key;
                        break;
                    }
                    String[] zhekouList = groupLevelMap.get(levelTopKey).split("~");
                    zhekou = new BigDecimal(zhekouList[0]);
                }
                int goodsId = StringUtils.stringParseInt(map.get("product_id"));
                //获取属性里最低的金额
                ConfiGureModel confiGureModel = confiGureModelMapper.getProductMinPriceAndMaxYprice(goodsId);
                BigDecimal     minPrice       = confiGureModel.getPrice();
                map.put("market_price", minPrice.toString());
                map.put("groupnum", levelTopKey);
                //开团价格
                BigDecimal kaiPrice = minPrice.multiply(zhekou).divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_UP);
                map.put("kaiprice", kaiPrice.toString());

                //获取活动时间
                String              productData    = map.get("group_data").toString();
                Map<String, String> productDataMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(productData, Map.class));
                Date                startDate      = null;
                if (productDataMap != null && !productDataMap.isEmpty())
                {
                    startDate = DateUtil.dateFormateToDate(productDataMap.get("starttime"), GloabConst.TimePattern.YMDHMS);
                }
                //设置时间 初始值
                if (startDate != null)
                {
                    map.put("leftTime", startDate.getTime() - System.currentTimeMillis());
                }
                map.put("hour", "00");
                map.put("mniuate", "00");
                map.put("second", "00");
                //查询活动商品属性信息
                ConfiGureModel confiGure = new ConfiGureModel();
                confiGure.setPid(goodsId);
                if (platFormId > 0)
                {
                    confiGure.setId(StringUtils.stringParseInt(map.get("attr_id")));
                    confiGure.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                    confiGure = confiGureModelMapper.selectOne(confiGure);
                    minPrice = confiGure.getPrice();
                }
                else
                {
                    minPrice = confiGureModelMapper.getProductMinPrice(goodsId);
                }
                //拼团最低价格阈值
                int minBili = 100000000;
                //拼团人数阈值
                int minMan = 999999;
                //查询最低价格和拼团人数
                if (groupLevelMap != null)
                {
                    for (Integer key : groupLevelMap.keySet())
                    {
                        //a:1:{i:2;s:5:"90~80";}
                        String[] bilis = groupLevelMap.get(key).split("~");
                        if (minBili > StringUtils.stringParseInt(bilis[0]))
                        {
                            minBili = StringUtils.stringParseInt(bilis[0]);
                            minMan = StringUtils.stringParseInt(bilis[1]);
                        }
                    }
                }
                minPrice = minPrice.multiply(new BigDecimal(String.valueOf(minBili))).divide(new BigDecimal(100), BigDecimal.ROUND_HALF_UP);
                map.put("min_price", minPrice);
                map.put("min_man", minMan);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取正在拼团得商品" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGroupGoodsInfo");
        }
        return resultList;
    }


    @Override
    public Map<String, BigDecimal> getGroupDiscountPrice(int storeId, String groupLevelStr, BigDecimal minPrice, int openNum) throws LaiKeAPIException
    {
        Map<String, BigDecimal> resultMap = new HashMap<>(16);
        try
        {
            //参团折扣价
            BigDecimal groupPrice = new BigDecimal("0");
            //开团价
            BigDecimal groupOpenPrice = new BigDecimal("0");
            //拼团参数处理 a:2:{i:2;s:5:"70~60";i:4;s:5:"65~55";}
            Map<Integer, String> groupLevelMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(groupLevelStr, Map.class));
            //拼团人数阈值
            int minMan = 100;
            //参团折扣
            BigDecimal zhekou = new BigDecimal("1");
            //开团折扣
            BigDecimal zhekouOpen = new BigDecimal("1");

            //是否开启团长优惠,没有开启则获取参团折扣
            GroupConfigModel groupConfigModel = new GroupConfigModel();
            groupConfigModel.setStore_id(storeId);
            groupConfigModel = groupConfigModelMapper.selectOne(groupConfigModel);

            //查询最低价格和拼团人数
            if (groupLevelMap != null)
            {
                String[] zhekouList;
                if (openNum > 0)
                {
                    //根据人数获取获取折扣
                    zhekouList = groupLevelMap.get(openNum).split("~");
                    minMan = openNum;
                }
                else
                {
                    //获取最大团信息
                    List<Integer> sortKeys = new ArrayList<>(groupLevelMap.keySet());
                    //排序
                    sortKeys.sort((o1, o2) -> o1.equals(o2) ? 0 : o1 < o2 ? 1 : -1);
                    zhekouList = groupLevelMap.get(sortKeys.get(0)).split("~");
                    minMan = sortKeys.get(0);
                }
                //参团折扣（zhekouList[1]=开团折扣）
                zhekou = new BigDecimal(zhekouList[0]);
                //开团折扣
                if (groupConfigModel.getOpen_discount() == 1)
                {
                    zhekouOpen = new BigDecimal(zhekouList[1]);
                }
                else
                {
                    //未开启团长折扣,则用参团折扣
                    zhekouOpen = zhekou;
                }

                groupPrice = minPrice.multiply(zhekou).divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_UP);
                groupOpenPrice = minPrice.multiply(zhekouOpen).divide(new BigDecimal("100"), BigDecimal.ROUND_HALF_UP);
            }
            resultMap.put("ptDiscount", zhekou);
            resultMap.put("groupPrice", groupPrice);
            resultMap.put("groupOpenPrice", groupOpenPrice);
            resultMap.put("kaiTuanDiscount", zhekouOpen);
            resultMap.put("minMan", new BigDecimal(minMan + ""));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取拼团价格 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGroupDiscountPrice");
        }
        return resultMap;
    }

    @Override
    public Map<String, Boolean> validataGroup(int storeId, String userId) throws LaiKeAPIException
    {
        return validataGroup(storeId, userId, false);
    }

    @Override
    public Map<String, Boolean> validataGroup(int storeId, String userId, boolean isPlatForm) throws LaiKeAPIException
    {
        Map<String, Boolean> resultMap = new HashMap<>(16);
//        try {
//            //正常参团
//            boolean isUserCan = true;
//            //正常开团
//            boolean isOpenGroup = true;
//            //用户是否重复参团
//            boolean isAgainCan = true;
//            //用户是否重复开团
//            boolean isAgainOpen = true;
//
//            //获取拼团配置信息
//            GroupConfigModel groupConfigModel = new GroupConfigModel();
//            groupConfigModel.setStore_id(storeId);
//            groupConfigModel = groupConfigModelMapper.selectOne(groupConfigModel);
//            //最多参团数量
//            int maxGroupNum = groupConfigModel.getCan_num();
//            //最大开团数量
//            int maxOpenNum = groupConfigModel.getOpen_num();
//            //查询当前用户开团数量
//            int userOpenNum;
//            if (!isPlatForm) {
//                GroupOpenModel groupOpenModel = new GroupOpenModel();
//                groupOpenModel.setStore_id(storeId);
//                groupOpenModel.setUid(userId);
//                groupOpenModel.setPtstatus(GroupOpenModel.Ptstatus.PT_ING);
//                userOpenNum = groupOpenModelMapper.selectCount(groupOpenModel);
//            } else {
//                PtGoGroupOpenModel ptGoGroupOpenModel = new PtGoGroupOpenModel();
//                ptGoGroupOpenModel.setStore_id(storeId);
//                ptGoGroupOpenModel.setUid(userId);
//                ptGoGroupOpenModel.setPtstatus(GroupOpenModel.Ptstatus.PT_ING);
//                userOpenNum = ptGoGroupOpenModelMapper.selectCount(ptGoGroupOpenModel);
//            }
//            //校验开团数量
//            if (userOpenNum >= maxOpenNum) {
//                logger.debug("用户{} 开团数量{},系统限制开团数量{}", userId, userOpenNum, maxGroupNum);
//                isOpenGroup = false;
//            }
//            //校验参团数量
//            int userCanNum;
//            if (!isPlatForm) {
//                GoGroupOrderModel goGroupOrderModel = new GoGroupOrderModel();
//                goGroupOrderModel.setStore_id(storeId);
//                goGroupOrderModel.setUser_id(userId);
//                goGroupOrderModel.setPtstatus(GoGroupOrderModel.Ptstatus.PT_ING);
//                goGroupOrderModel.setPid(GoGroupOrderModel.OrderPid.CANTUAN);
//                userCanNum = goGroupOrderModelMapper.selectCount(goGroupOrderModel);
//            } else {
//                PtGoGroupOrderModel ptGoGroupOrderModel = new PtGoGroupOrderModel();
//                ptGoGroupOrderModel.setStore_id(storeId);
//                ptGoGroupOrderModel.setUser_id(userId);
//                ptGoGroupOrderModel.setPtstatus(GoGroupOrderModel.Ptstatus.PT_ING);
//                ptGoGroupOrderModel.setPid(GoGroupOrderModel.OrderPid.CANTUAN);
//                userCanNum = ptGoGroupOrderModelMapper.selectCount(ptGoGroupOrderModel);
//            }
//            if (userCanNum >= maxGroupNum) {
//                logger.debug("用户{} 参团数量{},系统限制参团数量{}", userId, userCanNum, maxGroupNum);
//                isUserCan = false;
//            }
//            //不允许重复参团
//            if (groupConfigModel.getCan_again() == 0) {
//                //校验是否重复参团/开团
//                if (userCanNum >= 1) {
//                    logger.debug("用户{} 参团数量{},系统禁止重复参团", userId, userOpenNum);
//                    isAgainCan = false;
//                }
//                if (userOpenNum >= 1) {
//                    logger.debug("用户{} 开团数量{},系统禁止重复开团", userId, userOpenNum);
//                    isAgainOpen = false;
//                }
//            }
//
//            resultMap.put("user_can_open", isOpenGroup);
//            resultMap.put("user_can_can", isUserCan);
//            resultMap.put("isagain_can", isAgainCan);
//            resultMap.put("isagain_open", isAgainOpen);
//            //兼容php其它地方
//            resultMap.put("again_can", isAgainCan);
//            resultMap.put("again_open", isAgainOpen);
//        } catch (LaiKeAPIException l) {
//            throw l;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("拼团规则校验 异常" + e.getMessage());
//            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "validataGroup");
//        }
        return resultMap;
    }

    @Override
    public boolean reBackGoodsNum(int storeId, String orderno, int activityNo, String text) throws LaiKeAPIException
    {
        try
        {
            int count;
            //获取订单商品信息
            GoGroupOrderDetailsModel goGroupOrderDetailsModel = new GoGroupOrderDetailsModel();
            goGroupOrderDetailsModel.setStore_id(storeId);
            goGroupOrderDetailsModel.setR_sNo(orderno);
            List<GoGroupOrderDetailsModel> goGroupOrderDetailsModelList = goGroupOrderDetailsModelMapper.select(goGroupOrderDetailsModel);
            for (GoGroupOrderDetailsModel goGroupOrderDetail : goGroupOrderDetailsModelList)
            {
                int attrId = Integer.parseInt(goGroupOrderDetail.getSid());
                //回滚规格库存
                count = confiGureModelMapper.reduceGoodsStockNum(goGroupOrderDetail.getNum(), attrId);
                if (count < 1)
                {
                    logger.error("商品规格id={} 规格存扣减失败 数量{}", attrId, goGroupOrderDetail.getNum());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                //回滚总库存
                count = productListModelMapper.reduceGoodsStockNum(goGroupOrderDetail.getP_id(), -goGroupOrderDetail.getNum());
                if (count < 1)
                {
                    logger.error("商品id={} 商品总库存扣减失败 数量{}", goGroupOrderDetail.getP_id(), goGroupOrderDetail.getNum());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                //剩余库存
                int totalStockNum = confiGureModelMapper.sumConfigGureNum(attrId);
                //生成记录
                StockModel stockSave = new StockModel();
                stockSave.setType(1);
                stockSave.setContent(text);
                stockSave.setTotal_num(totalStockNum);
                stockSave.setFlowing_num(goGroupOrderDetail.getNum());
                stockSave.setStore_id(storeId);
                stockSave.setProduct_id(goGroupOrderDetail.getP_id());
                stockSave.setAttribute_id(attrId);
                stockSave.setUser_id(goGroupOrderDetail.getUser_id());
                stockSave.setAdd_date(new Date());
                count = stockModelMapper.insertSelective(stockSave);
                if (count < 1)
                {
                    logger.error("商品id={} 库存记录失败 数量{}", goGroupOrderDetail.getP_id(), goGroupOrderDetail.getNum());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团回退商品库存 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "reBackGoodsNum");
        }
    }

    @Override
    public void ptTask(int storeId) throws LaiKeAPIException
    {

    }

    @Override
    public void closeUnPaidOrder(int storeId) throws LaiKeAPIException
    {
        try
        {
            int row;
            //未付款保留时间
            int retainTime;
            //获取订单设置
            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(storeId);
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
            if (orderConfigModal != null)
            {
                retainTime = orderConfigModal.getOrder_failure();
                if (retainTime > 0)
                {
                    //时换秒
                    retainTime = DateUtil.dateConversion(retainTime, DateUtil.TimeType.TIME);
                    //计算保留的时间
                    Date endTime = DateUtil.getAddDateBySecond(new Date(), -retainTime);
                    row = goGroupOrderModelMapper.closeOrder(storeId, endTime);
                    logger.debug("一共清除【{}】个未支付订单", row);
                    //获取失效订单
                    List<GoGroupOrderModel> goGroupOrderModelList = goGroupOrderModelMapper.selectInvalidOrder(storeId, endTime);
                    for (GoGroupOrderModel goGroupOrderModel : goGroupOrderModelList)
                    {
                        //关闭明细
                        row = goGroupOrderDetailsModelMapper.updateStatus(goGroupOrderModel.getStore_id(), goGroupOrderModel.getsNo(), GoGroupOrderModel.OrderStatus.ORDER_CLOSE);
                        logger.debug("正在清除订单明细,订单号{},清除状态:{}", goGroupOrderModel.getsNo(), row > 0);
                        //回滚库存
                        reBackGoodsNum(storeId, goGroupOrderModel.getsNo(), goGroupOrderModel.getActivity_no(), "关闭订单,增加商品总库存");
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
    }
}

