package com.laiketui.core.lktconst;

/**
 * 默认值类：之所以放在这个地方是因为插件那边可以公用
 */
public interface DefaultImagesValues
{

    /**
     * oos 公共图片 - 店铺分享主图
     */
    public static final String OOS_DEFAULT_IMAGE_NAME_MCH = "https://laikeds.oss-cn-shenzhen.aliyuncs.com/public/shop_bg.png";

    /**
     * oos 公共图片 - 默认底图logo
     */
    public static final String OOS_DEFAULT_IMAGE_NAME_LOGO = "https://laikeds.oss-cn-shenzhen.aliyuncs.com/public/logo.jpg";

    /**
     * oos 公共图片 - 分销分享背景
     */
    public static final String OOS_DEFAULT_SHAREBG = "https://laikeds.oss-cn-shenzhen.aliyuncs.com/public/share_bg.png";

    /**
     * oos 公共图片 - logo
     */
    public static final String OOS_DEFAULT_LOGO = "https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/0/1550673836340.png";

    /**
     * oos 公共图片 - 分享图中的二维码背景
     */
    public static final String OOS_DEFAULT_QRCODE_BACK = "https://laikeds.oss-cn-shenzhen.aliyuncs.com/public/qrcode_back.png";

    /**
     * 带参数对图片进行处理
     */
    public static final String OOS_DEFAULT_IMAGE_NAME_LOGO_ADD = OOS_DEFAULT_IMAGE_NAME_LOGO + "?x-oss-process=image/resize,m_fixed,h_88,w_88";

}
