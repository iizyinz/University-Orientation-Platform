package com.example.backend.interceptor;

import com.example.backend.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String[] WHITE_LIST = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/captcha",
        "/api/public",
        "/swagger-ui",
        "/swagger-ui/",
        "/swagger-ui/index.html",
        "/v3/api-docs",
        "/v3/api-docs/",
        "/swagger-resources",
        "/webjars"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        for (String path : WHITE_LIST) {
            if (uri.contains(path)) {
                return true;
            }
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "未登录或Token无效");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                sendUnauthorized(response, "Token已过期");
                return false;
            }

            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            Integer userType = jwtUtil.getUserType(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("userType", userType);

            return true;
        } catch (Exception e) {
            sendUnauthorized(response, "Token验证失败");
            return false;
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(result));
    }
}