package com.eatcloud.storeservice.domain.store.ranking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/rankings")
public class RankTop3Controller {

    private final RankTop3Service service;

    public RankTop3Controller(RankTop3Service service) { this.service = service; }

    // 전체 카테고리 Top3 묶음
    @GetMapping
    public ResponseEntity<List<RankTop3Response>> getAll() { return ResponseEntity.ok(service.getAll()); }

    // 특정 카테고리 Top3
    @GetMapping("/{categoryId}")
    public ResponseEntity<List<RankTop3Response>> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(service.getByCategory(categoryId));
    }
}