package com.susang.stellarVision.application.collection.service;

import com.susang.stellarVision.application.collection.dto.CollectionListResponse;

public interface CollectionService {
    CollectionListResponse getMemberCollections(Long memberId);
}
