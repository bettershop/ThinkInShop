package com.laiketui.admins.admin.controller;

import com.laiketui.admins.api.admin.systems.AdminAddressService;
import com.laiketui.admins.api.admin.systems.AdminSearchService;
import com.laiketui.admins.api.admin.systems.AdminSmsService;
import com.laiketui.admins.api.admin.systems.AdminSystemService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.cascade.PublicCascadeService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import com.laiketui.domain.vo.mch.ApplyShopVo;
import com.laiketui.domain.vo.systems.*;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 系统管理
 *
 * @author Trick
 * @date 2021/1/15 9:27
 */
@Api(tags = "后台-系统管理")
@RestController
@RequestMapping("/admin/system")
public class AdminSystemManagerController
{

    @Autowired
    AdminAddressService adminAddressService;

    @Autowired
    private PubliceService publiceService;

    @ApiOperation("获取地址列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "name", value = "联系人/手机号", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型（1发货地址 2售后地址） 默认2", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getAddressInfo")
    @HttpApiMethod(apiKey = "admin.system.getAddressInfo")
    public Result getAddressInfo(MainVo vo, Integer id, String name, Integer type)
    {
        try
        {
            return Result.success(adminAddressService.getAddressInfo(vo, id, name, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("设置默认地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "int", paramType = "form")
    })
    @PostMapping("/setDefaultAddress")
    @HttpApiMethod(apiKey = "admin.system.setDefaultAddress")
    public Result setDefaultAddress(MainVo vo, int id)
    {
        try
        {
            adminAddressService.setDefaultAddress(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑地址")
    @PostMapping("/addAddressInfo")
    @HttpApiMethod(apiKey = "admin.system.addAddressInfo")
    public Result addAddressInfo(AddressVo vo)
    {
        try
        {
            return Result.success(adminAddressService.addAddressInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型（1发货地址 2售后地址） 默认2", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delAddress")
    @HttpApiMethod(apiKey = "admin.system.delAddress")
    public Result delAddress(MainVo vo, int id, int type)
    {
        try
        {
            return Result.success(adminAddressService.delAddress(vo, id, type));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private AdminSmsService smsService;

    @Autowired
    private PublicCascadeService publicCascadeService;

    @ApiOperation("获取短信列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "列表id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getSmsInfo")
    @HttpApiMethod(apiKey = "admin.system.getSmsInfo")
    public Result getSmsInfo(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(smsService.getSmsInfo(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("级联获取短信类型/类别/短信模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "superName", value = "上级名称", dataType = "string", paramType = "form")
    })
    @PostMapping("/getSmsTypeList")
    @HttpApiMethod(apiKey = "admin.system.getSmsTypeList")
    public Result getSmsTypeList(MainVo vo, String superName)
    {
        try
        {
            return Result.success(publicCascadeService.getSmsTypeList(vo, superName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("级联获取短信模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "0:验证码 1:短信通知", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "模板id", dataType = "int", paramType = "form"),
    })
    @PostMapping("/getSmsTemplateList")
    @HttpApiMethod(apiKey = "admin.system.getSmsTemplateList")
    public Result getSmsTemplateList(MainVo vo, Integer type, Integer id)
    {
        try
        {
            return Result.success(publicCascadeService.getSmsTemplateList(vo, type, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑短信列表")
    @PostMapping("/addMessageList")
    @HttpApiMethod(apiKey = "admin.system.addMessageList")
    public Result addMessageList(AddMessageListVo vo)
    {
        try
        {
            return Result.success(smsService.addMessageList(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除短信自定义配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "短信列表id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/delMessageList")
    @HttpApiMethod(apiKey = "admin.system.delMessageList")
    public Result delMessageList(MainVo vo, int id)
    {
        try
        {
            return Result.success(smsService.delMessageList(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取模板列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "短信模板id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "international", value = "是否是国际短信 0：否 1：是", dataType = "int", paramType = "form")
    })
    @PostMapping("/getSmsTemplateInfo")
    @HttpApiMethod(apiKey = "admin.system.getSmsTemplateInfo")
    public Result getSmsTemplateInfo(MainVo vo, Integer id,Integer international)
    {
        try
        {
            return Result.success(smsService.getSmsTemplateInfo(vo, id,international));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "短信模板id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "phone", value = "接收手机号", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "cpc", value = "区号", dataType = "string", paramType = "form")
    })
    @PostMapping("/addMessageTemplate")
    @HttpApiMethod(apiKey = "admin.system.addMessage")
    public Result addMessage(AddMessageVo vo, String phone,String cpc)
    {
        try
        {
            smsService.addMessage(vo, phone,cpc);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "短信模板id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delMessage")
    @HttpApiMethod(apiKey = "admin.system.delMessage")
    public Result delMessage(AddMessageVo vo, int id)
    {
        try
        {
            smsService.delMessage(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取模板核心配置信息")
    @PostMapping("/getTemplateConfigInfo")
    @HttpApiMethod(apiKey = "admin.system.getTemplateConfigInfo")
    public Result getTemplateConfigInfo(MainVo vo)
    {
        try
        {
            return Result.success(smsService.getTemplateConfigInfo(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑核心设置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "key", value = "accessKeyId", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "secret", value = "accessKeySecret", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "signName", value = "签名", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/addTemplateConfig")
    @HttpApiMethod(apiKey = "admin.system.addTemplateConfig")
    public Result addTemplateConfig(MainVo vo, String key, String secret, String signName)
    {
        try
        {
            return Result.success(smsService.addTemplateConfig(vo, key, secret, signName));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private AdminSystemService systemService;

    @ApiOperation("获取系统基础配置")
    @PostMapping("/getSystemIndex")
    @HttpApiMethod(apiKey = "admin.system.getSystemIndex")
    public Result getSystemIndex(MainVo vo)
    {
        try
        {
            return Result.success(systemService.getSystemIndex(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改系统【基础配置】信息")
    @PostMapping("/addSystemConfig")
    @HttpApiMethod(apiKey = "admin.system.addSystemConfig")
    public Result addSystemConfig(AddSystemVo vo)
    {
        try
        {
            return Result.success(systemService.addSystemConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取【系统配置】信息")
    @PostMapping("/getSetSystem")
    @HttpApiMethod(apiKey = "admin.system.getSetSystem")
    public Result getSetSystem(MainVo vo)
    {
        try
        {
            return Result.success(systemService.getSetSystem(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改【系统配置】信息")
    @PostMapping("/setSystem")
    @HttpApiMethod(apiKey = "admin.system.setSystem")
    public Result setSystem(SetSystemVo vo)
    {
        try
        {
            systemService.setSystem(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取协议列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "协议id", dataType = "int", paramType = "form")
    })
    @PostMapping("/getAgreementIndex")
    @HttpApiMethod(apiKey = "admin.system.getAgreementIndex")
    public Result getAgreementIndex(MainVo vo, Integer id)
    {
        try
        {
            return Result.success(systemService.getAgreementIndex(vo, id));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑协议")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "title", value = "标题", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "type", value = "类型 0:注册 1:店铺 2:隐私", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "content", value = "协议id", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/addAgreement")
    @HttpApiMethod(apiKey = "admin.system.addAgreement")
    public Result addAgreement(MainVo vo, Integer id, String title, int type, String content)
    {
        try
        {
            systemService.addAgreement(vo, id, title, type, content);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("删除协议")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "string", paramType = "form"),
    })
    @PostMapping("/delAgreement")
    @HttpApiMethod(apiKey = "admin.system.delAgreement")
    public Result delAgreement(MainVo vo, int id)
    {
        try
        {
            systemService.delAgreement(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("常见问题修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "returnProblem", value = "售后问题", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "payProblem", value = "支付问题", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/updateCommonProblem")
    @HttpApiMethod(apiKey = "admin.system.updateCommonProblem")
    public Result updateCommonProblem(MainVo vo, String returnProblem, String payProblem)
    {
        try
        {
            return Result.success(systemService.updateCommonProblem(vo, returnProblem, payProblem));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新手指南修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shoppingProcess", value = "购物流程", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "payType", value = "支付方式", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/updateBeginnerGuide")
    @HttpApiMethod(apiKey = "admin.system.updateBeginnerGuide")
    public Result updateBeginnerGuide(MainVo vo, String shoppingProcess, String payType)
    {
        try
        {
            return Result.success(systemService.updateBeginnerGuide(vo, shoppingProcess, payType));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("售后服务修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "refundPolicy", value = "退货政策", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "cancelOrderno", value = "取消订单", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "refundMoney", value = "退款流程", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "refundExplain", value = "退款说明", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/updateRefundService")
    @HttpApiMethod(apiKey = "admin.system.updateRefundService")
    public Result updateRefundService(MainVo vo, String refundPolicy, String cancelOrderno, String refundMoney, String refundExplain)
    {
        try
        {
            return Result.success(systemService.updateRefundService(vo, refundPolicy, cancelOrderno, refundMoney, refundExplain));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("关于我们修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "auboutMe", value = "关于我们", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/updateAboutMe")
    @HttpApiMethod(apiKey = "admin.system.updateAboutMe")
    public Result updateAboutMe(MainVo vo, HttpServletRequest request)
    {
        try
        {
            System.out.println(request.getParameter("auboutMe"));
            return Result.success(systemService.updateAboutMe(vo, request.getParameter("auboutMe")));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("上传图片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "files", value = "图片集合", required = true, dataType = "file", paramType = "form")
    })
    @PostMapping("/uploadImages")
    @HttpApiMethod(apiKey = "admin.system.uploadImages")
    public Result uploadImages(MainVo vo, List<MultipartFile> files)
    {
        try
        {
            return Result.success(systemService.uploadImages(vo, files));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("微信小程序配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appid", value = "appid", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "appsecret", value = "appsecret", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/updateWeiXinApi")
    public Result updateWeiXinApi(MainVo vo, String appid, String appsecret)
    {
        try
        {
            return Result.success(systemService.updateWeiXinApi(vo, appid, appsecret));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @Autowired
    private AdminSearchService searchService;

    @ApiOperation("搜搜配置查询")
    @PostMapping("/getSearchConfigIndex")
    @HttpApiMethod(apiKey = "admin.system.getSearchConfigIndex")
    public Result getSearchConfigIndex(MainVo vo)
    {
        try
        {
            return Result.success(searchService.getSearchConfigIndex(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑 搜搜配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isOpen", value = "是否开启", required = true, dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "limitNum", value = "关键词上限", required = true, dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "keyword", value = "关键词,多个用','分隔", required = true, dataType = "string", paramType = "form")
    })
    @PostMapping("/addSearchConfig")
    @HttpApiMethod(apiKey = "admin.system.addSearchConfig")
    public Result addSearchConfig(MainVo vo, int isOpen, Integer limitNum, String keyword)
    {
        try
        {
            return Result.success(searchService.addSearchConfig(vo, isOpen, limitNum, keyword));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取前端基础信息配置")
    @PostMapping("/getBasicConfiguration")
    @HttpApiMethod(apiKey = "admin.system.GetBasicConfiguration")
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

    @ApiOperation("获取前端基础配置-打印配置")
    @PostMapping("/getLogisticsAndPrinting")
    @HttpApiMethod(apiKey = "admin.system.GetLogisticsAndPrinting")
    public Result getPrintSetupConfig(MainVo vo)
    {
        try
        {
            return Result.success(systemService.getFrontConfig(vo, 2));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新增/修改基础配置")
    @PostMapping("/addOrUpdateFrontConfig")
    @HttpApiMethod(apiKey = "admin.system.SetBasicConfiguration")
    public Result addOrUpdateFrontConfig(FrontConfigVo vo)
    {
        try
        {
            systemService.addOrUpdateFrontConfig(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("物流及打印配置修改/新增")
    @PostMapping("/LogisticsAndPrinting")
    @HttpApiMethod(apiKey = "admin.system.LogisticsAndPrinting")
    public Result LogisticsAndPrinting(AddSystemVo vo)
    {
        try
        {
            systemService.LogisticsAndPrinting(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("搜索配置")
    @PostMapping("/getSearchAndSensitiveWords")
    @HttpApiMethod(apiKey = "admin.system.GetSearchAndSensitiveWords")
    public Result searchAndSensitiveWords(MainVo vo)
    {
        try
        {
            return Result.success(systemService.getFrontConfig(vo, 3));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("搜索配置修改/新增")
    @PostMapping("/searchAndSensitiveWords")
    @HttpApiMethod(apiKey = "admin.system.SearchAndSensitiveWords")
    public Result searchAndSensitiveWords(AddSystemVo vo)
    {
        try
        {
            systemService.searchAndSensitiveWords(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("敏感词列表")
    @PostMapping("/selectSensitive")
    @HttpApiMethod(apiKey = "admin.system.selectSensitive")
    public Result selectSensitive(SensitiveVo vo)
    {
        try
        {
            return Result.success(systemService.selectSensitive(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/修改敏感词")
    @PostMapping("/addSensitive")
    @HttpApiMethod(apiKey = "admin.system.addSensitive")
    public Result addSensitive(SensitiveVo vo) throws LaiKeAPIException
    {
        try
        {
            systemService.addSensitive(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("删除敏感词")
    @PostMapping("/delSensitive")
    @HttpApiMethod(apiKey = "admin.system.deleteSensitive")
    public Result deleteSensitive(SensitiveVo vo) throws LaiKeAPIException
    {
        try
        {
            systemService.deleteSensitive(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("敏感词导入")
    @PostMapping("/importSensitives")
    @HttpApiMethod(apiKey = "admin.system.importSensitives")
    public Result importSensitives(MainVo vo, MultipartFile[] image) throws LaiKeAPIException
    {
        try
        {
            systemService.importSensitives(vo, image);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("判断是否为敏感词")
    @PostMapping("/getDictionaryCatalogInfo")
    @HttpApiMethod(apiKey = "saas.PublicTools.getDictionaryCatalogInfo")
    public Result getDictionaryCatalogInfo(MainVo vo, String keyword) throws LaiKeAPIException {
        try
        {
            systemService.getDictionaryCatalogInfo(vo,keyword);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取国际化设置")
    @PostMapping("/storeIntenationSetting")
    @HttpApiMethod(apiKey = "admin.system.storeIntenationSetting")
    public Result storeIntenationSetting(MainVo vo)
    {
        try
        {
            return Result.success(systemService.getFrontConfig(vo, 4));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("新增/修改国际化配置")
    @PostMapping("/addOrUpdateStoreIntenationSetting")
    @HttpApiMethod(apiKey = "admin.system.addOrUpdateStoreIntenationSetting")
    public Result addOrUpdateStoreIntenationSetting(FrontConfigVo vo)
    {
        try
        {
            systemService.addOrUpdateStoreIntenationSetting(vo);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取分账设置")
    @PostMapping("/getAccountConfig")
    @HttpApiMethod(apiKey = "admin.system.getAccountConfig")
    public Result getAccountConfig(FrontConfigVo vo)
    {
        try
        {
            return Result.success(systemService.getFrontConfig(vo, 5));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("获取邮箱配置")
    @PostMapping("/getEmailConfig")
    @HttpApiMethod(apiKey = "admin.system.getEmailConfig")
    public Result getEmailConfig(MainVo vo)
    {
        try
        {
            return Result.success(publiceService.getEmailConfig(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("添加/编辑邮箱配置")
    @PostMapping("/addOrUpdateEmailConfig")
    @HttpApiMethod(apiKey = "admin.system.addOrUpdateEmailConfig")
    public Result addOrUpdateEmailConfig(MainVo vo,String mail_config,Integer id)
    {
        try
        {
            publiceService.addOrUpdateEmailConfig(vo,mail_config,id);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("快速配置")
    @PostMapping("/quickProfile")
    @HttpApiMethod(apiKey = "admin.system.quickProfile")
    public Result quickProfile(ApplyShopVo vo,String logo,String mail_config,String wx_headimgurl,String wx_name,String h_Address,String default_lang,Integer default_currency)
    {
        try
        {
            systemService.quickProfile(vo,logo,mail_config,wx_headimgurl,wx_name,h_Address,default_lang,default_currency);
            return Result.success();
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("授权证书上传")
    @PostMapping("/uploadAuth")
    @HttpApiMethod(apiKey = "admin.system.uploadAuth")
    public Result uploadFiles(@RequestParam("file") MultipartFile[] file, UploadFileVo vo, Integer isSave)
    {
        try
        {
            vo.setImage(file);
            return Result.success(systemService.uploadAuth(vo,isSave));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("获取授权域名信息")
    @PostMapping("/getAuthPath")
    @HttpApiMethod(apiKey = "admin.system.getAuthPath")
    public Result getAuthPath(MainVo vo)
    {
        try
        {
            return Result.success(systemService.getAuthPath(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }



    @ApiOperation("判断是否有自营店")
    @PostMapping("/checkHaveStoreMchId")
    @HttpApiMethod(apiKey = "admin.system.checkHaveStoreMchId")
    public Result checkHaveStoreMchId(MainVo vo)
    {
        try
        {
            return Result.success(systemService.checkHaveStoreMchId(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
