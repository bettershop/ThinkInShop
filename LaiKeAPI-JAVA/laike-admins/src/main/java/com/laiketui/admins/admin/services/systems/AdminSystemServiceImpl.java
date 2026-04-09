package com.laiketui.admins.admin.services.systems;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;
import com.laiketui.admins.api.admin.AdminGoodsDubboService;
import com.laiketui.admins.api.admin.saas.AdminCurrencyStoreService;
import com.laiketui.admins.api.admin.systems.AdminSystemService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.config.Config;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.config.*;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.systems.SystemConfigurationModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelAnalysisVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import com.laiketui.domain.vo.mch.ApplyShopVo;
import com.laiketui.domain.vo.systems.*;
import com.laiketui.root.license.LicenseUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;

import static com.laiketui.core.lktconst.DictionaryConst.WhetherMaven.WHETHER_NO;

/**
 * 系统配置
 *
 * @author Trick
 * @date 2021/1/15 9:28
 */
@Service
public class AdminSystemServiceImpl implements AdminSystemService
{
    private final Logger logger = LoggerFactory.getLogger(AdminSystemServiceImpl.class);

    @Autowired
    private SystemConfigurationModelMapper systemConfigurationModelMapper;

    @Autowired
    private ThirdModelMapper thirdModelMapper;

    @Autowired
    private HotKeywordsModelMapper hotKeywordsModelMapper;

    @Autowired
    private PrintSetupModelMapper printSetupModelMapper;

    @Autowired
    private SensitiveWordsModelMapper sensitiveWordsModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LicenseUtils licenseUtils;

    @Autowired
    private Config config;

    private static final String LICENSE_KEY = "license_key:";


    @Override
    public Map<String, Object> getSystemIndex(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);

            if (configModel != null)
            {
                configModel.setLogo(publiceService.getImgPath(configModel.getLogo(), vo.getStoreId()));
                configModel.setLogo1(publiceService.getImgPath(configModel.getLogo1(), vo.getStoreId()));
                configModel.setWx_headimgurl(publiceService.getImgPath(configModel.getWx_headimgurl(), vo.getStoreId()));
                if (configModel.getPush_Appid() == null)
                {
                    configModel.setPush_Appid("");
                }
                if (configModel.getPush_Appkey() == null)
                {
                    configModel.setPush_Appkey("");
                }
                if (configModel.getPush_MasterECRET() == null)
                {
                    configModel.setPush_MasterECRET("");
                }
            }
            HotKeywordsModel hotKeywordsModel = new HotKeywordsModel();
            hotKeywordsModel.setStore_id(vo.getStoreId());
            hotKeywordsModel = hotKeywordsModelMapper.selectOne(hotKeywordsModel);
            //订单打印配置
            PrintSetupModel printSetupModel = new PrintSetupModel();
            printSetupModel.setStoreId(vo.getStoreId());
            //管理后台店铺id为0
            printSetupModel.setMchId(0);
            printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
            if (printSetupModel == null)
            {
                printSetupModel = new PrintSetupModel();
            }
            resultMap.put("data", configModel);
            resultMap.put("hotKeywordsConfig", hotKeywordsModel);
            resultMap.put("printSetupConfig", printSetupModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取系统基本配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSystemIndex");
        }
        return resultMap;
    }

    @Override
    public boolean addSystemConfig(AddSystemVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel  user        = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int         count;
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);

            ConfigModel configModelSave = new ConfigModel();
            configModelSave.setIs_register(StringUtils.toString(vo.getIsRegister()));
            //没有这三个配置 禅道53268 【JAVA开发环境】管理后台（用户管理）：用户设置。头像经常丢失
