package com.laiketui.plugins.diy.dubbo;

import com.laiketui.common.mapper.DiyProjectModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyProjectVo;
import com.laiketui.plugins.api.diy.PluginsDiyProjectAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PluginsDiyProjectAdminServiceImpl implements PluginsDiyProjectAdminService
{

    @Autowired
    private DiyProjectModelMapper diyProjectModelMapper;

    @Override
    public Map<String, Object> getDiyProjectList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        return diyProjectModelMapper.getDiyProjectList(vo, id);
    }

    @Override
    public void addOrUpdateDiyProject(DiyProjectVo vo) throws LaiKeAPIException
    {
        diyProjectModelMapper.addOrUpdateDiyProject(vo);
    }

    @Override
    public void diyProjectStatus(MainVo vo, Integer id) throws LaiKeAPIException
    {
        diyProjectModelMapper.diyProjectStatus(vo, id);
    }

    @Override
    public void delDiyProject(MainVo vo, int id) throws LaiKeAPIException
    {
        diyProjectModelMapper.delDiyProject(vo, id);
    }

    @Override
    public Map<String, Object> getDiyProjectById(MainVo vo, int id) throws LaiKeAPIException
    {
        return diyProjectModelMapper.getDiyProjectById(vo, id);
    }
}