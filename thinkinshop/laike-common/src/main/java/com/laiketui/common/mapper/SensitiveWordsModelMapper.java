package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.config.SensitiveWordsModel;
import com.laiketui.domain.vo.systems.SensitiveVo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SensitiveWordsModelMapper extends BaseMapper<SensitiveWordsModel> {

    /**
     * 获取敏感词列表
     * @param paramMap
     * @return
     */
    List<SensitiveVo> selectList(Map<String, Object> paramMap);

    /**
     * 统计数量
     * @param paramMap
     * @return
     */
    Integer count(Map<String, Object> paramMap);

    /**
     * 批量删除敏感词
     * @param idList
     * @param storeId
     */
    void delBatchByIds(@Param("ids") List<String> idList,@Param("storeId") int storeId);

    /**
     * 批量新增
     * @param storeId
     * @param date
     * @param wordlist
     */
    void insertBatch(@Param("storeId") int storeId,@Param("date") Date date, @Param("wordList") List<String> wordlist);

    /**
     * 获取所有敏感词
     * @param storeId
     * @return
     */
    List<String> getWordList(@Param("storeId") int storeId);

    /**
     * 判断敏感词是否重复
     *
     * @param storeId
     * @param word
     * @param id
     * @return
     */
    int check(@Param("storeId") int storeId, @Param("word") String word, @Param("id") Integer id);
}
