package com.susang.stellarVision.application.collection.repository;

import com.susang.stellarVision.entity.MemberCollection;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberCollectionRepository extends JpaRepository<MemberCollection, Long> {
    @Query("SELECT mc.collection.id FROM MemberCollection mc WHERE mc.member.id = :memberId")
    Set<Long> findCollectionIdsByMemberId(@Param("memberId") Long memberId);

    List<MemberCollection> findByMemberId(Long memberId);

    @Modifying
    @Query("UPDATE MemberCollection mc SET mc.isSelect = false WHERE mc.member.id = :memberId")
    void updateAllSelectionFalseByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("UPDATE MemberCollection mc SET mc.isSelect = true WHERE mc.member.id = :memberId AND mc.collection.id IN :collectionIds")
    void updateSelectionTrue(@Param("memberId") Long memberId, @Param("collectionIds") List<Long> collectionIds);

    @Query("SELECT mc FROM MemberCollection mc JOIN FETCH mc.collection WHERE mc.member.id = :memberId AND mc.isSelect = true")
    List<MemberCollection> findByMemberIdAndIsSelectTrue(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(mc) > 0 FROM MemberCollection mc WHERE mc.member.id = :memberId AND mc.collection.id = :collectionId")
    boolean existsByMemberIdAndCollectionId(@Param("memberId") Long memberId, @Param("collectionId") Long collectionId);

}


