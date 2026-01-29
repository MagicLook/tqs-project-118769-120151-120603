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

    public MetricsFilter(MeterRegistry registry) {
        // Overall counters (RNF 4)
        this.totalRequests = Counter.builder("http.requests.total")
                .description("Total number of HTTP requests")
                .register(registry);

        this.successfulRequests = Counter.builder("http.requests.success")
                .description("Number of successful HTTP requests (2xx, 3xx)")
                .register(registry);

        this.failedRequests = Counter.builder("http.requests.failure")
                .description("Number of failed HTTP requests (4xx, 5xx)")
                .register(registry);

        // Reservation domain counters (RNF 1)
        this.reservationSuccess = Counter.builder("http.requests.success")
                .tag("domain", "reservation")
                .description("Successful reservation requests")
                .register(registry);

        this.reservationFailure = Counter.builder("http.requests.failure")
                .tag("domain", "reservation")
                .description("Failed reservation requests")
                .register(registry);

        // Staff management domain counters (RNF 2)
        this.staffSuccess = Counter.builder("http.requests.success")
                .tag("domain", "staff")
                .description("Successful staff management requests")
                .register(registry);

        this.staffFailure = Counter.builder("http.requests.failure")
                .tag("domain", "staff")
                .description("Failed staff management requests")
                .register(registry);

        // Catalog domain counters (RNF 3)
        this.catalogSuccess = Counter.builder("http.requests.success")
                .tag("domain", "catalog")
                .description("Successful catalog requests")
                .register(registry);

        this.catalogFailure = Counter.builder("http.requests.failure")
                .tag("domain", "catalog")
                .description("Failed catalog requests")
                .register(registry);

        // Payload size distribution summaries
        this.requestSize = DistributionSummary.builder("http.request.size")
                .description("Size of HTTP request payloads in bytes")
                .baseUnit("bytes")
                .register(registry);
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
            // Count total requests
            totalRequests.increment();

            // Track response size (if available)
            // Note: This may not always be available depending on response type

            // Determine success/failure based on status code
            int status = httpResponse.getStatus();
            boolean isSuccess = status >= 200 && status < 400;

            // Update overall counters
            if (isSuccess) {
                successfulRequests.increment();
            } else {
                failedRequests.increment();
            }

            // Update domain-specific counters
            String uri = httpRequest.getRequestURI();
            updateDomainCounters(uri, isSuccess);
        }
    }

    /**
     * Categorizes the request URI and updates the appropriate domain counters.
     */
    private void updateDomainCounters(String uri, boolean isSuccess) {
        if (uri == null)
            return;

        // Reservation domain: booking and my-bookings endpoints
        if (uri.contains("/booking") || uri.contains("/my-bookings")) {
            if (isSuccess) {
                reservationSuccess.increment();
            } else {
                reservationFailure.increment();
            }
        }
        // Staff management domain
        else if (uri.contains("/staff")) {
            if (isSuccess) {
                staffSuccess.increment();
            } else {
                staffFailure.increment();
            }
        }
        // Catalog domain: items and dashboard
        else if (uri.contains("/items") || uri.contains("/dashboard")) {
            if (isSuccess) {
                catalogSuccess.increment();
            } else {
                catalogFailure.increment();
            }
        }
    }
}
