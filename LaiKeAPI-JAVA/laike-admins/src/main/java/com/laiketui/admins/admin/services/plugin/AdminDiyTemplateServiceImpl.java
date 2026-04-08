package com.laiketui.admins.admin.services.plugin;

import com.laiketui.admins.api.admin.plugin.AdminDiyTemplateService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ActivityModel;
import com.laiketui.domain.config.BannerModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.home.UiNavigationBarModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.plugin.DiyModel;
import com.laiketui.domain.product.ActivityProModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.diy.AddDiyActivityVo;
import com.laiketui.domain.vo.admin.diy.SaveDiyUiVo;
import com.laiketui.domain.vo.plugin.BannerSaveVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * diy模板管理
 *
 * @author Trick
 * @date 2021/6/30 9:41
 */
@Service
public class AdminDiyTemplateServiceImpl implements AdminDiyTemplateService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private DiyModelMapper diyModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private BannerModelMapper bannerModelMapper;

    @Autowired
    private JumpPathModelMapper jumpPathModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ActivityProModelMapper activityProModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String h5Config     = "";
            String templateId   = "";
            String templateName = "默认模板";
            //是否开启diy插件
            int isDiy = 0;
            DiyModel diyModel = new DiyModel();
            diyModel.setStore_id(vo.getStoreId());
            diyModel.setMch_id(0);
            diyModel.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);

            diyModel = diyModelMapper.selectOne(diyModel);
            if (Objects.nonNull(diyModel))
            {
                templateId = diyModel.getId() + "";
                templateName = diyModel.getName();
                isDiy = 1;
            }
            //获取h5配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                h5Config = configModel.getH5_domain();
            }

            resultMap.put("status", isDiy);
            resultMap.put("name", templateName);
            resultMap.put("use_id", templateId);
            resultMap.put("H5_domain", h5Config);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("diy模板管理首页 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> bannerIndex(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type_not", 4);
            parmaMap.put("mch_id", 0);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total = bannerModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = bannerModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                map.put("image", publiceService.getImgPath(MapUtils.getString(map, "image"), vo.getStoreId()));
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取轮播图列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerIndex");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> bannerPathList(MainVo vo, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        List<Map<String, Object>> list = new ArrayList<>();
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type0", type);
            parmaMap.put("status", 1);
            parmaMap.put("type", 1);
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            if (type == 3)
            {
                list = jumpPathModelMapper.getMchList(parmaMap);
                resultMap.put("list", list);
                return resultMap;
            }
            list = jumpPathModelMapper.selectDynamic(parmaMap);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("轮播图路径分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerPathList");
        }
        return resultMap;
    }


    @Override
    public void bannerSave(BannerSaveVo vo) throws LaiKeAPIException
    {
        try
        {
            int row = 0;
            if (vo.getType0() == null)
            {
                vo.setType0(DiyType.CUSTOM);
            }
            BannerModel bannerSave = new BannerModel();
            BannerModel bannerOld  = null;
            if (vo.getId() != null)
            {
                bannerOld = bannerModelMapper.selectByPrimaryKey(vo.getId());
            }
            if (bannerOld == null)
            {
                //添加
                bannerSave.setStore_id(vo.getStoreId());
                int maxSort = bannerModelMapper.getMaxSort(vo.getStoreId());
                bannerSave.setSort(maxSort);
                bannerSave.setType(DiyType.CUSTOM + "");
            }
            else
            {
                bannerSave.setId(bannerOld.getId());
            }
            bannerSave.setOpen_type(vo.getType0().toString());
            bannerSave.setImage(ImgUploadUtils.getUrlImgByName(vo.getPicUrl(), true));
            bannerSave.setUrl(vo.getUrl());
            bannerSave.setAdd_date(new Date());
            if (bannerSave.getId() == null)
            {
                row = bannerModelMapper.insertSelective(bannerSave);
                publiceService.addAdminRecord(vo.getStoreId(), "添加了默认模板里面的轮播图", AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            else
            {
                row = bannerModelMapper.updateByPrimaryKeySelective(bannerSave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了默认模板里面的轮播图", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑 轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerSave");
        }
    }

    @Override
    public void bannerMoveTop(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            BannerModel bannerOld = bannerModelMapper.selectByPrimaryKey(id);
            if (bannerOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            int         maxSort      = bannerModelMapper.getMaxSort(vo.getStoreId());
            BannerModel bannerUpdate = new BannerModel();
            bannerUpdate.setId(id);
            bannerUpdate.setSort(maxSort);
            int row = bannerModelMapper.updateByPrimaryKeySelective(bannerUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSB, "置顶失败");
            }

            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的轮播图进行了置顶操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("轮播图置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerMoveTop");
        }
    }

    @Override
    public void bannerRemove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            int row = bannerModelMapper.move(id, id1);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDSB, "移动失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的轮播图进行了上移/下移操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("轮播图上移下移 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerRemove");
        }
    }

    @Override
    public void bannerDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            BannerModel bannerOld = bannerModelMapper.selectByPrimaryKey(id);
            if (bannerOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            //添加日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了默认模板里面的轮播图", AdminRecordModel.Type.DEL, vo.getAccessId());
            int row = bannerModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("轮播图删除 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerDel");
        }
    }

    @Autowired
    private UiNavigationBarModelMapper uiNavigationBarModelMapper;

    @Override
    public Map<String, Object> uiIndex(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total = uiNavigationBarModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = uiNavigationBarModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                String image = MapUtils.getString(map, "image");

                //bounced为前端本地文件夹路径，不需要修改，可以直接访问
                if (image.startsWith("/bounced"))
                {
                    continue;
                }
                map.put("image", publiceService.getImgPath(image, vo.getStoreId()));
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("ui导航栏列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uiIndex");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> uiIndexDetail(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("id", id);
            List<Map<String, Object>> list = uiNavigationBarModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                map.put("image", publiceService.getImgPath(MapUtils.getString(map, "image"), vo.getStoreId()));
            }

            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("ui导航栏列表明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uiIndexDetail");
        }
        return resultMap;
    }

    @Override
    public void uiSave(SaveDiyUiVo vo) throws LaiKeAPIException
    {
        try
        {
            int                  row;
            UiNavigationBarModel uiNavigationBarOld = null;
            if (vo.getId() != null)
            {
                uiNavigationBarOld = uiNavigationBarModelMapper.selectByPrimaryKey(vo.getId());
                if (uiNavigationBarOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLBCZ, "ui导航栏不存在");
                }
            }
            if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLMCBNWK, "导航栏名称不能为空");
            }
            else if (!DataCheckTool.checkLength(vo.getName(), 1, 4))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLMCBNCGGZ, "导航栏名称不能超过4个字");
            }
            if (StringUtils.isEmpty(vo.getPicUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLTPBNWK, "导航栏图片不能为空");
            }
            //自定义跳转地址可以为空
            if (!vo.getType0().equals(DiyType.CUSTOM) && StringUtils.isEmpty(vo.getUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TZDZBNWK, "跳转地址不能为空");
            }
            if (vo.getIsShow() == null)
            {
                vo.setIsShow(DictionaryConst.WhetherMaven.WHETHER_OK);
            }

            UiNavigationBarModel uiNavigationBarSave = new UiNavigationBarModel();
            uiNavigationBarSave.setType(vo.getType0());
            uiNavigationBarSave.setName(vo.getName());
            uiNavigationBarSave.setImage(ImgUploadUtils.getUrlImgByName(vo.getPicUrl(), true));
            uiNavigationBarSave.setIsshow(vo.getIsShow());
            uiNavigationBarSave.setUrl(vo.getUrl());
            uiNavigationBarSave.setIs_login(vo.getIsLogin());
            uiNavigationBarSave.setAdd_date(new Date());

            if (uiNavigationBarOld == null)
            {
                int maxSort = uiNavigationBarModelMapper.getMaxSort(vo.getStoreId());
                uiNavigationBarSave.setSort(maxSort);
                uiNavigationBarSave.setStore_id(vo.getStoreId());
            }
            else
            {
                uiNavigationBarSave.setId(uiNavigationBarOld.getId());
            }

            if (uiNavigationBarSave.getId() == null)
            {
                row = uiNavigationBarModelMapper.insertSelective(uiNavigationBarSave);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了默认模板里面的UI导航栏，名称为：" + vo.getName(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            else
            {
                row = uiNavigationBarModelMapper.updateByPrimaryKeySelective(uiNavigationBarSave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了默认模板里面的UI导航栏，名称为：" + vo.getName(), AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑 ui导航栏 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerSave");
        }
    }

    @Override
    public void uiTop(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int                  row                = 0;
            UiNavigationBarModel uiNavigationBarOld = uiNavigationBarModelMapper.selectByPrimaryKey(id);
            if (uiNavigationBarOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLBCZ, "ui导航栏不存在");
            }
            UiNavigationBarModel uiNavigationBarSave = new UiNavigationBarModel();
            uiNavigationBarSave.setId(uiNavigationBarOld.getId());
            int maxSort = uiNavigationBarModelMapper.getMaxSort(vo.getStoreId());
            uiNavigationBarSave.setSort(maxSort);
            row = uiNavigationBarModelMapper.updateByPrimaryKeySelective(uiNavigationBarSave);

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSB, "置顶失败");
            }

            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的UI导航栏：" + uiNavigationBarOld.getName() + "，进行了置顶操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("ui导航栏置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uiTop");
        }
    }

    @Override
    public void uiMove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            int row = uiNavigationBarModelMapper.move(id, id1);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDSB, "移动失败");
            }
            UiNavigationBarModel uiNavigationBarOld  = uiNavigationBarModelMapper.selectByPrimaryKey(id);
            UiNavigationBarModel uiNavigationBarOld1 = uiNavigationBarModelMapper.selectByPrimaryKey(id1);
            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的UI导航栏：" + uiNavigationBarOld.getName() + "," + uiNavigationBarOld1.getName() + "，进行了上移/下移操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("ui导航栏 上下移动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uiMove");
        }
    }

    @Override
    public void uiDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            UiNavigationBarModel uiNavigationBarModel = uiNavigationBarModelMapper.selectByPrimaryKey(id);
            if (uiNavigationBarModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLBCZ, "ui导航栏不存在");
            }
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了默认模板里面的UI导航栏，名称为：" + uiNavigationBarModel.getName(), AdminRecordModel.Type.DEL, vo.getAccessId());
            int row = uiNavigationBarModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除 ui导航栏 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uiDel");
        }
    }

    @Override
    public void uiIsShowSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            UiNavigationBarModel uiNavigationBarOld = uiNavigationBarModelMapper.selectByPrimaryKey(id);
            if (uiNavigationBarOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLBCZ, "ui导航栏不存在");
            }
            UiNavigationBarModel uiNavigationBarSave = uiNavigationBarModelMapper.selectByPrimaryKey(id);
            uiNavigationBarSave.setId(uiNavigationBarOld.getId());
            int isShow = 1;
            if (uiNavigationBarOld.getIsshow() == isShow)
            {
                isShow = 0;
            }
            uiNavigationBarSave.setIsshow(isShow);

            int row = uiNavigationBarModelMapper.updateByPrimaryKeySelective(uiNavigationBarSave);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KGSB, "开关失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));

            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的UI导航栏是否显示操作，名称为：" + uiNavigationBarOld.getName(), AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除 ui导航栏 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uiDel");
        }
    }

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Override
    public Map<String, Object> classIndex(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("sid", 0);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("status",1);
            int                       total = productClassModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = productClassModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                map.put("image", publiceService.getImgPath(MapUtils.getString(map, "image"), vo.getStoreId()));
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("ui导航栏列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classIndex");
        }
        return resultMap;
    }

    @Override
    public void reSort(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(vo.getStoreId());
            List<ProductClassModel> productListModelList = productClassModelMapper.select(productClassModel);
            int                     sort                 = 0;
            for (ProductClassModel classModel : productListModelList)
            {
                ProductClassModel productClassUpdate = new ProductClassModel();
                productClassUpdate.setCid(classModel.getCid());
                productClassUpdate.setSort(sort);
                productClassModelMapper.updateByPrimaryKeySelective(productClassUpdate);
                sort++;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品分类重新排序 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classIndex");
        }
    }

    @Override
    public void classTop(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ProductClassModel productClassOld = productClassModelMapper.selectByPrimaryKey(id);
            if (productClassOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPLBBCZ, "商品类别不存在");
            }
            int               maxSort           = productClassModelMapper.getGoodsClassMaxSort(vo.getStoreId());
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setCid(productClassOld.getCid());
            productClassModel.setSort(maxSort);

            int row = productClassModelMapper.updateByPrimaryKeySelective(productClassModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSB, "置顶失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的分类名称：" + productClassOld.getPname() + "，进行了置顶操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("类别置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classTop");
        }
    }

    @Override
    public void classMove(MainVo vo, int id, int id1) throws LaiKeAPIException
    {
        try
        {
            int row = productClassModelMapper.move(id, id1);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDSB, "移动失败");
            }
            ProductClassModel productClassOld  = productClassModelMapper.selectByPrimaryKey(id);
            ProductClassModel productClassOld1 = productClassModelMapper.selectByPrimaryKey(id1);
            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的分类名称：" + productClassOld.getPname() + "," + productClassOld1.getPname() + "，进行了上移/下移操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("类别上下移动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classMove");
        }
    }

    @Override
    public void classSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            ProductClassModel productClassOld = productClassModelMapper.selectByPrimaryKey(id);
            if (productClassOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBBCZ, "类别不存在");
            }

            ProductClassModel productClassSave = productClassModelMapper.selectByPrimaryKey(id);
            productClassSave.setCid(productClassOld.getCid());
            int isShow = 1;
            if (productClassOld.getIs_display() == isShow)
            {
                isShow = 0;
            }
            else
            {
                List<Map<String, Object>> goodsByClass = productClassModelMapper.getGoodsByClass(vo.getStoreId(), productClassOld.getCid());
                if (goodsByClass.size() <= 0)
                {
                    //禅道52063
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KGSB, "该分类下暂未绑定商品");
                }
            }
            productClassSave.setIs_display(isShow);

            int row = productClassModelMapper.updateByPrimaryKeySelective(productClassSave);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KGSB, "开关失败");
            }

            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的分类名称：" + productClassOld.getPname() + "，进行了是否显示操作", AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("类别是否显示开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classSwitch");
        }
    }

    @Autowired
    private ActivityModelMapper activityModelMapper;

    @Override
    public Map<String, Object> activityList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("id", id);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total = activityModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = activityModelMapper.selectDynamic(parmaMap);

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("活动管理列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityList");
        }
        return resultMap;
    }

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Override
    public Map<String, Object> getPluginTypeList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            List<Map<String, Object>> pluginArr = new ArrayList<>();
            //获取所有插件
            List<PluginsModel> pluginList = pluginsModelMapper.getPluginsAll(vo.getStoreId());
            for (PluginsModel plugin : pluginList)
            {
                Map<String, Object> dataMap = new HashMap<>(16);
                int                 value;
                switch (plugin.getPlugin_code())
                {
                    case DictionaryConst.Plugin.DISTRIBUTION:
                        if (!publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.DISTRIBUTION, null))continue;
                        //分销
                        value = DictionaryConst.GoodsActive.GOODSACTIVE_DISTRIBUTION;
                        break;
                    case DictionaryConst.Plugin.INTEGRAL:
                        if (!publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.INTEGRAL, null))continue;
                        //积分
                        value = DictionaryConst.GoodsActive.GOODSACTIVE_INTEGRAL;
                        break;
                    case DictionaryConst.Plugin.SECONDS:
                         if (!publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.SECONDS, null))continue;
                        //秒杀
                        value = DictionaryConst.GoodsActive.GOODSACTIVE_SECONDS;
                        break;
                    default:
                        continue;
                }
                dataMap.put("name", plugin.getPlugin_name());
                dataMap.put("value", value);
                dataMap.put("status", true);
                pluginArr.add(dataMap);
            }

            resultMap.put("list", pluginArr);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取插件类型 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPluginTypeList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activitySave(AddDiyActivityVo vo) throws LaiKeAPIException
    {
        try
        {
            int           row;
            ActivityModel activitySave = new ActivityModel();
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (vo.getActivityType() != null && vo.getActivityType() == ActivityModel.ACTIVITY_TYPE_PLUGIN)
            {
                //营销插件
                if (vo.getPlugType() == null || vo.getPlugType() == 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZHDLX, "请选择活动类型");
                }
                if (DictionaryConst.GoodsActive.SUBTRACTION_SECONDS.equals(vo.getPlugType()))
                {
                    vo.setName("满减");
                    activitySave.setSubtraction_id(vo.getSubtractionId());
                }
                activitySave.setP_id("");
            }
            else
            {
                //活动专题必须指定商品
                vo.setPlugType(0);
                if (StringUtils.isEmpty(vo.getPid()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSP, "请选择商品");
                }
                activitySave.setP_id(vo.getPid());
            }
            ActivityModel activityModelOld = null;
            activitySave.setActivity_type(vo.getActivityType());
            activitySave.setPlug_type(vo.getPlugType());
            activitySave.setName(vo.getName());
            activitySave.setSubtitle("");

            if (vo.getId() != null)
            {
                activityModelOld = activityModelMapper.selectByPrimaryKey(vo.getId());
            }
            else
            {
                activitySave.setSubtitle("");
                activitySave.setImage("0");
                activitySave.setImage("");
                activitySave.setUrl("");
                activitySave.setIs_display(DictionaryConst.WhetherMaven.WHETHER_NO);
                activitySave.setStore_id(vo.getStoreId());
            }
            //检查是否存在同营销插件活动
            if (vo.getActivityType() == 1)
            {
                if (activityModelOld == null || !vo.getPlugType().equals(activityModelOld.getPlug_type()))
                {
                    ActivityModel activityModel = new ActivityModel();
                    activityModel.setStore_id(vo.getStoreId());
                    activityModel.setActivity_type(vo.getActivityType());
                    activityModel.setPlug_type(vo.getPlugType());
                    int count = activityModelMapper.selectCount(activityModel);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDYCZ, "该营销插件活动类型已存在");
                    }
                }
            }

            //检查满减标题是否重复
            if (StringUtils.isNotEmpty(vo.getName()))
            {
                if (activityModelOld == null || !activityModelOld.getName().equals(vo.getName()))
                {
                    ActivityModel activityModel = new ActivityModel();
                    activityModel.setStore_id(vo.getStoreId());
                    activityModel.setName(vo.getName());
                    int count = activityModelMapper.selectCount(activityModel);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBTYCZ, "活动已存在");
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBTBNWK, "活动标题不能为空");
            }

            List<String> goodsIdList = null;
            if (activityModelOld != null)
            {
                activitySave.setId(activityModelOld.getId());
                row = activityModelMapper.updateByPrimaryKeySelective(activitySave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了默认模板里面的活动管理名称：" + vo.getName(), AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                activitySave.setIs_display(DictionaryConst.WhetherMaven.WHETHER_OK);
                activitySave.setAdd_date(new Date());
                activitySave.setSort(activityModelMapper.maxActivity(vo.getStoreId()));
                row = activityModelMapper.insertSelective(activitySave);
                publiceService.addAdminRecord(vo.getStoreId(), "添加了默认模板里面的活动管理名称：" + vo.getName(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }

            if (StringUtils.isNotEmpty(activitySave.getP_id()))
            {
                //只有修改修改的时候需要清一下数据
                if (activityModelOld != null)
                {
                    //删除之前的活动商品
                    ActivityProModel activityProModel = new ActivityProModel();
                    activityProModel.setActivity_id(activitySave.getId());
                    activityProModelMapper.delete(activityProModel);
                }
                //把商品写到活动表
                goodsIdList = DataUtils.convertToList(vo.getPid().split(SplitUtils.DH));
                if (goodsIdList != null)
                {
                    for (int i = 0; i < goodsIdList.size(); i++)
                    {
                        String           goodsId         = goodsIdList.get(i);
                        ActivityProModel activityProSave = new ActivityProModel();
                        activityProSave.setStore_id(vo.getStoreId());
                        activityProSave.setActivity_id(activitySave.getId());
                        activityProSave.setP_id(Integer.parseInt(goodsId));
                        activityProSave.setIs_display(DictionaryConst.WhetherMaven.WHETHER_OK);
                        activityProSave.setSort(i + 1);
                        activityProSave.setAdd_date(new Date());
                        activityProModelMapper.insertSelective(activityProSave);
                    }
                }
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
            //清空营销缓存页面
            redisUtil.del(GloabConst.RedisHeaderKey.HOME_MARKET_LIST_KEY + vo.getStoreId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存、编辑营销活动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activitySave");
        }
    }

    @Override
    public void activityMove(MainVo vo, int moveId, int moveId2) throws LaiKeAPIException
    {
        try
        {
            int row = activityModelMapper.move(moveId, moveId2);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDSB, "移动失败");
            }
            ActivityModel activityModel  = activityModelMapper.selectByPrimaryKey(moveId);
            ActivityModel activityModel1 = activityModelMapper.selectByPrimaryKey(moveId2);
            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的活动管理名称：" + activityModel.getName() + "," + activityModel1.getName() + "，进行了上移/下移操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("活动管理上下移动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityMove");
        }
    }

    @Override
    public Map<String, Object> getGoodsList(MainVo vo, String goodsName, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap   = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", adminModel.getShop_id());
            parmaMap.put("classId", classId);
            parmaMap.put("brandId", brandId);
            parmaMap.put("goodsNameLike", goodsName);
            List<Integer> goodsStatus = new ArrayList<>();
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            parmaMap.put("GoodsStatus", goodsStatus);
            parmaMap.put("not_activeGoods", "not_activeGoods");
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            int                       total = productListModelMapper.countProductList(parmaMap);
            List<Map<String, Object>> list  = productListModelMapper.getProductList(parmaMap);
            for (Map<String, Object> map : list)
            {
                map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsList");
        }
        return resultMap;
    }

    @Override
    public void activitySwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            ActivityModel activityModel = activityModelMapper.selectByPrimaryKey(id);
            if (activityModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            ActivityModel activityUpdate = new ActivityModel();
            activityUpdate.setId(activityModel.getId());
            int isShow = 1;
            if (activityModel.getIs_display() == isShow)
            {
                isShow = 0;
            }
            activityUpdate.setIs_display(isShow);
            int row = activityModelMapper.updateByPrimaryKeySelective(activityUpdate);

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDXSKGSB, "活动显示开关失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
            //活动页插件缓存清空
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.HOME_MARKET_LIST_KEY + vo.getStoreId()));
            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的活动管理名称：" + activityModel.getName() + "，进行了是否显示操作", AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("活动显示开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activitySwitch");
        }
    }

    @Override
    public void activityDel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            ActivityModel activityModel = activityModelMapper.selectByPrimaryKey(id);
            if (activityModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了默认模板里面的活动管理名称：" + activityModel.getName(), AdminRecordModel.Type.DEL, vo.getAccessId());
            int row = activityModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            //删除活动商品
            ActivityProModel activityProModel = new ActivityProModel();
            activityProModel.setStore_id(vo.getStoreId());
            activityProModel.setActivity_id(id);
            row = activityProModelMapper.delete(activityProModel);

            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("活动显示开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityDel");
        }
    }

    @Override
    public Map<String, Object> getActGoodsList(MainVo vo, int actId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ActivityModel activityModel = activityModelMapper.selectByPrimaryKey(actId);
            if (activityModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            int                       total = 0;
            List<Map<String, Object>> list  = new ArrayList<>();
            if (StringUtils.isNotEmpty(activityModel.getP_id()))
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("activityId", activityModel.getId());
                parmaMap.put("group_goodsId", "group_goodsId");
                parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("pageStart", vo.getPageNo());
                parmaMap.put("pageEnd", vo.getPageSize());
                total = activityProModelMapper.countGoodsDynamic(parmaMap);
                if (total > 0)
                {
                    list = activityProModelMapper.selectGoodsDynamic(parmaMap);
                }

                for (Map<String, Object> map : list)
                {
                    map.put("imgurl", publiceService.getImgPath(MapUtils.getString(map, "imgurl"), vo.getStoreId()));
                }
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取活动商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getActGoodsList");
        }
        return resultMap;
    }


    @Override
    public void actGoodsMove(MainVo vo, int id, int goodsId, int goodsId1) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ActivityModel activityModel = activityModelMapper.selectByPrimaryKey(id);
            if (activityModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            if (StringUtils.isEmpty(activityModel.getP_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            /*int index1 = -1;
            int index2 = -1;
            //重新排序
            String[] goodsIds = activityModel.getP_id().split(",");
            for (int i = 0; i < goodsIds.length; i++) {
                if (index1 > -1 && index2 > -1) {
                    break;
                }
                String gId = goodsIds[i];
                if (gId.equals(goodsId + "")) {
                    index1 = i;
                    continue;
                }
                if (gId.equals(goodsId1 + "")) {
                    index2 = i;
                }
            }
            if (index1 == -1 || index2 == -1) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HWQZYGSPBCZ, "换位其中一个商品不存在");
            }
            //换位
            String indexStr1 = goodsIds[index1];
            String indexStr2 = goodsIds[index2];
            goodsIds[index1] = indexStr2;
            goodsIds[index2] = indexStr1;

            ActivityModel activityUpdate = new ActivityModel();
            activityUpdate.setId(activityModel.getId());
            activityUpdate.setP_id(StringUtils.stringImplode(DataUtils.convertToList(goodsIds), ","));*/

            int row;
            if (goodsId > 0 && goodsId1 > 0)
            {
                row = activityProModelMapper.move(goodsId, goodsId1);
            }
            else
            {
                //置顶
                ActivityProModel activityProModel = activityProModelMapper.selectByPrimaryKey(goodsId);
                if (activityProModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDSPBCZ, "活动商品不存在");
                }
                ActivityProModel activityProUpdate = new ActivityProModel();
                activityProUpdate.setId(activityProModel.getId());
                activityProUpdate.setSort(activityProModelMapper.maxActivity(activityModel.getId()));
                row = activityProModelMapper.updateByPrimaryKeySelective(activityProUpdate);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDSPYDSB, "活动商品移动失败");
            }
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
    }

    @Override
    public void actGoodsSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ActivityProModel activityProModel = activityProModelMapper.selectByPrimaryKey(id);
            if (activityProModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDSPBCZ, "活动商品不存在");
            }
            int isShow = 1;
            if (activityProModel.getIs_display().equals(isShow))
            {
                isShow = 0;
            }
            ActivityProModel activityProUpdate = new ActivityProModel();
            activityProUpdate.setId(activityProModel.getId());
            activityProUpdate.setIs_display(isShow);
            int row = activityProModelMapper.updateByPrimaryKeySelective(activityProUpdate);

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("活动商品是否显示开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "actGoodsSwitch");
        }
    }

    @Override
    public boolean isDiyPlugin(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            return publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.DIY, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("是否开启diy插件 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "actGoodsMove");
        }
    }

    @Override
    public Map<String, Object> getTemplateDiyList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);


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
    public void addTemplate(MainVo vo, String jsonValue, String title, String cover, Integer id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int row;
            if (StringUtils.isEmpty(title))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBMCBNWK, "模板名称不能为空");
            }
            DiyModel diyOld = null;
            if (id != null)
            {
                diyOld = diyModelMapper.selectByPrimaryKey(id);
                if (diyOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "diy模板不存在");
                }
            }

            DiyModel diySave = new DiyModel();
            diySave.setCover(cover);
            diySave.setName(title);
            diySave.setValue(jsonValue);
            diySave.setUpdate_time(DateUtil.getTime());
            diySave.setAdd_time(DateUtil.getTime());
            if (diyOld == null)
            {
                if (StringUtils.isEmpty(jsonValue))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBNWK, "diy配置不能为空");
                }
                diySave.setStore_id(vo.getStoreId());
                diySave.setVersion("1.0");
                diySave.setStatus(DictionaryConst.WhetherMaven.WHETHER_NO);
                diySave.setType(1);
                diySave.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            }
            else
            {
                diySave.setId(id);
            }

            if (diyOld == null || !diyOld.getName().equals(title))
            {
                DiyModel diyModel = new DiyModel();
                diyModel.setStore_id(vo.getStoreId());
                diyModel.setName(title);
                int count = diyModelMapper.selectCount(diyModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBMCYCZ, "模板名称已存在");
                }
            }

            if (diyOld == null)
            {
                row = diyModelMapper.insertSelective(diySave);
            }
            else
            {
                row = diyModelMapper.updateByPrimaryKeySelective(diySave);
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBTJSB, "模板添加失败");
            }
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
    }

    @Override
    public void setDiy(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int      row;
            DiyModel diyOld = new DiyModel();
            diyOld.setId(id);
            diyOld.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            diyOld = diyModelMapper.selectOne(diyOld);
            if (diyOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "diy模板不存在");
            }
            //移除之前默认模板
            row = diyModelMapper.removeDefault(vo.getStoreId());

            if (diyOld.getStatus() == 0)
            {
                //设置该模板为默认
                DiyModel diyUpdate = new DiyModel();
                diyUpdate.setId(diyOld.getId());
                diyUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                row = diyModelMapper.updateByPrimaryKeySelective(diyUpdate);
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBSZSB, "diy模板设置失败");
            }
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
            int      row;
            DiyModel diyOld = new DiyModel();
            diyOld.setId(id);
            diyOld.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            diyOld = diyModelMapper.selectOne(diyOld);
            if (diyOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "diy模板不存在");
            }
            DiyModel diyUpdate = new DiyModel();
            diyUpdate.setId(diyOld.getId());
            diyUpdate.setIs_del(DictionaryConst.WhetherMaven.WHETHER_OK);
            row = diyModelMapper.updateByPrimaryKeySelective(diyUpdate);

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBSCSB, "diy模板删除失败");
            }
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
    public void uiIsLoginSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            UiNavigationBarModel uiNavigationBarOld = uiNavigationBarModelMapper.selectByPrimaryKey(id);
            if (uiNavigationBarOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHLBCZ, "ui导航栏不存在");
            }
            UiNavigationBarModel uiNavigationBarSave = new UiNavigationBarModel();
            uiNavigationBarSave.setId(uiNavigationBarOld.getId());
            int isLogin = 1;
            if (uiNavigationBarOld.getIs_login() == isLogin)
            {
                isLogin = 0;
            }
            uiNavigationBarSave.setIs_login(isLogin);

            int row = uiNavigationBarModelMapper.updateByPrimaryKeySelective(uiNavigationBarSave);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KGSB, "开关失败");
            }
            //涉及首页,这里清空首页缓存
            redisUtil.del(String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, vo.getStoreId()));

            publiceService.addAdminRecord(vo.getStoreId(), "将默认模板里面的UI导航栏是否登录操作，名称为：" + uiNavigationBarOld.getName(), AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("ui导航栏是否需要登录开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uiIsLoginSwitch");
        }
    }
}