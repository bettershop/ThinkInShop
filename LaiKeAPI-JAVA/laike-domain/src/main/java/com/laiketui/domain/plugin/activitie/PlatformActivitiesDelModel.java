package com.laiketui.domain.plugin.activitie;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "lkt_platform_activities_del")
public class PlatformActivitiesDelModel implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 平台活动ID
     */
    private Integer platform_activities_id;

    /**
     * 商户ID
     */
    private Integer mch_id;

    /**
     * @return id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取平台活动ID
     *
     * @return platform_activities_id - 平台活动ID
     */
    public Integer getPlatform_activities_id()
    {
        return platform_activities_id;
    }

    /**
     * 设置平台活动ID
     *
     * @param platform_activities_id 平台活动ID
     */
    public void setPlatform_activities_id(Integer platform_activities_id)
    {
        this.platform_activities_id = platform_activities_id;
    }

    /**
     * 获取商户ID
     *
     * @return mch_id - 商户ID
     */
    public Integer getMch_id()
    {
        return mch_id;
    }

    /**
     * 设置商户ID
     *
     * @param mch_id 商户ID
     */
    public void setMch_id(Integer mch_id)
    {
        this.mch_id = mch_id;
    }
}