package com.laiketui.admins.api.admin.data;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 数据报表接口
 *
 * @author Trick
 * @date 2021/1/12 9:28
 */
public interface AdminDataService
{


    /**
     * 获取新增会员列表
     *
     * @param vo        -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 9:30
     */
    Map<String, Object> getAddUserInfo(MainVo vo, String startDate, String endDate) throws LaiKeAPIException;

    /**
     * 获取新增用户列表
     *
     * @param vo        -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/9 15:14
     */
    Map<String, Object> getAddUserList(MainVo vo, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 会员分布信息
     *
     * @param vo        -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 14:47
     */
    Map<String, Object> getUserDistributionInfo(MainVo vo, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 获取会员消费列表
     *
     * @param vo        -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 15:09
     */
    Map<String, Object> getUserConsumptionInfo(MainVo vo, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 订单报表
     *
     * @param vo        -
     * @param startDate -
     * @param endDate   -
     * @param type      -1=今天 2= 本周 3=本月 4=本年
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 17:18
     */
    Map<String, Object> getOrderReport(MainVo vo, String startDate, String endDate, Integer type) throws LaiKeAPIException;

    interface ReportType
    {
        /**
         * 今天
         */
        Integer TODAY      = 1;
        /**
         * 本周
         */
        Integer THIS_WEEK  = 2;
        /**
         * 本月
         */
        Integer THIS_MONTH = 3;
        /**
         * 本年
         */
        Integer THIS_YEAR  = 4;
    }


    /**
     * 商品报表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @date 2021/1/12 17:55
     * @author Trick
     */
    Map<String, Object> getGoodsReport(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取【商品报表】库存预警商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/8 19:39
     */
    Map<String, Object> getGoodsReportGoodsList(MainVo vo) throws LaiKeAPIException;
}
