package com.laiketui.comps.task.services;

import com.laiketui.common.mapper.*;
import com.laiketui.comps.api.task.CompsTaskGoodsService;
import com.laiketui.core.lktconst.ErrorCode;
import com.xxl.job.core.context.XxlJobHelper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import org.apache.commons.collections.MapUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 商品定时任务实现
 *
 * @author Trick
 * @date 2020/12/15 16:12
 */
@Service
public class CompsTaskGoodsServiceImpl implements CompsTaskGoodsService
{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void goodsStatus() throws LaiKeAPIException
    {
        ProductListModel       productListModel     = null;
        StockModel             stockModel           = null;
        ConfiGureModel         confiGureModel       = null;
        List<ProductListModel> productListModelList = null;
        ProductListModel       updateGoods          = null;
        try
        {
            XxlJobHelper.log("定时刷新商品状态 开始执行 ");
            //获取所有商品信息
            productListModel = new ProductListModel();
            productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            productListModel.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
            productListModelList = productListModelMapper.select(productListModel);
            XxlJobHelper.log("总共需要处理商品数量{} ", productListModelList.size());
            for (ProductListModel goods : productListModelList)
            {
                // 当库存为0 则自动下架商品
                if (goods.getNum() == 0)
                {
                    updateGoods = new ProductListModel();
                    updateGoods.setId(goods.getId());
                    updateGoods.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                    int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                    XxlJobHelper.log("商品id{},因库存不足被下架 执行结果:{} ", goods.getId(), count > 0);
                }
                //获取商品属性库存
                confiGureModel = new ConfiGureModel();
                confiGureModel.setPid(goods.getId());
                List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
                for (ConfiGureModel confiGure : confiGureModelList)
                {
                    //库存预警
                    if (confiGure.getNum() <= goods.getMin_inventory())
                    {
                        XxlJobHelper.log(">>>>> 商品规格id{},当前库存数量{},下限数量{};库存达到下限 库存预警!  <<<<<", confiGure.getId(), goods.getNum(), confiGure.getMin_inventory());
                        //检查是否存在预警记录
                        stockModel = new StockModel();
                        stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                        stockModel.setProduct_id(goods.getId());
                        stockModel.setAttribute_id(confiGure.getId());
                        int count = stockModelMapper.selectCount(stockModel);
                        if (count > 0)
                        {
                            XxlJobHelper.log("该商品已做了库存预警");
                            continue;
                        }
                        stockModel.setTotal_num(confiGure.getTotal_num());
                        stockModel.setStore_id(goods.getStore_id());
                        stockModel.setContent("预警");
                        stockModel.setAdd_date(new Date());
                        int id = stockModelMapper.insertSelective(stockModel);
                        XxlJobHelper.log(">>>>> 库存预警执信息保存完成 id{} <<<<<", id);
                    }
                }
            }
            XxlJobHelper.log("定时刷新商品状态 执行完毕! ");
        }
        catch (Exception e)
        {
            XxlJobHelper.log("定时刷新商品状态 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, e.getMessage());
        }
        finally
        {
            productListModel = null;
            stockModel = null;
            confiGureModel = null;
            productListModelList = null;
            updateGoods = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sellGoods() throws LaiKeAPIException
    {
        //预售商品结束倒计时
        PreSellGoodsModel preSellGoodsModel = null;
        //预售商品所产生的待付款订单全部关闭并且定金结算给店铺
        List<Map<String, Object>> goodOrder            = null;
        List<ProductListModel>    productListModelList = null;
        ProductListModel          productListModel     = null;
        try
        {
            XxlJobHelper.log("定时刷新预售商品状态 开始执行 ");
            //获取所有商品信息
            productListModel = new ProductListModel();
            productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
//            productListModel.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
            productListModel.setIs_presell(1);
            productListModelList = productListModelMapper.select(productListModel);
            XxlJobHelper.log("总共需要处理预售商品数量{} ", productListModelList.size());
            for (ProductListModel goods : productListModelList)
            {
                //预售商品结束倒计时
                preSellGoodsModel = new PreSellGoodsModel();
                preSellGoodsModel.setProduct_id(goods.getId());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                if (preSellGoodsModel.getSell_type().equals(PreSellGoodsModel.DEPOSIT_PATTERN))
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(preSellGoodsModel.getBalance_pay_time());
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    calendar.add(Calendar.SECOND, -1);
                    Date end = calendar.getTime();
                    if (DateUtil.dateCompare(new Date(), end))
                    {
                        goods.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                        int count = productListModelMapper.updateByPrimaryKeySelective(goods);
                        XxlJobHelper.log("商品id{},因已过截止支付尾款时间被下架 执行结果:{} ", goods.getId(), count > 0);
                        //预售商品所产生的待付款订单全部关闭并且定金结算给店铺
                        goodOrder = preSellRecordModelMapper.getGoodOrder(goods.getId());
                        if (goodOrder.size() > 0)
                        {
                            goodOrder.stream().forEach(order ->
                            {
                                Integer    storeId    = MapUtils.getInteger(order, "store_id");
                                Integer    productId  = MapUtils.getInteger(order, "product_id");
                                String     sNo        = MapUtils.getString(order, "sNo");
                                OrderModel orderModel = new OrderModel();
                                orderModel.setsNo(sNo);
                                orderModel = orderModelMapper.selectOne(orderModel);
                                orderModel.setStatus(7);
                                orderModel.setArrive_time(new Date());
                                orderModel.setSettlement_status(DictionaryConst.WhetherMaven.WHETHER_OK);
                                orderModelMapper.updateByPrimaryKeySelective(orderModel);
                                List<Map<String, Object>> orderDetailNotClose = orderDetailsModelMapper.getOrderDetailNotClose(storeId, sNo);
                                orderDetailNotClose.stream().forEach(orderDetail ->
                                {
                                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                                    orderDetailsModel.setId(MapUtils.getInteger(orderDetail, "id"));
                                    orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
                                    orderDetailsModel.setR_status(7);
                                    orderDetailsModel.setSettlement_type(DictionaryConst.WhetherMaven.WHETHER_OK);
                                    orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                                    orderDetailsModel = null;
                                });
                                //结算定金
                                Integer payType = MapUtils.getInteger(order, "pay_type");
                                Integer isPay   = MapUtils.getInteger(order, "is_pay");
                                if (payType.equals(PreSellRecordModel.DEPOSIT) && isPay.equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                                {
                                    BigDecimal         totalAmount        = new BigDecimal(MapUtils.getString(order, "deposit"));
                                    MchModel           mchModel           = mchModelMapper.selectByPrimaryKey(goods.getMch_id());
                                    MchAccountLogModel mchAccountLogModel = new MchAccountLogModel();
                                    mchAccountLogModel.setStore_id(storeId);
                                    mchAccountLogModel.setMch_id(String.valueOf(mchModel.getId()));
                                    mchAccountLogModel.setRemake(sNo);
                                    mchAccountLogModel.setPrice(totalAmount);
                                    mchAccountLogModel.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_INCOME);
                                    mchAccountLogModel.setAccount_money(mchModel.getAccount_money());
                                    mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_ORDER);
                                    mchAccountLogModel.setAddtime(new Date());
                                    mchAccountLogModelMapper.insertSelective(mchAccountLogModel);
                                    mchModelMapper.settlementMchCash(mchModel.getStore_id(), mchModel.getId(), totalAmount);
                                    mchModel = null;
                                }
                                orderModel = null;
                                sNo = null;
                            });
                        }
                    }
                    end = null;
                    calendar = null;
                }
                else
                {
                    if (!Objects.isNull(preSellGoodsModel.getDeadline()))
                    {
                        if (DateUtil.dateCompare(new Date(), preSellGoodsModel.getDeadline()))
                        {
                            goods.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                            int count = productListModelMapper.updateByPrimaryKeySelective(goods);
                            XxlJobHelper.log("商品id{},因已过截止时间按被下架 执行结果:{} ", goods.getId(), count > 0);
                            //预售商品所产生的待付款订单全部关闭
                            goodOrder = preSellRecordModelMapper.getGoodOrder(goods.getId());
                            if (goodOrder.size() > 0)
                            {
                                goodOrder.stream().forEach(order ->
                                {
                                    Integer    storeId    = MapUtils.getInteger(order, "store_id");
                                    String     sNo        = MapUtils.getString(order, "sNo");
                                    OrderModel orderModel = new OrderModel();
                                    orderModel.setsNo(sNo);
                                    orderModel = orderModelMapper.selectOne(orderModel);
                                    orderModel.setStatus(7);
                                    orderModel.setSettlement_status(DictionaryConst.WhetherMaven.WHETHER_OK);
                                    orderModelMapper.updateByPrimaryKeySelective(orderModel);
                                    List<Map<String, Object>> orderDetailNotClose = orderDetailsModelMapper.getOrderDetailNotClose(storeId, sNo);
                                    orderDetailNotClose.stream().forEach(orderDetail ->
                                    {
                                        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                                        orderDetailsModel.setId(MapUtils.getInteger(orderDetail, "id"));
                                        orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
                                        orderDetailsModel.setR_status(7);
                                        orderDetailsModel.setSettlement_type(DictionaryConst.WhetherMaven.WHETHER_OK);
                                        orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
                                        orderDetailsModel = null;
                                    });
                                    orderModel = null;
                                });

                            }
                        }
                    }
                }
            }
            XxlJobHelper.log("定时刷新预售商品状态 执行完毕! ");
        }
        catch (Exception e)
        {
            XxlJobHelper.log("定时刷新预售商品状态 异常: " + e.getMessage());
            throw new  LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, e.getMessage());
        }
        finally
        {
            //预售商品结束倒计时
            preSellGoodsModel = null;
            //预售商品所产生的待付款订单全部关闭并且定金结算给店铺
            goodOrder = null;
            productListModelList = null;
            productListModel = null;
        }
    }

