package com.laiketui.plugins.api.diy;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-06-18-13:56
 * @Description:
 */
public interface PluginsDiyMchService {

    /**
     * 获取diy模板首页列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2022/04/28 16:00
     */
    Map<String, Object> getDiyList(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 修改/编辑diy模板
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    Map<String,Object> addOrUpdateDiy(DiyVo vo) throws LaiKeAPIException;


    /**
     * 设置diy模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    void diyStatus(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 删除diy模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    void delDiy(MainVo vo, int id) throws LaiKeAPIException;



    /**
     * 获取h5 预览地址
     *
     * @param vo
     * @return
     */
    Map<String, Object> index(MainVo vo);

    /**
     * 轮播图路径分类
     *
     * @param vo        -
     * @param type      - 跳转类型 1.分类 2.商品 3.店铺
     * @param lang_code - 语种
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/30 13:56
     */
    Map<String, Object> bannerPathList(MainVo vo, Integer type, String lang_code) throws LaiKeAPIException;
}
