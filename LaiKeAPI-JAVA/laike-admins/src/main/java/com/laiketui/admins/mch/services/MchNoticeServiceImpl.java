package com.laiketui.admins.mch.services;

import com.laiketui.admins.api.mch.MchNoticeService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MchNoticeServiceImpl implements MchNoticeService
{
    private final Logger logger = LoggerFactory.getLogger(MchNoticeServiceImpl.class);

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private OnlineMessageModelMapper onlineMessageModelMapper;

    @Override
    public Map<String, Object> noticeList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //订单(待发货)、订单(售后)、订单(提醒发货)、订单(订单关闭)、订单(新订单)、订单(收货)
            int[]               orderTypes = new int[]{1, 2, 3, 4, 5, 6};
            Map<String, Object> orderMap   = new HashMap<>(16);
            //商品(补货)、商品(违规下架)、商品审核(通过/拒绝)
            int[]               goodsTypes = new int[]{9, 15, 18};
            Map<String, Object> goodsMap   = new HashMap<>(16);
            //账单通知 用户提现审核、店铺提现(审核)、
            int[]               checkTypes = new int[]{24, 25};
            Map<String, Object> checkMap   = new HashMap<>(16);
            //获取订单相关的通知
            noticeData(vo.getStoreId(), user.getMchId(), orderTypes, orderMap);
            //获取商品相关的通知
            noticeData(vo.getStoreId(), user.getMchId(), goodsTypes, goodsMap);
            //获取账单相关的通知
            noticeData(vo.getStoreId(), user.getMchId(), checkTypes, checkMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            resultList.add(orderMap);
            resultList.add(goodsMap);
            resultList.add(checkMap);
            resultMap.put("list", resultList);
            //店铺是否有客服未读消息
            int mchOnlineMessageNotRead = 0;
            mchOnlineMessageNotRead = onlineMessageModelMapper.countMchMessageNotRead(vo.getStoreId(), user.getMchId().toString());
            resultMap.put("mchOnlineMessageNotRead", mchOnlineMessageNotRead);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商城消息通知 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeList");
        }
        return resultMap;
    }

    //通知消息结构辅助方法
    private void noticeData(int storeId, int mchId, int[] types, Map<String, Object> resultMap) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> map = new HashMap<>(16);
            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            map.put("store_id", storeId);
            if (mchId != 0)
            {
                map.put("mch_id", mchId);
            }
            map.put("read_or_not", 0);
            int                       totalMain = 0;
            List<Map<String, Object>> mainList  = new ArrayList<>();
            //获取当前订单的通知
            for (int type : types)
            {
                map.put("type", type);
                int total = messageLoggingModalMapper.countDynamic(map);
                totalMain += total;
                List<Map<String, Object>> noticeList     = messageLoggingModalMapper.selectDynamic(map);
                List<Map<String, Object>> noticeListTemp = new ArrayList<>();
                for (Map<String, Object> notice : noticeList)
                {
                    Map<String, Object> noticeMap = new HashMap<>(16);

                    if (MapUtils.getString(notice, "content").contains(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
                    {
                        //订单号
                        Integer orderId = MapUtils.getInteger(notice, "parameter");
                        if (orderId == null)
                        {
                            continue;
                        }
                        OrderModel orderModel = new OrderModel();
                        orderModel.setId(orderId);
                        orderModel = orderModelMapper.selectOne(orderModel);
                        if (orderModel == null)
                        {
                            //删除这种垃圾通知记录
                            MessageLoggingModal messageLoggingModal = new MessageLoggingModal();
                            messageLoggingModal.setId(MapUtils.getInteger(notice, "id"));
                            messageLoggingModalMapper.delete(messageLoggingModal);
                            logger.error("订单id:{}的订单不存在", orderId);
                        }
                        else
                        {
                            //是否自提
                            if (orderModel.getSelf_lifting() != null && orderModel.getSelf_lifting() == 1)
                            {
                                noticeMap.put("self_lifting", 1);
                            }
                            else
                            {
                                noticeMap.put("self_lifting", 0);
                            }
                        }
                    }
                    noticeMap.put("id", MapUtils.getInteger(notice, "id"));
                    if (type == 9)
                    {
                        ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(MapUtils.getInteger(notice, "parameter"));
                        if (confiGureModel == null)
                        {
                            noticeMap.put("parameter", "0");
                            logger.error("规格id:{}的商品不存在", MapUtils.getInteger(notice, "parameter"));
                        }
                        else
                        {
                            noticeMap.put("parameter", confiGureModel.getPid().toString());
                        }
                    }
                    else
                    {
                        noticeMap.put("parameter", MapUtils.getString(notice, "parameter"));

                    }
                    noticeMap.put("toUrl", MapUtils.getString(notice, "to_url"));
                    noticeMap.put("content", MapUtils.getString(notice, "content"));
                    noticeMap.put("is_popup", MapUtils.getString(notice, "is_popup"));
                    noticeMap.put("add_date", DateUtil.dateFormate(MapUtils.getString(notice, "add_date"), GloabConst.TimePattern.YMDHMS));
                    noticeListTemp.add(noticeMap);
                }
                Map<String, Object> noticeMap = new HashMap<>(16);
                noticeMap.put("list", noticeListTemp);
                noticeMap.put("total", total);
                noticeMap.put("type", type);
                mainList.add(noticeMap);
            }
            resultMap.put("list", mainList);
            resultMap.put("total", totalMain);
            resultMap.put("type", Arrays.toString(types));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商城消息通知通知 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void noticeRead(MainVo vo, Integer id, String types) throws LaiKeAPIException
    {
        try
        {
            User                user                = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            String[]            typeList            = null;
            MessageLoggingModal messageLoggingModal = new MessageLoggingModal();
            messageLoggingModal.setStore_id(vo.getStoreId());
            messageLoggingModal.setRead_or_not(DictionaryConst.WhetherMaven.WHETHER_OK);
            if (id != null)
            {
                messageLoggingModal.setId(id);
                messageLoggingModalMapper.updateByPrimaryKeySelective(messageLoggingModal);
                //pc店铺端已读后需要将h5的消息同步已读
                messageLoggingModal = messageLoggingModalMapper.selectOne(messageLoggingModal);
                messageLoggingModalMapper.readH5OrPcMch(vo.getStoreId(), messageLoggingModal.getMch_id(), messageLoggingModal.getContent(), MessageLoggingModal.Type.TYPE_H5MCH_COMMODITY_APPROVAL);
            }
            if (types != null)
            {
                typeList = types.split(SplitUtils.DH);
            }
            if (typeList != null)
            {
                for (String type : typeList)
                {
                    messageLoggingModal.setType(Integer.parseInt(type));
                    messageLoggingModalMapper.noticeRead(vo.getStoreId(), user.getMchId(), type);
                    if (type.equals(MessageLoggingModal.Type.TYPE_MCH_COMMODITY_APPROVAL.toString()))
                    {
                        messageLoggingModalMapper.noticeRead(vo.getStoreId(), user.getMchId(), MessageLoggingModal.Type.TYPE_H5MCH_COMMODITY_APPROVAL.toString());
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
            logger.error("标记消息已读 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeRead");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void noticePopup(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "id不能为空");
            }
            String[] idList = ids.split(SplitUtils.DH);
            int      count  = messageLoggingModalMapper.updateMessLogPopup(idList);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("标记已弹窗 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeRead");
        }
    }

}
