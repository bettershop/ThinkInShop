package com.laiketui.common.api;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 关于商品的公共接口
 *
 * @author Trick
 * @date 2020/11/11 14:57
 */
public interface PublicGoodsService
{


    /**
     * 添加商品数据加载
     * [php $product->add_page]
     *
     * @param storeId   -
     * @param adminName -
     * @param mchId     -
     * @param type      -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 10:08
     */
    Map<String, Object> addPage(int storeId, String adminName, int mchId, int type) throws LaiKeAPIException;

    Map<String, Object> addPage(MainVo vo, String adminName, int mchId, int type) throws LaiKeAPIException;


    /**
     * 编辑商品页面数据加载
     * [php $product->edit_page]
     *
     * @param storeId   -
     * @param adminName - 当前登录用户账号
     * @param mchId     - 店铺id
     * @param goodsId   - 商品id
     * @param type      - 0 = 平台 1=其它店铺
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 10:08
     */
    Map<String, Object> editPage(int storeId, String adminName, int mchId, int goodsId, int type) throws LaiKeAPIException;

    //国际化新增参数
    Map<String, Object> editPage(MainVo vo, String adminName, int mchId, int goodsId, int type) throws LaiKeAPIException;


    /**
     * 编辑商品页面数据加载
     * [php $product->edit_page]
     *
     * @param storeId   -
     * @param adminName - 当前登录用户账号
     * @param mchId     - 店铺id
     * @param goodsId   - 商品id
     * @param type      - 0 = 平台 1=其它店铺
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 10:08
     */
    Map<String, Object> livingEditPage(int storeId, String adminName, int mchId, int goodsId, int type, Integer roomId) throws LaiKeAPIException;


