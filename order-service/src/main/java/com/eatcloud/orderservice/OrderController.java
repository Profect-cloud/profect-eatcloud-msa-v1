package com.eatcloud.orderservice;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping("/health")
    public String health() {
        return "Order Service Connected! ✅";
    }

    @GetMapping
    public String getOrders() {
        return "Order Service is working! 🚀";
    }

    @GetMapping("/me")
    public Map<String, Object> me(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-User-Type", required = false) String userType,
        @RequestHeader(value = "X-Gateway-Validated", required = false) String gatewayValidated
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("userType", userType);
        result.put("gatewayValidated", "true".equalsIgnoreCase(gatewayValidated));
        return result;
    }
}