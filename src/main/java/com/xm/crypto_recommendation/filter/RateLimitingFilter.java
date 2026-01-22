package com.xm.crypto_recommendation.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple IP-based rate limiting filter.
 *
 * <p>
 * This filter limits the number of incoming HTTP requests per client IP
 * using the Bucket4j token bucket algorithm.
 * </p>
 *
 * <p>
 * The implementation is intentionally lightweight and stateless outside
 * of the in-memory bucket map. It is suitable for single-instance
 * deployments or demonstration purposes.
 * </p>
 *
 * <p>
 * In a production environment with multiple application instances,
 * a distributed bucket implementation (e.g. backed by Redis) would be
 * required to enforce global rate limits consistently.
 * </p>
 */
@Component
public class RateLimitingFilter implements Filter {

    /**
     * In-memory store mapping client IP addresses to rate-limiting buckets.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int REQUESTS_PER_MINUTE = 60;

    /**
     * Applies rate limiting before allowing the request to proceed.
     *
     * <p>
     * If the client exceeds the allowed number of requests, the filter
     * responds with HTTP 429 (Too Many Requests).
     * </p>
     */
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = extractClientIp(httpRequest);

        Bucket bucket = buckets.computeIfAbsent(clientIp, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Too many requests");
        }
    }

    /**
     * Creates a new rate-limiting bucket for a client IP.
     *
     * <p>
     * The bucket allows a fixed number of requests per minute and refills
     * at a constant rate.
     * </p>
     */
    private Bucket createNewBucket(String ip) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(REQUESTS_PER_MINUTE)
                        .refillIntervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    /**
     * Resolves the client IP address from the incoming request.
     *
     * <p>
     * If the request is forwarded through a proxy, the {@code X-Forwarded-For}
     * header is used. Otherwise, the remote address is taken directly.
     * </p>
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0] : request.getRemoteAddr();
    }
}
