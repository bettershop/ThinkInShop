package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.BrandClassVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author Trick
 * @date 2020/12/30 16:58
 */
public interface AdminBrandService
{


    /**
     * 查询品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    Map<String, Object> getBrandInfo(BrandClassVo vo, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 获取国家列表
     *
     * @param vo -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 10:28
     */
    List<CountryModel> getCountry(MainVo vo) throws LaiKeAPIException;

    /**
     * 增加/修改品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    boolean addBrand(BrandClassVo vo) throws LaiKeAPIException;

    int addBrand1(BrandClassVo vo) throws LaiKeAPIException;


    /**
     * 删除品牌
     *
     * @param vo      -
     * @param brandId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 11:18
     */
    boolean delBrand(MainVo vo, int brandId) throws LaiKeAPIException;

    /**
     * 品牌置顶
     *
     * @param vo      -
     * @param brandId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 11:25
     */
    boolean brandByTop(MainVo vo, int brandId) throws LaiKeAPIException;
}
