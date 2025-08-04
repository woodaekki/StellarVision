package com.susang.stellarVision.application.collection.repository;

import com.susang.stellarVision.entity.MemberCollection;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberCollectionRepository extends JpaRepository<MemberCollection, Long> {
    @Query("SELECT mc.collection.id FROM MemberCollection mc WHERE mc.member.id = :memberId")
    Set<Integer> findCollectionIdsByMemberId(@Param("memberId") Long memberId);
}


