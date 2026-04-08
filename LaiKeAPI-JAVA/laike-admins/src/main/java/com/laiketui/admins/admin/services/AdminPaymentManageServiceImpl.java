package com.laiketui.admins.admin.services;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.AdminPaymentManageService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.PaymentConfigModelMapper;
import com.laiketui.common.mapper.PaymentModelMapper;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.payment.PaymentConfigModel;
import com.laiketui.domain.payment.PaymentModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import com.laiketui.root.common.BuilderIDTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 支付管理
 *
 * @author Trick
 * @date 2021/7/15 15:22
 */
@Service
@RefreshScope
public class AdminPaymentManageServiceImpl implements AdminPaymentManageService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${node.wx-certp12-path}")
    private String certBasePath;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());

            List<Map<String, Object>> list  = paymentModelMapper.getPaymentConfigInfo(parmaMap);
            int                       total = paymentModelMapper.countPaymentConfigInfo(parmaMap);

            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品类别以及品牌信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> paymentParmaInfo(MainVo vo, int payId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //支付宝回调
            String notifyUrl = "";
            int    status    = 0;

            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
            paymentConfigModel.setPid(payId);
            paymentConfigModel.setStore_id(vo.getStoreId());
            paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);

            if (paymentConfigModel != null || payId == 3)
            {
                Map<String, Object> parma = new HashMap<>(16);
                if (paymentConfigModel != null)
                {
                    status = paymentConfigModel.getStatus();
                    try
                    {
                        parma = JSON.parseObject(paymentConfigModel.getConfig_data(), new TypeReference<Map<String, Object>>()
                        {
                        });
                    }
                    catch (Exception e1)
                    {
                        logger.debug("非json数据,正在尝试反php序列化");
                        try
                        {
                            parma = DataUtils.cast(SerializePhpUtils.getUnserializeObj(paymentConfigModel.getConfig_data(), Map.class));
                        }
                        catch (Exception e2)
                        {
                            logger.debug("反php序列化失败");
                            parma = new HashMap<>(16);
                        }
                    }
                    if (parma == null)
                    {
                        parma = new HashMap<>(16);
                    }
                }
                maskPaymentConfig(parma);
                parma.put("status", status);
                resultMap.put("config", parma);

            }
            resultMap.put("mrnotify_url", notifyUrl);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取支付参数 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "paymentParmaInfo");
        }
        return resultMap;
    }

    @SuppressWarnings("unchecked")
    private void maskPaymentConfig(Map<String, Object> config) throws LaiKeAPIException
    {
        if (config == null || config.isEmpty())
        {
            return;
        }
        for (Map.Entry<String, Object> entry : config.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map)
            {
                maskPaymentConfig((Map<String, Object>) value);
                continue;
            }
            if (value instanceof List)
            {
                maskPaymentConfigList((List<Object>) value, key);
                continue;
            }
            if (value instanceof String && isSensitivePayConfigKey(key))
            {
                entry.setValue(StringUtils.desensitizedSecret((String) value));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void maskPaymentConfigList(List<Object> list, String parentKey) throws LaiKeAPIException
    {
        if (list == null || list.isEmpty())
        {
            return;
        }
        for (int i = 0; i < list.size(); i++)
        {
            Object item = list.get(i);
            if (item instanceof Map)
            {
                maskPaymentConfig((Map<String, Object>) item);
                continue;
            }
            if (item instanceof String && isSensitivePayConfigKey(parentKey))
            {
                list.set(i, StringUtils.desensitizedSecret((String) item));
            }
        }
    }

    private boolean isSensitivePayConfigKey(String key)
    {
        if (StringUtils.isEmpty(key))
        {
            return false;
        }
        String lower = key.toLowerCase(Locale.ROOT);
        return lower.contains("key")
                || lower.contains("secret")
                || lower.contains("private")
                || lower.contains("cert")
                || lower.contains("pem")
                || lower.contains("password")
                || lower.contains("token");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPaymentParma(MainVo vo, String json, Integer id, Integer status) throws LaiKeAPIException
    {
        try
        {
            int        row;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            PaymentConfigModel paymentConfigOld  = null;
            PaymentConfigModel paymentConfigSave = new PaymentConfigModel();
            if (id != null)
            {
                paymentConfigOld = new PaymentConfigModel();
                paymentConfigOld.setStore_id(vo.getStoreId());
                paymentConfigOld.setPid(id);
                paymentConfigOld = paymentConfigModelMapper.selectOne(paymentConfigOld);
            }
            paymentConfigSave.setStatus(status);
            paymentConfigSave.setConfig_data(json);

            if (paymentConfigOld == null)
            {
                paymentConfigSave.setStore_id(vo.getStoreId());
                row = paymentConfigModelMapper.insertSelective(paymentConfigSave);
            }
            else
            {
                paymentConfigSave.setId(paymentConfigOld.getId());
                row = paymentConfigModelMapper.updateByPrimaryKeySelective(paymentConfigSave);
            }
            PaymentModel paymentModel = paymentModelMapper.selectByPrimaryKey(id);
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了" + paymentModel.getName() + "的参数信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZSB, "配置失败");
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("支付参数修改 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setPaymentParma");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPaymentSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int row;
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
            paymentConfigModel.setStore_id(vo.getStoreId());
            paymentConfigModel.setPid(id);
            paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);
            if (paymentConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBCZ, "支付方式不存在");
            }
            int isOpen = 1;
            if (paymentConfigModel.getStatus() == isOpen)
            {
                isOpen = 0;
            }
            PaymentConfigModel paymentConfigUpdate = new PaymentConfigModel();
            paymentConfigUpdate.setId(paymentConfigModel.getId());
            paymentConfigUpdate.setStatus(isOpen);
            //开关参数 是否显示 0否 1是
            row = paymentConfigModelMapper.updateByPrimaryKeySelective(paymentConfigUpdate);
            if (row < 1)
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
            logger.error("支付配置开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setPaymentSwitch");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settingDefaultPaytype(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int row;
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
            paymentConfigModel.setStore_id(vo.getStoreId());
            paymentConfigModel.setPid(id);
            paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);
            if (paymentConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBCZ, "支付方式不存在");
            }

            paymentConfigModelMapper.allisdefaultpay(vo.getStoreId());
            //开关参数 是否显示 0否 1是
            row = paymentConfigModelMapper.oneIsdefaultpay(id, vo.getStoreId());

            if (row < 1)
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
            logger.error("支付配置默认支付方式 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setPaymentSwitch");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadCertP12(UploadFileVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            //上传certp12文件保存位置
            StringBuilder saveStorePath = new StringBuilder(certBasePath).append(SplitUtils.FXG).append(vo.getStoreId()).append(SplitUtils.FXG);
            //File saveStoreFile = new File(saveStorePath.toString());

            /*if (!saveStoreFile.exists()) {
                saveStoreFile.mkdir();
            }*/

            saveStorePath = saveStorePath.append(SplitUtils.FXG).append(BuilderIDTool.getGuid());
            /*File saveFile = new File(saveStorePath.toString());
            if (!saveFile.exists()) {

            }*/
            FileUtil.mkdir(String.valueOf(saveStorePath));


            File p12 = new File(saveStorePath.append(SplitUtils.FXG).append("apiclient_cert.p12").toString());
            for (MultipartFile file : vo.getImage())
            {
                file.transferTo(p12);
            }
            if (SystemUtil.getOsInfo().isLinux())
            {
                Files.setPosixFilePermissions(Paths.get(p12.getPath()), PosixFilePermissions.fromString("rwxr-xr-x"));
            }
            resultMap.put("savePath", p12.getPath());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("文件上传异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImage");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPayment(MainVo vo, Integer id, String img, String remark) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误id为空", "id");
            }
            PaymentModel paymentModel = paymentModelMapper.selectByPrimaryKey(id);
            if (StringUtils.isEmpty(paymentModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "支付方式不存在");
            }
            paymentModel.setLogo(img);
            paymentModel.setDescription(remark);
            int i = paymentModelMapper.updateByPrimaryKeySelective(paymentModel);

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了" + paymentModel.getName() + "的配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "辑支付类型信息失败", "uploadImage");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("辑支付类型信息", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImage");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String setPaymentLoge(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException
    {
        try
        {
            List<String> imageUrlList = publiceService.uploadImage(image, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, vo.getStoreType(), vo.getStoreId());
            String       appImagImage = ImgUploadUtils.getUrlPure(imageUrlList.get(0), true);
            logger.debug("非小程序则系统生成二维码,路径{}", appImagImage);
//           return ImgUploadUtils.getPathImgByName(appImagImage);
            return appImagImage;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑支付类型图标上传错误", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImage");
        }
    }
}
