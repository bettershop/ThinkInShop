package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddPcConfigVo;
import com.laiketui.domain.vo.pc.AddBannerInfoVo;
import com.laiketui.domain.vo.pc.AddBottomInfoVo;

import java.util.Map;

/**
 * pc管理接口
 *
 * @author Trick
 * @date 2021/1/22 10:39
 */
public interface AdminPcManageService
{


    /**
     * 获取轮播图信息
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 10:43
     */
    Map<String, Object> getBannerInfo(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 添加/编辑轮播图信息
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 10:47
     */
    boolean addBannerInfo(AddBannerInfoVo vo) throws LaiKeAPIException;


    /**
     * 删除轮播图
     *
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 10:50
     */
    boolean delBannerById(int id) throws LaiKeAPIException;


    /**
     * 轮播图置顶
     *
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/22 10:50
     */
    void topBannerById(int id) throws LaiKeAPIException;

    /**
     * 获取PC商城底部栏图片配置
     *
     * @param vo
     * @param id
     * @return
     * @author gp
     * @date 2023-08-24
     */
    Map<String, Object> getBottomInfo(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 添加/编辑PC商城底部栏图片配置
     *
     * @param vo
     * @return
     * @author gp
     * @date 2023-08-24
     */
    Boolean addBottomInfo(AddBottomInfoVo vo) throws LaiKeAPIException;

    /**
     * 删除PC商城底部栏图片配置
     *
     * @param id
     * @return
     * @author gp
     * @date 2023-08-24
     */
    Boolean delBottomById(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 添加/编辑PC商城显示配置
     *
     * @param vo
     * @param config
     * @return
     * @throws LaiKeAPIException
     * @author gp
     * @date 2023-08-24
     */
    void addConfig(MainVo vo, AddPcConfigVo config) throws LaiKeAPIException;

    /**
     * 获取PC商城显示配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author gp
     * @date 2023-08-24
     */
    Map<String, Object> getConfig(MainVo vo) throws LaiKeAPIException;
}
