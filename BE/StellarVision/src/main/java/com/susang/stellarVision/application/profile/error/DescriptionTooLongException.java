package com.susang.stellarVision.application.profile.error;

import com.susang.stellarVision.common.exception.BadRequestException;

public class DescriptionTooLongException extends BadRequestException {
    public DescriptionTooLongException(String id) {super("DescriptionTooLongException", id);}
}
