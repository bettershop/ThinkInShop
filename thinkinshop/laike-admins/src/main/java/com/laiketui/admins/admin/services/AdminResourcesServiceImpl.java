package com.laiketui.admins.admin.services;

import com.laiketui.admins.api.admin.AdminResourcesService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.FilesRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.upload.ImgGroupModel;
import com.laiketui.domain.upload.UploadConfigModel;
import com.laiketui.domain.upload.UploadImagModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * 资源管理
 *
 * @author Trick
 * @date 2021/7/21 16:43
 */
@Service
public class AdminResourcesServiceImpl implements AdminResourcesService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FilesRecordModelMapper filesRecordModelMapper;

    @Autowired
    private UploadConfigModelMapper uploadConfigModelMapper;
    @Autowired
    private ImgGroupModelMapper     imgGroupModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo, String imageName, String startTime, String endTime, String groupId, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (StringUtils.isNotEmpty(imageName))
            {
                parmaMap.put("name", imageName);
            }
            if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime))
            {
                parmaMap.put("startTime", startTime);
                parmaMap.put("endTime", endTime);
            }
            if (StringUtils.isNotEmpty(groupId) && !groupId.equals("-1"))
            {
                parmaMap.put("groupId", groupId);
            }
            if (type == null)
            {
                //默认设置为查看图片
                type = 1;
            }
            //oos + minio
            List<String> uploadList = Arrays.asList(GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("uploadList", uploadList);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            //添加查找的是视频还是图片
            parmaMap.put("type", type);
            //获取自营店id
            Integer                   storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            MchModel                  mchModel   = mchModelMapper.selectByPrimaryKey(storeMchId);
            int                       total      = filesRecordModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list       = new ArrayList<>();
            if (total > 0)
            {
                list = filesRecordModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : list)
                {
                    map.put("imgUrl", publiceService.getImgPath(MapUtils.getString(map, "image_name"), vo.getStoreId()));
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                    map.put("groupName", "默认分类");
                    if (!map.containsKey("name"))
                    {
                        map.put("name", MapUtils.getString(map, "image_name"));
                    }
                    if (!"-1".equals(DataUtils.getStringVal(map, "group")))
                    {
                        ImgGroupModel imgGroupModel = imgGroupModelMapper.selectByPrimaryKey(DataUtils.getStringVal(map, "group"));
                        if (imgGroupModel != null)
                        {
                            map.put("groupName", imgGroupModel.getName());
                        }
                    }
                    if (StringUtils.isEmpty(MapUtils.getString(map, "mchName")))
                    {
                        if (Objects.nonNull(mchModel))
                        {
                            //默认为自营店上传
                            map.put("mchName", mchModel.getName());
                        }
                    }
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
            logger.error("资源列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public void downForZip(MainVo vo, HttpServletResponse response, String imgIds) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(imgIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            List<Map<String, Object>> imgNameMaps = filesRecordModelMapper.getImgNameByIds(Arrays.asList(imgIds.split(SplitUtils.DH)));
            if (imgNameMaps == null || imgNameMaps.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBCZ, "图片不存在");
            }
            /*String fileName=storeId + SplitUtils.FXG + storeType;
                        fileName.append(SplitUtils.FXG).append(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD1));
                        fileName.append(SplitUtils.FXG).append(imgName);*/
            for (Map<String, Object> map : imgNameMaps)
            {
                map.put("add_time", DateUtil.dateFormate(DataUtils.getStringVal(map, "add_time"), GloabConst.TimePattern.YMD1));
            }

            UploadImagModel imagModel = new UploadImagModel();
            imagModel.setUploadType(GloabConst.UploadConfigConst.IMG_UPLOAD_OSS);
            //从数据库获取上传配置key
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(imagModel.getUploadType());
            List<UploadConfigModel> uploadConfigs = uploadConfigModelMapper.select(uploadConfigModel);
            //装载参数
            imagModel.setUploadConfigs(uploadConfigs);

            //下载文件
            ImgUploadUtils file = new ImgUploadUtils();
            file.downImages(imagModel, imgNameMaps, response, vo.getStoreId(), vo.getStoreType());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量下载图片 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "downForZip");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(MainVo vo, String imgIds) throws LaiKeAPIException
    {
        try
        {
            AdminModel   adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int          row;
            List<String> ids        = DataUtils.convertToList(imgIds.split(SplitUtils.DH));
            if (ids == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            for (String id : ids)
            {
                row = filesRecordModelMapper.deleteByPrimaryKey(id);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
                }
            }
            publiceService.addAdminRecord(vo.getStoreId(), "将图片资源进行了批量删除操作", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    @Transactional
    public void addGroup(MainVo vo, String catalogueName, Integer id, Integer type) throws LaiKeAPIException
    {
        try
        {
            int        row;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(catalogueName))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLMBNWK, "目录名不能为空");
            }
            if (type != 1 && type != 2)
            {
                type = 1;
            }
            ImgGroupModel imgGroupSave = new ImgGroupModel();
            ImgGroupModel imgGroupOld  = null;
            if (id != null)
            {
                imgGroupOld = imgGroupModelMapper.selectByPrimaryKey(id);
                if (imgGroupOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLBCZ, "目录不存在");
                }
            }
            if (imgGroupOld == null || !imgGroupOld.getName().equals(catalogueName))
            {
                ImgGroupModel imgGroupModel = new ImgGroupModel();
                imgGroupModel.setStore_id(vo.getStoreId());
                imgGroupModel.setName(catalogueName);
                imgGroupModel.setType(type);
                int count = imgGroupModelMapper.selectCount(imgGroupModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLMYCZ, "目录名已存在");
                }
            }
            imgGroupSave.setName(catalogueName);
            imgGroupSave.setType(type);
            if (imgGroupOld != null)
            {
                imgGroupSave.setId(id);
                row = imgGroupModelMapper.updateByPrimaryKeySelective(imgGroupSave);
            }
            else
            {
                imgGroupSave.setSort(100);
                imgGroupSave.setIs_default(0);
                imgGroupSave.setAdd_date(new Date());
                imgGroupSave.setStore_id(vo.getStoreId());
                imgGroupSave.setMch_id(adminModel.getShop_id());
                row = imgGroupModelMapper.insertSelective(imgGroupSave);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLCJSB, "目录创建失败");
            }
            //服务器资源路径
            String realPath = ImgUploadUtils.DEFAULT_UPLOAD_PATH;
            //判断文件夹是否存在,不存在则创建
            File diyFile = new File(realPath + File.separator + PinyinUtils.getPinYin(catalogueName));
            if (!diyFile.exists() && !diyFile.isDirectory())
            {
                logger.debug("目录:{} 不存在,创建状态:{}", diyFile.getPath(), diyFile.mkdir());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("图片上传分类列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "groupList");
        }
    }

    @Override
    public Map<String, Object> groupList(MainVo vo, Integer type)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap   = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("type", type);
            parmaMap.put("mch_id", adminModel.getShop_id());
            List<Map<String, Object>> list = imgGroupModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                HashMap<String, Object> params = new HashMap<>();
                params.put("groupId", map.get("id"));
                params.put("store_id", map.get("store_id"));
                int i = filesRecordModelMapper.countDynamic(params);
                if (i > 0)
                {
                    map.put("haveContent", 1);
                }
            }
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("图片上传分类列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "groupList");
        }
        return resultMap;
    }

    @Override
    @Transactional
    public void delFile(MainVo vo, String ids)
    {
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            String[] idList = ids.split(SplitUtils.DH);
            for (String id : idList)
            {
                FilesRecordModel filesRecordOld = filesRecordModelMapper.selectByPrimaryKey(id);
                if (filesRecordOld == null)
                {
                    logger.debug("删除图片时 id:{} 找不到数据", id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBCZ, "图片不存在");
                }
                publiceService.delFileRecoderModel(filesRecordOld);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除图片 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delFile");
        }
    }

    @Override
    @Transactional
    public void delCatalogue(MainVo vo, int id)
    {
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            ImgGroupModel imgGroupModel = imgGroupModelMapper.selectByPrimaryKey(id);
            if (imgGroupModel != null)
            {
                //服务器资源路径
                String realPath = ImgUploadUtils.DEFAULT_UPLOAD_PATH;
                //判断文件夹是否存在,不存在则创建
                File diyFile = new File(realPath + File.separator + PinyinUtils.getPinYin(imgGroupModel.getName()));
                if (diyFile.exists() && diyFile.isDirectory())
                {
                    boolean flag = diyFile.delete();
                    logger.debug("目录:{} 存在,删除状态:{}", diyFile.getPath(), flag);
                    if (!flag)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLSCSB, "目录删除失败");
                    }
                }
                else
                {
                    logger.debug("目录:{} 不存在,删除失败", diyFile.getPath());
                }
                //删除图片分类
                int row = imgGroupModelMapper.deleteByPrimaryKey(id);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FLSCSB, "分类删除失败");
                }
                //删除文件夹里面的文件
                HashMap<String, Object> deleteMap = new HashMap<>();
                deleteMap.put("groupId", id + "");
                filesRecordModelMapper.delFileRecord(deleteMap);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLBCZ, "目录不存在");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除目录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delCatalogue");
        }
    }

    @Override
    @Transactional
    public Map<String, Object> uploadImage(UploadFileVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取商城默认配置
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
            uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
            if (StringUtils.isEmpty(uploadConfigModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB_001, "平台图片配置未配置!", "uploadImage");
            }
            //默认使用账号名称，没有则使用昵称
            if (adminModel.getName() != null)
            {
                vo.setAdd_user(adminModel.getName());
            }
            else
            {
                vo.setAdd_user(adminModel.getNickname());
            }
            String attrvalue = uploadConfigModel.getAttrvalue();
            vo.setUploadType(attrvalue);
            FilesRecordModel filesRecordSave = new FilesRecordModel();
            FilesRecordModel filesRecordOld  = null;
            if (vo.getId() != null)
            {
                filesRecordOld = filesRecordModelMapper.selectByPrimaryKey(vo.getId());
                if (filesRecordOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBCZ, "图片不存在");
                }
            }
            List<String> imgUrls = new ArrayList<>();
            if (filesRecordOld == null)
            {
                //上传图片
                List<MultipartFile> multipartFiles = DataUtils.convertToList(vo.getImage());
                if (multipartFiles == null || multipartFiles.isEmpty())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QWTJKBWJ, "请勿提交空白文件!", "uploadImage");
                }
                //处理图片名称重复的问题
                List<String> filesName = new ArrayList<>();
                for (MultipartFile file : multipartFiles)
                {
                    String fileName = file.getOriginalFilename();
                    filesName.add(fileName);
                }
                Map<String, Object> params = new HashMap<>();
                params.put("store_id", vo.getStoreId());
                params.put("names", filesName);
                params.put("store_type", vo.getStoreType());
                if (vo.getSupplierId() != null)
                {
                    params.put("supplierId", vo.getSupplierId());
                }
                if (vo.getMchId() != null)
                {
                    params.put("mchId", vo.getMchId());
                }
                //覆盖时获取用户信息用来修改图片信息
                if (vo.getCoverage() != null && vo.getCoverage().equals(1))
                {
                    params.put("mchId", adminModel.getShop_id());
                    params.put("store_type", GloabConst.StoreType.STORE_TYPE_PC_ADMIN);
                }
                List<Map<String, Object>> maps = filesRecordModelMapper.selectDynamic(params);
                if (maps != null && !maps.isEmpty())
                {
                    if (vo.getCoverage() != null && vo.getCoverage().equals(1))
                    {
                        //覆盖提交，逻辑删除原来的图片
                        FilesRecordModel filesRecordModel = new FilesRecordModel();
                        filesRecordModel.setId(Long.valueOf(DataUtils.getIntegerVal(maps.get(0), "id")));
                        filesRecordModel.setRecycle(1);
                        vo.setStoreType(GloabConst.StoreType.STORE_TYPE_PC_ADMIN);
                        int row = filesRecordModelMapper.updateByPrimaryKeySelective(filesRecordModel);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_WJMCCF, "文件名称重复");
                    }
                }
                List<String> imgUrlList = publiceService.uploadFiles(vo);
                /*List<MultipartFile> files = DataUtils.convertToList(vo.getImage());
                for (MultipartFile file : files) {
                    file.getSize();
                    BufferedImage read = ImageIO.read((File) file);
                    read.getHeight();
                    read.getWidth();
                }*/
                if (GloabConst.UploadConfigConst.IMG_UPLOAD_OSS.equals(vo.getUploadType())
                        || GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO.equals(vo.getUploadType()))
                {
                    for (String imgUrl : imgUrlList)
                    {
                        imgUrls.add(ImgUploadUtils.getUrlPure(imgUrl, true));
                    }
                }
            }
            else
            {
                filesRecordSave.setId(filesRecordOld.getId());
                if (vo.getMchId() != null)
                {
                    filesRecordSave.setMch_id(vo.getMchId());
                }
                //分组
                if (vo.getGroupId() != null)
                {
                    filesRecordSave.setGroup(vo.getGroupId().toString());
                }
                //修改图片参数
                if (StringUtils.isNotEmpty(vo.getTitle()))
                {
                    filesRecordSave.setTitle(vo.getTitle());
                }
                if (StringUtils.isNotEmpty(vo.getExplain()))
                {
                    filesRecordSave.setExplain(vo.getExplain());
                }
                if (StringUtils.isNotEmpty(vo.getName()))
                {
                    //需要判断在同一个店铺下或者是供应商下是否有相同名称的图片
                    filesRecordSave.setName(vo.getName());
                    FilesRecordModel filesRecordModel = new FilesRecordModel();
                    filesRecordModel.setName(vo.getName());
                    filesRecordModel.setMch_id(vo.getMchId());
                    filesRecordModel.setStore_id(String.valueOf(vo.getStoreId()));
                    filesRecordModel.setRecycle(0);
                    List<FilesRecordModel> select = filesRecordModelMapper.select(filesRecordModel);
                    if (select != null && !select.isEmpty())
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_WJMCCF, "文件名称重复");
                    }
                }
                if (StringUtils.isNotEmpty(vo.getDescribe()))
                {
                    filesRecordSave.setDescribe(vo.getDescribe());
                }
                if (StringUtils.isNotEmpty(vo.getAlternativeText()))
                {
                    filesRecordSave.setAlternative_text(vo.getAlternativeText());
                }
                int row = filesRecordModelMapper.updateByPrimaryKeySelective(filesRecordSave);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                }
                String imgUrl = publiceService.getImgPath(filesRecordOld.getImage_name(), vo.getStoreId());
                imgUrls.add(ImgUploadUtils.getUrlPure(imgUrl, true));
            }
            resultMap.put("imgUrls", imgUrls);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("图片上传 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImage");
        }
        return resultMap;
    }

    @Override
    @Transactional
    public void updateCatalogueShow(MainVo vo, String id, Integer type)
    {
        try
        {
            AdminModel   adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int          row;
            List<String> ids        = DataUtils.convertToList(id.split(SplitUtils.DH));
            if (ids == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //查询出该用户所有的分类，前端传过来的id则设置为常驻，其他的则设置为非常驻
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type", type);
            //parmaMap.put("mch_id", adminModel.getShop_id());
            List<Map<String, Object>> groupList = imgGroupModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> group : groupList)
            {
                ImgGroupModel imgGroupModel = new ImgGroupModel();
                imgGroupModel.setIs_show(1);
                imgGroupModel.setId(DataUtils.getIntegerVal(group, "id"));
                row = imgGroupModelMapper.updateByPrimaryKeySelective(imgGroupModel);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                }
            }
            for (String catalogueId : ids)
            {
                //如果前端传来的id为空，则将所有的都设置为非常驻
                if (StringUtils.isEmpty(catalogueId))
                {
                    break;
                }
                ImgGroupModel imgGroupModel = new ImgGroupModel();
                imgGroupModel.setIs_show(2);
                imgGroupModel.setId(Integer.valueOf(catalogueId));
                row = imgGroupModelMapper.updateByPrimaryKeySelective(imgGroupModel);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                }
            }
            publiceService.addAdminRecord(vo.getStoreId(), "将图片资源分类展示进行了批量更新操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改分类展示状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    @Transactional
    public void updateCatalogueByImageIds(MainVo vo, String imageIds, Integer catalogueId)
    {
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(imageIds) || StringUtils.isEmpty(catalogueId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            String[] idList = imageIds.split(SplitUtils.DH);
            for (String id : idList)
            {
                FilesRecordModel filesRecordModel = new FilesRecordModel();
                filesRecordModel.setGroup(String.valueOf(catalogueId));
                filesRecordModel.setId(Long.valueOf(id));
                int row = filesRecordModelMapper.updateByPrimaryKeySelective(filesRecordModel);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量修改分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delFile");
        }
    }
}


