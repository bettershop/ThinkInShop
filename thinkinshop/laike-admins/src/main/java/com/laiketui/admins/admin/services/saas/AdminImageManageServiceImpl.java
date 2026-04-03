package com.laiketui.admins.admin.services.saas;

import com.laiketui.admins.api.admin.saas.AdminImageManageService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.UploadConfigModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.upload.UploadConfigModel;
import com.laiketui.domain.vo.admin.image.AddImageConfigVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 图片管理
 *
 * @author Trick
 * @date 2021/1/29 17:54
 */
@Service
public class AdminImageManageServiceImpl implements AdminImageManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminImageManageServiceImpl.class);

    @Autowired
    private UploadConfigModelMapper uploadConfigModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> getImageConfigInfo(Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取商城默认图片上传配置
            if (StringUtils.isEmpty(type))
            {
                UploadConfigModel uploadConfigModel = new UploadConfigModel();
                uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
                uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
                if (StringUtils.isEmpty(uploadConfigModel))
                {
                    //默认oos
                    uploadConfigModel = new UploadConfigModel();
                    uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
                    uploadConfigModel.setType("默认上传方式");
                    uploadConfigModel.setAttr("upserver_type");
                    uploadConfigModel.setAttrvalue(GloabConst.UploadConfigConst.IMG_UPLOAD_OSS);
                    int i = uploadConfigModelMapper.insertSelective(uploadConfigModel);
                    if (i < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙");
                    }
                }
                String attrvalue = uploadConfigModel.getAttrvalue();
                type = Integer.valueOf(attrvalue);
            }
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(String.valueOf(type));
            List<UploadConfigModel> uploadConfigModels = uploadConfigModelMapper.select(uploadConfigModel);

            maskUploadConfigSecrets(uploadConfigModels);

            resultMap.put("data", uploadConfigModels);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取上传图片配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getImageConfigInfo");
        }
        return resultMap;
    }

    private void maskUploadConfigSecrets(List<UploadConfigModel> uploadConfigModels) throws LaiKeAPIException
    {
        if (uploadConfigModels == null || uploadConfigModels.isEmpty())
        {
            return;
        }
        for (UploadConfigModel model : uploadConfigModels)
        {
            if (model == null)
            {
                continue;
            }
            String attr = model.getAttr();
            if (StringUtils.isEmpty(attr))
            {
                continue;
            }
            String attrLower = attr.toLowerCase(Locale.ROOT);
            if (attrLower.contains("accesskey"))
            {
                model.setAttrvalue(StringUtils.desensitizedSecret(model.getAttrvalue()));
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addImageConfigInfo(AddImageConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel        adminModel        = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int               row               = 0;
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(vo.getType() + "");
            if (uploadConfigModelMapper.selectCount(uploadConfigModel) > 0)
            {
                //修改,删除之前的配置
                uploadConfigModelMapper.delete(uploadConfigModel);
            }
            //删除商城默认上传
            uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
            if (uploadConfigModelMapper.selectCount(uploadConfigModel) > 0)
            {
                //修改,删除之前的配置
                uploadConfigModelMapper.delete(uploadConfigModel);
            }
            uploadConfigModel.setType("默认上传方式");
            uploadConfigModel.setAttr("upserver_type");
            uploadConfigModel.setAttrvalue(vo.getType() + "");
            row = uploadConfigModelMapper.insertSelective(uploadConfigModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙");
            }
            //新增
            switch (vo.getType() + "")
            {
                case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST:
                    for (int i = 0; i < 2; i++)
                    {
                        UploadConfigModel uploadConfigModelSave = new UploadConfigModel();
                        uploadConfigModelSave.setType("本地");
                        uploadConfigModelSave.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST);
                        switch (i)
                        {
                            case 0:
                                uploadConfigModelSave.setAttr("uploadImg_domain");
                                if (StringUtils.isEmpty(vo.getUploadImgDomain()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXTPSCYM, "请填写图片上传域名");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getUploadImgDomain());
                                break;
                            case 1:
                                uploadConfigModelSave.setAttr("uploadImg");
                                if (StringUtils.isEmpty(vo.getUploadImg()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXBDCCWZ, "请填写本地存储位置");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getUploadImg());
                                break;
                            default:
                                break;
                        }
                        row = uploadConfigModelMapper.insertSelective(uploadConfigModelSave);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙");
                        }
                    }
                    break;
                case GloabConst.UploadConfigConst.IMG_UPLOAD_OSS:
                    for (int i = 0; i < 7; i++)
                    {
                        UploadConfigModel uploadConfigModelSave = new UploadConfigModel();
                        uploadConfigModelSave.setType("阿里云oos");
                        uploadConfigModelSave.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_OSS);
                        switch (i)
                        {
                            case 0:
                                uploadConfigModelSave.setAttr("Bucket");
                                if (StringUtils.isEmpty(vo.getOssbucket()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXCCKJMC, "请填写存储空间名称");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssbucket());
                                break;
                            case 1:
                                uploadConfigModelSave.setAttr("Endpoint");
                                if (StringUtils.isEmpty(vo.getOssendpoint()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写Endpoint");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssendpoint());
                                break;
                            case 2:
                                uploadConfigModelSave.setAttr("isopenzdy");
                                uploadConfigModelSave.setAttrvalue(vo.getIsOpenDiyDomain() + "");
                                break;
                            case 3:
                                uploadConfigModelSave.setAttr("AccessKeyID");
                                if (StringUtils.isEmpty(vo.getOssaccesskey()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写Access Key ID");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssaccesskey());
                                break;
                            case 4:
                                uploadConfigModelSave.setAttr("AccessKeySecret");
                                if (StringUtils.isEmpty(vo.getOssaccesssecret()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写Access Key Secret");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssaccesssecret());
                                break;
                            case 5:
                                uploadConfigModelSave.setAttr("imagestyle");
                                uploadConfigModelSave.setAttrvalue(vo.getOssimgstyleapi());
                                break;
                            case 6:
                                if (vo.getIsOpenDiyDomain() == 1)
                                {
                                    uploadConfigModelSave.setAttr("MyEndpoint");
                                    if (StringUtils.isEmpty(vo.getMyEndpoint()))
                                    {
                                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写MyEndpoint");
                                    }
                                    uploadConfigModelSave.setAttrvalue(vo.getMyEndpoint());
                                }
                                else
                                {
                                    continue;
                                }
                                break;
                            default:
                                break;
                        }
                        row = uploadConfigModelMapper.insertSelective(uploadConfigModelSave);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙");
                        }
                    }
                    //添加操作日志
                    publiceService.addAdminRecord(vo.getStoreId(), "修改了阿里云配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    break;
                case GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO:
                    for (int i = 0; i < 5; i++)
                    {
                        UploadConfigModel uploadConfigModelSave = new UploadConfigModel();
                        uploadConfigModelSave.setType("MinIO");
                        uploadConfigModelSave.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO);
                        switch (i)
                        {
                            case 0:
                                uploadConfigModelSave.setAttr("Bucket");
                                if (StringUtils.isEmpty(vo.getOssbucket()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXCCKJMC, "请填写存储空间名称");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssbucket());
                                break;
                            case 1:
                                uploadConfigModelSave.setAttr("Endpoint");
                                if (StringUtils.isEmpty(vo.getOssendpoint()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写Endpoint");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssendpoint());
                                break;
                            case 2:
                                uploadConfigModelSave.setAttr("AccessKeyID");
                                if (StringUtils.isEmpty(vo.getOssaccesskey()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写Access Key ID");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssaccesskey());
                                break;
                            case 3:
                                uploadConfigModelSave.setAttr("AccessKeySecret");
                                if (StringUtils.isEmpty(vo.getOssaccesssecret()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写Access Key Secret");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getOssaccesssecret());
                                break;
                            case 4:
                                uploadConfigModelSave.setAttr("serveruri");
                                if (StringUtils.isEmpty(vo.getServeruri()))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTX, "请填写Minio Server URI ");
                                }
                                uploadConfigModelSave.setAttrvalue(vo.getServeruri());
                                break;
                            default:
                                break;
                        }
                        row = uploadConfigModelMapper.insertSelective(uploadConfigModelSave);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙");
                        }
                    }
                    //添加操作日志
                    publiceService.addAdminRecord(vo.getStoreId(), "修改了Minio配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            configModelMapper.updateConfigAll(vo.getType());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑图片上传配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addImageConfigInfo");
        }
    }

    @Autowired
    private ConfigModelMapper configModelMapper;
}

