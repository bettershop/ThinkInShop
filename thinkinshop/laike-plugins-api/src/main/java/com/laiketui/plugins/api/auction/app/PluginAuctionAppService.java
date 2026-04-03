package com.laiketui.plugins.api.auction.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.vo.MainVo;

import java.util.Date;
import java.util.Map;

/**
 * 竞拍主流程
 *
 * @author Trick
 * @date 2022/7/12 16:20
 */
public interface PluginAuctionAppService
{

    /**
     * 竞拍专场
     *
     * @param vo       -
     * @param status   - 1=即将开始 2=竞拍中 3=已结束
     * @param type     - 专场类型 1=店铺专场 2=普通专场 3=报名专场
     * @param isMch    - 是否是店铺专场
     * @param pageType - 页面类型 1.店铺主页
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 16:24
     */
    Map<String, Object> getSpecialList(MainVo vo, Integer status, Integer type, Integer isMch, Integer isMchId, Integer pageType) throws LaiKeAPIException;

    /**
     * 获取专场即将开始页面
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/2 10:53
     */
    Map<String, Object> getSoonInfo(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 根据场次id获取下面的商品
     *
     * @param vo        -
     * @param sessionId - 场次id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 12:11
     */
    Map<String, Object> getSessionGoodsList(MainVo vo, String sessionId) throws LaiKeAPIException;

    /**
     * 获取竞拍商品详情
     *
     * @param vo   -
     * @param acid -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 20:04
     */
    Map<String, Object> getGoodsDetails(MainVo vo, Integer acid) throws LaiKeAPIException;

    /**
     * 获取当前竞拍商品出价记录
     *
     * @param vo   -
     * @param acid -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 20:41
     */
    Map<String, Object> getGoodsOutAmtRecord(MainVo vo, Integer acid) throws LaiKeAPIException;


    /**
     * 获取竞拍配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/13 20:41
     */
    Map<String, Object> getAuctionConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 围观
     *
     * @param vo        -
     * @param specialId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/4 15:05
     */
    void lookSpecial(MainVo vo, String specialId) throws LaiKeAPIException;

    /**
     * 分享专场
     *
     * @param vo         -
     * @param specialId  -
     * @param qrCodePath - 前端路径
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/12 14:47
     */
    Map<String, Object> shareSpecial(MainVo vo, String specialId, String qrCodePath) throws LaiKeAPIException;

    /**
     * 分享商品
     *
     * @param vo         -
     * @param acId       - 竞拍商品id
     * @param qrCodePath -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/16 20:06
     */
    Map<String, Object> shareGoods(MainVo vo, String acId, String qrCodePath) throws LaiKeAPIException;

    public static void main(String[] args)
    {

        long midTime = 1800000;

        DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS);

        long hh = midTime / 60 / 60;
        long mm = midTime / 60 % 60;
        long ss = midTime % 60;
        System.out.println("还剩" + hh + "小时" + mm + "分钟" + ss + "秒");
        System.out.println(hh + "" + mm + "" + ss + "");

    }
}
