package com.laiketui.comps.task.services;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.FilesRecordModelMapper;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.comps.api.task.CompsTaskCodeImageService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.log.FilesRecordModel;
import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service("codeImageTaskService")
public class CompsTaskCodeImageServiceImpl implements CompsTaskCodeImageService
{

    private final Logger logger = LoggerFactory.getLogger(CompsTaskApiCheckServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private FilesRecordModelMapper filesRecordModelMapper;

    /**
     * 300秒 没有使用验证码
     */
    long OVER_TIME = 300;

    @Override
    public void codeImgClear() throws LaiKeAPIException
    {
        try
        {
            Set<String>      keys             = redisUtil.keyScan(GloabConst.RedisHeaderKey.LOGIN_CODE_KEY + SplitUtils.XX);
            FilesRecordModel filesRecordModel = null;
            for (String codeImgKey : keys)
            {
                XxlJobHelper.log("开始删除:{}", codeImgKey);
                Object imgUrl  = redisUtil.get(codeImgKey);
                long   exptime = redisUtil.getExpire(codeImgKey);
                if (imgUrl != null)
                {
                    // redis中 过期时间exptime小于等于25分钟的验证码图片都删掉 ：也即验证码5分钟没有使用则删掉。
                    if (exptime <= (GloabConst.LktConfig.IMG_CODE_EXISTENCE_TIME - OVER_TIME))
                    {
                        filesRecordModel = filesRecordModelMapper.getFileRecordByName(ImgUploadUtils.getUrlImgByName(imgUrl.toString(), true));
                        publiceService.delFileRecoderModel(filesRecordModel);
                    }
                }
                XxlJobHelper.log("删除成功:{}", codeImgKey);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("检查接口出错!");
        }
        finally
        {
        }
    }

}
