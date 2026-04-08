package com.laiketui.admins.admin.services;


import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.AdminPcManageService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.BannerModelMapper;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.PcMallBottomModelMapper;
import com.laiketui.common.mapper.PcMallConfigModelMapper;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.BannerModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.PcMallBottomModel;
import com.laiketui.domain.config.PcMallConfigModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddPcConfigVo;
import com.laiketui.domain.vo.pc.AddBannerInfoVo;
import com.laiketui.domain.vo.pc.AddBottomInfoVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * pc管理
 *
 * @author Trick
 * @date 2021/1/22 10:39
 */
@Service
public class AdminPcManageServiceImpl implements AdminPcManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminPcManageServiceImpl.class);

    @Override
    public Map<String, Object> getBannerInfo(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type", BannerModel.BannerType.BANNER_TYPE_PC.toString());
            if (id != null && id > 0)
            {
                parmaMap.put("id", id);
            }
            parmaMap.put("mch_id", 0);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = bannerModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                String imagUrl = String.valueOf(map.get("image"));
                imagUrl = publiceService.getImgPath(imagUrl, vo.getStoreId());
                map.put("image", imagUrl);
            }
            int total = bannerModelMapper.countDynamic(parmaMap);

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取轮播图信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBannerInfo");
        }
        return resultMap;
    }

    @Override
    public boolean addBannerInfo(AddBannerInfoVo vo) throws LaiKeAPIException
    {
        try
        {
            int count;
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(vo.getImageUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBNWK, "轮播图不能为空");
            }
            BannerModel bannerModelOld = null;
            if (vo.getId() != null && vo.getId() > 0)
            {
                bannerModelOld = new BannerModel();
                bannerModelOld.setMch_id(0);
                bannerModelOld.setId(vo.getId());
                bannerModelOld.setStore_id(vo.getStoreId());
                bannerModelOld.setType(BannerModel.BannerType.BANNER_TYPE_PC.toString());
                bannerModelOld = bannerModelMapper.selectOne(bannerModelOld);
            }

            BannerModel bannerModelSave = new BannerModel();
            bannerModelSave.setUrl(vo.getPath());
            bannerModelSave.setImage(ImgUploadUtils.getUrlImgByName(vo.getImageUrl(), true));
            if (StringUtils.isEmpty(vo.getSort()))
            {
                bannerModelSave.setSort(bannerModelMapper.getPcMaxSort(vo.getStoreId()));
            }
            else
            {
                bannerModelSave.setSort(vo.getSort());
            }
            if (bannerModelOld != null)
            {
                bannerModelSave.setId(bannerModelOld.getId());
                count = bannerModelMapper.updateByPrimaryKeySelective(bannerModelSave);
            }
            else
            {
                bannerModelSave.setType(BannerModel.BannerType.BANNER_TYPE_PC.toString());
                bannerModelSave.setStore_id(vo.getStoreId());
                bannerModelSave.setAdd_date(new Date());
                count = bannerModelMapper.insertSelective(bannerModelSave);
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addBannerInfo");
        }
    }

    @Override
    public boolean delBannerById(int id) throws LaiKeAPIException
    {
        try
        {
            BannerModel bannerModel = bannerModelMapper.selectByPrimaryKey(id);
            if (bannerModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, bannerModel.getStore_id()));
            return bannerModelMapper.deleteByPrimaryKey(id) > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除轮播图 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBannerById");
        }
    }

    @Override
    public void topBannerById(int id) throws LaiKeAPIException
    {
        try
        {
            BannerModel bannerModel = bannerModelMapper.selectByPrimaryKey(id);
            if (bannerModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            BannerModel bannerModelUpdate = new BannerModel();
            bannerModelUpdate.setId(id);
            bannerModelUpdate.setSort(bannerModelMapper.getMaxSort(bannerModel.getStore_id()));

            if (bannerModelMapper.updateByPrimaryKeySelective(bannerModelUpdate) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("置顶轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "topBannerById");
        }
    }

    @Override
    public Map<String, Object> getBottomInfo(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (id != null && id > 0)
            {
                parmaMap.put("id", id);
            }
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> dataList = pcMallBottomModelMapper.selectList(parmaMap);
            int                       total    = pcMallBottomModelMapper.countDynamic(parmaMap);
            for (Map<String, Object> objectMap : dataList)
            {
                objectMap.put("add_date", DateUtil.dateFormate(MapUtils.getString(objectMap, "add_date"), GloabConst.TimePattern.YMDHMS));
            }
            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取PC商城底部栏图片配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBottomInfo");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addBottomInfo(AddBottomInfoVo vo) throws LaiKeAPIException
    {
        try
        {
            int count;
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(vo.getImages()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "图片不能为空", "image");
            }
            if (StringUtils.isEmpty(vo.getTitle()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "标题不能为空", "title");
            }
            PcMallBottomModel pcMallBottomModel = null;
            //修改
            if (vo.getId() != null && vo.getId() > 0)
            {
                pcMallBottomModel = new PcMallBottomModel();
                pcMallBottomModel.setId(vo.getId());
                pcMallBottomModel.setStoreId(vo.getStoreId());
                pcMallBottomModel = pcMallBottomModelMapper.selectOne(pcMallBottomModel);
            }

            PcMallBottomModel pcMallBottomSave = new PcMallBottomModel();
            pcMallBottomSave.setImage(vo.getImages());
            pcMallBottomSave.setTitle(vo.getTitle());
            pcMallBottomSave.setSubheading(vo.getSubheading());
            if (StringUtils.isEmpty(vo.getSort()))
            {
                pcMallBottomSave.setSort(pcMallBottomModelMapper.getPcMaxSort(vo.getStoreId()));
            }
            else
            {
                pcMallBottomSave.setSort(vo.getSort());
            }
            if (pcMallBottomModel != null)
            {
                pcMallBottomSave.setId(pcMallBottomModel.getId());
                pcMallBottomSave.setUpdateDate(new Date());
                count = pcMallBottomModelMapper.updateByPrimaryKeySelective(pcMallBottomSave);
            }
            else
            {
                pcMallBottomSave.setStoreId(vo.getStoreId());
                pcMallBottomSave.setAddDate(new Date());
                pcMallBottomSave.setUpdateDate(new Date());
                count = pcMallBottomModelMapper.insertSelective(pcMallBottomSave);
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑PC商城底部栏图片配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBottomInfo");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delBottomById(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            PcMallBottomModel pcMallBottomModel = pcMallBottomModelMapper.selectByPrimaryKey(id);
            if (pcMallBottomModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, pcMallBottomModel.getStoreId()));
            return pcMallBottomModelMapper.deleteByPrimaryKey(id) > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除PC商城底部栏图片配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBottomInfo");
        }
    }


    @Autowired
    private ConfigModelMapper configModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfig(MainVo vo, AddPcConfigVo config) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Date   now      = new Date();
            String mallIcon = config.getMallIcon();
            if (StringUtils.isEmpty(mallIcon))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "浏览器图标不能为空");
            }
            String mallName = config.getMallName();
            if (StringUtils.isEmpty(mallName))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "浏览器名称不能为空");
            }
            ConfigModel storeConfig = new ConfigModel();
            storeConfig.setStore_id(vo.getStoreId());
            storeConfig = configModelMapper.selectOne(storeConfig);
            if (storeConfig == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "商城配置不存在");
            }
            storeConfig.setHtml_icon(mallIcon);
            if (configModelMapper.updateByPrimaryKeySelective(storeConfig) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "更新商城浏览器图标失败");
            }
            //新增/编辑浏览器标签
            Map<String, Object> tabConfig = new HashMap<>(16);
            tabConfig.put("mallIcon", mallIcon);
            tabConfig.put("mallName", mallName);
            savePcMallConfig(vo.getStoreId(), PcMallConfigModel.LNQBQ, JSON.toJSONString(tabConfig), now, "新增/编辑浏览器标签失败");

            String mallLogo = config.getMallLogo();
            if (StringUtils.isEmpty(mallLogo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "商城logo不能为空");
            }
            //新增/编辑 登录页配置
            Map<String, Object> loginConfig = new HashMap<>(16);
            loginConfig.put("mallLogo", mallLogo);
            loginConfig.put("shortcutMenu2", config.getShortcutMenu2());
            loginConfig.put("copyright", config.getCopyright());
            loginConfig.put("archival", config.getArchival());
            loginConfig.put("authority", config.getAuthority());
            loginConfig.put("list", config.getList());
            savePcMallConfig(vo.getStoreId(), PcMallConfigModel.DLYPZ, JSON.toJSONString(loginConfig), now, "新增/编辑 登录页配置失败");

            String welcomeTerm = config.getWelcomeTerm();
            if (StringUtils.isEmpty(welcomeTerm))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "首页配置欢迎术语不能为空");
            }
            if (StringUtils.isEmpty(config.getAPPUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "浮窗设置 下载APP二维码不能为空");
            }
            if (StringUtils.isEmpty(config.getAPPExplain()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSZBTX, "浮窗设置 APP说明不能为空");
            }
            //没有使用了
