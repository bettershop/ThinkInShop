package com.laiketui.domain.dictionary;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


/**
 * 国家列表
 *
 * @author Trick
 * @date 2020/12/31 10:27
 */
@Table(name = "lkt_ds_country")
public class CountryModel implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    private String name;

    private String zh_name;

    private String code;

    private String code2;

    private Integer num3;

    /**
     * 是否显示 1 显示 0 不显示
     */
    private Integer is_show;

    /**
     * 国旗
     */
    private String national_flag;

    public String getNational_flag() {
        return national_flag;
    }

    public void setNational_flag(String national_flag) {
        this.national_flag = national_flag;
    }

    /**
     * @return id
     */
    public Short getId()
    {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Short id)
    {
        this.id = id;
    }

    /**
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * @return zh_name
     */
    public String getZh_name()
    {
        return zh_name;
    }

    /**
     * @param zh_name
     */
    public void setZh_name(String zh_name)
    {
        this.zh_name = zh_name == null ? null : zh_name.trim();
    }

    /**
     * @return code
     */
    public String getCode()
    {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(String code)
    {
        this.code = code == null ? null : code.trim();
    }

    /**
     * @return code2
     */
    public String getCode2()
    {
        return code2;
    }

    /**
     * @param code2
     */
    public void setCode2(String code2)
    {
        this.code2 = code2 == null ? null : code2.trim();
    }

    /**
     * 获取是否显示 1 显示 0 不显示
     *
     * @return is_show - 是否显示 1 显示 0 不显示
     */
    public Integer getIs_show()
    {
        return is_show;
    }

    /**
     * 设置是否显示 1 显示 0 不显示
     *
     * @param is_show 是否显示 1 显示 0 不显示
     */
    public void setIs_show(Integer is_show)
    {
        this.is_show = is_show;
    }

    public Integer getNum3()
    {
        return num3;
    }

    public void setNum3(Integer num3)
    {
        this.num3 = num3;
    }
}