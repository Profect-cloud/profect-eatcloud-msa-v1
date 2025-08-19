package com.eatcloud.adminservice.internal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// admin-service / internal/AdminInternalProbeController.java
@RestController
@RequestMapping("/internal")
public class AdminInternalProbeController {
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("service","admin-service","ok",true);
    }
}
