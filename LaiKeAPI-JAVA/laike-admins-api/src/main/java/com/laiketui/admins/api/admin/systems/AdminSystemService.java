package com.laiketui.admins.api.admin.systems;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import com.laiketui.domain.vo.mch.ApplyShopVo;
import com.laiketui.domain.vo.systems.AddSystemVo;
import com.laiketui.domain.vo.systems.FrontConfigVo;
import com.laiketui.domain.vo.systems.SensitiveVo;
import com.laiketui.domain.vo.systems.SetSystemVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 系统设置
 *
 * @author Trick
 * @date 2021/1/19 9:06
 */
public interface AdminSystemService
{


    /**
     * 获取系统基本配置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 9:07
     */
    Map<String, Object> getSystemIndex(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/修改系统配置信息
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 9:15
     */
    boolean addSystemConfig(AddSystemVo vo) throws LaiKeAPIException;

    /**
     * 获取系统配置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/4 14:49
     */
    Map<String, Object> getSetSystem(MainVo vo) throws LaiKeAPIException;

    /**
     * 系统配置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/4 14:48
     */
    void setSystem(SetSystemVo vo) throws LaiKeAPIException;


    /**
     * 获取协议列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 11:12
     */
    Map<String, Object> getAgreementIndex(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 添加/编辑协议
     *
     * @param vo      -
     * @param title   -
     * @param type    -
     * @param content -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 11:17
     */
    void addAgreement(MainVo vo, Integer id, String title, int type, String content) throws LaiKeAPIException;

    /**
     * 删除协议
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/19 10:33
     */
    void delAgreement(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 修改常简问题
     *
     * @param vo            -
     * @param returnProblem -
     * @param payProblem    -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 14:15
     */
    boolean updateCommonProblem(MainVo vo, String returnProblem, String payProblem) throws LaiKeAPIException;


    /**
     * 售后服务
     *
     * @param vo            -
     * @param refundPolicy  -
     * @param cancelOrderno -
     * @param refundMoney   -
     * @param refundExplain -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 14:29
     */
    boolean updateRefundService(MainVo vo, String refundPolicy, String cancelOrderno, String refundMoney, String refundExplain) throws LaiKeAPIException;


    /**
     * 新手指南
     *
     * @param vo              -
     * @param shoppingProcess -
     * @param payType         -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 14:39
     */
    boolean updateBeginnerGuide(MainVo vo, String shoppingProcess, String payType) throws LaiKeAPIException;


    /**
     * 关于我
     *
     * @param vo       -
     * @param auboutMe -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 14:31
     */
    boolean updateAboutMe(MainVo vo, String auboutMe) throws LaiKeAPIException;


    /**
     * 上传图片
     *
     * @param vo    -
     * @param files -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 14:54
     */
    boolean uploadImages(MainVo vo, List<MultipartFile> files) throws LaiKeAPIException;


    /**
     * 微信接口配置
     *
     * @param vo        -
     * @param appid     -
     * @param appsecret -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 16:22
     */
    boolean updateWeiXinApi(MainVo vo, String appid, String appsecret) throws LaiKeAPIException;


    /**
     * 获取前端基础配置
     * @param vo
     * @param type
     * @return
     */
    Map<String,Object> getFrontConfig(MainVo vo, Integer type);

    /**
     * 新增或修改前端基础配置
     * @param vo
     */
    void addOrUpdateFrontConfig(FrontConfigVo vo);


    /**
     * 更新商城默认语种
     *
     * @param vo
     */
    void addOrUpdateStoreIntenationSetting(FrontConfigVo vo);

    /**
     * 新增或修改物流打印配置
     * @param vo
     */
    void LogisticsAndPrinting(AddSystemVo vo);

    /**
     * 搜索配置修改/新增
     * @param vo
     */
    void searchAndSensitiveWords(AddSystemVo vo);

    /**
     * 获取敏感词列表
     * @param vo
     * @return
     */
    Map<String,Object> selectSensitive(SensitiveVo vo);


    /**
     * 添加或修改敏感词
     * @param vo
     */
    void addSensitive(SensitiveVo vo) throws LaiKeAPIException;

    /**
     * 删除敏感词
     * @param vo
     */
    void deleteSensitive(SensitiveVo vo) throws LaiKeAPIException;

    /**
     *
     * @param vo
     * @param files
     */
    void importSensitives(MainVo vo, MultipartFile[] files) throws LaiKeAPIException;


    /**
     * 获取关键字，判断是否为敏感词
     * @param vo
     * @param keyword
     */
    void getDictionaryCatalogInfo(MainVo vo, String keyword) throws LaiKeAPIException;

    /**
     * 快速配置
     * @param
     */
    void quickProfile(ApplyShopVo vo, String logo, String mail_config, String wxImgUrl, String wxName, String h_Address,String default_lang,Integer default_currency) throws LaiKeAPIException;

    boolean checkHaveStoreMchId(MainVo vo);


    /**
     * 授权证书上传
     * @param vo
     * @return
     */
    Map<String,Object> uploadAuth(UploadFileVo vo,Integer isSave) throws LaiKeAPIException;

    /**
     * 获取授权域名
     * @return
     */
    Map<String,Object> getAuthPath(MainVo vo) throws  LaiKeAPIException;

}
