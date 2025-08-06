package com.susang.stellarVision.application.video.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.susang.stellarVision.entity.Video;
import com.susang.stellarVision.entity.QVideo;
import com.susang.stellarVision.entity.QVideoTag;
import com.susang.stellarVision.entity.QMember;
import com.susang.stellarVision.application.video.dto.VideoSearchRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class VideoRepositoryImpl implements VideoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Video> search(VideoSearchRequest condition, Pageable pageable) {
        QVideo video = QVideo.video;
        QVideoTag tag = QVideoTag.videoTag;
        QMember member = QMember.member;

        List<Video> content = queryFactory.selectFrom(video).distinct()
                .leftJoin(video.member, member).fetchJoin().leftJoin(tag).on(tag.video.eq(video))
                .where(anyFieldContains(condition.getKeyword())).orderBy(video.createdAt.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        Long count = queryFactory.select(video.countDistinct()).from(video)
                .leftJoin(video.member, member).leftJoin(tag).on(tag.video.eq(video))
                .where(anyFieldContains(condition.getKeyword())).fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }

    private BooleanExpression anyFieldContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return QVideo.video.title.containsIgnoreCase(keyword)
                .or(QVideoTag.videoTag.tagName.containsIgnoreCase(keyword))
                .or(QMember.member.name.containsIgnoreCase(keyword));
    }
}