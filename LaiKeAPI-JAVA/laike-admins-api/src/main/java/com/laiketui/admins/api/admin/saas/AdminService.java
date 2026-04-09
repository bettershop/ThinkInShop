package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 管理员接口
 *
 * @author Trick
 * @date 2021/2/4 14:24
 */
public interface AdminService
{

    /**
     * 获取管理员基本信息
     *
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 14:25
     */
    Map<String, Object> getAdminInfo(int id) throws LaiKeAPIException;


    /**
     * 修改管理员基本信息
     *
     * @param id    -
     * @param file  -
     * @param nick  -
     * @param sex   -
     * @param phone -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 14:49
     */
    boolean setAdminInfo(int id, MultipartFile file, String nick, int sex, String phone) throws LaiKeAPIException;

    /**
     * 修改密码
     *
     * @param id     -
     * @param pwdOld -
     * @param pwdNew -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 14:27
     */
    boolean resetAdminPwd(int id, String pwdOld, String pwdNew) throws LaiKeAPIException;
}
