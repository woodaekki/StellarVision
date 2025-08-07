package com.susang.stellarVision.application.photo.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiPhotoRequest {

    @JsonProperty("photoid")
    private Long photoId;

    @JsonProperty("image_url")
    private String imageUrl;
}
