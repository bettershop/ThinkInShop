package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

public interface PublicSystemTellService
{

    /**
     * 标记公告以读
     *
     * @param vo
     * @param read_id    标记以读用户id
     * @param tell_id    公告id
     * @param isSupplier 是否未供应商
     * @throws LaiKeAPIException
     */
    void markToRead(MainVo vo, String read_id, Integer tell_id, boolean isSupplier) throws LaiKeAPIException;


    /**
     * 获取平台维护公告
     *
     * @param vo
     * @return
     */
    Map<String, Object> getUserTell(MainVo vo, String type) throws LaiKeAPIException;

}
