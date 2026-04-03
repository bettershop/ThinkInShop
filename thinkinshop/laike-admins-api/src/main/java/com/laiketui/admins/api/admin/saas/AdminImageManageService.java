package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.admin.image.AddImageConfigVo;

import java.util.Map;

/**
 * 图片管理
 *
 * @author Trick
 * @date 2021/1/29 17:54
 */
public interface AdminImageManageService
{

    /**
     * 获取上传图片配置
     *
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 17:55
     */
    Map<String, Object> getImageConfigInfo(Integer type) throws LaiKeAPIException;


    /**
     * 添加/编辑图片上传配置
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 18:06
     */
    void addImageConfigInfo(AddImageConfigVo vo) throws LaiKeAPIException;
}
