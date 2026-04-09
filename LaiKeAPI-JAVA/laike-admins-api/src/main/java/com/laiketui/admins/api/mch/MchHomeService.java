package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

/**
 * 首页接口
 *
 * @author Trick
 * @date 2021/5/28 9:58
 */
public interface MchHomeService
{

    /**
     * 店铺首页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 10:11
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取店铺页面信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/2 16:14
     */
    Map<String, Object> info(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取店铺列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/2 16:14
     */
    Map<String, Object> storeList(MainVo vo) throws LaiKeAPIException;

    /**
     * 省市级联
     *
     * @param vo   -
     * @param type -
     * @param sid  -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/11 10:50
     */
    List<AdminCgModel> getRegion(MainVo vo, Integer type, Integer sid) throws LaiKeAPIException;

    /**
     * 设置店铺后台管理-系统语言
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void selectLanguage(MainVo vo) throws LaiKeAPIException;
}
