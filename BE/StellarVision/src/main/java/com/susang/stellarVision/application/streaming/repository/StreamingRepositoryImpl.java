package com.susang.stellarVision.application.streaming.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.susang.stellarVision.application.streaming.dto.StreamingRoomDTO;
import com.susang.stellarVision.entity.QMember;
import com.susang.stellarVision.entity.QStreamingRoom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StreamingRepositoryImpl implements StreamingRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<StreamingRoomDTO> findStreamingRooms() {
        QStreamingRoom streamingRoom = QStreamingRoom.streamingRoom;
        QMember member = QMember.member;

        return jpaQueryFactory.select(Projections.constructor(
                        StreamingRoomDTO.class,
                        streamingRoom.id,
                        streamingRoom.sessionId,
                        streamingRoom.title,
                        streamingRoom.latitude,
                        streamingRoom.longitude,
                        member.id,
                        member.name,
                        member.email
                ))
                .from(streamingRoom)
                .join(streamingRoom.member, member)
                .where(streamingRoom.isDeleted.isFalse())
                .fetch();
    }
}
