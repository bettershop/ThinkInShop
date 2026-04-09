package com.laiketui.admins.admin.services.order;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.laiketui.admins.api.admin.order.AdminIndexService;
import com.laiketui.common.api.*;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.algorithm.DataAlgorithmTool;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.*;
import com.laiketui.domain.file.FileDeliveryModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.MchStoreModel;
import com.laiketui.domain.order.ExpressDeliveryModel;
import com.laiketui.domain.order.OrderConfigModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.virtual.WriteRecordModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelAnalysisVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.mch.FrontDeliveryVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.virtual.WriteRecordVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE;
import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED;
import static com.laiketui.domain.order.OrderModel.ORDER_CLOSE;

/**
 * 功能：管理后台订单管理功能接口
 *
 * @author wangxian
 */
@Service("orderIndexService")
public class AdminIndexServiceImpl implements AdminIndexService
{

    private final Logger logger = LoggerFactory.getLogger(AdminIndexServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> index(AdminOrderListVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (vo.getSelfLifting() == null)
            {
                //特殊订单分类 1=实物订单 2=自提订单(GM) 3=虚拟订单 4=活动订单 ,7=积分,8=秒杀, 9=,10=分销订单 11自提订单(GM + MS) 12:直播订单
                vo.setSelfLifting(1);
            }
            vo.setOperator(String.valueOf(GloabConst.LktConfig.LKT_CONFIG_TYPE_PT));
            resultMap = publicOrderService.pcMchOrderIndex(vo);
            if (vo.getExportType().equals(1))
            {
                exportOrderData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取订单列表 异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> kuaidishow(MainVo vo, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            resultMap = publicOrderService.getLogistics(orderNo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单物流信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "kuaidishow");
        }
        return resultMap;
    }

    //导出订单列表
    private void exportOrderData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"订单编号", "详情id", "创单时间", "产品名称", "规格", "件数", "价格", "订单总计", "数量", "下单类型", "订单状态", "订单类型", "用户ID", "联系人"
                    , "电话", "地址", "支付方式", "物流单号", "运费"};
            //对应字段
            String[] kayList = new String[]{"orderno", "detailId", "createDate", "goodsName", "attrStr", "needNum", "goodsPrice", "orderPrice", "goodsNum", "operationTypeName", "status", "otype", "userId", "userName"
                    , "mobile", "addressInfo", "payName", "courier_num", "detailFreight"};
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("订单列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> orderCount(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            parmaMap.put("storeRecycle", OrderModel.SHOW);
            //统计代发货的订单数量
            Map<String, Object> parmaOrderMap = new HashMap<>(16);
            parmaOrderMap.putAll(parmaMap);
          /*  List<Integer> statusList = new ArrayList<>();
            statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
            parmaOrderMap.put("statusList", statusList);*/
            List<String> list = new ArrayList<>();
            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
//            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_FX);
            parmaOrderMap.put("orderTypeList", list);
            int orderNum = orderModelMapper.countAdminOrderList(parmaOrderMap);
            //统计待发货 实物 订单
            Map<String, Object> parmaMap1 = new HashMap<>(16);
            parmaMap1.putAll(parmaMap);
            parmaMap1.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            parmaMap1.put("self_lifting", DictionaryConst.WhetherMaven.WHETHER_NO);
            parmaMap1.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            int shiWuNum = orderModelMapper.countAdminOrderList(parmaMap1);
            //统计 活动 订单
            Map<String, Object> parmaMap2 = new HashMap<>(16);
            parmaMap2.putAll(parmaMap);
            list = new ArrayList<>();
            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            list.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
            parmaMap2.put("self_lifting", DictionaryConst.WhetherMaven.WHETHER_NO);
            parmaMap2.put("orderTypeList_not", list);
            parmaMap2.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            int activityNum = orderModelMapper.countAdminOrderList(parmaMap2);

            //虚拟商品
            Map<String, Object> parmaMap3 = new HashMap<>(16);
            parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
            parmaMap3.putAll(parmaMap);

            parmaMap3.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_VI);
            int VINum = orderModelMapper.countAdminOrderList(parmaMap3);

            Map<String, Object> parmaMap4 = new HashMap<>(16);
            parmaMap4.putAll(parmaMap);
            parmaMap4.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            parmaMap4.put("self_lifting", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap4.put("status", ORDERS_R_STATUS_DISPATCHED);
            int ziTiNum = orderModelMapper.countAdminOrderList(parmaMap4);

            Map<String, Object> parmaMap5 = new HashMap<>(16);
            parmaMap5.putAll(parmaMap);
            parmaMap5.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            parmaMap5.put("self_lifting", "2");
            parmaMap5.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            int ziPeiNum = orderModelMapper.countAdminOrderList(parmaMap5);
            //统计 退货列表 订单
            int returnNum = returnOrderModelMapper.countOrderReturnWaitByStoreStatus(vo.getStoreId(), DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS, DictionaryConst.OrdersType.ORDERS_HEADER_GM);

            resultMap.put("orderNum", orderNum);
            resultMap.put("shiWuNum", shiWuNum);
            resultMap.put("activityNum", activityNum);
            resultMap.put("returnNum", returnNum);
            resultMap.put("VINum", VINum);
            resultMap.put("ziTiNum", ziTiNum);
            resultMap.put("ziPeiNum", ziPeiNum);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单统计 异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderCount");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> close(AdminOrderVo orderVo) throws LaiKeAPIException
    {
        Map returnMap = Maps.newHashMap();
        try
        {
            AdminModel        adminModel        = RedisDataTool.getRedisAdminUserCache(orderVo.getAccessId(), redisUtil);
            int               storeId           = orderVo.getStoreId();
            String            sNo               = orderVo.getOid();
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setR_status(ORDERS_R_STATUS_CLOSE);
            Map params = Maps.newHashMap();
            params.put("status", ORDER_CLOSE);
            params.put("storeId", storeId);
            params.put("orderno", sNo);
            int row = orderModelMapper.updateOrderInfo(params);
            if (row < 1)
            {
                logger.info(sNo + "修改订单状态失败:");
            }
            int row1 = orderDetailsModelMapper.updateOrderDetailsStatus(storeId, sNo, ORDER_CLOSE);
            logger.info(sNo + "订单关闭:");
            if (row1 < 1)
            {
                logger.info("sNo 修改订单状态失败！");
            }
            orderDetailsModel.setStore_id(storeId);
            orderDetailsModel.setR_sNo(sNo);
            List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetailsModel);
            for (OrderDetailsModel orderDetailsInfo : orderDetailsModels)
            {
                int            pid            = orderDetailsInfo.getP_id();
                int            goodsNum       = orderDetailsInfo.getNum();
                String         attributeId    = orderDetailsInfo.getSid();
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(Integer.valueOf(attributeId));
                confiGureModel.setPid(pid);
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                int totalNum = confiGureModel.getNum();
                row = productListModelMapper.addGoodsStockNum(pid, goodsNum);
                if (row < 1)
                {
                    logger.info("修改商品库存失败！");
                }
                confiGureModel.setNum(goodsNum);
                // addGoodsAttrStockNum 是增量更新，关闭订单时仅恢复可用库存，不改总库存
                confiGureModel.setTotal_num(0);
                row = confiGureModelMapper.addGoodsAttrStockNum(confiGureModel);
                if (row < 1)
                {
                    logger.info("修改商品属性库存失败！");
                }
                String     content    = " 管理员关闭订单【" + orderDetailsInfo.getR_sNo() + "】，返还库存" + goodsNum;
                StockModel stockModel = new StockModel();
                stockModel.setStore_id(storeId);
                stockModel.setProduct_id(pid);
                stockModel.setAttribute_id(Integer.valueOf(attributeId));
                stockModel.setTotal_num(totalNum);
                stockModel.setFlowing_num(goodsNum);
                stockModel.setType(0);
                stockModel.setUser_id(orderDetailsInfo.getUser_id());
                stockModel.setAdd_date(new Date());
                stockModel.setContent(content);
                stockModelMapper.insert(stockModel);

                //记录日志
                publiceService.addAdminRecord(orderVo.getStoreId(), adminModel.getName(), "关闭订单:" + sNo, AdminRecordModel.Type.DEL_ORDER, AdminRecordModel.Source.PC_PLATFORM);
            }
            returnMap.put("code", 200);
            return returnMap;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("关闭订单失败：{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "close");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> del(MainVo vo, String orders) throws LaiKeAPIException
    {
        Map<String, Object> returnMap = Maps.newHashMap();
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            String   adminName = adminModel.getName();
            int      storeId   = vo.getStoreId();
            String[] orderList = orders.split(SplitUtils.DH);
            for (String orderNo : orderList)
            {
                //商城删除订单
                int row1 = orderDetailsModelMapper.storeDelOrderDetails(storeId, orderNo);
                int row2 = orderModelMapper.storeDelOrder(storeId, orderNo);
                if (row1 >= 0 && row2 >= 0)
                {
                    //判断用户、商家、平台是否都已经删除订单
                    publicOrderService.allDelOrder(storeId, orderNo);
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败", "delOrder");
                }
                OrderModel orderModel = new OrderModel();
                orderModel.setStore_id(storeId);
                orderModel.setsNo(orderNo);
                orderModel = orderModelMapper.selectOne(orderModel);
                publiceService.addAdminRecord(vo.getStoreId(), "删除了订单ID：" + orderModel.getsNo() + "的信息", AdminRecordModel.Type.DEL_ORDER, vo.getAccessId());
            }
            return returnMap;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除订单失败：{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    public List<Map<String, Object>> orderPrint(AdminOrderVo orderVo) throws LaiKeAPIException
    {
        Map returnMap = Maps.newHashMap();
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(orderVo.getAccessId(), redisUtil);
            //获取管理后台订单打印配置
            PrintSetupModel printSetupModel = new PrintSetupModel();
            printSetupModel.setStoreId(orderVo.getStoreId());
            printSetupModel.setMchId(0);
            printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
            HashMap<String, Object> printSetupMap = new HashMap<>();
            if (printSetupModel != null)
            {
                printSetupMap.put("printName", printSetupModel.getPrintName());
                printSetupMap.put("printUrl", printSetupModel.getPrintUrl());
                printSetupMap.put("printSheng", printSetupModel.getSheng());
                printSetupMap.put("printShi", printSetupModel.getShi());
                printSetupMap.put("printxian", printSetupModel.getXian());
                printSetupMap.put("printAddress", printSetupModel.getAddress());
                printSetupMap.put("printPhone", printSetupModel.getPhone());
            }
            //订单号
            String                    sNos             = orderVo.getsNo();
            String[]                  orderNos         = sNos.split(SplitUtils.DH);
            List<Map<String, Object>> orderDetailsInfo = new ArrayList<>();
            for (String sNo : orderNos)
            {
                AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
                adminOrderDetailVo.setStoreId(orderVo.getStoreId());
                adminOrderDetailVo.setId(sNo);
                adminOrderDetailVo.setOperationName(adminModel.getName());
                Map<String, Object> tmpOrderDetails = publicOrderService.orderPcDetails(adminOrderDetailVo);
                tmpOrderDetails.putAll(printSetupMap);
                Map tmpMap = Maps.newHashMap();
                tmpMap.put("list", tmpOrderDetails);
                orderDetailsInfo.add(tmpMap);
            }
            return orderDetailsInfo;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("打印订单失败", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderPrint");
        }
    }

    /**
     * 获取快递公司信息
     *
     * @param express
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> searchExpress(String express) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("is_open", 1);
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            if (StringUtils.isNotEmpty(express))
            {
                paramMap.put("kuaidi_name", express);
            }

            int                       total            = expressModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> expressModelList = new ArrayList<>();
            if (total > 0)
            {
                expressModelList = expressModelMapper.selectDynamic(paramMap);
            }
            resultMap.put("total", total);
            resultMap.put("list", expressModelList);
            return resultMap;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取快递信息失败：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "searchExpress");
        }
    }

    @Override
    public Map<String, Object> deliveryView(AdminDeliveryVo adminDeliveryVo) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            int storeId = adminDeliveryVo.getStoreId();
            if (StringUtils.isNotEmpty(adminDeliveryVo.getsNo()))
            {
                String                    sNo        = adminDeliveryVo.getsNo();
                List<Map<String, Object>> ordersList = orderModelMapper.getDeleiveryOrders(storeId, sNo);
                // todo 未知变量
                int                       put             = 1;
                String                    mchId           = "";
                List<Map<String, Object>> returnGoodsList = new ArrayList<>();
                for (Map<String, Object> orderInfo : ordersList)
                {
                    mchId = StringUtils.trim(MapUtils.getString(orderInfo, "mch_id"), SplitUtils.DH);
                    orderInfo.put("imgurl", publicService.getImgPath(MapUtils.getString(orderInfo, "imgurl"), storeId));
                    int rstatus = MapUtils.getIntValue(orderInfo, "r_status");
                    if (rstatus == 1)
                    {
                        put = 0;
                    }
                    orderInfo.put("deliverNum", MapUtils.getIntValue(orderInfo, "num") - MapUtils.getIntValue(orderInfo, "deliver_num"));
                    if (StringUtils.isNotEmpty(DataUtils.getStringVal(ordersList.get(0), "self_lifting")))
                    {
                        retMap.put("self_lifting", DataUtils.getIntegerVal(ordersList.get(0), "self_lifting"));
                    }
                    returnGoodsList.add(orderInfo);
                }
                retMap.put("id", sNo);
                retMap.put("put", put);
                retMap.put("goods", returnGoodsList);
                retMap.put("count", returnGoodsList.size());
                if (StringUtils.isEmpty(mchId))
                {
                    retMap.put("logistics_type", false);
                }
                else
                {
                    retMap.put("logistics_type", publicExpressService.getMchHaveExpressSubtableByMchId(adminDeliveryVo, Integer.valueOf(mchId)));
                }
            }
            retMap.put("express", searchExpress(null));
            return retMap;
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取发货信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deliveryView");
        }
    }

    @Override
    public void deliverySave(MainVo vo,ShipDataVo shippingData) throws LaiKeAPIException
    {
        try
        {
                //1普通发货 2电子面单 3商家配送
                if (shippingData.getType() == 2)
                {
                    FaceSheetSend(vo, shippingData.getExpressId(), JSON.toJSONString(shippingData.getOrderList()));
                }
                else if (shippingData.getType() == 3)
                {
                    adminDeliveryForStoreSelf(vo, shippingData.getPsyInfo().getTel(), shippingData.getPsyInfo().getName(), orderDetailsModelMapper.selectByPrimaryKey(shippingData.getOrderList().get(0).getDetailId()).getR_sNo());
                }
                else
                {
                    // 请选择快递公司
                    if (shippingData.getExpressId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZKDGS, "请选择快递公司");
                    }
                    if (shippingData.getCourierNumber() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRKDDH, "请输入快递单号");
                    }
                    int count = orderDetailsModelMapper.getDeliverNumByExIdAndExNo(shippingData.getExpressId().toString(), shippingData.getCourierNumber());
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KDDHYCZ, "快递单号已存在");
                    }
                    publicOrderService.adminDelivery(vo, shippingData.getExpressId(), shippingData.getCourierNumber(), JSON.toJSONString(shippingData.getOrderList()));
                }
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deliverySave");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeliveryForStoreSelf(MainVo vo, String phone, String courier_name, String sNo) throws LaiKeAPIException
    {
        try
        {
            if (Strings.isNullOrEmpty(courier_name))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_PSYXMBNWK, "配送员姓名不能为空");
            }
            else if (Strings.isNullOrEmpty(phone))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机不能为空");
            }
           /* else if (!MobileUtils.isMobile(phone))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机格式不正确");
            }*/
            //装载参数
            FrontDeliveryVo sendVo = new FrontDeliveryVo();
            sendVo.setStoreId(vo.getStoreId());
            sendVo.setStoreType(vo.getStoreType());
            sendVo.setsNo(sNo);
            sendVo.setPhone(phone);
            sendVo.setCourier_name(courier_name);
            //发货
            publicOrderService.selfSend(sendVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "frontDelivery");
        }
    }

    @Override
    public Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newConcurrentMap();
        try
        {
            // 订单id sno
            String             sNo                = orderVo.getsNo();
            AdminOrderDetailVo adminOrderDetailVo = new AdminOrderDetailVo();
            adminOrderDetailVo.setId(sNo);
            adminOrderDetailVo.setsNo(sNo);
            adminOrderDetailVo.setOrderType("modify");
            adminOrderDetailVo.setType(orderVo.getType());
            adminOrderDetailVo.setStoreId(orderVo.getStoreId());
            Map<String, Object> adminOrderDetailsMap = publicOrderService.orderPcDetails(adminOrderDetailVo);
            List<ExpressModel>  expressModels        = publicExpressServicer.getExpressInfo();
            adminOrderDetailsMap.put("express", expressModels);
            retMap.putAll(adminOrderDetailsMap);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单编辑失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXQHQSB, "订单详情获取失败", "editOrderView");
        }
        return retMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException
    {
        try
        {
            AdminModel userCache = RedisDataTool.getRedisAdminUserCache(orderVo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(orderVo.getOrderNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败");
            }
            publicOrderService.modifyOrder(orderVo);

            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(orderVo.getStoreId());
            orderModel.setsNo(orderVo.getOrderNo());
            orderModel = orderModelMapper.selectOne(orderModel);
            publiceService.addAdminRecord(orderVo.getStoreId(), "修改了订单ID：" + orderModel.getsNo() + "的信息", AdminRecordModel.Type.UPDATE, orderVo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单编辑失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败", "saveEditOrder");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> helpOrder(HelpOrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        row;
            //商品信息
            List<Map<String, Object>> goodsList;
            try
            {
                goodsList = JSON.parseObject(URLDecoder.decode(vo.getProducts(), GloabConst.Chartset.UTF_8), new TypeReference<List<Map<String, Object>>>()
                {
                });
            }
            catch (Exception e)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPCSCW, "商品参数错误");
            }
            if (StringUtils.isEmpty(vo.getAddressId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误,用户收货地址id为null");
            }
            //获取会员信息
            User user = new User();
            user.setStore_id(vo.getStoreId());
            user.setUser_id(vo.getUserId());
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYBCZ, "会员不存在");
            }
            //获取会员收货地址
            UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            if (userAddress == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSHDZ, "请选择收货地址");
            }
            if (StringUtils.isEmpty(vo.getWipeOff()))
            {
                vo.setWipeOff("0");
            }

            OrderModel orderSave = new OrderModel();
            orderSave.setStore_id(vo.getStoreId());
            //生成订单号
            String orderNo = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            orderSave.setsNo(orderNo);
            //生产支付订单号
            String sealNo = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            orderSave.setReal_sno(sealNo);
            //优惠金额
            BigDecimal wipeOff = new BigDecimal(vo.getWipeOff());
            //订单总价
            BigDecimal orderPriceTotal = BigDecimal.ZERO;
            //订单商品总价
            BigDecimal orderGoodsPrice = BigDecimal.ZERO;
            //商品所属店铺
            Set<Integer> goodsMchIds = new HashSet<>();
            //总运费
            BigDecimal zfreight = BigDecimal.ZERO;
            //订单商品个数
            int orderNeedNum = 0;
            //每个商品价格,用于计算平摊
            Map<Integer, BigDecimal> currentPriceMap = new HashMap<>(16);
            //每个商品所有规格运费和
            Map<Integer, BigDecimal> goodsYunFeiMap = new HashMap<>(16);
            for (Map<String, Object> goods : goodsList)
            {
                int needNum = MapUtils.getIntValue(goods, "num");
                //规格id
                int attrId  = MapUtils.getIntValue(goods, "id");
                int goodsId = MapUtils.getIntValue(goods, "pid");
                //所设置的运费
                BigDecimal     freight        = new BigDecimal(MapUtils.getString(goods, "freight"));
                ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                BigDecimal     goodsAmt       = confiGureModel.getPrice().multiply(new BigDecimal(needNum));
                currentPriceMap.put(confiGureModel.getId(), goodsAmt);
                orderGoodsPrice = orderGoodsPrice.add(goodsAmt);
                zfreight = zfreight.add(freight);
                if (goodsYunFeiMap.containsKey(attrId))
                {
                    goodsYunFeiMap.put(attrId, goodsYunFeiMap.get(attrId).add(freight));
                }
                else
                {
                    goodsYunFeiMap.put(attrId, freight);
                }
            }
            //获取商品信息
            for (Map<String, Object> goods : goodsList)
            {
                int        needNum = MapUtils.getIntValue(goods, "num");
                int        attrId  = MapUtils.getIntValue(goods, "id");
                int        goodsId = MapUtils.getIntValue(goods, "pid");
                String     goodsName;
                BigDecimal goodsAttrPrice;
                //手动优惠金额
                BigDecimal manualOffer = BigDecimal.ZERO;

                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("goodsId", goodsId);
                parmaMap.put("attr_id", attrId);
                List<Map<String, Object>> configureList = confiGureModelMapper.getProductListLeftJoinMchDynamic(parmaMap);
                if (configureList == null || configureList.size() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPCSCW, "商品参数错误");
                }
                Map<String, Object> configureMap = configureList.get(0);
                goodsName = MapUtils.getString(configureMap, "product_title");
                //商品单价
                goodsAttrPrice = new BigDecimal(MapUtils.getString(configureMap, "price"));
                //商品总价
                BigDecimal goodsAmt = goodsAttrPrice.multiply(new BigDecimal(needNum));
                //为店铺添加一条购买记录
                MchBrowseModel mchBrowseSave = new MchBrowseModel();
                mchBrowseSave.setStore_id(vo.getStoreId());
                mchBrowseSave.setMch_id(MapUtils.getString(configureMap, "mch_id"));
                mchBrowseSave.setUser_id(user.getUser_id());
                mchBrowseSave.setEvent("购买了商品");
                mchBrowseSave.setAdd_time(new Date());
                mchBrowseModelMapper.insertSelective(mchBrowseSave);
                if (StringUtils.isNotEmpty(mchBrowseSave.getMch_id()))
                {
                    goodsMchIds.add(Integer.valueOf(mchBrowseSave.getMch_id()));
                }
                //计算单个商品价格
                if (BigDecimal.ZERO.compareTo(wipeOff) < 0)
                {
                    //有优惠的情况下均摊优惠
                    manualOffer = DataAlgorithmTool.orderPriceAverage(orderGoodsPrice, currentPriceMap, wipeOff).get(attrId);
                }
                List<Map<String, Object>> list = new ArrayList<>();
                parmaMap = new HashMap<>();
                parmaMap.put("num", needNum);
                list.add(parmaMap);
                parmaMap = new HashMap<>();
                parmaMap.put("pid", goodsId);
                list.add(parmaMap);
                parmaMap = new HashMap<>();
                parmaMap.put("cid", attrId);
                list.add(parmaMap);
                //获取商品运费信息
                List<Map<String, Object>> productsListMap = publiceService.productsList(list, null, 0, DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                if (CollectionUtils.isEmpty(productsListMap))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足2", "settlement");
                }
                BigDecimal goodsYunFei = goodsYunFeiMap.get(attrId);
                //记录订单明细
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(vo.getStoreId());
                orderDetailsModel.setUser_id(user.getUser_id());
                orderDetailsModel.setP_id(goodsId);
                orderDetailsModel.setP_name(goodsName);
                orderDetailsModel.setP_price(goodsAttrPrice);
                orderDetailsModel.setNum(needNum);
                orderDetailsModel.setFreight(goodsYunFei);
                orderDetailsModel.setUnit(MapUtils.getString(configureMap, "unit"));
                orderDetailsModel.setR_sNo(orderNo);
                orderDetailsModel.setAdd_time(new Date());
                orderDetailsModel.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
                orderDetailsModel.setSize(GoodsDataUtils.getProductSku(MapUtils.getString(configureMap, "attribute")));
                orderDetailsModel.setSid(attrId + "");
                orderDetailsModel.setManual_offer(manualOffer);
                orderDetailsModel.setPlatform_coupon_price(manualOffer);
                orderDetailsModel.setAfter_discount(goodsAmt.subtract(manualOffer));
                if (BigDecimal.ZERO.compareTo(orderDetailsModel.getAfter_discount()) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJESRYW, "立减金额输入有误");
                }
                row = orderDetailsModelMapper.insertSelective(orderDetailsModel);
                if (row < 1)
                {
                    logger.debug("添加订单详情失败 代客下单失败");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
                }
                //统计订单总金额
                orderPriceTotal = orderPriceTotal.add(orderDetailsModel.getAfter_discount());
                //库存记录
                AddStockVo addStockVo = new AddStockVo();
                addStockVo.setId(attrId);
                addStockVo.setPid(goodsId);
                addStockVo.setAddNum(-needNum);
                addStockVo.setStoreId(vo.getStoreId());
                addStockVo.setText(String.format("待客(%s)下单,商品出库:%s", user.getUser_id(), needNum));
                publicStockService.addGoodsStock(addStockVo, adminModel.getName());
                //增加销量
                productListModelMapper.updateProductListVolume(needNum, vo.getStoreId(), goodsId);
                //总订单表数量
                orderNeedNum = orderNeedNum + needNum;
            }
            //总运费为0， 更新子订单运费为0
            if (zfreight.compareTo(BigDecimal.ZERO) <= 0)
            {
                row = orderDetailsModelMapper.updateFreightZEROByRsNo(orderNo);
                if (row < 1)
                {
                    logger.debug("总运费为0， 更新子订单运费为0失败 代客下单失败");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
                }
            }
            if (vo.getIsOfflinePayment() == 0)
            {
                orderSave.setPay(DictionaryConst.OrderPayType.OFFLINE_PAY);
            }
            else
            {
                orderSave.setPay(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY);
            }
            //订单金额加总运费
            orderPriceTotal = orderPriceTotal.add(zfreight);
            //记录订单主信息
            orderSave.setUser_id(user.getUser_id());
            orderSave.setName(userAddress.getName());
            orderSave.setMobile(userAddress.getTel());
            orderSave.setNum(orderNeedNum);
            orderSave.setZ_freight(zfreight);
            orderSave.setOld_freight(zfreight);
            orderSave.setManual_offer(new BigDecimal(vo.getWipeOff()));
            orderSave.setZ_price(orderPriceTotal);
            orderSave.setOld_total(orderPriceTotal);
            orderSave.setSheng(userAddress.getSheng());
            orderSave.setShi(userAddress.getCity());
            orderSave.setXian(userAddress.getQuyu());
            orderSave.setAddress(userAddress.getAddress());
            orderSave.setRemark("");
            orderSave.setCoupon_price(wipeOff);
            orderSave.setPreferential_amount(wipeOff);
            orderSave.setManual_offer(wipeOff);
            orderSave.setAdd_time(new Date());
            orderSave.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            orderSave.setSpz_price(orderGoodsPrice);
            orderSave.setSource(Integer.valueOf(DictionaryConst.StoreSource.LKT_LY_013));
            orderSave.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            orderSave.setMch_id(StringUtils.stringImplode(new ArrayList<>(goodsMchIds), SplitUtils.DH, true));
            orderSave.setRemarks("");
            orderSave.setGrade_rate(BigDecimal.ZERO);
            orderSave.setOperation_type(3);
            orderSave.setPay_time(new Date());

            //获取用户的信息和货币的信息
            Integer currencyId = user.getPreferred_currency();
            if (currencyId != null)
            {
                Map userCurrency = currencyStoreModelMapper.getCurrencyInfo(user.getStore_id(), currencyId);
                orderSave.setCurrency_code(MapUtils.getString(userCurrency, "currency_code"));
                orderSave.setCurrency_symbol(MapUtils.getString(userCurrency, "currency_symbol"));
                orderSave.setExchange_rate(new BigDecimal(MapUtils.getDouble(userCurrency, "exchange_rate", 1.0)));
            }
            row = orderModelMapper.insertSelective(orderSave);
            if (row < 1)
            {
                logger.debug("添加订单失败 代客下单失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将用户ID：" + user.getUser_id() + "进行了代客下单", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            //支付
            if (orderSave.getZ_price().compareTo(BigDecimal.ZERO) > 0 && vo.getIsOfflinePayment() == 1)
            {
                publicUserService.userRechargeMoney(vo.getStoreId(), user.getId(), orderSave.getZ_price().negate(), 4, orderNo, "");
            }
            resultMap.put("sNo", orderNo);
            resultMap.put("order_id", orderSave.getId());
            resultMap.put("total", orderSave.getZ_price());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("代客下单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "helpOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> valetOrderSettlement(HelpOrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //商品信息
            List<Map<String, Object>> goodsList;
            try
            {
                goodsList = JSON.parseObject(URLDecoder.decode(vo.getProducts(), GloabConst.Chartset.UTF_8), new TypeReference<List<Map<String, Object>>>()
                {
                });
            }
            catch (Exception e)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPCSCW, "商品参数错误");
            }
            //获取会员信息
            User user = new User();
            user.setStore_id(vo.getStoreId());
            user.setUser_id(vo.getUserId());
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYBCZ, "会员不存在");
            }
            //用户收货地址
            UserAddress userAddress = null;
            if (vo.getAddressId() != null)
            {
                userAddress = commonAddressService.findAddress(vo.getStoreId(), vo.getUserId(), vo.getAddressId());
            }
            //全部选择商品信息
            List<Map<String, Object>> productList = new ArrayList<>();
            //订单商品总价
            BigDecimal orderGoodsPrice = BigDecimal.ZERO;
            //订单商品总价运费
            BigDecimal zfreight = BigDecimal.ZERO;
            //所有商品数量
            Integer allNum = 0;
            //获取商品信息
            for (Map<String, Object> goods : goodsList)
            {
                int        needNum = MapUtils.getIntValue(goods, "num");
                int        attrId  = MapUtils.getIntValue(goods, "id");
                int        goodsId = MapUtils.getIntValue(goods, "pid");
                BigDecimal freight = new BigDecimal(MapUtils.getString(goods, "freight"));
                allNum += needNum;
                //获取商品规格信息
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("goodsId", goodsId);
                parmaMap.put("attr_id", attrId);
                List<Map<String, Object>> configureList = confiGureModelMapper.getProductListLeftJoinMchDynamic(parmaMap);
                if (configureList == null || configureList.size() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPCSCW, "商品参数错误");
                }
                Map<String, Object> configureMap = configureList.get(0);
                //商品单价
                BigDecimal goodsAttrPrice = new BigDecimal(MapUtils.getString(configureMap, "price"));
                //商品总价
                BigDecimal goodsAmt = goodsAttrPrice.multiply(new BigDecimal(needNum));
                orderGoodsPrice = orderGoodsPrice.add(goodsAmt);
                //商品运费
                BigDecimal goodsYunFei = freight;
                //运费为-1须计算商品运费
                if (userAddress != null && freight.equals(new BigDecimal("-1")))
                {
                    List<Map<String, Object>> list = new ArrayList<>();
                    parmaMap = new HashMap<>();
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("num", needNum);
                    list.add(parmaMap);
                    parmaMap = new HashMap<>();
                    parmaMap.put("pid", goodsId);
                    list.add(parmaMap);
                    parmaMap = new HashMap<>();
                    parmaMap.put("cid", attrId);
                    list.add(parmaMap);
                    //获取商品运费信息
                    List<Map<String, Object>> productsListMap = publiceService.productsList(list, null, 0, DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                    if (CollectionUtils.isEmpty(productsListMap))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足2", "settlement");
                    }
                    //按照店铺归类的商品、运费、商品总价等信息
                    Map<String, Object> productsInfo = publiceService.settlementProductsInfo(productsListMap, vo.getStoreId(), DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                    //运费信息
                    Map<String, List<Map<String, Object>>> productsFreight = (Map<String, List<Map<String, Object>>>) productsInfo.get("products_freight");
                    //计算会员的产品价格和订单产品总价
                    List<Map<String, Object>> products = (List<Map<String, Object>>) productsInfo.get("products");
                    //计算会员优惠价格
                    MemberPriceVo memberPriceVo = new MemberPriceVo();
                    memberPriceVo.setUserId(vo.getUserId());
                    memberPriceVo.setStoreId(vo.getStoreId());
                    memberPriceVo.setMchProductList(products);
                    //不计算会员优惠
                    Map<String, Object> memberProductsInfo = publiceService.getMemberPrice(memberPriceVo, 0);
                    //拿出商品信息
                    productsListMap = (List<Map<String, Object>>) memberProductsInfo.get("products");
                    productsInfo = publicOrderService.getFreight(productsFreight, productsListMap, userAddress, vo.getStoreId(), DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                    //商品运费
                    goodsYunFei = new BigDecimal(MapUtils.getString(productsInfo, "yunfei"));

                }
                parmaMap = new HashMap<>();
                parmaMap.put("goodsId", goodsId);
                parmaMap.put("attr_id", attrId);
                parmaMap.put("store_id", vo.getStoreId());
                //查询当前商品信息
                List<Map<String, Object>> list    = confiGureModelMapper.getProductListLeftJoinMchDynamic(parmaMap);
                Map<String, Object>       product = list.get(0);
                product.put("imgurl", publiceService.getImgPath(MapUtils.getString(product, "imgurl"), vo.getStoreId()));
                product.put("attribute", GoodsDataUtils.getProductSkuValue(MapUtils.getString(product, "attribute")));
                if (goodsYunFei.compareTo(BigDecimal.ZERO) < 0)
                {
                    goodsYunFei = BigDecimal.ZERO;
                }
                product.put("freight", goodsYunFei);
                product.put("nums", needNum);
                zfreight = zfreight.add(goodsYunFei);
                productList.add(product);
            }
            //平台包邮设置
            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(vo.getStoreId());
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
            //店铺包邮设置
            Integer        mchId          = customerModelMapper.getStoreMchId(vo.getStoreId());
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(vo.getStoreId());
            mchConfigModel.setMch_id(mchId);
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
            if (orderConfigModal != null && orderConfigModal.getPackage_settings().equals(DictionaryConst.WhetherMaven.WHETHER_OK) && zfreight.compareTo(BigDecimal.ZERO) >= 0)
            {
                // 同单
                int sameOrder = orderConfigModal.getSame_order();
                if (sameOrder <= allNum)
                {
                    zfreight = BigDecimal.ZERO;
                    for (Map<String, Object> product : productList)
                    {
                        product.put("freight", BigDecimal.ZERO);
                    }
                }
            }
            if (mchConfigModel != null && mchConfigModel.getPackage_settings().equals(DictionaryConst.WhetherMaven.WHETHER_OK) && zfreight.compareTo(BigDecimal.ZERO) >= 0)
            {
                // 同单
                int sameOrder = mchConfigModel.getSame_order();
                if (sameOrder <= allNum)
                {
                    zfreight = BigDecimal.ZERO;
                    for (Map<String, Object> product : productList)
                    {
                        product.put("freight", BigDecimal.ZERO);
                    }
                }
            }
            //计算订单金额
            BigDecimal orderPrice = orderGoodsPrice.subtract(new BigDecimal(vo.getWipeOff())).add(zfreight);
            if (BigDecimal.ZERO.compareTo(orderPrice) > 0)
            {
                orderPrice = BigDecimal.ZERO;
            }
            //商品总价
            resultMap.put("goodsPriceTotal", orderGoodsPrice);
            //支付金额
            resultMap.put("payPrice", orderPrice);
            //运费
            resultMap.put("zfreight", zfreight);
            //全部商品信息
            resultMap.put("goosdList", productList);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("代客下单-结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSSB, "计算失败", "valetOrder");
        }
    }

    @Override
    public Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo)
    {
        try
        {
            String sNo = orderVo.getsNo();
            orderVo.setId(sNo);
            orderVo.setsNo(sNo);
            orderVo.setOrderType("see");
            return publicOrderService.orderPcDetails(orderVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单详情异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBJSB, "订单编辑失败", "orderDetailsInfo");
        }
    }

