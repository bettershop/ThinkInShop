package com.laiketui.common.api;

import java.util.List;

/**
 * 报表统计接口
 */
public interface PublicReportService
{

    List<List> getGoodsNumList(int storeid);

}
