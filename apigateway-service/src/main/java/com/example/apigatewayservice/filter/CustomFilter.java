package com.example.apigatewayservice.filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        // Custom Pre Filter
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Custom Pre filter : request id -> {}", request.getId());

            // Custom Post Filter
            return chain.filter(exchange)
                    // WebFlux 라는 비동기방식 서버지원시 Mono타입으로 전달
                    .then(Mono.fromRunnable(() -> {
                        log.info("post filter... : {} ", response.getStatusCode());
                    }));
        });
    }
    public static class Config {
        // config 정보가 있으면 여기에 config 정보를 넣을 수 있다.
    }
}
