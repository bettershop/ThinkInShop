package com.xxl.job.admin.controller.interceptor;

import com.laiketui.core.annotation.InterceptorConfig;
import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.service.LoginService;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限拦截
 *
 * @author xuxueli 2015-12-12 18:09:04
 */
@InterceptorConfig(
		includePatterns = "/**",
		excludePatterns = {
				"/toLogin",
				"/login",
				"/logout",
				"/static/**",
				"/swagger-ui.html",
				"/swagger-resources/**",
				"/webjars/**",
				"/v2/api-docs"
		},
		order = 10
)
@Component
public class PermissionInterceptor extends HandlerInterceptorAdapter {

	@Resource
	private LoginService loginService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (!(handler instanceof HandlerMethod)) {
			return super.preHandle(request, response, handler);
		}

		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();

		// 👇 新增：自动放行业务任务接口（不需登录）
		if (uri.startsWith(contextPath + "/plugin") ||
				uri.startsWith(contextPath + "/comps")||
				uri.startsWith(contextPath + "/admin")||
				uri.startsWith(contextPath + "/saas")||
				uri.startsWith(contextPath + "/pc")||
				uri.startsWith(contextPath + "/gw")||
				uri.startsWith(contextPath + "/app")) {
			return super.preHandle(request, response, handler);
		}

		// 原有权限逻辑（保持不变）
		boolean needLogin = true;
		boolean needAdminuser = false;
		HandlerMethod method = (HandlerMethod) handler;
		PermissionLimit permission = method.getMethodAnnotation(PermissionLimit.class);
		if (permission != null) {
			needLogin = permission.limit();
			needAdminuser = permission.adminuser();
		}

		if (needLogin) {
			XxlJobUser loginUser = loginService.ifLogin(request, response);
			if (loginUser == null) {
				response.setStatus(302);
				response.setHeader("location", contextPath + "/toLogin");
				return false;
			}
			if (needAdminuser && loginUser.getRole() != 1) {
				throw new RuntimeException(I18nUtil.getString("system_permission_limit"));
			}
			request.setAttribute(LoginService.LOGIN_IDENTITY_KEY, loginUser);
		}

		return super.preHandle(request, response, handler);
	}
}
