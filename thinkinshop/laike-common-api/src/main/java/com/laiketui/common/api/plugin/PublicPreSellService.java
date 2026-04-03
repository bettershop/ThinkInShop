package com.laiketui.common.api.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.main.RefundVo;

import java.util.Map;

/**
 * 预售公共方法
 *
 * @Author: sunH_
 * @Date: Create in 15:01 2023/8/24
 */

public interface PublicPreSellService
{

    /**
     * 预售退款逻辑
     *
     * @param vo
     * @param orderNo
     * @param isSendNotice
     * @param smsParmaMap
     * @return
     * @throws LaiKeAPIException
     */
    boolean returnAmt(RefundVo vo, String orderNo, boolean isSendNotice, Map<String, String> smsParmaMap) throws LaiKeAPIException;
}
