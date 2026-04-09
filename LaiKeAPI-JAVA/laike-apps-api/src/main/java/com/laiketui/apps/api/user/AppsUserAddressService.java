package com.laiketui.apps.api.user;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAddressVo;

import java.util.Map;

/**
 * 我的地址接口
 *
 * @author Trick
 * @date 2020/11/4 17:03
 */
public interface AppsUserAddressService
{


    /**
     * 地址管理首页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-11-03 17:42:32
     */
    Map<String, Object> index(MainVo vo, String pid) throws LaiKeAPIException;


    /**
     * 保存地址
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 17:06
     */
    void saveAddress(SaveAddressVo vo) throws LaiKeAPIException;


    /**
     * 根据id获取地址
     *
     * @param vo     -
     * @param addrId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 8:59
     */
    Map<String, Object> getAddressById(MainVo vo, int addrId) throws LaiKeAPIException;


    /**
     * 修改地址
     *
     * @param vo     -
     * @param addsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 9:17
     */
    boolean updateAddress(SaveAddressVo vo, int addsId) throws LaiKeAPIException;


    /**
     * 设置默认地址
     *
     * @param vo     -
     * @param addsId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 10:26
     */
    boolean setDefaultAddress(MainVo vo, int addsId) throws LaiKeAPIException;


    /**
     * 省市级联
     *
     * @param vo      -
     * @param level   - 省市县等级
     * @param groupId - 上级id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 11:45
     */
    Map<String, Object> getJoinCityCounty(MainVo vo, int level, int groupId) throws LaiKeAPIException;


    /**
     * 删除地址
     *
     * @param vo     -
     * @param addrId - 地址id
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 11:45
     */
    void delAdds(MainVo vo, int addrId) throws LaiKeAPIException;
}
