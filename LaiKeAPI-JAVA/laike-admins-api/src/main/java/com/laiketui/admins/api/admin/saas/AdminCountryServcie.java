package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.vo.saas.CountryVo;

import java.util.Map;


/**
 * 国家信息服务类
 */
public interface AdminCountryServcie
{

    /**
     * 获取国家信息列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> countryList(CountryVo vo) throws LaiKeAPIException;

    /**
     * 保存国家信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    void saveOrEditCountry(CountryModel countryModel) throws LaiKeAPIException;

    /**
     * 删除国家信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    void deleteCountry(CountryVo vo) throws LaiKeAPIException;

}
