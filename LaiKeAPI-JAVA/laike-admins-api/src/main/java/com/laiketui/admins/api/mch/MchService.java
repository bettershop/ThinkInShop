package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddMchVo;
import com.laiketui.domain.vo.mch.MchPrintSetupVo;
import com.laiketui.domain.vo.pc.AddBannerInfoVo;

import java.util.Map;

/**
 * 店铺设置
 *
 * @author Trick
 * @date 2021/5/27 11:37
 */
public interface MchService
{

    /**
     * 店铺信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 11:37
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;


    /**
     * 编辑店铺
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 14:03
     */
    Map<String, Object> edit(AddMchVo vo) throws LaiKeAPIException;

    /**
     * 设置店铺密码
     *
     * @param vo     -
     * @param pwd    -
     * @param pwdOld -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/20 9:48
     */
    Map<String, Object> setPassword(MainVo vo, String pwd, String pwdOld) throws LaiKeAPIException;

    /**
     * 获取轮播图列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 14:25
     */
    Map<String, Object> bannerList(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 轮播图路径分类
     *
     * @param vo   -
     * @param type - 跳转类型 1.分类 2.商品 3.店铺
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/11 11:00
     */
    Map<String, Object> bannerPathList(MainVo vo, Integer type) throws LaiKeAPIException;

    /**
     * 添加/编辑轮播图信息
     *
     * @param vo   -
     * @param type -  默认移动端轮播图  3=店铺轮播图
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 14:25
     */
    void addBannerInfo(AddBannerInfoVo vo, Integer type) throws LaiKeAPIException;

    /**
     * 修改轮播图序号
     *
     * @param vo   -
     * @param id   -
     * @param sort -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/20 10:43
     */
    void setBannerSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException;

    /**
     * 删除轮播图
     *
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 14:25
     */
    void delBannerById(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 轮播图置顶
     *
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 14:25
     */
    void topBannerById(int id) throws LaiKeAPIException;


    /**
     * 注销店铺
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 16:17
     */
    boolean delMchInfo(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取店铺设置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/21 19:09
     */
    Map<String, Object> getMchConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 查询店铺分类列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/11/01 10:20
     */
    Map<String, Object> mchClassList(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取订单打印配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author gp
     * @date 2022/11/01 10:20
     */
    Map<String, Object> getMchPrintSetup(MainVo vo) throws LaiKeAPIException;

    /**
     * 上传订单打印配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author gp
     * @date 2022/11/01 10:20
     */
    void setMchPrintSetup(MchPrintSetupVo vo) throws LaiKeAPIException;

    /**
     * pc店铺首页
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> mchIndex(MainVo vo, String startTime, String endTime) throws LaiKeAPIException;


    /**
     * 统计店铺信息
     */
    void statisticsMch(MainVo vo);
}
