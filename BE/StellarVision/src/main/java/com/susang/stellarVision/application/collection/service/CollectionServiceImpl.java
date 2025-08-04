package com.susang.stellarVision.application.collection.service;

import com.susang.stellarVision.application.collection.dto.CollectionListResponse;
import com.susang.stellarVision.application.collection.dto.CollectionResponse;
import com.susang.stellarVision.application.collection.repository.CollectionRepository;
import com.susang.stellarVision.application.collection.repository.MemberCollectionRepository;
import com.susang.stellarVision.entity.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final MemberCollectionRepository memberCollectionRepository;

    @Override
    public CollectionListResponse getMemberCollections(Long memberId) {
        List<Collection> all = collectionRepository.findAll();
        Set<Integer> collectedIds = memberCollectionRepository.findCollectionIdsByMemberId(memberId);

        List<CollectionResponse> responses = all.stream()
                .map(c -> CollectionResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .koreanName(c.getKoreanName())
                        .abbreviation(c.getAbbreviation())
                        .collected(collectedIds.contains(c.getId()))
                        .build())
                .toList();

        return new CollectionListResponse(responses);
    }

}
