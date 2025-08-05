package com.susang.stellarVision.application.photo.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhotoTagListResponse {
    private List<PhotoTagResponse> tags;
}
