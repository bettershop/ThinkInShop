package com.laiketui.admins.api.admin.terminal;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.mall.SaveTerminalAppVo;
import com.laiketui.domain.vo.admin.mall.SaveTerminalWeiXinVo;

import java.util.Map;

/**
 * 终端管理接口
 *
 * @author Trick
 * @date 2021/7/23 9:29
 */
public interface AdminTerminalService
{

    /**
     * 终端管理界面
     *
     * @param vo   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/23 9:30
     */
    Map<String, Object> index(MainVo vo, int type) throws LaiKeAPIException;

    /**
     * 保存app配置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/23 18:13
     */
    void saveApp(SaveTerminalAppVo vo) throws LaiKeAPIException;

    /**
     * 保存小程序配置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/26 15:17
     */
    void saveWeiXin(SaveTerminalWeiXinVo vo) throws LaiKeAPIException;
}
