package com.laiketui.apps.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.*;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.user.AddBankVo;

import java.util.List;
import java.util.Map;

/**
 * 店铺业务接口
 *
 * @author Trick
 * @date 2020/9/30 10:52
 */
public interface AppsMchBaseService
{


    /**
     * 申请店铺
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/30 10:59
     */
    void applyShop(ApplyShopVo vo) throws LaiKeAPIException;


    /**
     * 我的店铺
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 16:30
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取店铺银行卡
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/24 15:19
     */
    Map<String, Object> bankList(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加店铺银行卡
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/5 15:42
     */
    void addBank(AddBankVo vo) throws LaiKeAPIException;


    /**
     * 编辑店铺银行卡
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/19 17:02
     */
    void updateBank(AddBankVo vo) throws LaiKeAPIException;

    /**
     * 根据id获取店铺银行卡信息
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/10 9:30
     */
    Map<String, Object> mchBankInfo(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 验证店铺名是否合法
     *
     * @param vo   -
     * @param name -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 11:01
     */
    boolean verifyStoreName(MainVo vo, String name) throws LaiKeAPIException;


    /**
     * 继续申请店铺
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 16:38
     */
    Map<String, Object> continueApply(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 添加商品页面
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/10 17:24
     */
    Map<String, Object> addGoodsPage(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 添加商品页面-加载更多
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/11 11:53
     */
    Map<String, Object> addGoodsPageLoad(AddGoodsPageLoadVo vo) throws LaiKeAPIException;


    /**
     * 添加商品
     *
     * @param vo        -
     * @param shopId    -
     * @param proId     -
     * @param labbelId  -
     * @param freightId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/11 14:41
     */
    void addGoods(MainVo vo, int shopId, String proId, String labbelId, Integer freightId) throws LaiKeAPIException;

    /**
     * 获取自选商品详情
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/27 12:33
     */
    Map<String, Object> getZxGoodsInfoById(MainVo vo, int shopId, int goodsId) throws LaiKeAPIException;


    /**
     * 加载上传商品页面
     * 【php mchAction.upload_merchandise_page】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 10:19
     */
    Map<String, Object> uploadMerchandisePage(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 获取商品类别
     *
     * @param vo      -
     * @param classId -
     * @param brandId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 15:11
     */
    Map<String, Object> getClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException;

    /**
     * 选择商品类别
     *
     * @param vo      -
     * @param classId -
     * @param brandId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 15:11
     */
    Map<String, Object> choiceClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException;


    /**
     * 获取属性名称
     *
     * @param vo         -
     * @param attributes -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 16:24
     */
    Map<String, Object> getAttributeName(MainVo vo, String attributes) throws LaiKeAPIException;


    /**
     * 获取属性值
     *
     * @param vo         -
     * @param attributes - 属性名称集
     * @param attrValues -属性一对多,json [{"attr_group_name":"1","attr_list":[{"attr_name":"1","status":true},{"attr_name":"2","status":true}]}]
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 17:29
     */
    Map<String, Object> getAttributeValue(MainVo vo, String attributes, String attrValues) throws LaiKeAPIException;

    /**
     * 我的商品
     * 【php mchAction.my_merchandise、my_merchandise_load】
     *
     * @param vo     -
     * @param shopId -
     * @param type   -
     * @param status -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 19:38
     */
    Map<String, Object> myMerchandise(MainVo vo, Integer shopId, String type, Integer status, Integer commodity_type) throws LaiKeAPIException;


    /**
     * 编辑商品-加载数据
     * 【php mchAction.modify】
     *
     * @param vo     -
     * @param shopId -
     * @param pid    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/16 9:04
     */
    Map<String, Object> modify(MainVo vo, int shopId, int pid) throws LaiKeAPIException;


    /**
     * 保存商品信息
     * 【php mchAction.upload_merchandise】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/17 15:26
     */
    void saveGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;


    /**
     * 重新编辑商品
     * 【php mchAction.re_edit】
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/20 9:19
     */
    boolean reEdit(UploadMerchandiseVo vo) throws LaiKeAPIException;


    /**
     * 加载 修改商品库存 页面
     * 【php mchAction.up_stock_page】
     *
     * @param vo     -
     * @param shopId -
     * @param pid    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/18 11:02
     */
    Map<String, Object> upStockPage(MainVo vo, int shopId, int pid) throws LaiKeAPIException;


    /**
     * 修改商品规格库存
     * 【php mchAction.up_stock】
     *
     * @param vo              -
     * @param shopId          -
     * @param confiGureModels - 商品规格json参数 [{"id":3095,"pid":755,num:1},{"id":3096,"pid":755,num:1},...]
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 15:07
     */
    void upStock(MainVo vo, int shopId, String confiGureModels) throws LaiKeAPIException;


    /**
     * 提交审核/撤销审核
     * 【php mchAction.submit_audit】
     *
     * @param vo     -
     * @param shopId -
     * @param pId    -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 17:25
     */
    boolean submitAudit(MainVo vo, int shopId, int pId) throws LaiKeAPIException;


    /**
     * 删除我的商品
     * <p>
     * 【php mchAction.del_my_merchandise】
     *
     * @param vo     -
     * @param shopId -
     * @param pId    -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 17:59
     */
    boolean delMyMerchandise(MainVo vo, int shopId, int pId) throws LaiKeAPIException;


    /**
     * 我的商品上下架
     * <p>
     * 【php mchAction.my_merchandise_status】
     *
     * @param vo     -
     * @param shopId -
     * @param pId    -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/19 18:09
     */
    boolean myMerchandiseStatus(MainVo vo, int shopId, String pId) throws LaiKeAPIException;


    /**
     * 加载店铺主页
     *
     * @param vo         -
     * @param shopId     -
     * @param shopListId -
     * @param longitude  -
     * @param latitude   -
     * @param type       - 1=推荐,2=全部商品,3=商品分类
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/23 9:32
     */
    Map<String, Object> storeHomepage(MainVo vo, int shopId, Integer shopListId, String longitude, String latitude, int type, String lang_code) throws LaiKeAPIException;


    /**
     * 进入店铺主页的时候添加浏览记录
     *
     * @param vo
     * @param shopId
     * @return
     * @throws LaiKeAPIException
     */
    void browseRecord(MainVo vo, int shopId) throws LaiKeAPIException;

    /**
     * 店铺主页加载更多
     *
     * @param vo     -
     * @param shopId -
     * @param type   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/23 16:57
     */
    Map<String, Object> storeHomepageLoad(MainVo vo, int shopId, int type, String lang_code) throws LaiKeAPIException;


    /**
     * 收藏店铺
     *
     * @param vo     -
     * @param shopId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/23 17:43
     */
    boolean collectionShop(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 进入设置店铺
     * 【php mch.into_set_shop】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/23 18:16
     */
    Map<String, Object> intoSetShop(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 设置店铺
     * 【php mch.set_shop】
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 9:14
     */
    boolean setShop(SetShopVo vo) throws LaiKeAPIException;


    /**
     * 注销店铺
     * 【php mch.cancellation_shop】
     *
     * @param vo     -
     * @param shopId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 12:00
     */
    void cancellationShop(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 我的顾客
     * 【php mch.shop_customer】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/24 15:25
     */
    Map<String, Object> shopCustomer(MainVo vo, int shopId) throws LaiKeAPIException;

    /**
     * 我的粉丝
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/02/21 15:25
     */
    Map<String, Object> shopFans(MainVo vo, int shopId) throws LaiKeAPIException;

    /**
     * @param vo
     * @param cid
     * @throws LaiKeAPIException
     */
    void removeFans(MainVo vo, Integer cid) throws LaiKeAPIException;


    /**
     * 我的订单
     * 【php mch.my_order】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 9:11
     */
    Map<String, Object> myOrder(MchOrderIndexVo vo) throws LaiKeAPIException;


    /**
     * 发货列表数据
     * 【php mch.deliver_show】
     *
     * @param vo      -
     * @param orderno -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 14:56
     */
    Map<String, Object> deliverShow(MainVo vo, String orderno) throws LaiKeAPIException;


    /**
     * 点击发货按钮-弹出填写发货信息
     * 【php mch.into_send】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 15:51
     */
    Map<String, Object> intoSend(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 发货
     * 【php mch.send】
     *
     * @param vo          -
     * @param shopId      -
     * @param sNo         -
     * @param expressId   -
     * @param courierNum  -
     * @param orderListId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/26 15:51
     */
    void send(MainVo vo, int shopId, String sNo, Integer expressId, String courierNum, String orderListId) throws LaiKeAPIException;


    /**
     * 关闭订单
     * 【php mch.closing_order】
     *
     * @param vo      -
     * @param shopId  -
     * @param orderno -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/27 9:06
     */
    boolean closingOrder(MainVo vo, int shopId, String orderno) throws LaiKeAPIException;


    /**
     * 修改订单
     * 【php mch.upOrder】
     *
     * @param vo          -
     * @param shopId      -
     * @param orderno     -
     * @param orderDetail -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/27 10:35
     */
    boolean upOrder(MainVo vo, int shopId, String orderno, String orderDetail) throws LaiKeAPIException;


    /**
     * 订单详情
     * 【php mch.orderDetails】
     *
     * @param vo     -
     * @param shopId -
     * @param sNo    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/27 11:13
     */
    Map<String, Object> orderDetails(MainVo vo, int shopId, String sNo) throws LaiKeAPIException;


    /**
     * 售后订单详情
     *
     * @param vo     -
     * @param shopId -
     * @param sNo    -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/11 16:23
     */
    Map<String, Object> returnOrderDetails(MainVo vo, int shopId, String sNo, int id) throws LaiKeAPIException;

    /**
     * 我的提现
     * 【php mch.my_wallet】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 10:15
     */
    Map<String, Object> myWallet(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 账户明细
     * 【php mch.account_details】
     *
     * @param vo       -
     * @param shopId   -
     * @param startDay - 开始时间
     * @param endDay   - 结束时间
     * @param orderNo  - 订单号
     * @param type     - 1=售后明细 2=提现明细
     * @param tabIndex - 0：审核中 1：审核通过 2：拒绝
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 11:06
     */
    Map<String, Object> accountDetails(MainVo vo, int shopId, int type, int tabIndex, String startDay, String endDay, String orderNo) throws LaiKeAPIException;

    /**
     * 提现明细
     *
     * @param vo     -
     * @param shopId -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/22 14:34
     */
    Map<String, Object> withdrawalDetails(MainVo vo, int shopId, int id) throws LaiKeAPIException;

    /**
     * 删除提现明细
     *
     * @param vo     -
     * @param shopId -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/23 17:11
     */
    Map<String, Object> delWithdrawalDetails(MainVo vo, int shopId, int id) throws LaiKeAPIException;

    /**
     * 账户收入/支出
     *
     * @param vo      -
     * @param shopId  -
     * @param status  -
     * @param keyWord -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/9/13 14:34
     */
    Map<String, Object> walletDetails(MainVo vo, int shopId, int status, String keyWord) throws LaiKeAPIException;

    /**
     * 添加我的门店
     * 【php mch.addStore】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 14:52
     */
    boolean addStore(AddStoreVo vo) throws LaiKeAPIException;


    /**
     * 编辑我的门店页面数据
     * 【php mch.edit_store_page】
     *
     * @param vo     -
     * @param shopId -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 16:16
     */
    Map<String, Object> editStorePage(MainVo vo, Integer shopId, int id) throws LaiKeAPIException;


    /**
     * 查看我的门店
     * 【php mch.see_my_store】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 11:04
     */
    Map<String, Object> seeMyStore(MainVo vo, int shopId, Integer proId) throws LaiKeAPIException;

    /**
     * 订单发货
     *
     * @param vo             -
     * @param orderDetailIds -
     * @throws LaiKeAPIException-
     * @author Administrator
     * @date 2021/8/2 14:42
     */
    void deliverySave(MainVo vo, String orderDetailIds) throws LaiKeAPIException;


    /**
     * 编辑我的门店
     * 【php mch.edit_store】
     *
     * @param vo) -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 16:39
     */
    boolean editStore(AddStoreVo vo) throws LaiKeAPIException;


    /**
     * 删除我的店铺
     * 【php mch.del_store】
     *
     * @param vo     -
     * @param shopId -
     * @param ids    - 门店id集
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 17:16
     */
    boolean delStore(MainVo vo, int shopId, String ids) throws LaiKeAPIException;

    /**
     * 验证码扫码订单信息
     *
     * @param vo
     * @param shopId
     * @param orderId
     * @param extractionCode
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> orderInfoForCode(MainVo vo, int shopId, Integer orderId, String extractionCode, Integer writeShopId) throws LaiKeAPIException;

    /**
     * 验证码提货
     * 【php mch.verification_extraction_code】
     *
     * @param vo             -
     * @param shopId         -
     * @param orderId        -
     * @param extractionCode -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/30 17:59
     */
    Map<String, Object> verificationExtractionCode(MainVo vo, int shopId, Integer orderId, String extractionCode, Integer mch_store_id, Integer pid) throws LaiKeAPIException;


    /**
     * 运费列表
     * 【php mch.freight_list】
     *
     * @param vo     -
     * @param shopId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 14:29
     */
    Map<String, Object> freightList(MainVo vo, int shopId) throws LaiKeAPIException;


    /**
     * 添加/修改运费
     * 【php mch.freight_add】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 16:13
     */
    void addFreight(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 删除运费
     * 【php mch.freight_del】
     *
     * @param vo     -
     * @param ids    -
     * @param shopId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/1 17:25
     */
    void freightDel(MainVo vo, String ids, int shopId) throws LaiKeAPIException;


    /**
     * 修改默认运费
     * 【php mch.set_default】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 16:15
     */
    void setDefault(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 编辑运费显示页面
     * 【php mch.freight_modify_show】
     *
     * @param vo    -
     * @param mchId -
     * @param id    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 9:10
     */
    Map<String, Object> freightModifyShow(MainVo vo, int mchId, int id) throws LaiKeAPIException;


    /**
     * 售后流程
     * 【php mch.examine】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/4 14:19
     */
    void examine(RefundVo vo) throws LaiKeAPIException;

    /**
     * 店铺银行卡解绑
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/10 16:31
     */
    void delBank(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 结算列表及详情
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/02/21 9:10
     */
    Map<String, Object> getSettlementOrderList(OrderSettlementVo vo) throws LaiKeAPIException;

    /**
     * 添加门店账户
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addMchStoreAccount(MchStoreAccountVo vo) throws LaiKeAPIException;

    /**
     * 删除门店账户
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void delMchStoreAccount(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 门店账户列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/02/21 9:10
     */
    Map<String, Object> mchStoreAccountList(MchStoreAccountVo vo) throws LaiKeAPIException;

    /**
     * 上传收款二维码
     *
     * @param vo
     * @param mchId
     * @param code
     * @throws LaiKeAPIException
     */
    void collectionCode(MainVo vo, Integer mchId, String code) throws LaiKeAPIException;


    /**
     * 门店管理员列表
     *
     * @param vo
     * @param mch_store_id 门店id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> StoreAdminList(MainVo vo, Integer mch_store_id) throws LaiKeAPIException;

    /**
     * 添加修改管理员
     *
     * @param vo
     * @param mch_store_id   门店id
     * @param account_number 管理员账号
     * @param password       密码
     * @param id             修改
     * @throws LaiKeAPIException
     */
    void addAdmin(MainVo vo, Integer mch_store_id, String account_number, String password, Integer id) throws LaiKeAPIException;

    /**
     * 删除管理员
     *
     * @param vo
     * @param mch_store_id
     * @param id
     * @throws LaiKeAPIException
     */
    void delAdmin(MainVo vo, Integer mch_store_id, Integer id) throws LaiKeAPIException;

    /**
     * 查看我的门店预约时间段
     *
     * @param vo
     * @param mchStoreId -门店id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getAppointmenTime(MainVo vo, Integer mchStoreId, Integer w_id) throws LaiKeAPIException;

    /**
     * 编辑我的门店预约时间段
     *
     * @param vo
     * @param w_id -门店id
     * @return
     * @throws LaiKeAPIException
     */
    Boolean editAppointmentTime(EditAppointVo vo, Integer w_id) throws LaiKeAPIException;

    /**
     * 添加我的门店预约时间段
     *
     * @param vo
     * @param store_id -门店id
     * @return
     * @throws LaiKeAPIException
     */
    Boolean addAppointmentTime(AddAppointVo vo, Integer store_id);

    /**
     * 删除我的门店预约时间段
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> deleteAppointmentTime(EditAppointVo vo);

    /**
     * 编辑我的门店预约时间段
     *
     * @param mchId
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getWriteStore(Integer mchId, Integer storeId);

    /**
     * 虚拟商品根据orderId来判断是否有预约
     *
     * @param dId
     * @return
     * @throws LaiKeAPIException
     */
    Integer getWrite(Integer dId);

    /**
     * 虚拟商品根据商品id来查询商品可用门店信息
     *
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> get_write_shop(String sNo);

    void selfSend(MainVo vo, int shopId, String sNo, String phone, String courier_name) throws LaiKeAPIException;

    /**
     * 极速退款
     * @param vo
     * @return
     */
    Map<String, Object> quickRefund(RefundVo vo);

    /**
     * 获取店铺diy数据
     *
     * @param vo
     * @param shopId
     * @param longitude
     * @param latitude
     * @return
     */
    Map<String, Object> getMchDiyHomePage(MainVo vo, int shopId, String longitude, String latitude);

}
