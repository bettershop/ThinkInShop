package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrIndexService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.config.GuideModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 小程序首页
 *
 * @author Trick
 * @date 2020/10/10 9:01
 */
@Api(tags = "商城首页")
@RestController
@Validated
@RequestMapping("/app/home")
public class AppsCstrIndexController
{

    @Autowired
    AppsCstrIndexService appsCstrIndexService;

    @Autowired
    PubliceService publiceService;

    @Autowired
    RedisUtil redisUtil;

    @ApiOperation("首页是否div接口")
    @PostMapping("hasDiy")
    @HttpApiMethod(urlMapping = "app.index.hasDiy")
    public Result hasDiy(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrIndexService.hasDiy(vo.getStoreId()));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("插件状态")
    @PostMapping("pluginStatus")
    @HttpApiMethod(urlMapping = "app.index.pluginStatus")
    public Result pluginStatus(MainVo vo, Integer mchId)
    {
        try
        {
            return Result.success(appsCstrIndexService.pluginStatus(vo, mchId));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("首页接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "accessId", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "storeType", value = "来源", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "latitude", value = "纬度", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "longitude", value = "经度", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("index")
    @HttpApiMethod(urlMapping = {"app.index.index"})
    public Result index(MainVo vo, String longitude, String latitude)
    {
        try
        {
            //商城首页缓存key 如果该缓存不存在则强制刷新用户相应的首页缓存
            // 在缓存key中加入语言信息，确保不同语言的首页数据分开缓存
            String redisIndexCacheKey = String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId() + "_" + vo.getLanguage());
            Object              indexData          = null;
            Map<String, Object> resultJson         = null;
            //是否重新获取数据并刷新缓存标识
            boolean isReloadData = true;

            //当前用户数据标识key
            StringBuilder thisStr = new StringBuilder(vo.getStoreId()).append(SplitUtils.DH).append(vo.getAccessId())
                    .append(SplitUtils.DH).append(vo.getStoreType()).append(SplitUtils.DH).append(vo.getLanguage())
                    .append(SplitUtils.DH).append(longitude).append(SplitUtils.DH).append(latitude);
            //首页数据缓存处理-当前用户
            String indexCacheCondition = String.format(GloabConst.RedisHeaderKey.INDEX_CACHE_CONDITION_, vo.getStoreId() + "_" + vo.getLanguage(), vo.getAccessId());

            if (redisUtil.hasKey(redisIndexCacheKey))
            {
                String str = "";
                if (redisUtil.hasKey(indexCacheCondition))
                {
                    //读取当前用户缓存
                    str = redisUtil.get(indexCacheCondition).toString();
                    //直接读取缓存数据
                    if (thisStr.toString().equals(str))
                    {
                        indexData = redisUtil.get(redisIndexCacheKey);
                        resultJson = DataUtils.cast(indexData);
                    }
                }
                //重新获取数据
//                isReloadData = indexData == null;
            }

            if (isReloadData)
            {
                //重新读取数据
                resultJson = appsCstrIndexService.index(vo, longitude, latitude);
                redisUtil.set(indexCacheCondition, thisStr, 300);
                redisUtil.set(redisIndexCacheKey, resultJson, 300);
            }
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("distributionList")
    @ApiOperation("获取分销商品")
    @HttpApiMethod(apiKey = "app.index.distribution_list")
    public Result distributionList(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrIndexService.distributionList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取推荐商家")
    @PostMapping("getMchList")
    @HttpApiMethod(apiKey = "app.index.getMchList")
    public Result getMchList(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrIndexService.getMchList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取分类列表")
    @PostMapping("classList")
    @HttpApiMethod(urlMapping = "app.index.classList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shop_id", value = "店铺id", dataType = "Integer", paramType = "form"),
            @ApiImplicitParam(name = "sort_criteria", value = "排序条件", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "sort", value = "排序", dataType = "String", paramType = "form")
    })

    public Result classList(MainVo vo, Integer shop_id, String sort_criteria, String sort)
    {
        try
        {
            return Result.success(appsCstrIndexService.classList(vo, shop_id, sort_criteria, sort));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取用户是否是会员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商城id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("getUserGradeRate")
    @HttpApiMethod(urlMapping = "app.index.get_membership_status")
    public Result getUserGradeRate(@NotNull @ParamsMapping("store_id") int storeId)
    {
        try
        {
            Map<String, Object> resultJson = appsCstrIndexService.getMembershipStatus(storeId);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("加载更多商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cid", value = "分类id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "sort_criteria", value = "排序条件", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "sort", value = "排序字段", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("get_more")
    @HttpApiMethod(urlMapping = "app.index.get_more")
    public Result getMore(MainVo vo, int cid, String sort_criteria, String sort)
    {
        try
        {
            List<Map<String, Object>> resultJson = appsCstrIndexService.getMore(vo, cid, sort_criteria, sort);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取地理位置")
    @PostMapping("get_location")
    @HttpApiMethod(urlMapping = {"app.index.get_location", "app.index.get_Longitude_and_latitude"})
    public Result getLocation(MainVo vo, @ParamsMapping("GroupID") Integer groupId)
    {
        try
        {
            Map<String, Object> resultJson = appsCstrIndexService.getLocation(vo, groupId);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取引导图api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "store_type", value = "来源", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("guided_graph")
    @HttpApiMethod(urlMapping = "app.index.guided_graph")
    public Result guidedGraph(@RequestParam("store_id") int storeId, @RequestParam("access_id") String accessId,
                              String language, @RequestParam("guideType") int guideType)
    {
        try
        {
            List<GuideModel> result = appsCstrIndexService.guidedGraph(storeId, accessId, language, guideType);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新品上市")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "page", value = "分页", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("new_arrival")
    @HttpApiMethod(urlMapping = "app.index.new_arrival")
    public Result newArrival(MainVo vo)
    {
        try
        {
            Map<String, Object> result = appsCstrIndexService.newArrival(vo);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("(优选店铺)推荐门店api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "longitude", value = "经度", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "latitude", value = "维度", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "cid", value = "店铺分类id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "lang_code", value = "语种", required = true, dataType = "String", paramType = "form"),
    })
    @PostMapping("recommend_stores")
    @HttpApiMethod(urlMapping = "app.index.recommend_stores")
    public Result recommendStores(MainVo vo, @NotNull String longitude, @NotNull String latitude, Integer cid,String lang_code)
    {
        try
        {
            Map<String, Object> result = appsCstrIndexService.recommendStores(vo, longitude, latitude, cid,lang_code);
            return Result.success(result);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("店铺分类")
    @PostMapping("mchClass")
    @HttpApiMethod(urlMapping = {"app.index.mchClass", "mch.mall.Mch.mchClass"})
    public Result mchClass(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrIndexService.mchClass(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("更改语言api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "language", value = "语言", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("select_language")
    @HttpApiMethod(urlMapping = "app.index.select_language")
    public Result selectLanguage(@RequestParam("store_id") @NotNull int storeId, @RequestParam("access_id") @NotNull String accessId, @NotNull String language)
    {
        try
        {
            boolean result = appsCstrIndexService.selectLanguage(storeId, accessId, language);
            if (result)
            {
                return Result.success();
            }
            else
            {
                return Result.error();
            }
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("更改货币api")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store_id", value = "商城id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "access_id", value = "授权id", required = true, dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "currency", value = "语言", required = true, dataType = "String", paramType = "form")
    })
    @PostMapping("changeCurrency")
    @HttpApiMethod(urlMapping = "app.index.changeCurrency")
    public Result changeCurrency(CurrencyStoreVo vo)
    {
        try
        {
            appsCstrIndexService.changeCurrency(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("推荐商品api(好物优选)")
    @PostMapping("recommend")
    @HttpApiMethod(urlMapping = "app.index.recommend")
    public Result recommend(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrIndexService.recommend(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取平台用户公告")
    @PostMapping("getUserTell")
    @HttpApiMethod(urlMapping = "app.index.getUserTell")
    public Result getUserTell(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrIndexService.getUserTell(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("标记公告以读")
    @PostMapping("/markToRead")
    @HttpApiMethod(apiKey = "app.Index.markToRead")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tell_id", value = "公告id", dataType = "int", paramType = "form")
    })
    public Result markToRead(MainVo vo, Integer tell_id)
    {
        try
        {
            appsCstrIndexService.markToRead(vo, tell_id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取正在直播的主播")
    @PostMapping("queryLiving")
    @HttpApiMethod(urlMapping = "plugin.living.App.queryLiving")
    public Result queryLiving(MainVo vo)
    {
        try
        {
            return Result.success(appsCstrIndexService.queryLiving(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取前端基础信息配置")
    @PostMapping("/getMchBasicConfiguration")
    @HttpApiMethod(apiKey = "app.index.GetBasicConfiguration")
    public Result getFrontMsgAndLoginConfig(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getFrontConfig(vo, 1));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
