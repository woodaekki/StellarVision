package com.susang.stellarVision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(
        name = "streaming_rooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_SESSION_ID", columnNames = {"session_id"}),
                @UniqueConstraint(name = "UK_RECORDING_ID", columnNames = {"recording_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class StreamingRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "recording_id")
    private String recordingId;

    @Column(name = "recording_url")
    private String recordingUrl;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_member_id", nullable = false)
    private Member member;

    @Builder
    public StreamingRoom(String sessionId, String title, BigDecimal latitude, BigDecimal longitude,
            String recordingId, String recordingUrl, LocalDateTime recordedAt, boolean isDeleted,
            Member member) {
        this.sessionId = sessionId;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordingId = recordingId;
        this.recordingUrl = recordingUrl;
        this.recordedAt = recordedAt;
        this.isDeleted = false;
        this.member = member;
    }

    public void endSession() {
        this.isDeleted = true;
    }

    public void markRecordingStarted(String recordingId) {
        this.recordingId = recordingId;
    }

    public void markRecordingStopped(String recordingUrl, LocalDateTime recordedAt) {
        this.recordingUrl = recordingUrl;
        this.recordedAt = recordedAt;
    }
}
