package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.LangModel;
import com.laiketui.domain.vo.saas.LanguageVo;

import java.util.Map;

public interface AdminLanguageService
{
    /**
     * 获取语种列表
     *
     * @param vo   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
     * @author wx
     * @date 2025/6/25 18:06
     */
    Map<String, Object> index(LanguageVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑语种
     *
     * @param langModel -
     * @throws LaiKeAPIException-
     * @author wx
     * @date 2025/7/7 10:30
     */
    void addLanguage(LangModel langModel) throws LaiKeAPIException;

    /**
     * 删除语种
     *
     * @param id -
     * @throws LaiKeAPIException-
     * @author wx
     * @date 2025/7/7 10:40
     */
    void delLanguage(Integer id) throws LaiKeAPIException;

}
