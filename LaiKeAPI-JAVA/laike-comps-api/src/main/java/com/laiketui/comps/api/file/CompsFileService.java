package com.laiketui.comps.api.file;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.FilesVo;
import com.laiketui.domain.vo.files.UploadFileVo;

import java.util.Map;

/**
 * 文件资源接口
 *
 * @author Trick
 * @date 2021/7/7 18:17
 */
public interface CompsFileService
{

    /**
     * 文件列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/7 18:18
     */
    Map<String, Object> index(FilesVo vo) throws LaiKeAPIException;

    /**
     * 图片上传分类列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 9:36
     */
    Map<String, Object> groupList(MainVo vo) throws LaiKeAPIException;

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
     * 创建目录
     *
     * @param vo            -
     * @param catalogueName -
     * @param id            -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 15:30
     */
    void createCatalogue(MainVo vo, String catalogueName, Integer id) throws LaiKeAPIException;

    /**
     * 删除目录
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 16:59
     */
    void delCatalogue(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 删除文件
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 17:00
     */
    void delFile(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 下载外链图片到oss上
     *
     * @param vo     -
     * @param imgUrl -
     * @param text   -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/8 17:15
     */
    void uploadUrlFiles(MainVo vo, String imgUrl, String text, Integer mchId) throws LaiKeAPIException;
}
