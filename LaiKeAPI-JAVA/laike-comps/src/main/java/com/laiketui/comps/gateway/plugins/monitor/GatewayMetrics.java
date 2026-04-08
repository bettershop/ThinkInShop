package com.laiketui.comps.gateway.plugins.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GatewayMetrics {

//    @Autowired
//    private MeterRegistry registry;

    public void record(String governKey, boolean success, long costMs) {
//        registry.counter("gateway_request_total",
//                        "api", governKey,
//                        "success", String.valueOf(success))
//                .increment();
//
//        registry.timer("gateway_request_latency_ms",
//                        "api", governKey)
//                .record(costMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        log.info(governKey + " success: " + success + " costMs: " + costMs);
    }

}

