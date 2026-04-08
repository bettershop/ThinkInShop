package com.laiketui.plugins.api.member;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.member.BuyMemberVo;
import com.laiketui.domain.vo.plugin.member.MemberProQueryVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 16:00 2022/6/29
 */
public interface PluginMemberAppService
{

    /**
     * 查询会员商品列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> memberProList(MemberProQueryVo vo) throws LaiKeAPIException;

    /**
     * 获取配置列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取会员购买记录
     *
     * @param vo
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getBuyRecord(MainVo vo, String userId) throws LaiKeAPIException;

    /**
     * 关闭自动续费弹框
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void closeFrame(MainVo vo) throws LaiKeAPIException;

    /**
     * 充值会员结算页面
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> settlement(BuyMemberVo vo) throws LaiKeAPIException;

    /**
     * 充值会员
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> payment(BuyMemberVo vo) throws LaiKeAPIException;

    /**
     * 会员中心页面数据
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> memberCenter(MainVo vo) throws LaiKeAPIException;

    /**
     * 查询会员协议
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> agreement(MainVo vo) throws LaiKeAPIException;

}
