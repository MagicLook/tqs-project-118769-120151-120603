package com.magiclook.config;

import java.io.IOException;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet Filter that tracks metrics for SLO monitoring:
 * - Success/failure counters per domain (reservation, staff, catalog)
 * - Request/response payload sizes
 * - Overall success rate (RNF 4: 99%)
 */
@Component
public class MetricsFilter implements Filter {

    // Metric name constants to avoid duplication
    private static final String METRIC_SUCCESS = "http.requests.success";
    private static final String METRIC_FAILURE = "http.requests.failure";
    private static final String TAG_DOMAIN = "domain";
    private static final String DOMAIN_RESERVATION = "reservation";
    private static final String DOMAIN_CATALOG = "catalog";
    private static final String DOMAIN_STAFF = "staff";

    // Overall counters for RNF 4 (99% success rate)
    private final Counter totalRequests;
    private final Counter successfulRequests;
    private final Counter failedRequests;

    // Categorized counters for each SLO domain
    private final Counter reservationSuccess;
    private final Counter reservationFailure;
    private final Counter staffSuccess;
    private final Counter staffFailure;
    private final Counter catalogSuccess;
    private final Counter catalogFailure;

    // Payload size tracking
    private final DistributionSummary requestSize;

    // MeterRegistry for dynamic counters
    private final MeterRegistry meterRegistry;

    public MetricsFilter(MeterRegistry registry) {
        // Overall counters (RNF 4)
        this.totalRequests = Counter.builder("http.requests.overall")
                .description("Total number of HTTP requests")
                .register(registry);

        this.successfulRequests = Counter.builder("http.requests.overall.success")
                .description("Number of successful HTTP requests (2xx, 3xx)")
                .register(registry);

        this.failedRequests = Counter.builder("http.requests.overall.failure")
                .description("Number of failed HTTP requests (4xx, 5xx)")
                .register(registry);

        // Reservation domain counters (RNF 1)
        this.reservationSuccess = Counter.builder(METRIC_SUCCESS)
                .tag(TAG_DOMAIN, DOMAIN_RESERVATION)
                .description("Successful reservation requests")
                .register(registry);

        this.reservationFailure = Counter.builder(METRIC_FAILURE)
                .tag(TAG_DOMAIN, DOMAIN_RESERVATION)
                .description("Failed reservation requests")
                .register(registry);

        this.staffSuccess = Counter.builder(METRIC_SUCCESS)
                .tag(TAG_DOMAIN, DOMAIN_STAFF)
                .description("Successful staff management requests")
                .register(registry);

        this.staffFailure = Counter.builder(METRIC_FAILURE)
                .tag(TAG_DOMAIN, DOMAIN_STAFF)
                .description("Failed staff management requests")
                .register(registry);

        this.catalogSuccess = Counter.builder(METRIC_SUCCESS)
                .tag(TAG_DOMAIN, DOMAIN_CATALOG)
                .description("Successful catalog requests")
                .register(registry);

        this.catalogFailure = Counter.builder(METRIC_FAILURE)
                .tag(TAG_DOMAIN, DOMAIN_CATALOG)
                .description("Failed catalog requests")
                .register(registry);

        this.requestSize = DistributionSummary.builder("http.request.size")
                .description("Size of HTTP request payloads in bytes")
                .baseUnit("bytes")
                .register(registry);

        // Store registry for dynamic counters
        this.meterRegistry = registry;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Track request size
        int contentLength = httpRequest.getContentLength();
        if (contentLength > 0) {
            requestSize.record(contentLength);
        }

        try {
            // Process the request
            chain.doFilter(request, response);
        } finally {
            String uri = httpRequest.getRequestURI();

            if (!shouldSkipMetrics(uri)) {
                totalRequests.increment();
                int status = httpResponse.getStatus();
                boolean isSuccess = status >= 200 && status < 400;

                // Update overall counters
                if (isSuccess) {
                    successfulRequests.increment();
                } else {
                    failedRequests.increment();
                }

                // Update domain-specific counters
                String domain = getDomain(uri);
                updateDomainCounters(domain, isSuccess);

                // Track errors with endpoint, status code, and domain
                if (!isSuccess) {
                    String endpoint = normalizeEndpoint(uri);
                    Counter.builder("http.errors")
                            .tag("endpoint", endpoint)
                            .tag("status", String.valueOf(status))
                            .tag(TAG_DOMAIN, domain != null ? domain : "other")
                            .description("HTTP errors by endpoint and status")
                            .register(meterRegistry)
                            .increment();
                }
            }
        }
    }

    private String normalizeEndpoint(String uri) {
        if (uri == null)
            return "unknown";
        return uri.replaceAll("/\\d+", "/{id}");
    }

    private boolean shouldSkipMetrics(String uri) {
        if (uri == null)
            return true;

        return uri.startsWith("/actuator")
                // Static resources directories
                || uri.startsWith("/css") || uri.startsWith("/js")
                || uri.startsWith("/images") || uri.startsWith("/fonts")
                || uri.startsWith("/static") || uri.startsWith("/webjars")
                // Special pages
                || uri.equals("/favicon.ico") || uri.startsWith("/error")
                // Static file extensions
                || uri.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot|map)$");
    }

    private String getDomain(String uri) {
        if (uri == null)
            return null;
        if (uri.contains("/booking") || uri.contains("/my-bookings")) {
            return DOMAIN_RESERVATION;
        } else if (uri.contains("/staff")) {
            return DOMAIN_STAFF;
        } else if (uri.contains("/items") || uri.contains("/dashboard")) {
            return DOMAIN_CATALOG;
        }
        return null;
    }

    private void updateDomainCounters(String domain, boolean isSuccess) {
        if (domain == null)
            return;

        switch (domain) {
            case DOMAIN_RESERVATION:
                if (isSuccess) {
                    reservationSuccess.increment();
                } else {
                    reservationFailure.increment();
                }
                break;
            case DOMAIN_STAFF:
                if (isSuccess) {
                    staffSuccess.increment();
                } else {
                    staffFailure.increment();
                }
                break;
            case DOMAIN_CATALOG:
                if (isSuccess) {
                    catalogSuccess.increment();
                } else {
                    catalogFailure.increment();
                }
                break;
            default:
                break;
        }
    }
}