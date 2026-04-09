package com.laiketui.domain.seata;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "t_kucun")
public class SeataStorage implements Serializable
{
    @Id
    private Integer sid;

    private Integer pid;

    private Integer num;

    /**
     * @return sid
     */
    public Integer getSid()
    {
        return sid;
    }

    /**
     * @param sid
     */
    public void setSid(Integer sid)
    {
        this.sid = sid;
    }

    /**
     * @return pid
     */
    public Integer getPid()
    {
        return pid;
    }

    /**
     * @param pid
     */
    public void setPid(Integer pid)
    {
        this.pid = pid;
    }

    /**
     * @return num
     */
    public Integer getNum()
    {
        return num;
    }

    /**
     * @param num
     */
    public void setNum(Integer num)
    {
        this.num = num;
    }
}