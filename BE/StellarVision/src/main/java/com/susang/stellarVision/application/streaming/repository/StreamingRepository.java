package com.susang.stellarVision.application.streaming.repository;

import com.susang.stellarVision.entity.StreamingRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamingRepository extends JpaRepository<StreamingRoom, Long>, StreamingRepositoryCustom {

    Optional<StreamingRoom> findBySessionId(String sessionId);

    Optional<StreamingRoom> findByRecordingId(String recordingId);
}
