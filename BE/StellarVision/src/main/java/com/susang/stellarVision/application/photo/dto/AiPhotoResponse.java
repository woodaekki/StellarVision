package com.susang.stellarVision.application.photo.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiPhotoResponse {
    private Long photoid;
    private List<Prediction> predictions;
}
