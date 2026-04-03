package com.laiketui.admins.api.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.goods.BrandClassVo;
import com.laiketui.domain.vo.goods.GoodsClassVo;
import com.laiketui.domain.vo.goods.StockInfoVo;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.supplier.GoodsQueryVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 15:56 2022/9/21
 */
public interface SupplierGoodsService
{

    /**
     * 查询商品池
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> proList(GoodsQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 添加商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> addGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;

    /**
     * 断供/起售
     *
     * @param vo
     * @param goodId
     * @throws LaiKeAPIException
     */
    void operation(MainVo vo, Integer goodId) throws LaiKeAPIException;

    /**
     * 删除
     *
     * @param vo
     * @param goodId
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, String goodId) throws LaiKeAPIException;

    /**
     * 根据id获取商品信息
     *
     * @param vo     -
     * @param goodId -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, int goodId) throws LaiKeAPIException;

    /**
     * 提交/撤销申请
     *
     * @param vo
     * @param goodId
     * @throws LaiKeAPIException
     */
    void submitRevoke(MainVo vo, String goodId) throws LaiKeAPIException;

    /**
     * 查询类别
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getClassInfo(GoodsClassVo vo) throws LaiKeAPIException;

    /**
     * 获取公共分类
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getPublicClass(GoodsClassVo vo) throws LaiKeAPIException;

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
     */
    void addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId) throws LaiKeAPIException;

    /**
     * 删除当前类别
     *
     * @param vo      -
     * @param classId -
     * @throws LaiKeAPIException -
     */
    void delClass(MainVo vo, int classId) throws LaiKeAPIException;

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
     * 查询品牌
     *
     * @param vo
     * @param brandName
     * @param status
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getBrandInfo(MainVo vo, String brandName, Integer status) throws LaiKeAPIException;

    /**
     * 增加/修改品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    void addBrand(BrandClassVo vo) throws LaiKeAPIException;

    /**
     * 删除品牌
     *
     * @param vo      -
     * @param brandId -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    void delBrand(MainVo vo, int brandId) throws LaiKeAPIException;

    /**
     * 查询运费
     *
     * @param vo     -
     * @param fid    -
     * @param status -
     * @param mchId  -
     * @param name   -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, Integer otype, String name) throws LaiKeAPIException;

    /**
     * 运费设置默认开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException -
     */
    void freightSetDefault(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 增加/修改运费
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    boolean addFreight(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 删除运费
     *
     * @param vo         -
     * @param freightIds -
     * @throws LaiKeAPIException -
     */
    void delFreight(MainVo vo, String freightIds) throws LaiKeAPIException;

    /**
     * 城市
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> cityInfo(MainVo vo) throws LaiKeAPIException;

    /**
     * 查看关联店铺
     *
     * @param vo
     * @param mchName
     * @param startTime
     * @param endTime
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> relationMch(MainVo vo, String mchName, String startTime, String endTime) throws LaiKeAPIException;

    /**
     * 关联店铺查看商品
     *
     * @param vo
     * @param mchId
     * @param proName
     * @param status
     * @param startTime
     * @param endTime
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> mchPro(MainVo vo, Integer mchId, String proName, Integer status, String startTime, String endTime) throws LaiKeAPIException;

    /**
     * 获取库存信息列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 10:53
     */
    Map<String, Object> getStockInfo(StockInfoVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取库存详细信息列表
     *
     * @param vo     -
     * @param attrId - 属性id
     * @param pid    - 商品id
     * @param type   - 类型 0.入库 1.出库 2.预警
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 10:53
     */
    Map<String, Object> getStockDetailInfo(StockInfoVo vo, Integer attrId, Integer pid, Integer type, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 增加库存
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 14:21
     */
    Map<String, Object> addStock(AddStockVo vo) throws LaiKeAPIException;
}
