package com.susang.stellarVision.application.photo.error;

import com.susang.stellarVision.common.exception.NotFoundException;

public class PhotoNotFoundException extends NotFoundException {
    public PhotoNotFoundException(String id) {
        super("Photo", id);
    }


}