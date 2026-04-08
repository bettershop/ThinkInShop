package com.laiketui.admins.api.admin.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.AddStoreConfigVo;
import com.laiketui.domain.vo.mch.AddIMchVo;
import com.laiketui.domain.vo.mch.AddMchVo;
import com.laiketui.domain.vo.user.WithdrawalVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 店铺管理
 *
 * @author Trick
 * @date 2021/1/26 11:32
 */
public interface AdminMchManageService
{


    /**
     * 获取商城店铺列表
     *
     * @param vo            -
     * @param id            -
     * @param isOpen        -
     * @param name          -
     * @param promiseStatus -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 11:38
     */
    Map<String, Object> getMchInfo(MainVo vo, Integer id, Integer isOpen, String name, Integer promiseStatus, Integer cid) throws LaiKeAPIException;

    /**
     * 新增商铺信息
     *
     * @param vo
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2023/4/12 14:29
     */
    boolean addMchInfo(AddIMchVo vo) throws LaiKeAPIException;

    /**
     * 查询未创建店铺用户信息
     *
     * @param vo
     * @param queryUser
     * @return map<userId, UserName></>
     */
    Map<String, Object> addMchGetUserInfo(MainVo vo, String queryUser);

    /**
     * 修改商户信息
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 14:05
     */
    boolean modifyMchInfo(AddMchVo vo) throws LaiKeAPIException;


    /**
     * 删除店铺
     *
     * @param vo     -
     * @param shopId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 15:23
     */
    boolean delMchInfo(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 获取商户审核信息
     *
     * @param vo           -
     * @param id           -
     * @param reviewStatus -
     * @param name         -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 15:51
     */
    Map<String, Object> getMchExamineInfo(MainVo vo, Integer id, Integer reviewStatus, String name, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 商户审核通过/拒绝
     *
     * @param vo           -
     * @param mchId        -
     * @param reviewStatus -
     * @param text         -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 16:20
     */
    boolean examineMch(MainVo vo, int mchId, Integer reviewStatus, String text) throws LaiKeAPIException;

    /**
     * 添加/修改店铺分类
     *
     * @param vo
     * @param id
     * @param name
     * @param img
     * @param sort
     * @param isDisplay
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/11/01 10:20
     */
    void addMchClass(MainVo vo, Integer id, String name, String img, Integer sort, Integer isDisplay) throws LaiKeAPIException;

    /**
     * 是否显示
     *
     * @param vo
     * @param id
     * @param isDisplay
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/11/01 10:20
     */
    void isDisplay(MainVo vo, Integer id, Integer isDisplay) throws LaiKeAPIException;

    /**
     * 查询店铺分类列表
     *
     * @param vo
     * @param id
     * @param name
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/11/01 10:20
     */
    Map<String, Object> mchClassList(MainVo vo, Integer id, String name, Integer isDisPlay) throws LaiKeAPIException;

    /**
     * 删除店铺分类
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/11/01 10:20
     */
    void delMchClass(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 获取商品审核列表
     *
     * @param vo        -
     * @param mchName   -
     * @param goodsName -
     * @param goodsId   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 16:40
     */
    Map<String, Object> getGoodsExamineInfo(MainVo vo, String mchName, String goodsName, Integer goodsId) throws LaiKeAPIException;


    /**
     * 获取商品详细信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 17:26
     */
    Map<String, Object> getGoodsDetailInfo(MainVo vo, int goodsId) throws LaiKeAPIException;


    /**
     * 商品审核
     *
     * @param vo      -
     * @param goodsId -
     * @param status  -
     * @param text    -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/27 13:42
     */
    boolean goodsExamine(MainVo vo, int goodsId, int status, String text) throws LaiKeAPIException;

    interface ExamineStatus
    {
        /**
         * 拒绝
         */
        Integer REFUSE = 0;
        /**
         * 通过
         */
        Integer ADOPT  = 1;
    }


    /**
     * 获取提现列表
     *
     * @param vo     -
     * @param status -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/27 15:36
     */
    Map<String, Object> getWithdrawalInfo(WithdrawalVo vo, Integer status, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 店铺提现审核
     *
     * @param vo     -
     * @param id     -
     * @param stauts -
     * @param text   -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/27 15:59
     */
    boolean withdrawalExamineMch(MainVo vo, int id, int status, String text) throws LaiKeAPIException;


    /**
     * 获取商城设置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/27 17:43
     */
    Map<String, Object> getStoreConfigInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 设置商城配置
     *
     * @param vo -
     * @return LaiKeAPIException
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/27 17:47
     */
    boolean setStoreConfigInfo(AddStoreConfigVo vo) throws LaiKeAPIException;

    /**
     * 上传店铺信息
     *
     * @param vo
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/2/14 9:57
     */
    void location(MainVo vo) throws LaiKeAPIException;
}
