package com.laiketui.cdc.services.openapi.user;

import com.laiketui.cdc.openapi.user.UserService;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.AddUserVo;
import com.laiketui.domain.vo.user.UpdateUserVo;
import com.laiketui.domain.vo.user.UserVo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl  {

    
    public Map<String, Object> getUserInfo(UserVo vo, HttpServletResponse response) throws LaiKeAPIException {
        return null;
    }

    
    public boolean updateUserById(UpdateUserVo vo) throws LaiKeAPIException {
        return false;
    }

    
    public Map<String, Object> getUserGradeType(MainVo vo) throws LaiKeAPIException {
        return null;
    }

    
    public boolean userRechargeMoney(MainVo vo, int id, BigDecimal money, Integer type, String remake) throws LaiKeAPIException {
        return false;
    }

    
    public boolean delUserById(MainVo vo, int id) throws LaiKeAPIException {
        return false;
    }

    
    public void saveUser(AddUserVo vo) throws LaiKeAPIException {

    }

    
    public void uploadAddUser(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException {

    }

    
    public void delUploadRecord(MainVo vo, String id) throws LaiKeAPIException {

    }

    
    public Map<String, Object> uploadRecordList(MainVo vo, String key, Integer status, String startDate, String endDate) throws LaiKeAPIException {
        return null;
    }

}
