package com.laiketui.plugins.api.group.appMch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.OrderModifyVo;
import com.laiketui.plugins.api.group.vo.*;

import java.util.Map;

/**
 * 拼团-活动管理-移动店铺端接口类
 * gp
 * 2023-07-05
 */
public interface PluginsGroupAppMchService
{
    /**
     * 进入拼团管理界面
     *
     * @param vo
     * @return gp
     * 2023-07-11
     */
    Map<String, Object> getSpecialManage(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取拼团活动列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> index(PluginsGroupGoodsActivityQueryVo vo) throws LaiKeAPIException;

    /**
     * 未参加拼团的商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> goodsList(PluginsGroupGoodsQueryVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑拼团活动
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    void addGroupActivity(PluginsGroupGroupSaveActivityVo vo) throws LaiKeAPIException;

    /**
     * 编辑拼团回显数据
     *
     * @param vo -
     * @param id - 拼团id
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> editPage(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 拼团是否显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    void groupIsShowSwitch(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 删除拼团活动
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    void delGroup(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 删除拼团活动商品
     *
     * @param vo   -
     * @param id   - 活动商品id
     * @param acId -活动id(删除所有商品)
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    void delGroupGoods(MainVo vo, String id, String acId) throws LaiKeAPIException;

    /**
     * 手动开始拼团活动
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    void startGroup(MainVo vo, String id, Integer type) throws LaiKeAPIException;

    /**
     * 开团记录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> openRecordList(PluginsGroupTeamVo vo) throws LaiKeAPIException;

    /**
     * 删除开团记录
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    void delOpenRecord(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 开团详情
     *
     * @param vo     -
     * @param openId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> openRecordDetailList(MainVo vo, String openId) throws LaiKeAPIException;


    /**
     * 参团记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> joinRecordList(PluginsGroupJoinRecordVo vo) throws LaiKeAPIException;

    /**
     * 删除参团记录
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    void delJoinRecord(MainVo vo, String id) throws LaiKeAPIException;


    /**
     * 获取拼团订单明细
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException;

    /**
     * 订单详情
     *
     * @param orderVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo) throws LaiKeAPIException;


    /**
     * 订单详情
     *
     * @param vo      -
     * @param orderNo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-05
     */
    Map<String, Object> detail(MainVo vo, String orderNo) throws LaiKeAPIException;

    /**
     * 根据id获取拼团商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, Integer goodsId) throws LaiKeAPIException;

    /**
     * 拼团设置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/22 11:16
     */
    void setConfig(PluginsGroupSaveConfigVo vo);

    /**
     * 获取配置信息
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/22 11:16
     */
    Map<String, Object> getConfig(MainVo vo) throws LaiKeAPIException;
}
