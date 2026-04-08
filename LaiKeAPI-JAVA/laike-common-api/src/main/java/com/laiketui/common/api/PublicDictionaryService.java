package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.dic.DicVo;
import com.laiketui.domain.vo.systems.AddDictionaryDetailVo;

import java.util.List;
import java.util.Map;

/**
 * 字典操作
 *
 * @author Trick
 * @date 2020/12/28 14:56
 */
public interface PublicDictionaryService
{


    /**
     * 根据名称获取字典明细
     *
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/28 15:00
     */
    Map<String, Object> getDictionaryByName(String name) throws LaiKeAPIException;

    /**
     * 根据名称获取字典明细
     *
     * @param dicVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/17 16:27
     */
    Map<String, Object> getDictionaryByName(DicVo dicVo) throws LaiKeAPIException;

    /**
     * 根据名称获取字典明细ctext特殊处理
     *
     * @param dicVo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/17 16:27
     */
    Map<String, Object> getDictionaryByName2(DicVo dicVo) throws LaiKeAPIException;

    /**
     * 根据id获取字典信息
     *
     * @param name      - 目录名称
     * @param superName - 可选上级
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/15 16:34
     */
    List<Map<String, Object>> getDictionaryById(String name, String superName) throws LaiKeAPIException;


    /**
     * 获取字典数据列表
     *
     * @param vo    -
     * @param id    -
     * @param dicNo -
     * @param key   -
     * @param value -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 14:03
     */
    Map<String, Object> getDictionaryInfo(MainVo vo, Integer id, String dicNo, String key, String value, Integer status) throws LaiKeAPIException;


    /**
     * 获取数据名称列表
     *
     * @param vo   -
     * @param id   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 17:00
     */
    Map<String, Object> getDictionaryCatalogInfo(MainVo vo, Integer id, String name) throws LaiKeAPIException;

    /**
     * 获取目录下拉
     *
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 15:59
     */
    Map<String, Object> getDictionaryCatalogList() throws LaiKeAPIException;


    /**
     * 根据字典目录id获取字典代码
     *
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 16:14
     */
    Map<String, Object> getDictionaryCode(int id) throws LaiKeAPIException;


    /**
     * 添加数据字典目录
     *
     * @param id     -
     * @param name   -
     * @param token  -
     * @param isOpen -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 15:07
     */
    boolean addDictionaryInfo(Integer id, String name, int isOpen, String token) throws LaiKeAPIException;


    /**
     * 删除字典目录
     *
     * @param idList -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 17:24
     */
    boolean delDictionary(MainVo vo, List<Integer> idList) throws LaiKeAPIException;


    /**
     * 添加/修改字典表明细
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 14:41
     */
    void addDictionaryDetailInfo(AddDictionaryDetailVo vo) throws LaiKeAPIException;


    /**
     * 字典明细开关
     *
     * @param vo -
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 18:36
     */
    boolean switchDictionaryDetail(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 字典开关
     *
     * @param vo -
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 18:36
     */
    boolean switchDictionary(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 删除字典明细
     *
     * @param idList -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 17:24
     */
    boolean delDictionaryDetailInfo(MainVo vo, List<Integer> idList) throws LaiKeAPIException;

    List<Map<String, Object>> getDictionaryByName(Map<String, Object> map) throws LaiKeAPIException;
}
