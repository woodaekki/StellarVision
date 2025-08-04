package com.susang.stellarVision.application.streaming.repository;

import com.susang.stellarVision.entity.StreamingRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StreamingRepository extends JpaRepository<StreamingRoom, Long> {

    Optional<StreamingRoom> findBySessionId(String sessionId);

    @Query("SELECT r FROM StreamingRoom r JOIN FETCH r.member m WHERE r.isDeleted = false")
    List<StreamingRoom> findAllWithMember();
}