    /**
     * 商品列表
     * [php $product->product_list]
     *
     * @param storeId   -
     * @param adminName -
     * @param mchId     -
     * @param type      - 0 = 平台 1=pc店铺 2=店铺
     * @param map       -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 10:07
     */
    Map<String, Object> productList(int storeId, String adminName, int mchId, int type, Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取分类及品牌
     * 【php $product->get_classified_brands 】
     *
     * @param storeId -
     * @param classId -
     * @param brandId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 11:25
     */
    Map<String, Object> getClassifiedBrands(int storeId, Integer classId, Integer brandId) throws LaiKeAPIException;

    Map<String, Object> getClassifiedBrands(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException;

    /**
     * 获取属性
     * 【php $product->attribute1 】
     *
     * @param storeId       -
     * @param attributeList -
     * @param pageModel     - 分页
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 16:46
     */
    List<Map<String, Object>> attribute1(int storeId, PageModel pageModel, List<String> attributeList,Integer sid) throws LaiKeAPIException;

    List<Map<String, Object>> attribute1(MainVo vo, PageModel pageModel, List<String> attributeList, Integer sid) throws LaiKeAPIException;

    /**
     * 商品类别找上级 - 拼接字符串
     * 【php Tools.str_option】
     *
     * @param storeId -
     * @param cid     - 类别id
     * @param res     - 上级拼接字符串  -1-2-3-4...
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/13 11:23
     */
    String strOption(int storeId, int cid, String res) throws LaiKeAPIException;


    /**
     * 递归招找上级
     *
     * @param storeId   -
     * @param cid       -
     * @param resultMap - 结果
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 14:07
     */
    void getClassLevelAllInfo(int storeId, int cid, Map<Integer, List<ProductClassModel>> resultMap) throws LaiKeAPIException;

    /**
     * 递归招找上级(供应商)
     *
     * @param storeId   -
     * @param cid       -
     * @param resultMap - 结果
     * @throws LaiKeAPIException -
     */
    void getSupplierClassLevelAllInfo(int storeId, int cid, Map<Integer, List<Map<String, Object>>> resultMap, int i) throws LaiKeAPIException;

    /**
     * 递归找下级
     *
     * @param storeId  -
     * @param sid      -
     * @param classIds - 需要显示的下级id集
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/11 16:19
     */
    List<Map<String, Object>> getClassLevelLowAll(int storeId, int sid, List<Integer> classIds) throws LaiKeAPIException;

    List<Map<String, Object>> getClassLevelLowAll(int storeId, int sid) throws LaiKeAPIException;

    /**
     * 获取店铺下的所有商品分类 gp
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> mchAllFenlei(int storeId, Integer mchId) throws LaiKeAPIException;

    /**
     * 获取类别顶级id
     *
     * @param storeId -
     * @param cid     -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/2 15:23
     */
    Integer getClassTop(int storeId, int cid) throws LaiKeAPIException;

    /**
     * 获取属性值
     * [php $product->attribute_name1]
     *
     * @param storeId        -
     * @param attributeNames -
     * @param attrValue      - 属性值对象,{"attr_group_name":"1","attr_list":[{"attr_name":"1","status":true}
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 18:13
     */
    Map<String, Object> attributeName1(int storeId, String attributeNames, Map<String, Object> attrValue,Integer sid) throws LaiKeAPIException;


    /**
     * 上传商品
     * 【php $product->add_product】
     *
     * @param vo        - 如果vo中存在商品id则是修改
     * @param adminName -
     * @param mchId     -
     * @param type      -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/17 15:39
     */
    boolean addProduct(UploadMerchandiseVo vo, String adminName, int mchId, int type) throws LaiKeAPIException;

    boolean addProduct1(UploadMerchandiseVo vo, String adminName, int mchId, int type) throws LaiKeAPIException;

    /**
     * 获取属性代码
     *
     * @param storeId -
     * @param skuName - 属性名称(传了上级id则使用上级id)
     * @param sid     - 上级id
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/2/3 11:12 move-> 2021/9/8 17:57
     */
    String getSkuCode(int storeId, String skuName, Integer sid) throws LaiKeAPIException;

    /**
     * 根据id删除商品流程
     *
     * @param storeId -
     * @param goodsId -
     * @param mchId   - 可选
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 14:41
     */
    boolean delGoodsById(int storeId, int goodsId, Integer mchId) throws LaiKeAPIException;


    /**
     * 上下架商品
     * 【php product.upperAndLowerShelves】
     *
     * @param storeId  -
     * @param goodsIds -
     * @param mchId    -
     * @param status   -1=上架 0=下架
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 15:13
     */
    void upperAndLowerShelves(int storeId, String goodsIds, Integer mchId, Integer status) throws LaiKeAPIException;


    /**
     * 删除轮播图
     * 【php Tool.delBanner】
     *
     * @param storeId  -
     * @param goodsIds -
     * @param str      - url中的参数字段
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 16:13
     */
    boolean delBanner(int storeId, String goodsIds, String str) throws LaiKeAPIException;

    /**
     * 重新编辑商品
     * 【php $product->re_edit】
     * 调用 addProduct
     *
     * @param vo        -
     * @param adminName -
     * @param mchId     -
     * @param type      -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/20 9:53
     */
    boolean editProduct(UploadMerchandiseVo vo, String adminName, int mchId, int type) throws LaiKeAPIException;


    /**
     * 查询
     * 【php】$product->chaxun($value->id,$store_id);
     *
     * @param id      - 商品id
     * @param storeId -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/28 16:58
     */
    Integer chaxun(int id, int storeId) throws LaiKeAPIException;


    /**
     * 获取商品设置
     * 【php PC_Tools.getProductSettings】
     *
     * @param storeId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/22 14:09
     */
    List<Integer> getProductSettings(int storeId) throws LaiKeAPIException;

    /**
     * 创建默认的分类与品牌
     *
     * @param storeId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/11/2 10:55
     */
    void builderDefaultClassBrand(int storeId) throws LaiKeAPIException;

    /**
     * 获取商品标签集合
     *
     * @param storeId -
     * @param labelId -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/11/22 14:58
     */
    List<Map<String, String>> getGoodsLabelList(int storeId, List<String> labelId) throws LaiKeAPIException;

    /**
     * 获取商品运费
     *
     * @param loginUser -
     * @param goodsId   -
     * @return BigDecimal
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/30 17:20
     */
    BigDecimal getGoodsFreight(User loginUser, int goodsId) throws LaiKeAPIException;


    /**
     * 获取产品分类和品牌的下拉框
     *
     * @param storeId
     * @param classId
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> dropDownProClassAndBrand(Integer storeId, Integer classId) throws LaiKeAPIException;

    /**
     * 批量设置商品位置
     *
     * @param vo
     * @param goodsIds
     * @param status   位置信息
     */
    void batchSelectionOfLocations(MainVo vo, String goodsIds, String status);

    /**
     * 批量预警
     *
     * @param vo
     * @param goodsIds
     * @param minInventory
     * @return
     */
    void batchWarning(MainVo vo, String goodsIds, Integer minInventory);

    /**
     * 批量设置运费模板
     *
     * @param vo
     * @param goodsIds
     * @param fid
     * @return
     */
    void batchSetShippingFees(MainVo vo, String goodsIds, Integer fid);
}
