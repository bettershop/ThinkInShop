package com.laiketui.comps.im.api;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.im.CompsOnlineMessageService;
import com.laiketui.comps.im.common.consts.CompsOnlineMessageConst;
import com.laiketui.comps.im.websocket.CompsWebSocketClusterDispatcher;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.onlinemessage.OnlineMessageModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.onlinemessage.AddOnlineMessageVo;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CompsOnlineMessageServiceImpl implements CompsOnlineMessageService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OnlineMessageModelMapper onlineMessageModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private CompsWebSocketClusterDispatcher webSocketClusterDispatcher;

    @Autowired
    public ThreadPoolTaskExecutor asyncUploadExcelExecutor;

    /**
     * WebSocket消息推送专用线程池
     */
    @Autowired(required = false)
    private ThreadPoolTaskExecutor webSocketMessageExecutor;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Override
    public Map<String, Object> getMessageList(MainVo vo, String userId, String mchId, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //多端登录校验
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            if (userId == null || mchId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            List<Map<String, Object>> newMessageList = new ArrayList<>();
            List<Map<String, Object>> messageList = onlineMessageModelMapper.getMessageListByParams(vo.getStoreId(), userId, mchId, null);
            for (Map<String, Object> map : messageList)
            {
                // 是否为店铺 0 否 1 是
                Integer is_mch_send = DataUtils.getIntegerVal(map, "is_mch_send");
                if (1 == is_mch_send)
                {
                    // 设置图片路径
                    map.put("img", publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId()));
                }
                if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_MCH)
                {
                    String content = MapUtils.getString(map, "content");
                    //判断是否为JSON格式
                    if (content.startsWith("{"))
                    {
                        OrderMsg orderMsg = JSON.parseObject(content, OrderMsg.class);
                        if (Objects.nonNull(orderMsg.getPro()))
                        {
                            Pro pro = orderMsg.getPro();
                            if (StringUtils.isNotEmpty(pro.getId())) continue;
                        }
                    }
                }
                newMessageList.add(map);
            }
            // 设置消息已读
            if (OnlineMessageModel.SEND_TYPE_USER.equals(type))
            {
                //返回用户头像
                User   user       = userBaseMapper.selectByUserIdOne(vo.getStoreId(), userId);
                String headimgurl = user.getHeadimgurl();
                if (headimgurl == null)
                {
                    headimgurl = "";
                }
                resultMap.put("userImg", headimgurl);
                //只用后两个参数，分别为发送者、接收者
                onlineMessageModelMapper.updateIsRead(vo.getStoreId(), mchId, userId);
            }
            else
            {
                onlineMessageModelMapper.updateIsRead(vo.getStoreId(), userId, mchId);
            }
            resultMap.put("list", newMessageList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询在线消息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMessageList");
        }

        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMessage(AddOnlineMessageVo addOnlineMessageVo) throws LaiKeAPIException
    {
        try
        {
            //多端登录校验
            RedisDataTool.isLogin(addOnlineMessageVo.getAccessId(), redisUtil);
            if (addOnlineMessageVo.getIs_mch_send() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }

            if (!addOnlineMessageVo.getIs_mch_send().equals(OnlineMessageModel.SEND_TYPE_USER) && !addOnlineMessageVo.getIs_mch_send().equals(OnlineMessageModel.SEND_TYPE_MCH))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            List<OnlineMessageModel> addList = new ArrayList<>();
            if (addOnlineMessageVo.getImg_list() == null || addOnlineMessageVo.getImg_list().isEmpty())
            {
                String content = addOnlineMessageVo.getContent();
                OrderMsg orderMsg = new OrderMsg();
                orderMsg.setText(content);
                //发送订单消息
                if (Objects.nonNull(addOnlineMessageVo.getOrderId()))
                {
                    OrderModel orderModel = orderModelMapper.selectByPrimaryKey(addOnlineMessageVo.getOrderId());
                    if (orderModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ,"订单不存在","addMessage");
                    }
                    List<Map<String, Object>> orderList = orderModelMapper.getOrderProductInfo(orderModel.getStore_id(), orderModel.getsNo());
                    Map<String, Object> map = orderList.get(0);
                    BigDecimal oldPrice = orderModel.getOld_total().setScale(2, BigDecimal.ROUND_HALF_UP);
                    Order order = new Order();
                    order.setPrice(oldPrice);
                    String img = MapUtils.getString(map, "img");
                    if (StringUtils.isNotEmpty(img))
                    {
                        img = publiceService.getImgPath(img,orderModel.getStore_id());
                    }
                    order.setOrderId(orderModel.getId());
                    String pName = MapUtils.getString(map, "p_name");
                    order.setOrderName(pName);
                    order.setImgUrl(img);
                    order.setNum(orderModel.getNum());
                    order.setOrderNo(orderModel.getsNo());
                    order.setAddTime(DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
                    orderMsg.setOrder(order);
                    orderMsg.setPro(new Pro());
                }
                else if (Objects.nonNull(addOnlineMessageVo.getpId()))
                {
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(addOnlineMessageVo.getpId());

                    if (Objects.isNull(productListModel))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQSPXXSB,"获取商品信息失败","addMessage");
                    }
                    ProductListModel productListParma = new ProductListModel();
                    productListParma.setId(productListModel.getId());
                    productListParma.setStore_id(productListModel.getStore_id());
                    //获取商品最低价格
                    Map<String, BigDecimal> goodsPricMap = productListModelMapper.getGoodsPriceMinAndMax(productListParma);
                    BigDecimal minPrice = goodsPricMap.get("minPrice");
                    Pro pro = new Pro();
                    if (StringUtils.isNotEmpty(productListModel.getCover_map()))
                    {
                        pro.setCover_map(publiceService.getImgPath(productListModel.getCover_map(),productListModel.getStore_id()));
                    }
                    if (StringUtils.isNotEmpty(productListModel.getImgurl()))
                    {
                        pro.setImgurl(publiceService.getImgPath(productListModel.getImgurl(),productListModel.getStore_id()));
                    }
                    pro.setPrice(minPrice.toString());
                    pro.setProduct_title(productListModel.getProduct_title());
                    //付款人数
                    int payPeople = orderDetailsModelMapper.payPeople(productListModel.getStore_id(), productListModel.getId());
                    pro.setPayPeople(payPeople);
                    pro.setId(productListModel.getId());
                    orderMsg.setPro(pro);
                    orderMsg.setOrder(new Order());
                }
                //发送文本消息
                else
                {
                    orderMsg.setOrder(new Order());
                    orderMsg.setPro(new Pro());
                }
                content = JSON.toJSONString(orderMsg);
                OnlineMessageModel onlineMessageModel = getOnlineMessageModel(addOnlineMessageVo, content);
                addList.add(onlineMessageModel);
            }
            else
            {
                //批量发送图片
                for (String img : addOnlineMessageVo.getImg_list())
                {
                    OnlineMessageModel onlineMessageModel = getOnlineMessageModel(addOnlineMessageVo, img);
                    addList.add(onlineMessageModel);
                }
            }

            //添加消息
            onlineMessageModelMapper.insertList(addList);

            // websocket map 对应key
            String key;
            String tokey;
            String fromKey;
            String userId;
            String mchId;
            //用户列表
            String userKey;
            //是否是商店发来的消息
            if (addOnlineMessageVo.getIs_mch_send().equals(OnlineMessageModel.SEND_TYPE_USER))
            {
                userId = addOnlineMessageVo.getSend_id();
                mchId = addOnlineMessageVo.getReceive_id();
//                key = addOnlineMessageVo.getReceive_id() + SplitUtils.DH + addOnlineMessageVo.getReceive_id();
//                tokey = key;
                fromKey = mchId + SplitUtils.DH + mchId;
                userKey = userId + SplitUtils.DH + userId;
            }
            else
            {
                userId = addOnlineMessageVo.getReceive_id();
                mchId = addOnlineMessageVo.getSend_id();
//                key = addOnlineMessageVo.getReceive_id() + SplitUtils.DH + addOnlineMessageVo.getSend_id();
//                tokey = key ;
                fromKey = mchId + SplitUtils.DH + mchId;
                userKey = userId + SplitUtils.DH + userId;
            }

            //消息id集合
            List<Integer> idList = addList.stream().map(OnlineMessageModel::getId).collect(Collectors.toList());
            //查询消息
            List<Map<String, Object>> messageList = onlineMessageModelMapper.getMessageListByParams(addOnlineMessageVo.getStoreId(), userId, mchId, idList);
            //推送发送的最后一条数据
            String finallyMessage = "";
            for (Map<String, Object> map : messageList)
            {
                finallyMessage = MapUtils.getString(map, "content");
                // 是否为店铺 0 否 1 是
                Integer is_mch_send = DataUtils.getIntegerVal(map, "is_mch_send");
                if (OnlineMessageModel.SEND_TYPE_MCH.equals(is_mch_send))
                {
                    map.put("img", publiceService.getImgPath(MapUtils.getString(map, "img"), addOnlineMessageVo.getStoreId()));
                }
                //异步编排多端消息推送，当商家发送消息的时候，会将消息推送到用户的各个端口
                String messageJson = JSON.toJSONString(map);

                // 使用WebSocket专用线程池（如果存在）或Excel上传线程池
                ThreadPoolTaskExecutor executor = webSocketMessageExecutor != null ? webSocketMessageExecutor : asyncUploadExcelExecutor;

                CompletableFuture<Void> AppLetFuture = CompletableFuture.runAsync(() ->
                {
                    // 推送消息->小程序
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.APPLET + fromKey, messageJson);
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.APPLET + userKey, messageJson);
                }, executor).exceptionally(ex ->
                {
                    logger.error("推送消息->小程序失败, fromKey:{}, userKey:{}, error:{}",
                            fromKey, userKey, ex != null ? ex.getMessage() : "unknown");
                    return null;
                });

                CompletableFuture<Void> AppFuture = CompletableFuture.runAsync(() ->
                {
                    // 推送消息->APP
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.APP + fromKey, messageJson);
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.APP + userKey, messageJson);
                }, executor).exceptionally(ex ->
                {
                    logger.error("推送消息->APP失败, fromKey:{}, userKey:{}, error:{}",
                            fromKey, userKey, ex != null ? ex.getMessage() : "unknown");
                    return null;
                });

                CompletableFuture<Void> PcStoreFuture = CompletableFuture.runAsync(() ->
                {
                    // 推送消息->pc端商城
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.PcStore + fromKey, messageJson);
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.PcStore + userKey, messageJson);
                }, executor).exceptionally(ex ->
                {
                    logger.error("推送消息->pc端商城失败, fromKey:{}, userKey:{}, error:{}",
                            fromKey, userKey, ex != null ? ex.getMessage() : "unknown");
                    return null;
                });

                CompletableFuture<Void> H5Future = CompletableFuture.runAsync(() ->
                {
                    // 推送消息->H5
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.H5 + fromKey, messageJson);
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.H5 + userKey, messageJson);
                }, executor).exceptionally(ex ->
                {
                    logger.error("推送消息->H5失败, fromKey:{}, userKey:{}, error:{}",
                            fromKey, userKey, ex != null ? ex.getMessage() : "unknown");
                    return null;
                });

                CompletableFuture<Void> PcMchFuture = CompletableFuture.runAsync(() ->
                {
                    // 推送消息->pc店铺
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.PcMch + fromKey, messageJson);
                    webSocketClusterDispatcher.dispatchToKey(CompsOnlineMessageConst.StoreType.PcMch + userKey, messageJson);
                }, executor).exceptionally(ex ->
                {
                    logger.error("推送消息->pc店铺失败, fromKey:{}, userKey:{}, error:{}",
                            fromKey, userKey, ex != null ? ex.getMessage() : "unknown");
                    return null;
                });

                // 等待全部发送结束，设置超时避免阻塞
                try {
                    CompletableFuture.allOf(AppLetFuture, AppFuture, PcStoreFuture, H5Future, PcMchFuture)
                            .get(5, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.warn("部分消息推送超时, fromKey:{}, userKey:{}", fromKey, userKey);
                }
            }

            // websocket 推送店铺的用户会话列表
            if (OnlineMessageModel.SEND_TYPE_USER.equals(addOnlineMessageVo.getIs_mch_send()))
            {
                List<Map<String, Object>> mchUserList = onlineMessageModelMapper.mchUserList(addOnlineMessageVo.getStoreId(), mchId, userId, null);
                if (mchUserList != null && mchUserList.size() > 0)
                {
                    Map<String, Object> map = mchUserList.get(0);
                    // 设置用户列表在线状态
                    Object user_id = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + map.get("user_id"));
                    if (user_id != null)
                    {
                        map.put("is_online", 1);
                    }
                    //如果用户头像为空默认设置“”
                    String headimgurl = MapUtils.getString(map, "headimgurl");
                    if (null == headimgurl)
                    {
                        map.put("headimgurl", "");
                    }
                    //发送的最后一条信息
                    map.put("content", finallyMessage);
                    // websocket店铺推送当前用户状态信息（按接收方ID跨节点分发）
                    webSocketClusterDispatcher.dispatchToReceiverId(mchId, JSON.toJSONString(map));
                }
            }
            else
            {
                //websocket 推送用户的店铺会话列表
                List<Map<String, Object>> userMchList = onlineMessageModelMapper.getUserMchList(addOnlineMessageVo.getStoreId(), mchId, userId);
                if (userMchList != null && userMchList.size() > 0)
                {
                    Map<String, Object> map = userMchList.get(0);
                    // 设置店铺列表在线状态
                    //h5店铺
                    Object H5Mch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + map.get("user_id"));
                    //pc店铺
                    Object PcMch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_STORE_MCH_FLAG + map.get("user_id"));
                    if (H5Mch != null || PcMch != null)
                    {
                        map.put("is_online", 1);
                    }
                    //如果店铺头像为空默认设置“”
                    String headimgurl = MapUtils.getString(map, "headimgurl");
                    headimgurl = publiceService.getImgPath(headimgurl, addOnlineMessageVo.getStoreId());
                    if (null == headimgurl)
                    {
                        map.put("headimgurl", "");
                    }
                    else
                    {
                        map.put("headimgurl", headimgurl);
                    }
                    map.put("content", finallyMessage);
                    // websocket用户推送当前店铺状态信息（按接收方ID跨节点分发）
                    webSocketClusterDispatcher.dispatchToReceiverId(userId, JSON.toJSONString(map));
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询在线消息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addMessage");
        }
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderMsg{
        private Pro pro;
        private String text;
        private Order order;
    }

    @Data
    public static class Pro{
        private String product_title;
        private String cover_map;
        private String imgurl;
        private String price;
        private Integer payPeople;
        private Integer id;
    }

    @Data
    public static class Order{
        private String orderName;
        private BigDecimal price;
        private Integer num;
        private String orderNo;
        private String addTime;
        private String imgUrl;
        private Integer orderId;
    }


    private OnlineMessageModel getOnlineMessageModel(AddOnlineMessageVo addOnlineMessageVo, String img)
    {
        OnlineMessageModel onlineMessageModel = new OnlineMessageModel();
        onlineMessageModel.setStore_id(addOnlineMessageVo.getStoreId());
        onlineMessageModel.setAdd_date(new Date());
        onlineMessageModel.setSend_id(addOnlineMessageVo.getSend_id());
        onlineMessageModel.setReceive_id(addOnlineMessageVo.getReceive_id());
        onlineMessageModel.setContent(img);
        onlineMessageModel.setIs_mch_send(addOnlineMessageVo.getIs_mch_send());
        // 设置为未读
        onlineMessageModel.setIs_read(0);
        return onlineMessageModel;
    }

    @Override
    public Map<String, Object> mchUserList(MainVo vo, String mchId, String userName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //多端登录校验
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            if (mchId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }

            List<Map<String, Object>> list = onlineMessageModelMapper.mchUserList(vo.getStoreId(), mchId, null, userName);
            Map<String, Object>       mchUserArticleOneMessage;
            // 设置用户列表在线状态
            for (Map<String, Object> map : list)
            {
                String userId  = MapUtils.getString(map, "user_id");
                Object user_id = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + userId);
                if (user_id != null)
                {
                    map.put("is_online", 1);
                }
                //如果用户头像为空默认设置“”
                String headimgurl = MapUtils.getString(map, "headimgurl");
                if (null == headimgurl)
                {
                    map.put("headimgurl", "");
                }
                //获取最近的一条聊天记录
                mchUserArticleOneMessage = onlineMessageModelMapper.getUserMchArticleOneMessage(vo.getStoreId(), mchId, userId);
                if (mchUserArticleOneMessage != null)
                {
                    map.put("content", MapUtils.getString(mchUserArticleOneMessage, "content"));
                    map.put("addTime", getMessageTime(MapUtils.getString(mchUserArticleOneMessage, "add_date")));
                }
                else
                {
                    map.put("content", "");
                    //当前时间 HH:mm
                    map.put("addTime", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.HM));
                }
            }

            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询店铺会话的用户列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchUserList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> userMchList(MainVo vo, String userId, String mchId, String mchName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //多端登录校验
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            if (userId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            List<Map<String, Object>> list = onlineMessageModelMapper.userMchList(vo.getStoreId(), null, userId, mchName);
            Map<String, Object>       userMchArticleOneMessage;
            Map<String, Object>       map  = null;
            //商品详情页进入客服,当前商品的店铺在客服列表的第一个
            boolean IsHaveMchId = false;
            // 设置店铺列表在线状态
            for (int i = 0; i < list.size(); i++)
            {
                map = list.get(i);
                String mch_id = MapUtils.getString(map, "mch_id");
                //h5店铺
                Object H5Mch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + map.get("user_id"));
                //pc店铺
                Object PcMch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_STORE_MCH_FLAG + map.get("user_id"));
                if (H5Mch != null || PcMch != null)
                {
                    map.put("is_online", 1);
                }
                //如果店铺头像为空默认设置“”
                String headimgurl = MapUtils.getString(map, "headimgurl");
                headimgurl = publiceService.getImgPath(headimgurl, vo.getStoreId());
                if (null == headimgurl)
                {
                    map.put("headimgurl", "");
                }
                else
                {
                    map.put("headimgurl", headimgurl);
                }
                //获取最近的一条聊天记录
                userMchArticleOneMessage = onlineMessageModelMapper.getUserMchArticleOneMessage(vo.getStoreId(), mch_id, userId);
                if (userMchArticleOneMessage != null)
                {
                    map.put("content", MapUtils.getString(userMchArticleOneMessage, "content"));
                    map.put("addTime", getMessageTime(MapUtils.getString(userMchArticleOneMessage, "add_date")));
                }
                else
                {
                    map.put("content", "");
                    //当前时间 HH:mm
                    map.put("addTime", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.HM));
                }
                //商品详情页进入客服,当前商品的店铺在客服列表的第一个
                if (StringUtils.isNotEmpty(mchId))
                {
                    if (mchId.equals(mch_id))
                    {
                        IsHaveMchId = true;
                        if (i != 0)
                        {
                            Map<String, Object> objectMap = list.get(0);
                            list.set(0, map);
                            list.set(i, objectMap);
                        }
                    }
                }
            }
            //获取当前聊天店铺信息  ->首次建立连接
            if (!IsHaveMchId && StringUtils.isNotEmpty(mchId))
            {
                Map<String, Object> mchMap   = new HashMap<>();
                MchModel            mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                mchMap.put("name", mchModel.getName());
                mchMap.put("mch_id", String.valueOf(mchModel.getId()));
                mchMap.put("user_id", userId);
                mchMap.put("headimgurl", publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId()));
                //以读
                mchMap.put("no_read_num", 0);
                //是否在线
                mchMap.put("is_online", 0);
                //h5店铺
                Object H5Mch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + mchModel.getUser_id());
                //pc店铺
                Object PcMch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_STORE_MCH_FLAG + mchModel.getUser_id());
                if (H5Mch != null || PcMch != null)
                {
                    mchMap.put("is_online", 1);
                }
                mchMap.put("content", "");
                //当前时间 HH:mm
                mchMap.put("addTime", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.HM));
                //首次建立连接，放在第一位
                list.add(0, mchMap);
            }
            //获取当前聊天店铺信息  ->首次建立连接
