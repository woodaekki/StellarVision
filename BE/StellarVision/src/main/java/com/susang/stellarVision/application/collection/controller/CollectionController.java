package com.susang.stellarVision.application.collection.controller;

import com.susang.stellarVision.application.collection.dto.CollectionListResponse;
import com.susang.stellarVision.application.collection.service.CollectionService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping()
    public ResponseEntity<APIResponse<CollectionListResponse>> getMemberCollections(@AuthenticationPrincipal CustomUserDetails details) {
        Long memberId = details.getMember().getId();
        CollectionListResponse response = collectionService.getMemberCollections(memberId);
        return ResponseEntity.ok(APIResponse.success(response));
    }
}
