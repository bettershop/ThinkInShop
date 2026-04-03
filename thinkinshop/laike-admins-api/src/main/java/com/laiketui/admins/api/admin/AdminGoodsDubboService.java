package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.GoodsClassVo;
import com.laiketui.domain.vo.goods.GoodsConfigureVo;
import com.laiketui.domain.vo.mch.ApplyShopVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 后台添加
 *
 * @author Trick
 * @date 2020/12/28 17:27
 */
public interface AdminGoodsDubboService
{

    /**
     * 获取分类及品牌
     * 【php Ajax->getDefaultView 】
     *
     * @param vo      -
     * @param classId -
     * @param brandId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 10:28
     */
    Map<String, Object> getClassifiedBrands(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException;


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
     * 获取商品列表-规格
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/3 16:59
     */
    Map<String, Object> getGoodsConfigureList(GoodsConfigureVo vo) throws LaiKeAPIException;


    /**
     * 获取属性值
     *
     * @param vo         -
     * @param attributes -
     * @param attrId     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 17:29
     */
    Map<String, Object> getAttributeValue(MainVo vo, String attributes, Integer attrId) throws LaiKeAPIException;

    /**
     * 添加自营店铺 【初始化商城会用到】
     * 【php AddAction.mch】
     *
     * @param vo   -
     * @param logo -
     * @return Map -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/28 17:32
     */
    Map<String, Object> addMch(ApplyShopVo vo, String logo) throws LaiKeAPIException;


    /**
     * 加载添加商品页面数据
     * 【php AddAction.getDefaultView】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 10:02
     */
    Map<String, Object> getAddPage(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加商品
     * 【php AddAction.execute】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 10:12
     */
    Map<String, Object> addGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;

    Map<String, Object> addGoods1(UploadMerchandiseVo vo) throws LaiKeAPIException;

    /**
     * 批量上传商品
     *
     * @param vo             -
     * @param image          -
     * @param productClassId -
     * @param brandId        -
     * @param freightId      -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/4 20:41
     */
    Boolean uploadAddGoods(MainVo vo, List<MultipartFile> image, String productClassId, String brandId, String freightId) throws LaiKeAPIException;

    /**
     * 删除上传记录
     *
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/8 21:04
     */
    void delUploadRecord(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 批量上传记录列表
     *
     * @param vo        -
     * @param key       -
     * @param status    -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/5 17:04
     */
    Map<String, Object> uploadRecordList(MainVo vo, String key, Integer status, String startDate, String endDate) throws LaiKeAPIException;

    /**
     * 编辑商品序号
     *
     * @param vo   -
     * @param id   -
     * @param sort -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 17:34
     */
    void editSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException;

    /**
     * 根据id获取商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 14:12
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, int goodsId) throws LaiKeAPIException;


    /**
     * 后台删除商品
     *
     * @param vo  -
     * @param pId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 14:35
     */
    boolean delGoodsById(MainVo vo, String pId) throws LaiKeAPIException;


    /**
     * 上下架商品
     *
     * @param vo       -
     * @param goodsIds -
     * @param status   -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 17:15
     */
    void upperAndLowerShelves(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException;


    /**
     * 商品置顶
     *
     * @param vo      -
     * @param goodsId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 17:43
     */
    boolean goodsByTop(MainVo vo, int goodsId) throws LaiKeAPIException;


    /**
     * 商品位置移动
     *
     * @param vo             -
     * @param currentGoodsId - 当前商品id
     * @param moveGoodsId    - 需要移动的商品id
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 17:53
     */
    boolean goodsMovePosition(MainVo vo, int currentGoodsId, int moveGoodsId) throws LaiKeAPIException;


    /**
     * 查询类别
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 10:12
     */
    Map<String, Object> getClassInfo(GoodsClassVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 批量设置商品位置
     *
     * @param vo
     * @param goodsIds
     * @param status
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

    /**
     * 获取店铺运费列表
     *
     * @param vo
     * @param goodsIds
     * @return
     */
    Map<String, Object> batchObtainShippingFees(MainVo vo, String goodsIds);


    interface ClassType
    {
        /**
         * 查询下级
         */
        Integer SUBORDINATE = 1;
        /**
         * 查询上级
         */
        Integer SUPERIOR    = 2;
        /**
         * 根据id查询
         */
        Integer ID          = 3;
        /**
         * 查询第一级
         */
        Integer FIRST_STAGE = 0;
    }


    /**
     * 获取当前类别所有上级
     *
     * @param vo      -
     * @param classId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 13:55
     */
    Map<String, Object> getClassLevelTopAllInfo(MainVo vo, int classId) throws LaiKeAPIException;

    /**
     * 删除当前类别
     *
     * @param vo      -
     * @param classId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 14:34
     */
    void delClass(MainVo vo, int classId) throws LaiKeAPIException;


    /**
     * 添加商品类别
     *
     * @param vo        -
     * @param classId   -
     * @param className -
     * @param ename     -
     * @param img       -
     * @param level     -
     * @param fatherId  -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 15:31
     */
    void addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId) throws LaiKeAPIException;

//    @Deprecated
//    int addClass1(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId) throws LaiKeAPIException;


    /**
     * 类别置顶
     *
     * @param vo      -
     * @param classId -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/30 16:42
     */
    boolean classSortTop(MainVo vo, Integer classId) throws LaiKeAPIException;
}