/*            configModelSave.setLogo(vo.getLogoUrl());
            configModelSave.setLogo1(vo.getWxHeader());
            configModelSave.setWx_headimgurl(vo.getWxHeader());*/
            configModelSave.setH5_domain(vo.getPageDomain());
            configModelSave.setMessage_day(vo.getMessageSaveDay());
            configModelSave.setExp_time(vo.getAppLoginValid());
            configModelSave.setCustomer_service(vo.getServerClient());
            configModelSave.setTencent_key(vo.getTencentKey());
            //默认开启，前端开关按钮隐藏了
            configModelSave.setIs_push(DictionaryConst.WhetherMaven.WHETHER_OK);
            configModelSave.setPush_Appkey(vo.getPushAppkey());
            configModelSave.setPush_Appid(vo.getPushAppid());
            configModelSave.setPush_MasterECRET(vo.getPushMasterEcret());
            configModelSave.setIs_Kicking(vo.getIsKicking());
            configModelSave.setIs_express(vo.getIsExpress());
            configModelSave.setExpress_key(vo.getExpressKey());
            configModelSave.setExpress_number(vo.getExpressNumber());
            configModelSave.setExpress_address(vo.getExpressAddress());

            configModelSave.setWatermark_name(vo.getWatermarkName());
            configModelSave.setWatermark_url(vo.getWatermarkUrl());
            configModelSave.setPc_mch_path(vo.getPcMchPath());
            configModelSave.setExpress_secret(vo.getExpress_secret());
            configModelSave.setExpress_tempId(vo.getExpress_tempId());
            //分账设置
            configModelSave.setIsAccounts(vo.getIsAccounts());
            if (vo.getIsAccounts() == 1 && "".equals(vo.getAccountsSet()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "开启分账必须填写分账账号", "configModelSave");
            }
            configModelSave.setAccountsSet(vo.getAccountsSet());
            //校验数据
            configModelSave = DataCheckTool.checkConfigDataFormate(configModelSave);

            if (configModel != null)
            {
                //pc店铺默认地址如有修改则需要同步这个商城底下所有店铺的pc端地址
                if (StringUtils.isNotEmpty(configModel.getPc_mch_path()) && !configModel.getPc_mch_path().equals(vo.getPcMchPath()))
                {
                    synchronizationPcMchPath(vo.getStoreId(), vo.getPcMchPath());
                }
                configModelSave.setIs_register(vo.getIsRegister().toString());
                configModelSave.setId(configModel.getId());
                count = configModelMapper.updateByPrimaryKeySelective(configModelSave);
            }
            else
            {
                configModelSave.setStore_id(vo.getStoreId());
                configModelSave.setIs_register(vo.getIsRegister().toString());
                count = configModelMapper.insertSelective(configModelSave);
            }

            //新增/修改搜索配置
            boolean checkSearch = addOrUpdateSearchConfig(vo);
            if (!checkSearch)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
            }

            //订单打印配置
            boolean flag = addOrUpdatePrint(vo);
            if (!flag)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了基础配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑系统配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
        }
    }

    @Override
    public Map<String, Object> getSetSystem(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                configModel.setLogo(configModel.getLogon_logo());
            }
            resultMap.put("config",configModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取系统配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSetSystem");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setSystem(SetSystemVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        row;
            if (StringUtils.isEmpty(vo.getLogoUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DLYBNWK, "登录页logo不能为空");
            }
            if (StringUtils.isEmpty(vo.getCopyrightInformation()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BQXXBNWK, "版权信息不能为空");
            }
            if (StringUtils.isEmpty(vo.getRecordInformation()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BAXXBNWK, "备案信息不能为空");
            }
            /*if (StringUtils.isEmpty(vo.getH5Domain())) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BAXXBNWK, "H5地址不能为空");
            }
            if (StringUtils.isEmpty(vo.getDomainPath())) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BAXXBNWK, "根路径不能为空");
            }
            if (StringUtils.isEmpty(vo.getStoreIdPrefix())){
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "商城ID规则设置不能为空");
            }*/
            if (StringUtils.isEmpty(vo.getAdminDefaultPortrait()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "管理员默认头像不能为空");
            }
            SystemConfigurationModel systemConfigurationOld = null;
            if (vo.getId() != null)
            {
                systemConfigurationOld = systemConfigurationModelMapper.selectByPrimaryKey(vo.getId());
            }
            SystemConfigurationModel systemConfigurationSave = new SystemConfigurationModel();
            systemConfigurationSave.setLogo(vo.getLogoUrl());
            systemConfigurationSave.setCopyright_information(vo.getCopyrightInformation());
            systemConfigurationSave.setRecord_information(vo.getRecordInformation());
            //systemConfigurationSave.setStore_id_prefix(vo.getStoreIdPrefix());
            systemConfigurationSave.setAdminDefaultPortrait(vo.getAdminDefaultPortrait());
            //解码
            vo.setLinkPageJson(URLDecoder.decode(vo.getLinkPageJson(), GloabConst.Chartset.UTF_8));
            if (StringUtils.isNotEmpty(vo.getLinkPageJson()))
            {
                try
                {
                    com.alibaba.fastjson2.JSON.parseObject(vo.getLinkPageJson(), new com.alibaba.fastjson2.TypeReference<List<Map<String, Object>>>()
                    {
                    });
                }
                catch (Exception e)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DLLJCSCW, "登录链接参数错误");
                }
                systemConfigurationSave.setLink_to_landing_page(vo.getLinkPageJson());
            }
            if (systemConfigurationOld == null)
            {
                systemConfigurationSave.setStore_id(AdminModel.PLATFORM_STORE_ID);
                systemConfigurationSave.setAdd_time(new Date());
                row = systemConfigurationModelMapper.insertSelective(systemConfigurationSave);
            }
            else
            {
                systemConfigurationSave.setId(systemConfigurationOld.getId());
                row = systemConfigurationModelMapper.updateByPrimaryKeySelective(systemConfigurationSave);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了系统设置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZSB, "配置失败");
            }

            /*ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null) {
                ConfigModel configModelUpdate = new ConfigModel();
                configModelUpdate.setId(configModel.getId());
                configModelUpdate.setH5_domain(vo.getH5Domain());
                configModelUpdate.setDomain(vo.getDomainPath());
                row = configModelMapper.updateByPrimaryKeySelective(configModelUpdate);
                if (row < 1) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZSB, "配置失败");
                }
            }else {
                configModel = new ConfigModel();
                configModel.setStore_id(vo.getStoreId());
                configModel.setId(configModel.getId());
                configModel.setH5_domain(vo.getH5Domain());
                configModel.setDomain(vo.getDomainPath());
                row = configModelMapper.insertSelective(configModel);
                if (row < 1) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZSB, "配置失败");
                }
            }
            //https://java.houjiemeishi.com/# 禅道：43023
            row = thirdModelMapper.updateWorkDomain(vo.getH5Domain());
            if (row < 1){
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZSB, "修改后地址配置失败");
            }*/
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("系统配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setSystem");
        }
    }

    @Override
    public Map<String, Object> getAgreementIndex(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AgreementModel agreementModel = new AgreementModel();
            if (id != null && id > 0)
            {
                agreementModel.setId(id);
            }
            agreementModel.setStore_id(vo.getStoreId());
            List<AgreementModel> agreementModelList = agreementModelMapper.select(agreementModel);

            resultMap.put("list", agreementModelList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取协议列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAgreementIndex");
        }
        return resultMap;
    }

    @Override
    public void addAgreement(MainVo vo, Integer id, String title, int type, String content) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            if (StringUtils.isEmpty(title))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XYBTBNWK, "协议标题不能为空");
            }
            if (StringUtils.isEmpty(content))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NRBNWK, "内容不能为空");
            }
            AgreementModel agreementModel = null;
            if (id != null)
            {
                agreementModel = agreementModelMapper.selectByPrimaryKey(id);
            }

            AgreementModel agreementModelSave = new AgreementModel();
            agreementModelSave.setName(title);
            agreementModelSave.setType(type);
            agreementModelSave.setContent(content);
            agreementModelSave.setModify_date(new Date());
            String typeName = "";
            //类型 0:注册 1:店铺 2.隐私 3.会员 4.供应商
            if (type == 0)
            {
                typeName = "注册";
            }
            else if (type == 1)
            {
                typeName = "店铺";
            }
            else if (type == 2)
            {
                typeName = "隐私";
            }
            else if (type == 3)
            {
                typeName = "会员";
            }
            else if (type == 4)
            {
                typeName = "供应商";
            }
            if (agreementModel != null)
            {
                agreementModelSave.setId(agreementModel.getId());
                count = agreementModelMapper.updateByPrimaryKeySelective(agreementModelSave);

                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了" + typeName + "协议信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());

            }
            else
            {
                AgreementModel agreementCount = new AgreementModel();
                agreementCount.setStore_id(vo.getStoreId());
                agreementCount.setType(type);
                if (agreementModelMapper.selectCount(agreementCount) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XYYCZ, "协议已存在");
                }
                agreementModelSave.setStore_id(vo.getStoreId());
                count = agreementModelMapper.insertSelective(agreementModelSave);

                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了" + typeName + "协议", AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            if (count < 1)
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
            logger.error("添加/编辑协议 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAgreement");
        }
    }

    @Override
    public void delAgreement(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel     user           = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            AgreementModel agreementModel = agreementModelMapper.selectByPrimaryKey(id);
            if (agreementModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XYBCZ, "协议不存在");
            }
            if (agreementModelMapper.deleteByPrimaryKey(id) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //添加操作日志
            String typeName = "";
            //类型 0:注册 1:店铺 2.隐私 3.会员 4.供应商
            if (agreementModel.getType() == 0)
            {
                typeName = "注册";
            }
            else if (agreementModel.getType() == 1)
            {
                typeName = "店铺";
            }
            else if (agreementModel.getType() == 2)
            {
                typeName = "隐私";
            }
            else if (agreementModel.getType() == 3)
            {
                typeName = "会员";
            }
            else if (agreementModel.getType() == 4)
            {
                typeName = "供应商";
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了" + typeName + "协议", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除协议 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delAgreement");
        }
    }

    @Override
    public boolean updateCommonProblem(MainVo vo, String returnProblem, String payProblem) throws LaiKeAPIException
    {
        try
        {
            int count;
            if (StringUtils.isEmpty(returnProblem))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHWTBNWK, "售后问题不能为空");
            }
            if (StringUtils.isEmpty(payProblem))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFWTBNWK, "支付问题不能为空");
            }
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXSZJCPZ, "请先设置基础配置");
            }
            ConfigModel configModelUpdate = new ConfigModel();
            configModelUpdate.setId(configModel.getId());
            configModelUpdate.setPayment_issues(payProblem);
            configModelUpdate.setAfter_sales_issues(returnProblem);
            count = configModelMapper.updateByPrimaryKeySelective(configModelUpdate);

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("修改常简问题 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateCommonProblem");
        }
    }

    @Override
    public boolean updateRefundService(MainVo vo, String refundPolicy, String cancelOrderno, String refundMoney, String refundExplain) throws LaiKeAPIException
    {
        try
        {
            int count;
            if (StringUtils.isEmpty(refundPolicy))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_THZCBNWK, "退货政策不能为空");
            }
            if (StringUtils.isEmpty(cancelOrderno))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXDDBNWK, "取消订单不能为空");
            }
            if (StringUtils.isEmpty(refundMoney))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKLCBNWK, "退款流程不能为空");
            }
            if (StringUtils.isEmpty(refundExplain))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSMBNWK, "退款说明不能为空");
            }
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXSZJCPZ, "请先设置基础配置");
            }
            ConfigModel configModelUpdate = new ConfigModel();
            configModelUpdate.setId(configModel.getId());
            configModelUpdate.setReturn_policy(refundPolicy);
            configModelUpdate.setRefund_process(refundMoney);
            configModelUpdate.setCancellation_order(cancelOrderno);
            configModelUpdate.setRefund_instructions(refundExplain);
            count = configModelMapper.updateByPrimaryKeySelective(configModelUpdate);

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("修改常简问题 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateCommonProblem");
        }
    }

    @Override
    public boolean updateBeginnerGuide(MainVo vo, String shoppingProcess, String payType) throws LaiKeAPIException
    {
        try
        {
            int count;
            if (StringUtils.isEmpty(shoppingProcess))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GWLCBNWK, "购物流程不能为空");
            }
            if (StringUtils.isEmpty(payType))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBNWK, "支付方式不能为空");
            }
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXSZJCPZ, "请先设置基础配置");
            }
            ConfigModel configModelUpdate = new ConfigModel();
            configModelUpdate.setId(configModel.getId());
            configModelUpdate.setPayment_method(payType);
            configModelUpdate.setShopping_process(shoppingProcess);
            count = configModelMapper.updateByPrimaryKeySelective(configModelUpdate);

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("修改常简问题 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateCommonProblem");
        }
    }

    @Override
    public boolean updateAboutMe(MainVo vo, String auboutMe) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            if (StringUtils.isEmpty(auboutMe))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NRBNWK, "内容不能为空");
            }
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXSZJCPZ, "请先设置基础配置");
            }
            ConfigModel configModelUpdate = new ConfigModel();
            configModelUpdate.setId(configModel.getId());
            configModelUpdate.setAboutus(auboutMe);
            count = configModelMapper.updateByPrimaryKeySelective(configModelUpdate);

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了关于我们的内容信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改关于我 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateAboutMe");
        }
    }

    @Override
    public boolean uploadImages(MainVo vo, List<MultipartFile> files) throws LaiKeAPIException
    {
        try
        {
            if (files == null || files.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBNWK, "图片不能为空");
            }
            //上传图片
            List<String> imagUrls = publiceService.uploadImage(files, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, vo.getStoreType(), vo.getStoreId());

            logger.debug("图片上传成功,上传{}张,成功{}张", files.size(), imagUrls.size());
            return imagUrls.size() > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("图片上传 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImages");
        }
    }

    @Override
    public boolean updateWeiXinApi(MainVo vo, String appid, String appsecret) throws LaiKeAPIException
    {
        try
        {
            int count;
            if (StringUtils.isEmpty(appid))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XCXBNWK, "小程序appid不能为空");
            }
            if (StringUtils.isEmpty(appsecret))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XCXMYBNWK, "小程序密钥不能为空");
            }
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXSZJCPZ, "请先设置基础配置");
            }

            ConfigModel configModelUpdate = new ConfigModel();
            configModelUpdate.setAppid(appid);
            configModelUpdate.setAppsecret(appsecret);
            count = configModelMapper.updateByPrimaryKeySelective(configModelUpdate);

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("微信小程序配置 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadWeiXinApi");
        }
    }

    @Override
    public Map<String, Object>  getFrontConfig(MainVo vo, Integer type)
    {
        Map<String, Object> resultMap    = new HashMap<>(16);
        SystemInfoVo        systemInfoVo = new SystemInfoVo();
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(configModel))
            {
                switch (type)
                {
                    case 1:
                        //1：基础信息配置
                        systemInfoVo.setMessageSaveDay(configModel.getMessage_day());
                        systemInfoVo.setLogon_logo(configModel.getLogon_logo());
                        systemInfoVo.setAppLoginValid(configModel.getExp_time());
                        systemInfoVo.setCopyright_information(configModel.getCopyright_information());
                        systemInfoVo.setRecord_information(configModel.getRecord_information());
                        systemInfoVo.setLink_to_landing_page(configModel.getLink_to_landing_page());
                        systemInfoVo.setAdmin_default_portrait(configModel.getAdmin_default_portrait());
                        systemInfoVo.setWatermark_url(configModel.getWatermark_url());
                        systemInfoVo.setWatermark_name(configModel.getWatermark_name());
                        //h5地址 支付后跳转使用
                        systemInfoVo.setH5_domain(configModel.getH5_domain());
                        systemInfoVo.setApp_logo(configModel.getApp_logo());
                        systemInfoVo.setHtml_icon(configModel.getHtml_icon());
                        systemInfoVo.setStore_name(configModel.getStore_name());

                        CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(vo.getStoreId());
                        systemInfoVo.setStore_logo(customerModel.getMerchant_logo());

                        break;
                    case 2:
                        //2：快递100配置
                        systemInfoVo.setExpressAddress(configModel.getExpress_address());
                        systemInfoVo.setExpressKey(configModel.getExpress_key());
                        systemInfoVo.setExpress_secret(configModel.getExpress_secret());
                        systemInfoVo.setExpress_tempId(configModel.getExpress_tempId());
                        systemInfoVo.setExpressNumber(configModel.getExpress_number());
                        systemInfoVo.setSiid(StringUtils.desensitizedSecret(configModel.getSiid()));
                        systemInfoVo.setCloud_notify(configModel.getCloud_notify());
                        systemInfoVo.setIs_open_cloud(configModel.getIs_open_cloud());
                        systemInfoVo.setTrack_secret(StringUtils.desensitizedSecret(configModel.getTrack_secret()));
                        //订单打印配置
                        PrintSetupModel printSetupModel = new PrintSetupModel();
                        printSetupModel.setStoreId(vo.getStoreId());
                        printSetupModel.setMchId(0);
                        printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
                        if (Objects.nonNull(printSetupModel))
                        {
                            systemInfoVo.setPrintName(printSetupModel.getPrintName());
                            systemInfoVo.setPrintUrl(printSetupModel.getPrintUrl());
                            systemInfoVo.setSheng(printSetupModel.getSheng());
                            systemInfoVo.setShi(printSetupModel.getShi());
                            systemInfoVo.setXian(printSetupModel.getXian());
                            systemInfoVo.setAddress(printSetupModel.getAddress());
                            systemInfoVo.setPhone(printSetupModel.getPhone());
                        }
                        break;
                    case 3:
                        //敏感词
                        HotKeywordsModel hotKeywordsModel = new HotKeywordsModel();
                        hotKeywordsModel.setStore_id(vo.getStoreId());
                        hotKeywordsModel = hotKeywordsModelMapper.selectOne(hotKeywordsModel);
                        if (Objects.nonNull(hotKeywordsModel))
                        {
                            resultMap.put("list", hotKeywordsModel);
                            return resultMap;
                        }
                        break;
                    case 4:
                        //国际化配置
                        //h5地址 支付后跳转使用
                        systemInfoVo.setDefault_lang_code(configModel.getDefault_lang_code());
                        break;
                    case 5:
                        //分账
                        systemInfoVo.setIs_accounts(Objects.isNull(configModel.getIsAccounts()) ? 0 : configModel.getIsAccounts());
                        systemInfoVo.setAccounts_set(configModel.getAccountsSet());
                        break;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取系统基本配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getFrontMsgAndLoginConfig");
        }
        resultMap.put("list", systemInfoVo);
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateFrontConfig(FrontConfigVo vo)
    {
        ConfigModel configModel = new ConfigModel();
        configModel.setStore_id(vo.getStoreId());
        ConfigModel configModelSave = new ConfigModel();
        int         count           = 0;
        try
        {
            initConfig(configModelSave, vo);
            ConfigModel model = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(model))
            {
                configModelSave.setId(model.getId());
                configModelSave.setH5_domain(vo.getH_Address());
                count = configModelMapper.updateByPrimaryKeySelective(configModelSave);
            }
            else
            {
                count = configModelMapper.insertSelective(configModelSave);
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
            }

            if (StringUtils.isNotEmpty(vo.getStore_logo()))
            {
                customerModelMapper.updateLogo(vo.getStore_logo(),vo.getStoreId());
            }

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了基础配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑系统配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addOrUpdateFrontConfig");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateStoreIntenationSetting(FrontConfigVo vo)
    {
        ConfigModel configModel = new ConfigModel();
        configModel.setStore_id(vo.getStoreId());
        ConfigModel configModelSave = new ConfigModel();
        configModelSave.setDefault_lang_code(vo.getDefault_lang_code());
        int count = 0;
        try
        {
            ConfigModel model = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(model))
            {
                configModelSave.setId(model.getId());
                count = configModelMapper.updateByPrimaryKeySelective(configModelSave);
            }
            else
            {
                count = configModelMapper.insertSelective(configModelSave);
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了国际化配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑系统配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addOrUpdateFrontConfig");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void LogisticsAndPrinting(AddSystemVo vo)
    {
        ConfigModel configModel = new ConfigModel();
        configModel.setStore_id(vo.getStoreId());
        int         count           = 0;
        ConfigModel configModelSave = new ConfigModel();
        try
        {
            configModelSave.setExpress_address(vo.getExpressAddress());
            configModelSave.setExpress_key(vo.getExpressKey());
            configModelSave.setExpress_number(vo.getExpressNumber());
            configModelSave.setExpress_secret(vo.getExpress_secret());
            configModelSave.setExpress_tempId(vo.getExpress_tempId());
            configModelSave.setSiid(vo.getSiid());
            configModelSave.setCloud_notify(vo.getCloud_notify());
            configModelSave.setIs_open_cloud(vo.getIs_open_cloud());
            configModelSave.setTrack_secret(vo.getTrack_secret());
            ConfigModel model = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(model))
            {
                configModelSave.setId(model.getId());
                count = configModelMapper.updateByPrimaryKeySelective(configModelSave);
            }
            else
            {
                count = configModelMapper.insertSelective(configModelSave);
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
            }
            //物流及打印配置修改
            boolean flag = addOrUpdatePrint(vo);
            if (!flag)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了基础配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑系统配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "LogisticsAndPrinting");
        }
    }

    @Override
    public void searchAndSensitiveWords(AddSystemVo vo)
    {
        boolean flag = addOrUpdateSearchConfig(vo);
        if (!flag)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSystemConfig");
        }
    }

    @Override
    public Map<String, Object> selectSensitive(SensitiveVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNotEmpty(vo.getWord()))
            {
                paramMap.put("keyword", vo.getWord());
            }
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("recycle", WHETHER_NO);

            paramMap.put("pageNo", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());

            List<SensitiveVo> list = sensitiveWordsModelMapper.selectList(paramMap);
            resultMap.put("list", list);

            Integer total = sensitiveWordsModelMapper.count(paramMap);
            resultMap.put("total", Objects.nonNull(total) ? total : 0);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取敏感词列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "selectSensitive");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSensitive(SensitiveVo vo) throws LaiKeAPIException
    {
        try
        {
            SensitiveWordsModel addModel = new SensitiveWordsModel();
            addModel.setStoreId(vo.getStoreId());
            addModel.setWord(vo.getWord());

            int count = sensitiveWordsModelMapper.check(vo.getStoreId(), vo.getWord(), vo.getId());
            if (count > 0) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YMGC, "<UNK>", "addSensitive");
            }

            if (Objects.nonNull(vo.getId()))
            {
                addModel.setId(vo.getId());
                addModel = sensitiveWordsModelMapper.selectByPrimaryKey(vo.getId());
                if (Objects.isNull(addModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
                }
                addModel.setWord(vo.getWord());
                if (sensitiveWordsModelMapper.updateByPrimaryKeySelective(addModel) < 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "编辑敏感词失败");
                }
                return;
            }
            addModel.setAddTime(new Date());
            if (sensitiveWordsModelMapper.insertSelective(addModel) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "添加敏感词失败");
            }
            redisUtil.del(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS + vo.getStoreId());

            publiceService.addAdminRecord(vo.getStoreId(), "进行了敏感词操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加敏感词失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSensitive");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSensitive(SensitiveVo vo) throws LaiKeAPIException
    {
        List<String> idList  = Arrays.asList(vo.getIds().split(SplitUtils.DH));
        int          storeId = vo.getStoreId();
        try
        {
            sensitiveWordsModelMapper.delBatchByIds(idList, storeId);
            redisUtil.del(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS + vo.getStoreId());
            publiceService.addAdminRecord(vo.getStoreId(), "进行了删除敏感词操作", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除敏感词 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deleteSensitive");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSensitives(MainVo vo, MultipartFile[] files) throws LaiKeAPIException
    {
        try
        {
            ExcelAnalysisVo     excelAnalysisVo = new ExcelAnalysisVo();
            List<MultipartFile> multipartFiles  = DataUtils.convertToList(files);
            excelAnalysisVo.setFile(multipartFiles);

            //标题
            String[]     fileTitle  = new String[]{"敏感词"};
            List<String> titleNames = new ArrayList<>(Arrays.asList(fileTitle));
            excelAnalysisVo.setTitleName(titleNames);

            //内容
            String[]     dataTitle  = new String[]{"word"};
            List<String> valueNames = new ArrayList<>(Arrays.asList(dataTitle));
            excelAnalysisVo.setValueKey(valueNames);

            Map<String, Object>       resultMap     = EasyPoiExcelUtil.analysisExcel(excelAnalysisVo);
            List<Map<String, Object>> excelDataList = DataUtils.cast(resultMap.get("list"));
            List<String>              wordlist      = new ArrayList<>();
            if (excelDataList != null)
            {
                for (Map<String, Object> map : excelDataList)
                {
                    String word = MapUtils.getString(map, "word", null);
                    if (word == null || word.isEmpty())
                    {
                        continue;
                    }
                    wordlist.add(word);
                }
            }

            if (CollectionUtils.isNotEmpty(wordlist))
            {
                List<String> oldWordList = sensitiveWordsModelMapper.getWordList(vo.getStoreId());

                //去除重复敏感词
                List<String> newWords = wordlist.stream()
                        .filter(word -> !oldWordList.contains(word))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(newWords))
                {
                    sensitiveWordsModelMapper.insertBatch(vo.getStoreId(), new Date(), newWords);
                }
            }
            redisUtil.del(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS + vo.getStoreId());
            publiceService.addAdminRecord(vo.getStoreId(), "进行了批量导入敏感词操作", AdminRecordModel.Type.ADD, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量导入敏感词 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "importSensitives");
        }
    }

    @Override
    public void getDictionaryCatalogInfo(MainVo vo, String keyword) throws LaiKeAPIException
    {
        try {
            publiceService.checkIsSensitiveWords(vo.getStoreId(), keyword);
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            logger.error("获取敏感词列表异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryCatalogInfo");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quickProfile(ApplyShopVo vo, String logo, String mail_config, String wxImgUrl, String wxName, String h_Address,String default_lang,Integer default_currency) throws LaiKeAPIException
    {
        try
        {

            AdminModel admin = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            int i = 0;

            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (Objects.isNull(configModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QWSXTSZ, "请完善系统设置");
            }
            //添加邮箱
            configModel.setMail_config(mail_config);
            //移动端域名
            configModel.setH5_domain(h_Address);

            //用户默认头像,昵称
            String wxHeadImg = ImgUploadUtils.getUrlImgByName(wxImgUrl, true);
            configModel.setWx_headimgurl(wxHeadImg);
            configModel.setWx_name(wxName);

            if (StringUtils.isEmpty(default_lang))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WSZMRYZ, "未设置默认语种");
            }
            if (Objects.isNull(default_currency))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WSZMRBZ, "未设置默认币种");
            }

            configModel.setDefault_lang_code(default_lang);

            i = configModelMapper.updateByPrimaryKeySelective(configModel);

            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }

            //添加管理员默认语种
            AdminModel adminModel = new AdminModel();
            adminModel.setId(admin.getId());
            adminModel.setLang(default_lang);
            i = adminModelMapper.updateByPrimaryKeySelective(adminModel);

            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }

            //如果国际化设置未设置默认币种，则在快速配置里面新增
            Map defaultCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(vo.getStoreId());
            if (Objects.isNull(defaultCurrencyMap))
            {
                CurrencyStoreModel updateDefault = new CurrencyStoreModel();
                updateDefault.setStore_id(vo.getStoreId());
                updateDefault.setCurrency_id(default_currency);
                updateDefault.setDefault_currency(CurrencyStoreModel.DefaultCurrency.YES);
                adminCurrencyStoreService.setDefaultCurrency(updateDefault,false);
            }

            //添加自营店
            adminGoodsDubboService.addMch(vo,logo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取敏感词列表异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", e.getMessage());
        }
    }

    @Override
    public boolean checkHaveStoreMchId(MainVo vo)
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            return Objects.nonNull(storeMchId) && storeMchId != 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", e.getMessage());
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadAuth(UploadFileVo vo,Integer isSave) throws LaiKeAPIException
    {

        Map<String, Object> result = new HashMap<>();
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            //授权证书路径
            StringBuilder authPath = new StringBuilder(config.getAuthPath()).append(SplitUtils.FXG);

            authPath.append("laike_license");
            logger.info("授权证书保存路径：{}",authPath);
            //创建文件夹
            FileUtil.mkdir(String.valueOf(authPath));
            File authFile = new File(authPath.toString());

            MultipartFile file = vo.getImage()[0];

            //上传文件
            file.transferTo(authFile);

            //linux设置读写权限
            if (SystemUtil.getOsInfo().isLinux())
            {
                Files.setPosixFilePermissions(Paths.get(authFile.getPath()), PosixFilePermissions.fromString("rwxr-xr-x"));
            }
            String path = authFile.getPath();

            logger.info("授权证书路径：{}",path);
            result.put("savePath",path);

            //返回授权域名
            Map message = licenseUtils.getMessage(path);
            String domain = (String) message.get("domain");
            logger.info("domain{}",domain);

            if (StringUtils.isNotEmpty(domain))
            {
                String targetDomain = "java.houjiemeishi.com";
                String str = getDomainAndAfter(targetDomain, domain);
                result.put("domain",str);
            }
            if (Objects.nonNull(isSave) && isSave == 1)
            {
                redisUtil.set(LICENSE_KEY,path);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> getAuthPath(MainVo vo) throws LaiKeAPIException
    {
        Map<String,Object> result = new HashMap<>();
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String path = "";
            String str = "";
            if (redisUtil.hasKey(LICENSE_KEY))
            {
                path = (String) redisUtil.get(LICENSE_KEY);
                logger.info("path:{}",path);
                Map message = licenseUtils.getMessage(path);
                String domain = (String) message.get("domain");
                String targetDomain = "java.houjiemeishi.com";
                str = getDomainAndAfter(targetDomain, domain);
            }
            result.put("domain",str);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", e.getMessage());
        }
        return result;
    }

    private static String getDomainAndAfter(String targetDomain, String domainList)
    {
        // 使用逗号分隔域名字符串
        String[] domains = domainList.split(",");

        // 查找目标域名并返回后面的所有域名
        boolean found = false;
        StringBuilder result = new StringBuilder();

        for (String domain : domains)
        {
            if (found)
            {
                result.append(domain).append(",");
            }
            if (domain.equals(targetDomain))
            {
                found = true;
                result.append(domain).append(",");
            }
        }
        if (result.length() > 0)
        {
            return result.deleteCharAt(result.length() - 1).toString();
        }
        return null;
    }


    private void synchronizationPcMchPath(int storeId, String pcMchUrl) throws LaiKeAPIException
    {
        try
        {
            MchModel mchModelParam = new MchModel();
            mchModelParam.setStore_id(storeId);
            List<MchModel> mchModelList = mchModelMapper.select(mchModelParam);
            if (mchModelList != null && mchModelList.size() > 0)
            {
                for (MchModel mchModel : mchModelList)
                {
                    mchModel.setPc_mch_path(pcMchUrl + "/?storeId=" + mchModel.getStore_id());
                    mchModelMapper.updateByPrimaryKeySelective(mchModel);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("同步商城所有店铺pc端地址 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "synchronizationPcMchPath");
        }
    }

    /**
     * 基础信息初始化
     *
     * @param model
     * @param vo
     */
    private void initConfig(ConfigModel model, FrontConfigVo vo)
    {
        //同名的属性都会拷贝
        BeanUtils.copyProperties(vo, model);

        model.setMessage_day(vo.getMessageSaveDay());
        model.setExp_time(vo.getLogin_validity());
        model.setWatermark_name(vo.getWatermarkName());
        model.setWatermark_url(vo.getWatermarkUrl());

        model.setLogon_logo(vo.getLogon_logo());
        model.setCopyright_information(vo.getCopyright_information());
        model.setRecord_information(vo.getRecord_information());
        model.setLink_to_landing_page(vo.getLink_to_landing_page());
        model.setAdmin_default_portrait(vo.getAdmin_default_portrait());

    }

    private boolean addOrUpdatePrint(AddSystemVo vo)
    {
        PrintSetupModel printSetupModel = new PrintSetupModel();
        printSetupModel.setStoreId(vo.getStoreId());
        int count;
        //管理后台店铺id为0
        printSetupModel.setMchId(0);
        printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
        if (printSetupModel != null)
        {
            printSetupModel.setSheng(vo.getSheng());
            printSetupModel.setShi(vo.getShi());
            printSetupModel.setXian(vo.getXian());
            printSetupModel.setAddress(vo.getAddress());
            printSetupModel.setPrintName(vo.getPrintName());
            printSetupModel.setPrintUrl(vo.getPrintUrl());
            printSetupModel.setPhone(vo.getPhone());
            count = printSetupModelMapper.updateByPrimaryKeySelective(printSetupModel);
        }
        else
        {
            printSetupModel = new PrintSetupModel();
            printSetupModel.setStoreId(vo.getStoreId());
            //管理后台店铺id为0
            printSetupModel.setMchId(0);
            printSetupModel.setSheng(vo.getSheng());
            printSetupModel.setShi(vo.getShi());
            printSetupModel.setXian(vo.getXian());
            printSetupModel.setAddress(vo.getAddress());
            printSetupModel.setPrintName(vo.getPrintName());
            printSetupModel.setPrintUrl(vo.getPrintUrl());
            printSetupModel.setPhone(vo.getPhone());
            printSetupModel.setAddTime(new Date());
            count = printSetupModelMapper.insert(printSetupModel);
        }
        return count > 0;
    }

    private boolean addOrUpdateSearchConfig(AddSystemVo vo)
    {
        HotKeywordsModel hotKeywordsModel = new HotKeywordsModel();
        hotKeywordsModel.setStore_id(vo.getStoreId());
        hotKeywordsModel = hotKeywordsModelMapper.selectOne(hotKeywordsModel);
        Integer isOpen   = vo.getIsOpen();
        Integer limitNum = vo.getLimitNum();
        String  keyword  = vo.getKeyword();
        int     count;
        if (isOpen != null && isOpen == 1)
        {
            if (limitNum == null || limitNum < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZSYG, "上限至少一个");
            }
            if (StringUtils.isEmpty(keyword))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GJZBNWK, "关键词不能为空");
            }
            else if (keyword.split(SplitUtils.DH).length > limitNum)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GJZBNDYXZSL, "关键词不能大于限制数量");
            }
        }

        HotKeywordsModel hotKeywordsModelSave = new HotKeywordsModel();
        hotKeywordsModelSave.setNum(limitNum);
        hotKeywordsModelSave.setIs_open(isOpen);
        hotKeywordsModelSave.setKeyword(keyword);
        if (hotKeywordsModel != null)
        {
            hotKeywordsModelSave.setId(hotKeywordsModel.getId());
            count = hotKeywordsModelMapper.updateByPrimaryKeySelective(hotKeywordsModelSave);
        }
        else
        {
            hotKeywordsModelSave.setStore_id(vo.getStoreId());
            count = hotKeywordsModelMapper.insertSelective(hotKeywordsModelSave);
        }
        return count > 0;
    }


    @Autowired
    private AdminGoodsDubboService adminGoodsDubboService;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private AgreementModelMapper agreementModelMapper;

    @Autowired
    private  CustomerModelMapper customerModelMapper;

    @Autowired
    private AdminCurrencyStoreService adminCurrencyStoreService;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;
}

