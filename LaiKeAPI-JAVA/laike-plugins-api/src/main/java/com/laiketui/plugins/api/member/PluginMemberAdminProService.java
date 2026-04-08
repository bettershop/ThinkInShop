package com.laiketui.plugins.api.member;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.member.MemberProQueryVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 10:16 2022/7/1
 */
public interface PluginMemberAdminProService
{

    /**
     * 查询会员商品列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> memberProList(MemberProQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 查询非会员商品列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> proList(MemberProQueryVo vo) throws LaiKeAPIException;

    /**
     * 添加会员商品
     *
     * @param vo
     * @param proIds
     * @throws LaiKeAPIException
     */
    void addMemberPro(MainVo vo, String proIds) throws LaiKeAPIException;

    /**
     * 移除会员商品
     *
     * @param vo
     * @param proIds 商品id(多选逗号隔开)
     * @throws LaiKeAPIException
     */
    void delMemberPro(MainVo vo, String proIds) throws LaiKeAPIException;
}
