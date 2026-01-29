# Grafana SLO Queries & Visualization Guide

This document contains all PromQL queries for visualizing your MagicLook SLOs (Service Level Objectives) in Grafana, using the custom `@Timed` metrics.

---

## 📊 Custom Metric Names Reference

| SLO | Metric Name | Controller |
|-----|-------------|------------|
| Reservation (RNF1) | `request_reservation_seconds` | `BookingController` |
| Staff Management (RNF2) | `request_staff_management_seconds` | `StaffController` |
| Catalog (RNF3) | `request_catalog_seconds` | `UserController` |

---

## 🎯 SLO 1: Reservation Latency (RNF1)

**Target:** 98% of reservation operations must respond within 1 second

### Reservation SLO Compliance (Gauge)

**Visualization Type:** `Gauge`  
**Thresholds:** Red < 98, Yellow 98-99, Green ≥ 99

```promql
sum(rate(request_reservation_seconds_bucket{le="1.0"}[5m])) 
/ 
sum(rate(request_reservation_seconds_count[5m])) * 100
```

### SLO Compliance Over Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Percent (0-100)

```promql
sum(rate(request_reservation_seconds_bucket{le="1.0"}[5m])) 
/ 
sum(rate(request_reservation_seconds_count[5m])) * 100
```

### P95 Latency (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Seconds

```promql
histogram_quantile(0.95, sum(rate(request_reservation_seconds_bucket[5m])) by (le))
```

### Request Rate by Operation (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** requests/sec

```promql
sum by (operation) (rate(request_reservation_seconds_count[5m]))
```

### Total Request Rate (Stat)

**Visualization Type:** `Stat`  
**Unit:** requests/sec

```promql
sum(rate(request_reservation_seconds_count[5m]))
```

---

## 🎯 SLO 2: Staff Management Latency (RNF2)

**Target:** 95% of staff management operations must respond within 2 seconds

### SLO Compliance (Gauge)

**Visualization Type:** `Gauge`  
**Thresholds:** Red < 95, Yellow 95-98, Green ≥ 98

```promql
sum(rate(request_staff_management_seconds_bucket{le="2.0"}[5m])) 
/ 
sum(rate(request_staff_management_seconds_count[5m])) * 100
```

### SLO Compliance Over Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Percent (0-100)

```promql
sum(rate(request_staff_management_seconds_bucket{le="2.0"}[5m])) 
/ 
sum(rate(request_staff_management_seconds_count[5m])) * 100
```

### P95 Latency (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Seconds

```promql
histogram_quantile(0.95, sum(rate(request_staff_management_seconds_bucket[5m])) by (le))
```

### Request Rate by Operation (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** requests/sec

```promql
sum by (operation) (rate(request_staff_management_seconds_count[5m]))
```

### Total Request Rate (Stat)

**Visualization Type:** `Stat`  
**Unit:** requests/sec

```promql
sum(rate(request_staff_management_seconds_count[5m]))
```

---

## 🎯 SLO 3: Catalog Latency (RNF3)

**Target:** 95% of catalog browsing operations must respond within 800ms

### SLO Compliance (Gauge)

**Visualization Type:** `Gauge`  
**Thresholds:** Red < 95, Yellow 95-98, Green ≥ 98

```promql
sum(rate(request_catalog_seconds_bucket{le="0.8"}[5m])) 
/ 
sum(rate(request_catalog_seconds_count[5m])) * 100
```

### SLO Compliance Over Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Percent (0-100)

```promql
sum(rate(request_catalog_seconds_bucket{le="0.8"}[5m])) 
/ 
sum(rate(request_catalog_seconds_count[5m])) * 100
```

### P95 Latency (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Seconds

```promql
histogram_quantile(0.95, sum(rate(request_catalog_seconds_bucket[5m])) by (le))
```

### Request Rate by Operation (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** requests/sec

```promql
sum by (operation) (rate(request_catalog_seconds_count[5m]))
```

### Total Request Rate (Stat)

**Visualization Type:** `Stat`  
**Unit:** requests/sec

```promql
sum(rate(request_catalog_seconds_count[5m]))
```

---

## 🎯 SLO 4: Success Rate (RNF4)

**Target:** 99% overall application success rate

### Success Rate (Gauge)

**Visualization Type:** `Gauge`  
**Thresholds:** Red < 99, Yellow 99-99.5, Green ≥ 99.5

```promql
(1 - (
  sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
  / 
  sum(rate(http_server_requests_seconds_count[5m]))
)) * 100
```

### Success Rate Over Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Percent (0-100)

```promql
(1 - (
  sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
  / 
  sum(rate(http_server_requests_seconds_count[5m]))
)) * 100
```

