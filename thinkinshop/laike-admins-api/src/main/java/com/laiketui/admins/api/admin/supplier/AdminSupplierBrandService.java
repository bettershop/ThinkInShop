package com.laiketui.admins.api.admin.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 15:08 2022/9/19
 */
public interface AdminSupplierBrandService
{

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
    Map<String, Object> auditList(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime) throws LaiKeAPIException;

    /**
     * 审核
     *
     * @param vo
     * @param id
     * @param status 审核状态 0.待审核 1.审核通过 2.不通过
     * @param remark
     * @throws LaiKeAPIException
     */
    void examine(MainVo vo, Integer id, Integer status, String remark) throws LaiKeAPIException;
}
