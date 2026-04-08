package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.dictionary.DictionaryListModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 数据字典表
 *
 * @author Trick
 * @date 2020/9/24 13:25
 */
public interface DictionaryListModelMapper extends BaseMapper<DictionaryListModel>
{


    /**
     * 获取字典值
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 10:50
     */
    List<Map<String, Object>> getDictionaryDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * value不能相同 不同语言的可以相同 比如商品展示位置 代码查询的时候用的值是一样的
     */
    @Select("select count(1) from lkt_data_dictionary_list a,lkt_data_dictionary_name b where a.sid=#{sid} " +
            " and a.sid=b.id and a.code like concat(b.dic_code,'%') and a.value=#{value} and a.recycle=0 and a.lang_code = ${landCode}")
    int countDicListCode(int sid, String value,String landCode);

    /**
     * text 不能相同
     */
    @Select("select count(1) from lkt_data_dictionary_list a,lkt_data_dictionary_name b where a.sid=#{sid} " +
            " and a.sid=b.id and a.code like concat(b.dic_code,'%') and a.ctext=#{name} and a.recycle=0")
    int countDicListName(int sid, String name);

    @Select("SELECT a.`ctext` FROM lkt_data_dictionary_list a RIGHT JOIN lkt_data_dictionary_name b on a.sid = b.id WHERE b.name = #{name} AND a.value = #{value} and a.recycle= 0")
    String getDictionaryText(String name, String value);

    /**
     * 根据字典名称获取字典列表
     *
     * @param map 参数Map，应包含：
     *            - name: 字典名称（如"积分类型"）
     *            - status: 状态（可选）
     *            - lang_code: 语言代码（可选）
     * @return 字典列表
     * @throws LaiKeAPIException 业务异常
     * @author Trick
     * @date 2023/12/15
     */
    List<Map<String, Object>> getDictionaryByName(Map<String, Object> map) throws LaiKeAPIException;

}
