package com.laiketui.domain.config;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "lkt_map")
public class AdminCgModel implements Serializable
{
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "district_pid")
    private Integer districtPid;

    @Column(name = "district_show_order")
    private Integer districtShowOrder;

    @Column(name = "district_level")
    private Integer districtLevel;

    @Column(name = "district_childcount")
    private Integer district_ChildCount;

    @Column(name = "district_delete")
    private Integer districtDelete;

    @Column(name = "district_num")
    private Integer districtNum;

    @Column(name = "district_country_num")
    private Integer districtCountryNum;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getDistrictName()
    {
        return districtName;
    }

    public void setDistrictName(String districtName)
    {
        this.districtName = districtName;
    }

    public Integer getDistrictPid()
    {
        return districtPid;
    }

    public void setDistrictPid(Integer districtPid)
    {
        this.districtPid = districtPid;
    }

    public Integer getDistrictShowOrder()
    {
        return districtShowOrder;
    }

    public void setDistrictShowOrder(Integer districtShowOrder)
    {
        this.districtShowOrder = districtShowOrder;
    }

    public Integer getDistrictLevel()
    {
        return districtLevel;
    }

    public void setDistrictLevel(Integer districtLevel)
    {
        this.districtLevel = districtLevel;
    }

    public Integer getDistrict_ChildCount()
    {
        return district_ChildCount;
    }

    public void setDistrict_ChildCount(Integer district_ChildCount)
    {
        this.district_ChildCount = district_ChildCount;
    }

    public Integer getDistrictDelete()
    {
        return districtDelete;
    }

    public void setDistrictDelete(Integer districtDelete)
    {
        this.districtDelete = districtDelete;
    }

    public Integer getDistrictNum()
    {
        return districtNum;
    }

    public void setDistrictNum(Integer districtNum)
    {
        this.districtNum = districtNum;
    }

    public Integer getDistrictCountryNum()
    {
        return districtCountryNum;
    }

    public void setDistrictCountryNum(Integer districtCountryNum)
    {
        this.districtCountryNum = districtCountryNum;
    }
}