/*            if (StringUtils.isNotEmpty(mchId)){
                Map<String, Object> mchMap = new HashMap<>();
                MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                mchMap.put("name", mchModel.getName());
                mchMap.put("mch_id", String.valueOf(mchModel.getId()));
                mchMap.put("user_id", userId);
                mchMap.put("headimgurl", publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId()));
                //以读
                mchMap.put("no_read_num", 0);
                //是否在线
                mchMap.put("is_online", 0);
                //h5店铺
                Object H5Mch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + mchModel.getUser_id());
                //pc店铺
                Object PcMch = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_STORE_MCH + mchModel.getUser_id());
                if (H5Mch != null || PcMch != null) {
                    mchMap.put("is_online", 1);
                }
                mchMap.put("content", "");
                //当前时间 HH:mm
                mchMap.put("addTime",DateUtil.dateFormate(new Date(), GloabConst.TimePattern.HM));
                //首次建立连接，放在第一位
                list.add(0, mchMap);
            }*/
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询用户会话的店铺列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "userMchList");
        }
        return resultMap;
    }

    /**
     * 消息时间特殊处理
     *
     * @param time
     * @return
     */
    private static String getMessageTime(String time)
    {
        //判断时间是否是当天
        // 当前时间
        Date             now = new Date();
        SimpleDateFormat sf  = new SimpleDateFormat(GloabConst.TimePattern.YMD);
        //获取今天的日期
        String nowDay = sf.format(now);
        //对比的时间
        if (sf.format(DateUtil.dateFormateToDate(time, GloabConst.TimePattern.YMD)).equals(nowDay))
        {
            //当天 -> 返回 HH:mm
            return DateUtil.dateFormate(time, GloabConst.TimePattern.HM);
        }
        else
        {
            //非当天-> 返回 MM/dd
            return DateUtil.dateFormate(time, GloabConst.TimePattern.MD1);

        }
    }

    public static void main(String[] args)
    {
        OrderMsg orderMsg = new OrderMsg();
        orderMsg.setText("987");
        orderMsg.setPro(new Pro());
        orderMsg.setOrder(new Order());
        String jsonString = JSON.toJSONString(orderMsg);
        System.out.println(jsonString); // Output: {"pro":{},"order":{},"text":"987"}
    }
}
