package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.weixin.AddThridVo;

import java.util.Map;

/**
 * 授权管理
 *
 * @author Trick
 * @date 2021/2/3 16:23
 */
public interface AdminAuthorizeManageService
{
    /**
     * 获取小程序发布列表
     *
     * @param examineStatus -
     * @param releaseStatus -
     * @param appName       -
     * @param pageModel     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/3 17:21
     */
    Map<String, Object> getThirdInfo(Integer examineStatus, Integer releaseStatus, String appName, PageModel pageModel) throws LaiKeAPIException;


    /**
     * 发布小程序代码
     *
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/3 17:22
     */
    boolean release(Integer id) throws LaiKeAPIException;


    /**
     * 获取小程序配置参数信息
     *
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 10:26
     */
    Map<String, Object> getThridParmate(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑小程序配置参数
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 9:21
     */
    boolean addThridParmate(AddThridVo vo) throws LaiKeAPIException;
}