    /**
     * 解析京东商品详情带<style></style>标签 【小程序用】
     *
     * @param productContent
     * @return
     */
    public static String getJDProductDetailWithStyle(String productContent)
    {
        if (productContent.contains("</style>"))
        {
            String   html = productContent;
            Document doc  = Jsoup.parse(html);
            Elements divs = doc.select("div.ssd-module"); // 带有href属性的a标签
            for (Element div : divs)
            {
                // 创建新的img元素，并设置src属性为提取到的图片URL
                String  divClass = "." + div.attr("data-id");
                String  css      = getImagseCss(doc, divClass);
                String  imageUrl = css.substring(css.indexOf("url(") + 4, css.indexOf(")"));
                Element img      = doc.createElement("img");
                img.attr("src", "https:" + imageUrl);
                css = css.replaceAll(divClass, "");
                css = css.substring(1, css.length() - 1);
                String[]      styleProperties = css.split(";");
                StringBuilder style           = new StringBuilder();
                for (String property : styleProperties)
                {
                    if (property.contains("width:"))
                    {
                        style.append("width=" + property.substring("width:".length())).append(";");
                    }
                    else if (property.contains("height:"))
                    {
                        style.append("height=" + property.substring("height:".length() + 1)).append(";");
                    }
                    else if (property.contains("background-color:"))
                    {
                        style.append("background-color=" + property.substring("background-color:".length() + 1)).append(";");
                    }
                    else if (property.contains("background-size:"))
                    {
                        style.append("background-size=" + property.substring("background-size:".length() + 1)).append(";");
                    }
                }
                img.attr("style", style.toString());
                div.append(img.toString());
            }
            return doc.head().html() + doc.body().html();
        }
        return productContent;
    }

    /**
     * 获取京东图片样式
     *
     * @param doc
     * @param targetClass
     * @return
     */
    public static String getImagseCss(Document doc, String targetClass)
    {
        Elements styles     = doc.select("style");
        String   classStyle = "";
        Element  style      = styles.first();
        String   cssContent = style.data();
        int      startIndex = cssContent.indexOf(targetClass);
        if (startIndex != -1)
        {
            int endIndex = cssContent.indexOf("}", startIndex);
            if (endIndex != -1)
            {
                classStyle = cssContent.substring(startIndex, endIndex + 1);
            }
        }
        return classStyle;
    }

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;
}

