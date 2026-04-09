package com.laiketui.domain.vo.invoice;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: sunH_
 * @Date: Create in 14:47 2022/7/28
 */
@ApiModel(description = "上传发票文件")
public class UploadInvoiceVo extends MainVo
{

    @ApiModelProperty(value = "发票id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "文件路径", name = "filePath")
    private String  filePath;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getFile()
    {
        return filePath;
    }

    public void setFile(String file)
    {
        this.filePath = file;
    }
}
