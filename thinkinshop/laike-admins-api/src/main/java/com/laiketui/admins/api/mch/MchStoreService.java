package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddStoreVo;

import java.util.Map;

/**
 * 门店管理
 *
 * @author Trick
 * @date 2021/5/27 16:25
 */
public interface MchStoreService
{

    /**
     * 门店列表
     *
     * @param vo         -
     * @param mchName    -
     * @param mchStoreId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 16:26
     */
    Map<String, Object> index(MainVo vo, Integer mchStoreId, String mchName) throws LaiKeAPIException;


    /**
     * 添加我的门店
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 14:52
     */
    void addStore(AddStoreVo vo) throws LaiKeAPIException;

    /**
     * 设置默认门店
     *
     * @param vo         -
     * @param mchStoreId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/20 16:44
     */
    void setDefaultStore(MainVo vo, Integer mchStoreId) throws LaiKeAPIException;

    /**
     * 编辑我的门店页面数据
     * 【php mch.edit_store_page】
     *
     * @param vo     -
     * @param shopId -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 18:02
     */
    Map<String, Object> editStorePage(MainVo vo, int shopId, int id) throws LaiKeAPIException;


    /**
     * 删除我的店铺
     * 【php mch.del_store】
     *
     * @param vo  -
     * @param ids - 门店id集
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/27 18:19
     */
    void delStore(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 获取门店管理员列表
     *
     * @param vo
     * @param mch_store_id   门店id
     * @param account_number 管理员账号
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getAdminList(MainVo vo, Integer mch_store_id, String account_number) throws LaiKeAPIException;

    /**
     * 添加修改管理员
     *
     * @param vo
     * @param mch_store_id   门店id
     * @param account_number 管理员账号
     * @param password       密码
     * @param id             修改
     * @throws LaiKeAPIException
     */
    void addAdmin(MainVo vo, Integer mch_store_id, String account_number, String password, Integer id) throws LaiKeAPIException;


    /**
     * mch.Mch.Store.DelAdmin
     * 删除管理员
     *
     * @param vo
     * @param mch_store_id
     * @param id
     * @throws LaiKeAPIException
     */
    void delAdmin(MainVo vo, Integer mch_store_id, Integer id) throws LaiKeAPIException;
}
