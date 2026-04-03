package com.laiketui.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "lkt_lang")
public class LangModel
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 语言名称
     */
    private String lang_name;

    /**
     * 语言编码
     */
    private String lang_code;

    /**
     * 展示位置
     */
    private Integer show_num;

    /**
     * 操作时间
     */
    private Date op_time;

    /**
     * 是否回收 1 不回收 2 回收
     */
    private int recycle;

    /**
     * 是否显示 1显示 2 不显示
     */
    private int is_show = 1;

    public Date getOp_time()
    {
        return op_time;
    }

    public void setOp_time(Date op_time)
    {
        this.op_time = op_time;
    }

    public int getRecycle()
    {
        return recycle;
    }

    public void setRecycle(int recycle)
    {
        this.recycle = recycle;
    }

    public int getIs_show()
    {
        return is_show;
    }

    public void setIs_show(int is_show)
    {
        this.is_show = is_show;
    }

    /**
     * 获取语言名称
     *
     * @return lang_name - 语言名称
     */
    public String getLang_name()
    {
        return lang_name;
    }

    /**
     * 设置语言名称
     *
     * @param lang_name 语言名称
     */
    public void setLang_name(String lang_name)
    {
        this.lang_name = lang_name == null ? null : lang_name.trim();
    }

    /**
     * 获取语言编码
     *
     * @return lang_code - 语言编码
     */
    public String getLang_code()
    {
        return lang_code;
    }

    /**
     * 设置语言编码
     *
     * @param lang_code 语言编码
     */
    public void setLang_code(String lang_code)
    {
        this.lang_code = lang_code == null ? null : lang_code.trim();
    }

    /**
     * 获取展示位置
     *
     * @return show_num - 展示位置
     */
    public Integer getShow_num()
    {
        return show_num;
    }

    /**
     * 设置展示位置
     *
     * @param show_num 展示位置
     */
    public void setShow_num(Integer show_num)
    {
        this.show_num = show_num;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

}