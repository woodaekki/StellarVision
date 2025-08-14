package com.susang.stellarVision.application.streaming.repository;

import com.susang.stellarVision.application.streaming.dto.StreamingRoomDTO;
import java.util.List;

public interface StreamingRepositoryCustom {

    List<StreamingRoomDTO> findStreamingRooms();
}
