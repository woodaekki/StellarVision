package com.susang.stellarVision.application.photo.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class PhotoResponseDTO {
    private Long id;
    private String originalFilename;
    private String extension;
    private LocalDateTime createdAt;
    private String downloadUrl;
}