package com.laiketui.comps.task.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.api.*;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.comps.api.task.CompsTaskMchService;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.coupon.CouponModal;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.mch.MchStoreWriteModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.CommentsModel;
import com.laiketui.domain.vo.dic.DicVo;
import com.xxl.job.core.context.XxlJobHelper;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * 店铺相关任务实现
 *
 * @author Trick
 * @date 2020/12/14 9:36
 */
@Service
public class CompsTaskMchServiceImpl implements CompsTaskMchService
{

    @Autowired
    private CompsTaskService taskService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private CommentsModelMapper      commentsModelMapper;
    @Autowired
    private MchStoreWriteModelMapper mchStoreWriteModelMapper;
    @Autowired
    private MchModelMapper           mchModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private SupplierOrderFrightModelMapper supplierOrderFrightModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Autowired
    private SupplierAccountLogModelMapper supplierAccountLogModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private PublicCouponService publicCouponService;

    private final Logger logger = LoggerFactory.getLogger(CompsTaskMchServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extractionCodeTask() throws LaiKeAPIException
    {
        List<OrderModel> orderModelList = null;
        try
        {
            XxlJobHelper.log("提取码定时任务 开始执行!");
            //获取全部自提信息
            OrderModel orderModel = new OrderModel();
            orderModel.setSelf_lifting(OrderModel.SELF_LIFTING_PICKED_UP);
            orderModelList = orderModelMapper.select(orderModel);
            for (OrderModel order : orderModelList)
            {
                if (order.getExtraction_code() != null)
                {
                    String[] rewList = order.getExtraction_code().split(",");
                    if (rewList.length < 3)
                    {
                        XxlJobHelper.log("订单{} 自提订单,自提信息格式不完整,跳过! 参数{}", order.getsNo(), JSON.toJSONString(rewList));
                        continue;
                    }
                    String endTime = DateUtil.timeStamp2Date(rewList[2], null);
                    String sysTime = DateUtil.timeStamp2Date(DateUtil.timeStamp(), null);
                    if (!DateUtil.dateCompare(endTime, sysTime))
                    {
                        //生成取货码
                        String              extractionCode = publicMchService.extractionCode();
                        File                file           = QRCode.from(extractionCode).to(ImageType.PNG).withCharset("utf-8").withSize(250, 250).file();
                        InputStream         inputStream    = new FileInputStream(file);
                        MultipartFile       mfile          = new MockMultipartFile(extractionCode, extractionCode + "." + ImageType.PNG.toString(), MediaType.IMAGE_PNG_VALUE, inputStream);
                        List<MultipartFile> files          = new ArrayList<>();
                        files.add(mfile);
                        //上传取回码
                        List<String> list = publiceService.uploadImage(files, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, Integer.parseInt(DictionaryConst.StoreSource.LKT_LY_001), order.getStore_id());
                        if (list.size() > 0)
                        {
                            OrderModel updateOrder = new OrderModel();
                            updateOrder.setId(order.getId());
                            //有效期三十分钟
                            int  second  = 60 * 30;
                            long endDate = DateUtil.getAddDateBySecond(new Date(), second).getTime() / 1000;
                            updateOrder.setExtraction_code(extractionCode);
                            updateOrder.setExtraction_code_img(list.get(0));
                            int count = orderModelMapper.updateByPrimaryKeySelective(updateOrder);
                            if (count < 1)
                            {
                                XxlJobHelper.log("提货码更新失败 订单号:{}", order.getsNo());
                            }
                            else
                            {
                                XxlJobHelper.log("提货码更新成功 订单号:{}", order.getsNo());
                            }
                            updateOrder = null;
                            list = null;
                            files = null;
                            endTime = null;
                            sysTime = null;
                        }
                        else
                        {
                            XxlJobHelper.log("图片上传失败 订单号:{}", order.getsNo());
                        }
                    }
                    rewList = null;
                }
            }
            XxlJobHelper.log("提取码定时任务 执行完毕!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("提取码定时任务 异常: " + e.getMessage());
        }
        finally
        {
            orderModelList = null;
        }
    }

    @Autowired
    private PublicDictionaryService publicDictionaryService;
    @Autowired
    private PublicOrderService      publicOrderService;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remindDeliver() throws LaiKeAPIException
    {
        List<Integer>             mchIdList      = null;
        OrderModel                orderModel     = null;
        List<OrderModel>          orderModelList = null;
        List<DictionaryListModel> showList       = null;
        try
        {
            XxlJobHelper.log("修改买家能否提醒发货 开始执行!");
            //获取所有商城id
            List<Integer> storeIds = taskService.getStoreIdAll();
            DicVo         dicVo    = new DicVo();
            dicVo.setName("订单类型");
            dicVo.setShowChild(true);
            Map<String, Object> showAdrMap = publicDictionaryService.getDictionaryByName(dicVo);
            showList = DataUtils.cast(showAdrMap.get("value"));
            for (Integer storeId : storeIds)
            {
                mchIdList = mchModelMapper.getStoreMchIdList(storeId);
                for (Integer mchId : mchIdList)
                {
                    XxlJobHelper.log("修改买家能否提醒发货 店铺id{} 开始执行!", mchId);
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
                            //间隔时间 - 秒
                            int remind = MapUtils.getInteger(configMap, "remind");
                            orderModel = new OrderModel();
                            orderModel.setStore_id(storeId);
                            orderModel.setDelivery_status(1);
                            orderModel.setOtype(dic.getValue());
                            orderModel.setMch_id(SplitUtils.DH + mchId + SplitUtils.DH);
                            orderModel.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
                            orderModelList = orderModelMapper.select(orderModel);
                            XxlJobHelper.log("商城id{},店铺id{} 一共有{}个订单被提醒发货,每个订单提醒发货间隔{}秒（0代表只提醒一次）", storeId, mchId, orderModelList.size(), remind);
                            for (OrderModel order : orderModelList)
                            {
                                if (order.getRemind() == null)
                                {
                                    continue;
                                }
                                //提醒发货时间间隔
                                Date remindDate = order.getRemind();
                                XxlJobHelper.log("正在处理订单{} 下次提醒时间{}", order.getsNo(), DateUtil.dateFormate(remindDate, GloabConst.TimePattern.YMDHMS));
                                if (remind > 0)
                                {
                                    if (DateUtil.dateCompare(new Date(), remindDate))
                                    {
                                        int count = orderModelMapper.updateDeliveryReset(order.getId(), 0, 0);
                                        XxlJobHelper.log("订单处理完毕 订单提醒发货重置结果:{}", count > 0);
                                    }
                                    else
                                    {
                                        XxlJobHelper.log("该订单无需处理 间隔时间{}", DateUtil.dateFormate(remindDate, GloabConst.TimePattern.YMDHMS));
                                    }
                                }
                                else
                                {
                                    XxlJobHelper.log("该订单无需处理");
                                }
                                remindDate = null;
                            }
                            configMap = null;
                        }
                    }

                }
            }
            XxlJobHelper.log("修改买家能否提醒发货 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.log("修改买家能否提醒发货按钮 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            mchIdList = null;
            orderModel = null;
            orderModelList = null;
            showList = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoGoodComment() throws LaiKeAPIException
    {
        List<Integer> mchIdList = null;
        //获取未评论的订单
        List<Map<String, Object>> orderList          = null;
        List<DictionaryListModel> showList           = null;
        CommentsModel             commentsModel      = null;
        String                    autoCommentContent = null;
        try
        {
            XxlJobHelper.log("系统自动好评 开始执行!");
            List<Integer> storeIdList = taskService.getStoreIdAll();
            DicVo         dicVo       = new DicVo();
            dicVo.setName("订单类型");
            dicVo.setShowChild(true);
            Map<String, Object> showAdrMap = publicDictionaryService.getDictionaryByName(dicVo);
            showList = DataUtils.cast(showAdrMap.get("value"));

            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("================== 商城id={}-开始执行自动好评 ==================", storeId);
                mchIdList = mchModelMapper.getStoreMchIdList(storeId);
                for (Integer mchId : mchIdList)
                {
                    XxlJobHelper.log("============== 店铺id={}-开始执行自动好评 ==============", mchId);
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

                            Integer commentDay = MapUtils.getInteger(configMap, "commentDay");
                            if (commentDay != null && commentDay > 0)
                            {
                                //自动评价内容
                                autoCommentContent = MapUtils.getString(configMap, "autoCommentContent");
                                if (StringUtils.isEmpty(autoCommentContent))
                                {
                                    continue;
                                }
                                //获取未评论的订单
                                orderList = orderDetailsModelMapper.getReceivingCommentsInfo(storeId, dic.getValue(), mchId, commentDay, new Date());
                                for (Map<String, Object> map : orderList)
                                {
                                    int id = MapUtils.getIntValue(map, "id");
                                    //订单号
                                    String orderno = map.get("r_sNo").toString();
                                    //规格id
                                    int sid = Integer.parseInt(map.get("sid").toString());
                                    //买家id
                                    String clientId = map.get("user_id").toString();
                                    //商品id
                                    String goodsId = map.get("goodsId").toString();

                                    XxlJobHelper.log(">>>> 订单{} 未自动评价,系统自动好评 开关状态:{}", orderno);
                                    commentsModel = new CommentsModel();
                                    commentsModel.setStore_id(storeId);
                                    commentsModel.setOid(orderno);
                                    commentsModel.setUid(clientId);
                                    commentsModel.setPid(goodsId);
                                    commentsModel.setAttribute_id(sid);
                                    commentsModel.setSize(MapUtils.getString(map, "size"));
                                    commentsModel.setContent(autoCommentContent);
                                    commentsModel.setCommentType("5");
                                    commentsModel.setAnonymous("0");
                                    commentsModel.setOrder_detail_id(id);
                                    commentsModel.setAdd_time(new Date());
                                    commentsModelMapper.insertSelective(commentsModel);
                                    XxlJobHelper.log(">>>> 已自动评价,评论id:{}", commentsModel.getId());
                                    goodsId = null;
                                    clientId = null;
                                    orderno = null;
                                }
                            }
                            else
                            {
                                XxlJobHelper.log("==== 该店铺不自动评价 ====");
                            }
                            configMap = null;
                        }
                    }
                }

            }
            XxlJobHelper.log("系统自动好评 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("系统自动好评 异常:{} ", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            mchIdList = null;
            //获取未评论的订单
            orderList = null;
            commentsModel = null;
            autoCommentContent = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orderSettlement() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        //获取商城下所有店铺id
        List<Integer>             mchIdList = null;
        List<DictionaryListModel> showList  = null;
        try
        {
            XxlJobHelper.log("商家订单结算 开始执行!");
            storeIdList = taskService.getStoreIdAll();
            DicVo dicVo = new DicVo();
            dicVo.setName("订单类型");
            dicVo.setShowChild(true);
            Map<String, Object> showAdrMap = publicDictionaryService.getDictionaryByName(dicVo);
            showList = DataUtils.cast(showAdrMap.get("value"));
            for (int storeId : storeIdList)
            {
                //获取商城下所有店铺id
                mchIdList = mchModelMapper.getStoreMchIdList(storeId);
                XxlJobHelper.log(">>>>>>>> 开始处理商城id:{} <<<<<<<<", storeId);
                for (int mchId : mchIdList)
                {
                    XxlJobHelper.log("==== 开始处理店铺id:{} ====", mchId);
                    if (showList != null)
                    {
                        for (DictionaryListModel dic : showList)
                        {
                            XxlJobHelper.log("========= 正在处理【{}】插件-结算订单 =========", dic.getCtext());
                            Map<String, Object> configMap = publicOrderService.getOrderConfig(storeId, mchId, dic.getValue().toUpperCase());
                            if (configMap == null)
                            {
                                XxlJobHelper.log("【{}】插件-未开启/未配置 ", dic.getCtext());
                                continue;
                            }
                            if (configMap.containsKey("isSettlement") && !MapUtils.getBooleanValue(configMap, "isSettlement"))
                            {
                                XxlJobHelper.log("【{}】插件-未开启/未配置 ", dic.getCtext());
                                continue;
                            }
                            Map<String, Object> paramMap = new LinkedHashMap<>(16);
                            paramMap.put("storeId", storeId);
                            paramMap.put("mchId", mchId);
                            paramMap.put("orderType", dic.getValue());
                            Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(paramMap));
                            Map<String, Object> resultMap   = httpApiUtils.executeHttpApi("laike.order.task.http.orderSettlement", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                            XxlJobHelper.log("laike.order.task.orderSettlement远程调用返回结果: " + MapUtils.getString(resultMap, "logger"));

                            configMap = null;
                            paramMap = null;
                            paramApiMap = null;
                            resultMap = null;

                        }
                    }
                    XxlJobHelper.log("==== 处理店铺id:{}已完成 ====", mchId);
                }
                XxlJobHelper.log(">>>>>>>> 处理商城id:{}已完成 <<<<<<<<", storeId);
            }
            XxlJobHelper.log("商家订单结算 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("商家订单结算 异常: ", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
            //获取商城下所有店铺id
            mchIdList = null;
            showList = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanVIWrite() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        //获取商城下所有店铺id
        List<Integer> mchIdList = null;
        try
        {
            XxlJobHelper.log("清除虚拟商品需要预约待支付订单 开始执行!");
            storeIdList = taskService.getStoreIdAll();
            for (int storeId : storeIdList)
            {
                //获取商城下所有店铺id
                mchIdList = mchModelMapper.getStoreMchIdList(storeId);
                XxlJobHelper.log(">>>>>>>> 开始处理商城id:{} <<<<<<<<", storeId);
                for (int mchId : mchIdList)
                {
                    XxlJobHelper.log("==== 开始处理店铺id:{} ====", mchId);
                    Map<String, Object> paramMap = new LinkedHashMap<>(16);
                    paramMap.put("storeId", storeId);
                    paramMap.put("mchId", mchId);
                    paramMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_VI);
                    List<Map<String, Object>> orderList = orderModelMapper.cleanVIWrite(paramMap);
                    for (Map<String, Object> map : orderList)
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
                        paramMap.clear();
                        paramMap.put("store_id", storeId);
                        paramMap.put("orderno", orderNo);
                        paramMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                        List<Map<String, Object>> orderDetailsModelList = orderDetailsModelMapper.getOrderDetailByGoodsInfo(paramMap);

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

                        orderNo = null;
                        oType = null;

                    }

                    XxlJobHelper.log("==== 处理店铺id:{}已完成 ====", mchId);
                }
                XxlJobHelper.log(">>>>>>>> 处理商城id:{}已完成 <<<<<<<<", storeId);
            }
            XxlJobHelper.log("清除虚拟商品需要预约待支付订单 执行完毕!");

        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("清除虚拟商品需要预约待支付订单 异常: ", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
            //获取商城下所有店铺id
            mchIdList = null;
        }
    }


}

