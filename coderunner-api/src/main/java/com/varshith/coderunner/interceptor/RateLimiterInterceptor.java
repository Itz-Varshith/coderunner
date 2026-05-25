package com.varshith.coderunner.interceptor;

import com.varshith.coderunner.service.RatelimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/*
* Interceptor class for rate limiter, this class intercepts every request to select end points and returns false if the corresponding service does not accept the user.
* */

@Component
@RequiredArgsConstructor
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final RatelimiterService ratelimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String ip = request.getRemoteAddr();

        if (!ratelimiterService.checkRequest(ip)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return false;
        }

        return true;
    }
}
