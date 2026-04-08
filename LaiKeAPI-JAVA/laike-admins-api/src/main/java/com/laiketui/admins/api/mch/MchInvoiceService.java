package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.AdminInvoiceVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 20:23 2022/7/28
 */
public interface MchInvoiceService
{

    /**
     * 查询发票列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getList(AdminInvoiceVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 查看发票详情
     *
     * @param vo
     * @param id
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getInfo(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 上传发票文件
     *
     * @param vo
     * @param id
     * @param filePath
     * @throws LaiKeAPIException
     */
    void uploadInvoice(MainVo vo, Integer id, String filePath) throws LaiKeAPIException;

    /**
     * 删除发票信息
     *
     * @param vo
     * @param ids
     * @throws LaiKeAPIException
     */
    void delInvoice(MainVo vo, String ids) throws LaiKeAPIException;
}
