package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.member.MemberProModel;

import java.util.List;
import java.util.Map;

public interface MemberProModelMapper extends BaseMapper<MemberProModel>
{

    /**
     * 获取会员商品信息 -
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/07/01 14:04
     */
    List<Map<String, Object>> getMemberPro(Map<String, Object> map) throws LaiKeAPIException;

    Integer countMemberPro(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取非会员商品
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getPro(Map<String, Object> map) throws LaiKeAPIException;

    Integer countPro(Map<String, Object> map) throws LaiKeAPIException;

}