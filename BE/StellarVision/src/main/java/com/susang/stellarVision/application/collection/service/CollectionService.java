package com.susang.stellarVision.application.collection.service;

import com.susang.stellarVision.application.collection.dto.CollectionListResponse;
import com.susang.stellarVision.application.collection.dto.SelectCollectionRequest;
import com.susang.stellarVision.application.collection.dto.SelectedCollectionListResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;

public interface CollectionService {

    CollectionListResponse getMemberCollections(CustomUserDetails details);

    CollectionListResponse getCollectionsByMemberId(Long memberId);

    void updateMyBadge(CustomUserDetails userDetails,
            SelectCollectionRequest selectCollectionRequest);

    SelectedCollectionListResponse getBadges(Long memberId);
}
