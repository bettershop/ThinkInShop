package com.laiketui.apps.mch.services;

import com.laiketui.apps.api.mch.AppsMchNoticeService;
import com.laiketui.common.mapper.MessageLoggingModalMapper;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
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

/**
 * h5店铺消息通知
 * gp
 * 2023-08-31
 */
@Service
public class AppsMchNoticeServiceImpl implements AppsMchNoticeService
{
    private final Logger logger = LoggerFactory.getLogger(AppsMchNoticeServiceImpl.class);

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderModelMapper orderModelMapper;


    @Override
    public Map<String, Object> noticeList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> allMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //订单(待发货)、订单(售后)、订单(提醒发货)、订单(订单关闭)、订单(新订单)、订单(收货)、商品(审核)、商品(补货)、h5店铺保证金拒绝消息通知、h5店铺用户提交竞拍保证金提醒、h5店铺消息通知(您的店铺提现申请提交成功，正在等待管理员审核！)
            List<Integer> orderTypes = Arrays.asList(1, 2, 3, 4, 5, 15, 6, 9, 16, 17, 22, 23);
            //获取订单相关的通知
            noticeData(vo, user.getMchId(), orderTypes, allMap);

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
        return allMap;
    }

    //通知消息结构辅助方法
    private void noticeData(MainVo vo, int mchId, List<Integer> types, Map<String, Object> resultMap) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> map = new HashMap<>(16);
            map.put("store_id", vo.getStoreId());
            map.put("mch_id", mchId);
            map.put("pageStart", vo.getPageNo());
            map.put("pageEnd", vo.getPageSize());
            map.put("orderTypes", types);
            //所有消息
            int allTotal = messageLoggingModalMapper.countDynamic(map);
            map.put("read_or_not", 0);
            //未读消息
            int totalMain = messageLoggingModalMapper.countDynamic(map);


            //获取当前所有通知
            map.remove("read_or_not");
            map.put("readSort", DataUtils.Sort.ASC.toString());
            map.put("add_date_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> noticeList     = messageLoggingModalMapper.selectDynamic(map);
            List<Map<String, Object>> noticeListTemp = new ArrayList<>();
            for (Map<String, Object> notice : noticeList)
            {
                String              typeName  = "";
                Map<String, Object> noticeMap = new HashMap<>(16);
                if (MapUtils.getString(notice, "content").contains(DictionaryConst.OrdersType.ORDERS_HEADER_GM))
                {
                    //订单号
                    Integer    orderId    = MapUtils.getInteger(notice, "parameter");
                    OrderModel orderModel = new OrderModel();
                    orderModel.setId(orderId);
                    List<OrderModel> select = orderModelMapper.select(orderModel);
                    if (select == null || select.size() == 0)
                    {
                        //删除这种垃圾通知记录
                        MessageLoggingModal messageLoggingModal = new MessageLoggingModal();
                        messageLoggingModal.setId(MapUtils.getInteger(notice, "id"));
                        messageLoggingModalMapper.delete(messageLoggingModal);
                        logger.error("订单id:{}的订单不存在", orderId);
                    }
                }
                Integer type = MapUtils.getInteger(notice, "type");
                //通知类型
                switch (type)
                {
                    case 1:
                        typeName = "订单代发货通知";
                        break;
                    case 2:
                        typeName = "售后待处理通知";
                        break;
                    case 3:
                        typeName = "发货提醒通知";
                        break;
                    case 4:
                        typeName = "订单关闭通知";
                        break;
                    case 5:
                        typeName = "新订单通知";
                        break;
                    case 6:
                        typeName = "订单收货通知";
                        break;
                    case 9:
                        typeName = "商品补货通知";
                        break;
                    case 16:
                        typeName = "保证金审核通知";
                        break;
                    case 17:
                        typeName = "商品审核通知";
                        break;
                    case 22:
                        typeName = "用户提交竞拍保证金通知";
                        break;
                    case 23:
                        typeName = "提现审核结果通知";
                        break;
                }
                noticeMap.put("id", MapUtils.getInteger(notice, "id"));
                noticeMap.put("content", MapUtils.getString(notice, "content"));
                noticeMap.put("time", DateUtil.dateFormate(MapUtils.getString(notice, "add_date"), GloabConst.TimePattern.YMDHMS));
//                noticeMap.put("time", MapUtils.getString(notice, "add_date"));
                //以读未读
                noticeMap.put("type", MapUtils.getInteger(notice, "read_or_not") + 1);
                noticeMap.put("title", typeName);
                noticeListTemp.add(noticeMap);
            }
            resultMap.put("message", noticeListTemp);
            resultMap.put("noread", totalMain);
            resultMap.put("total", allTotal);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("h5店铺消息通知通知 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> noticeRead(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> map = new HashMap<>();
        try
        {
            User                user                = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MessageLoggingModal messageLoggingModal = messageLoggingModalMapper.selectByPrimaryKey(id);
            if (StringUtils.isEmpty(messageLoggingModal))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "消息id不存在", "noticeRead");
            }
            //适配h5消息通知结构
            ArrayList<Object>       list    = new ArrayList<>();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("store_id", vo.getStoreId());
            hashMap.put("id", messageLoggingModal.getId());
            hashMap.put("content", messageLoggingModal.getContent());
            hashMap.put("time", messageLoggingModal.getAdd_date());
            String  typeName = "";
            Integer type     = messageLoggingModal.getType();
            //通知类型
            switch (type)
            {
                case 1:
                    typeName = "订单待发货通知";
                    break;
                case 2:
                    typeName = "售后待处理通知";
                    break;
                case 3:
                    typeName = "发货提醒通知";
                    break;
                case 4:
                    typeName = "订单关闭通知";
                    break;
                case 5:
                    typeName = "新订单通知";
                    break;
                case 6:
                    typeName = "订单收货通知";
                    break;
                case 9:
                    typeName = "商品补货通知";
                    break;
                case 16:
                    typeName = "保证金审核通知";
                    break;
                case 17:
                    typeName = "商品审核通知";
                    break;
                case 22:
                    typeName = "用户提交竞拍保证金通知";
                    break;
                case 23:
                    typeName = "提现审核结果通知";
                    break;
            }
            hashMap.put("title", typeName);
            list.add(hashMap);
            map.put("message", hashMap);
            //如果为商品审核，需要将pc店铺的商品审核消息同步已读
            if (MessageLoggingModal.Type.TYPE_H5MCH_COMMODITY_APPROVAL.equals(type))
            {
                messageLoggingModalMapper.readH5OrPcMch(vo.getStoreId(), messageLoggingModal.getMch_id(), messageLoggingModal.getContent(), MessageLoggingModal.Type.TYPE_MCH_COMMODITY_APPROVAL);
            }
            //标记以读
            messageLoggingModal = new MessageLoggingModal();
            messageLoggingModal.setStore_id(vo.getStoreId());
            messageLoggingModal.setRead_or_not(DictionaryConst.WhetherMaven.WHETHER_OK);
            messageLoggingModal.setId(id);
            int i = messageLoggingModalMapper.updateByPrimaryKeySelective(messageLoggingModal);
            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeData");
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
        return map;
    }

    @Override
    public Integer messageNum(MainVo vo)
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //订单(待发货)、订单(售后)、订单(提醒发货)、订单(订单关闭)、订单(新订单)、订单(收货)、商品(审核)、商品(补货)
            List<Integer> orderTypes = Arrays.asList(1, 2, 3, 4, 5, 6, 9, 16, 17, 22, 23);
            //获取订单相关的通知
            return noticeData(vo, user.getMchId(), orderTypes);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺消息通知获取未读消息条数 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeRead");
        }
    }

    //通知消息结构辅助方法
    private Integer noticeData(MainVo vo, int mchId, List<Integer> types) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> map = new HashMap<>(16);
            map.put("store_id", vo.getStoreId());
            map.put("mch_id", mchId);
            map.put("pageStart", vo.getPageNo());
            map.put("pageEnd", vo.getPageSize());
            map.put("orderTypes", types);
            map.put("read_or_not", 0);
            //未读消息
            return messageLoggingModalMapper.countDynamic(map);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("h5店铺消息通知通知 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeData");
        }
    }

    @Override
    public void allMessage(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User          user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            List<Integer> orderTypes = Arrays.asList(1, 2, 3, 4, 5, 6, 9, 16, 17, 18, 22, 23);
            for (Integer type : orderTypes)
            {
                messageLoggingModalMapper.noticeRead(vo.getStoreId(), user.getMchId(), String.valueOf(type));
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("标记全部以读 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMessage(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            User                user                = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MessageLoggingModal messageLoggingModal = new MessageLoggingModal();
            int                 i;
            if (StringUtils.isEmpty(ids))
            {
                messageLoggingModal.setStore_id(vo.getStoreId());
                messageLoggingModal.setMch_id(user.getMchId());
                i = messageLoggingModalMapper.delete(messageLoggingModal);
                if (i < 1)
                {
                    logger.debug("消息id:" + "全部删除失败! mchId" + user.getMchId());
                }
            }
            else
            {
                String[] id = ids.split(SplitUtils.DH);
                messageLoggingModal.setStore_id(vo.getStoreId());
                messageLoggingModal.setMch_id(user.getMchId());
                for (String s : id)
                {
                    messageLoggingModal.setId(Integer.valueOf(s));
                    i = messageLoggingModalMapper.delete(messageLoggingModal);
                    if (i < 1)
                    {
                        logger.debug("消息id:" + s + "删除失败! mchId" + user.getMchId());
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
            logger.error("删除消息通知 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeData");
        }
    }

}
