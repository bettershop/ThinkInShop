package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.goods.BrandClassVo;
import com.laiketui.domain.vo.goods.GoodsClassVo;
import com.laiketui.domain.vo.goods.StockInfoVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.mch.pc.DelBrandVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 商品管理
 *
 * @author Trick
 * @date 2021/5/31 15:55
 */
public interface MchGoodsService
{

    /**
     * 商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/31 16:32
     */
    Map<String, Object> index(DefaultViewVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 代售可选商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/01/29 11:32
     */
    Map<String, Object> consignmentPro(DefaultViewVo vo) throws LaiKeAPIException;

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
    void addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId,Integer type) throws LaiKeAPIException;

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

    /**
     * 查询品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    Map<String, Object> getBrandInfo(BrandClassVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 增加/修改品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    boolean addBrand(BrandClassVo vo) throws LaiKeAPIException;

    /**
     * 查询审核列表
     *
     * @param vo
     * @param condition
     * @param status
     * @param startTime
     * @param endTime
     * @param level
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> classAuditList(MainVo vo, String condition, Integer status, String startTime, String endTime,Integer level) throws LaiKeAPIException;

    /**
     * 查询审核列表
     *
     * @param vo
     * @param condition
     * @param status
     * @param startTime
     * @param endTime
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> brandAuditList(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime) throws LaiKeAPIException;

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
     * 获取国家列表
     *
     * @param vo -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 10:28
     */
    List<CountryModel> getCountry(MainVo vo) throws LaiKeAPIException;

    /**
     * pc店铺端删除品牌
     *
     * @param vo
     */
    void delBrand(DelBrandVo vo);

    /**
     * pc店铺端删除分类
     *
     * @param vo
     * @param classId 分离id
     */
    void delClass(MainVo vo, int classId);

    /**
     * 获取库存详情信息
     * @param vo
     * @param attrId
     * @param pid
     * @param type
     * @param response
     * @return
     */
    Map<String, Object> getStockDetailInfo(StockInfoVo vo, Integer attrId, Integer pid, Integer type, HttpServletResponse response);


    /**
     * 库存预警添加库存
     * @param vo
     */
    void addInventory(AddStockVo vo);

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

    interface CommodityType
    {
        /**
         * 当前店铺自选商品
         */
        Integer STORE_OPTIONAL = -2;
        /**
         * 实物商品 - 获取所有商品
         */
        Integer PHYSICAL_GOODS = 1;
        /**
         * 虚拟商品
         */
        Integer VIRTUAL_GOODS  = 2;
        /**
         * 自选
         */
        Integer OPTIONAL       = 3;
        /**
         * 待审核
         */
        Integer TO_BE_REVIEWED = 4;
        /**
         * 审核失败
         */
        Integer AUDIT_FAILED   = 5;
    }

    /**
     * 获取店铺商品配置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/7 11:36
     */
    Map<String, Object> getCommoditySetup(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取商品类别
     *
     * @param vo      -
     * @param classId -
     * @param brandId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/31 17:21
     */
    Map<String, Object> getClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException;

    /**
     * 获取商品列表
     *
     * @param vo   -
     * @param name -
     * @param id   -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021-12-07 16:14:36
     */
    Map<String, Object> getGoodsLabel(MainVo vo, String name, Integer id) throws LaiKeAPIException;

    /**
     * 获取属性名称
     *
     * @param vo         -
     * @param attributes -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/31 17:21
     */
    Map<String, Object> getAttributeName(MainVo vo, String attributes) throws LaiKeAPIException;


    /**
     * 获取属性值
     *
     * @param vo            -
     * @param attributesIds -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/31 17:21
     */
    Map<String, Object> getAttributeValue(MainVo vo, String attributesIds) throws LaiKeAPIException;


    /**
     * 获取商品规格列表
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/3 14:27
     */
    Map<String, Object> getAttrByGoodsId(MainVo vo, Integer goodsId) throws LaiKeAPIException;

    /**
     * 获取添加商品页面数据
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/29 10:02
     */
    Map<String, Object> getAddPage(MainVo vo) throws LaiKeAPIException;


    /**
     * 上传商品
     *
     * @param vo -
     * @return vo
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/1 13:21
     */
    Map<String, Object> addGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;

    /**
     * 添加自选商品
     *
     * @param vo       -
     * @param goodsIds -
     * @param yunFeiId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/7 20:30
     */
    void addZxGoods(MainVo vo, String goodsIds, Integer yunFeiId) throws LaiKeAPIException;

    /**
     * 编辑商品序号
     *
     * @param vo   -
     * @param id   -
     * @param sort -
     * @throws LaiKeAPIException-
     * @author sunH
     * @date 2023/11/14 17:34
     */
    void editSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException;


    /**
     * 根据id获取商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @param isZx    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/1 15:01
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, int goodsId, boolean isZx) throws LaiKeAPIException;


    /**
     * 上下架商品
     *
     * @param vo       -
     * @param goodsIds -
     * @param status   -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/1 16:18
     */
    boolean upperAndLowerShelves(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException;


    /**
     * 添加库存页面数据
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/1 16:27
     */
    Map<String, Object> stockPage(MainVo vo, int goodsId) throws LaiKeAPIException;


    /**
     * 增加库存
     *
     * @param vo    -
     * @param stock -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/1 18:24
     */
    Map<String, Object> addStock(MainVo vo, String stock) throws LaiKeAPIException;


    /**
     * 根据商品id删除商品
     *
     * @param vo       -
     * @param goodsIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 9:18
     */
    void delGoodsById(MainVo vo, String goodsIds) throws LaiKeAPIException;


    /**
     * 获取商品审核列表
     *
     * @param vo           -
     * @param classId      -
     * @param brandId      -
     * @param examinStatus -  2=审核中  3=审核未通过  4=待审核
     * @param goodsName    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 10:14
     */
    Map<String, Object> getGoodsExamineInfo(MainVo vo, Integer classId, Integer brandId, Integer examinStatus, String goodsName, Integer goodsId) throws LaiKeAPIException;


    /**
     * 提交审核/撤销审核
     *
     * @param vo   -
     * @param pIds -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 11:04
     */
    Map<String, Object> submitAudit(MainVo vo, String pIds) throws LaiKeAPIException;


    /**
     * 上传图片
     *
     * @param vo    -
     * @param image -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 11:16
     */
    Map<String, Object> uploadImgs(MainVo vo, MultipartFile image) throws LaiKeAPIException;
}
