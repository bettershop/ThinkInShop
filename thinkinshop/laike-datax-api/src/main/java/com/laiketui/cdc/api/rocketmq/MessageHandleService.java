package com.laiketui.cdc.api.rocketmq;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.datax.DataChangeInfo;

import java.io.Serializable;

/**
 * 消息处理接口
 */
public interface MessageHandleService extends Serializable {

    /**
     *  监听消息内容处理
     * @param dataChangeInfo 监听到的内容
     * @throws LaiKeAPIException
     */
    void handleMessage(DataChangeInfo dataChangeInfo ) throws LaiKeAPIException;

}
