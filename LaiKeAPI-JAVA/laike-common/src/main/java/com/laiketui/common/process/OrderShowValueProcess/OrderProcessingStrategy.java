package com.laiketui.common.process.OrderShowValueProcess;

import java.util.Map;

public interface OrderProcessingStrategy
{
    void processOrder(Map<String, Object> resMap, Map<String,Object> params);
}