/*            if (StringUtils.isEmpty(config.getH5Url())){
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "商品分享设置 分享h5二维码不能为空");
            }
            if (StringUtils.isEmpty(config.getTextExplain())){
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "商品分享设置 文字说明不能为空");
            }*/
            //新增/编辑 首页配置
            Map<String, Object> homeConfig = new HashMap<>(16);
            homeConfig.put("welcomeTerm", welcomeTerm);
            homeConfig.put("shortcutMenu3", config.getShortcutMenu3());
            homeConfig.put("APPUrl", config.getAPPUrl());
            homeConfig.put("APPExplain", config.getAPPExplain());
            homeConfig.put("H5Url", config.getH5Url());
            homeConfig.put("textExplain", config.getTextExplain());
            savePcMallConfig(vo.getStoreId(), PcMallConfigModel.SYPZ, JSON.toJSONString(homeConfig), now, "新增/编辑 页配置失败");
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了PC商城的配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑PC商城显示配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addConfig");
        }
    }


    @Override
    public Map<String, Object> getConfig(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            PcMallConfigModel pcMallConfigModel = new PcMallConfigModel();
            pcMallConfigModel.setStoreId(vo.getStoreId());
            List<PcMallConfigModel> pcMallConfigModelList = pcMallConfigModelMapper.select(pcMallConfigModel);
            //无配置/配置错误（信息不全）
            if (pcMallConfigModelList == null || pcMallConfigModelList.size() != 3)
            {
                return resultMap;
            }
            for (PcMallConfigModel mallConfigModel : pcMallConfigModelList)
            {
                Integer type = mallConfigModel.getType();
                switch (type)
                {
                    case PcMallConfigModel.LNQBQ:
                        resultMap.put(String.valueOf(PcMallConfigModel.LNQBQ), mallConfigModel.getValue());
                        break;
                    case PcMallConfigModel.DLYPZ:
                        resultMap.put(String.valueOf(PcMallConfigModel.DLYPZ), mallConfigModel.getValue());
                        break;
                    case PcMallConfigModel.SYPZ:
                        resultMap.put(String.valueOf(PcMallConfigModel.SYPZ), mallConfigModel.getValue());
                        break;
                    default:
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "PC商城配置异常", "getConfig");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑PC商城显示配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addConfig");
        }
        return resultMap;
    }

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private BannerModelMapper bannerModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PcMallBottomModelMapper pcMallBottomModelMapper;

    @Autowired
    private PcMallConfigModelMapper pcMallConfigModelMapper;

    private void savePcMallConfig(int storeId, int type, String json, Date now, String failMessage) throws LaiKeAPIException
    {
        PcMallConfigModel pcMallConfigModel     = pcMallConfigModelMapper.getConfigByStoreIdAndType(storeId, type);
        PcMallConfigModel pcMallConfigModelSave = new PcMallConfigModel();
        int               row;
        if (pcMallConfigModel == null)
        {
            pcMallConfigModelSave.setStoreId(storeId);
            pcMallConfigModelSave.setType(type);
            pcMallConfigModelSave.setValue(json);
            pcMallConfigModelSave.setAddDate(now);
            pcMallConfigModelSave.setUpdateDate(now);
            row = pcMallConfigModelMapper.insert(pcMallConfigModelSave);
        }
        else
        {
            pcMallConfigModelSave.setId(pcMallConfigModel.getId());
            pcMallConfigModelSave.setValue(json);
            pcMallConfigModelSave.setUpdateDate(now);
            row = pcMallConfigModelMapper.updateByPrimaryKeySelective(pcMallConfigModelSave);
        }
        if (row < 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, failMessage);
        }
    }
}
