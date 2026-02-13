package com.algoverse.platform.interceptor;

import com.algoverse.platform.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {
        String ip = request.getRemoteAddr();
        String key = ip; // Default to IP

        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            key = principal.getName(); // Use User ID if authenticated
        }

        boolean allowed;
        String uri = request.getRequestURI();

        if (uri.contains("/api/ai/generate")) {
            allowed = rateLimitService.allowAiGeneration(key);
        } else if (uri.contains("/api/ai/analyze")) {
            allowed = rateLimitService.allowAiAnalysis(key);
        } else {
            allowed = rateLimitService.allowRequest(key);
        }

        if (allowed) {
            return true;
        } else {
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", "60");
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "You have exhausted your API Request Quota");
            return false;
        }
    }
}
