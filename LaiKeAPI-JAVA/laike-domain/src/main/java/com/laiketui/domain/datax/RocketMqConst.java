package com.laiketui.domain.datax;

public interface RocketMqConst
{

    // 延迟消息 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h (1-18)
    /**
     * 延迟发送时间 2 = 5s
     */
    public static final int DELAY_LEVEL = 2;

    /**
     * 默认发送消息超时时间
     */
    public static final long TIMEOUT = 3000;

    /**
     * 队列名称
     */
    public static final String FLINK_CDC_RMQ_MAIN_TOPIC = "flink-cdc-rmq-main-topic";


    /**
     * Flink mysql 事件类型
     */
    class MySqlEventType
    {
        //读
        public static final String READ   = "READ";
        //增
        public static final String CREATE = "CREATE";
        //删
        public static final String DELETE = "DELETE";
        //改
        public static final String UPDATE = "UPDATE";
    }

    /**
     * FLINK-CDC 监听的表
     */
    class WatchTablesName
    {

        //规格表
        public static final String LKT_CONFIGURE = "lkt_configure";

        //商品主表
        public static final String LKT_PRODUCT_LIST = "lkt_product_list";

        //商品订单表
        public static final String LKT_ORDER = "lkt_order";

        //订单详情表
        public static final String LKT_ORDER_DETAILS = "lkt_order_details";

        //用户表
        public static final String LKT_USER = "lkt_user";

        //购物车表
        public static final String LKT_CART = "lkt_cart";


    }

    class SellOverRedisKeys
    {

        //正价(普通)商品库存
        public static final String PRODUCT_STOCK_KEY  = "PRODUCT_STOCK_";
        //正价(普通)商品状态
        public static final String PRODUCT_STATUS_KEY = "PRODUCT_STATUS_";
        //用户余额key
        public static final String USER_MONEY_KEY     = "USER_MONEY_";
        //购物车Key
        public static final String SHOP_CART_KEY      = "SHOP_CART_";
    }


}
