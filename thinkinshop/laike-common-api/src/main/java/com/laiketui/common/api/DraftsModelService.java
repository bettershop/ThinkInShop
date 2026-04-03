package com.laiketui.common.api;

import com.laiketui.domain.vo.MainVo;

import java.util.Map;

public interface DraftsModelService
{
    /**
     * 新增草稿箱
     * @param vo
     */
    void add(MainVo vo,Integer type,String text,Integer id);

    /**
     * 草稿箱列表
     * @param vo
     * @return
     */
    Map<String,Object> list(MainVo vo,Integer type);

    /**
     * 删除
     * @param id
     * @return
     */
    void del(String id);

    /**
     * 获取text
     * @param id
     * @return
     */
    Map<String,Object> getTextById(Integer id);
}
