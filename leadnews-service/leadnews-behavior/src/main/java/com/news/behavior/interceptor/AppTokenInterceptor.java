package com.news.behavior.interceptor;

import com.news.utils.thread.AppThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AppTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader("userId");
        if (userId == null) {
            return true;
        }
        try {
            AppThreadLocalUtil.setUserId(Integer.valueOf(userId));
            return true;
        } catch (NumberFormatException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        AppThreadLocalUtil.clear();
    }
}
