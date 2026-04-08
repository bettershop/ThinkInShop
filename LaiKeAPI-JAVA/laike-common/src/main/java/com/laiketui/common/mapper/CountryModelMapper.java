package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.vo.saas.CountryVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CountryModelMapper extends BaseMapper<CountryModel>
{
    /**
     * 获取区号列表
     * @return
     */
    List<Map<String, Object>> getItuList(@Param("keyword") String keyword);

    public int countCountry(CountryVo vo);

    public List<CountryModel> queryCountryList(CountryVo vo);

    @Select("SELECT * from lkt_ds_country where code2 = #{code2}")
    CountryModel selectByCode2(String code2);

}
