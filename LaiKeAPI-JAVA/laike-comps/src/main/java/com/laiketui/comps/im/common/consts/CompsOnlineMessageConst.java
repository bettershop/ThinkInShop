package com.laiketui.comps.im.common.consts;


public interface CompsOnlineMessageConst
{

    /**
     * MODULE_NAME: 在线消息
     */
    String MODULE_NAME = "laike-online-message";

    /**
     * 来源
     */
    interface StoreType
    {
        /**
         * 小程序
         */
        String APPLET  = "APPLET_";
        /**
         * APP
         */
        String APP     = "APP_";
        /**
         * pc端商城
         */
        String PcStore = "PcStore_";
        /**
         * H5
         */
        String H5      = "H5_";
        /**
         * pc店铺
         */
        String PcMch   = "PcMch_";
    }

    /**
     * 来源
     */
    interface StoreSource
    {
        /**
         * 小程序
         */
        String LKT_LY_001 = "1";
        /**
         * APP
         */
        String LKT_LY_002 = "11";
        /**
         * pc端
         */
        String LKT_LY_006 = "6";
        /**
         * H5
         */
        String LKT_LY_007 = "2";
        /**
         * pc店铺
         */
        String LKT_LY_008 = "7";

    }

}
