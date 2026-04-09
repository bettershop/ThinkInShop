package com.laiketui.comps.file.services;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.FilesRecordModelMapper;
import com.laiketui.common.mapper.ImgGroupModelMapper;
import com.laiketui.common.mapper.UploadConfigModelMapper;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.file.CompsFileService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.FilesRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.upload.ImgGroupModel;
import com.laiketui.domain.upload.UploadConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.FilesVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

/**
 * 资源上传
 *
 * @author Trick
 * @date 2021/7/7 18:20
 */
@Service
public class CompsFileServiceImpl implements CompsFileService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ImgGroupModelMapper imgGroupModelMapper;

    @Autowired
    private FilesRecordModelMapper filesRecordModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private UploadConfigModelMapper uploadConfigModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Override
    public Map<String, Object> index(FilesVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            ImgGroupModel imgGroupModel = null;
            if (vo.getGroupId() != null)
            {
                imgGroupModel = new ImgGroupModel();
                imgGroupModel.setStore_id(vo.getStoreId());
                imgGroupModel.setId(vo.getGroupId());
                imgGroupModel = imgGroupModelMapper.selectOne(imgGroupModel);
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (vo.getGroupId() != null && vo.getGroupId() != -1)
            {
                parmaMap.put("groupId", vo.getGroupId());
            }
            if (Objects.nonNull(vo.getImg_type()))
            {
                parmaMap.put("img_type", vo.getImg_type());
            }
            else
            {
                parmaMap.put("img_type", 0);
            }
            if (StringUtils.isNotEmpty(vo.getTitle()))
            {
                parmaMap.put("title", vo.getTitle());
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startTime", vo.getStartDate());
            }
            if (StringUtils.isNotEmpty(vo.getEndDate()))
            {
                parmaMap.put("endTime", vo.getEndDate());
            }
            if (!Objects.isNull(vo.getMchId()))
            {
                parmaMap.put("mchId", vo.getMchId());
            }
            if (!Objects.isNull(vo.getSupplierId()))
            {
                parmaMap.put("supplierId", vo.getSupplierId());
            }
            //获取上传默认上传配置
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
            uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
            if (StringUtils.isEmpty(uploadConfigModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTTPWPZ, "平台图片配置未配置!", "uploadImage");
            }
            String attrvalue = uploadConfigModel.getAttrvalue();
            parmaMap.put("upload_mode", attrvalue);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("notFile", "notFile");
            int total = filesRecordModelMapper.countDynamic(parmaMap);
            //禅道 50974 排除MP4 和webg的格式文件
            List<Map<String, Object>> list = filesRecordModelMapper.selectDynamic(parmaMap);

            for (Map<String, Object> map : list)
            {
                String        imgName    = MapUtils.getString(map, "image_name");
                StringBuilder imgUrl     = new StringBuilder();
                String        uploadType = MapUtils.getString(map, "upload_mode");
                switch (uploadType)
                {
                    case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST:
                        //获取远程路径
                        imgUrl.append(DataUtils.getServcerUrl(vo.getRequest()));
                        String path;
                        if (imgGroupModel != null)
                        {
                            path = PinyinUtils.getPinYin(imgGroupModel.getName());
                            imgUrl.append(File.separator).append(path);
                        }
                        imgUrl.append(File.separator).append(imgName);
                        break;
                    case GloabConst.UploadConfigConst.IMG_UPLOAD_OSS:
                    case GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO:
                        imgUrl.append(publiceService.getImgPath(imgName, vo.getStoreId()));
                        break;
                    default:
                        break;
                }
                map.put("url", imgUrl);
                //diy前端插件图片路径必须要用该参数
                map.put("att_dir", imgUrl);
                if (!map.containsKey("name"))
                {
                    map.put("name", DataUtils.getStringVal(map, "image_name"));
                }
            }

            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("文件列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> groupList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String              loginForUserInfo = RedisDataTool.isLoginForUserInfo(vo.getAccessId(), redisUtil);
            Map<String, Object> userInfo         = addForUser(loginForUserInfo, vo.getAccessId());
            Map<String, Object> parmaMap         = new HashMap<>(16);
            //供应商添加还是商家添加，用于填充 商家id 或者 供应商id做不同角色查询
            if (userInfo.containsKey("supplierId"))
            {
                parmaMap.put("supplier_id", DataUtils.getStringVal(userInfo, "supplierId"));
            }
            else if (userInfo.containsKey("mchId"))
            {
                parmaMap.put("mch_id", DataUtils.getStringVal(userInfo, "mchId"));
            }
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> list = imgGroupModelMapper.selectDynamic(parmaMap);

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
    public void createCatalogue(MainVo vo, String catalogueName, Integer id) throws LaiKeAPIException
    {
        try
        {
            int                 row;
            String              loginForUserInfo = RedisDataTool.isLoginForUserInfo(vo.getAccessId(), redisUtil);
            Map<String, Object> userInfo         = addForUser(loginForUserInfo, vo.getAccessId());
            if (StringUtils.isEmpty(catalogueName))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLMBNWK, "目录名不能为空");
            }
            ImgGroupModel imgGroupSave = new ImgGroupModel();
            ImgGroupModel imgGroupOld  = null;
            //供应商添加还是商家添加，用于填充 商家id 或者 供应商id
            if (userInfo.containsKey("supplierId"))
            {
                imgGroupSave.setSupplier_id(DataUtils.getIntegerVal(userInfo, "supplierId"));
            }
            else if (userInfo.containsKey("mchId"))
            {
                imgGroupSave.setMch_id(DataUtils.getIntegerVal(userInfo, "mchId"));
            }
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
                //供应商添加还是商家添加，用于填充 商家id 或者 供应商id
                if (userInfo.containsKey("supplierId"))
                {
                    imgGroupModel.setSupplier_id(DataUtils.getIntegerVal(userInfo, "supplierId"));
                }
                else if (userInfo.containsKey("mchId"))
                {
                    imgGroupModel.setMch_id(DataUtils.getIntegerVal(userInfo, "mchId"));
                }
                imgGroupModel.setStore_id(vo.getStoreId());
                imgGroupModel.setName(catalogueName);
                int count = imgGroupModelMapper.selectCount(imgGroupModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MLMYCZ, "目录名已存在");
                }
            }
            imgGroupSave.setName(catalogueName);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> uploadImage(UploadFileVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String loginForUserInfo = RedisDataTool.isLoginForUserInfo(vo.getAccessId(), redisUtil);
            //填充add_user字段
            Map<String, Object> userInfo = addForUser(loginForUserInfo, vo.getAccessId());
            String              add_user = DataUtils.getStringVal(userInfo, "add_user");
            vo.setAdd_user(add_user);
            //获取商城默认配置
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
            uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
            if (StringUtils.isEmpty(uploadConfigModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB_001, "平台图片配置未配置!", "uploadImage");
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
                    if (userInfo.containsKey("supplierId"))
                    {
                        params.put("supplierId", DataUtils.getStringVal(userInfo, "supplierId"));
                    }
                    else if (userInfo.containsKey("mchId"))
                    {
                        params.put("mchId", DataUtils.getStringVal(userInfo, "mchId"));
                    }
                    params.put("store_type", DataUtils.getStringVal(userInfo, "store_type"));
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
                        int row = filesRecordModelMapper.updateByPrimaryKeySelective(filesRecordModel);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                        }
                    }
                    else
                    {
                        if (Objects.isNull(vo.getImg_type())) {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_WJMCCF, "文件名称重复");
                        }
                    }
                }
                List<String> imgUrlList = publiceService.uploadFiles(vo);
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

    private Map<String, Object> addForUser(String loginForUserInfo, String accessId)
    {
        String              add_user   = "";
        int                 store_type = GloabConst.StoreType.STORE_TYPE_PC_ADMIN;
        Map<String, Object> result     = new HashMap<>();
        if (StringUtils.isNotEmpty(loginForUserInfo))
        {
            //三个端，后台、店铺、供应商
            if (GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN.equals(loginForUserInfo))
            {
                AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(accessId, redisUtil);
                if (adminModel != null)
                {
                    if (StringUtils.isNotEmpty(adminModel.getName()))
                    {
                        add_user = adminModel.getName();
                    }
                    else
                    {
                        add_user = adminModel.getNickname();
                    }
                    result.put("mchId", adminModel.getShop_id());
                }
            }
            else if (GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN.equals(loginForUserInfo))
            {
                User user = RedisDataTool.getRedisUserCache(accessId, redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
                if (user != null)
                {
                    add_user = user.getZhanghao();
                    store_type = GloabConst.StoreType.STORE_TYPE_PC_MCH;
                    result.put("mchId", user.getMchId());
                }
            }
            else if (GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN.equals(loginForUserInfo))
            {
                SupplierModel supplierCache = RedisDataTool.getSupplierCache(accessId, redisUtil);
                if (supplierCache != null)
                {
                    add_user = supplierCache.getSupplier_name();
                    result.put("supplierId", supplierCache.getId());
                }
            }
        }
        result.put("add_user", add_user);
        result.put("store_type", store_type);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delCatalogue(MainVo vo, int id) throws LaiKeAPIException
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
                FilesRecordModel filesRecordDel = new FilesRecordModel();
                filesRecordDel.setGroup(id + "");
                filesRecordModelMapper.deleteByPrimaryKey(filesRecordDel);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delFile(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            String[] idList = ids.split(SplitUtils.DH);

            if (idList.length == 1) {
                FilesRecordModel filesRecordOld = filesRecordModelMapper.selectByPrimaryKey(idList[0]);
                if (Objects.nonNull(filesRecordOld.getDiy_img_type()) && filesRecordOld.getDiy_img_type() == 0) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XTZTTPBKSC, "系统主题图片不可删除");
                }
            }
            int i = 0;
            for (String id : idList)
            {
                FilesRecordModel filesRecordOld = filesRecordModelMapper.selectByPrimaryKey(id);
                if (filesRecordOld == null)
                {
                    logger.debug("删除图片时 id:{} 找不到数据", id);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBCZ, "图片不存在");
                } else if (Objects.nonNull(filesRecordOld.getDiy_img_type()) && filesRecordOld.getDiy_img_type() == 0) {
                    i++;
                    continue;
                }
                publiceService.delFileRecoderModel(filesRecordOld);
            }
            if (i == idList.length) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XTZTTPBKSC, "系统主题图片不可删除");
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
    @Transactional(rollbackFor = Exception.class)
    public void uploadUrlFiles(MainVo vo, String imgUrl, String text, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            logger.debug("正在下载外链图片 url:{}", imgUrl);
            String           imgOssUrl      = publiceService.uploadImage(imgUrl, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, vo.getStoreType(), vo.getStoreId(), mchId);
            FilesRecordModel filesRecordOld = new FilesRecordModel();
            filesRecordOld.setImage_name(ImgUploadUtils.getUrlImgByName(imgOssUrl, true));
            filesRecordOld.setStore_id(vo.getStoreId() + "");
            filesRecordOld.setStore_type(vo.getStoreType() + "");
            if (StringUtils.isNotEmpty(mchId))
            {
                filesRecordOld.setMch_id(mchId);
            }
            filesRecordOld.setGroup("-1");
            filesRecordOld = filesRecordModelMapper.selectOne(filesRecordOld);
            if (filesRecordOld != null)
            {
                FilesRecordModel filesRecordUpdate = new FilesRecordModel();
                filesRecordUpdate.setId(filesRecordOld.getId());
                filesRecordUpdate.setExplain(text);
                filesRecordModelMapper.updateByPrimaryKeySelective(filesRecordUpdate);
            }
            logger.debug("上传到oss后的图片 url:{}", imgUrl);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("下载外链图片到oss上 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadUrlFiles");
        }
    }
}

