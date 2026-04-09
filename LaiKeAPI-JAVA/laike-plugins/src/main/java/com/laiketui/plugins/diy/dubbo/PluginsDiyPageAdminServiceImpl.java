package com.laiketui.plugins.diy.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.*;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.DiyModelMapper;
import com.laiketui.common.mapper.DiyPageBindModelMapper;
import com.laiketui.common.mapper.DiyPageModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.diy.DiyPageModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.plugin.DiyModel;
import com.laiketui.domain.plugin.DiyPageBindModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyPageVo;
import com.laiketui.plugins.api.diy.PluginsDiyPageAdminService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PluginsDiyPageAdminServiceImpl implements PluginsDiyPageAdminService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DiyPageModelMapper diyPageModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private DiyPageBindModelMapper diyPageBindModelMapper;

    @Autowired
    private DiyModelMapper diyModelMapper;

    @Override
    public Map<String, Object> getDiyPageList(MainVo vo, String name, Integer status) throws LaiKeAPIException {
        Map<String, Object> result = new HashMap<>();
        try {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> param = new HashMap<>();
            param.put("store_id", vo.getStoreId());
            param.put("page_name", name);
            param.put("status", status);
            param.put("type", 2);
            param.put("recycle", DictionaryConst.WhetherMaven.WHETHER_NO);
            int total = diyPageModelMapper.countDynamic(param);
            List<Map<String, Object>> list = new ArrayList<>();
            if (total > 0) {
                param.put("pageSize", vo.getPageSize());
                param.put("pageNo", vo.getPageNo());
                list = diyPageModelMapper.getDiyPageList(param);
            }
            result.put("list", list);
            result.put("total", total);
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            logger.error("error：：：：{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, e.getMessage(), "delDiy");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addOrUpdateDiyPage(DiyPageVo vo) throws LaiKeAPIException {
        Map<String, Object> result = new HashMap<>();
        try {
            AdminModel adminUserCache = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            DiyPageModel diyPageModel = new DiyPageModel();
            diyPageModel.setStore_id(vo.getStoreId());

            int count = diyPageModelMapper.checkCountName(vo.getPage_name(), Objects.isNull(vo.getId()) ? null : vo.getId());
            if (count > 0) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YMMCYCZ, "页面名称已存在");
            }

            //新增绑定关系
            if (Objects.isNull(vo.getId()) && vo.getType() == 1) {
                if (StringUtils.isEmpty(vo.getLink())) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LJDZBNWK, "链接地址不能为空");
                }
                diyPageModel.setPage_key(vo.getPage_key());
                diyPageModel.setPage_name(vo.getPage_name());
                diyPageModel.setLink(vo.getLink());
                diyPageModel.setCreate_by(adminUserCache.getName());
                diyPageModel.setType(vo.getPage_type());
                diyPageModelMapper.insertSelective(diyPageModel);
                result.put("id", diyPageModel.getId());
            } else {
                diyPageModel.setId(vo.getId());
                diyPageModel.setPage_context(vo.getPage_context());
                diyPageModel.setImage(vo.getUrl());
                diyPageModelMapper.updateByPrimaryKeySelective(diyPageModel);

                //编辑页面，页面绑定的所有主题信息都会修改
                List<Integer> diyIds = diyPageBindModelMapper.getDiyIdByPageId(vo.getId());
                if (CollectionUtils.isNotEmpty(diyIds)) {
                    for (Integer diyId : diyIds) {
                        DiyModel diyModel = diyModelMapper.selectByPrimaryKey(diyId);
                        if (StringUtils.isNotEmpty(diyModel.getValue())) {
                            updatePageJson(diyModel.getValue(), diyId, vo.getLink(), vo.getPage_context());
                        }
                    }
                }
            }
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
        return result;
    }

    /**
     * 编辑页面json，同步主题json
     *
     * @param value
     * @param diyId
     * @param pageKey
     * @param pageJson
     */
    private void updatePageJson(String value, Integer diyId, String pageKey, String pageJson) {
        boolean flag = false;
        JSONArray diyValueJson = JSON.parseArray(value);
        for (Object json : diyValueJson) {
            JSONObject jsonObject = (JSONObject) json;
            if (jsonObject.containsKey(pageKey)) {
                jsonObject.put("defaultArray", pageJson);
                flag = true;
            }
        }
        if (flag) {
            DiyModel diyModel = new DiyModel();
            diyModel.setId(diyId);
            diyModel.setValue(JSONObject.toJSONString(diyValueJson));
            diyModelMapper.updateByPrimaryKeySelective(diyModel);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBindDiyPage(MainVo vo, Integer diyId, Integer pageId) throws LaiKeAPIException {
        try {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            List<DiyPageBindModel> diyPageBindModelList = diyPageBindModelMapper.getBindUnitByDiyIdAndPageId(diyId, pageId);
            if (CollectionUtils.isNotEmpty(diyPageBindModelList))
            {

                //diy数据
                DiyModel diyModel = diyModelMapper.selectByPrimaryKey(diyId);

                String diyJson = diyModel.getValue();


                for (DiyPageBindModel diyPageBindModel : diyPageBindModelList)
                {
                    diyJson = publiceService.delBindUnit(pageId, diyId, diyPageBindModel.getUnit(), diyPageBindModel.getLink_key(), diyPageBindModel.getId(), diyJson, true);
                }
                if (StringUtils.isNotEmpty(diyJson))
                {
                    diyModel.setValue(diyJson);
                    diyModelMapper.updateByPrimaryKeySelective(diyModel);
                }
            }
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
    }

    @Override
    public Map<String, Object> getDiyPageById(MainVo vo, int id) throws LaiKeAPIException {
        try {
            Map<String, Object> result = new HashMap<>();
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            DiyPageModel diyPageModel = diyPageModelMapper.selectByPrimaryKey(id);

            Map<String, Object> param = new HashMap<>();
            param.put("id", id);
            param.put("groupBy", "groupBy");
            List<Map<String, Object>> bindDiyList = diyPageBindModelMapper.getBindDiyList(param);
            result.put("bindDiyList", bindDiyList);
            result.put("model", diyPageModel);
            return result;
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
    }

    @Override
    public Map<String, Object> getDiyPageBindList(MainVo vo, Integer id, String name) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> param = new HashMap<>();
            param.put("id", id);
            param.put("name", name);
            param.put("groupBy", "groupBy");

            int total = diyPageBindModelMapper.countByPageId(id, name);
            if (total > 0)
            {
                /*param.put("pageSize", vo.getPageSize());
                param.put("pageNo", vo.getPageNo());*/
                list = diyPageBindModelMapper.getBindDiyList(param);
            }
            result.put("list", list);
            result.put("total", CollectionUtils.isEmpty(list) ? 0 : list.size());
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delDiyPage(MainVo vo, Integer id) {
        try {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            DiyPageBindModel diyPageBindModel = new DiyPageBindModel();
            diyPageBindModel.setDiy_page_id(id);

            DiyPageModel diyPageModel = diyPageModelMapper.selectByPrimaryKey(id);
            String link = diyPageModel.getLink();

            //获取绑定了该页面的主题
            List<DiyPageBindModel> diyPageBindModelList = diyPageBindModelMapper.select(diyPageBindModel);
            if (CollectionUtils.isNotEmpty(diyPageBindModelList))
            {
                Set<Integer> diyIdList = diyPageBindModelList.stream().map(DiyPageBindModel::getDiy_id).collect(Collectors.toSet());
                for (Integer diyId : diyIdList)
                {
                    DiyModel diyModel = diyModelMapper.selectByPrimaryKey(diyId);
                    if (Objects.nonNull(diyModel))
                    {
                        String value = diyModel.getValue();
                        if (StringUtils.isNotEmpty(value))
                        {
                            //String newValue = value.replace(link, "");
                            String newValue = replaceLinkInLargeJson(value, link);
                            if (StringUtils.isNotEmpty(newValue))
                            {
                                diyModel.setValue(newValue);
                                int count = diyModelMapper.updateByPrimaryKeySelective(diyModel);
                                if (count < 1)
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
                                }
                            }
                        }
                    }
                }
            }
            diyPageBindModelMapper.delete(diyPageBindModel);
            diyPageModelMapper.remove(id);
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
    }


    /**
     * 替换主题json中绑定的页面，防止内存溢出
     * @param oldJson
     * @param targetLink
     * @return
     */
    private String replaceLinkInLargeJson(String oldJson, String targetLink) {
        if (oldJson == null || oldJson.isEmpty()) {
            return oldJson;
        }

        JsonFactory factory = new JsonFactory();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             JsonParser parser = factory.createParser(oldJson);
             JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8)) {

            while (true) {
                JsonToken currentToken = parser.nextToken();
                if (currentToken == null) {
                    break; // 解析结束
                }

                // 只处理字段名，且字段名为"value"
                if (currentToken == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    // 先写入字段名
                    generator.writeFieldName(fieldName);

                    // 读取字段值的token
                    JsonToken valueToken = parser.nextToken();
                    if (valueToken == null) {
                        break; // 异常结构，提前退出
                    }

                    // 仅当字段名是"value"且值是字符串类型，才判断是否替换
                    if ("value".equals(fieldName) && valueToken == JsonToken.VALUE_STRING) {
                        String fieldValue = parser.getValueAsString();
                        if (targetLink.equals(fieldValue)) {
                            // 目标值，替换为空字符串
                            generator.writeString("");
                        } else {
                            // 非目标值，原样写入字符串
                            generator.writeString(fieldValue);
                        }
                    } else {
                        // 非目标字段名，或值不是字符串类型，完整复制值结构
                        generator.copyCurrentEvent(parser); // 复制当前值的所有内容（包括对象/数组内部结构）
                    }
                } else {
                    // 非字段名（如对象开始、数组开始、结束符等），直接复制
                    generator.copyCurrentEvent(parser);
                }
            }

            generator.flush();
            return out.toString("UTF-8");
        } catch (Exception e) {
            return null;
        }
    }



    @Override
    public DiyPageModel getPageJson(MainVo vo,Integer diyId, String link) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            DiyPageModel diyPageModel = new DiyPageModel();
            diyPageModel.setLink(link);
            diyPageModel.setStore_id(vo.getStoreId());
            diyPageModel = diyPageModelMapper.selectOne(diyPageModel);
            if (Objects.nonNull(diyPageModel))
            {
               int count = diyPageBindModelMapper.getBindData(diyId,diyPageModel.getId());
               if (count == 0)
               {
                 return diyPageModel;
               }
            }

        }catch (LaiKeAPIException l)
        {
            throw l;
        } catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPageJson");
        }
        return null;
    }
}

