package com.laiketui.plugins.diy.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.DictionaryListModelMapper;
import com.laiketui.common.mapper.DiyModelMapper;
import com.laiketui.common.mapper.DiyPageBindModelMapper;
import com.laiketui.common.mapper.PluginsModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.plugin.DiyModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.diy.DiyVo;
import com.laiketui.plugins.api.diy.PluginsDiyAdminService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: sunH_
 * @Date: Create in 15:47 2022/4/28
 */
@Service
public class PluginsDiyAdminServiceImpl implements PluginsDiyAdminService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DiyModelMapper diyModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private DiyPageBindModelMapper diyPageBindModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    private static final String DIY_JSON_KEY = "diy_json_key:";

    @Override
    public Map<String, Object> getDiyList(MainVo vo, Integer id,Integer theme_type,String theme_type_code) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            DiyModel diyModel = new DiyModel();
            diyModel.setStore_id(vo.getStoreId());
            diyModel.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            if (!Objects.isNull(id))
            {
                diyModel.setId(id);
            }
            if (Objects.nonNull(theme_type))
            {
                diyModel.setTheme_type(theme_type);
            }

            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("id",id);
            paramMap.put("store_id",vo.getStoreId());
            paramMap.put("theme_type",theme_type);
            paramMap.put("mch_id", 0);
            int total = diyModelMapper.countDynamic(paramMap);

            if (StringUtils.isNotEmpty(theme_type_code))
            {
                diyModel.setTheme_dict_code(theme_type_code);
            }
            diyModel.setMch_id(0);
            List<DiyModel> select = diyModelMapper.selectList(diyModel);

            //系统主题需要查询图片域名,进入详情再查询，首页无需展示
            if (CollectionUtils.isNotEmpty(select) && Objects.nonNull(id))
            {
                for (DiyModel model : select)
                {
                    if (StringUtils.isNotEmpty(model.getValue()) && model.getTheme_type() == 1)
                    {
                        String value = "";
                        if (redisUtil.hasKey(DIY_JSON_KEY + model.getId()))
                        {
                            value = redisUtil.get(DIY_JSON_KEY + model.getId()).toString();
                        }
                        else
                        {
                            value = updateDiyImgJson(model.getValue(), model.getId(), vo.getStoreId());
                        }
                        model.setValue(value);
                    }
                }
            }

            List<Map<String,Object>> mapList = new ArrayList<>();
            Map<String, List<DiyModel>> map = new HashMap<>();

            Map<String,Object> param = new HashMap<>();
            param.put("name","DIY主题");
            param.put("status",1);
            param.put("lang_code",vo.getLanguage());

            List<Map<String, Object>> dictionary = dictionaryListModelMapper.getDictionaryByName(param);
                //获取字典数据
                    map = select.stream()
                            .collect(Collectors.groupingBy(
                                    item -> item.getTheme_dict_code() != null ?
                                            item.getTheme_dict_code() : "UNKNOWN"
                            ));

            if (CollectionUtils.isNotEmpty(dictionary))
            {
                for (Map<String, Object> dictionaryListMode : dictionary)
                {
                    String value = MapUtils.getString(dictionaryListMode, "value");
                    String text = MapUtils.getString(dictionaryListMode, "ctext");
                    String code = MapUtils.getString(dictionaryListMode, "code");
                    Map<String,Object> totalMap = new HashMap<>();
                    totalMap.put("value",value);
                    totalMap.put("count", map.containsKey(code) ? map.get(code).size() : 0);
                    totalMap.put("code", code);
                    totalMap.put("text", text);
                    mapList.add(totalMap);
                }
            }
            resultMap.put("details",mapList);
            resultMap.put("list", select);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取diy模板首页列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "actGoodsMove");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String,Object> addOrUpdateDiy(DiyVo vo) throws LaiKeAPIException
    {
        Map<String,Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            vo.setMchId(0);
            resultMap = publiceService.addOrUpdateDiy(vo);
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("活动商品上下移动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "actGoodsMove");
        }
        return resultMap;
    }


    //获取diy 系统主题的图片路径
    private String updateDiyImgJson(String json, Integer diyId, Integer storeId) {
        JSONArray jsonArray = JSON.parseArray(json);
        processJsonArray(jsonArray, storeId);
        String value = JSON.toJSONString(jsonArray);
        redisUtil.set(DIY_JSON_KEY + diyId, value);
        return value;
    }

    private void processJsonArray(JSONArray array, Integer storeId) {
        for (Object obj : array) {
            JSONObject jsonObj = (JSONObject) obj;

            //导航图片
            String iconPath = jsonObj.getString("relative_iconPath");
            if (StringUtils.isNotEmpty(iconPath)) {
                String imgPath = publiceService.getImgPath(iconPath, storeId);
                jsonObj.put("iconPath", imgPath);
            }
            String selectedIconPath = jsonObj.getString("relative_selectedIconPath");
            if (StringUtils.isNotEmpty(selectedIconPath)) {
                String imgPath = publiceService.getImgPath(selectedIconPath, storeId);
                jsonObj.put("selectedIconPath", imgPath);
            }

            JSONObject defaultArray = jsonObj.getJSONObject("defaultArray");
            if (defaultArray != null) {
                processDefaultArray(defaultArray, storeId);
            }
        }
    }

    private void processDefaultArray(JSONObject defaultArray, Integer storeId) {
        for (String key : defaultArray.keySet()) {
            JSONObject defArrayObject = (JSONObject) defaultArray.get(key);
            //可能存在的图片都在以下json里面
            processConfigType(defArrayObject, "swiperConfig", storeId);
            processConfigType(defArrayObject, "menuConfig", storeId);
            processConfigType(defArrayObject, "advConfig", storeId);
        }
    }

    private void processConfigType(JSONObject parent, String configKey, Integer storeId) {
        if (parent.containsKey(configKey)) {
            JSONArray list = parent.getJSONObject(configKey).getJSONArray("list");
            if (list != null && !list.isEmpty()) {
                updateImagePaths(list, storeId);
            }
        }
    }

    private void updateImagePaths(JSONArray imageList, Integer storeId) {
        for (Object imgObj : imageList) {
            JSONObject imageObject = (JSONObject) imgObj;
            String img = imageObject.getString("relative_img");
            if (StringUtils.isNotEmpty(img)) {
                String imgPath = publiceService.getImgPath(img, storeId);
                imageObject.put("img", imgPath);
                imageObject.put("relative_img", imgPath);
            }
        }
    }

    @Override
    public void diyStatus(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publiceService.diyStatus(vo,id,0);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置diy模板 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDiy");
        }
    }

    @Override
    public void delDiy(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publiceService.delDiy(vo,id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除diy模板 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
    }

    @Override
    public Map<String, Object> getBindPageList(MainVo vo, Integer id) throws LaiKeAPIException {
        Map<String,Object> map = new HashMap<>();
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            List<Map<String, Object>> list = diyPageBindModelMapper.getBindPageListByDiyId(id);
            map.put("list", list);
        } catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
        return map;
    }

    @Override
    public Map<String, Object> getPluginStatus(MainVo vo) throws LaiKeAPIException
    {
        //插件状态
        Map<String, Object> pluginMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            List<String> pluginCodeList  = pluginsModelMapper.getPluginsCodeAll(vo.getStoreId());
            for (String pluginCode : pluginCodeList)
            {
                publiceService.frontPlugin(vo.getStoreId(), null, pluginCode, pluginMap,true);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPluginStatus");
        }
        return pluginMap;
    }
}

