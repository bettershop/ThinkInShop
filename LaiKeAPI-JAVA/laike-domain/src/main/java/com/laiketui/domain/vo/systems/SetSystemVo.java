package  com.laiketui.domain.vo.systems;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 添加/修改 系统配置
 *
 * @author Trick
 * @date 2021/1/19 9:16
 */
@ApiModel(description = "添加/修改 系统配置参数")
public class SetSystemVo extends MainVo
{

    @ApiModelProperty(value = "id", name = "id")
    private Integer id;
    @ApiModelProperty(value = "公司logo", name = "logoUrl")
    private String  logoUrl;
    @ApiModelProperty(value = "版权信息", name = "copyrightInformation")
    private String  copyrightInformation;
    @ApiModelProperty(value = "recordInformation", name = "备案信息")
    private String  recordInformation;
    @ApiModelProperty(value = "linkPageList", name = "登录页链接 json数组")
    private String  linkPageJson;
    @ApiModelProperty(value = "h5Domain", name = "h5地址")
    private String  h5Domain;
    @ApiModelProperty(value = "domainPath", name = "根路径")
    private String  domainPath;
    @ApiModelProperty(value = "storeIdPrefix", name = "商城id前缀")
    private String  storeIdPrefix;
    @ApiModelProperty(value = "adminDefaultPortrait", name = "添加管理员默认头像")
    private String  adminDefaultPortrait;

    public String getH5Domain()
    {
        return h5Domain;
    }

    public void setH5Domain(String h5Domain)
    {
        this.h5Domain = h5Domain;
    }

    public String getDomainPath()
    {
        return domainPath;
    }

    public void setDomainPath(String domainPath)
    {
        this.domainPath = domainPath;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
        this.logoUrl = logoUrl;
    }

    public String getCopyrightInformation()
    {
        return copyrightInformation;
    }

    public void setCopyrightInformation(String copyrightInformation)
    {
        this.copyrightInformation = copyrightInformation;
    }

    public String getRecordInformation()
    {
        return recordInformation;
    }

    public void setRecordInformation(String recordInformation)
    {
        this.recordInformation = recordInformation;
    }

    public String getLinkPageJson()
    {
        return linkPageJson;
    }

    public void setLinkPageJson(String linkPageJson)
    {
        this.linkPageJson = linkPageJson;
    }

    public String getStoreIdPrefix()
    {
        return storeIdPrefix;
    }

    public void setStoreIdPrefix(String storeIdPrefix)
    {
        this.storeIdPrefix = storeIdPrefix;
    }

    public String getAdminDefaultPortrait()
    {
        return adminDefaultPortrait;
    }

    public void setAdminDefaultPortrait(String adminDefaultPortrait)
    {
        this.adminDefaultPortrait = adminDefaultPortrait;
    }
}
