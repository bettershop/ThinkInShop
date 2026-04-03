package com.laiketui.admins.admin.services.service;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.report.AdminOrderReportService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.mapper.OrderReportModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.report.OrderReportModel;
import com.laiketui.domain.vo.MainVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class AdminOrderReportServiceImpl implements AdminOrderReportService
{

    @Autowired
    private OrderReportModelMapper orderReportModelMapper;


    /**
     * 订单报表状态数量统计-----顶上
     *
     * @param vo
     * @return
     */
    public List<OrderReportModel> getOrderData(MainVo vo)
    {

        Example          example  = new Example(OrderReportModel.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type", 1);
        List<OrderReportModel> orderReportModels = orderReportModelMapper.selectByExample(example);

        return orderReportModels;
    }

    /**
     * 订单报表实时订单统计---订单量
     *
     * @param vo
     * @return
     */
    public OrderReportModel getOrderAmount(MainVo vo)
    {
        Example          example  = new Example(OrderReportModel.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type", 2);
        OrderReportModel orderReportModel = orderReportModelMapper.selectOneByExample(example);
        return orderReportModel;
    }

    /**
     * 订单报表实时订单统计----订单金额
     *
     * @param vo
     * @return
     */
    public Object getTotalAmount(MainVo vo) throws LaiKeAPIException
    {
        Example          example  = new Example(OrderReportModel.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type", 7);
        criteria.andEqualTo("storeId", vo.getStoreId());
        OrderReportModel orderReportModel = orderReportModelMapper.selectOneByExample(example);
        if (orderReportModel != null)
        {
            return JSON.parseObject(orderReportModel.getData());
        }
        else
        {
            Map<String, Object> orderData = publicOrderService.getOrderData(vo.getStoreId());
            OrderReportModel    model     = new OrderReportModel();
            model.setType(7);
            model.setStoreId(vo.getStoreId());
            model.setNum(0);
            model.setData(JSON.toJSONString(orderData));
            orderReportModelMapper.insert(model);
            return orderData;
        }

    }

    /**
     * 订单报表实时订单统计---退款订单
     *
     * @param vo
     * @return
     */
    public Object getRefundData(MainVo vo) throws LaiKeAPIException
    {
        Example          example  = new Example(OrderReportModel.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type", 6);
        criteria.andEqualTo("storeId", vo.getStoreId());
        OrderReportModel orderReportModel = orderReportModelMapper.selectOneByExample(example);
        if (orderReportModel != null)
        {
            return JSON.parseObject(orderReportModel.getData());
        }
        else
        {
            Map<String, Object> refundOrderData = publicOrderService.getRefundOrderData(vo.getStoreId());
            OrderReportModel    model           = new OrderReportModel();
            //订单报表实时订单统计---退款订单
            model.setType(6);
            model.setStoreId(vo.getStoreId());
            model.setNum(0);
            model.setData(JSON.toJSONString(refundOrderData));
            orderReportModelMapper.insert(model);
            return refundOrderData;
        }
    }

    @Autowired
    private PublicOrderService publicOrderService;

}
