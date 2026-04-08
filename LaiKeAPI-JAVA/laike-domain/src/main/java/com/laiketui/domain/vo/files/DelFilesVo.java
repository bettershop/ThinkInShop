package com.laiketui.domain.vo.files;

import com.laiketui.domain.upload.UploadImagModel;
import com.laiketui.domain.vo.MainVo;

import java.util.Date;

/**
 * 删除文件参数
 *
 * @author Trick
 * @date 2021/7/8 18:13
 */
public class DelFilesVo extends MainVo
{

    /**
     * 上传类型
     */
    private String uploadType;
    private String fileName;
    private int    storeId;
    private int    storeType;

    /**
     * 图片上传时间
     */
    private Date   addTime;
    /**
     * 目录
     */
    private String catalogue;

    /**
     * oss参数
     */
    private UploadImagModel uploadImagModel;

    public String getUploadType()
    {
        return uploadType;
    }

    public void setUploadType(String uploadType)
    {
        this.uploadType = uploadType;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public int getStoreId()
    {
        return storeId;
    }

    public void setStoreId(int storeId)
    {
        this.storeId = storeId;
    }

    public int getStoreType()
    {
        return storeType;
    }

    public void setStoreType(int storeType)
    {
        this.storeType = storeType;
    }

    public String getCatalogue()
    {
        return catalogue;
    }

    public void setCatalogue(String catalogue)
    {
        this.catalogue = catalogue;
    }

    public UploadImagModel getUploadImagModel()
    {
        return uploadImagModel;
    }

    public void setUploadImagModel(UploadImagModel uploadImagModel)
    {
        this.uploadImagModel = uploadImagModel;
    }

    public Date getAddTime()
    {
        return addTime;
    }

    public void setAddTime(Date addTime)
    {
        this.addTime = addTime;
    }
}
