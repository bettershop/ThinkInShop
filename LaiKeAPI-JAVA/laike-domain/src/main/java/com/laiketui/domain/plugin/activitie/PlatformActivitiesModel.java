package com.laiketui.domain.plugin.activitie;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


@Table(name = "lkt_platform_activities")
public class PlatformActivitiesModel implements Serializable
{
    /**
     * id自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商品id
     */
    private Integer store_id;

    /**
     * 封面图片
     */
    private String image;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动类型 如 pt:拼团 ms:秒杀
     */
    private String type;

    /**
     * 活动状态 （1未开始 2进行中 3结束）
     */
    private Integer status;

    /**
     * 默认数量（秒杀）
     */
    private Integer default_num;

    /**
     * 秒杀时段（秒杀）
     */
    private String buy_time;

    /**
     * 拼团人数（拼团）
     */
    private Integer group_man;

    /**
     * 参团折扣（拼团）
     */
    private Integer group_can;

    /**
     * 开团折扣（拼团）
     */
    private Integer group_kai;

    /**
     * 拼团时间（拼团）
     */
    private Integer group_time;

    /**
     * 开团数量限制（拼团）
     */
    private Integer group_kai_num;

    /**
     * 参团数量限制（拼团）
     */
    private Integer group_can_num;

    /**
     * 是否开启参团重复 （1开启 0不开启）（拼团）
     */
    private Integer group_can_again;

    /**
     * 是否免邮(1 是 0 否)
     */
    private Integer free_freight;

    /**
     * 开始时间
     */
    private Date starttime;

    /**
     * 结束时间
     */
    private Date endtime;

    /**
     * 报名开始时间
     */
    private Date join_starttime;

    /**
     * 报名结束时间
     */
    private Date join_endtime;

    /**
     * 是否删除(1 是 0 否)
     */
    private Integer isdelete;

    /**
     * 添加日期
     */
    private Date addtime;

    /**
     * 获取id自增
     *
     * @return id - id自增
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * 设置id自增
     *
     * @param id id自增
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * 获取商品id
     *
     * @return store_id - 商品id
     */
    public Integer getStore_id()
    {
        return store_id;
    }

    /**
     * 设置商品id
     *
     * @param store_id 商品id
     */
    public void setStore_id(Integer store_id)
    {
        this.store_id = store_id;
    }

    /**
     * 获取封面图片
     *
     * @return image - 封面图片
     */
    public String getImage()
    {
        return image;
    }

    /**
     * 设置封面图片
     *
     * @param image 封面图片
     */
    public void setImage(String image)
    {
        this.image = image == null ? null : image.trim();
    }

    /**
     * 获取活动名称
     *
     * @return name - 活动名称
     */
    public String getName()
    {
        return name;
    }

    /**
     * 设置活动名称
     *
     * @param name 活动名称
     */
    public void setName(String name)
    {
        this.name = name == null ? null : name.trim();
    }

    /**
     * 获取活动类型 如 pt:拼团 ms:秒杀
     *
     * @return type - 活动类型 如 pt:拼团 ms:秒杀
     */
    public String getType()
    {
        return type;
    }

    /**
     * 设置活动类型 如 pt:拼团 ms:秒杀
     *
     * @param type 活动类型 如 pt:拼团 ms:秒杀
     */
    public void setType(String type)
    {
        this.type = type == null ? null : type.trim();
    }

    /**
     * 获取活动状态 （1未开始 2进行中 3结束）
     *
     * @return status - 活动状态 （1未开始 2进行中 3结束）
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置活动状态 （1未开始 2进行中 3结束）
     *
     * @param status 活动状态 （1未开始 2进行中 3结束）
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * 获取默认数量（秒杀）
     *
     * @return default_num - 默认数量（秒杀）
     */
    public Integer getDefault_num()
    {
        return default_num;
    }

    /**
     * 设置默认数量（秒杀）
     *
     * @param default_num 默认数量（秒杀）
     */
    public void setDefault_num(Integer default_num)
    {
        this.default_num = default_num;
    }

    /**
     * 获取秒杀时段（秒杀）
     *
     * @return buy_time - 秒杀时段（秒杀）
     */
    public String getBuy_time()
    {
        return buy_time;
    }

    /**
     * 设置秒杀时段（秒杀）
     *
     * @param buy_time 秒杀时段（秒杀）
     */
    public void setBuy_time(String buy_time)
    {
        this.buy_time = buy_time == null ? null : buy_time.trim();
    }

    /**
     * 获取拼团人数（拼团）
     *
     * @return group_man - 拼团人数（拼团）
     */
    public Integer getGroup_man()
    {
        return group_man;
    }

    /**
     * 设置拼团人数（拼团）
     *
     * @param group_man 拼团人数（拼团）
     */
    public void setGroup_man(Integer group_man)
    {
        this.group_man = group_man;
    }

    /**
     * 获取参团折扣（拼团）
     *
     * @return group_can - 参团折扣（拼团）
     */
    public Integer getGroup_can()
    {
        return group_can;
    }

