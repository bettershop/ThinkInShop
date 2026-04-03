package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;

import java.util.Map;

/**
 * h5授权信息接口
 *
 * @author Trick
 * @date 2020/10/9 14:10
 */
public interface AppsCstrJssdkService
{

    /**
     * 获取h5信息
     *
     * @param storeId - 商城id
     * @param url     - 小程序地址
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/9 14:12
     */
    Map<String, String> getData(int storeId, String url) throws LaiKeAPIException;

}
