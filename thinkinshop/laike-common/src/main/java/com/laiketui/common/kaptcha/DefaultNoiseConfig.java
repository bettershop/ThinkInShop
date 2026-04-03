package com.laiketui.common.kaptcha;

import com.google.code.kaptcha.NoiseProducer;

import java.awt.image.BufferedImage;

/**
 * 干扰器实现类 --无干扰器
 */

public class DefaultNoiseConfig implements NoiseProducer
{
    public DefaultNoiseConfig()
    {
    }

    @Override
    public void makeNoise(BufferedImage bufferedImage, float v, float v1, float v2, float v3)
    {
    }
}
