package com.laiketui.plugins.api.living;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.LoginVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/20
 */
public interface LivingPcService
{

    /**
     * 加载 修改商品库存 页面
     *
     * @param vo  -
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> queryCommissionPage(MainVo vo, Integer pid, Integer roomId) throws LaiKeAPIException;


    /**
     * 修改商品规格库存
     *
     * @param vo              -
     * @param living_id       -
     * @param confiGureModels - 商品规格json参数 [{"id":3095,"pid":755,num:1},{"id":3096,"pid":755,num:1},...]
     * @throws LaiKeAPIException -
     */
    void insertLivingPro(MainVo vo, int living_id, String confiGureModels) throws LaiKeAPIException;

    /**
     * 删除商品
     *
     * @param vo
     * @param pid
     * @throws LaiKeAPIException
     */
    void deleteLivingPro(MainVo vo, Integer pid, Integer roomId) throws LaiKeAPIException;


    /**
     * 编辑商品-加载数据
     *
     * @param vo  -
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> modify(MainVo vo, int pid, int roomId) throws LaiKeAPIException;

    /**
     * 登录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 9:41
     */
    Map<String, Object> login(LoginVo vo) throws LaiKeAPIException;

    /**
     * 获取图形验证码
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/2 16:59
     */
    Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException;


    /**
     * 退出登录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 9:50
     */
    Map<String, Object> loginOut(MainVo vo) throws LaiKeAPIException;


}