    /**
     * 设置参团折扣（拼团）
     *
     * @param group_can 参团折扣（拼团）
     */
    public void setGroup_can(Integer group_can)
    {
        this.group_can = group_can;
    }

    /**
     * 获取开团折扣（拼团）
     *
     * @return group_kai - 开团折扣（拼团）
     */
    public Integer getGroup_kai()
    {
        return group_kai;
    }

    /**
     * 设置开团折扣（拼团）
     *
     * @param group_kai 开团折扣（拼团）
     */
    public void setGroup_kai(Integer group_kai)
    {
        this.group_kai = group_kai;
    }

    /**
     * 获取拼团时间（拼团）
     *
     * @return group_time - 拼团时间（拼团）
     */
    public Integer getGroup_time()
    {
        return group_time;
    }

    /**
     * 设置拼团时间（拼团）
     *
     * @param group_time 拼团时间（拼团）
     */
    public void setGroup_time(Integer group_time)
    {
        this.group_time = group_time;
    }

    /**
     * 获取开团数量限制（拼团）
     *
     * @return group_kai_num - 开团数量限制（拼团）
     */
    public Integer getGroup_kai_num()
    {
        return group_kai_num;
    }

    /**
     * 设置开团数量限制（拼团）
     *
     * @param group_kai_num 开团数量限制（拼团）
     */
    public void setGroup_kai_num(Integer group_kai_num)
    {
        this.group_kai_num = group_kai_num;
    }

    /**
     * 获取参团数量限制（拼团）
     *
     * @return group_can_num - 参团数量限制（拼团）
     */
    public Integer getGroup_can_num()
    {
        return group_can_num;
    }

    /**
     * 设置参团数量限制（拼团）
     *
     * @param group_can_num 参团数量限制（拼团）
     */
    public void setGroup_can_num(Integer group_can_num)
    {
        this.group_can_num = group_can_num;
    }

    /**
     * 获取是否开启参团重复 （1开启 0不开启）（拼团）
     *
     * @return group_can_again - 是否开启参团重复 （1开启 0不开启）（拼团）
     */
    public Integer getGroup_can_again()
    {
        return group_can_again;
    }

    /**
     * 设置是否开启参团重复 （1开启 0不开启）（拼团）
     *
     * @param group_can_again 是否开启参团重复 （1开启 0不开启）（拼团）
     */
    public void setGroup_can_again(Integer group_can_again)
    {
        this.group_can_again = group_can_again;
    }

    /**
     * 获取是否免邮(1 是 0 否)
     *
     * @return free_freight - 是否免邮(1 是 0 否)
     */
    public Integer getFree_freight()
    {
        return free_freight;
    }

    /**
     * 设置是否免邮(1 是 0 否)
     *
     * @param free_freight 是否免邮(1 是 0 否)
     */
    public void setFree_freight(Integer free_freight)
    {
        this.free_freight = free_freight;
    }

    /**
     * 获取开始时间
     *
     * @return starttime - 开始时间
     */
    public Date getStarttime()
    {
        return starttime;
    }

    /**
     * 设置开始时间
     *
     * @param starttime 开始时间
     */
    public void setStarttime(Date starttime)
    {
        this.starttime = starttime;
    }

    /**
     * 获取结束时间
     *
     * @return endtime - 结束时间
     */
    public Date getEndtime()
    {
        return endtime;
    }

    /**
     * 设置结束时间
     *
     * @param endtime 结束时间
     */
    public void setEndtime(Date endtime)
    {
        this.endtime = endtime;
    }

    /**
     * 获取报名开始时间
     *
     * @return join_starttime - 报名开始时间
     */
    public Date getJoin_starttime()
    {
        return join_starttime;
    }

    /**
     * 设置报名开始时间
     *
     * @param join_starttime 报名开始时间
     */
    public void setJoin_starttime(Date join_starttime)
    {
        this.join_starttime = join_starttime;
    }

    /**
     * 获取报名结束时间
     *
     * @return join_endtime - 报名结束时间
     */
    public Date getJoin_endtime()
    {
        return join_endtime;
    }

    /**
     * 设置报名结束时间
     *
     * @param join_endtime 报名结束时间
     */
    public void setJoin_endtime(Date join_endtime)
    {
        this.join_endtime = join_endtime;
    }

    /**
     * 获取是否删除(1 是 0 否)
     *
     * @return isdelete - 是否删除(1 是 0 否)
     */
    public Integer getIsdelete()
    {
        return isdelete;
    }

    /**
     * 设置是否删除(1 是 0 否)
     *
     * @param isdelete 是否删除(1 是 0 否)
     */
    public void setIsdelete(Integer isdelete)
    {
        this.isdelete = isdelete;
    }

    /**
     * 获取添加日期
     *
     * @return addtime - 添加日期
     */
    public Date getAddtime()
    {
        return addtime;
    }

    /**
     * 设置添加日期
     *
     * @param addtime 添加日期
     */
    public void setAddtime(Date addtime)
    {
        this.addtime = addtime;
    }
}