package com.laiketui.admins.admin.services.systems;

import com.laiketui.admins.api.admin.systems.AdminSearchService;
import com.laiketui.common.mapper.HotKeywordsModelMapper;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.HotKeywordsModel;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 搜索配置
 *
 * @author Trick
 * @date 2021/1/15 9:31
 */
@Service
public class AdminSearchServiceImpl implements AdminSearchService
{
    private final Logger logger = LoggerFactory.getLogger(AdminSearchServiceImpl.class);

    @Override
    public Map<String, Object> getSearchConfigIndex(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            HotKeywordsModel hotKeywordsModel = new HotKeywordsModel();
            hotKeywordsModel.setStore_id(vo.getStoreId());
            hotKeywordsModel = hotKeywordsModelMapper.selectOne(hotKeywordsModel);

            resultMap.put("data", hotKeywordsModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取查询配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSearchConfigIndex");
        }
        return resultMap;
    }

    @Override
    public boolean addSearchConfig(MainVo vo, int isOpen, Integer limitNum, String keyword) throws LaiKeAPIException
    {
        try
        {
            int              count;
            HotKeywordsModel hotKeywordsModel = new HotKeywordsModel();
            hotKeywordsModel.setStore_id(vo.getStoreId());
            hotKeywordsModel = hotKeywordsModelMapper.selectOne(hotKeywordsModel);
            if (isOpen == 1)
            {
                if (limitNum == null || limitNum < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZSYG, "上限至少一个");
                }
                if (StringUtils.isEmpty(keyword))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GJZBNWK, "关键词不能为空");
                }
                else if (keyword.split(SplitUtils.DH).length > limitNum)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GJZBNDYXZSL, "关键词不能大于限制数量");
                }
            }

            HotKeywordsModel hotKeywordsModelSave = new HotKeywordsModel();
            hotKeywordsModelSave.setNum(limitNum);
            hotKeywordsModelSave.setIs_open(isOpen);
            hotKeywordsModelSave.setKeyword(keyword);
            if (hotKeywordsModel != null)
            {
                hotKeywordsModelSave.setId(hotKeywordsModel.getId());
                count = hotKeywordsModelMapper.updateByPrimaryKeySelective(hotKeywordsModelSave);
            }
            else
            {
                hotKeywordsModelSave.setStore_id(vo.getStoreId());
                count = hotKeywordsModelMapper.insertSelective(hotKeywordsModelSave);
            }

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑搜搜配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSearchConfig");
        }
    }

    @Autowired
    private HotKeywordsModelMapper hotKeywordsModelMapper;
}

