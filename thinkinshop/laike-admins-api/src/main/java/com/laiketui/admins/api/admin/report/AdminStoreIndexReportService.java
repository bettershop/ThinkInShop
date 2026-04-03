package com.laiketui.admins.api.admin.report;

import java.util.Map;

public interface AdminStoreIndexReportService
{
    /**
     * 首页报表的数据展示---全量
     *
     * @param storeid
     * @return Map<String, Object>
     */
    Map<String, Object> getReportData(Integer storeid);
}
