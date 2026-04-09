package com.laiketui.apps.api.app.activitie;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

/**
 * 平台活动管理
 *
 * @author Trick
 * @date 2021/4/25 10:15
 */
public interface AppsCstrPlatformActivitiesService
{

    /**
     * 平台活动管理首页
     *
     * @param vo    -
     * @param mchId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/25 10:18
     */
    List<Map<String, Object>> index(MainVo vo, int mchId) throws LaiKeAPIException;


    /**
     * 活动id
     *
     * @param vo -
     * @param id -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/25 13:45
     */
    List<Map<String, Object>> activityDetails(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 商品列表显示
     *
     * @param vo    -
     * @param id    -
     * @param mchId -
     * @param type  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/25 15:24
     */
    Map<String, Object> proList(MainVo vo, int id, int mchId, int type) throws LaiKeAPIException;

    /**
     * 0.商品列表 1.待审核
     */
    interface ProListType
    {
        Integer PRODUCTLIST = 0;
        Integer REVIEWED    = 1;
    }


    /**
     * 修改活动商品
     *
     * @param vo           -
     * @param id           -
     * @param activityType -
     * @param type         -审核状态 null=删除 0=撤销 1=提交
     * @param num          -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/25 16:49
     */
    Map<String, Object> modifyPro(MainVo vo, int id, String activityType, Integer type, Integer num) throws LaiKeAPIException;

    /**
     * 审核状态 null=删除 0=撤销 1=提交
     */
    interface ModifyProType
    {
        Integer REVOKE     = 0;
        Integer SUBMIT     = 1;
        Integer ADDITIONAL = 99;
    }


    /**
     * 活动动商品展示页
     *
     * @param vo        -
     * @param id        -
     * @param mchId     -
     * @param cid       -
     * @param brandId   -
     * @param goodsName -
     * @param type      - 活动类型
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/25 17:23
     */
    Map<String, Object> listdetail(MainVo vo, Integer id, Integer mchId, Integer cid, Integer brandId, String goodsName, String type) throws LaiKeAPIException;


    /**
     * 添加秒杀商品
     *
     * @param vo        -
     * @param id        -
     * @param mchId     -
     * @param goodsData -[{"id":"16601","price":0.008,"num":"3","pid":"1260"},
     *                  {"id":"16602","price":0.008,"num":"3","pid":"1260"},
     *                  {"id":"16582","price":0.8,"num":"3","pid":"1258"},....]
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/26 10:22
     */
    Map<String, Object> addSecondGoods(MainVo vo, int id, int mchId, String goodsData) throws LaiKeAPIException;


    /**
     * 添加拼团商品
     *
     * @param vo        -
     * @param id        -
     * @param mchId     -
     * @param goodsData -[{"id":"16601","price":0.008,"num":"3","pid":"1260"},
     *                  {"id":"16602","price":0.008,"num":"3","pid":"1260"},
     *                  {"id":"16582","price":0.8,"num":"3","pid":"1258"},....]
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/26 11:40
     */
    Map<String, Object> addGroupGoods(MainVo vo, int id, int mchId, String goodsData) throws LaiKeAPIException;

}
