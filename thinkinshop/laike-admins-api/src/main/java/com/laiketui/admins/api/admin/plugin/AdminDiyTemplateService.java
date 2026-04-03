package com.laiketui.admins.api.admin.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.diy.AddDiyActivityVo;
import com.laiketui.domain.vo.admin.diy.SaveDiyUiVo;
import com.laiketui.domain.vo.plugin.BannerSaveVo;

import java.util.Map;

/**
 * diy模板管理 接口
 *
 * @author Trick
 * @date 2021/6/30 9:42
 */
public interface AdminDiyTemplateService
{
    interface DiyType
    {
        /**
         * 自定义
         */
        Integer CUSTOM  = 0;
        /**
         * 分类
         */
        Integer CLASS   = 1;
        /**
         * 商品
         */
        Integer PRODUCT = 2;
        /**
         * 店铺
         */
        Integer STORE   = 3;
    }

    /**
     * diy模板管理首页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 11:32
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取轮播图列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 13:39
     */
    Map<String, Object> bannerIndex(MainVo vo) throws LaiKeAPIException;

    /**
     * 轮播图路径分类
     *
     * @param vo   -
     * @param type - 跳转类型 1.分类 2.商品 3.店铺
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 13:56
     */
    Map<String, Object> bannerPathList(MainVo vo, Integer type) throws LaiKeAPIException;

    /**
     * 添加/编辑 轮播图
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 14:52
     */
    void bannerSave(BannerSaveVo vo) throws LaiKeAPIException;

    /**
     * 轮播图置顶
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 14:52
     */
    void bannerMoveTop(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 轮播图上移下移
     *
     * @param vo  -
     * @param id  -
     * @param id1 -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 14:52
     */
    void bannerRemove(MainVo vo, int id, int id1) throws LaiKeAPIException;

    /**
     * 轮播图删除
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 15:11
     */
    void bannerDel(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * ui导航栏列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 9:25
     */
    Map<String, Object> uiIndex(MainVo vo) throws LaiKeAPIException;

    /**
     * ui导航栏列表明细
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 9:25
     */
    Map<String, Object> uiIndexDetail(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 添加/编辑 ui导航栏
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 9:25
     */
    void uiSave(SaveDiyUiVo vo) throws LaiKeAPIException;

    /**
     * ui导航栏置顶
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 9:25
     */
    void uiTop(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * ui导航栏 上下移动
     *
     * @param vo  -
     * @param id  -
     * @param id1 -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 9:25
     */
    void uiMove(MainVo vo, int id, int id1) throws LaiKeAPIException;


    /**
     * 删除 ui导航栏
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 9:25
     */
    void uiDel(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 是否显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 9:25
     */
    void uiIsShowSwitch(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 分类管理列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 11:08
     */
    Map<String, Object> classIndex(MainVo vo) throws LaiKeAPIException;

    /**
     * 重新排序
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/17 19:48
     */
    void reSort(MainVo vo) throws LaiKeAPIException;

    /**
     * 类别置顶
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 11:08
     */
    void classTop(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 类别上下移动
     *
     * @param vo  -
     * @param id  -
     * @param id1 -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 11:08
     */
    void classMove(MainVo vo, int id, int id1) throws LaiKeAPIException;


    /**
     * 类别是否显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 11:08
     */
    void classSwitch(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 活动管理列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 16:31
     */
    Map<String, Object> activityList(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 获取插件类型
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 17:00
     */
    Map<String, Object> getPluginTypeList(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取商品列表
     *
     * @param vo        -
     * @param goodsName -
     * @param classId   -
     * @param brandId   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 17:00
     */
    Map<String, Object> getGoodsList(MainVo vo, String goodsName, Integer classId, Integer brandId) throws LaiKeAPIException;

    /**
     * 保存、编辑营销活动
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 16:41
     */
    void activitySave(AddDiyActivityVo vo) throws LaiKeAPIException;

    /**
     * 活动管理上下移动
     *
     * @param vo      -
     * @param moveId  -
     * @param moveId2 -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/17 15:31
     */
    void activityMove(MainVo vo, int moveId, int moveId2) throws LaiKeAPIException;

    /**
     * 活动显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 18:51
     */
    void activitySwitch(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 删除活动
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/1 18:51
     */
    void activityDel(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 获取活动商品列表
     *
     * @param vo    -
     * @param actId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/2 9:36
     */
    Map<String, Object> getActGoodsList(MainVo vo, int actId) throws LaiKeAPIException;


    /**
     * 活动商品上下移动
     *
     * @param vo       -
     * @param id       -
     * @param goodsId  -
     * @param goodsId1 -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/2 9:36
     */
    void actGoodsMove(MainVo vo, int id, int goodsId, int goodsId1) throws LaiKeAPIException;

    /**
     * 活动商品是否显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/20 10:00
     */
    void actGoodsSwitch(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 是否开启diy插件
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/2 14:59
     */
    boolean isDiyPlugin(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取diy模板首页列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/2 18:40
     */
    Map<String, Object> getTemplateDiyList(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 修改/编辑diy模板
     *
     * @param vo        -
     * @param id        -
     * @param jsonValue -
     * @param title     -
     * @param cover     -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/2 17:53
     */
    void addTemplate(MainVo vo, String jsonValue, String title, String cover, Integer id) throws LaiKeAPIException;


    /**
     * 设置diy模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/2 18:33
     */
    void setDiy(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 删除diy模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/2 18:33
     */
    void delDiy(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * ui导航栏是否需要登录开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author wukun
     * @date 2023/5/11 16:42
     */
    void uiIsLoginSwitch(MainVo vo, int id) throws LaiKeAPIException;

}
