package com.trading.apigateway.filter;

import org.springframework.stereotype.Component;

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

        if(!request.getHeaders().containsKey("Authorization")) {
            log.warn("Authorization header header : {}", path);

            return unauthorizedResponse(exchange, "Missing Authorization header");
        }

        String authHeader = request.getHeaders().getFirst("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Invalid Authorization format");
        }

        String token = authHeader.substring(7);
        try{
            Claims claim = extarctClaims(token);
            String userId = claims.get("userId", String.class);
            Strign email  = claims.getSubject();

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
