package com.laiketui.common.kaptcha;

import java.util.Properties;

/**
 * KaptchaConfig
 * Properties自定义配置
 *
 * @author gp
 * @date 2023-10-07
 */
public class PropertiesConfig
{
    /**
     * 蓝色，无边框，背景颜色-白
     *
     * @param properties
     * @return Properties
     */
    public static Properties getPropertiesConfig(Properties properties)
    {
        // 图片边框
        properties.setProperty("kaptcha.border", "no");
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "0,102,204");
        //干扰器实现类 --无干扰器
        properties.setProperty("kaptcha.noise.impl", "com.laiketui.common.kaptcha.DefaultNoiseConfig");
        //遮挡实现类 -- 无遮挡实现类
        properties.setProperty("kaptcha.obscurificator.impl", "com.laiketui.common.kaptcha.WaterRippleConfig");
        //渐变背景颜色开始值
        properties.setProperty("kaptcha.background.clear.from", "255,255,255");
        //渐变背景颜色结束值
        properties.setProperty("kaptcha.background.clear.to", "255,255,255");

        return properties;
    }


    /**
     * 红色，无边框，背景颜色-白
     *
     * @param properties
     * @return Properties
     */
    public static Properties getPropertiesConfig02(Properties properties)
    {
        // 图片边框
        properties.setProperty("kaptcha.border", "no");
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "red");
        //干扰器实现类 --无干扰器
        properties.setProperty("kaptcha.noise.impl", "com.laiketui.common.kaptcha.DefaultNoiseConfig");
        //遮挡实现类 -- 无遮挡实现类
        properties.setProperty("kaptcha.obscurificator.impl", "com.laiketui.common.kaptcha.WaterRippleConfig");
        //渐变背景颜色开始值
        properties.setProperty("kaptcha.background.clear.from", "255,255,255");
        //渐变背景颜色结束值
        properties.setProperty("kaptcha.background.clear.to", "255,255,255");

        return properties;
    }
}
