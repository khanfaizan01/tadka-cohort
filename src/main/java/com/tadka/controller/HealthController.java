package com.tadka.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        long start = System.nanoTime();
        entityManager.createNativeQuery("SELECT 1").getSingleResult();
        long elapsed = System.nanoTime() - start;
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("responseTime", elapsed / 1_000_000.0 + "ms");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        long start = System.nanoTime();
        entityManager.createNativeQuery("SELECT 1").getSingleResult();
        long elapsed = System.nanoTime() - start;
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("responseTime", elapsed / 1_000_000.0 + "ms");
        return ResponseEntity.ok(response);
    }
}
