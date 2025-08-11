package com.susang.stellarVision.application.collection.service;

import com.susang.stellarVision.application.collection.dto.CollectionIdRequest;
import com.susang.stellarVision.application.collection.dto.CollectionListResponse;
import com.susang.stellarVision.application.collection.dto.CollectionResponse;
import com.susang.stellarVision.application.collection.dto.SelectCollectionRequest;
import com.susang.stellarVision.application.collection.dto.SelectedCollectionListResponse;
import com.susang.stellarVision.application.collection.dto.SelectedCollectionResponse;
import com.susang.stellarVision.application.collection.error.InvalidCollectionSelectionException;
import com.susang.stellarVision.application.collection.repository.CollectionRepository;
import com.susang.stellarVision.application.collection.repository.MemberCollectionRepository;
import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.entity.Collection;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.MemberCollection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final MemberCollectionRepository memberCollectionRepository;
    private final MemberRepository memberRepository;

    @Override
    public CollectionListResponse getMemberCollections(CustomUserDetails details) {

        Long memberId = details.getMember().getId();
        Member member = memberRepository
                .findByIdWithProfile(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));

        List<Collection> all = collectionRepository.findAll();
        Set<Integer> collectedIds = memberCollectionRepository.findCollectionIdsByMemberId(memberId);

        List<CollectionResponse> responses = all.stream()
                .map(c -> CollectionResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .koreanName(c.getKoreanName())
                        .abbreviation(c.getAbbreviation())
                        .collected(collectedIds.contains(c.getId().intValue()))
                        .build())
                .toList();

        return new CollectionListResponse(responses);
    }

    @Override
    public CollectionListResponse getCollectionsByMemberId(Long memberId) {
        Member member = memberRepository
                .findByIdWithProfile(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
        List<MemberCollection> collected = memberCollectionRepository.findByMemberId(memberId);

        List<CollectionResponse> responses = collected.stream()
                .map(mc -> {
                    Collection c = mc.getCollection();
                    return CollectionResponse.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .koreanName(c.getKoreanName())
                            .abbreviation(c.getAbbreviation())
                            .collected(true)
                            .build();
                })
                .toList();

        return new CollectionListResponse(responses);
    }

    @Override
    @Transactional
    public void updateMyBadge(CustomUserDetails userDetails,
            SelectCollectionRequest selectCollectionRequest) {
        Long memberId = userDetails.getMember().getId();

        Set<Long> collectedIds = memberCollectionRepository.findCollectionIdsByMemberId(memberId)
                .stream()
                .map(Integer::longValue)
                .collect(Collectors.toSet());

        List<Long> selectedIds = selectCollectionRequest.getIds()
                .stream()
                .map(CollectionIdRequest::getId)
                .toList();

        for (Long id : selectedIds) {
            if (!collectedIds.contains(id)) {
                throw new InvalidCollectionSelectionException(id.toString());
            }
        }
        memberCollectionRepository.updateAllSelectionFalseByMemberId(memberId);

        memberCollectionRepository.updateSelectionTrue(memberId, selectedIds);


    }

    @Override
    @Transactional(readOnly = true)
    public SelectedCollectionListResponse getBadges(Long memberId) {
        List<MemberCollection> selectedCollections = memberCollectionRepository.findByMemberIdAndIsSelectTrue(memberId);

        List<SelectedCollectionResponse> responses = selectedCollections.stream()
                .map(mc -> SelectedCollectionResponse.builder()
                        .id(mc.getCollection().getId())
                        .name(mc.getCollection().getName())
                        .koreanName(mc.getCollection().getKoreanName())
                        .abbreviation(mc.getCollection().getAbbreviation())
                        .isSelect(true)
                        .build())
                .toList();

        return SelectedCollectionListResponse.builder()
                .selectedCollections(responses)
                .build();
    }
}
