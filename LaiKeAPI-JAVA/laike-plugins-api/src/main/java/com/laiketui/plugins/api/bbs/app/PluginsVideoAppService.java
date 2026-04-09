package com.laiketui.plugins.api.bbs.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @Author: liuao
 * @Date: 2025-10-10-11:20
 * @Description:
 */
public interface PluginsVideoAppService
{
    /**
     * 视频处理
     * @param requestBody
     */
    void processData(String requestBody) throws LaiKeAPIException;

    /**
     * 生成视频密钥
     * @param vo
     * @return
     */
    Map<String,Object> generateMediaSign(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取播放地址
     * @param vo
     * @param fileId
     * @return
     */
    Map<String,Object> getPlaybackAddress(MainVo vo, String fileId) throws LaiKeAPIException;
}
