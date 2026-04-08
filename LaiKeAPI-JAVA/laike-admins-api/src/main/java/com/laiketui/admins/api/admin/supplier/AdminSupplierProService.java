package com.laiketui.admins.api.admin.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.supplier.GoodsQueryVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 15:50 2022/9/15
 */
public interface AdminSupplierProService
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
     * 审核商品
     *
     * @param vo
     * @param id
     * @param status 审核状态：1.待审核，2.审核通过，3.审核不通过，4.暂不审核
     * @param remark
     * @throws LaiKeAPIException
     */
    void examine(MainVo vo, Integer id, Integer status, String remark) throws LaiKeAPIException;

    /**
     * 删除商品
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void del(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 违规下架
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void violation(MainVo vo, Integer id, String remark, String img) throws LaiKeAPIException;

    /**
     * 修改排序值
     *
     * @param vo
     * @param id
     * @param sort
     * @throws LaiKeAPIException
     */
    void sortUpdate(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException;
}
