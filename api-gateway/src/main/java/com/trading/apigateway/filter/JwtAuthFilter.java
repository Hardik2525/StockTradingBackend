package com.trading.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            // skip JWT for public endpoints

            if(isPublicEndpoint(path)) {
                log.debug("Public endpoint - skipping JWT : {}", path);
                return chain.filter(exchange);
            }

            String authorization = request.getHeaders().getFirst("Authorization");

            if (authorization == null || authorization.isBlank()) {
                log.warn("Authorization header missing: {}", path);
                return unauthorizedResponse(exchange, "Missing Authorization header");
            }

            String authHeader = request.getHeaders().getFirst("Authorization");

            if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse(exchange, "Invalid Authorization format");
            }

            String token = authHeader.substring(7);
            try{
                Claims claims = extractClaims(token);
                String userId = claims.get("userId", String.class);
                String email  = claims.getSubject();

                log.debug("User {} authenticated with email {}", userId, email);

                ServerHttpRequest modifiedRequest = request.mutate().header("X-User-Id", userId).build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
            catch(Exception e){
                log.error("Error authenticating user : {}", e.getMessage());
                return unauthorizedResponse(exchange, "Invalid token or expired token");
            }
        };
    }

    private Claims extractClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized access to : {}", exchange.getRequest().getPath());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/register") || path.startsWith("/health") || path.startsWith("/ws") || path.startsWith("/login");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private boolean enabled = true;
    }
}
