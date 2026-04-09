package com.laiketui.admins.api.admin.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.sign.AddSignConfigVo;

import java.util.Map;

/**
 * 后台签到接口
 *
 * @author Trick
 * @date 2021/5/11 11:03
 */
public interface AdminSignService
{

    /**
     * 签到列表
     *
     * @param vo       -
     * @param userName -
     * @param userType -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/11 11:16
     */
    Map<String, Object> index(MainVo vo, String userName, Integer userType) throws LaiKeAPIException;

    /**
     * 签到明细
     *
     * @param vo        -
     * @param userId    -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/11 14:28
     */
    Map<String, Object> signDetail(MainVo vo, String userId, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 删除签到记录
     *
     * @param vo     -
     * @param userId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/11 14:50
     */
    Map<String, Object> delSign(MainVo vo, String userId) throws LaiKeAPIException;


    /**
     * 获取签到设置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/11 14:56
     */
    Map<String, Object> signConfigInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑签到配置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/11 16:51
     */
    Map<String, Object> addSignConfig(AddSignConfigVo vo) throws LaiKeAPIException;
}
