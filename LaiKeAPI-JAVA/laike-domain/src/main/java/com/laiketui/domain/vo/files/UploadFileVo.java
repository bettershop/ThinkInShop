package com.laiketui.domain.vo.files;

import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传参数
 *
 * @author Trick
 * @date 2021/7/8 11:39
 */
@ApiModel(description = "文件上传参数")
public class UploadFileVo extends MainVo
{

    @ApiModelProperty(value = "id", name = "id")
    private Integer         id;
    @ApiModelProperty(value = "店铺id", name = "mchId")
    private Integer         mchId;
    @ApiModelProperty(value = "供应商id", name = "supplierId")
    private Integer         supplierId;
    @ApiModelProperty(value = "分组id 默认-1(全部)", name = "groupId")
    private Integer         groupId = -1;
    @ApiModelProperty(value = "上传类型 1=本地,2=阿里云oss", name = "uploadType")
    private String          uploadType;
    @ApiModelProperty(value = "文件流集合", name = "image")
    @ParamsMapping("file")
    private MultipartFile[] image;
    @ApiModelProperty(value = "图片类型 0:业务图片 1：diy图片")
    private Integer img_type;
    @ApiModelProperty(value = "diy模块图片类型 0：系统图片 1：自定义图片")
    private Integer diy_img_type;
    @ApiModelProperty(value = "主题id")
    private Integer diyId;
    @ApiModelProperty(value = "图片名", name = "image_name")
    private String  image_name;
    @ApiModelProperty(value = "图片标题", name = "title")
    private String  title;
    @ApiModelProperty(value = "说明", name = "explain")
    private String  explain;
    @ApiModelProperty(value = "代替文本", name = "alternativeText")
    private String  alternativeText;
    @ApiModelProperty(value = "图像描述", name = "describe")
    private String  describe;
    @ApiModelProperty(value = "图片名称", name = "name")
    private String  name;
    @ApiModelProperty(value = "文件类型", name = "type")
    private String  type;
    @ApiModelProperty(value = "上传人", name = "add_user")
    private String  add_user;
    @ApiModelProperty(value = "确认覆盖提交", name = "coverage")
    private Integer coverage;

    public Integer getDiyId() {
        return diyId;
    }

    public void setDiyId(Integer diyId) {
        this.diyId = diyId;
    }

    public Integer getDiy_img_type() {
        return diy_img_type;
    }

    public void setDiy_img_type(Integer diy_img_type) {
        this.diy_img_type = diy_img_type;
    }

    public Integer getImg_type() {
        return img_type;
    }

    public void setImg_type(Integer img_type) {
        this.img_type = img_type;
    }

    public Integer getCoverage()
    {
        return coverage;
    }

    public void setCoverage(Integer coverage)
    {
        this.coverage = coverage;
    }

    public Integer getSupplierId()
    {
        return supplierId;
    }

    public String getImage_name()
    {
        return image_name;
    }

    public String getAdd_user()
    {
        return add_user;
    }

    public void setAdd_user(String add_user)
    {
        this.add_user = add_user;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setImage_name(String image_name)
    {
        this.image_name = image_name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setSupplierId(Integer supplierId)
    {
        this.supplierId = supplierId;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getMchId()
    {
        return mchId;
    }

    public void setMchId(Integer mchId)
    {
        this.mchId = mchId;
    }

    public Integer getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Integer groupId)
    {
        this.groupId = groupId;
    }

    public String getUploadType()
    {
        return uploadType;
    }

    public void setUploadType(String uploadType)
    {
        this.uploadType = uploadType;
    }

    public MultipartFile[] getImage()
    {
        return image;
    }

    public void setImage(MultipartFile[] image)
    {
        this.image = image;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getExplain()
    {
        return explain;
    }

    public void setExplain(String explain)
    {
        this.explain = explain;
    }

    public String getAlternativeText()
    {
        return alternativeText;
    }

    public void setAlternativeText(String alternativeText)
    {
        this.alternativeText = alternativeText;
    }

    public String getDescribe()
    {
        return describe;
    }

    public void setDescribe(String describe)
    {
        this.describe = describe;
    }
}
