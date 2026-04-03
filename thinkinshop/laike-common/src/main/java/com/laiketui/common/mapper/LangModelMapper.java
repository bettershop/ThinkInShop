package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.LangModel;
import com.laiketui.domain.vo.saas.LanguageVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LangModelMapper extends BaseMapper<LangModel>
{

    public int count(LanguageVo vo);

    public List<LangModel> queryList(LanguageVo vo);

    @Select("SELECT * from lkt_lang where lang_code = #{langCode}")
    LangModel getStoreDefaultLangByLangCode(String langCode);

}