    @Autowired
    private FileDeliveryModelMapper fileDeliveryModelMapper;


    /**
     * 批量发货
     *
     * @param vo    -
     * @param image -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelivery(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException
    {
        try
        {
            AdminModel      user            = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ExcelAnalysisVo excelAnalysisVo = new ExcelAnalysisVo();
            excelAnalysisVo.setFile(image);
            List<String> titleNames = new ArrayList<>();
            titleNames.add("订单编号");
            titleNames.add("订单明细id");
            titleNames.add("物流公司名称");
            titleNames.add("物流单号");
            excelAnalysisVo.setTitleName(titleNames);
            List<String> valueNames = new ArrayList<>();
            valueNames.add("orderNo");
            valueNames.add("detailId");
            valueNames.add("wuName");
            valueNames.add("wuNo");
            excelAnalysisVo.setValueKey(valueNames);
            Map<String, Object>       resultMap     = EasyPoiExcelUtil.analysisExcel(excelAnalysisVo);
            List<Map<String, Object>> excelDataList = DataUtils.cast(resultMap.get("list"));
            if (excelDataList != null)
            {
                //设置回滚点
//                Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
                String                    fileName  = image.get(0).getOriginalFilename();
                StringBuilder             errorText = new StringBuilder();
                OrderDetailsModel         orderDetailsModel;
                List<Map<String,Object>> detailIdList;
                HashMap<String, Object> detailMap;
                int successNum = 0;
                for (Map<String, Object> map : excelDataList)
                {
                    List<ShipDataVo.OrderDetail> orderList = new ArrayList<>();
                    ShipDataVo shipDataVo = new ShipDataVo();
                    ShipDataVo.OrderDetail orderDetail = new ShipDataVo.OrderDetail();
                    int x = MapUtils.getIntValue(map, "x");
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "errorText")))
                    {
                        errorText.append(MapUtils.getString(map, "errorText"));
                    }
                    if (map.containsKey("orderNo"))
                    {
                        //商品发货信息
                        String orderNo = MapUtils.getString(map, "orderNo");
                        //订单信息
                        OrderModel orderModel = new OrderModel();
                        orderModel.setStore_id(vo.getStoreId());
                        orderModel.setsNo(orderNo);
                        orderModel = orderModelMapper.selectOne(orderModel);

                        //发货
                        try
                        {
                            //详情id
                            String detailId = MapUtils.getString(map, "detailId");
                            if (StringUtils.isEmpty(detailId))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDMXBCZ,"订单明细id不存在");
                            }
                            orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(detailId);
                            if (orderDetailsModel == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                            }
                            detailIdList = new ArrayList<>();
                            detailMap = new HashMap<>();
                            detailMap.put("detailId", orderDetailsModel.getId());
                            orderDetail.setDetailId(orderDetailsModel.getId());
                            int deliverNum = 0;
                            if (orderDetailsModel.getDeliverNum() != null)
                            {
                                deliverNum = orderDetailsModel.getDeliverNum();
                            }
                            // 确保不会出现负数
                            int remainingNum = Math.max(0, orderDetailsModel.getNum() - deliverNum);
                            detailMap.put("num", remainingNum);
                            orderDetail.setNum(remainingNum);
                            orderList.add(orderDetail);
                            shipDataVo.setOrderList(orderList);
                            shipDataVo.setType(1);
                            detailIdList.add(detailMap);
                            String  wuName = MapUtils.getString(map, "wuName");
                            Integer wuId   = 0;
                            String  wuNo   = MapUtils.getString(map, "wuNo");
                            shipDataVo.setCourierNumber(wuNo);
                            if (orderDetailsModel.getR_status().equals(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYFHBNJXXG, "订单已发货,不能修改");
                            }
                            if (orderModel == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                            }
                            if (orderModel.getStatus() == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYFHBNJXXG, "订单未付款,不能修改");
                            }
                            if (orderModel.getStatus() == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDYFHBNJXXG, "订单已发货,不能修改");
                            }
                            if (orderModel.getSelf_lifting() == 2)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PSDDBKSYPLFH,"配送订单不可使用批量发货");
                            }
                            String       mchId        = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
                            ExpressModel expressModel = new ExpressModel();
                            expressModel.setKuaidi_name(wuName);
                            expressModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                            expressModel = expressModelMapper.selectOne(expressModel);
                            if (expressModel != null)
                            {
                                wuId = expressModel.getId();
                                shipDataVo.setExpressId(wuId);
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZKDGS, "快递公司不存在");
                            }
                            //判断快递公司是否配置了电子面单 -配置了就进行电子面单发货
                            //获取店铺的对应物流电子面单配置
                            ExpressSubtableModel expressSubtableModel = new ExpressSubtableModel();
                            expressSubtableModel.setStoreId(vo.getStoreId());
                            expressSubtableModel.setMchId(Integer.parseInt(mchId));
                            expressSubtableModel.setExpressId(wuId);
                            expressSubtableModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_NO);
                            expressSubtableModel = expressSubtableModelMapper.selectOne(expressSubtableModel);
                            if (expressSubtableModel != null && !StringUtils.isEmpty(expressSubtableModel.getPartnerId()))
                            {
                                FaceSheetSend(vo, wuId, JSON.toJSONString(detailIdList));
                            }
                            else
                            {
                                deliverySave(vo,shipDataVo);
                            }
                        }
                        catch (LaiKeAPIException l)
                        {
                            logger.error("批量发货失败 异常", l);
                            errorText.append(String.format("第%s行 %s", x, l.getMessage())).append(SplitUtils.DH);
                            // 继续处理下一个订单，不退出循环
                            continue;
                        }
                        publiceService.addAdminRecord(vo.getStoreId(), user.getName(), "将订单ID：" + orderModel.getsNo() + "进行了发货", AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_PLATFORM);
                    }
                    successNum++;
                }

                //记录
                FileDeliveryModel fileDeliveryModel = new FileDeliveryModel();
                fileDeliveryModel.setId(BuilderIDTool.getSnowflakeId() + "");
                fileDeliveryModel.setName(fileName);
                fileDeliveryModel.setAdd_date(new Date());
                fileDeliveryModel.setType(1);
                fileDeliveryModel.setOrderNum(successNum);
                int status = 1;
                if (StringUtils.isNotEmpty(errorText))
                {
                    status = 0;
                    fileDeliveryModel.setText(errorText.toString());
                }
                fileDeliveryModel.setStatus(status);
                fileDeliveryModel.setMch_id(user.getShop_id());
                fileDeliveryModelMapper.insertSelective(fileDeliveryModel);
                return status == 1;
            }
            return false;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单发货列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchDelivery");
        }
    }

    /**
     * 发货列表
     *
     * @param vo        -
     * @param fileName  -
     * @param status    -
     * @param startDate -
     * @param endDate   -
     * @param response
     * @return Map
     * @throws LaiKeAPIException-
     */
    @Override
    public Map<String, Object> deliveryList(MainVo vo, String fileName, Integer status, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("mchId", user.getShop_id());
            paramMap.put("name", fileName);
            paramMap.put("status", status);
            paramMap.put("startDate", startDate);
            paramMap.put("endDate", endDate);
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("type", "1");
            int                       total      = fileDeliveryModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (total > 0)
            {
                resultList = fileDeliveryModelMapper.selectDynamic(paramMap);
            }
            resultList.stream().forEach(stringObjectMap ->
            {
                if ((int) stringObjectMap.get("status") == 0)
                {
                    stringObjectMap.put("statusName", "发货失败");
                }
                else
                {
                    stringObjectMap.put("statusName", "发货成功");
                }
            });
            if (vo.getExportType() == 1)
            {
                exportBulkDeliveryData(resultList, response);
                return null;
            }
            resultMap.put("total", total);
            resultMap.put("list", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货列表 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerList");
        }
        return resultMap;
    }

