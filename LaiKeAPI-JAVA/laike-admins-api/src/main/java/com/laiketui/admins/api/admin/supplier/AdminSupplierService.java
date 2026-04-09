package com.laiketui.admins.api.admin.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierConfigModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.AdminOrderDetailVo;
import com.laiketui.domain.vo.order.AdminOrderListVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.supplier.AddSupplierVo;
import com.laiketui.domain.vo.supplier.GetSupplierVo;
import com.laiketui.domain.vo.supplier.SupplierConfigVo;
import com.laiketui.domain.vo.supplier.SupplierWithdrawVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 14:13 2022/9/14
 */
public interface AdminSupplierService
{

    /**
     * 添加修改供应商信息
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addOrUpdate(AddSupplierVo vo) throws LaiKeAPIException;

    /**
     * 删除供应商
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 锁定解锁
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void lock(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 查询
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getList(GetSupplierVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单列表
     *
     * @param adminOrderVo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderIndex(AdminOrderListVo adminOrderVo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 订单详情
     *
     * @param orderVo
     * @return
     */
    Map<String, Object> orderDetailsInfo(AdminOrderDetailVo orderVo);

    /**
     * 订单结算 列表
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> settlementList(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 提现记录
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> withdraw(SupplierWithdrawVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 审核提现记录
     *
     * @param vo
     * @param id
     * @param status
     * @param voucher
     * @param remark
     * @throws LaiKeAPIException
     */
    void examineWithdraw(MainVo vo, Integer id, Integer status, String voucher, String remark) throws LaiKeAPIException;

    /**
     * 查询供应商配置信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    SupplierConfigModel getConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 更新供应商配置信息
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addUpdateConfig(SupplierConfigVo vo) throws LaiKeAPIException;

    /**
     * 经营收益
     *
     * @param vo
     * @param startTime
     * @param endTime
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> income(MainVo vo, String startTime, String endTime) throws LaiKeAPIException;

    /**
     * 设置供应商商品是否需要审核
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void isExamine(MainVo vo) throws LaiKeAPIException;
}
