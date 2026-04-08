package com.laiketui.plugins.api.integral.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.order.*;
import com.laiketui.domain.vo.plugin.integral.AddIntegralVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;
import com.laiketui.domain.vo.sec.QueryProVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * h5积分店铺接口类
 * gp
 * 2023-07-13
 */
public interface PluginsIntegralAppMchService
{
    /**
     * 积分商品列表
     *
     * @param vo        -
     * @param goodsName -
     * @param id        -
     * @return Map
     * @throws LaiKeAPIException -
     *                           gp
     *                           2023-07-13
     */
    Map<String, Object> index(MainVo vo, String goodsName, Integer id) throws LaiKeAPIException;

    /**
     * 获取商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException- gp
     *                            2023-07-13
     */
    Map<String, Object> getProList(QueryProVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑积分商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     *                           gp
     *                           2023-07-13
     */
    Map<String, Object> addIntegral(AddIntegralVo vo) throws LaiKeAPIException;


    /**
     * 删除
     *
     * @param vo  -
     * @param ids -
     * @return Map
     * @throws LaiKeAPIException -
     *                           gp
     *                           2023-07-13
     */
    Map<String, Object> del(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 添加库存
     *
     * @param vo    -
     * @param proId -
     * @param num   -
     * @throws LaiKeAPIException- gp
     *                            2023-07-13
     */
    void addStock(MainVo vo, int proId, int num) throws LaiKeAPIException;

    /**
     * 秒杀订单首页
     *
     * @param adminOrderVo -
     * @param response     -
     * @return Map
     * @throws LaiKeAPIException- gp
     *                            2023-07-14
     */
    Map<String, Object> orderIndex(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 关闭订单
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException- gp
     *                            2023-07-14
     */
    void closeOrder(OrderVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 平台订单详情
     *
     * @param orderVo -
     * @return -
     * gp
     * 2023-07-14
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);


    /**
     * 保存编辑订单
     *
     * @param orderVo -
     * @throws LaiKeAPIException- gp
     *                            2023-07-14
     */
    void saveEditOrder(EditOrderVo orderVo) throws LaiKeAPIException;


    /**
     * 编辑订单界面
     *
     * @param orderVo
     * @return
     * @throws LaiKeAPIException gp
     *                           2023-07-14
     */
    Map<String, Object> editOrderView(OrderModifyVo orderVo) throws LaiKeAPIException;


    /**
     * 获取订单物流信息
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     *                           gp
     *                           2023-07-14
     */
    Map<String, Object> kuaidishow(MainVo vo, String orderno) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo             -
     * @param exId           -
     * @param exNo           -
     * @param orderDetailIds -
     * @throws LaiKeAPIException-
     * @author Administrator
     * @date 2021/8/2 14:42
     * gp
     * 2023-07-14
     */
    void deliverySave(MainVo vo, Integer exId, String exNo, String orderDetailIds) throws LaiKeAPIException;


    /**
     * 进入发货界面
     *
     * @param adminDeliveryVo -
     * @return Map<String, Object>
     * @throws LaiKeAPIException- gp
     *                            2023-07-14
     */
    Map<String, Object> deliveryView(String express) throws LaiKeAPIException;

    /**
     * 获取评论列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException;

    /**
     * 获取评论详细信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getCommentsDetailInfoById(GetCommentsDetailInfoVo vo) throws LaiKeAPIException;

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
     * 获取积分订单未查看评价数量
     * @param vo
     * @return
     */
    Integer getCommentsCount(MainVo vo) throws LaiKeAPIException;
}
