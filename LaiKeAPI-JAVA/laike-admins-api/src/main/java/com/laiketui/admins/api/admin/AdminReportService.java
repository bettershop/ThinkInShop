package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.admin.MchReportVo;

import java.util.Map;

/**
 * 报表管理
 *
 * @author Trick
 * @date 2021/7/5 10:36
 */
public interface AdminReportService
{
    /**
     * 店铺营业额报表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/5 10:37
     */
    Map<String, Object> mchTurnoverReport(MchReportVo vo) throws LaiKeAPIException;

    /**
     * 商户新增用户报表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/5 14:33
     */
    Map<String, Object> mchTurnoverNewUserReport(MchReportVo vo) throws LaiKeAPIException;

    /**
     * 商户订单报表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/5 15:05
     */
    Map<String, Object> mchTurnoverOrderReport(MchReportVo vo) throws LaiKeAPIException;
}
