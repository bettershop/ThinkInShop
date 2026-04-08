package com.laiketui.admins.admin.services.saas;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.saas.AdminDistrictService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.vo.saas.DistrictVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("adminDistrictService")
@Slf4j
public class AdminDistrictServiceImpl implements AdminDistrictService
{
    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Override
    public Map<String, Object> districtList(DistrictVo vo) throws LaiKeAPIException
    {
        log.info("districtList入参信息", JSON.toJSONString(vo));
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (vo.getId() != null)
            {
                AdminCgModel adminCgModel = new AdminCgModel();
                adminCgModel.setId(vo.getId());
                adminCgModel = adminCgModelMapper.selectOne(adminCgModel);
                change(vo, adminCgModel, false);
                resultMap.put("model", vo);
            }
            else
            {
                int                       total = adminCgModelMapper.countDistrict(vo);
                List<Map<String, Object>> list  = adminCgModelMapper.queryDistrictList(vo);
                if (!Objects.isNull(list))
                {
                    for (Map<String, Object> map : list)
                    {
                        AdminCgModel adminCgModel = new AdminCgModel();
                        Integer districtPid = MapUtils.getInteger(map, "district_pid");
                        if (Objects.nonNull(districtPid))
                        {
                            adminCgModel.setId(districtPid);
                            adminCgModel = adminCgModelMapper.selectOne(adminCgModel);
                            if (adminCgModel != null) {
                                map.put("district_pname", adminCgModel.getDistrictName());
                            }
                        }
                        else
                        {
                            map.put("district_pname", "-");
                        }
                    }
                }
                resultMap.put("list", list);
                resultMap.put("total", total);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            log.error("获取国际信息列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        log.info("districtList出参信息", JSON.toJSONString(resultMap));
        return resultMap;
    }

    public void change(DistrictVo vo, AdminCgModel adminCgModel, boolean vo2model)
    {
        if (vo2model)
        {
            adminCgModel.setId(vo.getId());
            adminCgModel.setDistrictDelete(vo.getDistrict_delete());
            adminCgModel.setDistrictName(vo.getDistrict_name());
            adminCgModel.setDistrictLevel(vo.getDistrict_level());
            adminCgModel.setDistrictNum(vo.getDistrict_num());
            adminCgModel.setDistrictPid(vo.getDistrict_pid());
            adminCgModel.setDistrictShowOrder(vo.getDistrict_show_order());
            adminCgModel.setDistrict_ChildCount(vo.getDistrict_childcount());
            adminCgModel.setDistrictCountryNum(vo.getDistrict_country_num());
        }
        else
        {
            vo.setDistrict_childcount(adminCgModel.getDistrict_ChildCount());
            vo.setDistrict_delete(adminCgModel.getDistrictDelete());
            vo.setDistrict_level(adminCgModel.getDistrictLevel());
            vo.setDistrict_num(adminCgModel.getDistrictNum());
            vo.setDistrict_name(adminCgModel.getDistrictName());
            vo.setDistrict_pid(adminCgModel.getDistrictPid());
            vo.setDistrict_show_order(adminCgModel.getDistrictShowOrder());
            vo.setDistrict_country_num(adminCgModel.getDistrictCountryNum());
            vo.setId(adminCgModel.getId());
        }

    }

    @Override
    public void saveOrEditDistrict(DistrictVo vo) throws LaiKeAPIException
    {
        log.info("saveOrEditCountry入参信息", JSON.toJSONString(vo));
        try
        {
            AdminCgModel adminCgModel = new AdminCgModel();
            change(vo, adminCgModel, true);
            if (adminCgModel.getId() != null)
            {
                adminCgModelMapper.updateByPrimaryKeySelective(adminCgModel);
            }
            else
            {
                AdminCgModel cgModel = new AdminCgModel();
                cgModel = adminCgModelMapper.selectOne(adminCgModel);
                if (cgModel != null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DQYCZ, "地区已存在", "saveOrEditDistrict");
                }
                adminCgModelMapper.insertSelective(adminCgModel);
            }

        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    @Override
    public void deleteDistrict(DistrictVo vo) throws LaiKeAPIException
    {
        try
        {
            log.info("deleteDistrict入参信息", JSON.toJSONString(vo));
            AdminCgModel adminCgModel = new AdminCgModel();
            String       ids          = vo.getIds();
            if (StringUtils.isEmpty(ids) && (vo.getId() == null))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "deleteDistrict");
            }

            if (StringUtils.isNotEmpty(ids))
            {
                for (String id : ids.split(","))
                {
                    if (StringUtils.isNotEmpty(id))
                    {
                        adminCgModel.setId(Integer.parseInt(id));
                        adminCgModelMapper.delete(adminCgModel);
                    }
                }
            }

            if (vo.getId() != null)
            {
                adminCgModel.setId(vo.getId());
                adminCgModelMapper.delete(adminCgModel);
            }
        }
        catch (Exception e)
        {
            log.error("操作失败，{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "deleteDistrict");
        }
    }

    @Override
    public Map<String, Object> allDistrict() throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> list = adminCgModelMapper.allDistrict();
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            log.error("获取国际信息列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

}
