package com.susang.stellarVision.application.collection.error;

import com.susang.stellarVision.common.exception.BadRequestException;

public class InvalidCollectionSelectionException extends BadRequestException {

    public InvalidCollectionSelectionException(String collectionId) {
        super("Collection", collectionId);
    }
}
