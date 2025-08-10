package com.susang.stellarVision.application.follow.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.susang.stellarVision.application.follow.dto.FollowMemberDTO;
import com.susang.stellarVision.entity.QFollow;
import com.susang.stellarVision.entity.QMember;
import com.susang.stellarVision.entity.QProfile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FollowMemberDTO> findFollowingMembersByFromMemberId(Long fromMemberId) {
        QFollow follow = QFollow.follow;
        QMember member = QMember.member;
        QProfile profile = QProfile.profile;

        return jpaQueryFactory.select(Projections.constructor(
                        FollowMemberDTO.class,
                        follow.toMember.id,
                        follow.toMember.email,
                        follow.id,
                        follow.toMember.name,
                        profile.profileS3Key,
                        follow.createdAt
                ))
                .from(follow)
                .join(follow.toMember, member)
                .leftJoin(member.profile, profile)
                .where(
                        follow.fromMember.id.eq(fromMemberId),
                        follow.toMember.isDeleted.eq(false)
                )
                .fetch();
    }

    @Override
    public List<FollowMemberDTO> findFollowerMembersByToMemberId(Long toMemberId) {
        QFollow follow = QFollow.follow;
        QMember member = QMember.member;
        QProfile profile = QProfile.profile;

        return jpaQueryFactory.select(Projections.constructor(
                        FollowMemberDTO.class,
                        follow.fromMember.id,
                        follow.fromMember.email,
                        follow.id,
                        follow.fromMember.name,
                        profile.profileS3Key,
                        follow.createdAt
                ))
                .from(follow)
                .join(follow.fromMember, member)
                .leftJoin(member.profile, profile)
                .where(
                        follow.toMember.id.eq(toMemberId),
                        follow.fromMember.isDeleted.eq(false)
                )
                .fetch();
    }
}
