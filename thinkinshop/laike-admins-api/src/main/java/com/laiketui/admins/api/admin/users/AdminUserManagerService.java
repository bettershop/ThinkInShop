package com.laiketui.admins.api.admin.users;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.AddUserVo;
import com.laiketui.domain.vo.user.UpdateUserVo;
import com.laiketui.domain.vo.user.UserVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 会员管理
 *
 * @author Trick
 * @date 2021/1/7 10:59
 */
public interface AdminUserManagerService
{


    /**
     * 加载会员列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 11:07
     */
    Map<String, Object> getUserInfo(UserVo vo, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 修改用户资料
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 16:08
     */
    boolean updateUserById(UpdateUserVo vo) throws LaiKeAPIException;

    /**
     * 获取会员等级类型
     *
     * @param vo -
     * @return LaiKeAPIException
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/22 19:37
     */
    Map<String, Object> getUserGradeType(MainVo vo) throws LaiKeAPIException;

    /**
     * 给用户充值
     *
     * @param vo     -
     * @param id     - 用户主键id
     * @param money  -
     * @param type   - 1=余额充值 2=积分充值 3=
     * @param remake 备注
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 17:09
     */
    boolean userRechargeMoney(MainVo vo, int id, BigDecimal money, Integer type, String remake) throws LaiKeAPIException;


    /**
     * 删除用户
     *
     * @param vo -
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 18:13
     */
    boolean delUserById(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 保存用户
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 10:53
     */
    void saveUser(AddUserVo vo) throws LaiKeAPIException;

    /**
     * 批量上传用户
     *
     * @param vo    -
     * @param image -
     * @throws LaiKeAPIException-
     */
    Boolean uploadAddUser(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException;

    /**
     * 删除上传记录
     *
     * @param id -
     * @throws LaiKeAPIException-
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
     */
    Map<String, Object> uploadRecordList(MainVo vo, String key, Integer status, String startDate, String endDate) throws LaiKeAPIException;

}
