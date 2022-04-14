package com.example.apigatewayservice.filter;

import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    @Override
    public GatewayFilter apply(Config config) {

        // Custom Pre Filter
        return ((exchange, chain) -> {
            // API 호출시 헤더에 받았던 토큰을 전달하는 작업을 한다.
            // 토큰이 제대로 왔는지 등등에 대한 검증 후 완료됨을 반환.
            ServerHttpRequest request = exchange.getRequest();

            // 인증이 제대로 안된 상태일 경우
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            log.info("request.getHeaders().get(HttpHeaders.AUTHORIZATION) : {}", request.getHeaders().get(HttpHeaders.AUTHORIZATION));
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION)
                    .get(0); // Bearer Token이 있다.

            String jwt = authorizationHeader.replace("Bearer", "");
            log.info("jwt : {}", jwt);
            if(!isJwtValid(jwt)) {
                return onError(exchange, "JWT token is not Valid", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        });
    }
    // Mono, Flux -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange,
                               String err,
                               HttpStatus httpStatus) {
        // Spring WebFlux 에서는 Servlet을 쓰지 않는다.
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(err);
        return response.setComplete();
    }

    private boolean isJwtValid(String jwt) {
        String subject = null;
        try {
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(jwt).getBody()
                    .getSubject();
            log.info("subject : {}", subject);
        } catch (Exception ex) {
            return false;
        }

        if(subject == null || subject.isEmpty()) {
            return false;
        }

        return true;
    }

    public static class Config {

    }
}
