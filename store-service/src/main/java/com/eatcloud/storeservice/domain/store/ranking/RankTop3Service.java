package com.eatcloud.storeservice.domain.store.ranking;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankTop3Service {

    private final RankTop3Repository repo;

    public RankTop3Service(RankTop3Repository repo) {
        this.repo = repo;
    }

    public List<RankTop3Response> getByCategory(Integer categoryId) {
        List<RankTop3View> rows = repo.findByCategory(categoryId);
        return rows.stream().map(RankTop3Response::from).toList();
    }

    public List<RankTop3Response> getAll() {
        return repo.findAllRanks().stream().map(RankTop3Response::from).toList();
    }
}
