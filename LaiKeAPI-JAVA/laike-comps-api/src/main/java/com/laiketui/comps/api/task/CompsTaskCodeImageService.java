package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

public interface CompsTaskCodeImageService
{

    /**
     * 清理验证码图片：超过5分钟没有使用的验证码图片从数据库，minio或者阿里云oss删除。
     *
     * @throws LaiKeAPIException
     */
    void codeImgClear() throws LaiKeAPIException;


}
