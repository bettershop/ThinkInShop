package com.laiketui.common.consts;

/**
 * @Author: liuao
 * @Date: 2026-01-28-14:46
 * @Description:
 */
public enum PluginsCodeEnum
{
    /**
     * 限时折扣
     */
    FLASH_SALE("flashsale", "限时折扣"),

    /**
     * 积分商城
     */
    INTEGRAL("integral", "积分商城"),

    /**
     * 会员中心
     */
    MEMBER("member", "会员中心"),

    /**
     * 领券中心
     */
    COUPON("coupon", "领券中心"),

    /**
     * 分销中心
     */
    DISTRIBUTION("distribution", "分销中心"),

    /**
     * 超值拼团
     */
    GO_GROUP("go_group", "超值拼团"),

    /**
     * 限时秒杀
     */
    SECONDS("seconds", "限时秒杀"),

    /**
     * 预售中心
     */
    PRESELL("presell", "预售中心"),

    /**
     * 直播
     */
    LIVING("living", "直播"),

    /**
     * 平台竞拍
     */
    AUCTION("auction", "平台竞拍"),

    /**
     * 优选店铺
     */
    MCH("mch", "优选店铺"),

    /**
     * 种草社区
     */
    ADVERTISING("advertising", "种草社区");

    private final String code;
    private final String name;

    PluginsCodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code查找对应的枚举（常用辅助方法）
     * @param code 模块编码
     * @return 对应的枚举，若不存在返回null
     */
    public static String getByCode(String code)
    {
        if (code == null || code.isEmpty())
        {
            return "";
        }
        for (PluginsCodeEnum module : values()) {

            if (module.code.equals(code))
            {
                return module.getName();
            }
        }
        return "";
    }

    /**
     * 根据中文名称查找对应的枚举
     * @param name 模块中文名称
     * @return 对应的枚举，若不存在返回null
     */
    public static PluginsCodeEnum getByName(String name)
    {
        if (name == null || name.isEmpty())
        {
            return null;
        }
        for (PluginsCodeEnum module : values())
        {
            if (module.name.equals(name))
            {
                return module;
            }
        }
        return null;
    }
}
