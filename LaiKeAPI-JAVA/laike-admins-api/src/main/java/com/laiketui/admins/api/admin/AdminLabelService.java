package com.laiketui.admins.api.admin;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 商品标签管理
 *
 * @author Trick
 * @date 2021/6/25 18:05
 */
public interface AdminLabelService
{

    /**
     * 获取商品列表
     *
     * @param vo   -
     * @param name -
     * @param id   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/25 18:06
     */
    Map<String, Object> index(MainVo vo, String name, Integer id) throws LaiKeAPIException;

    /**
     * 添加/编辑商品标签
     *
     * @param vo   -
     * @param name -
     * @param id   -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 10:30
     * @update sunH
     * @date 2021/7/7 10:30
     */
    void addGoodsLabel(MainVo vo, String name, Integer id, String color) throws LaiKeAPIException;

    /**
     * 删除商品标签
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 10:40
     */
    void delGoodsLabel(MainVo vo, int id) throws LaiKeAPIException;
}
