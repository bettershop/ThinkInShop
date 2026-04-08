package com.laiketui.common.kaptcha;

import com.google.code.kaptcha.GimpyEngine;

import java.awt.image.BufferedImage;

/**
 * 遮挡实现类 -- 无阻挡类
 */

public class WaterRippleConfig implements GimpyEngine
{
    public WaterRippleConfig()
    {
    }

    @Override
    public BufferedImage getDistortedImage(BufferedImage bufferedImage)
    {
        return bufferedImage;
    }
}
