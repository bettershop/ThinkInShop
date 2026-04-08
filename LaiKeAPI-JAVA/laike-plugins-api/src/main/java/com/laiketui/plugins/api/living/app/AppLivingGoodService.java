package com.laiketui.plugins.api.living.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.living.QueryLiveProVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/19
 */
public interface AppLivingGoodService
{

    /**
     * 主播中心-商品管理
     *
     * @param vo
     * @return
     */
    Map<String, Object> goodIndex(QueryLiveProVo vo);

    /**
     * 加载 修改商品库存 页面
     *
     * @param vo  -
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> queryCommissionPage(MainVo vo, Integer pid, Integer roomId) throws LaiKeAPIException;


    /**
     * 修改商品规格库存
     *
     * @param vo              -
     * @param living_id       -
     * @param confiGureModels - 商品规格json参数 [{"id":3095,"pid":755,num:1,"live_price":0.01},{"id":3096,"pid":755,num:1,"live_price":0.01},...]
     * @throws LaiKeAPIException -
     */
    void insertLivingPro(MainVo vo, int living_id, String confiGureModels) throws LaiKeAPIException;

    /**
     * 删除直播商品
     *
     * @param vo
     * @param pid
     * @throws LaiKeAPIException
     */
    void deleteLivingPro(MainVo vo, Integer pid, Integer roomId) throws LaiKeAPIException;

    /**
     * 置顶
     *
     * @param vo
     * @param pid       商品ID
     * @param living_id 直播间ID
     */
    void topping(MainVo vo, Integer pid, Integer living_id);


    /**
     * 编辑商品-加载数据
     *
     * @param vo  -
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> modify(MainVo vo, int pid, int roomId) throws LaiKeAPIException;

    /**
     * 查询分类和品牌下拉框
     *
     * @param vo
     * @param classId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryDropDown(MainVo vo, Integer classId) throws LaiKeAPIException;

    /**
     * 获取产品的详情信息
     *
     * @param vo
     * @param pro_id
     * @param room_Id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> proDetails(MainVo vo, Integer pro_id, Integer room_Id) throws LaiKeAPIException;

    /**
     * 讲解
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    void updateProExplain(MainVo vo, Integer proId, Integer roomId) throws LaiKeAPIException;

    /**
     * 查询直播间的商品
     *
     * @param vo
     * @return
     */
    Map<String, Object> livingPro(QueryLiveProVo vo);
}
