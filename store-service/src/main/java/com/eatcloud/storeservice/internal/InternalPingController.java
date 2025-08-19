// store-service
package com.eatcloud.storeservice.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class InternalPingController {

    @GetMapping("/internal/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "store-service",
                "ok", true,
                "ts", Instant.now().toString()
        );
    }
}
