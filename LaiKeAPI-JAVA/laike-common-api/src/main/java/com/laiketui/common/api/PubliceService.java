package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.LangModel;
import com.laiketui.domain.Page;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.log.FilesRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.diy.DiyVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import com.laiketui.domain.vo.order.MemberPriceVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 公共服务
 *
 * @author Trick
 * @date 2020/10/23 10:21
 */
public interface PubliceService
{

    /**
     * 获取验证码图片
     *
     * @return Map
     * @throws LaiKeAPIException -
     * @author vvx
     * @date 2024/05/23 10:22
     */
    public Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取图片路径- 【已遗弃,后期将删除该方法】
     *
     * @param imgName -
     * @param storeId -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 10:22
     */
    @Deprecated
    String getImgPath(String imgName, String storeId) throws LaiKeAPIException;

    /**
     * 获取图片路径 - 根据指定的商城id获取oss图片
     *
     * @param imgName -
     * @param storeId -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/28 11:05
     */
    String getImgPath(String imgName, Integer storeId) throws LaiKeAPIException;

    /**
     * 获取已经登陆的用户
     *
     * @param vo -
     * @return User
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/30 10:02
     */
    User getRedisUserCache(MainVo vo) throws LaiKeAPIException;


    /**
     * 上传文件
     *
     * @param vo -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 11:50
     */
    List<String> uploadFiles(UploadFileVo vo) throws LaiKeAPIException;

