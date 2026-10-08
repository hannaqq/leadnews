package com.news.app.gateway.filter;

import com.news.app.gateway.util.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        if (path.startsWith("/user/api/v1/creator-applications/admin/")) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }
        if(path.equals("/user/api/v1/login/login_auth") || path.equals("/actuator/health")){
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("token");

        if(StringUtils.isBlank(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();

        }

        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            int result = AppJwtUtil.verifyToken(claimsBody);
            if(result == 1 || result == 2 || !"app".equals(claimsBody.getAudience())){
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            Object userId = claimsBody.get("id");
            ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                httpHeaders.set("userId", userId + "");
            }).build();
            exchange.mutate().request(serverHttpRequest);

        }catch (Exception e){
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
