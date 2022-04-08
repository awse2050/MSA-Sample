package com.example.apigatewayservice.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Log4j2
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        // Custom Pre Filter
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global Pre filter : baseMessage -> {}", config.getBaseMessage());

            if(config.isPreLogger()) {
                log.info("Global Filter Start request Id: {}", request.getId());
            }

            // Custom Post Filter
            return chain.filter(exchange)
                    // WebFlux 라는 비동기방식 서버지원시 Mono타입으로 전달
                    .then(Mono.fromRunnable(() -> {
                        if(config.isPostLogger()) {
                            log.info("Global Post filter... : {} ", response.getStatusCode());
                        }
                    }));
        });
    }

    @Getter
    @Setter // 필요
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
        // config 정보가 있으면 여기에 config 정보를 넣을 수 있다.
    }
}