    /**
     * 上传图片
     *
     * @param multipartFiles - 图片集
     * @param uploadType     - 上传方式
     * @param storeType      - 来源
     * @param storeId        - 商城id
     * @param mchId          - 店铺id
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 9:58
     */
    List<String> uploadImage(List<MultipartFile> multipartFiles, String uploadType, int storeType, int storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 上传图片
     *
     * @param multipartFiles -
     * @param uploadType     - 上传方式
     * @param storeType      - 来源
     * @param storeId        -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/4 10:15
     */
    List<String> uploadImage(List<MultipartFile> multipartFiles, String uploadType, int storeType, int storeId) throws LaiKeAPIException;

    /**
     * 临时图片[二维码/验证码]使用这个方法上传：这个方法不会将临时图片地址插入到lkt_file_recoder表 验证码将不会出现在图片管理器里面 有个坏处就是，得手动去删这些临时图片
     *
     * @param multipartFiles
     * @param uploadType
     * @param storeType
     * @param storeId
     * @param iscode
     * @return
     * @throws LaiKeAPIException
     */
    List<String> uploadImage(List<MultipartFile> multipartFiles, String uploadType, int storeType, int storeId, boolean iscode) throws LaiKeAPIException;


    /**
     * 图片外链上传
     *
     * @param url        - 下载源
     * @param uploadType - 上传方式
     * @param storeType  - 来源
     * @param storeId    -
     * @return String - 返回图片名称
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/22 16:12
     */
    String uploadImage(String url, String uploadType, int storeType, int storeId, Integer mchId) throws LaiKeAPIException;


    /**
     * 添加商品轮播图
     *
     * @param imageNameList -
     * @param pid           - 商品id
     * @param isUpdate      - 是否更新轮播图 ，如果不跟新则删除之前的轮播图
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/22 17:16
     */
    void addRotationImage(List<String> imageNameList, int pid, boolean isUpdate) throws LaiKeAPIException;

    /**
     * 验证token
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 10:22
     */
    Map<String, Object> verifyToken(MainVo vo) throws LaiKeAPIException;


    /**
     * token是否过期
     *
     * @param token -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/15 17:03
     */
    boolean verifyToken(String token) throws LaiKeAPIException;


    /**
     * 获取折扣
     *
     * @param storeId -
     * @param user    -
     * @return BigDecimal - 几折
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 10:26
     */
    BigDecimal getUserGradeRate(int storeId, User user) throws LaiKeAPIException;

    /**
     * 获取折扣
     *
     * @param storeId   -
     * @param user      -
     * @param isLowRate - 是否获取最低折扣
     * @return BigDecimal
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/26 11:20
     */
    BigDecimal getUserGradeRate(int storeId, User user, boolean isLowRate) throws LaiKeAPIException;


    /**
     * 获取用户/游客信息
     *
     * @param accessId - 令牌
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 14:53
     */
    User getUserInfo(String accessId) throws LaiKeAPIException;


    /**
     * 获取附近店铺
     *
     * @param tencentKey -
     * @param storeId    -
     * @param latitude   - 维度
     * @param longitude  - 经度
     * @param pageModel  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/26 15:11
     */
    List<Map<String, Object>> getNearbyShops(String tencentKey, int storeId, String latitude, String longitude, Page pageModel) throws LaiKeAPIException;


    /**
     * 区分购物车结算和立即购买---列出选购商品
     * <p>
     * php : tools.products_list
     *
     * @param goodsInfoList -[{"pid":"979"},{"cid":"5648"},{"num":1},{"sec_id":"6"}--秒杀id,{}] 商品信息
     * @param cartIds       - 购物车
     * @param buyType       - 购买类型 默认0 再次购买1
     * @param orderHead     - 订单头部
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 14:42
     */
    List<Map<String, Object>> productsList(List<Map<String, Object>> goodsInfoList, String cartIds, int buyType, String orderHead) throws LaiKeAPIException;

    /**
     * 插件活动是否开启
     *
     * @param storeId    -
     * @param pluginCode -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/22 15:01
     */
    boolean activityPlugin(int storeId, String pluginCode) throws LaiKeAPIException;


    /**
     * 获取店铺商品统计信息
     * 返回:
     * [key:    quantity_on_sale、quantity_sold、collection_num]
     * [value:  在售商品数量、已售数量、收藏数量]
     * 【php: mch.commodity_information】
     *
     * @param storeId -
     * @param mchId   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-01-18 11:17:54
     */
    Map<String, Object> commodityInformation(int storeId, int mchId,String lang_code) throws LaiKeAPIException;


    /**
     * 获取商品评论信息
     *
     * @param parmaMap -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/29 17:37
     */
    List<Map<String, Object>> getGoodsCommentList(Map<String, Object> parmaMap) throws LaiKeAPIException;

    /**
     * 获取商品回复明细
     *
     * @param parmaMap -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/10 20:15
     */
    Map<String, Object> getCommentsDetailInfoById(Map<String, Object> parmaMap) throws LaiKeAPIException;

    /**
     * 删除评论回复
     *
     * @param vo  -
     * @param cid -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/10 20:43
     */
    void delCommentsDetailInfoById(MainVo vo, int cid) throws LaiKeAPIException;

    /**
     * 校验密码
     *
     * @param header  -
     * @param phone   -
     * @param keyCode -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 9:17
     */
    boolean validatePhoneCode(String header, String phone, String keyCode) throws LaiKeAPIException;


    /**
     * 会员签到
     * [签到插件 php]sign.test
     *
     * @param user -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 10:07
     */
    Map<String, Integer> sign(User user) throws LaiKeAPIException;


    /**
     * 判断插件是否开启【自营店】
     *
     * @param pluginCode - 插件代码
     * @param pluginMap  - 返回结果引用
     * @param storeId    -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 10:00
     */
    boolean frontPlugin(int storeId, String pluginCode, Map<String, Object> pluginMap) throws LaiKeAPIException;

    /**
     * 判断插件是否开启
     *
     * @param pluginCode - 插件代码
     * @param pluginMap  - 返回结果引用
     * @param storeId    -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/12/9 17:02
     */
    boolean frontPlugin(int storeId, Integer mchId, String pluginCode, Map<String, Object> pluginMap,boolean isShow) throws LaiKeAPIException;

    /**
     * 获取商品支持的活动类型
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/1 15:46
     */
    List<Map<String, Object>> getGoodsActive(int storeId) throws LaiKeAPIException;

    /**
     * 获取商品支持的活动类型
     *
     * @param storeId -
     * @param active  -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/26 16:28
     */
    List<Map<String, Object>> getGoodsActive(int storeId, Integer active) throws LaiKeAPIException;


    /**
     * 整理用户购物车数据
     * 场景:
     * 防止未登录前加入商品到购物车登陆后不见;
     * 处理:
     * 获取未登录购物车信息合并到登陆后用户购物车
     * [php loginAction.updateUser]
     *
     * @param vo     -
     * @param userid -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 15:03
     */
    void arrangeUserCart(MainVo vo, String userid) throws LaiKeAPIException;


    /**
     * 获取协议
     *
     * @param storeId -
     * @param type    - 协议类型
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 16:10
     */
    Map<String, Object> getAgreement(int storeId, int type) throws LaiKeAPIException;

    /**
     * 订单确认页面获取商品信息
     *
     * @param products    -
     * @param storeId     -
     * @param productType -
     * @return List
     * @throws LaiKeAPIException -
     * @author wangxian
     * @date 2020/11/11 10:17
     */
    Map<String, Object> settlementProductsInfo(List<Map<String, Object>> products, int storeId, String productType) throws LaiKeAPIException;

    Map<String, Object> settlementProductsInfoVI(List<Map<String, Object>> products, int storeId, String productType);

    /**
     * 剔除已自选商品id集
     *
     * @param storeId -
     * @param shopId  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/11 9:30
     */
    List<Integer> getCheckedZiXuanGoodsList(int storeId, int shopId) throws LaiKeAPIException;

    /**
     * 计算会员折扣【update 2022-02-10 11:07:30 后期删除】
     *
     * @param params
     * @param storeId
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    @Deprecated
    Map<String, Object> getMemberPrice(List<Map<String, Object>> params, String userId, int storeId) throws LaiKeAPIException;

    /**
     * 计算会员折扣
     *
     * @param vo
     * @param vipSource
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getMemberPrice(MemberPriceVo vo, Integer vipSource) throws LaiKeAPIException;

    /**
     * 发送短信
     * 【php Tool.generate_code】
     *
     * @param storeId     -
     * @param phone       -
     * @param type        - 短信类型
     * @param smsType     - 短信模式  0:验证码 1:短信通知
     * @param smsParmaMap - 短信参数 ,只有通知类型的短信才传
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/3 16:20
     */
    boolean sendSms(int storeId, String phone, int type, int smsType, Map<String, String> smsParmaMap) throws LaiKeAPIException;


    /**
     * 验证短信模板
     *
     * @param storeId     -
     * @param phone       -
     * @param code        -
     * @param template    - 模板名称
     * @param smsParmaMap - 模板参数
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/18 15:51
     */
    boolean sendValidatorSmsTemplate(int storeId, String phone, String code, String template, Map<String, String> smsParmaMap,StringBuilder stringBuilder) throws LaiKeAPIException;


    /**
     * 获取微信token
     *
     * @param storeId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/17 9:13
     */
    String getWeiXinToken(int storeId) throws LaiKeAPIException;


    /**
     * 获取小程序二维码
     *
     * @param storeId   -
     * @param storeType -
     * @param path      -
     * @param scene     -
     * @param width     -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/25 11:23
     */
    String getWeiXinAppQrcode(int storeId, int storeType, String scene, String path, int width) throws LaiKeAPIException;


    /**
     * 获取当前商户logo
     *
     * @param storeId   -
     * @param storeType -
     * @param qrCodeUrl -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/25 11:36
     */
    String getStoreQrcode(int storeId, int storeType, String qrCodeUrl) throws LaiKeAPIException;


    /**
     * 添加操作记录
     *
     * @param storeId -
     * @param userId  -
     * @param text    -
     * @param type    - 0:登录/退出 1:添加 2:修改 3:删除 4:导出 5:启用/禁用 6:通过/拒绝 10删除订单
     * @param source  - 1.管理平台 2.PC店铺
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 17:40
     */
    boolean addAdminRecord(int storeId, String userId, String text, int type, int source) throws LaiKeAPIException;

    /**
     * @param storeId
     * @param userId
     * @param text
     * @param type    - 0:登录/退出 1:添加 2:修改 3:删除 4:导出 5:启用/禁用 6:通过/拒绝 10删除订单
     * @param source  - 1.管理平台 2.PC店铺
     * @param adminId - lkt_admin的id
     * @return
     * @throws LaiKeAPIException
     */
    boolean addAdminRecord(int storeId, String userId, String text, int type, int source, int mchId, int adminId) throws LaiKeAPIException;

    /**
     * 添加操作记录
     *
     * @param storeId -
     * @param userId  -
     * @param text    -
     * @param type    - 0:登录/退出 1:添加 2:修改 3:删除 4:导出 5:启用/禁用 6:通过/拒绝 10删除订单
     * @param source  - 1.管理平台 2.PC店铺
     * @param mchId   - 店铺id
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-12-07 15:01:38
     */
    boolean addAdminRecord(int storeId, String userId, String text, int type, int source, int mchId) throws LaiKeAPIException;

    /**
     * @param val
     * @return
     */
    List<DictionaryListModel> getDictionaryList(String val);

    /**
     * 省市级联
     *
     * @param level   - 省市县等级
     * @param groupId - 上级id
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 11:45
     */
    List<Map<String, Object>> getJoinCityCounty(int level, int groupId, String sel_city) throws LaiKeAPIException;

    /**
     * 省市级联
     *
     * @param level   - 省市县等级
     * @param groupId - 上级id
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 11:45
     */
    List<Map<String, Object>> getJoinCityCounty(int level, int groupId) throws LaiKeAPIException;


    /**
     * 修改后台消息表信息
     *
     * @param storeId              -
     * @param mchId                -
     * @param type                 -
     * @param parma                -
     * @param messageLoggingUpdate - 需要修改的值
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 10:08
     */
    void messageUpdate(int storeId, int mchId, int type, String parma, MessageLoggingModal messageLoggingUpdate) throws LaiKeAPIException;


    /**
     * 申请提现
     *
     * @param vo   -
     * @param user - 提现人
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 12:09
     */
    boolean withdrawals(Withdrawals1Vo vo, User user) throws LaiKeAPIException;

    /**
     * 获取后台权限按钮
     * 【php dbAction.Jurisdiction】
     *
     * @param storeId -
     * @param admin   - 当前登录人
     * @param url     - 需要判断权限的url
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/3 15:28
     */
    @Deprecated
    boolean jurisdiction(int storeId, AdminModel admin, String url) throws LaiKeAPIException;

    /**
     * 验证图像验证码
     *
     * @param token -
     * @param code  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/16 17:08
     */
    void validateImgCode(String token, String code) throws LaiKeAPIException;

    /**
     * 图片删除
     *
     * @param filesRecordModel
     * @throws LaiKeAPIException
     */
    void delFileRecoderModel(FilesRecordModel filesRecordModel) throws LaiKeAPIException;


    static void main(String[] args)
    {
        Date a = DateUtil.dateFormateToDate("2022-06-10", GloabConst.TimePattern.YMD);
        Date b = DateUtil.dateFormateToDate("2022-06-10", GloabConst.TimePattern.YMD);
        System.out.println(DateUtil.dateConversion(b.getTime() / 1000, a.getTime() / 1000, DateUtil.TimeType.DAY));
    }


    /**
     * 增加操作记录，pc和管理后台共用一个接口
     *
     * @param storeId
     * @param text
     * @param type    - 0:登录/退出 1:添加 2:修改 3:删除 4:导出 5:启用/禁用 6:通过/拒绝 10删除订单
     * @return
     * @throws LaiKeAPIException
     */
    boolean addAdminRecord(int storeId, String text, int type, String token) throws LaiKeAPIException;


    /**
     * 获取店铺店门店列表
     *
     * @param vo
     * @return
     */
    Map<String, Object> getMchStore(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取系统支持的语言
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    List<LangModel> getAdminLangs(MainVo vo) throws LaiKeAPIException;

    //移动端获取商城所有绑定的语言
    List<LangModel> getAppLangs(MainVo vo) throws LaiKeAPIException;

    //PC店铺端
    List<LangModel> getMchLangs(MainVo vo) throws LaiKeAPIException;

    //移动端获取商城所有绑定的货币信息
    List<Map<String, Object>> getAppCurencys(CurrencyStoreVo vo) throws LaiKeAPIException;

    List<Map<String, Object>> getPcMchCurencys(CurrencyStoreVo vo) throws LaiKeAPIException;

    //获取商城所有绑定的默认货币信息
    Map<String, Object> getStoreDefaultCurrency(CurrencyStoreVo vo) throws LaiKeAPIException;

    //获取商城所有绑定的默认货币信息
    Map<String, Object> getStoreDefaultLang(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取国家列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    List<CountryModel> getCountryList(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取语种name
     *
     * @param langCode
     * @return
     */
    String getLangName(String langCode) throws LaiKeAPIException;

    /**
     * 获取国家名称
     *
     * @param countryNum
     * @return
     */
    String getCountryName(int countryNum) throws LaiKeAPIException;


    /**
     * 随机生成账号
     * @return
     */
    String generateAccount(Integer length);

    /**
     * 修改/编辑diy模板
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    Map<String, Object> addOrUpdateDiy(DiyVo vo);

    /**
     * 设置diy模板
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2022/04/28 16:00
     */
    void diyStatus(MainVo vo, Integer id,Integer mchId);

    /**
     * 删除模板
     * @param vo
     * @param id
     */
    void delDiy(MainVo vo, int id);
    /**
     * 更改货币
     *
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 9:18
     */
    void changeCurrency(CurrencyStoreVo vo) throws LaiKeAPIException;

    /**
     * 获取用户下单时的货币最新信息 【防止汇率已经被修改，如果被修改那么提示用户重新下单】
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    CurrencyStoreModel fetchUserCurrencyInfo(CurrencyStoreVo vo) throws LaiKeAPIException;

    /**
     * 检查是否是敏感词
     *
     * @param storeId
     * @param keyword
     */
    void checkIsSensitiveWords(int storeId, String keyword) throws LaiKeAPIException;

    String delBindUnit(Integer diyPageId, Integer diyId, String unit, String linkKey, Integer bindId, String diyValue, boolean isUnBind) throws LaiKeAPIException;

    /**
     * 获取邮箱配置
     * @param vo
     */
    Map<String,Object> getEmailConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑邮箱配置
     * @param vo
     * @param config
     */
    void addOrUpdateEmailConfig(MainVo vo, String config,Integer id) throws LaiKeAPIException;

    /**
     * 获取系统商城名称和浏览器icon
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getSystemIconAndName(MainVo vo) throws LaiKeAPIException;

    /**
     * 发送邮箱验证码
     * @param email
     * @param vo
     * @throws LaiKeAPIException
     */
    void sendEmail(String email, MainVo vo) throws LaiKeAPIException;

    /**
     * 批量获取图片路径（性能最佳）
     * @param imgNames  图片名集合
     * @param storeId   商城ID
     * @return Map<imgName, url>
     */
    Map<String, String> batchGetImgPath(Collection<String> imgNames, Integer storeId) throws LaiKeAPIException;

    /**
     * @param vo
     * @param type
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getFrontConfig(MainVo vo, Integer type) throws LaiKeAPIException;

}