    //导出批量发货记录
    private void exportBulkDeliveryData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"文件ID", "文件名称", "文件状态", "批量发货时间", "失败原因"};
            //对应字段
            String[] kayList = new String[]{"id", "name", "statusName", "add_date", "text"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("批量发货记录表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delDelivery(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            AdminModel        user              = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            FileDeliveryModel fileDeliveryModel = fileDeliveryModelMapper.selectByPrimaryKey(id);
            if (fileDeliveryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在");
            }
            int row = fileDeliveryModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了文件id：" + id + " 的批量订单发货记录的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除发货记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerList");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> verificationExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id, Integer pid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());

            parmaMap.put("id", orderId);
            parmaMap.put("supplierPro", "supplierPro");
            List<Map<String, Object>> orderInfo = orderModelMapper.getOrdersNumDynamic(parmaMap);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(orderInfo))
            {
                Map<String, Object> map = orderInfo.get(0);
                //订单号
                String orderno = map.get("sNo").toString();
                //订单状态
                int status = Integer.parseInt(map.get("status").toString());
                //订单金额
                BigDecimal orderAmt = new BigDecimal(map.get("z_price").toString());
                //积分
                BigDecimal allow = new BigDecimal("0");
                String     mchId = StringUtils.trim(MapUtils.getString(map, "mch_id"), SplitUtils.DH);
                if (map.containsKey("allow"))
                {
                    allow = new BigDecimal(map.get("allow").toString());
                }

                //虚拟商品特殊处理
                if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED || status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED)
                {
                    //取货代码 E9ds5B,1601349348,1601351148
                    String keyCode = MapUtils.getString(map, "extraction_code");
                    if (StringUtils.isEmpty(keyCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHMBCZ, "取货码不存在");
                    }
                    String[] codeStr = keyCode.split(",");
                    if (codeStr.length < 3 || StringUtils.isEmpty(codeStr[0]) || !StringUtils.isInteger(codeStr[2]))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                    }
                    //验证码
                    String code = codeStr[0];
                    //失效时间
                    long endTime = Long.parseLong(codeStr[2]);
                    //系统时间
                    long sysTime = Long.parseLong(DateUtil.timeStamp());
                    if (sysTime > endTime)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMYSX, "验证码已失效");
                    }
                    if (!code.equals(extractionCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
                    }

                    //标记订单已完成
                    parmaMap.clear();
                    parmaMap.put("orderno", orderno);

                    //订单详情的更新参数
                    Map<String, Object> detailParmaMap = new HashMap<>();
                    //虚拟订单特殊处理
                    int write_off_num = 0;
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                        /*ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                        returnOrderModel.setsNo(orderno);
                        List<ReturnOrderModel> select = returnOrderModelMapper.select(returnOrderModel);
                        if (select.size() > 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_HXCSYW, "核销次数已用完");
                        }*/
                        //核销记录录入
                        WriteRecordModel writeRecordModel = new WriteRecordModel();
                        //待核销次数
                        write_off_num = MapUtils.getIntValue(map, "write_off_num");
                        //已预约核销次数
                        int after_write_off_num = MapUtils.getIntValue(map, "after_write_off_num");
                        if (write_off_num <= 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_HXCSYW, "核销次数已用完");
                        }
                        //更新订单详情中的核销次数
                        detailParmaMap.put("write_off_num", write_off_num - 1);
                        detailParmaMap.put("after_write_off_num", after_write_off_num + 1);

                        //多商品核销处理
                        if (orderInfo.size() > 1)
                        {
                            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                            orderDetailsModel.setR_sNo(orderno);
                            orderDetailsModel.setP_id(pid);
                            OrderDetailsModel orderDetailsModel_result = orderDetailsModelMapper.selectOne(orderDetailsModel);
                            writeRecordModel.setP_id(orderDetailsModel_result.getId());
                            //详单中对于具体哪个商品来进行处理
                            detailParmaMap.put("p_id", pid);
                            //订单表中当详单中所有的商品都核销完后再变成完成状态
                            Integer num = 0;
                            for (Map<String, Object> o_map : orderInfo)
                            {
                                num += DataUtils.getIntegerVal(o_map, "write_off_num");
                            }
                            if (num > 1)
                            {
                                parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
                            }
                            else
                            {
                                parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                                parmaMap.put("arrive_time", new Date());
                            }
                        }
                        else
                        {
                            writeRecordModel.setP_id(MapUtils.getInteger(map, "dId"));
                            if (write_off_num - 1 < 1)
                            {
                                parmaMap.put("arrive_time", new Date());
                                detailParmaMap.put("arrive_time", new Date());
                                parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                            }
                        }

                        //添加核销记录
                        writeRecordModel.setS_no(orderno);
                        writeRecordModel.setWrite_code(extractionCode);
                        writeRecordModel.setWrite_time(new Date());
                        //如果是线下核销且无需预约，则使用核销时用户选择的门店信息，有预约的详单表中有数据
                        if (MapUtils.getIntValue(map, "is_appointment") == ProductListModel.IS_APPOINTMENT.noOpin)
                        {
                            writeRecordModel.setWrite_store_id(mch_store_id);
                        }
                        else
                        {
                            writeRecordModel.setWrite_store_id(DataUtils.getIntegerVal(map, "mch_store_write_id"));
                        }

                        if (DataUtils.getIntegerVal(detailParmaMap, "write_off_num") > 0)
                        {
                            writeRecordModel.setStatus(WriteRecordModel.status.continueWrite);
                        }
                        else
                        {
                            writeRecordModel.setStatus(WriteRecordModel.status.isWrite);
                        }
                        int i = writeRecordModelMapper.insertSelective(writeRecordModel);
                        if (i < 1)
                        {
                            logger.info("添加核销记录失败 参数:" + com.alibaba.fastjson2.JSON.toJSONString(writeRecordModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                        }
                    }
                    //订单的状态处理
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")) && DataUtils.getIntegerVal(detailParmaMap, "write_off_num") > 0)
                    {
                        detailParmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
                    }
                    else
                    {
                        detailParmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                    }
                    //平台端算自营店，店主核销
                    parmaMap.put("VerifiedBy_type", 1);
                    //4154 【JAVA开发环境】秒杀：管理后台--自提订单--核销，提示核销成功，但是页面状态没有刷新，要收到刷新才会变更状态
                    parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                    parmaMap.put("arrive_time", new Date());
                    int count = orderModelMapper.updateByOrdernoDynamic(parmaMap);
                    if (count < 1)
                    {
                        logger.info("订单标记已完成失败 参数:" + com.alibaba.fastjson2.JSON.toJSONString(parmaMap));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                    //标记明细状态
                    parmaMap.clear();
                    detailParmaMap.put("orderno", orderno);
                    //detailParmaMap.put("arrive_time", new Date());

                    count = orderDetailsModelMapper.updateByOrdernoDynamic(detailParmaMap);
                    if (count < 1)
                    {
                        logger.info("订单标记已完成失败 参数:" + com.alibaba.fastjson2.JSON.toJSONString(detailParmaMap));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                    //卖家收入,虚拟商品需要等到全部核销次数完后才记录
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                        if (write_off_num - 1 < 1)
                        {
                            publicMchService.clientConfirmReceipt(vo.getStoreId(), Integer.parseInt(mchId), orderno, orderAmt, allow);
                        }
                    }
                    else
                    {
                        publicMchService.clientConfirmReceipt(vo.getStoreId(), Integer.parseInt(mchId), orderno, orderAmt, allow);
                    }
                    //确认收货 普通商品赠送积分(只有普通订单可以自提，if判断可以不用)
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(MapUtils.getString(map, "otype")))
                    {
                        //如果订单之前已经获得了一笔积分则不再获取
                        SignRecordModel signRecordCount = new SignRecordModel();
                        signRecordCount.setStore_id(vo.getStoreId());
                        signRecordCount.setUser_id(MapUtils.getString(map, "user_id"));
                        signRecordCount.setsNo(orderno);
                        if (signRecordModelMapper.selectCount(signRecordCount) == 0)
                        {
                            publicMemberService.memberSettlement(vo.getStoreId(), MapUtils.getString(map, "user_id"),
                                    orderno, new BigDecimal(MapUtils.getString(map, "z_price", "0")), IntegralConfigModel.GiveStatus.RECEIVING);
                        }
                        else
                        {
                            logger.debug("订单{}已经获得过一笔【会员购物积分】积分,此次不获嘚积分.", orderno);
                        }
                    }
                    resultMap.put("sNo", orderno);
                    resultMap.put("p_price", orderAmt);

                    publiceService.addAdminRecord(vo.getStoreId(), "核销了订单ID：" + orderno, AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    //返回用户和商品信息
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                        Map<String, Object> map1 = orderInfo.get(0);
                        //用户信息
                        resultMap.put("mobile", MapUtils.getString(map1, "mobile"));
                        resultMap.put("name", MapUtils.getString(map1, "name"));

                        parmaMap.put("store_id", vo.getStoreId());
                        parmaMap.put("user_id", MapUtils.getString(orderInfo.get(0), "user_id"));
                        parmaMap.put("id", orderId);
                        List<Map<String, Object>> orderCodesInfo = orderModelMapper.seeExtractionCode(parmaMap);
                        //查询预约门店和预约时间
                        parmaMap.clear();
                        parmaMap.put("s_no", orderno);
                        List<Map<String, Object>> writeRecords = writeRecordModelMapper.selectBySno(parmaMap);
                        if (writeRecords != null && writeRecords.size() > 0)
                        {
                            Map<String, Object> writeRecord = writeRecords.get(0);
                            if (!Strings.isNullOrEmpty(DataUtils.getStringVal(writeRecord, "write_time")))
                            {
                                resultMap.put("time", DataUtils.getStringVal(writeRecord, "write_time"));
                                resultMap.put("store_name", DataUtils.getStringVal(writeRecord, "store_name"));
                            }
                        }

                        List<Map<String, Object>> products = new ArrayList<>();
                        for (int i = 0; i < orderCodesInfo.size(); i++)
                        {
                            Map<String, Object> productMap = orderCodesInfo.get(i);
                            // 商品ID
                            int proId = MapUtils.getInteger(productMap, "pid");
                            // 商品规格id
                            int                 productSid = MapUtils.getInteger(productMap, "sid");
                            Map<String, Object> params     = new HashMap<>();
                            params.put("store_id", vo.getStoreId());
                            params.put("sid", productSid);
                            params.put("p_id", proId);
                            List<Map<String, Object>> productInfoList = productListModelMapper.getGoodsTitleAndImg(params);
                            if (CollectionUtils.isEmpty(productInfoList))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeExtractionCode");
                            }
                            Map<String, Object> productInfoMap = productInfoList.get(0);
                            // 商品名称
                            String product_title = DataUtils.getStringVal(productInfoMap, "product_title");
                            // 拼图片路径
                            String              img           = publiceService.getImgPath(DataUtils.getStringVal(productInfoMap, "img"), vo.getStoreId());
                            Map<String, Object> productRetMap = new HashMap<>();
                            productRetMap.put("p_id", proId);
                            productRetMap.put("product_title", product_title);
                            productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                            productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                            productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                            productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                            productRetMap.put("write_off_num", write_off_num);
                            productRetMap.put("img", img);
                            products.add(productRetMap);
                        }
                        resultMap.put("por_list", products);
                        resultMap.put("num", products.size());
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证提货码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationExtractionCode");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> ShippingRecords(GetExpressDeliveryListVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            resultMap = publicExpressService.getExpressDeliveryList(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单发货列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "ShippingRecords");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> expressGetPro(MainVo vo, Integer id, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            resultMap = publicExpressService.getGoodsByExpressDeliveryId(vo, id, name);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
        return resultMap;
    }

    @Override
    public String CancelElectronicWaybill(MainVo vo, Integer id) throws LaiKeAPIException
    {
        String msg;
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            msg = publicOrderService.cancelElectronicSheetDelivery(vo, id);
            ExpressDeliveryModel expressDeliveryModel = expressDeliveryModelMapper.selectByPrimaryKey(id);
            publiceService.addAdminRecord(vo.getStoreId(), "取消了订单ID：" + expressDeliveryModel.getsNo() + " 的面单打印", AdminRecordModel.Type.DEL_ORDER, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("取消电子面单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
        return msg;
    }

    @Override
    public void FaceSheetSend(MainVo vo, Integer exId, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicOrderService.electronicSheetDelivery(vo, exId, orderDetailIds);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发货记录商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressGetPro");
        }
    }

    @Override
    public Map<String, Object> getMchStore(MainVo vo, Integer mchId, Integer pid, String sNo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel                user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            List<ProductListModel>    products = new ArrayList<>();
            List<Map<String, Object>> dataInfo = new ArrayList<>();
            if (Strings.isNullOrEmpty(sNo))
            {
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(pid);
                productListModel.setCommodity_type(ProductListModel.COMMODITY_TYPE.virtual);
                products = productListModelMapper.select(productListModel);
            }
            else
            {
                products = orderDetailsModelMapper.getProductInfoByNO(sNo);
            }
            for (ProductListModel product : products)
            {
                String mchStoreIds = product.getWrite_off_mch_ids();
                // 全部门店都可以核销
                if ("0".equals(mchStoreIds) || StringUtils.isEmpty(mchStoreIds))
                {
                    MchStoreModel mchStoreModel = new MchStoreModel();
                    mchStoreModel.setMch_id(mchId);
                    List<MchStoreModel> list = mchStoreModelMapper.select(mchStoreModel);
                    for (MchStoreModel storeModel : list)
                    {
                        Map<String, Object> map1 = new HashMap<>();
                        String              name = storeModel.getName();
                        //结束时间
                        String address = storeModel.getSheng() + storeModel.getShi() + storeModel.getXian() + storeModel.getAddress();
                        map1.put("name", name);
                        map1.put("address", address);
                        map1.put("id", storeModel.getId());
                        dataInfo.add(map1);
                    }
                }
                else
                {
                    String[]            split             = mchStoreIds.split(SplitUtils.DH);
                    List<String>        ids               = Arrays.asList(split);
                    List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.selectByIds(ids);
                    for (MchStoreModel mchStoreModel : mchStoreModelList)
                    {
                        Map<String, Object> map1 = new HashMap<>();
                        String              name = mchStoreModel.getName();
                        //结束时间
                        String address = mchStoreModel.getSheng() + mchStoreModel.getShi() + mchStoreModel.getXian() + mchStoreModel.getAddress();
                        map1.put("name", name);
                        map1.put("address", address);
                        map1.put("id", mchStoreModel.getId());
                        dataInfo.add(map1);
                    }
                }
                // 使用Stream API和Collectors.toMap进行去重
                Collection<Map<String, Object>> uniqueDataInfo = dataInfo.stream()
                        .collect(Collectors.toMap(
                                map -> map.get("id"), // key为id
                                Function.identity(),  // value为原始的Map
                                (existing, replacement) -> existing // 如果遇到重复的id，保留现有的（第一个遇到的）
                        )).values();

                // 将去重后的集合转换回List
                List<Map<String, Object>> uniqueDataList = new ArrayList<>(uniqueDataInfo);
                resultMap.put("total", uniqueDataList.size());
                resultMap.put("list", uniqueDataList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载编辑我的门店页面数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getWriteRecord(WriteRecordVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("s_no", vo.getSNo());
            List<Map<String, Object>> list     = writeRecordModelMapper.selectDynamic(parmaMap);
            List<Map<String, Object>> dataInfo = new ArrayList<>();
            for (Map<String, Object> map : list)
            {
                Map<String, Object> map1 = new HashMap<>();
                String              name = MapUtils.getString(map, "name");

                map1.put("name", name);
                map1.put("code", MapUtils.getString(map, "write_code"));
                map1.put("time", DateUtil.dateFormate(MapUtils.getString(map, "write_time"), GloabConst.TimePattern.YMDHMS));
                dataInfo.add(map1);
            }
            resultMap.put("list", dataInfo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载编辑我的门店页面数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> testExtractionCode(MainVo vo, Integer orderId, String extractionCode, Integer mch_store_id)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (orderId == null)
            {
                if (extractionCode.contains(SplitUtils.DH))
                {
                    parmaMap.put("extraction_code1", extractionCode);
                }
                else
                {
                    parmaMap.put("extraction_code", extractionCode);
                }
            }
            parmaMap.put("id", orderId);
            parmaMap.put("supplierPro", "supplierPro");
            List<Map<String, Object>> orderInfo = orderModelMapper.getOrdersNumDynamic(parmaMap);
            if (orderId == null && (orderInfo == null || orderInfo.size() < 1))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
            }
            if (orderId != null && (orderInfo == null || orderInfo.size() < 1))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            if (orderInfo != null && orderInfo.size() > 0)
            {
                Map<String, Object> map = orderInfo.get(0);
                //订单号
                String orderno = map.get("sNo").toString();
                //订单状态
                int status = Integer.parseInt(map.get("status").toString());
                //订单金额
                BigDecimal orderAmt = new BigDecimal(map.get("z_price").toString());

                String mchId = StringUtils.trim(MapUtils.getString(map, "mch_id"), SplitUtils.DH);

                //虚拟商品特殊处理
                if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED || status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED)
                {
                    //取货代码 E9ds5B,1601349348,1601351148
                    String keyCode = MapUtils.getString(map, "extraction_code");
                    if (StringUtils.isEmpty(keyCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHMBCZ, "取货码不存在");
                    }
                    String[] codeStr = keyCode.split(",");
                    if (codeStr.length < 3 || StringUtils.isEmpty(codeStr[0]) || !StringUtils.isInteger(codeStr[2]))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                    }
                    //验证码
                    String code = codeStr[0];
                    //失效时间
                    long endTime = Long.parseLong(codeStr[2]);
                    //系统时间
                    long sysTime = Long.parseLong(DateUtil.timeStamp());
                    if (sysTime > endTime)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMYSX, "验证码已失效");
                    }
                    if (!code.equals(extractionCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
                    }
                    //标记订单已完成
                    parmaMap.clear();
                    int write_off_num = MapUtils.getIntValue(map, "write_off_num");
                    if (write_off_num <= 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_HXCSYW, "核销次数已用完");
                    }


                    parmaMap.put("orderno", orderno);
                    parmaMap.put("arrive_time", new Date());
                    resultMap.put("sNo", orderno);
                    resultMap.put("p_price", orderAmt);

                    //返回用户和商品信息
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                        Map<String, Object> map1 = orderInfo.get(0);
                        //用户信息
                        resultMap.put("mobile", MapUtils.getString(map1, "mobile"));
                        resultMap.put("name", MapUtils.getString(map1, "name"));

                        parmaMap.put("store_id", vo.getStoreId());
                        parmaMap.put("user_id", MapUtils.getString(orderInfo.get(0), "user_id"));
                        parmaMap.put("id", orderId);
                        List<Map<String, Object>> orderCodesInfo = orderModelMapper.seeExtractionCode(parmaMap);
                        //查询预约门店和预约时间，当商品为需要预约的才显示预约信息
                        parmaMap.clear();
                        parmaMap.put("s_no", orderno);
                        List<Map<String, Object>> writeRecords = writeRecordModelMapper.selectBySno(parmaMap);
                        if (writeRecords != null && writeRecords.size() > 0)
                        {
                            Map<String, Object> writeRecord = writeRecords.get(0);
                            if (!Strings.isNullOrEmpty(DataUtils.getStringVal(writeRecord, "write_time")))
                            {
                                resultMap.put("time", DataUtils.getStringVal(writeRecord, "write_time"));
                                resultMap.put("store_name", DataUtils.getStringVal(writeRecord, "store_name"));
                            }
                        }

                        List<Map<String, Object>> products = new ArrayList<>();
                        for (int i = 0; i < orderCodesInfo.size(); i++)
                        {
                            Map<String, Object> productMap = orderCodesInfo.get(i);
                            // 商品ID
                            int proId = MapUtils.getInteger(productMap, "pid");
                            // 商品规格id
                            int                 productSid = MapUtils.getInteger(productMap, "sid");
                            Map<String, Object> params     = new HashMap<>();
                            params.put("store_id", vo.getStoreId());
                            params.put("sid", productSid);
                            params.put("p_id", proId);
                            List<Map<String, Object>> productInfoList = productListModelMapper.getGoodsTitleAndImg(params);
                            if (CollectionUtils.isEmpty(productInfoList))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeExtractionCode");
                            }
                            Map<String, Object> productInfoMap = productInfoList.get(0);
                            // 商品名称
                            String product_title = DataUtils.getStringVal(productInfoMap, "product_title");
                            // 拼图片路径
                            String              img           = publiceService.getImgPath(DataUtils.getStringVal(productInfoMap, "img"), vo.getStoreId());
                            Map<String, Object> productRetMap = new HashMap<>();
                            if (mch_store_id != null)
                            {
                                resultMap.put("mch_store_id", mch_store_id);
                                for (Map<String, Object> m : productInfoList)
                                {
                                    //取出商品可核销门店，多门店用，隔开的，为0则为全部门店，33，44，55
                                    String   mchIds = MapUtils.getString(m, "write_off_mch_ids");
                                    String[] split  = mchIds.split(SplitUtils.DH);
                                    for (int j = 0; j < split.length; j++)
                                    {
                                        if (mch_store_id != null && (mch_store_id.toString().equals(split[j]) || "0".equals(split[j])))
                                        {
                                            productRetMap.put("p_id", proId);
                                            productRetMap.put("product_title", product_title);
                                            productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                                            productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                                            productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                                            productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                                            productRetMap.put("write_off_num", write_off_num);
                                            productRetMap.put("img", img);
                                            products.add(productRetMap);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                productRetMap.put("p_id", proId);
                                productRetMap.put("product_title", product_title);
                                productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                                productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                                productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                                productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                                productRetMap.put("write_off_num", write_off_num);
                                productRetMap.put("img", img);
                                products.add(productRetMap);
                            }
                        }
                        resultMap.put("por_list", products);
                        resultMap.put("num", products.size());
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证提货码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationExtractionCode");
        }
        OrderModel orderModel = new OrderModel();
        orderModel.setId(orderId);
        OrderModel result          = orderModelMapper.selectOne(orderModel);
        if (result == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
        }
        String     extraction_code = result.getExtraction_code();
        if (StringUtils.isEmpty(extraction_code))
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHMBCZ, "取货码不存在");
        }
        String[]   split           = extraction_code.split(",");
        if (split.length == 0 || !extractionCode.equals(split[0]))
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delivery(MainVo vo, ShipDataVo shippingData)
    {
        try
        {
            this.deliverySave(vo,shippingData);
        }catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delivery");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExpressDeliveryStatus(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String[] idList = ids.split(SplitUtils.DH);
            for (String id : idList)
            {
                ExpressDeliveryModel expressDeliveryModel = expressDeliveryModelMapper.selectByPrimaryKey(id);
                expressDeliveryModel.setIsStatus(expressDeliveryModel.getIsStatus() ^ 1);
                expressDeliveryModelMapper.updateByPrimaryKeySelective(expressDeliveryModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量修改电子面单状态 异常：{}",e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateExpressDeliveryStatus");
        }
    }

    @Override
    public void overridePrint(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);

            String[] idList = ids.split(SplitUtils.DH);

            for (String id : idList)
            {
                ExpressDeliveryModel expressDeliveryModel = expressDeliveryModelMapper.selectByPrimaryKey(id);
                publicOrderService.overridePrint(configModel,expressDeliveryModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("电子面单复打 异常：{}",e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "overridePrint");
        }
    }


    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private UserBaseMapper           userBaseMapper;
    @Autowired
    private WriteRecordModelMapper   writeRecordModelMapper;
    @Autowired
    private MchStoreWriteModelMapper mchStoreWriteModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private PublicAddressService publicAddressService;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private PublicAdminRecordService publicAdminRecordService;
    @Autowired
    private NoticeModelMapper        noticeModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;
    @Autowired
    private PublicAdminService     publicAdminService;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private PubliceService publicService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private PublicExpressService publicExpressServicer;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicAddressService commonAddressService;

    @Autowired
    private StoreSelfDeliveryModelMapper storeSelfDeliveryModelMapper;

    @Autowired
    private PrintSetupModelMapper printSetupModelMapper;

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private PublicExpressService publicExpressService;

    @Autowired
    private ExpressSubtableModelMapper expressSubtableModelMapper;

    @Autowired
    private ExpressDeliveryModelMapper expressDeliveryModelMapper;
    @Autowired
    private MchStoreModelMapper        mchStoreModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;
}
