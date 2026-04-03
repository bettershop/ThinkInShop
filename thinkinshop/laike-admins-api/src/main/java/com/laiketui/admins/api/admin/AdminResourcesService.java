package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 资源管理
 *
 * @author Trick
 * @date 2021/7/21 16:42
 */
public interface AdminResourcesService
{

    /**
     * 资源列表
     *
     * @param vo        -
     * @param imageName -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/21 16:44
     */
    Map<String, Object> index(MainVo vo, String imageName, String startTime, String endTime, String groupId, Integer type) throws LaiKeAPIException;

    /**
     * 批量下载图片
     *
     * @param vo       -
     * @param response -
     * @param imgIds   -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/22 10:22
     */
    void downForZip(MainVo vo, HttpServletResponse response, String imgIds) throws LaiKeAPIException;

    /**
     * 批量删除
     *
     * @param vo     -
     * @param imgIds -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/22 14:59
     */
    void del(MainVo vo, String imgIds) throws LaiKeAPIException;

    /**
     * 创建分类
     *
     * @param vo            -
     * @param catalogueName -
     * @param id            -
     * @param type
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 15:30
     */
    void addGroup(MainVo vo, String catalogueName, Integer id, Integer type) throws LaiKeAPIException;

    /**
     * 分类列表
     *
     * @param vo   -
     * @param type -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 16:59
     */
    Map<String, Object> groupList(MainVo vo, Integer type) throws LaiKeAPIException;

    /**
     * 批量删除文件
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 16:59
     */
    void delFile(MainVo vo, String ids);

    /**
     * 删除目录
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 16:59
     */
    void delCatalogue(MainVo vo, int id);

    /**
     * 图片上传
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 11:36
     */
    Map<String, Object> uploadImage(UploadFileVo vo) throws LaiKeAPIException;

    /**
     * 修改目录是否常驻显示
     *
     * @param vo   -
     * @param type
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 11:36
     */
    void updateCatalogueShow(MainVo vo, String id, Integer type);

    /**
     * 批量更改图片的分类
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 11:36
     */
    void updateCatalogueByImageIds(MainVo vo, String imageIds, Integer catalogueId);
}
