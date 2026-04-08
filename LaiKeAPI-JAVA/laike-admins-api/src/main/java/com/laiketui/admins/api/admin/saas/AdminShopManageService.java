package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.AddShopVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 商户管理
 *
 * @author Trick
 * @date 2021/1/28 9:20
 */
public interface AdminShopManageService
{

    /**
     * 获取商城列表
     *
     * @param storeId   -
     * @param storeName -
     * @param startDate -
     * @param endDate   -
     * @param pageModel -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 9:21
     */
    Map<String, Object> getShopInfo(MainVo vo, String storeName, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 是否启用开关
     *
     * @param storeId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 11:10
     */
    boolean setStoreOpenSwitch(MainVo vo, Integer storeId) throws LaiKeAPIException;


    /**
     * 设置默认商城
     *
     * @param storeId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/11 14:39
     */
    void setStoreDefaultSwitch(MainVo vo, Integer storeId) throws LaiKeAPIException;


    /**
     * 添加/编辑商城
     *
     * @param vo -
     * @param ip -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 11:42
     */
    boolean addStore(AddShopVo vo, String ip) throws LaiKeAPIException;


    /**
     * 删除商城
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 15:01
     */
    boolean delStore(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 重置管理员密码
     *
     * @param adminId -
     * @param pwd     -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 15:25
     */
    boolean resetAdminPwd(MainVo vo, int adminId, String pwd) throws LaiKeAPIException;

    /**
     * 校验商城是否有自营店
     * @param vo
     * @return
     */
    Boolean checkShopHavaSelfOwnedShop(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取语种和币种
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String,Object> getLandingCurrency(MainVo vo) throws LaiKeAPIException;

}
