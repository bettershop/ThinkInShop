package com.laiketui.plugins.api.living.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/28
 */
public interface AppLivingCommissionService
{
    /**
     * 获取提现记录
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 提现
     *
     * @param vo
     * @param money
     * @throws LaiKeAPIException
     */
    Map<String, Object> withdrawal(MainVo vo, BigDecimal money) throws LaiKeAPIException;
}
