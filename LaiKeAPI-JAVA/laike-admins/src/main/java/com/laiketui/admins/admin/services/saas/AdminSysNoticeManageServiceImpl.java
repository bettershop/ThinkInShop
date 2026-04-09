package com.laiketui.admins.admin.services.saas;

import com.laiketui.admins.api.admin.saas.AdminSysNoticeManageService;
import com.laiketui.common.mapper.SystemTellModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 多商户控制台-公告管理
 *
 * @author Trick
 * @date 2021/2/1 10:27
 */
@Service
public class AdminSysNoticeManageServiceImpl implements AdminSysNoticeManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminSysNoticeManageServiceImpl.class);

    @Autowired
    private SystemTellModelMapper systemTellModelMapper;

}

