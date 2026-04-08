package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.api.DraftsModelService;
import com.laiketui.common.mapper.BrandClassModelMapper;
import com.laiketui.common.mapper.DraftsModelMapper;
import com.laiketui.common.mapper.ProductClassModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.DraftsModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 草稿箱
 * @version 1.0
 * @description: liuao
 * @date 2025/1/16 15:37
 */
@Service
public class DraftsModelServiceServiceImpl implements DraftsModelService
{
    @Autowired
    private DraftsModelMapper draftsModelMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ProductClassModelMapper productClassModelMapper;
    @Autowired
    private BrandClassModelMapper  brandClassModelMapper;


    @Override
    public void add(MainVo vo,Integer type,String text,Integer id)
    {
        try
        {
            Integer storeId = vo.getStoreId();
            String  accessId = vo.getAccessId();
            DraftsModel draftsModel = new DraftsModel();
            draftsModel.setStore_id(storeId);
            draftsModel.setText(text);
            int count = 0;
            User user;
            switch (type)
            {
                case 1:
                     user = getUser(accessId, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN);
                    //管理后台
                    Integer mchId = user.getMchId();
                    draftsModel.setMch_id(mchId);
                    break;
                case 2:
                    //供应商
                    user = getUser(accessId, GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN);
                    Integer supplierId = user.getId();
                    draftsModel.setSupplier_id(supplierId);
                    break;
                case 3:
                    //店铺后台
                    user = getUser(accessId,GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN);
                    Integer userMchId = user.getMchId();
                    draftsModel.setMch_id(userMchId);
                    break;
            }

            if (Objects.nonNull(id))
            {
                draftsModel.setId(id);
                count = draftsModelMapper.update(draftsModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "编辑草稿箱失败", "add");
                }
                return;
            }
            draftsModel.setAdd_time(new Date());
            count = draftsModelMapper.insertSelective(draftsModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加草稿箱失败", "add");
            }
        }catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "add");
        }
    }

    @Override
    public Map<String, Object> list(MainVo vo,Integer type)
    {
        Map<String,Object> resultMap = new HashMap<>();
        try
        {
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("pageNo",vo.getPageNo());
            paramMap.put("pageSize",vo.getPageSize());
            paramMap.put("storeId",vo.getStoreId());
            String accessId = vo.getAccessId();
            User user;
            switch (type){
                case 1:
                    user = getUser(accessId, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN);
                    paramMap.put("mchId",user.getMchId());
                    break;
                case 2:
                    user = getUser(accessId, GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN);
                    paramMap.put("supplierId",user.getId());
                    break;
                case 3:
                    user = getUser(accessId,GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN);
                    paramMap.put("mchId",user.getMchId());
                    break;
            }
            int count = draftsModelMapper.count(paramMap);
            List<Map<String,Object>> list = new ArrayList<>();
            if (count > 0)
            {
                list = draftsModelMapper.list(paramMap);
                if (CollectionUtils.isNotEmpty(list))
                {
                    for (Map<String, Object> map : list)
                    {
                        if (map.containsKey("text"))
                        {
                            String text = MapUtils.getString(map, "text");
                            JSONObject jsonObject = JSONObject.parseObject(text);
                            //图片
                            String showImg = jsonObject.getString("showImg");
                            if (StringUtils.isNotEmpty(showImg))
                            {
                                String[] imgList = showImg.split(SplitUtils.DH);
                                map.put("imgurls", imgList[0]);
                            }
                            map.put("commodity_type",Objects.nonNull(jsonObject.getInteger("commodityType")) ? jsonObject.getInteger("commodityType") : 0);
                            //商品名称
                            String productTitle = jsonObject.getString("productTitle");
                            map.put("product_title", productTitle);
                            //分类名称
                            Integer productClassId = jsonObject.getInteger("productClassId");
                            if (Objects.nonNull(productClassId))
                            {
                                ProductClassModel productClassModel = productClassModelMapper.selectByPrimaryKey(productClassId);
                                map.put("pname", productClassModel.getPname());
                            }
                            //品牌名称
                            Integer brandId = jsonObject.getInteger("brandId");
                            if (Objects.nonNull(brandId))
                            {
                                BrandClassModel brandClassModel = brandClassModelMapper.selectByPrimaryKey(brandId);
                                map.put("brand_name", brandClassModel.getBrand_name());
                            }
                            //价格
                            String initial = jsonObject.getString("initial");
                            if (Objects.nonNull(initial))
                            {
                                Map<String, String> res = getPrice(initial);
                                if (res.containsKey("sj"))
                                {
                                    map.put("price", res.get("sj"));
                                }
                            }
                            //添加日期
                            map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                        }
                    }
                }
            }
            resultMap.put("total",count);
            resultMap.put("list",list);

        }catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "list");
        }
        return resultMap;
    }

    //获取价格
    private Map<String, String> getPrice(String initial)
    {
        Map<String, String> result = new HashMap<>();
        String[] pairs = initial.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 0) {
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : null;
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public void del(String id)
    {
        try
        {
            List<String> list = Arrays.asList(id.split(SplitUtils.DH));
            draftsModelMapper.deleteBatch(list);
        }catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    public Map<String,Object> getTextById(Integer id)
    {
        Map<String,Object> resultMap = new HashMap<>();
        DraftsModel draftsModel = draftsModelMapper.getById(id);
        if (Objects.nonNull(draftsModel))
        {
            resultMap.put("text",draftsModel.getText());
        }
        return resultMap;
    }

    /**
     * 获取当前登录用户数据
     * @param accessId
     * @return
     */
    private User getUser(String accessId, String token){
        return RedisDataTool.getRedisUserCache(accessId, redisUtil, token);
    }

}
