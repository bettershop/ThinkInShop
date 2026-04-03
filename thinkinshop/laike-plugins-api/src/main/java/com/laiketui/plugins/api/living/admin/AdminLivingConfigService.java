package com.laiketui.plugins.api.living.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.living.AddLivingConfigVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/5/28
 */
public interface AdminLivingConfigService
{

    /**
     * 查询直播配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加直播设置
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addLivingConfig(AddLivingConfigVo vo) throws LaiKeAPIException;

    void addSensitive(MainVo vo, String word, Integer id);

    void deleteSensitive(MainVo vo, String ids);

    Map<String, Object> selectSensitive(MainVo vo, String word);

    void addSensitives(MainVo vo, MultipartFile[] words);
}
