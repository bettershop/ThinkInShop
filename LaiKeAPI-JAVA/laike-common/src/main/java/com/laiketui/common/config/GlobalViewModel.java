package com.laiketui.common.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalViewModel
{

    @ModelAttribute("ctx")
    public String ctx(HttpServletRequest request)
    {
        return request.getContextPath();
    }

    @ModelAttribute("loginUser")
    public Object loginUser(HttpServletRequest request)
    {
        return request.getAttribute("XXL_JOB_LOGIN_IDENTITY");
    }
}

