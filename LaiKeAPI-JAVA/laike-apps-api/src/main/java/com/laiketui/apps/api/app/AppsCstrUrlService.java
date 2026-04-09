package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 首页授权接口
 *
 * @author Trick
 * @date 2020/10/9 14:10
 */
public interface AppsCstrUrlService
{

    /**
     * 获取首页授权url
     *
     * @param storeId  - 商城id
     * @param language -
     * @param accessId -
     * @param get      -  参数
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/9 14:12
     */
    Map<String, Object> getUrl(int storeId, String language, String accessId, String get) throws LaiKeAPIException;

    /**
     * 获取在线客服url
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/7 12:12
     */
    Map<String, Object> getServerUrl(MainVo vo) throws LaiKeAPIException;

}
