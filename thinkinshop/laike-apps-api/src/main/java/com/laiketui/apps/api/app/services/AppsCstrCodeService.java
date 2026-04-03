package com.laiketui.apps.api.app.services;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.ShareVo;

import java.util.Map;

/**
 * 二维码接口
 *
 * @author Trick
 * @date 2020/12/17 11:23
 */
public interface AppsCstrCodeService
{


    /**
     * 生成小程序二维码 【需要正式版小程序才可扫描使用】
     * 【php getcodeAction.get_share_qrcode】
     *
     * @param vo    -
     * @param path  - 扫码进入的小程序页面路径
     * @param scene - 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
     * @param id    - 用于图片名称
     * @param width - 图片宽度
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/17 11:32
     */
    String getShareQrcode(MainVo vo, String scene, Integer width, String path, String id) throws LaiKeAPIException;


    /**
     * 店铺分享
     * 【php getcodeAction.share_shop】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/17 15:37
     */
    Map<String, Object> shareShop(MainVo vo, int shopId, String url) throws LaiKeAPIException;


    /**
     * 商品分享
     * 【php getcodeAction.share】
     *
     * @param vo  -
     * @param url -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/18 13:53
     */
    Map<String, Object> goodsShare(ShareVo vo, String url) throws LaiKeAPIException;

    /**
     * 获取小程序二维码参数
     *
     * @param vo  -
     * @param key -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/4/20 20:10
     */
    Map<String, Object> getCodeParameter(MainVo vo, String key) throws LaiKeAPIException;

    /**
     * 获取小程序二维码
     *
     * @param vo  -
     * @param url -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/10 15:35
     */
    Map<String, Object> rqCodeInfo(ShareVo vo, String url) throws LaiKeAPIException;
}
