package com.laiketui.plugins.api.group.appMch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.order.RefundQueryVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 退货管理
 * <p>
 * gp
 * 2023-07-10
 */
public interface PluginsGroupAppMchRefundService
{

    /**
     * 获取退货列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-04-18 16:09:17
     */
    Map<String, Object> getRefundList(RefundQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取售后详情
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/19 18:59
     */
    Map<String, Object> getRefundById(RefundQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 售后审核 通过/拒绝
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023-04-18 16:09:17
     */
    boolean examine(RefundVo vo) throws LaiKeAPIException;
}
