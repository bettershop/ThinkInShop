package com.laiketui.admins.api.admin.systems;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.systems.AddressVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 地址管理接口
 *
 * @author Trick
 * @date 2021/1/15 9:32
 */
public interface AdminAddressService
{


    /**
     * 获取地址列表
     *
     * @param vo   -
     * @param id   - 可选
     * @param name - 联系人
     * @param type - 类型（1发货地址 2售后地址） 默认2
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/15 9:33
     */
    Map<String, Object> getAddressInfo(MainVo vo, Integer id, String name, Integer type) throws LaiKeAPIException;

    interface AddressType
    {
        /**
         * 发货地址
         */
        Integer SHIPPING_ADDRESS = 1;
        /**
         * 售后地址
         */
        Integer SALES_ADDRESS    = 2;
    }

    public static void main(String[] args)
    {
        BigDecimal startingAmt  = new BigDecimal("10");
        BigDecimal price        = new BigDecimal("235");
        BigDecimal currentPrice = new BigDecimal("200");
        BigDecimal markUpAmt    = new BigDecimal("10");

        BigDecimal[] bei = price.divideAndRemainder(markUpAmt);
        if (startingAmt.compareTo(price) != 0 && BigDecimal.ZERO.compareTo(bei[1]) != 0)
        {
            System.out.println("当前出的价不是设置的加价幅度的倍数");
        }
    }


    /**
     * 添加/修改地址信息
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/15 10:49
     */
    boolean addAddressInfo(AddressVo vo) throws LaiKeAPIException;

    /**
     * 设置默认
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/14 9:28
     */
    void setDefaultAddress(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 删除地址
     *
     * @param vo   -
     * @param id   -
     * @param type -类型（1发货地址 2售后地址） 默认2
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/15 11:28
     */
    boolean delAddress(MainVo vo, int id, int type) throws LaiKeAPIException;
}
