package com.laiketui.apps.api.app.divaccount;

import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/3/13
 */
public interface AppsCstrLedgerRecordsService
{

    Map<String, Object> queryLedgerRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate);
}
