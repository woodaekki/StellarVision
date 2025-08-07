package com.susang.stellarVision.application.collection.error;

import com.susang.stellarVision.common.exception.NotFoundException;

public class CollectionNotFoundException extends NotFoundException {
  public CollectionNotFoundException(String id) {
    super("Collection", id);
  }
}
