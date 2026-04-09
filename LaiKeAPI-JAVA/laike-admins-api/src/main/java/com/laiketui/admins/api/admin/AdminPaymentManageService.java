package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 支付管理
 *
 * @author Trick
 * @date 2021/7/15 15:22
 */
public interface AdminPaymentManageService
{

    /**
     * 支付管理列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/15 15:23
     */
    Map<String, Object> index(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 获取支付参数
     *
     * @param vo    -
     * @param payId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/15 15:42
     */
    Map<String, Object> paymentParmaInfo(MainVo vo, int payId) throws LaiKeAPIException;

    /**
     * 支付参数修改
     *
     * @param vo     -
     * @param json   -
     * @param id     -
     * @param status -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/15 15:23
     */
    void setPaymentParma(MainVo vo, String json, Integer id, Integer status) throws LaiKeAPIException;

    /**
     * 支付配置开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/15 17:19
     */
    void setPaymentSwitch(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 设置默认开关
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void settingDefaultPaytype(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 文件上传
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/15 17:19
     */
    Map<String, Object> uploadCertP12(UploadFileVo vo) throws LaiKeAPIException;

    /**
     * 编辑支付类型信息
     *
     * @param vo     -
     * @param img    -
     * @param remark -
     *               gp
     *               2023-08-23
     */
    void setPayment(MainVo vo, Integer id, String img, String remark) throws LaiKeAPIException;

    /**
     * 编辑支付类型图标上传
     *
     * @param vo
     * @param image
     */
    String setPaymentLoge(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException;
}
