package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.log.FilesRecordModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 图片上传记录
 *
 * @author Trick
 * @date 2021/7/8 10:19
 */
public interface FilesRecordModelMapper extends BaseMapper<FilesRecordModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取图片上传配置 获取最新一条
     *
     * @param filesRecordModel -
     * @return FilesRecordModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 18:09
     */
    @Select("SELECT id,store_id,store_type,`group`,upload_mode,image_name,add_time,diy_id,diy_img_type,img_type " +
            " FROM lkt_files_record WHERE store_id = #{store_id} AND image_name = #{image_name} " +
            "order by add_time desc limit 1")
    FilesRecordModel getImageUrlOne(FilesRecordModel filesRecordModel) throws LaiKeAPIException;

    /**
     * 根据图片名称查询图片记录
     *
     * @param image_name
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT id,store_id,store_type,`group`,upload_mode,image_name,add_time " +
            " FROM lkt_files_record WHERE image_name = #{image_name} " +
            "order by add_time desc limit 1")
    FilesRecordModel getFileRecordByName(String image_name) throws LaiKeAPIException;

    /**
     * 根据图片名称删除图片记录
     *
     * @param image_name
     * @return
     * @throws LaiKeAPIException
     */
    @Delete("DELETE   FROM lkt_files_record WHERE image_name = #{image_name} ")
    void delFileRecordByName(String image_name) throws LaiKeAPIException;

    /**
     * 删除图片记录
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    void delFileRecord(Map<String, Object> map) throws LaiKeAPIException;

    @Select("<script> " +
            " SELECT image_name,store_id,store_type,add_time from lkt_files_record where " +
            " <if test='ids != null '> " +
            " <foreach collection=\"ids\" item=\"id\" separator=\",\" open=\" id in(\" close=\")\"> " +
            "   #{id,jdbcType=INTEGER} " +
            " </foreach> " +
            " </if>" +
            " </script> ")
    List<Map<String, Object>> getImgNameByIds(@Param("ids") List<String> ids) throws LaiKeAPIException;

    List<FilesRecordModel> batchGetByImageNames(@Param("storeId") String storeId,
                                                @Param("imageNames") List<String> imageNames);
}