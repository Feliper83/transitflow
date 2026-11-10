package com.transitflow.gateway;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Stamps a fresh correlation id on every inbound request that doesn't already
 * carry one, so a single booking can be traced across all downstream services.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            ServerHttpRequest mutated = request.mutate()
                    .header(HEADER, correlationId)
                    .build();
            exchange = exchange.mutate().request(mutated).build();
        }
        exchange.getResponse().getHeaders().add(HEADER, correlationId);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
