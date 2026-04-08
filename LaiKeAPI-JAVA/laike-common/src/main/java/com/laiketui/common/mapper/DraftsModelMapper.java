package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.DraftsModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DraftsModelMapper extends BaseMapper<DraftsModel>
{

    /**
     * 统计数量
     * @param map -
     * @return
     * @throws LaiKeAPIException
     */
    int count(Map<String, Object> map);

    /**
     * 草稿箱列表
     * @param map
     * @return
     */
    List<Map<String, Object>> list(Map<String, Object> map);

    /**
     * 批量删除
     * @param list
     */
    void deleteBatch(@Param("list") List<String> list);

    /**
     * 新增
     * @param draftsModel
     * @return
     */
    int add(@Param("model") DraftsModel draftsModel);

    /**
     * 获取草稿箱内容
     * @param id
     * @return
     */
    DraftsModel getById(@Param("id") Integer id);

    /**
     * 编辑
     * @param draftsModel
     * @return
     */
    int update(@Param("model") DraftsModel draftsModel);
}
