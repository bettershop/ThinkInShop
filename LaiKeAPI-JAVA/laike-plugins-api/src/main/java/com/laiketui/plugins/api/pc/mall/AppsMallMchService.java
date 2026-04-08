package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.ApplyShopVo;
import com.laiketui.domain.vo.pc.MchHomeVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 店铺主页
 *
 * @author Trick
 * @date 2021/6/21 15:20
 */
public interface AppsMallMchService
{
    /**
     * 加载店铺主页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-21 15:21:26
     */
    Map<String, Object> storeHomepage(MchHomeVo vo) throws LaiKeAPIException;

    /**
     * 店铺点击收藏/取消
     *
     * @param vo     -
     * @param shopId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/22 14:47
     */
    void collectionShop(MainVo vo, int shopId) throws LaiKeAPIException;

    /**
     * 我的店铺
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/22 16:08
     */
    Map<String, Object> myIndex(MainVo vo) throws LaiKeAPIException;

    /**
     * 申请开店
     *
     * @param vo   -
     * @param file -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/22 16:24
     */
    void applyShop(ApplyShopVo vo, MultipartFile file) throws LaiKeAPIException;


    /**
     * 获取协议
     *
     * @param storeId -
     * @param type    - 协议类型
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-22 16:59:14
     */
    Map<String, Object> getAgreement(int storeId, int type) throws LaiKeAPIException;


    /**
     * 继续申请店铺
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-22 17:03:27
     */
    Map<String, Object> continueApply(MainVo vo, Integer shopId) throws LaiKeAPIException;


    /**
     * 省市级联
     *
     * @param level   - 省市县等级
     * @param groupId - 上级id
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-22 17:06:14
     */
    List<Map<String, Object>> getJoinCityCounty(int level, int groupId) throws LaiKeAPIException;

    /**
     * 查看我的门店
     * 【php mch.see_my_store】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 11:04
     */
    Map<String, Object> seeMyStore(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 验证店铺名是否合法
     *
     * @param vo   -
     * @param name -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 11:01
     */
    boolean verifyStoreName(MainVo vo, String name) throws LaiKeAPIException;

}