### Error Rate by Status Code (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** requests/sec

```promql
sum by (status) (rate(http_server_requests_seconds_count{status=~"4..|5.."}[5m]))
```

### Total Errors (Stat)

**Visualization Type:** `Stat`  
**Unit:** requests/sec

```promql
sum(rate(http_server_requests_seconds_count{status=~"4..|5.."}[5m]))
```

---

## 🖥️ System Health Metrics

### JVM Heap Usage (Gauge)

**Visualization Type:** `Gauge`  
**Thresholds:** Green < 70, Yellow 70-85, Red ≥ 85  
**Unit:** Percent (0-100)

```promql
sum(jvm_memory_used_bytes{area="heap"}) 
/ 
sum(jvm_memory_max_bytes{area="heap"}) * 100
```

### JVM Heap Over Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Bytes

```promql
jvm_memory_used_bytes{area="heap"}
```

### CPU Usage (Gauge)

**Visualization Type:** `Gauge`  
**Thresholds:** Green < 70, Yellow 70-85, Red ≥ 85  
**Unit:** Percent (0-100)

```promql
process_cpu_usage * 100
```

### CPU Usage Over Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Percent (0-100)

```promql
process_cpu_usage * 100
```

### Active Threads (Stat)

**Visualization Type:** `Stat`  
**Unit:** Short

```promql
jvm_threads_live_threads
```

### GC Pause Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Seconds

```promql
rate(jvm_gc_pause_seconds_sum[5m])
```

### Database Connections - Active (Stat)

**Visualization Type:** `Stat`  
**Unit:** Short

```promql
hikaricp_connections_active
```

### Database Connections Over Time (Time Series)

**Visualization Type:** `Time Series`  
**Unit:** Short

```promql
hikaricp_connections_active
hikaricp_connections_pending
hikaricp_connections_idle
```

### Application Uptime (Stat)

**Visualization Type:** `Stat`  
**Unit:** Duration (hh:mm:ss)

```promql
process_uptime_seconds
```

---

## 📐 Recommended Dashboard Layout

### Row 1: SLO Overview (4 Gauge panels)
| Panel | Query | Type |
|-------|-------|------|
| Reservation SLO | `sum(rate(..._bucket{le="1.0"}...)) / sum(rate(..._count...)) * 100` | Gauge |
| Staff Mgmt SLO | `sum(rate(..._bucket{le="2.0"}...)) / sum(rate(..._count...)) * 100` | Gauge |
| Catalog SLO | `sum(rate(..._bucket{le="0.8"}...)) / sum(rate(..._count...)) * 100` | Gauge |
| Success Rate | `(1 - error_rate) * 100` | Gauge |

### Row 2: Latency Trends (3 Time Series panels)
| Panel | Query | Type |
|-------|-------|------|
| Reservation P95 | `histogram_quantile(0.95, ...)` | Time Series |
| Staff Mgmt P95 | `histogram_quantile(0.95, ...)` | Time Series |
| Catalog P95 | `histogram_quantile(0.95, ...)` | Time Series |

### Row 3: Traffic (2 Time Series panels)
| Panel | Query | Type |
|-------|-------|------|
| Request Rate by SLO | All 3 metrics combined | Time Series |
| Errors by Status | `sum by (status) (...)` | Time Series |

### Row 4: System Health (4 panels)
| Panel | Query | Type |
|-------|-------|------|
| Heap Usage | `jvm_memory_used / jvm_memory_max * 100` | Gauge |
| CPU Usage | `process_cpu_usage * 100` | Gauge |
| DB Connections | `hikaricp_connections_active` | Stat |
| Uptime | `process_uptime_seconds` | Stat |

---

## ⚠️ Important Notes

1. **le (less than or equal to)**: This is a histogram bucket label. `le="1.0"` counts all requests that completed in ≤ 1 second.

2. **rate() vs irate()**: Use `rate()` for smoother graphs (averages over time range), use `irate()` for instant rate (last two data points).

3. **Time Range [5m]**: Adjust based on your needs:
   - `[1m]` - More responsive, noisier
   - `[5m]` - Good balance (recommended)
   - `[15m]` - Smoother, less responsive

4. **No Data Handling**: If queries return `NaN` or no data:
   - Check if the app is running and receiving traffic
   - Verify Prometheus is scraping: http://localhost:9090/targets
   - Check available metrics: http://localhost:8080/actuator/prometheus

5. **Grafana Units**:
   - For percentages: Set unit to `Percent (0-100)`
   - For latency: Set unit to `seconds (s)` or `milliseconds (ms)`
   - For rates: Set unit to `requests/sec` or `ops/sec`
