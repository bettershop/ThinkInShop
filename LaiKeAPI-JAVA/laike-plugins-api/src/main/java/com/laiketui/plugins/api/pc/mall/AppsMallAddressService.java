package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAddressVo;

import java.util.List;
import java.util.Map;

/**
 * 地址管理接口
 *
 * @author Trick
 * @date 2021/6/18 15:52
 */
public interface AppsMallAddressService
{


    /**
     * 地址管理首页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 17:04
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;


    /**
     * 保存地址
     *
     * @param vo -
     * @return boolean
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 17:06
     */
    List<Map<String, Object>> saveAddress(SaveAddressVo vo) throws LaiKeAPIException;


    /**
     * 根据id获取地址
     *
     * @param vo        -
     * @param addressId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 8:59
     */
    Map<String, Object> getAddressById(MainVo vo, int addressId) throws LaiKeAPIException;


    /**
     * 修改地址
     *
     * @param vo     -
     * @param addsId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 9:17
     */
    List<Map<String, Object>> updateAddress(SaveAddressVo vo, Integer addsId) throws LaiKeAPIException;


    /**
     * 设置默认地址
     *
     * @param vo     -
     * @param addsId -
     * @return List - 返回最新地址列表
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 10:26
     */
    List<Map<String, Object>> setDefaultAddress(MainVo vo, Integer addsId) throws LaiKeAPIException;

    /**
     * 获取全部省市县
     *
     * @param vo -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/2/17 14:33
     */
    List<Map<String, Object>> getAddressList(MainVo vo) throws LaiKeAPIException;

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
     * @param vo        -
     * @param addressId - 地址id
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 11:45
     */
    List<Map<String, Object>> delAdds(MainVo vo, Integer addressId) throws LaiKeAPIException;
}
