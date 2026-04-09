package com.laiketui.domain.vo.plugin.auction;

import lombok.Data;

/**
 * @Author: liuao
 * @Date: 2025-11-28-17:22
 * @Description:
 */
@Data
public class ChannelVo
{
    private Integer type;

    private String name;

    public ChannelVo(Integer type, String name) {
        this.type = type;
        this.name = name;
    }
}
